package com.vertex.vos.Utilities;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseConnectionPool {
    private static final HikariDataSource dataSource;

    static {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://charot_dev:3306/vos_database");
        config.setUsername("developmentUser");
        config.setPassword("developer");
        dataSource = new HikariDataSource(config);
    }

    public static HikariDataSource getDataSource() {
        return dataSource;
    }

    public static void testConnection() {
        try (Connection connection = dataSource.getConnection()) {
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
    }
}
