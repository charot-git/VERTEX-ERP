package com.example.vos;

import com.zaxxer.hikari.HikariDataSource;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
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

    public ImageView logoutButton, vosIcon;
    @FXML
    public AnchorPane parentPane;
    private HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    @FXML
    private TextField searchBar;

    public TextField getSearchBar() {
        return searchBar;
    }

    public void setSearchBar(TextField searchBar) {
        this.searchBar = searchBar;
    }


    @FXML
    private HBox profileBox;
    @FXML
    private Label nameText, positionText;
    private double xOffset, yOffset;
    private double borderOffset = 5; // Width of the border for resizing



    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
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
            System.err.println("Error loading HumanResourcesNavigation.fxml: " + e.getMessage());
        }
    }

    public void loadAdminContent(MouseEvent mouseEvent) {
        try {
            // Load ChatContent.fxml file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("AdminContent.fxml"));
            Parent adminContent = loader.load();

            AdminContentController adminController = loader.getController();

            // Set the loaded content as the content of the contentPane
            AnchorPane contentPane = (AnchorPane) ((Node) mouseEvent.getSource()).getScene().getRoot().lookup("#contentPane");
            contentPane.getChildren().clear(); // Clear existing content
            contentPane.getChildren().add(adminContent);

        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception according to your needs
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
        try {
            // Load the login FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("loginForm.fxml"));
            Parent root = loader.load();

            // Get the controller and set signInButton as the default button
            LoginController loginController = loader.getController();
            loginController.setDefaultButton();

            // Create a new stage for the login
            Stage loginStage = new Stage();
            loginStage.setTitle("Login");

            // Set the scene for the login stage
            Scene scene = new Scene(root, 600, 400); // Adjust dimensions as needed
            loginStage.setScene(scene);

            // Show the login stage
            loginStage.show();
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

    public void closeButton(MouseEvent mouseEvent) {
        Platform.exit();
    }

    public void maximizeButton(MouseEvent mouseEvent) {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        if (stage.isMaximized()) {
            stage.setMaximized(false);
            stage.setWidth(800);
            stage.setHeight(600);
        } else {
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
        stage.setX(mouseEvent.getScreenX() - xOffset);
        stage.setY(mouseEvent.getScreenY() - yOffset);
    }

    public void getWindowOffset(MouseEvent mouseEvent) {
        xOffset = mouseEvent.getSceneX();
        yOffset = mouseEvent.getSceneY();
    }
}
