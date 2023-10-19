package com.example.vos;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;

public class AdminContentController {
    public void openRegistration(MouseEvent mouseEvent) {
        try {
            // Load ChatContent.fxml file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("adminRegistration.fxml"));
            Parent regNavigator = loader.load();

            RegistrationNavigatorController controller = loader.getController();


            // Set the loaded content as the content of the contentPane
            AnchorPane contentPane = (AnchorPane) ((Node) mouseEvent.getSource()).getScene().getRoot().lookup("#contentPane");
            contentPane.getChildren().clear(); // Clear existing content
            contentPane.getChildren().add(regNavigator);

        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception according to your needs
        }
    }

    public void openHumanResources(MouseEvent mouseEvent) {
        try {
            // Load ChatContent.fxml file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("humanResourcesNavigation.fxml"));
            Parent hrNavigator = loader.load();

            HumanResourcesNavigationController controller = loader.getController();

            // Set the loaded content as the content of the contentPane
            AnchorPane contentPane = (AnchorPane) ((Node) mouseEvent.getSource()).getScene().getRoot().lookup("#contentPane");
            contentPane.getChildren().clear(); // Clear existing content
            contentPane.getChildren().add(hrNavigator);
        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception according to your needs
            System.err.println("Error loading humanResourcesNavigation.fxml: " + e.getMessage());
        }
    }

    public void openAssetsEquipments(MouseEvent mouseEvent) {
    }
}
