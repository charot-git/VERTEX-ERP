package com.example.vos;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;

import java.io.IOException;

public class HumanResourcesNavigationController {
    public void employeeManagement(MouseEvent mouseEvent) {
        TextField searchBar  = SharedFunctions.getSearchBar();
        if (searchBar!=null){
        try {
            // Load employeeManagement.fxml file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("employeeManagement.fxml"));
            Parent hrNavigator = loader.load();
            EmployeeManagementController controller = loader.getController();


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
        else{
            System.err.println("not found");
        }
    }

    public void policiesManagement(MouseEvent mouseEvent) {
    }

    public void attendanceManagement(MouseEvent mouseEvent) {
    }

    public void annoucementManagement(MouseEvent mouseEvent) {
    }

    public void memoManagement(MouseEvent mouseEvent) {
    }

    public void payrollManagement(MouseEvent mouseEvent) {
    }
}
