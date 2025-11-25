package edu.univ.erp.service;

import edu.univ.erp.data.DatabaseFactory;
import edu.univ.erp.domain.UserRole;
import org.mindrot.jbcrypt.BCrypt;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    // 1. Helper to fetch all courses for the dropdown
    public List<Map<String, String>> getAllCourses() throws Exception {
        List<Map<String, String>> list = new ArrayList<>();
        try (Connection conn = DatabaseFactory.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT course_code, title FROM courses")) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, String> map = new HashMap<>();
                    map.put("code", rs.getString("course_code"));
                    map.put("title", rs.getString("title"));
                    list.add(map);
                }
            }
        } catch (SQLException e) {
            throw new Exception("Error fetching courses: " + e.getMessage(), e);
        }
        return list;
    }

    // 2. Helper to fetch all instructors for the dropdown
    public List<Map<String, String>> getAllInstructors() throws Exception {
        List<Map<String, String>> list = new ArrayList<>();
        try (Connection conn = DatabaseFactory.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT user_id, full_name FROM instructors")) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, String> map = new HashMap<>();
                    map.put("id", String.valueOf(rs.getInt("user_id")));
                    map.put("name", rs.getString("full_name"));
                    list.add(map);
                }
            }
        } catch (SQLException e) {
            throw new Exception("Error fetching instructors: " + e.getMessage(), e);
        }
        return list;
    }

    // 3. The main method to create a section
    public void createSection(String courseCode, int instructorId, String dayTime, String room, int capacity) throws Exception {
        String sql = "INSERT INTO sections (course_code, instructor_id, days_times, room, capacity) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseFactory.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, courseCode);
            stmt.setInt(2, instructorId);
            stmt.setString(3, dayTime);
            stmt.setString(4, room);
            stmt.setInt(5, capacity);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new Exception("Database Error: " + e.getMessage(), e);
        }
    }

    // --- Backup & Restore Bonus ---

    public void backupDB(String filePath) throws Exception {
        String dbName = "univ_erp"; 
        String dbUser = "root";
        String dbPass = "Punya@52"; // Matches your DatabaseFactory config

        // --- CHANGE: Removed hardcoded path ---
        String mysqldumpCmd = "mysqldump";

        // Command: mysqldump -u root -pPunya@52 --set-gtid-purged=OFF --databases univ_erp -r "path/to/file.sql"
        List<String> commands = new ArrayList<>();
        commands.add(mysqldumpCmd);
        commands.add("-u");
        commands.add(dbUser);
        commands.add("-p" + dbPass);
        commands.add("--set-gtid-purged=OFF"); // Prevent GTID conflicts
        commands.add("--databases");
        commands.add(dbName);
        commands.add("-r");
        commands.add(filePath);

        ProcessBuilder pb = new ProcessBuilder(commands);
        pb.redirectErrorStream(true); // Merge stderr into stdout
        Process process = pb.start();
        
        // Capture output for debugging
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }
        
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            String errorMsg = output.toString().trim();
            if (errorMsg.isEmpty()) {
                errorMsg = "Unknown error occurred";
            }
            throw new Exception("Backup failed: " + errorMsg);
        }
    }

    public void restoreDB(String filePath) throws Exception {
        String dbUser = "root";
        String dbPass = "Punya@52";

        // --- CHANGE: Removed hardcoded path ---
        String mysqlCmd = "mysql";

        // Command: mysql -u root -pPunya@52 < "path/to/file.sql"
        List<String> commands = new ArrayList<>();
        commands.add(mysqlCmd);
        commands.add("-u");
        commands.add(dbUser);
        commands.add("-p" + dbPass);

        ProcessBuilder pb = new ProcessBuilder(commands);
        pb.redirectErrorStream(true); // Merge stderr into stdout
        // This effectively does the "< file.sql" part
        pb.redirectInput(new File(filePath));

        Process process = pb.start();
        
        // Capture output for debugging
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }
        
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            String errorMsg = output.toString().trim();
            if (errorMsg.isEmpty()) {
                errorMsg = "Unknown error occurred";
            }
            throw new Exception("Restore failed: " + errorMsg);
        }
    }
}
