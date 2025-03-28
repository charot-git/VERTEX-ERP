package com.vertex.vos;

import com.vertex.vos.Objects.UserSession;
import com.vertex.vos.Utilities.DialogUtils;
import com.vertex.vos.Utilities.HistoryManager;
import com.vertex.vos.Utilities.ModuleManager;
import com.vertex.vos.Utilities.WindowLoader;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;


public class InternalOperationsContentController implements Initializable {
    @FXML
    public VBox openStockTransfer;
    public VBox openSalesEncodingTemp;
    public VBox openPhysicalInventory;
    public VBox openOffsettingModule;
    public VBox openRafModule;
    public VBox openBadStockTransfer;
    public VBox openConsolidation;
    public TilePane interOps1TilePane;
    public TilePane interOps2TilePane;
    public TilePane interOps3TilePane;
    public VBox openInventoryControls;
    public VBox openSummaryReport;
    public VBox openPurchaseOrder;
    @Setter
    private AnchorPane contentPane; // Declare contentPane variable
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
    ProgressIndicator progressIndicator = new ProgressIndicator();

    private final HistoryManager historyManager = new HistoryManager();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        List<VBox> vboxes = List.of(openSummaryReport, openPurchaseOrder, openInventoryControls, openConsolidation, openBadStockTransfer, openRafModule, openOffsettingModule, openReceiving, openLogistics, openPickList, openSalesInvoice, openSalesOrder, openInventoryLedger, openStockTransfer, openSalesReturns, openSalesEncodingTemp, openPhysicalInventory);
        ModuleManager moduleManager1 = new ModuleManager(interOps1TilePane, vboxes);
        ModuleManager moduleManager2 = new ModuleManager(interOps2TilePane, vboxes);
        ModuleManager moduleManager3 = new ModuleManager(interOps3TilePane, vboxes);
        moduleManager1.updateTilePane();
        moduleManager2.updateTilePane();
        moduleManager3.updateTilePane();

        new HoverAnimation(openConsolidation);
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
        new HoverAnimation(openOffsettingModule);
        new HoverAnimation(openRafModule);
        new HoverAnimation(openBadStockTransfer);

        openReceiving.setOnMouseClicked(event -> {
            WindowLoader.openWindowAsync(openReceiving, "com/vertex/vos/receivingIOperations.fxml", "Receiving", controller -> {
                ((ReceivingIOperationsController) controller).setContentPane(contentPane);
            });
        });
        openSalesReturns.setOnMouseClicked(event -> {
            openSalesReturnsWindow();
        });
        openLogistics.setOnMouseClicked(event -> {
            loadContent("LogisticNavigation.fxml", "logistics_dispatch");
        });
        openPickList.setOnMouseClicked(event -> {
            WindowLoader.openWindowAsync(openPickList, "com/vertex/vos/PickLists.fxml", "Pick Lists", controller -> {
                ((PickListsController) controller).loadPickLists();
            });
        });
        openSalesInvoice.setOnMouseClicked(event -> {
            WindowLoader.openWindowAsync(openSalesInvoice, "com/vertex/vos/SalesInvoices.fxml", "Sales Invoices", controller -> {
                ((SalesInvoicesController) controller).loadSalesInvoices();
            });
        });
        openSalesOrder.setOnMouseClicked(event -> {
            WindowLoader.openWindowAsync(openSalesOrder, "com/vertex/vos/SalesOrderList.fxml", "Sales Order List", controller -> {
                ((SalesOrderListController) controller).loadSalesOrder();
            });
        });

        openInventoryLedger.setOnMouseClicked(event -> {
            WindowLoader.openWindowAsync(openSalesOrder, "com/vertex/vos/inventoryLedgerIOperations.fxml", "Sales Order List", controller -> {
                ((InventoryLedgerIOperationsController) controller).setContentPane(contentPane);
            });
        });
        openStockTransfer.setOnMouseClicked(event -> {
            openStockTransferWindow();
        });
        openBadStockTransfer.setOnMouseClicked(event -> {
            openBadStockTransferWindow();
        });

