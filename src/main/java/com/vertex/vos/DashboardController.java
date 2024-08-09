package com.vertex.vos;

import com.vertex.vos.Objects.SharedFunctions;
import com.vertex.vos.Objects.Taskbar;
import com.vertex.vos.Objects.UserSession;
import com.vertex.vos.Utilities.*;
import com.zaxxer.hikari.HikariDataSource;
import javafx.animation.RotateTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.*;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.*;

public class DashboardController implements Initializable {
    private Timer logoutTimer;
    public ImageView logoutButton, vosIcon;
    @FXML
    public AnchorPane parentPane;
    public ImageView forwardForm;
    public ImageView backForm;
    @FXML
    ImageView employeeProfile;
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    private final HikariDataSource auditTrailSource = AuditTrailDatabaseConnectionPool.getDataSource();

    private final LogoutManager logoutManager = new LogoutManager(dataSource, auditTrailSource);
    private final HistoryManager historyManager = new HistoryManager();
    private int currentNavigationId = -1; // Initialize to a default value


    @FXML
    private TextField searchBar;

    @FXML
    private HBox profileBox;
    @FXML
    private Label nameText, positionText;
    private double xOffset, yOffset;
    @FXML
    private VBox chatNavigation;
    @FXML
    private VBox adminNavigation;
    @FXML
    private VBox accountingNavigation;
    @FXML
    private VBox iOPSNavigation;
    @FXML
    private VBox eOPSNavigation;
    @FXML
    private VBox financialReportsNavigation;
    @FXML
    private VBox calendarNavigation;
    @FXML
    private VBox logoutNavigation;
    @FXML
    private AnchorPane navigationPane;
    @FXML
    private VBox navigationBox;
    @FXML
    private AnchorPane contentPane;
    @FXML
    private VBox closeBox;
    @FXML
    private VBox maximizeBox;
    @FXML
    private VBox minimizeBox;
    @FXML
    private Button forwardButton;
    @FXML
    private Button backButton;
    @FXML
    private ImageView cog;
    @FXML
    private Rectangle profileHover;

    private TranslateTransition slideInTransition;
    private TranslateTransition slideOutTransition;
    @FXML
    private HBox profileContainer;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initializeUIProperties();
        String lastPageLoaded = historyManager.getLastForm(UserSession.getInstance().getSessionId());

