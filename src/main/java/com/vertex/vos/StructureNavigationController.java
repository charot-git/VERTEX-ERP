package com.vertex.vos;

import com.vertex.vos.Constructors.HoverAnimation;
import com.vertex.vos.Constructors.UserSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class StructureNavigationController implements Initializable {

    private AnchorPane contentPane;
    @FXML
    private VBox structureBox;
    @FXML
    private VBox supplierBox;
    @FXML
    private VBox complianceBox;
    @FXML
    private VBox salesmanBox;

    public void setContentPane(AnchorPane contentPane) {
        this.contentPane = contentPane;
    }

    private final HistoryManager historyManager = new HistoryManager();

    private int currentNavigationId = -1; // Initialize to a default value
    @FXML
    private VBox companyBox;
    @FXML
    private VBox industryBox;
    @FXML
    private VBox divisionBox;
    @FXML
    private VBox branchBox;
    @FXML
    private VBox departmentBox;
    @FXML
    private VBox systemEmployeeBox;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        companyBox.setOnMouseClicked(mouseEvent -> loadContent("tableManager.fxml", "company"));
        industryBox.setOnMouseClicked(mouseEvent -> loadContent("tableManager.fxml", "industry"));
        divisionBox.setOnMouseClicked(mouseEvent -> loadContent("tableManager.fxml", "division"));
        branchBox.setOnMouseClicked(mouseEvent -> loadContent("tableManager.fxml", "branch"));
        departmentBox.setOnMouseClicked(mouseEvent -> loadContent("tableManager.fxml", "department"));
        systemEmployeeBox.setOnMouseClicked(mouseEvent -> loadContent("tableManager.fxml", "system_employee"));
        salesmanBox.setOnMouseClicked(mouseEvent -> loadContent("tableManager.fxml", "salesman"));


        animationInitialization();
    }

    private void animationInitialization() {
        new HoverAnimation(companyBox);
        new HoverAnimation(industryBox);
        new HoverAnimation(divisionBox);
        new HoverAnimation(branchBox);
        new HoverAnimation(departmentBox);
        new HoverAnimation(systemEmployeeBox);
        new HoverAnimation(salesmanBox);
    }

    @FXML
    private void loadContent(String fxmlFileName, String registrationType) {
        System.out.println("Loading content: " + fxmlFileName + " for registration type: " + registrationType); // Debug statement
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFileName));
            Parent content = loader.load();

            TableManagerController controller = loader.getController();
            controller.setContentPane(contentPane);
            controller.setRegistrationType(registrationType);

            String sessionId = UserSession.getInstance().getSessionId();
            currentNavigationId = historyManager.addEntry(sessionId, fxmlFileName);

            ContentManager.setContent(contentPane, content); // Assuming contentPane is your AnchorPane
        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception according to your needs
            System.err.println("Error loading " + fxmlFileName + ": " + e.getMessage());
        }
    }
}
