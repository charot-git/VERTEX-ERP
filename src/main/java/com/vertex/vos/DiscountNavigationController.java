package com.vertex.vos;

import com.vertex.vos.Objects.HoverAnimation;
import com.vertex.vos.Objects.UserSession;
import com.vertex.vos.Utilities.ToDoAlert;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class DiscountNavigationController implements Initializable {
    private AnchorPane contentPane;
    @FXML
    private VBox lineBox;

    @FXML
    private VBox typeBox;

    private final HistoryManager historyManager = new HistoryManager();

    private int currentNavigationId = -1; // Initialize to a default value
    @FXML
    private VBox grossBox;

    public void setContentPane(AnchorPane contentPane) {
        this.contentPane = contentPane;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        animationSetUp();
        lineBox.setOnMouseClicked(mouseEvent -> loadContent("tableManager.fxml", "line_discount"));
        typeBox.setOnMouseClicked(mouseEvent -> loadContent("tableManager.fxml", "discount_type"));
        grossBox.setOnMouseClicked(mouseEvent -> ToDoAlert.showToDoAlert());
    }

    @FXML
    private void loadContent(String fxmlFileName, String registrationType) {
        System.out.println("Loading content: " + fxmlFileName + " for registration type: " + registrationType); // Debug statement
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFileName));
            Parent content = loader.load();

            // Set the controller for the loaded FXML file
            if (fxmlFileName.equals("tableManager.fxml")) {
                TableManagerController controller = loader.getController();
                controller.setRegistrationType(registrationType);
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

    private void animationSetUp() {
        new HoverAnimation(typeBox);
        new HoverAnimation(lineBox);
        new HoverAnimation(grossBox);
    }
}
