package edu.univ.erp.data;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public final class DatabaseFactory {

    private static HikariDataSource authDataSource;
    private static HikariDataSource erpDataSource;

    static {
        initAuthDB();
        initErpDB();
    }

    private DatabaseFactory() {
        // Utility class
    }

    private static void initAuthDB() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://localhost:3306/univ_auth");
        config.setUsername("root"); // TODO: set to local DB user
        config.setPassword("password"); // TODO: set to local DB password
        config.setMaximumPoolSize(5);
        authDataSource = new HikariDataSource(config);
    }

    private static void initErpDB() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://localhost:3306/univ_erp");
        config.setUsername("root"); // TODO: set to local DB user
        config.setPassword("password"); // TODO: set to local DB password
        config.setMaximumPoolSize(10);
        erpDataSource = new HikariDataSource(config);
    }

    public static Connection getAuthConnection() throws SQLException {
        return authDataSource.getConnection();
    }

    public static Connection getErpConnection() throws SQLException {
        return erpDataSource.getConnection();
    }
}
