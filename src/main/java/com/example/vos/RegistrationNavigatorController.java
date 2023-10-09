package com.example.vos;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;

public class RegistrationNavigatorController {
    public void supplierRegistration(MouseEvent mouseEvent) {
        try {
            // Load ChatContent.fxml file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("SupplierNavigation.fxml"));
            Parent supplierContent = loader.load();

            SupplierNavigationController supplierNavigationController = loader.getController();

            // Set the loaded content as the content of the contentPane
            AnchorPane contentPane = (AnchorPane) ((Node) mouseEvent.getSource()).getScene().getRoot().lookup("#contentPane");
            contentPane.getChildren().clear(); // Clear existing content
            contentPane.getChildren().add(supplierContent);

        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception according to your needs
        }
    }
}
