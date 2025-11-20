package edu.univ.erp.access;

import edu.univ.erp.data.DatabaseFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public final class AccessControl {

    private static final String MAINT_SQL =
            "SELECT setting_value FROM settings WHERE setting_key = 'maintenance_on'";

    private AccessControl() {
        // Utility class
    }

    public static boolean isMaintenanceModeOn() {
        try (Connection conn = DatabaseFactory.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(MAINT_SQL);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return Boolean.parseBoolean(rs.getString("setting_value"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
