package com.vertex.vos;

import com.vertex.vos.Objects.SessionData;
import com.vertex.vos.Objects.UserSession;
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
import java.sql.*;
import java.util.Base64;
import java.util.Objects;
import java.util.Properties;
import java.util.UUID;

public class LoginController {

    public CheckBox rememberMe;
    @FXML
    private Button signInButton;
    @FXML
    private AnchorPane anchorPane;
    @FXML
    private Label loginFailed;
    @FXML
    private TextField emailField;
    @FXML
    private Label headerText;
    @FXML
    private Label subText;
    @FXML
    private PasswordField passwordField;

    private static final String CONFIG_FILE_PATH = System.getProperty("user.home") + "/config.properties";
    private static final String REMEMBER_ME_FILE_PATH = System.getProperty("user.home") + "/remember.properties";
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    @FXML
    private void initialize() {
        loginFailed.setVisible(false);
        Platform.runLater(this::loadSessionIdLocally);
        loadRememberMePreference();
        setDefaultButton();
    }

    private void loadRememberMePreference() {
        Properties properties = new Properties();
        try (InputStream input = new FileInputStream(REMEMBER_ME_FILE_PATH)) {
            properties.load(input);
            String email = properties.getProperty("email");
            String password = properties.getProperty("password");

            if (email != null && !email.isEmpty() && password != null && !password.isEmpty()) {
                emailField.setText(email);
                passwordField.setText(new String(Base64.getDecoder().decode(password)));
                rememberMe.setSelected(true);
            }
        } catch (IOException e) {
            System.err.println("Failed to load remember me preferences: " + e.getMessage());
        }
    }

    private void saveRememberMePreference(String email, String password) {
        try (OutputStream output = new FileOutputStream(REMEMBER_ME_FILE_PATH)) {
            Properties properties = new Properties();
            properties.setProperty("email", email);
            properties.setProperty("password", Base64.getEncoder().encodeToString(password.getBytes()));
            properties.store(output, null);
        } catch (IOException e) {
            System.err.println("Failed to store remember me preferences: " + e.getMessage());
        }
    }

    private void clearRememberMePreference() {
        try (OutputStream output = new FileOutputStream(REMEMBER_ME_FILE_PATH)) {
            Properties properties = new Properties();
            properties.remove("email");
            properties.remove("password");
            properties.store(output, null);
        } catch (IOException e) {
            System.err.println("Failed to clear remember me preferences: " + e.getMessage());
        }
    }

    private void loadSessionIdLocally() {
        Properties properties = new Properties();
        try (InputStream input = new FileInputStream(CONFIG_FILE_PATH)) {
            properties.load(input);
            String sessionId = properties.getProperty("sessionId");
            if (sessionId != null && !sessionId.isEmpty()) {
                validateSession(sessionId);
            } else {
                setWelcomeMessages("Welcome to VOS!", "Sign in and start the day!");
            }
        } catch (IOException e) {
            handleError("Failed to load configuration file", e);
        }
    }

