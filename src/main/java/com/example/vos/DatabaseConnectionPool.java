package com.example.vos;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseConnectionPool {
    private static HikariDataSource dataSource;

    static {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://127.0.0.1:3306/vos_database");
        config.setUsername("root");
        config.setPassword("");
        // You can configure other HikariCP settings as needed.

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
