package com.example.vos;

import com.zaxxer.hikari.HikariDataSource;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.*;
import java.sql.*;
import java.util.Properties;
import java.util.UUID;

public class LoginController {
    public Button signInButton;
    public AnchorPane anchorPane;
    @FXML
    private TextField emailField;
    @FXML
    private Label headerText;
    @FXML
    private Label subText;
    @FXML
    private PasswordField passwordField;
    private HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    @FXML
    private void initialize() {
        Platform.runLater(this::loadSessionIdLocally);
    }

    private void loadSessionIdLocally() {
        Properties properties = new Properties();
        try (InputStream input = new FileInputStream("config.properties")) {
            properties.load(input);
            String sessionId = properties.getProperty("sessionId");

            // Check if session ID exists
            if (sessionId != null && !sessionId.isEmpty()) {
                try (Connection connection = dataSource.getConnection()) {
                    String query = "SELECT * FROM session WHERE session_id = ? AND expiry_time > NOW()";
                    try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                        preparedStatement.setString(1, sessionId);
                        ResultSet resultSet = preparedStatement.executeQuery();
                        if (resultSet.next()) {
                            getDataFromSession(sessionId);
                        } else {
                            // Session not found or expired, show login prompt
                            headerText.setText("Welcome back VOS!");
                            subText.setText("What's in our agenda today?");
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else {
                headerText.setText("Welcome to VOS!");
                subText.setText("Sign in and start the day!");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private SessionData getDataFromSession(String sessionId) {
        SessionData sessionData = null;
        try (Connection connection = dataSource.getConnection()) {
            String query = "SELECT user_id, session_id, expiry_time, created_at, updated_at, session_data " +
                    "FROM session " +
                    "WHERE session_id = ? AND expiry_time > NOW()";

            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, sessionId);
                ResultSet resultSet = preparedStatement.executeQuery();

                if (resultSet.next()) {
                    sessionData = new SessionData();
                    sessionData.setUserId(resultSet.getInt("user_id"));
                    sessionData.setSessionId(resultSet.getString("session_id"));
                    sessionData.setExpiryTime(resultSet.getTimestamp("expiry_time"));
                    sessionData.setCreatedAt(resultSet.getTimestamp("created_at"));
                    sessionData.setUpdatedAt(resultSet.getTimestamp("updated_at"));
                    sessionData.setSessionData(resultSet.getString("session_data"));

                    Timestamp expiryTime = sessionData.getExpiryTime();
                    Timestamp currentTime = new Timestamp(System.currentTimeMillis());
                    if (expiryTime.after(currentTime)) {
                        // Session is valid, load the dashboard
                        authBySession(sessionData.getUserId(), sessionId);
                    } else {
                        // Session has expired
                    }

                } else {
                    // Session not found or has expired
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sessionData;
    }

    private void authBySession(int userId, String sessionId) {
        try (Connection connection = dataSource.getConnection()) {
            String query = "SELECT * FROM user WHERE user_id = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, userId);
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    String firstName = resultSet.getString("user_fname");
                    String middleName = resultSet.getString("user_mname");
                    String lastName = resultSet.getString("user_lname");
                    String position = resultSet.getString("user_position");

                    // Start the user session and set user details
                    UserSession userSession = UserSession.getInstance();
                    userSession.setSessionId(sessionId);
                    userSession.setUserId(userId);
                    userSession.setUserFirstName(firstName);
                    userSession.setUserMiddleName(middleName);
                    userSession.setUserLastName(lastName);
                    userSession.setUserPosition(position);

                    loadDashboard(userId);


                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void storeSessionIdLocally(String sessionId) {
        // Store session ID in a local file (similar to previous explanation)
        try (OutputStream output = new FileOutputStream("config.properties")) {
            Properties properties = new Properties();
            properties.setProperty("sessionId", sessionId);
            properties.store(output, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                        int rowsAffected = insertSessionStatement.executeUpdate();
                        if (rowsAffected > 0) {
                            storeSessionIdLocally(sessionId);
                            loadDashboard(userId);

                        } else {
                            showAlert("Session not stored", "Please try again");
                        }

                    }

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
            dashboardStage.setTitle("Vertex");
            dashboardStage.initStyle(StageStyle.UNDECORATED);
            // Set the scene for the dashboard stage
            Scene scene = new Scene(root); // Adjust dimensions as needed
            dashboardStage.setScene(scene);

            closeLogin();

            // Maximize the dashboard stage if desired
            dashboardStage.setMaximized(true);
            // Show the dashboard stage
            dashboardStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closeLogin() {
        Stage close = (Stage) emailField.getScene().getWindow();
        close.close();
    }

    public void setDefaultButton() {
        Platform.runLater(() -> {
            // Set signInButton as the default button for the scene
            Scene scene = signInButton.getScene();
            scene.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ENTER) {
                    signInButton.fire();
                    event.consume();
                }
            });
        });
    }

}
