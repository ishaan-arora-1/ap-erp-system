package edu.univ.erp.service;

import edu.univ.erp.access.AccessControl;
import edu.univ.erp.data.DatabaseFactory;
import edu.univ.erp.domain.User;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InstructorService {

    public List<Map<String, String>> getMySections(User instructor) throws Exception {
        List<Map<String, String>> sections = new ArrayList<>();
        String sql = "SELECT section_id, course_code, days_times FROM sections WHERE instructor_id = ?";

        try (Connection conn = DatabaseFactory.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, instructor.getUserId());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, String> row = new HashMap<>();
                    row.put("id", String.valueOf(rs.getInt("section_id")));
                    row.put("display", rs.getString("course_code") + " (" + rs.getString("days_times") + ")");
                    sections.add(row);
                }
            }
        } catch (SQLException e) {
            throw new Exception("Database Error: " + e.getMessage(), e);
        }
        return sections;
    }

    public List<Map<String, Object>> getClassList(int sectionId, User instructor) throws Exception {
        if (!isSectionAssignedTo(sectionId, instructor.getUserId())) {
            throw new Exception("Access Denied: You are not the instructor for this section.");
        }

        String sql = """
            SELECT e.enrollment_id, s.full_name, s.roll_no,
                   g.component_name, g.score
            FROM enrollments e
            JOIN students s ON e.student_id = s.user_id
            LEFT JOIN grades g ON e.enrollment_id = g.enrollment_id
            WHERE e.section_id = ?
            ORDER BY s.roll_no
            """;

        Map<Integer, Map<String, Object>> tempMap = new HashMap<>();
        try (Connection conn = DatabaseFactory.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, sectionId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int eid = rs.getInt("enrollment_id");
                    tempMap.putIfAbsent(eid, new HashMap<>());
                    Map<String, Object> student = tempMap.get(eid);

                    student.put("enrollment_id", eid);
                    student.put("name", rs.getString("full_name"));
                    student.put("roll", rs.getString("roll_no"));

                    String comp = rs.getString("component_name");
                    if (comp != null) {
                        student.put(comp, rs.getDouble("score"));
                    }
                }
            }
        } catch (SQLException e) {
            throw new Exception("Database Error: " + e.getMessage(), e);
        }
        return new ArrayList<>(tempMap.values());
    }

    public void saveGrade(int enrollmentId, String component, double score) throws Exception {
        if (AccessControl.isMaintenanceModeOn()) {
            throw new Exception("Maintenance Mode ON: Grading is disabled.");
        }

        String deleteSql = "DELETE FROM grades WHERE enrollment_id = ? AND component_name = ?";
        String insertSql = "INSERT INTO grades (enrollment_id, component_name, score) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseFactory.getErpConnection()) {
            try (PreparedStatement delStmt = conn.prepareStatement(deleteSql)) {
                delStmt.setInt(1, enrollmentId);
                delStmt.setString(2, component);
                delStmt.executeUpdate();
            }
            try (PreparedStatement insStmt = conn.prepareStatement(insertSql)) {
                insStmt.setInt(1, enrollmentId);
                insStmt.setString(2, component);
                insStmt.setDouble(3, score);
                insStmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new Exception("Database Error: " + e.getMessage(), e);
        }
    }

    private boolean isSectionAssignedTo(int sectionId, int instructorId) {
        String sql = "SELECT 1 FROM sections WHERE section_id = ? AND instructor_id = ?";
        try (Connection conn = DatabaseFactory.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, sectionId);
            stmt.setInt(2, instructorId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }

    // --- New Method for CSV Import ---

    public void importGradesFromCSV(File file) throws Exception {
        if (AccessControl.isMaintenanceModeOn()) {
            throw new Exception("Maintenance Mode ON: Import disabled.");
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean isHeader = true;
            int count = 0;

            while ((line = br.readLine()) != null) {
                // Skip the header row
                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                String[] parts = line.split(",");
                // Expected Format: EnrollmentID,Quiz,Midterm,EndSem
                if (parts.length >= 4) {
                    try {
                        int enrollId = Integer.parseInt(parts[0].trim());
                        double quiz = Double.parseDouble(parts[1].trim());
                        double mid = Double.parseDouble(parts[2].trim());
                        double end = Double.parseDouble(parts[3].trim());

                        saveGrade(enrollId, "Quiz", quiz);
                        saveGrade(enrollId, "Midterm", mid);
                        saveGrade(enrollId, "EndSem", end);
                        count++;
                    } catch (NumberFormatException ignored) {
                        // Skip malformed rows
                        System.err.println("Skipping invalid row: " + line);
                    }
                }
            }

            if (count == 0) {
                throw new Exception("No valid grade rows found or file is empty.");
            }
        } catch (Exception e) {
            throw new Exception("Import Error: " + e.getMessage(), e);
        }
    }
}
