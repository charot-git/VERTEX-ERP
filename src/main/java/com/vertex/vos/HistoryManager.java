package com.vertex.vos;

import com.vertex.vos.Utilities.DatabaseConnectionPool;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.*;

public class HistoryManager {
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    public int addEntry(String sessionId, String formName) {
        int generatedId = -1;
        try (Connection connection = dataSource.getConnection()) {
            String insertQuery = "INSERT INTO navigation_history (session_id, form_name, timestamp) VALUES (?, ?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {
                preparedStatement.setString(1, sessionId);
                preparedStatement.setString(2, formName);
                preparedStatement.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
                preparedStatement.executeUpdate();

                ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    generatedId = generatedKeys.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception properly
        }
        return generatedId;
    }

    public String navigateBackward(int currentId) {
        String previousForm = null;
        try (Connection connection = dataSource.getConnection()) {
            String selectQuery = "SELECT form_name FROM navigation_history WHERE id < ? ORDER BY id DESC LIMIT 1";
            try (PreparedStatement preparedStatement = connection.prepareStatement(selectQuery)) {
                preparedStatement.setInt(1, currentId);
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    previousForm = resultSet.getString("form_name");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception properly
        }
        return previousForm;
    }


    public String navigateForward(int currentId) {
        String nextForm = null;
        try (Connection connection = dataSource.getConnection()) {
            String selectQuery = "SELECT form_name FROM navigation_history WHERE id > ? ORDER BY id ASC LIMIT 1";
            try (PreparedStatement preparedStatement = connection.prepareStatement(selectQuery)) {
                preparedStatement.setInt(1, currentId);
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    nextForm = resultSet.getString("form_name");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception properly
        }
        return nextForm;
    }

}
