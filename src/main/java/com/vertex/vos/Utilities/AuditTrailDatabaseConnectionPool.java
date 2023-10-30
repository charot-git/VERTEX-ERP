package com.vertex.vos.Utilities;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class AuditTrailDatabaseConnectionPool {
    private static final HikariDataSource auditTrailSource;

    static {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://127.0.0.1:3306/audit_trail_database");
        config.setUsername("root");
        config.setPassword("andrei123");
        // You can configure other HikariCP settings as needed.

        auditTrailSource = new HikariDataSource(config);
    }

    public static HikariDataSource getDataSource() {
        return auditTrailSource;
    }

    public static void testConnection() {
        try (Connection connection = auditTrailSource.getConnection()) {
            if (connection.isValid(5)) { // 5 seconds timeout for connection validation
                System.out.println("Connection is valid.");
            } else {
                System.out.println("Connection is not valid.");
            }
        } catch (SQLException e) {
            System.out.println("Error testing the connection: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        testConnection();
    }
}
