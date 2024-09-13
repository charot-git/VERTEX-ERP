package com.vertex.vos;

import com.vertex.vos.HoverAnimation;
import com.vertex.vos.Utilities.HistoryManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.Setter;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class PurchaseOrderTypeController implements Initializable {
    @FXML
    private VBox openTradePurchaseOrder;
    @FXML
    private VBox openNonTradePurchaseOrder;

    @Setter
    private AnchorPane contentPane; // Declare contentPane variable

    private final HistoryManager historyManager = new HistoryManager();

    private final int currentNavigationId = -1; // Initialize to a default value

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        new HoverAnimation(openNonTradePurchaseOrder);
        new HoverAnimation(openTradePurchaseOrder);

        openNonTradePurchaseOrder.setOnMouseClicked(MouseEvent -> loadContent("purchaseOrderEntryAccounting.fxml", "non-trade"));
        openTradePurchaseOrder.setOnMouseClicked(MouseEvent -> loadContent("purchaseOrderEntryAccounting.fxml", "trade"));
    }

    private void loadContent(String fxmlFileName, String type) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFileName));
            Parent content = loader.load();

            // Create a new stage for the content
            Stage stage = new Stage();
            stage.setTitle("Purchase Order Entry");
            stage.setScene(new Scene(content));
            stage.setMaximized(true);
            if (fxmlFileName.equals("purchaseOrderEntryAccounting.fxml")) {
                PurchaseOrderEntryController controller = loader.getController();
                controller.setPurchaseOrderType(type);
            }

            stage.showAndWait(); // Show the new window and wait for it to close
        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception according to your needs
            System.err.println("Error loading " + fxmlFileName + ": " + e.getMessage());
        }
    }

}
