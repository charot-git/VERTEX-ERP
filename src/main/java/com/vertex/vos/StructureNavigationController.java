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
    @FXML
    private VBox truckBox;

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
        companyBox.setOnMouseClicked(mouseEvent -> loadContent("company"));
        industryBox.setOnMouseClicked(mouseEvent -> loadContent("industry"));
        divisionBox.setOnMouseClicked(mouseEvent -> loadContent("division"));
        branchBox.setOnMouseClicked(mouseEvent -> loadContent("branch"));
        departmentBox.setOnMouseClicked(mouseEvent -> loadContent("department"));
        systemEmployeeBox.setOnMouseClicked(mouseEvent -> loadContent("system_employee"));
        salesmanBox.setOnMouseClicked(mouseEvent -> loadContent("salesman"));
        truckBox.setOnMouseClicked(mouseEvent -> loadContent("vehicles"));

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
        new HoverAnimation(truckBox);

    }

    @FXML
    private void loadContent(String registrationType) {
        System.out.println("Loading content: " + "tableManager.fxml" + " for registration type: " + registrationType); // Debug statement
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("tableManager.fxml"));
            Parent content = loader.load();

            TableManagerController controller = loader.getController();
            controller.setContentPane(contentPane);
            controller.setRegistrationType(registrationType);

            String sessionId = UserSession.getInstance().getSessionId();
            currentNavigationId = historyManager.addEntry(sessionId, "tableManager.fxml");

            ContentManager.setContent(contentPane, content); // Assuming contentPane is your AnchorPane
        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception according to your needs
            System.err.println("Error loading " + "tableManager.fxml" + ": " + e.getMessage());
        }
    }
}
