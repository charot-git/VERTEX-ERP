package com.example.vos;

import com.zaxxer.hikari.HikariDataSource;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;
import java.util.ResourceBundle;

public class DashBoardController implements Initializable {
    private final NavigationManager navigationManager = new NavigationManager();
    public ImageView logoutButton, vosIcon;
    @FXML
    public AnchorPane parentPane;
    public ImageView forwardForm;
    public ImageView backForm;
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    @FXML
    private TextField searchBar;

    @FXML
    private HBox profileBox;
    @FXML
    private Label nameText, positionText;
    private double xOffset, yOffset;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        backForm.setOnMouseClicked(event -> {
            navigationManager.navigateBack();
        });
        forwardForm.setOnMouseClicked(event -> {
            navigationManager.navigateForward();
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
            profileBox.setVisible(false);
            // Set up event handlers for window dragging
            parentPane.setOnMousePressed(this::getWindowOffset);
            parentPane.setOnMouseDragged(this::moveWindow);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public void loadChatContent(MouseEvent mouseEvent) {
        try {
            // Load employeeManagement.fxml file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ChatContent.fxml"));
            Parent chatContent = loader.load();

            ChatContentController chatContentController = loader.getController();


            // Set the loaded content as the content of the contentPane
            AnchorPane contentPane = (AnchorPane) ((Node) mouseEvent.getSource()).getScene().getRoot().lookup("#contentPane");
            contentPane.getChildren().clear(); // Clear existing content
            contentPane.getChildren().add(chatContent);

            // Set maximum dimensions for the contentPane (parent of hrNavigator)
            double maxNavigatorWidth = contentPane.getWidth() - 20; // Adjust as needed
            double maxNavigatorHeight = contentPane.getHeight() - 20; // Adjust as needed
            contentPane.setMaxSize(maxNavigatorWidth, maxNavigatorHeight);
            contentPane.setMinSize(0, 0); // Allow contentPane to shrink smaller if necessary

            // Make hrNavigator fit inside contentPane while preserving aspect ratio
            AnchorPane.setTopAnchor(chatContent, 0.0);
            AnchorPane.setRightAnchor(chatContent, 0.0);
            AnchorPane.setBottomAnchor(chatContent, 0.0);
            AnchorPane.setLeftAnchor(chatContent, 0.0);

        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception according to your needs
            System.err.println("Error loading humanResourcesNavigation.fxml: " + e.getMessage());
        }
    }

    public void loadAdminContent(MouseEvent mouseEvent) {
        try {
            // Load employeeManagement.fxml file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("AdminContent.fxml"));
            Parent hrNavigator = loader.load();
            AdminContentController controller = loader.getController();


            // Set the loaded content as the content of the contentPane
            AnchorPane contentPane = (AnchorPane) ((Node) mouseEvent.getSource()).getScene().getRoot().lookup("#contentPane");
            contentPane.getChildren().clear(); // Clear existing content
            contentPane.getChildren().add(hrNavigator);

            // Set maximum dimensions for the contentPane (parent of hrNavigator)
            double maxNavigatorWidth = contentPane.getWidth() - 20; // Adjust as needed
            double maxNavigatorHeight = contentPane.getHeight() - 20; // Adjust as needed
            contentPane.setMaxSize(maxNavigatorWidth, maxNavigatorHeight);
            contentPane.setMinSize(0, 0); // Allow contentPane to shrink smaller if necessary

            // Make hrNavigator fit inside contentPane while preserving aspect ratio
            AnchorPane.setTopAnchor(hrNavigator, 0.0);
            AnchorPane.setRightAnchor(hrNavigator, 0.0);
            AnchorPane.setBottomAnchor(hrNavigator, 0.0);
            AnchorPane.setLeftAnchor(hrNavigator, 0.0);

        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception according to your needs
            System.err.println("Error loading humanResourcesNavigation.fxml: " + e.getMessage());
        }
    }

    public void loadAccountingContent(MouseEvent mouseEvent) {
    }

    public void loadIOpsContent(MouseEvent mouseEvent) {
    }

    public void loadEOpsContent(MouseEvent mouseEvent) {
    }

    public void loadFSContent(MouseEvent mouseEvent) {
    }

    public void loadCalendarContent(MouseEvent mouseEvent) {
    }

    @FXML
    public void handleLogout(MouseEvent mouseEvent) {
        boolean userConfirmedLogout = signOutAlert.showAlert("Sign Out", "Are you sure you want to sign out?");
        if (userConfirmedLogout) {
            signOut();
        } else {
            System.out.println("Cancelled");
        }
    }

    private void signOut() {
        // Clear the session data in the database
        UserSession userSession = UserSession.getInstance();
        String sessionId = userSession.getSessionId();

        try (Connection connection = dataSource.getConnection()) {
            String deleteSessionQuery = "DELETE FROM session WHERE session_id = ?";
            try (PreparedStatement deleteSessionStatement = connection.prepareStatement(deleteSessionQuery)) {
                deleteSessionStatement.setString(1, sessionId);
                int rowsAffected = deleteSessionStatement.executeUpdate();
                if (rowsAffected > 0) {
                    // Clear session data locally
                    clearSessionLocally();

                    // Show login prompt
                    showAlert("Logged Out", "You have been successfully logged out.");
                } else {
                    showAlert("Logout Failed", "Failed to logout. Please try again.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Close the dashboard window if open
        Stage dashboardStage = (Stage) logoutButton.getScene().getWindow();
        dashboardStage.close();
        // Open the login window
        showLogin();
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

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);

        alert.showAndWait();
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

    public void showProfile(MouseEvent mouseEvent) {
        profileBox.setVisible(true);

    }

    public void hideProfile(MouseEvent mouseEvent) {
        profileBox.setVisible(false);
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
}
