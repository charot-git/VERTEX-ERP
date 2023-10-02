package com.example.vos;

import com.zaxxer.hikari.HikariDataSource;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.UUID;

public class SessionManager {
    private HikariDataSource dataSource; // Your HikariCP data source

    public SessionManager(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }



    public String createSession(int userId) {
        String sessionId = UUID.randomUUID().toString();
        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime expiryTime = currentTime.plusHours(1); // Expiry time is 1 hour from now

        try (Connection connection = dataSource.getConnection()) {
            String insertSessionQuery = "INSERT INTO session (session_id, user_id, expiry_time, created_at, updated_at) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement insertSessionStatement = connection.prepareStatement(insertSessionQuery)) {
                insertSessionStatement.setString(1, sessionId);
                insertSessionStatement.setInt(2, userId);
                insertSessionStatement.setTimestamp(3, Timestamp.valueOf(expiryTime)); // 1 hour session expiry
                insertSessionStatement.setTimestamp(4, Timestamp.valueOf(currentTime)); // created_at remains the same
                insertSessionStatement.setTimestamp(5, Timestamp.valueOf(currentTime)); // updated_at is set to current time
                insertSessionStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return sessionId;
    }
    // Getter and setter for dataSource (optional)
    public HikariDataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }
}
