package com.vertex.vos.Utilities;

import com.vertex.vos.Constructors.DatabaseConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseConnectionPool {
    private static final HikariDataSource dataSource;

    static {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(DatabaseConfig.DATABASE_URL + "vos_database");
        config.setUsername(DatabaseConfig.DATABASE_USERNAME);
        config.setPassword(DatabaseConfig.DATABASE_PASSWORD);
        config.setConnectionTimeout(5000); // Set connection timeout to 5 seconds
        dataSource = new HikariDataSource(config);
    }

    public static HikariDataSource getDataSource() {
        return dataSource;
    }

    public static boolean testConnection() {
        try (Connection ignored = dataSource.getConnection()) {
            return true; // Connection successful
        } catch (SQLException e) {
            e.printStackTrace();
            return false; // Connection failed
        }
    }
}