        openSalesEncodingTemp.setOnMouseClicked(event -> {
            openSalesEncodingTempWindow();
        });
        openPhysicalInventory.setOnMouseClicked(event -> {
            openPhysicalInventoryWindow();
        });
        openOffsettingModule.setOnMouseClicked(event -> {
            openOffsettingWindow();
        });
        openRafModule.setOnMouseClicked(event -> {
            openRafModuleWindow();
        });
        openConsolidation.setOnMouseClicked(event -> {
            loadContent("ConsolidationSubModules.fxml", "");
        });
        openPurchaseOrder.setOnMouseClicked(mouseEvent -> {
            openPurchaseOrderWindow();
        });
        openSummaryReport.setOnMouseClicked(mouseEvent -> {
            openPurchaseOrderSummaryWindow();
        });
    }

    Stage purchaseOrderSummaryStage;

    private void openPurchaseOrderSummaryWindow() {
        if (purchaseOrderSummaryStage == null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("purchaseOrderConfirmationAccounting.fxml"));
                Parent root = loader.load();
                purchaseOrderSummaryStage = new Stage();
                purchaseOrderSummaryStage.setTitle("Purchase Order Summary");
                purchaseOrderSummaryStage.setMaximized(true);
                purchaseOrderSummaryStage.setScene(new Scene(root));
                purchaseOrderSummaryStage.show();

                // Reset reference when the stage is closed
                purchaseOrderSummaryStage.setOnCloseRequest(event -> purchaseOrderSummaryStage = null);
            } catch (IOException e) {
                DialogUtils.showErrorMessage("Error", "Unable to open purchase order summary.");
                e.printStackTrace();
            }
        }
    }

    Stage purchaseOrderStage;

    private void openPurchaseOrderWindow() {
        if (purchaseOrderStage == null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("purchaseOrderTypeAccounting.fxml"));
                Parent root = loader.load();
                purchaseOrderStage = new Stage();
                purchaseOrderStage.setTitle("Purchase Order");
                purchaseOrderStage.setMaximized(true);
                purchaseOrderStage.setScene(new Scene(root));
                purchaseOrderStage.show();

                // Reset reference when the stage is closed
                purchaseOrderStage.setOnCloseRequest(event -> purchaseOrderStage = null);

            } catch (IOException e) {
                DialogUtils.showErrorMessage("Error", "Unable to open purchase order window.");
                e.printStackTrace();
            }
        } else {
            purchaseOrderStage.toFront();
        }
    }


    @Getter
    private Stage pickListStage;

    private void openPickListWindow() {
        if (pickListStage == null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("PickLists.fxml"));
                Parent root = loader.load();
                PickListsController controller = loader.getController();
                controller.loadPickLists();
                controller.setInternalOperationsContentController(this);

                pickListStage = new Stage();
                pickListStage.setTitle("Pick Lists");
                pickListStage.setMaximized(true);
                pickListStage.setScene(new Scene(root));
                pickListStage.show();

                // Reset reference when the stage is closed
                pickListStage.setOnCloseRequest(event -> pickListStage = null);

            } catch (IOException e) {
                DialogUtils.showErrorMessage("Error", "Unable to open.");
                e.printStackTrace();
            }
        } else {
            if (!pickListStage.isShowing()) {
                pickListStage.show();
            }
        }
    }

    private Stage tripSummaryStage;

    private void openTripSummaryWindow() {
        if (tripSummaryStage == null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("TripSummaryList.fxml"));
                Parent root = loader.load();
                TripSummaryListController controller = loader.getController();
                tripSummaryStage = new Stage();
                tripSummaryStage.setTitle("Trip Summary List");
                tripSummaryStage.setMaximized(true);
                tripSummaryStage.setScene(new Scene(root));
                controller.loadTripSummaryList();
                tripSummaryStage.show();

                // Reset reference when the stage is closed
                tripSummaryStage.setOnCloseRequest(event -> tripSummaryStage = null);

            } catch (IOException e) {
                DialogUtils.showErrorMessage("Error", "Unable to open.");
                e.printStackTrace();
            }
        } else {
            tripSummaryStage.toFront();
        }
    }


    private void openBadStockTransferWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("StockTransferList.fxml"));
            Parent root = loader.load();
            StockTransferListController controller = loader.getController();
            controller.loadBadStockTransfer();
            Stage stage = new Stage();
            stage.setTitle("Bad Stock Transfer List");
            stage.setMaximized(true);
            stage.setScene(new Scene(root));
            controller.setStage(stage);
            stage.show();
        } catch (IOException e) {
            DialogUtils.showErrorMessage("Error", "Unable to open.");
            e.printStackTrace();
        }
    }

    private void openStockTransferWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("StockTransferList.fxml"));
            Parent root = loader.load();
            StockTransferListController controller = loader.getController();
            controller.loadStockTransfer();
            Stage stage = new Stage();
            stage.setTitle("Stock Transfer List");
            stage.setMaximized(true);
            stage.setScene(new Scene(root));
            controller.setStage(stage);
            stage.show();
        } catch (IOException e) {
            DialogUtils.showErrorMessage("Error", "Unable to open.");
            e.printStackTrace();
        }
    }

    private void openRafModuleWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("RemittanceFindings.fxml"));
            Parent root = loader.load();
            RemittanceFindingsController controller = loader.getController();
            controller.loadRemittanceFindings();
            Stage stage = new Stage();
            stage.setTitle("Remittance Audit Findings");
            stage.setMaximized(true);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            DialogUtils.showErrorMessage("Error", "Unable to open.");
            e.printStackTrace();
        }
    }

    private void openOffsettingWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("PhysicalInventorySummary.fxml"));
            Parent root = loader.load();
            PhysicalInventorySummaryController controller = loader.getController();
            controller.loadPhysicalInventoryDataForOffsetting();
            Stage stage = new Stage();
            stage.setTitle("Offsetting");
            stage.setMaximized(true);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            DialogUtils.showErrorMessage("Error", "Unable to open.");
            e.printStackTrace();
        }

    }

    private void openPhysicalInventoryWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("PhysicalInventorySummary.fxml"));
            Parent root = loader.load();
            PhysicalInventorySummaryController controller = loader.getController();
            controller.loadPhysicalInventory();
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
    private void loadContent(String fxmlFileName, String type) {
        System.out.println("Loading content: " + fxmlFileName); // Debug statement
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFileName));
            Parent content = loader.load();

            switch (fxmlFileName) {
                case "inventoryLedgerIOperations.fxml" -> {
                    InventoryLedgerIOperationsController controller = loader.getController();
                    controller.setContentPane(contentPane);
                }
                case "stockTransfer.fxml" -> {
                    StockTransferController controller = loader.getController();
                    controller.setContentPane(contentPane);
                    controller.createNewGoodStockTransfer();
                }
                case "tableManager.fxml" -> {
                    TableManagerController controller = loader.getController();
                    controller.setContentPane(contentPane);
                    controller.setRegistrationType(type);
                }
                case "ConsolidationSubModules.fxml" -> {
                    ConsolidationSubModulesController controller = loader.getController();
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