        if (lastPageLoaded.equals("tableManager.fxml")) {
            loadContent("ChatContent.fxml", true);
        } else {
            loadLastPage();
        }
        startUserActivityTracking();
    }

    private void startUserActivityTracking() {
        logoutTimer = new Timer();
        final long inactivityDuration = 30 * 60 * 1000;
        parentPane.setOnMouseClicked(mouseEvent -> resetLogoutTimer());
        logoutTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    logoutManager.logoutUser(UserSession.getInstance().getSessionId(), "TIMEOUT");
                    System.exit(0);
                    showLogin();
                });
            }
        }, inactivityDuration);
    }

    private void resetLogoutTimer() {
        if (logoutTimer != null) {
            logoutTimer.cancel();
            startUserActivityTracking(); // Restart the timer on user activity
        }
    }


    private void loadLastPage() {
        String lastPageLoaded = historyManager.getLastForm(UserSession.getInstance().getSessionId());
        if (!lastPageLoaded.isEmpty()) {
            loadContent(lastPageLoaded, true);
        }
    }

    private void initializeUIProperties() {
        ImageCircle.circular(employeeProfile);

        profileContainer.setTranslateX(400);

        slideInTransition = new TranslateTransition(Duration.seconds(0.3), profileContainer);
        slideInTransition.setToX(0);

        slideOutTransition = new TranslateTransition(Duration.seconds(0.3), profileContainer);
        slideOutTransition.setToX(400);

        // Set up the event handlers for hover
        profileHover.setOnMouseEntered(event -> {
            slideInTransition.play(); // Slide in when hovered
            profileHover.setVisible(false);
        });
        profileContainer.setOnMouseExited(event -> {
            if (profileContainer.getTranslateX() == 0) { // Check if profileContainer is visible
                slideOutTransition.play(); // Slide out only if it's visible
                slideOutTransition.setOnFinished(e -> profileHover.setVisible(true));
            }
        });


        SharedFunctions.setSearchBar(searchBar);

        try (Connection connection = dataSource.getConnection()) {
            // Assuming you have stored the user's name and position in the user session
            UserSession userSession = UserSession.getInstance();
            String name = userSession.getUserFirstName() + " " + userSession.getUserLastName();
            String position = userSession.getUserPosition();
            String imageUrl = userSession.getUserPic();
            // Set the labels to your name and position
            nameText.setText(name);
            positionText.setText(position);
            if (imageUrl != null && !imageUrl.isEmpty()) {
                // Use Paths to get a valid file URL
                File imageFile = new File(imageUrl);
                String absolutePath = imageFile.toURI().toString();
                Image image = new Image(absolutePath);

                employeeProfile.setImage(image);
            }


            parentPane.setOnMousePressed(this::getWindowOffset);
            parentPane.setOnMouseDragged(this::moveWindow);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        RotateTransition rotateTransition = new RotateTransition(Duration.seconds(1), cog);
        rotateTransition.setByAngle(360); // Rotate 360 degrees
        rotateTransition.setCycleCount(RotateTransition.INDEFINITE); // Keep rotating indefinitely

        // Add event handlers for hover
        cog.setOnMouseEntered(event -> {
            rotateTransition.playFromStart(); // Play the animation when hovered
        });

        cog.setOnMouseExited(event -> {
            rotateTransition.pause(); // Pause the animation when the mouse exits
        });
    }


    private void loadContent(String fxmlFileName, boolean isFromNavigate) {
        System.out.println("Loading content: " + fxmlFileName); // Debug statement
        try {
            if (!isFromNavigate) {
                String sessionId = UserSession.getInstance().getSessionId();
                currentNavigationId = historyManager.addEntry(sessionId, fxmlFileName);
            }
            clearSelectedStyles();

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFileName));
            Parent content = loader.load();

            switch (fxmlFileName) {
                case "ChatContent.fxml" -> {
                    ChatContentController controller = loader.getController();
                    controller.setContentPane(contentPane); // Pass the contentPane instance
                    chatNavigation.getStyleClass().add("selected");
                }
                case "AdminContent.fxml" -> {
                    AdminContentController adminContentController = loader.getController();
                    adminContentController.setContentPane(contentPane);
                    adminNavigation.getStyleClass().add("selected");
                }
                case "AccountingContent.fxml" -> {
                    AccountingContentController controller = loader.getController();
                    controller.setContentPane(contentPane); // Pass the contentPane instance
                    accountingNavigation.getStyleClass().add("selected");
                }
                case "InternalOperationsContent.fxml" -> {
                    InternalOperationsContentController controller = loader.getController();
                    controller.setContentPane(contentPane); // Pass the contentPane instance
                    iOPSNavigation.getStyleClass().add("selected");
                }
                case "ExternalOperationsContent.fxml" -> {
                    ExternalOperationsContentController controller = loader.getController();
                    controller.setContentPane(contentPane); // Pass the contentPane instance
                    eOPSNavigation.getStyleClass().add("selected");
                }
                case "ReportsContent.fxml" -> {
                    ReportsContentController controller = loader.getController();
                    controller.setContentPane(contentPane);
                    financialReportsNavigation.getStyleClass().add("selected");
                }
                case "CalendarContent.fxml" -> {
                    CalendarContentController controller = loader.getController();
                    controller.setContentPane(contentPane); // Pass the contentPane instance
                    calendarNavigation.getStyleClass().add("selected");
                }
                case "SettingsContent.fxml" -> {
                    SettingsContentController controller = loader.getController();
                    controller.setContentPane(contentPane);
                }
            }
            ContentManager.setContent(contentPane, content);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading " + fxmlFileName + ": " + e.getMessage());
        }
    }

    private void clearSelectedStyles() {
        chatNavigation.getStyleClass().remove("selected");
        adminNavigation.getStyleClass().remove("selected");
        accountingNavigation.getStyleClass().remove("selected");
        iOPSNavigation.getStyleClass().remove("selected");
        eOPSNavigation.getStyleClass().remove("selected");
        financialReportsNavigation.getStyleClass().remove("selected");
        calendarNavigation.getStyleClass().remove("selected");
    }


    public void navigateBackward(MouseEvent mouseEvent) {
        String previousForm = historyManager.navigateBackward(currentNavigationId);
        if (previousForm != null) {
            loadContent(previousForm, true);
            currentNavigationId--; // Decrement the id for backward navigation
        }
    }

    public void navigateForward(MouseEvent mouseEvent) {
        String nextForm = historyManager.navigateForward(currentNavigationId);
        if (nextForm != null) {
            loadContent(nextForm, true);
            currentNavigationId++; // Increment the id for forward navigation
        }
    }


    public void loadChatContent(MouseEvent mouseEvent) {
        Platform.runLater(() -> {
            // Code to be executed on the JavaFX Application Thread
            loadContent("ChatContent.fxml", false);
        });
    }


    public void loadAdminContent(MouseEvent mouseEvent) {
        loadContent("AdminContent.fxml", false);
    }

    public void loadAccountingContent(MouseEvent mouseEvent) {
        loadContent("AccountingContent.fxml", false);
    }

    public void loadIOpsContent(MouseEvent mouseEvent) {
        loadContent("InternalOperationsContent.fxml", false);
    }

    public void loadEOpsContent(MouseEvent mouseEvent) {
        ToDoAlert.showToDoAlert();
        loadContent("ExternalOperationsContent.fxml", false);
    }

    public void loadFSContent(MouseEvent mouseEvent) {
        loadContent("ReportsContent.fxml", false);
    }

    public void loadCalendarContent(MouseEvent mouseEvent) {
        loadContent("CalendarContent.fxml", false);
    }

    public void loadSettingsContent(MouseEvent mouseEvent) {
        loadContent("SettingsContent.fxml", false);
    }

    @FXML
    public void handleLogout(MouseEvent mouseEvent) {
        boolean userConfirmedLogout = signOutAlert.showAlert("Sign Out", "Are you sure you want to sign out?");
        if (userConfirmedLogout) {
            UserSession userSession = UserSession.getInstance();
            String sessionId = userSession.getSessionId();
            logoutManager.logoutUser(sessionId, "SIGNOUT");
            System.exit(0);
            showLogin();
        }
    }


    private void showLogin() {
        Platform.runLater(() -> {
            try {
                Stage loginStage = new Stage();
                LoginForm loginForm = new LoginForm();
                loginForm.showLoginForm(loginStage); // Pass the Stage to showLoginForm()
            } catch (Exception e) {
                e.printStackTrace(); // Handle exceptions according to your needs
            }
        });
    }


    public void closeButton(MouseEvent mouseEvent) {
        ConfirmationAlert confirmationAlert = new ConfirmationAlert("Exit VOS", "Are you sure you want to exit VOS?", "", true);
        boolean userExit = confirmationAlert.showAndWait();
        if (userExit) {
            if (dataSource.isRunning()) {
                dataSource.close();
                auditTrailSource.close();
                if (dataSource.isClosed()) {
                    System.exit(0);
                }
            }
        }
    }

    public void maximizeButton(MouseEvent mouseEvent) {
        Stage stage = (Stage) logoutButton.getScene().getWindow();

        if (stage.getWidth() == 800) {
            // Maximize the stage on the current screen
            maximizeOnCurrentScreen(stage);
        } else {
            // Restore down
            stage.setMaximized(false);
            stage.setWidth(800);
            stage.setHeight(600);

            // Calculate center coordinates of the screen
            centerStageOnCurrentScreen(stage);
        }
    }

    private void maximizeOnCurrentScreen(Stage stage) {
        Screen currentScreen = getCurrentScreen(stage);
        Rectangle2D bounds = currentScreen.getVisualBounds();

        stage.setX(bounds.getMinX());
        stage.setY(bounds.getMinY());
        stage.setWidth(bounds.getWidth());
        stage.setHeight(bounds.getHeight());

        stage.setMaximized(true);
    }

    private void centerStageOnCurrentScreen(Stage stage) {
        Screen currentScreen = getCurrentScreen(stage);
        Rectangle2D bounds = currentScreen.getVisualBounds();
        double centerX = bounds.getMinX() + bounds.getWidth() / 2;
        double centerY = bounds.getMinY() + bounds.getHeight() / 2;

        // Set the stage position to the center of the screen
        stage.setX(centerX - stage.getWidth() / 2);
        stage.setY(centerY - stage.getHeight() / 2);
    }

    private Screen getCurrentScreen(Stage stage) {
        return Screen.getScreensForRectangle(stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight())
                .stream()
                .findFirst()
                .orElse(Screen.getPrimary());
    }

    public void minimizeButton(MouseEvent mouseEvent) {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        stage.setIconified(true);
    }

    public void moveWindow(MouseEvent mouseEvent) {
        Stage stage = (Stage) vosIcon.getScene().getWindow();

        if ((stage.getWidth() == 800)) {
            double mouseX = mouseEvent.getScreenX();
            double mouseY = mouseEvent.getScreenY();

            // Define a threshold for resizing (borderOffset)
            // Width of the border for resizing
            double borderOffset = 5;
            if (mouseX >= stage.getX() + stage.getWidth() - borderOffset &&
                    mouseX <= stage.getX() + stage.getWidth() + borderOffset &&
                    mouseY >= stage.getY() + stage.getHeight() - borderOffset &&
                    mouseY <= stage.getY() + stage.getHeight() + borderOffset) {
                // Resize
                stage.setWidth(mouseX - stage.getX());
                stage.setHeight(mouseY - stage.getY());
            } else {
                stage.setX(mouseX - xOffset);
                stage.setY(mouseY - yOffset);
            }
        }
    }

    public void getWindowOffset(MouseEvent mouseEvent) {
        xOffset = mouseEvent.getSceneX();
        yOffset = mouseEvent.getSceneY();
    }



    public void setUserAccess(int userId) {
        List<VBox> vboxes = List.of(chatNavigation, adminNavigation, accountingNavigation, iOPSNavigation, eOPSNavigation, financialReportsNavigation, calendarNavigation, logoutNavigation);
        TaskbarManager taskbarManager = new TaskbarManager(navigationBox, vboxes);
        taskbarManager.updateParentVBox();
    }

}