    private void validateSession(String sessionId) {
        try (Connection connection = dataSource.getConnection()) {
            String query = "SELECT * FROM session WHERE session_id = ? AND expiry_time > NOW()";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, sessionId);
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    getDataFromSession(sessionId);
                } else {
                    setWelcomeMessages("Welcome back VOS!", "What's in our agenda today?");
                }
            }
        } catch (SQLException e) {
            handleError("Failed to validate session", e);
        }
    }

    private void getDataFromSession(String sessionId) {
        try (Connection connection = dataSource.getConnection()) {
            String query = "SELECT user_id, session_id, expiry_time, created_at, updated_at, session_data FROM session WHERE session_id = ? AND expiry_time > NOW()";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, sessionId);
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    Timestamp expiryTime = resultSet.getTimestamp("expiry_time");
                    Timestamp currentTime = new Timestamp(System.currentTimeMillis());
                    if (expiryTime.after(currentTime)) {
                        authBySession(resultSet.getInt("user_id"), sessionId);
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
        UserSession userSession = UserSession.getInstance();
        userSession.setSessionId(sessionId);
        userSession.setUserId(userId);
        userSession.setUserFirstName(resultSet.getString("user_fname"));
        userSession.setUserMiddleName(resultSet.getString("user_mname"));
        userSession.setUserLastName(resultSet.getString("user_lname"));
        userSession.setUserDepartment(resultSet.getInt("user_department"));
        userSession.setUserPosition(resultSet.getString("user_position"));
        userSession.setUserPic(resultSet.getString("user_image"));

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

        loadDashboard();
    }

    private void loadDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("dashBoard.fxml"));
            Parent root = loader.load();
            DashboardController dashboardController = loader.getController();
            Stage dashboardStage = new Stage();
            dashboardStage.setTitle("Vertex ERP");
            dashboardStage.initStyle(StageStyle.UNDECORATED);
            dashboardStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/vos.png"))));
            Scene scene = new Scene(root);
            setFullScreen(dashboardStage, scene);
            closeLogin();
            Platform.runLater(dashboardStage::show);
        } catch (IOException e) {
            handleError("Failed to load dashboard", e);
        }
    }

    private void setFullScreen(Stage stage, Scene scene) {
        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
        stage.setX(primaryScreenBounds.getMinX());
        stage.setY(primaryScreenBounds.getMinY());
        stage.setWidth(primaryScreenBounds.getWidth());
        stage.setHeight(primaryScreenBounds.getHeight());
        stage.setScene(scene);
    }

    @FXML
    private void handleSignInButtonAction() {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.isEmpty()) {
            loginFailed.setText("Enter email to sign in");
            return;
        }
        if (password.isEmpty()) {
            loginFailed.setText("Enter password to sign in");
            return;
        }

        try (Connection connection = dataSource.getConnection()) {
            String query = "SELECT * FROM user WHERE user_email = ? AND user_password = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, email);
                preparedStatement.setString(2, password);
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    String sessionId = UUID.randomUUID().toString();
                    int userId = resultSet.getInt("user_id");
                    handleRememberMePreference(email, password);
                    startUserSessionAfterLogin(userId, email, sessionId, resultSet);
                } else {
                    loginFailed.setText("Invalid email or password");
                    loginFailed.setVisible(true);
                }
            }
        } catch (SQLException e) {
            handleError("Sign-in failed", e);
        }
    }

    private void handleRememberMePreference(String email, String password) {
        if (rememberMe.isSelected()) {
            saveRememberMePreference(email, password);
        } else {
            clearRememberMePreference();
        }
    }

    private void startUserSessionAfterLogin(int userId, String email, String sessionId, ResultSet resultSet) throws SQLException {
        if (createSession(userId, email, sessionId)) {
            UserSession userSession = UserSession.getInstance();
            userSession.setSessionId(sessionId);
            userSession.setUserId(userId);
            userSession.setUserFirstName(resultSet.getString("user_fname"));
            userSession.setUserMiddleName(resultSet.getString("user_mname"));
            userSession.setUserLastName(resultSet.getString("user_lname"));
            userSession.setUserDepartment(resultSet.getInt("user_department"));
            userSession.setUserPosition(resultSet.getString("user_position"));
            userSession.setUserPic(resultSet.getString("user_image"));

            loadDashboard();
        }
    }

    private boolean createSession(int userId, String email, String sessionId) {
        String insertSQL = "INSERT INTO session (user_id, session_id, expiry_time, session_data) VALUES (?, ?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(insertSQL)) {
            preparedStatement.setInt(1, userId);
            preparedStatement.setString(2, sessionId);
            preparedStatement.setTimestamp(3, new Timestamp(System.currentTimeMillis() + 30 * 24 * 60 * 60 * 1000L));
            preparedStatement.setString(4, "");
            preparedStatement.executeUpdate();

            Properties properties = new Properties();
            properties.setProperty("sessionId", sessionId);
            try (OutputStream output = new FileOutputStream(CONFIG_FILE_PATH)) {
                properties.store(output, null);
            } catch (IOException e) {
                handleError("Failed to save session ID locally", e);
            }

            AuditTrailEntry entry = new AuditTrailEntry();
            entry.setUserId(userId);
            entry.setAction("USER_LOGIN");
            entry.setTableName("user");
            entry.setRecordId(userId);
            entry.setFieldName("user_email");
            entry.setOldValue("");
            entry.setNewValue(email);
            entry.setTimestamp(new Timestamp(System.currentTimeMillis()));

            AuditTrailDAO auditTrailDAO = new AuditTrailDAO();
            auditTrailDAO.insertAuditTrailEntry(entry);

            return true;
        } catch (SQLException e) {
            handleError("Failed to create session", e);
            return false;
        }
    }

    private void handleError(String message, Exception e) {
        DialogUtils.showErrorMessage("Error", message + ": " + e.getMessage());
        e.printStackTrace();
    }

    private void setWelcomeMessages(String header, String sub) {
        headerText.setText(header);
        subText.setText(sub);
    }

    private void setDefaultButton() {
        anchorPane.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                signInButton.fire();
            }
        });
    }

    private void closeLogin() {
        Stage stage = (Stage) signInButton.getScene().getWindow();
        stage.close();
    }
}
