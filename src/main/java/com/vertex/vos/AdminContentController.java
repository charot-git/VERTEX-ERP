package com.vertex.vos;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;

public class AdminContentController {

    private AnchorPane contentPane; // Declare contentPane variable

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
        loadContent("assetsEquipments.fxml"); // Replace with your FXML file name
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
            }
            else if (fxmlFileName.equals("assetsEquipments.fxml")) {
                AssetsEquipmentsController controller = loader.getController();
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
}
