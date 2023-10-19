package com.example.vos;

import javafx.fxml.FXML;
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("supplierNavigation.fxml"));
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

    public void companyRegistration(MouseEvent mouseEvent) {
        try {
            // Load employeeManagement.fxml file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("companyRegistration.fxml"));
            Parent companyRegistration = loader.load();
            CompanyRegistrationController controller = loader.getController();


            // Set the loaded content as the content of the contentPane
            AnchorPane contentPane = (AnchorPane) ((Node) mouseEvent.getSource()).getScene().getRoot().lookup("#contentPane");
            contentPane.getChildren().clear(); // Clear existing content
            contentPane.getChildren().add(companyRegistration);

            // Set maximum dimensions for the contentPane (parent of hrNavigator)
            double maxNavigatorWidth = contentPane.getWidth() - 20; // Adjust as needed
            double maxNavigatorHeight = contentPane.getHeight() - 20; // Adjust as needed
            contentPane.setMaxSize(maxNavigatorWidth, maxNavigatorHeight);
            contentPane.setMinSize(0, 0); // Allow contentPane to shrink smaller if necessary

            // Make hrNavigator fit inside contentPane while preserving aspect ratio
            AnchorPane.setTopAnchor(companyRegistration, 0.0);
            AnchorPane.setRightAnchor(companyRegistration, 0.0);
            AnchorPane.setBottomAnchor(companyRegistration, 0.0);
            AnchorPane.setLeftAnchor(companyRegistration, 0.0);

        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception according to your needs
            System.err.println("Error loading humanResourcesNavigation.fxml: " + e.getMessage());
        }
    }

    @FXML
    private void productRegistration(MouseEvent mouseEvent) {
        try {
            // Load employeeManagement.fxml file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("registerProductTemplate.fxml"));
            Parent companyRegistration = loader.load();
            RegisterProductTemplateController controller = loader.getController();


            // Set the loaded content as the content of the contentPane
            AnchorPane contentPane = (AnchorPane) ((Node) mouseEvent.getSource()).getScene().getRoot().lookup("#contentPane");
            contentPane.getChildren().clear(); // Clear existing content
            contentPane.getChildren().add(companyRegistration);

            // Set maximum dimensions for the contentPane (parent of hrNavigator)
            double maxNavigatorWidth = contentPane.getWidth() - 20; // Adjust as needed
            double maxNavigatorHeight = contentPane.getHeight() - 20; // Adjust as needed
            contentPane.setMaxSize(maxNavigatorWidth, maxNavigatorHeight);
            contentPane.setMinSize(0, 0); // Allow contentPane to shrink smaller if necessary

            // Make hrNavigator fit inside contentPane while preserving aspect ratio
            AnchorPane.setTopAnchor(companyRegistration, 0.0);
            AnchorPane.setRightAnchor(companyRegistration, 0.0);
            AnchorPane.setBottomAnchor(companyRegistration, 0.0);
            AnchorPane.setLeftAnchor(companyRegistration, 0.0);

        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception according to your needs
            System.err.println("Error loading humanResourcesNavigation.fxml: " + e.getMessage());
        }
    }
}
