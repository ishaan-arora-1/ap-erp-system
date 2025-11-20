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

    private static final String LOGIN_QUERY =
            "SELECT user_id, role, password_hash FROM users_auth WHERE username = ? AND status = 'ACTIVE'";

    public User login(String username, String password) throws Exception {
        try (Connection conn = DatabaseFactory.getAuthConnection();
             PreparedStatement stmt = conn.prepareStatement(LOGIN_QUERY)) {

            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String dbHash = rs.getString("password_hash");

                    if (BCrypt.checkpw(password, dbHash)) {
                        int userId = rs.getInt("user_id");
                        String roleStr = rs.getString("role");

                        updateLastLogin(userId);

                        return new User(userId, username, UserRole.valueOf(roleStr));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new Exception("Database error during login.", e);
        }

        return null;
    }

    private void updateLastLogin(int userId) {
        String update = "UPDATE users_auth SET last_login = NOW() WHERE user_id = ?";
        try (Connection conn = DatabaseFactory.getAuthConnection();
             PreparedStatement stmt = conn.prepareStatement(update)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Could not update last login: " + e.getMessage());
        }
    }
}
