package com.vertex.vos;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;

public class SupplierNavigationController {

    private AnchorPane contentPane; // Declare contentPane variable

    public void setContentPane(AnchorPane contentPane) {
        this.contentPane = contentPane;
    }

    private final HistoryManager historyManager = new HistoryManager();

    private int currentNavigationId = -1; // Initialize to a default value


    public void productRegister(MouseEvent mouseEvent) {
        loadContent("tableManager.fxml", "product");
    }
    @FXML
    private void supplierInfoRegister(MouseEvent mouseEvent) {
        loadContent("tableManager.fxml", "supplier");
    }


    @FXML
    private void loadContent(String fxmlFileName, String registrationType) {
        System.out.println("Loading content: " + fxmlFileName + " for registration type: " + registrationType); // Debug statement
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFileName));
            Parent content = loader.load();

            // Set the controller for the loaded FXML file
            switch (fxmlFileName) {
                case "supplierNavigation.fxml" -> {
                    SupplierNavigationController controller = loader.getController();
                    controller.setContentPane(contentPane);
                }
                case "tableManager.fxml" -> {
                    TableManagerController controller = loader.getController();
                    controller.setRegistrationType(registrationType); // Set the registration type
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
}
