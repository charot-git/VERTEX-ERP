package com.vertex.vos;

import com.vertex.vos.Objects.UserSession;
import com.vertex.vos.Utilities.DialogUtils;
import com.vertex.vos.Utilities.HistoryManager;
import com.vertex.vos.Utilities.ModuleManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.Setter;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class InternalOperationsContentController implements Initializable {
    @FXML
    public VBox openStockTransfer;
    public TilePane tilePane;
    public VBox openSalesEncodingTemp;
    public VBox openPhysicalInventory;
    @Setter
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
    @FXML
    private VBox openSalesReturns;

    private final HistoryManager historyManager = new HistoryManager();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        List<VBox> vboxes = List.of(openTripSummary, openReceiving, openLogistics, openPickList, openSalesInvoice, openSalesOrder, openInventoryLedger, openStockTransfer, openSalesReturns, openSalesEncodingTemp, openPhysicalInventory);
        ModuleManager moduleManager = new ModuleManager(tilePane, vboxes);
        moduleManager.updateTilePane();

        new HoverAnimation(openTripSummary);
        new HoverAnimation(openReceiving);
        new HoverAnimation(openLogistics);
        new HoverAnimation(openPickList);
        new HoverAnimation(openSalesInvoice);
        new HoverAnimation(openSalesOrder);
        new HoverAnimation(openInventoryLedger);
        new HoverAnimation(openStockTransfer);
        new HoverAnimation(openSalesReturns);
        new HoverAnimation(openSalesEncodingTemp);
        new HoverAnimation(openPhysicalInventory);

        openTripSummary.setOnMouseClicked(event -> {
            loadContent("tableManager.fxml", "trip_summary");
        });
        openReceiving.setOnMouseClicked(event -> {
            openReceivingWindow();
        });
        openSalesReturns.setOnMouseClicked(event -> {
            openSalesReturnsWindow();
        });
        openLogistics.setOnMouseClicked(event -> {
            loadContent("tableManager.fxml", "logistics_dispatch");
        });
        openPickList.setOnMouseClicked(event -> {
            loadContent("pickList.fxml", "");
        });
        openSalesInvoice.setOnMouseClicked(event -> {
            openSalesInvoiceWindow();
        });
        openSalesOrder.setOnMouseClicked(event -> {
            loadContent("tableManager.fxml", "sales_order");
        });
        openInventoryLedger.setOnMouseClicked(event -> {
            loadContent("inventoryLedgerIOperations.fxml", "salesOrder");
        });
        openStockTransfer.setOnMouseClicked(event -> {
            loadContent("tableManager.fxml", "stock_transfer");
        });

        openSalesEncodingTemp.setOnMouseClicked(event -> {
            openSalesEncodingTempWindow();
        });
        openPhysicalInventory.setOnMouseClicked(event -> {
            openPhysicalInventoryWindow();
        });
    }

    private void openPhysicalInventoryWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("PhysicalInventorySummary.fxml"));
            Parent root = loader.load();
            PhysicalInventorySummaryController controller = loader.getController();
            Stage stage = new Stage();
            stage.setTitle("Physical Inventory");
            stage.setMaximized(true);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            DialogUtils.showErrorMessage("Error", "Unable to open.");
            e.printStackTrace();
        }

    }

    private void openSalesEncodingTempWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("SalesInvoiceTemporary.fxml"));
            Parent root = loader.load();
            SalesInvoiceTemporaryController controller = loader.getController();


            Stage stage = new Stage();
            stage.setTitle("Sales Encoding Temporary");
            controller.createNewSalesEntry(stage);
            stage.setMaximized(true);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            DialogUtils.showErrorMessage("Error", "Unable to open.");
            e.printStackTrace();
        }

    }

    private void openSalesReturnsWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("SalesReturns.fxml"));
            Parent root = loader.load();
            SalesReturnsListController controller = loader.getController();

            controller.loadSalesReturn();
            Stage stage = new Stage();
            stage.setTitle("Sales Returns");
            stage.setMaximized(true);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            DialogUtils.showErrorMessage("Error", "Unable to open receiving.");
            e.printStackTrace();
        }

    }

    private void openSalesInvoiceWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("SalesInvoices.fxml"));
            Parent root = loader.load();
            SalesInvoicesController controller = loader.getController();

            controller.setContentPane(contentPane);
            controller.loadSalesInvoices();
            controller.invoiceDisplay();
            Stage stage = new Stage();
            stage.setTitle("Sales Invoices");
            stage.setMaximized(true);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            DialogUtils.showErrorMessage("Error", "Unable to open receiving.");
            e.printStackTrace();
        }

    }

    private void openReceivingWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("receivingIOperations.fxml"));
            Parent root = loader.load();
            ReceivingIOperationsController controller = loader.getController();

            controller.setContentPane(contentPane);
            Stage stage = new Stage();
            stage.setTitle("Receiving");
            stage.setMaximized(true);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            DialogUtils.showErrorMessage("Error", "Unable to open receiving.");
            e.printStackTrace();
        }

    }

    private void loadContent(String fxmlFileName, String type) {
        System.out.println("Loading content: " + fxmlFileName); // Debug statement
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFileName));
            Parent content = loader.load();

            switch (fxmlFileName) {
                case "salesOrder.fxml" -> {
                    SalesOrderEntryController controller = loader.getController();
                    controller.setContentPane(contentPane);
                }
                case "inventoryLedgerIOperations.fxml" -> {
                    InventoryLedgerIOperationsController controller = loader.getController();
                    controller.setContentPane(contentPane);
                }
                case "stockTransfer.fxml" -> {
                    StockTransferController controller = loader.getController();
                    controller.setContentPane(contentPane);
                    controller.createNewTransfer();
                }
                case "tableManager.fxml" -> {
                    TableManagerController controller = loader.getController();
                    controller.setContentPane(contentPane);
                    controller.setRegistrationType(type);
                }
                case "pickList.fxml" -> {
                    PickListController controller = loader.getController();
                    controller.setContentPane(contentPane);
                }
            }


            // Add entry to navigation history and get the generated ID
            String sessionId = UserSession.getInstance().getSessionId();
            // Initialize to a default value
            int currentNavigationId = historyManager.addEntry(sessionId, fxmlFileName);

            ContentManager.setContent(contentPane, content); // Assuming contentPane is your AnchorPane
        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception according to your needs
            System.err.println("Error loading " + fxmlFileName + ": " + e.getMessage());
        }
    }
}
