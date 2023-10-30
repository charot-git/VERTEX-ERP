package com.vertex.vos;

import com.vertex.vos.Constructors.HoverAnimation;
import com.vertex.vos.Constructors.UserSession;
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

public class HumanResourcesNavigationController implements Initializable {

    private AnchorPane contentPane; // Declare contentPane variable
    @FXML
    private VBox payrollBox;
    @FXML
    private VBox memoBox;
    @FXML
    private VBox announcementBox;
    @FXML
    private VBox attendanceBox;
    @FXML
    private VBox manageEmployeesBox;
    @FXML
    private VBox managePoliciesBox;

    public void setContentPane(AnchorPane contentPane) {
        this.contentPane = contentPane;
    }

    private final HistoryManager historyManager = new HistoryManager();

    private int currentNavigationId = -1; // Initialize to a default value


    public void employeeManagement(MouseEvent mouseEvent) {
        loadContent("tableManager.fxml", "employee");

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

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        new HoverAnimation(announcementBox);
        new HoverAnimation(attendanceBox);
        new HoverAnimation(manageEmployeesBox);
        new HoverAnimation(managePoliciesBox);
        new HoverAnimation(memoBox);
        new HoverAnimation(payrollBox);
    }
}
