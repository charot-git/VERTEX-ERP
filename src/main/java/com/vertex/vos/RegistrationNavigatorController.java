package com.vertex.vos;

import com.vertex.vos.Objects.HoverAnimation;
import com.vertex.vos.Objects.UserSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class RegistrationNavigatorController implements Initializable {
    private AnchorPane contentPane;
    @FXML
    private VBox structureBox;
    @FXML
    private VBox supplierBox;
    @FXML
    private VBox complianceBox;
    @FXML
    private VBox productsBox;
    @FXML
    private VBox discountBox;
    @FXML
    private VBox customerBox;
    @FXML
    private VBox bankBox;

    public void setContentPane(AnchorPane contentPane) {
        this.contentPane = contentPane;
    }

    private final HistoryManager historyManager = new HistoryManager();

    private int currentNavigationId = -1; // Initialize to a default value

    @FXML
    private void loadContent(String fxmlFileName, String registrationType) {
        System.out.println("Loading content: " + fxmlFileName + " for registration type: " + registrationType); // Debug statement
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFileName));
            Parent content = loader.load();

            // Set the controller for the loaded FXML file
            switch (fxmlFileName) {
                case "structureNavigation.fxml" -> {
                    StructureNavigationController controller = loader.getController();
                    controller.setContentPane(contentPane);
                }
                case "supplierNavigation.fxml" -> {
                    SupplierNavigationController controller = loader.getController();
                    controller.setContentPane(contentPane);
                }
                case "complianceNavigation.fxml" ->{
                    ComplianceNavigationController controller = loader.getController();
                    controller.setContentPane(contentPane);
                }
                case "discountNavigation.fxml" ->{
                    DiscountNavigationController controller = loader.getController();
                    controller.setContentPane(contentPane);
                }
                case "tableManager.fxml" ->{
                    TableManagerController controller = loader.getController();
                    controller.setRegistrationType(registrationType);
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

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        structureBox.setOnMouseClicked(mouseEvent -> loadContent("structureNavigation.fxml", "none"));
        supplierBox.setOnMouseClicked(mouseEvent -> loadContent("supplierNavigation.fxml", "none"));
        complianceBox.setOnMouseClicked(mouseEvent -> loadContent("complianceNavigation.fxml", "none"));
        productsBox.setOnMouseClicked(mouseEvent -> loadContent("tableManager.fxml", "product"));
        discountBox.setOnMouseClicked(mouseEvent -> loadContent("discountNavigation.fxml", "none"));
        customerBox.setOnMouseClicked(mouseEvent -> loadContent("tableManager.fxml", "customer"));
        bankBox.setOnMouseClicked(mouseEvent -> loadContent("tableManager.fxml", "bank"));
        animationSetUp();
    }

    private void animationSetUp() {
        new HoverAnimation(structureBox);
        new HoverAnimation(supplierBox);
        new HoverAnimation(complianceBox);
        new HoverAnimation(productsBox);
        new HoverAnimation(discountBox);
        new HoverAnimation(customerBox);
        new HoverAnimation(bankBox);
    }
}
