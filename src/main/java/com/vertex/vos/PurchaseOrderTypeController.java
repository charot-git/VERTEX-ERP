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

public class PurchaseOrderTypeController implements Initializable {
    @FXML
    private VBox openTradePurchaseOrder;
    @FXML
    private VBox openNonTradePurchaseOrder;

    private AnchorPane contentPane; // Declare contentPane variable

    public void setContentPane(AnchorPane contentPane) {
        this.contentPane = contentPane;
    }

    private final HistoryManager historyManager = new HistoryManager();

    private int currentNavigationId = -1; // Initialize to a default value

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        new HoverAnimation(openNonTradePurchaseOrder);
        new HoverAnimation(openTradePurchaseOrder);

        openNonTradePurchaseOrder.setOnMouseClicked(MouseEvent -> loadContent("purchaseOrderEntryAccounting.fxml", "non-trade"));
        openTradePurchaseOrder.setOnMouseClicked(MouseEvent -> loadContent("purchaseOrderEntryAccounting.fxml" ,"trade"));
    }

    private void loadContent(String fxmlFileName, String type) {
        System.out.println("Loading content: " + fxmlFileName); // Debug statement
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFileName));
            Parent content = loader.load();


            if (fxmlFileName.equals("purchaseOrderEntryAccounting.fxml")) {
                PurchaseOrderEntryController controller = loader.getController();
                controller.setContentPane(contentPane);
                controller.setPurchaseOrderType(type);
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
