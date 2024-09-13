package com.vertex.vos;

import com.vertex.vos.HoverAnimation;
import com.vertex.vos.Objects.UserSession;
import com.vertex.vos.Utilities.HistoryManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class TermsNavigationController implements Initializable {
    AnchorPane contentPane;
    @FXML
    private VBox deliveryTermsBox;
    @FXML
    private VBox paymentTermsBox;
    @FXML
    private VBox discountSetUpBox;

    public void setContentPane(AnchorPane contentPane) {
        this.contentPane = contentPane;
    }

    private final HistoryManager historyManager = new HistoryManager();

    private int currentNavigationId = -1; // Initialize to a default value

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        new HoverAnimation(deliveryTermsBox);
        new HoverAnimation(paymentTermsBox);
        new HoverAnimation(discountSetUpBox);

        deliveryTermsBox.setOnMouseClicked(mouseEvent -> loadContent("tableManager.fxml", "delivery_terms"));
        paymentTermsBox.setOnMouseClicked(mouseEvent -> loadContent("tableManager.fxml", "payment_terms"));
        discountSetUpBox.setOnMouseClicked(mouseEvent -> loadContent("tableManager.fxml", "discount_setup"));


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
}
