package edu.univ.erp.service;

import edu.univ.erp.access.AccessControl;
import edu.univ.erp.data.DatabaseFactory;
import edu.univ.erp.domain.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StudentService {

    private void notify(User user, String msg) {
        NotificationService.addNotification(user, msg);
    }

    public List<Map<String, String>> getCourseCatalog() throws Exception {
        List<Map<String, String>> catalog = new ArrayList<>();
        String sql = """
            SELECT s.section_id, c.course_code, c.title, c.credits,
                   i.full_name as instructor, s.days_times, s.room, s.capacity,
                   (SELECT COUNT(*) FROM enrollments e WHERE e.section_id = s.section_id) as enrolled_count
            FROM sections s
            JOIN courses c ON s.course_code = c.course_code
            JOIN instructors i ON s.instructor_id = i.user_id
            """;

        try (Connection conn = DatabaseFactory.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Map<String, String> row = new HashMap<>();
                row.put("id", String.valueOf(rs.getInt("section_id")));
                row.put("code", rs.getString("course_code"));
                row.put("title", rs.getString("title"));
                row.put("credits", rs.getString("credits"));
                row.put("instructor", rs.getString("instructor"));
                row.put("time", rs.getString("days_times"));

                int cap = rs.getInt("capacity");
                int filled = rs.getInt("enrolled_count");
                row.put("seats", (cap - filled) + " / " + cap);

                catalog.add(row);
            }
        } catch (SQLException e) {
            throw new Exception("Unable to load course catalog. Please try again.");
        }
        return catalog;
    }

    public void register(User student, int sectionId) throws Exception {
        if (AccessControl.isMaintenanceModeOn()) {
            throw new Exception("System is under maintenance. Changes are currently disabled.");
        }

        try (Connection conn = DatabaseFactory.getErpConnection()) {
            ensureNotDuplicate(conn, student.getUserId(), sectionId);
            ensureCapacity(conn, sectionId);
            insertEnrollment(conn, student.getUserId(), sectionId);

            // Get course name for notification
            String courseName = getCourseName(conn, sectionId);
            notify(student, "Registered for course: " + courseName);
        } catch (SQLException e) {
            throw new Exception("Unable to complete registration. Please try again.");
        }
    }
    
    private String getCourseName(Connection conn, int sectionId) throws SQLException {
        String sql = "SELECT c.course_code, c.title FROM sections s JOIN courses c ON s.course_code = c.course_code WHERE s.section_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, sectionId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("course_code") + " - " + rs.getString("title");
                }
            }
        }
        return "Unknown Course";
    }
    
    private java.time.LocalDate getSectionDropDeadline(Connection conn, int sectionId) throws SQLException {
        String sql = "SELECT drop_deadline FROM sections WHERE section_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, sectionId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    java.sql.Date sqlDate = rs.getDate("drop_deadline");
                    if (sqlDate != null) {
                        return sqlDate.toLocalDate();
        }
                }
            }
        }
        return null; // No deadline set means no restriction
    }

    public void drop(User student, int sectionId) throws Exception {
        if (AccessControl.isMaintenanceModeOn()) {
            throw new Exception("System is under maintenance.");
        }

        try (Connection conn = DatabaseFactory.getErpConnection()) {
            // Check section-specific drop deadline
            java.time.LocalDate deadline = getSectionDropDeadline(conn, sectionId);
            if (deadline != null && java.time.LocalDate.now().isAfter(deadline)) {
                throw new Exception("The drop deadline (" + deadline + ") has passed for this course.");
            }
            
            // Get course name for notification before dropping
            String courseName = getCourseName(conn, sectionId);
            
            // First, get the enrollment_id
            int enrollmentId = -1;
            String findEnrollment = "SELECT enrollment_id FROM enrollments WHERE student_id = ? AND section_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(findEnrollment)) {
                stmt.setInt(1, student.getUserId());
                stmt.setInt(2, sectionId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        enrollmentId = rs.getInt("enrollment_id");
                    } else {
                        throw new Exception("You are not enrolled in this section.");
                    }
                }
            }

            // Delete any grades associated with this enrollment
            String deleteGrades = "DELETE FROM grades WHERE enrollment_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deleteGrades)) {
                stmt.setInt(1, enrollmentId);
                stmt.executeUpdate();
            }

            // Now delete the enrollment
            String deleteEnrollment = "DELETE FROM enrollments WHERE enrollment_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deleteEnrollment)) {
                stmt.setInt(1, enrollmentId);
                stmt.executeUpdate();
            }

            notify(student, "Dropped course: " + courseName);
        } catch (SQLException e) {
            throw new Exception("Unable to drop this course. Please try again.");
        }
    }

    public List<Map<String, String>> getMySections(User student) throws Exception {
        List<Map<String, String>> list = new ArrayList<>();
        String sql = """
            SELECT s.section_id, c.course_code, c.title, i.full_name, s.days_times, s.room, s.drop_deadline
            FROM enrollments e
            JOIN sections s ON e.section_id = s.section_id
            JOIN courses c ON s.course_code = c.course_code
            JOIN instructors i ON s.instructor_id = i.user_id
            WHERE e.student_id = ?
            """;

        try (Connection conn = DatabaseFactory.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, student.getUserId());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, String> row = new HashMap<>();
                    row.put("id", String.valueOf(rs.getInt("section_id")));
                    row.put("display", rs.getString("course_code") + ": " + rs.getString("title"));
                    row.put("info", rs.getString("days_times") + " (" + rs.getString("room") + ")");
                    row.put("instructor", rs.getString("full_name"));
                    
                    // Format drop deadline
                    java.sql.Date deadline = rs.getDate("drop_deadline");
                    if (deadline != null) {
                        row.put("deadline", deadline.toString());
                    } else {
                        row.put("deadline", "No deadline");
                    }
                    
                    list.add(row);
                }
            }
        } catch (SQLException e) {
            throw new Exception("Unable to load your schedule. Please refresh and try again.");
        }
        return list;
    }

    public Map<String, Double> getGrades(User student, int sectionId) throws Exception {
        Map<String, Double> grades = new HashMap<>();
        // Query joins enrollments to find the correct enrollment_id, then gets grades
        String sql = """
            SELECT g.component_name, g.score
            FROM grades g
            JOIN enrollments e ON g.enrollment_id = e.enrollment_id
            WHERE e.student_id = ? AND e.section_id = ?
            """;
        
        try (Connection conn = DatabaseFactory.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, student.getUserId());
            stmt.setInt(2, sectionId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    grades.put(rs.getString("component_name"), rs.getDouble("score"));
                }
            }
        } catch (SQLException e) {
            throw new Exception("Unable to load grades. Please try again.");
        }
        return grades;
    }

    private void ensureNotDuplicate(Connection conn, int studentId, int sectionId) throws SQLException, Exception {
        String checkDup = "SELECT 1 FROM enrollments WHERE student_id = ? AND section_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(checkDup)) {
            stmt.setInt(1, studentId);
            stmt.setInt(2, sectionId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    throw new Exception("You are already registered for this section.");
                }
            }
        }
    }

    private void ensureCapacity(Connection conn, int sectionId) throws SQLException, Exception {
        String checkCap = "SELECT capacity, (SELECT COUNT(*) FROM enrollments WHERE section_id = ?) as filled FROM sections WHERE section_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(checkCap)) {
            stmt.setInt(1, sectionId);
            stmt.setInt(2, sectionId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    if (rs.getInt("filled") >= rs.getInt("capacity")) {
                        throw new Exception("Section is full.");
                    }
                }
            }
        }
    }

    private void insertEnrollment(Connection conn, int studentId, int sectionId) throws SQLException {
        String insert = "INSERT INTO enrollments (student_id, section_id) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(insert)) {
            stmt.setInt(1, studentId);
            stmt.setInt(2, sectionId);
            stmt.executeUpdate();
        }
    }

    public List<Map<String, String>> getTranscriptData(User student) throws Exception {
        List<Map<String, String>> transcript = new ArrayList<>();
        
        // Get all sections the student is enrolled in
        String sql = """
            SELECT e.enrollment_id, c.course_code, c.title
            FROM enrollments e
            JOIN sections s ON e.section_id = s.section_id
            JOIN courses c ON s.course_code = c.course_code
            WHERE e.student_id = ?
            """;

        try (Connection conn = DatabaseFactory.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, student.getUserId());
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int enrollId = rs.getInt("enrollment_id");
                    String code = rs.getString("course_code");
                    String title = rs.getString("title");

                    // Fetch grades for this specific enrollment to calculate final score
                    Map<String, Double> grades = getGradesForEnrollment(conn, enrollId);
                    
                    double quiz = grades.getOrDefault("Quiz", 0.0);
                    double mid = grades.getOrDefault("Midterm", 0.0);
                    double end = grades.getOrDefault("EndSem", 0.0);
                    double finalGrade = (quiz * 0.2) + (mid * 0.3) + (end * 0.5);

                    Map<String, String> record = new HashMap<>();
                    record.put("code", code);
                    record.put("title", title);
                    record.put("grade", String.format("%.2f", finalGrade));
                    
                    transcript.add(record);
                }
            }
        } catch (SQLException e) {
            throw new Exception("Unable to load transcript data. Please try again.");
        }
        return transcript;
    }

    // Helper to get grades reusing an existing connection
    private Map<String, Double> getGradesForEnrollment(Connection conn, int enrollmentId) throws SQLException {
        Map<String, Double> grades = new HashMap<>();
        String sql = "SELECT component_name, score FROM grades WHERE enrollment_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, enrollmentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    grades.put(rs.getString("component_name"), rs.getDouble("score"));
                }
            }
        }
        return grades;
    }
}
