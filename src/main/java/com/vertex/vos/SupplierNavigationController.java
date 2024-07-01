package com.vertex.vos;

import com.vertex.vos.Objects.HoverAnimation;
import com.vertex.vos.Objects.UserSession;
import com.vertex.vos.Utilities.ToDoAlert;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class SupplierNavigationController implements Initializable {

    private AnchorPane contentPane;
    @FXML
    private VBox supplierInfoBox;
    @FXML
    private VBox supplierTypeBox;
    @FXML
    private VBox segmentBox;
    @FXML
    private VBox categoryBox;
    @FXML
    private VBox brandBox;
    @FXML
    private VBox termsBox;
    @FXML
    private VBox sectionBox;
    @FXML
    private VBox classBox;
    @FXML
    private VBox unitBox;


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

            if (fxmlFileName.equals("termsNavigation.fxml")) {
                TermsNavigationController controller = loader.getController();
                controller.setContentPane(contentPane);
            } else {
                TableManagerController controller = loader.getController();
                controller.setRegistrationType(registrationType); // Set the registration type
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

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        supplierInfoBox.setOnMouseClicked(mouseEvent -> loadContent("tableManager.fxml", "supplier"));
        supplierTypeBox.setOnMouseClicked(mouseEvent -> ToDoAlert.showToDoAlert());
        categoryBox.setOnMouseClicked(mouseEvent -> loadContent("tableManager.fxml", "category"));
        brandBox.setOnMouseClicked(mouseEvent -> loadContent("tableManager.fxml", "brand"));
        segmentBox.setOnMouseClicked(mouseEvent -> loadContent("tableManager.fxml", "segment"));
        termsBox.setOnMouseClicked(mouseEvent -> loadContent("termsNavigation.fxml", ""));
        sectionBox.setOnMouseClicked(mouseEvent -> loadContent("tableManager.fxml", "section"));
        classBox.setOnMouseClicked(mouseEvent -> loadContent("tableManager.fxml", "class"));
        unitBox.setOnMouseClicked(mouseEvent -> loadContent("tableManager.fxml", "unit"));
        animationInitialization();
    }

    private void animationInitialization() {
        new HoverAnimation(supplierInfoBox);
        new HoverAnimation(supplierTypeBox);
        new HoverAnimation(categoryBox);
        new HoverAnimation(brandBox);
        new HoverAnimation(segmentBox);
        new HoverAnimation(termsBox);
        new HoverAnimation(sectionBox);
        new HoverAnimation(classBox);
        new HoverAnimation(unitBox);
    }
}
