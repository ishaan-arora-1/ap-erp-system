package edu.univ.erp.auth;

import edu.univ.erp.data.DatabaseFactory;
import edu.univ.erp.domain.User;
import edu.univ.erp.domain.UserRole;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * AuthService - Handles all authentication-related operations
 * 
 * Security Features:
 * - BCrypt password hashing (never stores plain-text passwords)
 * - Temporary account lockout: 10 minutes after 5 failed login attempts
 * - Automatic unlock: Lockout expires after 10 minutes
 * - Password verification without timing attacks (BCrypt handles this)
 * - Tracks last login time for audit purposes
 * - Allows users to change their own passwords
 */
public class AuthService {

    /**
     * SQL query to fetch user authentication data
     * Only active users can log in (status = 'ACTIVE')
     * Fetches: user_id, role, password_hash, failed_attempts, lockout_until
     */
    private static final String LOGIN_QUERY =
            "SELECT user_id, role, password_hash, failed_attempts, lockout_until FROM users_auth WHERE username = ? AND status = 'ACTIVE'";

    /**
     * Authenticate a user with username and password
     * 
     * Security Flow:
     * 1. Look up user by username (only ACTIVE status users)
     * 2. Check if account is temporarily locked (lockout expires after 10 minutes)
     * 3. If lockout expired, clear it automatically
     * 4. Verify password using BCrypt (secure comparison)
     * 5. On success: Reset failed attempts and lockout, update last login
     * 6. On failure: Increment failed attempts, lock account after 5th attempt
     * 
     * @param username The username to authenticate
     * @param password The plain-text password (will be compared to BCrypt hash)
     * @return User object if authentication succeeds
     * @throws Exception if credentials invalid, account locked, or database error
     */
    public User login(String username, String password) throws Exception {
        try (Connection conn = DatabaseFactory.getAuthConnection()) {
            // Using try-with-resources for automatic connection cleanup
            try (PreparedStatement stmt = conn.prepareStatement(LOGIN_QUERY)) {
                // Prevent SQL injection by using prepared statement
                stmt.setString(1, username);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        // User found - extract authentication data
                        int userId = rs.getInt("user_id");
                        String dbHash = rs.getString("password_hash");  // BCrypt hash from DB
                        int attempts = rs.getInt("failed_attempts");
                        String roleStr = rs.getString("role");
                        java.sql.Timestamp lockoutUntil = rs.getTimestamp("lockout_until");

                        // 1. Check if account is currently locked
                        if (lockoutUntil != null) {
                            long now = System.currentTimeMillis();
                            long lockoutTime = lockoutUntil.getTime();
                            
                            if (now < lockoutTime) {
                                // Still locked - calculate remaining time
                                long remainingMs = lockoutTime - now;
                                long remainingMinutes = (remainingMs / 1000 / 60) + 1; // Round up
                                throw new Exception("Account temporarily locked. Try again in " + remainingMinutes + " minute(s).");
                            } else {
                                // Lockout period expired - automatically clear it
                                clearLockout(conn, userId);
                            }
                        }

                        // 2. Verify password against BCrypt hash
                        // BCrypt.checkpw handles timing-attack-safe comparison
                        if (BCrypt.checkpw(password, dbHash)) {
                            // SUCCESS: Password is correct
                            resetFailedAttemptsAndLockout(conn, userId);
                            updateLastLogin(conn, userId);
                            return new User(userId, username, UserRole.valueOf(roleStr));
                        } else {
                            // FAILURE: Wrong password
                            int newAttempts = attempts + 1;
                            
                            if (newAttempts >= 5) {
                                // 5th failed attempt - lock account for 10 minutes
                                lockAccount(conn, userId, 10);
                                throw new Exception("Too many failed attempts. Account locked for 10 minutes.");
                            } else {
                                // Increment attempt counter
                                incrementFailedAttempts(conn, userId);
                                throw new Exception("Invalid credentials. (Attempt " + newAttempts + "/5)");
                            }
                        }
                    } else {
                        // Username not found or account not active
                        throw new Exception("Invalid credentials.");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new Exception("Database error during login.", e);
        }
    }

    /**
     * Allow user to change their password
     * 
     * Security:
     * - Requires old password verification (prevents unauthorized changes)
     * - New password is hashed with BCrypt before storage
     * - BCrypt auto-generates salt for each password
     * 
     * @param userId The user's ID
     * @param oldPassword Current password (must match for change to succeed)
     * @param newPassword New password (will be BCrypt hashed)
     * @throws Exception if old password wrong or database error
     */
    public void changePassword(int userId, String oldPassword, String newPassword) throws Exception {
        try (Connection conn = DatabaseFactory.getAuthConnection()) {
            // 1. Verify Old Password - security check to prevent unauthorized changes
            String currentHash = null;
            try (PreparedStatement stmt = conn.prepareStatement("SELECT password_hash FROM users_auth WHERE user_id = ?")) {
                stmt.setInt(1, userId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        currentHash = rs.getString("password_hash");
                    }
                }
            }

            // Verify old password matches
            if (currentHash == null || !BCrypt.checkpw(oldPassword, currentHash)) {
                throw new Exception("Incorrect old password.");
            }

            // 2. Hash new password and update database
            // BCrypt.gensalt() generates a unique salt for this password
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

    /**
     * Increment the failed login attempts counter
     * Called when user enters wrong password (before 5th attempt)
     */
    private void incrementFailedAttempts(Connection conn, int userId) {
        try (PreparedStatement stmt = conn.prepareStatement("UPDATE users_auth SET failed_attempts = failed_attempts + 1 WHERE user_id = ?")) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Lock account for specified number of minutes
     * Sets lockout_until to current time + minutes
     * Also increments failed_attempts counter
     * 
     * @param conn Database connection
     * @param userId User to lock
     * @param minutes How many minutes to lock for
     */
    private void lockAccount(Connection conn, int userId, int minutes) {
        try (PreparedStatement stmt = conn.prepareStatement(
                "UPDATE users_auth SET failed_attempts = failed_attempts + 1, lockout_until = DATE_ADD(NOW(), INTERVAL ? MINUTE) WHERE user_id = ?")) {
            stmt.setInt(1, minutes);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Clear lockout without resetting failed attempts
     * Called when lockout period has expired
     */
    private void clearLockout(Connection conn, int userId) {
        try (PreparedStatement stmt = conn.prepareStatement("UPDATE users_auth SET lockout_until = NULL WHERE user_id = ?")) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Reset failed attempts counter and clear any lockout
     * Called when user successfully logs in
     */
    private void resetFailedAttemptsAndLockout(Connection conn, int userId) {
        try (PreparedStatement stmt = conn.prepareStatement("UPDATE users_auth SET failed_attempts = 0, lockout_until = NULL WHERE user_id = ?")) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Update last login timestamp
     * Used for audit purposes and user activity tracking
     */
    private void updateLastLogin(Connection conn, int userId) {
        try (PreparedStatement stmt = conn.prepareStatement("UPDATE users_auth SET last_login = NOW() WHERE user_id = ?")) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
