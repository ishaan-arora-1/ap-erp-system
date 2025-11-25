package edu.univ.erp.auth;

import edu.univ.erp.data.DatabaseFactory;
import edu.univ.erp.domain.User;
import edu.univ.erp.domain.UserRole;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthService {

    // Updated query to fetch failed_attempts
    private static final String LOGIN_QUERY =
            "SELECT user_id, role, password_hash, failed_attempts FROM users_auth WHERE username = ? AND status = 'ACTIVE'";

    public User login(String username, String password) throws Exception {
        try (Connection conn = DatabaseFactory.getAuthConnection()) {
            // We use a transaction or specific logic here, but for simplicity, we keep auto-commit
            try (PreparedStatement stmt = conn.prepareStatement(LOGIN_QUERY)) {
                stmt.setString(1, username);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        int userId = rs.getInt("user_id");
                        String dbHash = rs.getString("password_hash");
                        int attempts = rs.getInt("failed_attempts");
                        String roleStr = rs.getString("role");

                        // 1. Check Lockout
                        if (attempts >= 5) {
                            throw new Exception("Account LOCKED due to too many failed attempts. Contact Admin.");
                        }

                        // 2. Verify Password
                        if (BCrypt.checkpw(password, dbHash)) {
                            // Success: Reset attempts and update last login
                            resetFailedAttempts(conn, userId);
                            updateLastLogin(conn, userId);
                            return new User(userId, username, UserRole.valueOf(roleStr));
                        } else {
                            // Failure: Increment attempts
                            incrementFailedAttempts(conn, userId);
                            throw new Exception("Invalid credentials. (Attempt " + (attempts + 1) + "/5)");
                        }
                    } else {
                        throw new Exception("Invalid credentials.");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new Exception("Database error during login.", e);
        }
    }

    // --- NEW: Change Password Method ---
    public void changePassword(int userId, String oldPassword, String newPassword) throws Exception {
        try (Connection conn = DatabaseFactory.getAuthConnection()) {
            // 1. Verify Old Password
            String currentHash = null;
            try (PreparedStatement stmt = conn.prepareStatement("SELECT password_hash FROM users_auth WHERE user_id = ?")) {
                stmt.setInt(1, userId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        currentHash = rs.getString("password_hash");
                    }
                }
            }

            if (currentHash == null || !BCrypt.checkpw(oldPassword, currentHash)) {
                throw new Exception("Incorrect old password.");
            }

            // 2. Update to New Password
            String newHash = BCrypt.hashpw(newPassword, BCrypt.gensalt());
            try (PreparedStatement stmt = conn.prepareStatement("UPDATE users_auth SET password_hash = ? WHERE user_id = ?")) {
                stmt.setString(1, newHash);
                stmt.setInt(2, userId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new Exception("Database Error: " + e.getMessage(), e);
        }
    }

    // --- Helper Methods ---
    private void incrementFailedAttempts(Connection conn, int userId) {
        try (PreparedStatement stmt = conn.prepareStatement("UPDATE users_auth SET failed_attempts = failed_attempts + 1 WHERE user_id = ?")) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void resetFailedAttempts(Connection conn, int userId) {
        try (PreparedStatement stmt = conn.prepareStatement("UPDATE users_auth SET failed_attempts = 0 WHERE user_id = ?")) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateLastLogin(Connection conn, int userId) {
        try (PreparedStatement stmt = conn.prepareStatement("UPDATE users_auth SET last_login = NOW() WHERE user_id = ?")) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
