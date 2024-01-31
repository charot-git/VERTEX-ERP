package com.vertex.vos;

import com.vertex.vos.Constructors.HoverAnimation;
import com.vertex.vos.Constructors.UserSession;
import com.vertex.vos.Utilities.ToDoAlert;
import javafx.animation.ScaleTransition;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AdminContentController implements Initializable {

    private AnchorPane contentPane; // Declare contentPane variable
    @FXML
    private VBox registrationBox;
    @FXML
    private VBox hrBox;
    @FXML
    private VBox aeBox;

    public void setContentPane(AnchorPane contentPane) {
        this.contentPane = contentPane;
    }

    private final HistoryManager historyManager = new HistoryManager();

    private int currentNavigationId = -1; // Initialize to a default value


    public void openRegistration(MouseEvent mouseEvent) {
        loadContent("adminRegistration.fxml"); // Replace with your FXML file name
    }

    public void openHumanResources(MouseEvent mouseEvent) {
        loadContent("humanResourcesNavigation.fxml"); // Replace with your FXML file name
    }

    public void openAssetsEquipments(MouseEvent mouseEvent) {
        loadContent("tableManager.fxml");
    }

    private void loadContent(String fxmlFileName) {
        System.out.println("Loading content: " + fxmlFileName); // Debug statement
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFileName));
            Parent content = loader.load();

            // Set the controller for the loaded FXML file
            if (fxmlFileName.equals("adminRegistration.fxml")) {
                RegistrationNavigatorController controller = loader.getController();
                controller.setContentPane(contentPane);
            } else if (fxmlFileName.equals("humanResourcesNavigation.fxml")) {
                HumanResourcesNavigationController controller = loader.getController();
                controller.setContentPane(contentPane);
            } else if (fxmlFileName.equals("assetsEquipmentsRegistration.fxml")) {
                AssetsEquipmentsController controller = loader.getController();
                controller.setContentPane(contentPane);
            } else if (fxmlFileName.equals("tableManager.fxml")) {
                TableManagerController controller = loader.getController();
                controller.setRegistrationType("assets_and_equipments");
                controller.setContentPane(contentPane);
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

    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Set hover animations for registrationBox
        new HoverAnimation(registrationBox);
        // Set hover animations for hrBox
        new HoverAnimation(hrBox);
        // Set hover animations for aeBox
        new HoverAnimation(aeBox);
    }

}
