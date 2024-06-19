package com.vertex.vos;

import com.vertex.vos.Constructors.SessionData;
import com.vertex.vos.Constructors.UserSession;
import com.vertex.vos.Utilities.AuditTrailDAO;
import com.vertex.vos.Utilities.AuditTrailEntry;
import com.vertex.vos.Utilities.DatabaseConnectionPool;
import com.vertex.vos.Utilities.DialogUtils;
import com.zaxxer.hikari.HikariDataSource;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Properties;
import java.util.UUID;

public class LoginController {

    @FXML private Button signInButton;
    @FXML private AnchorPane anchorPane;
    @FXML private Label loginFailed;
    @FXML private TextField emailField;
    @FXML private Label headerText;
    @FXML private Label subText;
    @FXML private PasswordField passwordField;

    private static final String CONFIG_FILE_PATH = System.getProperty("user.home") + "/config.properties";
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    @FXML
    private void initialize() {
        loginFailed.setVisible(false);
        Platform.runLater(this::loadSessionIdLocally);
    }

    private void loadSessionIdLocally() {
        Properties properties = new Properties();
        try (InputStream input = new FileInputStream(CONFIG_FILE_PATH)) {
            properties.load(input);
            String sessionId = properties.getProperty("sessionId");
            if (sessionId != null && !sessionId.isEmpty()) {
                try (Connection connection = dataSource.getConnection()) {
                    String query = "SELECT * FROM session WHERE session_id = ? AND expiry_time > NOW()";
                    try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                        preparedStatement.setString(1, sessionId);
                        ResultSet resultSet = preparedStatement.executeQuery();
                        if (resultSet.next()) {
                            getDataFromSession(sessionId);
                        } else {
                            headerText.setText("Welcome back VOS!");
                            subText.setText("What's in our agenda today?");
                        }
                    }
                } catch (SQLException e) {
                    handleError("Failed to load session data", e);
                }
            } else {
                headerText.setText("Welcome to VOS!");
                subText.setText("Sign in and start the day!");
            }
        } catch (IOException e) {
            handleError("Failed to load configuration file", e);
        }
    }

    private void getDataFromSession(String sessionId) {
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
                        authBySession(sessionData.getUserId(), sessionId);
                    }
                }
            }
        } catch (SQLException e) {
            handleError("Failed to fetch session data", e);
        }
    }

    private void authBySession(int userId, String sessionId) {
        try (Connection connection = dataSource.getConnection()) {
            String query = "SELECT * FROM user WHERE user_id = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, userId);
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    startUserSession(userId, sessionId, resultSet);
                }
            }
        } catch (SQLException e) {
            handleError("Authentication failed", e);
        }
    }

    private void startUserSession(int userId, String sessionId, ResultSet resultSet) throws SQLException {
        String firstName = resultSet.getString("user_fname");
        String middleName = resultSet.getString("user_mname");
        String lastName = resultSet.getString("user_lname");
        String position = resultSet.getString("user_position");
        String image = resultSet.getString("user_image");
        int department = resultSet.getInt("user_department");

        UserSession userSession = UserSession.getInstance();
        userSession.setSessionId(sessionId);
        userSession.setUserId(userId);
        userSession.setUserFirstName(firstName);
        userSession.setUserMiddleName(middleName);
        userSession.setUserLastName(lastName);
        userSession.setUserDepartment(department);
        userSession.setUserPosition(position);
        userSession.setUserPic(image);

        AuditTrailEntry authBySessionEntry = new AuditTrailEntry();
        authBySessionEntry.setTimestamp(new Timestamp(System.currentTimeMillis()));
        authBySessionEntry.setUserId(userId);
        authBySessionEntry.setAction("AUTHENTICATION_BY_SESSION");
        authBySessionEntry.setTableName("user");
        authBySessionEntry.setRecordId(userId);
        authBySessionEntry.setFieldName("user_id");
        authBySessionEntry.setOldValue("");
        authBySessionEntry.setNewValue(String.valueOf(userId));

        AuditTrailDAO auditTrailDAO = new AuditTrailDAO();
        auditTrailDAO.insertAuditTrailEntry(authBySessionEntry);

        loadDashboard(userId);
    }

    private void loadDashboard(int userId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("dashBoard.fxml"));
            Parent root = loader.load();
            DashboardController dashboardController = loader.getController();
            Image image = new Image(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/vos.png"));
            Stage dashboardStage = new Stage();
            dashboardStage.setTitle("Vertex ERP");
            dashboardStage.initStyle(StageStyle.UNDECORATED);

            dashboardStage.getIcons().add(image);
            Scene scene = new Scene(root);

            Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
            dashboardStage.setX(primaryScreenBounds.getMinX());
            dashboardStage.setY(primaryScreenBounds.getMinY());
            dashboardStage.setWidth(primaryScreenBounds.getWidth());
            dashboardStage.setHeight(primaryScreenBounds.getHeight());

            dashboardStage.setScene(scene);

            closeLogin();

            dashboardStage.show();

        } catch (IOException e) {
            handleError("Failed to load dashboard", e);
        }
    }

    @FXML
    private void handleSignInButtonAction() {
        String email = emailField.getText();
        String password = passwordField.getText();
        try (Connection connection = dataSource.getConnection()) {
            String query = "SELECT * FROM user WHERE user_email = ? AND user_password = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, email);
                preparedStatement.setString(2, password);
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    String sessionId = UUID.randomUUID().toString();
                    int userId = resultSet.getInt("user_id");
                    startUserSessionAfterLogin(userId, email, sessionId);
                } else if (email.isEmpty()) {
                    loginFailed.setText("Enter email to sign in");
                } else if (password.isEmpty()) {
                    loginFailed.setText("Enter password to sign in");
                } else {
                    loginFailed.setText("Wrong credentials");
                }
            }
        } catch (SQLException e) {
            handleError("Login failed", e);
        }
    }

    private void startUserSessionAfterLogin(int userId, String email, String sessionId) {
        try (Connection connection = dataSource.getConnection()) {
            String insertSessionQuery = "INSERT INTO session (session_id, user_id, expiry_time, created_at, updated_at, session_data) VALUES (?, ?, ?, NOW(), NOW(), ?)";
            try (PreparedStatement insertSessionStatement = connection.prepareStatement(insertSessionQuery)) {
                Timestamp expiryTime = new Timestamp(System.currentTimeMillis() + 3600000 * 8);

                insertSessionStatement.setString(1, sessionId);
                insertSessionStatement.setInt(2, userId);
                insertSessionStatement.setTimestamp(3, expiryTime);
                insertSessionStatement.setString(4, sessionId);

                int rowsAffected = insertSessionStatement.executeUpdate();
                if (rowsAffected > 0) {
                    AuditTrailEntry loginAuditEntry = new AuditTrailEntry();
                    loginAuditEntry.setTimestamp(new Timestamp(System.currentTimeMillis()));
                    loginAuditEntry.setUserId(userId);
                    loginAuditEntry.setAction("LOGIN");
                    loginAuditEntry.setTableName("user");
                    loginAuditEntry.setRecordId(userId);
                    loginAuditEntry.setFieldName("user_email");
                    loginAuditEntry.setOldValue(email);
                    loginAuditEntry.setNewValue(email);

                    AuditTrailDAO auditTrailDAO = new AuditTrailDAO();
                    auditTrailDAO.insertAuditTrailEntry(loginAuditEntry);

                    storeSessionIdLocally(sessionId);
                    loadDashboard(userId);
                } else {
                    DialogUtils.showErrorMessage("Session not stored", "Please try again");
                }
            }
        } catch (SQLException e) {
            handleError("Failed to start user session after login", e);
        }
    }

    private void storeSessionIdLocally(String sessionId) {
        try (OutputStream output = new FileOutputStream(CONFIG_FILE_PATH)) {
            Properties properties = new Properties();
            properties.setProperty("sessionId", sessionId);
            properties.store(output, null);
        } catch (IOException e) {
            handleError("Failed to store session ID locally", e);
        }
    }

    private void closeLogin() {
        Stage loginStage = (Stage) emailField.getScene().getWindow();
        loginStage.close();
    }

    public void setDefaultButton() {
        Platform.runLater(() -> {
            Scene scene = signInButton.getScene();
            scene.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ENTER) {
                    signInButton.fire();
                    event.consume();
                }
            });
        });
    }

    private void handleError(String message, Exception e) {
        e.printStackTrace();
        DialogUtils.showErrorMessage("Error", message + ": " + e.getMessage());
    }
}
