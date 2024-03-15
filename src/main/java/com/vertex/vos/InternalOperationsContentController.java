package com.vertex.vos;

import com.vertex.vos.Constructors.HoverAnimation;
import com.vertex.vos.Constructors.UserSession;
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

public class InternalOperationsContentController implements Initializable {
    private AnchorPane contentPane; // Declare contentPane variable
    @FXML
    private VBox openTripSummary;
    @FXML
    private VBox openLogistics;
    @FXML
    private VBox openPickList;
    @FXML
    private VBox openSalesInvoice;
    @FXML
    private VBox openSalesOrder;
    @FXML
    private VBox openReceiving;
    @FXML
    private VBox openInventoryLedger;

    public void setContentPane(AnchorPane contentPane) {
        this.contentPane = contentPane;
    }

    private final HistoryManager historyManager = new HistoryManager();

    private int currentNavigationId = -1; // Initialize to a default value

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        new HoverAnimation(openTripSummary);
        new HoverAnimation(openReceiving);
        new HoverAnimation(openLogistics);
        new HoverAnimation(openPickList);
        new HoverAnimation(openSalesInvoice);
        new HoverAnimation(openSalesOrder);
        new HoverAnimation(openInventoryLedger);

        openTripSummary.setOnMouseClicked(event -> {
            ToDoAlert.showToDoAlert();
        });
        openReceiving.setOnMouseClicked(event -> {
            loadContent("receivingIOperations.fxml");
        });
        openLogistics.setOnMouseClicked(event -> {
            ToDoAlert.showToDoAlert();
        });
        openPickList.setOnMouseClicked(event -> {
            ToDoAlert.showToDoAlert();
        });
        openSalesInvoice.setOnMouseClicked(event -> {
            ToDoAlert.showToDoAlert();
        });
        openSalesOrder.setOnMouseClicked(event -> {
            loadContent("salesOrderIOperations.fxml");
        });
        openInventoryLedger.setOnMouseClicked(event -> {
            loadContent("inventoryLedgerIOperations.fxml");
        });
    }

    private void loadContent(String fxmlFileName) {
        System.out.println("Loading content: " + fxmlFileName); // Debug statement
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFileName));
            Parent content = loader.load();

            switch (fxmlFileName) {
                case "receivingIOperations.fxml" -> {
                    ReceivingIOperationsController controller = loader.getController();
                    controller.setContentPane(contentPane);
                }
                case "salesOrderIOperations.fxml" -> {
                    SalesOrderIOperationsController controller = loader.getController();
                    controller.setContentPane(contentPane);
                }
                case "inventoryLedgerIOperations.fxml" -> {
                    InventoryLedgerIOperationsController controller = loader.getController();
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
}
