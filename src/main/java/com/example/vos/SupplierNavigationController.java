package com.example.vos;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;

public class SupplierNavigationController {

    public void productRegister(MouseEvent mouseEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ProductRegistration.fxml"));
            Parent productRegistration = loader.load();
            ProductRegistrationController productRegistrationController = loader.getController();

            // Set the loaded content as the content of the contentPane
            AnchorPane contentPane = (AnchorPane) ((Node) mouseEvent.getSource()).getScene().getRoot().lookup("#contentPane");
            contentPane.getChildren().clear(); // Clear existing content
            contentPane.getChildren().add(productRegistration);

            // Set maximum dimensions for the contentPane (parent of productRegistration)
            double maxRegistrationWidth = contentPane.getWidth() - 20; // Adjust as needed
            double maxRegistrationHeight = contentPane.getHeight() - 20; // Adjust as needed
            contentPane.setMaxSize(maxRegistrationWidth, maxRegistrationHeight);
            contentPane.setMinSize(0, 0); // Allow contentPane to shrink smaller if necessary

            // Make productRegistration fit inside contentPane while preserving aspect ratio
            AnchorPane.setTopAnchor(productRegistration, 0.0);
            AnchorPane.setRightAnchor(productRegistration, 0.0);
            AnchorPane.setBottomAnchor(productRegistration, 0.0);
            AnchorPane.setLeftAnchor(productRegistration, 0.0);

        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception according to your needs
            System.err.println("Error loading ProductRegistration.fxml: " + e.getMessage());
        }
    }
}
