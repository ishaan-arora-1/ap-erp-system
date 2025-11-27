package edu.univ.erp.data;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * DatabaseFactory - Manages database connections using HikariCP connection pooling
 * 
 * Design Decisions:
 * - Uses TWO separate databases for security: univ_auth and univ_erp
 * - HikariCP provides efficient connection pooling and management
 * - Static initialization ensures connections are ready before first use
 * - Password can be overridden via system property: -Ddb.password="yourpass"
 * 
 * Security: Authentication data (passwords) kept separate from business data
 */
public final class DatabaseFactory {

    // Connection pools - one for each database
    private static HikariDataSource authDataSource;  // Handles authentication queries
    private static HikariDataSource erpDataSource;   // Handles business logic queries

    /**
     * Static initializer block - runs when class is first loaded
     * This ensures database connections are established before any queries
     * If connection fails, application won't start (fail-fast principle)
     */
    static {
        try {
            initAuthDB();
            initErpDB();
        } catch (Exception e) {
            System.err.println("FATAL: Failed to initialize database connections");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            // Throw Error to prevent application from starting with broken DB
            throw new ExceptionInInitializerError(e);
        }
    }

    /**
     * Private constructor prevents instantiation
     * This is a utility class with only static methods
     */
    private DatabaseFactory() {
        // Utility class
    }

    /**
     * Initialize connection pool for Authentication database
     * 
     * Database: univ_auth
     * Contains: users_auth table (usernames, password hashes, roles)
     * Pool Size: 5 connections (smaller since auth queries are fast)
     */
    private static void initAuthDB() {
        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:mysql://localhost:3306/univ_auth");
            config.setUsername("root");
            
            // Password can be overridden: mvn exec:java -Ddb.password="yourpass"
            // Falls back to "Punya@52" if not specified
            String pass = System.getProperty("db.password", "Punya@52");
            config.setPassword(pass);
            
            // Auth queries are fast, so smaller pool is sufficient
            config.setMaximumPoolSize(5);
            authDataSource = new HikariDataSource(config);
            System.out.println("Auth database connection initialized successfully");
        } catch (Exception e) {
            System.err.println("Failed to initialize Auth database connection");
            throw e;
        }
    }

    /**
     * Initialize connection pool for ERP business database
     * 
     * Database: univ_erp
     * Contains: students, instructors, courses, sections, enrollments, grades
     * Pool Size: 10 connections (larger for concurrent business operations)
     */
    private static void initErpDB() {
        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:mysql://localhost:3306/univ_erp");
            config.setUsername("root");
            
            // Same password as auth database
            String pass = System.getProperty("db.password", "Punya@52");
            config.setPassword(pass);
            
            // Larger pool for business operations (registration, grading, etc.)
            config.setMaximumPoolSize(10);
            erpDataSource = new HikariDataSource(config);
            System.out.println("ERP database connection initialized successfully");
        } catch (Exception e) {
            System.err.println("Failed to initialize ERP database connection");
            throw e;
        }
    }

    /**
     * Get a connection from the Auth database pool
     * Used for: Login, password changes, user authentication
     * 
     * @return Connection from the auth pool
     * @throws SQLException if no connection available or database error
     */
    public static Connection getAuthConnection() throws SQLException {
        return authDataSource.getConnection();
    }

    /**
     * Get a connection from the ERP database pool
     * Used for: All business logic (courses, enrollments, grades, etc.)
     * 
     * @return Connection from the ERP pool
     * @throws SQLException if no connection available or database error
     */
    public static Connection getErpConnection() throws SQLException {
        return erpDataSource.getConnection();
    }
}
