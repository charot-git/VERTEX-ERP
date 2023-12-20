package com.vertex.vos.Utilities;

import com.vertex.vos.Constructors.UserSession;
import com.zaxxer.hikari.HikariDataSource;
import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Properties;

public class LogoutManager {
    private final HikariDataSource dataSource;
    private final HikariDataSource auditTrailSource;

    public LogoutManager(HikariDataSource dataSource, HikariDataSource auditTrailSource) {
        this.dataSource = dataSource;
        this.auditTrailSource = auditTrailSource;
    }

    public void logoutUser(String sessionId, String signOutType) {
        try (Connection connection = dataSource.getConnection()) {
            String deleteSessionQuery = "DELETE FROM session WHERE session_id = ?";
            try (PreparedStatement deleteSessionStatement = connection.prepareStatement(deleteSessionQuery)) {
                deleteSessionStatement.setString(1, sessionId);
                int rowsAffected = deleteSessionStatement.executeUpdate();
                if (rowsAffected > 0) {
                    clearSessionLocally();
                    logAuditTrailEntry(UserSession.getInstance().getUserId(), signOutType, "User logged out.");
                    if (signOutType.equals("TIMEOUT")){
                        showAlert("User Time Out", "You have been inactive for too long.");
                    }
                    else {
                        showAlert("Logged Out", "You have been successfully logged out.");
                    }
                } else {
                    showAlert("Logout Failed", "Failed to logout. Please try again.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "An error occurred during logout. Please try again later.");
        }
    }

    private void logAuditTrailEntry(int userId, String action, String description) {
        try (Connection connection = auditTrailSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "INSERT INTO audit_trail_table (timestamp, user_id, action, table_name, record_id, field_name, old_value, new_value) VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
             )) {

            preparedStatement.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            preparedStatement.setInt(2, userId);
            preparedStatement.setString(3, action);
            preparedStatement.setString(4, "session"); // Table name or action-specific identifier
            preparedStatement.setInt(5, 0); // No record_id for session logout
            preparedStatement.setString(6, "N/A");
            preparedStatement.setString(7, "N/A");
            preparedStatement.setString(8, description);

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception appropriately, e.g., log it or show an error message
        }
    }

    private void clearSessionLocally() {
        // Clear session data in the local file (config.properties)
        try (OutputStream output = new FileOutputStream("config.properties")) {
            Properties properties = new Properties();
            properties.setProperty("sessionId", "");
            properties.store(output, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);

        alert.showAndWait();

    }
}
