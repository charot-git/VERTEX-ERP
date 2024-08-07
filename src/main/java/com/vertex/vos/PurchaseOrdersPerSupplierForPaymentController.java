package com.vertex.vos;

import com.vertex.vos.Objects.ComboBoxFilterUtil;
import com.vertex.vos.Objects.PurchaseOrder;
import com.vertex.vos.Utilities.DialogUtils;
import com.vertex.vos.Utilities.PurchaseOrderDAO;
import com.vertex.vos.Utilities.SupplierDAO;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

public class PurchaseOrdersPerSupplierForPaymentController implements Initializable {
    @FXML
    private ComboBox<String> supplier;
    @FXML
    private TableView<PurchaseOrder> purchaseOrdersForPayment;
    @FXML
    private TableColumn<PurchaseOrder, Integer> orderNo;
    @FXML
    private TableColumn<PurchaseOrder, BigDecimal> totalAmount;
    @FXML
    private TableColumn<PurchaseOrder, String> paymentStatus;
    @FXML
    private TableColumn<PurchaseOrder, LocalDate> placedOn;
    @FXML
    private TableColumn<PurchaseOrder, String> paymentDue;

    private final SupplierDAO supplierDAO = new SupplierDAO();
    private final PurchaseOrderDAO purchaseOrderDAO = new PurchaseOrderDAO();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initializeTableColumns();
        loadPurchaseOrdersForPayment();
    }

    private void initializeTableColumns() {
        orderNo.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getPurchaseOrderNo()).asObject());
        totalAmount.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getTotalAmount()));
        placedOn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getDateApproved().toLocalDate()));
        paymentStatus.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPaymentStatusString()));

        paymentDue.setCellValueFactory(cellData -> {
            LocalDate leadTimePayment = cellData.getValue().getLeadTimePayment();
            return new SimpleStringProperty(leadTimePayment != null ? leadTimePayment.toString() : "TBD");
        });
    }

    void loadPurchaseOrdersForPayment() {
        CompletableFuture.supplyAsync(() -> {
            try {
                return supplierDAO.getAllSuppliersWithPayables();
            } catch (Exception e) {
                // Log the exception and show an error message on the JavaFX thread
                Platform.runLater(() -> DialogUtils.showErrorMessage("Error", "An error occurred while fetching suppliers: " + e.getMessage()));
                return null;
            }
        }).thenAccept(supplierNames -> {
            if (supplierNames != null) {
                Platform.runLater(() -> {
                    supplier.setItems(supplierNames);
                    ComboBoxFilterUtil.setupComboBoxFilter(supplier, supplierNames);
                });
            }
        }).thenRun(() -> {
            // Add listener to the ComboBox
            Platform.runLater(() -> supplier.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    loadItemsForPayment(newValue);
                }
            }));
        }).thenRun(() -> {
            // Set up the TableRowFactory
            Platform.runLater(() -> purchaseOrdersForPayment.setRowFactory(tv -> {
                TableRow<PurchaseOrder> row = new TableRow<>();
                row.setOnMouseClicked(event -> {
                    if (event.getButton() == MouseButton.SECONDARY && !row.isEmpty()) {
                        PurchaseOrder selectedOrder = row.getItem();
                        payableAction(selectedOrder);
                    }
                });
                return row;
            }));
        }).exceptionally(ex -> {
            Platform.runLater(() -> DialogUtils.showErrorMessage("Error", "An error occurred while setting up the UI: " + ex.getMessage()));
            return null;
        });
    }

    private void payableAction(PurchaseOrder selectedOrder) {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem payItem = new MenuItem("Pay");
        MenuItem voucherItem = new MenuItem("Voucher");
        contextMenu.getItems().addAll(payItem, voucherItem);
        payItem.setOnAction(event -> openPurchaseOrderForPayment(selectedOrder));
        voucherItem.setOnAction(event -> openPurchaseOrderForVoucher(selectedOrder));
        purchaseOrdersForPayment.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                contextMenu.show(purchaseOrdersForPayment, event.getScreenX(), event.getScreenY());
            } else {
                contextMenu.hide();
            }
        });
    }

    private void openPurchaseOrderForVoucher(PurchaseOrder selectedOrder) {
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vertex/vos/VoucherForm.fxml"));
                Parent content = loader.load();
                VoucherFormController controller = loader.getController();

                controller.setPurchaseOrderPaymentList(this);
                controller.initData(selectedOrder);

                Stage stage = new Stage();
                stage.setTitle("Voucher Order#" + selectedOrder.getPurchaseOrderNo());
                stage.setResizable(true);
                stage.setMaximized(true);
                stage.setScene(new Scene(content));
                stage.showAndWait();
            } catch (IOException e) {
                DialogUtils.showErrorMessage("Error", "Failed to load the Payables Form: " + e.getMessage());
                e.printStackTrace();  // Add this for debugging
            } catch (Exception e) {
                DialogUtils.showErrorMessage("Error", "An unexpected error occurred: " + e.getMessage());
                e.printStackTrace();  // Add this for debugging
            }
        });
    }


    public void loadItemsForPayment(String supplierName) {
        CompletableFuture.supplyAsync(() -> {
            try {
                int supplierId = supplierDAO.getSupplierIdByName(supplierName);
                return purchaseOrderDAO.getPurchaserOrdersForPaymentBySupplier(supplierId);
            } catch (Exception e) {
                // Handle exception and show error message on JavaFX thread
                Platform.runLater(() -> DialogUtils.showErrorMessage("Error", "An error occurred while loading items: " + e.getMessage()));
                return null;
            }
        }).thenAccept(purchaseOrders -> {
            if (purchaseOrders != null) {
                Platform.runLater(() -> {
                    ObservableList<PurchaseOrder> observablePurchaseOrders = FXCollections.observableArrayList(purchaseOrders);
                    purchaseOrdersForPayment.setItems(observablePurchaseOrders);
                });
            }
        }).exceptionally(ex -> {
            Platform.runLater(() -> DialogUtils.showErrorMessage("Error", "An error occurred while processing items: " + ex.getMessage()));
            return null;
        });
    }

    private void openPurchaseOrderForPayment(PurchaseOrder selectedOrder) {
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vertex/vos/PayablesForm.fxml"));
                Parent content = loader.load();
                PayablesFormController controller = loader.getController();

                controller.setPurchaseOrderPaymentList(this);
                controller.initializePayment(selectedOrder);

                Stage stage = new Stage();
                stage.setTitle("Pay Order#" + selectedOrder.getPurchaseOrderNo());
                stage.setResizable(true);
                stage.setMaximized(true);
                stage.setScene(new Scene(content));
                stage.showAndWait();
            } catch (IOException e) {
                DialogUtils.showErrorMessage("Error", "Failed to load the Payables Form: " + e.getMessage());
                e.printStackTrace();  // Add this for debugging
            } catch (Exception e) {
                DialogUtils.showErrorMessage("Error", "An unexpected error occurred: " + e.getMessage());
                e.printStackTrace();  // Add this for debugging
            }
        });
    }

}
