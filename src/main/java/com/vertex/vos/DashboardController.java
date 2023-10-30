package com.vertex.vos;

import com.vertex.vos.Constructors.SharedFunctions;
import com.vertex.vos.Constructors.UserSession;
import com.vertex.vos.Utilities.*;
import com.zaxxer.hikari.HikariDataSource;
import javafx.animation.RotateTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {
    public ImageView logoutButton, vosIcon;
    @FXML
    public AnchorPane parentPane;
    public ImageView forwardForm;
    public ImageView backForm;
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

        profileContainer.setTranslateX(400); // Set initial translation to hide the container

        // Create a TranslateTransition for sliding the profileContainer in and out
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
            // Set the labels to your name and position
            nameText.setText(name);
            positionText.setText(position);


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


    private void loadContent(String fxmlFileName) {
        System.out.println("Loading content: " + fxmlFileName); // Debug statement
        try {

            clearSelectedStyles();

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFileName));
            Parent content = loader.load();

            // Set the controller for the loaded FXML file
            switch (fxmlFileName) {
                case "ChatContent.fxml" -> {
                    ChatContentController controller = loader.getController();
                    controller.setContentPane(contentPane); // Pass the contentPane instance
                    chatNavigation.getStyleClass().add("selected");
                }
                case "AdminContent.fxml" -> {
                    AdminContentController adminContentController = loader.getController();
                    adminContentController.setContentPane(contentPane); // Pass the contentPane instance
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
                case "FinancialStatementContent.fxml" -> {
                    FinancialStatementContentController controller = loader.getController();
                    controller.setContentPane(contentPane); // Pass the contentPane instance
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


            // Add entry to navigation history and get the generated ID
            String sessionId = UserSession.getInstance().getSessionId();
            currentNavigationId = historyManager.addEntry(sessionId, fxmlFileName);

            ContentManager.setContent(contentPane, content); // Assuming contentPane is your AnchorPane
        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception according to your needs
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
            loadContent(previousForm);
        }
    }

    public void navigateForward(MouseEvent mouseEvent) {
        String nextForm = historyManager.navigateForward(currentNavigationId);
        if (nextForm != null) {
            loadContent(nextForm);
        }
    }

    public void loadChatContent(MouseEvent mouseEvent) {
        loadContent("ChatContent.fxml");
    }

    public void loadAdminContent(MouseEvent mouseEvent) {
        loadContent("AdminContent.fxml");
    }

    public void loadAccountingContent(MouseEvent mouseEvent) {
        loadContent("AccountingContent.fxml");
    }

    public void loadIOpsContent(MouseEvent mouseEvent) {
        loadContent("InternalOperationsContent.fxml");
    }

    public void loadEOpsContent(MouseEvent mouseEvent) {
        ToDoAlert.showToDoAlert();
        loadContent("ExternalOperationsContent.fxml");
    }

    public void loadFSContent(MouseEvent mouseEvent) {
        ToDoAlert.showToDoAlert();
        loadContent("FinancialStatementContent.fxml");
    }

    public void loadCalendarContent(MouseEvent mouseEvent) {
        loadContent("CalendarContent.fxml");
    }

    public void loadSettingsContent(MouseEvent mouseEvent) {
        loadContent("SettingsContent.fxml");
    }

    @FXML
    public void handleLogout(MouseEvent mouseEvent) {
        boolean userConfirmedLogout = signOutAlert.showAlert("Sign Out", "Are you sure you want to sign out?");
        if (userConfirmedLogout) {
            UserSession userSession = UserSession.getInstance();
            String sessionId = userSession.getSessionId();
            logoutManager.logoutUser(sessionId);
            Stage dashboardStage = (Stage) logoutButton.getScene().getWindow();
            dashboardStage.close();
            showLogin();
        } else {
            System.out.println("Cancelled");
        }
    }


    private void showLogin() {
        Platform.runLater(() -> {
            try {
                Stage loginStage = new Stage();
                LoginForm loginForm = new LoginForm();
                loginForm.start(loginStage);
            } catch (Exception e) {
                e.printStackTrace(); // Handle exceptions according to your needs
            }
        });
    }


    public void closeButton(MouseEvent mouseEvent) {
        Platform.exit();
    }

    public void maximizeButton(MouseEvent mouseEvent) {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        if (stage.isMaximized()) {
            // Restore down
            stage.setMaximized(false);
            stage.setWidth(800);
            stage.setHeight(600);

            // Calculate center coordinates of the screen
            var screen = Screen.getPrimary();
            Rectangle2D bounds = screen.getVisualBounds();
            double centerX = bounds.getMinX() + bounds.getWidth() / 2;
            double centerY = bounds.getMinY() + bounds.getHeight() / 2;

            // Set the stage position to the center of the screen
            stage.setX(centerX - stage.getWidth() / 2);
            stage.setY(centerY - stage.getHeight() / 2);
        } else {
            // Maximize
            stage.setMaximized(true);
        }
    }


    public void minimizeButton(MouseEvent mouseEvent) {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        stage.setIconified(true);
    }

    public void moveWindow(MouseEvent mouseEvent) {
        Stage stage = (Stage) vosIcon.getScene().getWindow();

        if (!stage.isMaximized()) {
            // If the mouse is near the border of the stage, resize; otherwise, move
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
                // Move
                stage.setX(mouseX - xOffset);
                stage.setY(mouseY - yOffset);
            }
        }
    }


    public void getWindowOffset(MouseEvent mouseEvent) {
        xOffset = mouseEvent.getSceneX();
        yOffset = mouseEvent.getSceneY();
    }


    private Timestamp getCurrentTimestamp() {
        // Implement this method to get the current timestamp
        return new Timestamp(System.currentTimeMillis());
    }
}
