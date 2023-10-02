package com.example.vos;

import com.zaxxer.hikari.HikariDataSource;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.util.UUID;

public class LoginController {
    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button signInButton;

    private HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    @FXML
    private void initialize() {
        passwordField.setOnKeyPressed(event -> {
            if (event.getCode().equals(KeyCode.ENTER)) {
                handleSignInButtonAction();
            }
        });

    }



    @FXML
    private void handleSignInButtonAction() {
        String email = emailField.getText();
        String password = passwordField.getText();

        try (Connection connection = dataSource.getConnection()) {
            String query = "SELECT user_id, user_fname, user_mname, user_lname, user_position FROM user WHERE user_email = ? AND user_password = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, email);
                preparedStatement.setString(2, password);
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {



                    String sessionId = UUID.randomUUID().toString();
                    int userId = resultSet.getInt("user_id");
                    String firstName = resultSet.getString("user_fname");
                    String middleName = resultSet.getString("user_mname");
                    String lastName = resultSet.getString("user_lname");
                    String position = resultSet.getString("user_position");

                    Timestamp expiryTime = new Timestamp(System.currentTimeMillis() + 3600000); // 1 hour in milliseconds


                    // Start the user session and set user details
                    UserSession userSession = UserSession.getInstance();
                    userSession.setSessionId(sessionId);
                    userSession.setUserId(userId);
                    userSession.setUserFirstName(firstName);
                    userSession.setUserMiddleName(middleName);
                    userSession.setUserLastName(lastName);
                    userSession.setUserPosition(position);

                    // Store session ID in the database
                    String insertSessionQuery = "INSERT INTO session (session_id, user_id, expiry_time, created_at, updated_at, session_data) VALUES (?, ?, ?, NOW(), NOW(), ?)";
                    try (PreparedStatement insertSessionStatement = connection.prepareStatement(insertSessionQuery)) {
                        insertSessionStatement.setString(1, sessionId); // Set the session_id
                        insertSessionStatement.setInt(2, userId);
                        insertSessionStatement.setTimestamp(3, expiryTime);
                        insertSessionStatement.setString(4, sessionId);
                        insertSessionStatement.executeUpdate();
                    }


                    loadDashboard(userId);
                } else {
                    showAlert("Login Failed", "Invalid credentials. Please try again.");
                }
            }
        } catch (SQLException e) {
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

    private void loadDashboard(int userId) {
        try {
            // Load the dashboard FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("dashBoard.fxml"));
            Parent root = loader.load();
            DashBoardController dashboardController = loader.getController();

            // Create a new stage for the dashboard
            Stage dashboardStage = new Stage();
            dashboardStage.setTitle("Dashboard");

            // Set the scene for the dashboard stage
            Scene scene = new Scene(root, 800, 600); // Adjust dimensions as needed
            dashboardStage.setScene(scene);

            // Maximize the dashboard stage if desired
            dashboardStage.setMaximized(true);

            // Show the dashboard stage
            dashboardStage.show();

            // Close the login form stage
            ((Stage) signInButton.getScene().getWindow()).close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
