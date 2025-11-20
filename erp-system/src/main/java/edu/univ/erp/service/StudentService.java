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
            throw new Exception("Database Error: " + e.getMessage(), e);
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
        } catch (SQLException e) {
            throw new Exception("Database Error: " + e.getMessage(), e);
        }
    }

    public void drop(User student, int sectionId) throws Exception {
        if (AccessControl.isMaintenanceModeOn()) {
            throw new Exception("System is under maintenance.");
        }

        try (Connection conn = DatabaseFactory.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "DELETE FROM enrollments WHERE student_id = ? AND section_id = ?")) {
            stmt.setInt(1, student.getUserId());
            stmt.setInt(2, sectionId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new Exception("Database Error: " + e.getMessage(), e);
        }
    }

    public List<Map<String, String>> getMySections(User student) throws Exception {
        List<Map<String, String>> list = new ArrayList<>();
        String sql = """
            SELECT s.section_id, c.course_code, c.title, i.full_name, s.days_times, s.room
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
                    list.add(row);
                }
            }
        } catch (SQLException e) {
            throw new Exception("Database Error: " + e.getMessage(), e);
        }
        return list;
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
}
