package edu.univ.erp.data;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public final class DatabaseFactory {

    private static HikariDataSource authDataSource;
    private static HikariDataSource erpDataSource;

    static {
        try {
            initAuthDB();
            initErpDB();
        } catch (Exception e) {
            System.err.println("FATAL: Failed to initialize database connections");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            throw new ExceptionInInitializerError(e);
        }
    }

    private DatabaseFactory() {
        // Utility class
    }

    private static void initAuthDB() {
        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:mysql://localhost:3306/univ_auth");
            config.setUsername("root");
            
            String pass = System.getProperty("db.password", "Punya@52");
            config.setPassword(pass);
            
            config.setMaximumPoolSize(5);
            authDataSource = new HikariDataSource(config);
            System.out.println("Auth database connection initialized successfully");
        } catch (Exception e) {
            System.err.println("Failed to initialize Auth database connection");
            throw e;
        }
    }

    private static void initErpDB() {
        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:mysql://localhost:3306/univ_erp");
            config.setUsername("root");
            
            String pass = System.getProperty("db.password", "Punya@52");
            config.setPassword(pass);
            
            config.setMaximumPoolSize(10);
            erpDataSource = new HikariDataSource(config);
            System.out.println("ERP database connection initialized successfully");
        } catch (Exception e) {
            System.err.println("Failed to initialize ERP database connection");
            throw e;
        }
    }

    public static Connection getAuthConnection() throws SQLException {
        return authDataSource.getConnection();
    }

    public static Connection getErpConnection() throws SQLException {
        return erpDataSource.getConnection();
    }
}
