package edu.univ.erp.service;

import edu.univ.erp.data.DatabaseFactory;
import edu.univ.erp.domain.UserRole;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class AdminService {

    public void registerUser(String username, String rawPassword, UserRole role,
                             String fullName, String extraInfo) throws Exception {
        Connection authConn = null;
        Connection erpConn = null;
        PreparedStatement authStmt = null;
        ResultSet generatedKeys = null;

        try {
            authConn = DatabaseFactory.getAuthConnection();
            erpConn = DatabaseFactory.getErpConnection();

            String authSql = "INSERT INTO users_auth (username, role, password_hash) VALUES (?, ?, ?)";
            authStmt = authConn.prepareStatement(authSql, Statement.RETURN_GENERATED_KEYS);

            String hashed = BCrypt.hashpw(rawPassword, BCrypt.gensalt());

            authStmt.setString(1, username);
            authStmt.setString(2, role.toString());
            authStmt.setString(3, hashed);
            authStmt.executeUpdate();

            generatedKeys = authStmt.getGeneratedKeys();
            if (generatedKeys == null || !generatedKeys.next()) {
                throw new Exception("Failed to create user ID.");
            }
            int newUserId = generatedKeys.getInt(1);

            insertProfile(erpConn, role, newUserId, fullName, extraInfo);

        } catch (SQLException e) {
            e.printStackTrace();
            throw new Exception("Database Error: " + e.getMessage(), e);
        } finally {
            if (generatedKeys != null) {
                try {
                    generatedKeys.close();
                } catch (SQLException ignored) {}
            }
            if (authStmt != null) {
                try {
                    authStmt.close();
                } catch (SQLException ignored) {}
            }
            if (authConn != null) {
                try {
                    authConn.close();
                } catch (SQLException ignored) {}
            }
            if (erpConn != null) {
                try {
                    erpConn.close();
                } catch (SQLException ignored) {}
            }
        }
    }

    private void insertProfile(Connection erpConn, UserRole role, int userId,
                               String fullName, String extraInfo) throws SQLException {
        if (role == UserRole.STUDENT) {
            String studSql = "INSERT INTO students (user_id, full_name, roll_no, year) VALUES (?, ?, ?, ?)";
            try (PreparedStatement studStmt = erpConn.prepareStatement(studSql)) {
                studStmt.setInt(1, userId);
                studStmt.setString(2, fullName);
                studStmt.setString(3, extraInfo);
                studStmt.setInt(4, 2025);
                studStmt.executeUpdate();
            }
        } else if (role == UserRole.INSTRUCTOR) {
            String instSql = "INSERT INTO instructors (user_id, full_name, department) VALUES (?, ?, ?)";
            try (PreparedStatement instStmt = erpConn.prepareStatement(instSql)) {
                instStmt.setInt(1, userId);
                instStmt.setString(2, fullName);
                instStmt.setString(3, extraInfo);
                instStmt.executeUpdate();
            }
        }
    }

    public void createCourse(String code, String title, int credits) throws Exception {
        try (Connection conn = DatabaseFactory.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO courses (course_code, title, credits) VALUES (?, ?, ?)")) {
            stmt.setString(1, code);
            stmt.setString(2, title);
            stmt.setInt(3, credits);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new Exception("Database Error: " + e.getMessage(), e);
        }
    }

    public void setMaintenanceMode(boolean enable) throws Exception {
        try (Connection conn = DatabaseFactory.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE settings SET setting_value = ? WHERE setting_key = 'maintenance_on'")) {
            stmt.setString(1, String.valueOf(enable));
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new Exception("Database Error: " + e.getMessage(), e);
        }
    }
}
