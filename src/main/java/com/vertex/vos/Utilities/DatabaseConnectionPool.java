package com.vertex.vos.Utilities;

import com.vertex.vos.Objects.DatabaseConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseConnectionPool {
    @Getter
    private static final HikariDataSource dataSource;

    static {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(DatabaseConfig.DATABASE_URL + "vos_database");
        config.setUsername(DatabaseConfig.DATABASE_USERNAME);
        config.setPassword(DatabaseConfig.DATABASE_PASSWORD);

        // ✅ Optimized HikariCP settings
        config.setMaximumPoolSize(20); // Adjust based on your needs
        config.setMinimumIdle(5); // Maintain minimum 5 connections in the pool
        config.setIdleTimeout(300000); // 5 minutes before idle connections are removed
        config.setMaxLifetime(1800000); // 30 minutes max lifetime per connection
        config.setConnectionTimeout(20000); // 20 seconds timeout for getting a connection
        config.setLeakDetectionThreshold(45000); // Detect connection leaks after 45 seconds
        config.setAutoCommit(true); // Auto-commit enabled for general use cases

        dataSource = new HikariDataSource(config);
    }

    /**
     * Gets a connection from the pool.
     * Always close the connection after use to avoid exhaustion.
     */
    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * Tests database connection.
     * @return true if successful, false if a connection error occurs.
     */
    public static boolean testConnection() {
        try (Connection connection = getConnection()) {
            return connection.isValid(2); // Validate connection within 2 seconds
        } catch (SQLException e) {
            System.err.println("Database connection test failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Properly closes the HikariCP DataSource when the application shuts down.
     */
    public static void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            System.out.println("HikariCP DataSource has been closed.");
        }
    }
}
