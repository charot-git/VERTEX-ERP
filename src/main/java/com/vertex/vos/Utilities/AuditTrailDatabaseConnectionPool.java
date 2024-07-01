package com.vertex.vos.Utilities;

import com.vertex.vos.Objects.DatabaseConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class AuditTrailDatabaseConnectionPool {
    private static final HikariDataSource auditTrailSource;

    static {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(DatabaseConfig.DATABASE_URL + "audit_trail_database");
        config.setUsername(DatabaseConfig.DATABASE_USERNAME);
        config.setPassword(DatabaseConfig.DATABASE_PASSWORD);

        auditTrailSource = new HikariDataSource(config);
    }

    public static HikariDataSource getDataSource() {
        return auditTrailSource;
    }

    public static void testConnection() {
        try (Connection connection = auditTrailSource.getConnection()) {
            if (connection.isValid(5)) {
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
