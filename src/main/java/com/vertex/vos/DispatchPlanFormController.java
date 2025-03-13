package com.vertex.vos;

import com.vertex.vos.DAO.DispatchPlanDAO;
import com.vertex.vos.Enums.DispatchStatus;
import com.vertex.vos.Objects.*;
import com.vertex.vos.Utilities.ConfirmationAlert;
import com.vertex.vos.Utilities.DialogUtils;
import com.vertex.vos.Utilities.DragDropDataStore;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import lombok.Setter;
import org.controlsfx.control.textfield.TextFields;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class DispatchPlanFormController {

    @FXML
    private TableView<SalesOrder> availableOrdersTable;
    private final ObservableList<SalesOrder> availableOrdersList = FXCollections.observableArrayList();

    @FXML
    private TableView<SalesOrder> selectedOrdersTable;
    private final ObservableList<SalesOrder> selectedOrdersList = FXCollections.observableArrayList();

    @FXML
    private TableColumn<SalesOrder, String> customerCol, orderNoCol, purchaseNoCol, salesmanCol, supplierCol;

    @FXML
    private TableColumn<SalesOrder, Double> totalAmountCol;

    @FXML
    private TableColumn<SalesOrder, String> selectedCustomerCol, selectedOrderNoCol, selectedPurchaseNoCol, selectedSalesmanCol, selectedSupplierCol;

    @FXML
    private TableColumn<SalesOrder, Double> selectedTotalAmountCol;

    @FXML
    private Button confirmButton;

    @FXML
    private ButtonBar buttonBar;

    @FXML
    private DatePicker dateField;

    @FXML
    private Label docNoLabel, totalAmountField;

    @FXML
    private TextField clusterField, vehicleField;

    @FXML
    private ComboBox<DispatchStatus> statusField;

    @FXML
    public void initialize() {
        statusField.setItems(FXCollections.observableArrayList(DispatchStatus.values()));

        availableOrdersTable.setItems(availableOrdersList);
        selectedOrdersTable.setItems(selectedOrdersList);

        statusField.setEditable(false);

        // Set up TableColumn bindings using standard Java getters
        customerCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCustomer().getStoreName()));
        orderNoCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getOrderNo()));
        purchaseNoCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getPurchaseNo()));
        salesmanCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getSalesman().getSalesmanName()));
        supplierCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getSupplier().getSupplierName()));
        totalAmountCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getTotalAmount()));

        selectedCustomerCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCustomer().getStoreName()));
        selectedOrderNoCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getOrderNo()));
        selectedPurchaseNoCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getPurchaseNo()));
        selectedSalesmanCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getSalesman().getSalesmanName()));
        selectedSupplierCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getSupplier().getSupplierName()));
        selectedTotalAmountCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getTotalAmount()));

        Platform.runLater(() -> {
            if (dispatchPlanListController != null) {
                TextFields.bindAutoCompletion(vehicleField, dispatchPlanListController.vehicles.stream().map(Vehicle::getVehiclePlate).collect(Collectors.toList()));
                TextFields.bindAutoCompletion(clusterField, dispatchPlanListController.clusters.stream().map(Cluster::getClusterName).collect(Collectors.toList()));
                statusField.setItems(FXCollections.observableArrayList(DispatchStatus.values()));
            }
        });

        availableOrdersTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        availableOrdersTable.setOnDragDetected(event -> {
            if (!availableOrdersTable.getSelectionModel().getSelectedItems().isEmpty()) {
                Dragboard db = availableOrdersTable.startDragAndDrop(TransferMode.MOVE);
                ClipboardContent content = new ClipboardContent();

                DragDropDataStore.setDraggedItems(availableOrdersTable.getSelectionModel().getSelectedItems());
                content.putString("dragged");

                db.setContent(content);
                event.consume();
            }
        });
        selectedOrdersTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        implementReceiveOnDrag();
    }

    private void implementReceiveOnDrag() {
        selectedOrdersTable.setOnDragOver(event -> {
            if (event.getGestureSource() != selectedOrdersTable && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });

        selectedOrdersTable.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;

            if ("dragged".equals(db.getString())) {
                List<SalesOrder> droppedItems = DragDropDataStore.getDraggedItems();

                if (droppedItems == null || droppedItems.isEmpty()) {
                    DialogUtils.showErrorMessage("Error", "No valid items found.");
                    return;
                }


                for (SalesOrder item : droppedItems) {
                    SalesOrder order = getSalesOrder(item);

                    // 🛑 Check if the product is already in the invoice
                    boolean exists = selectedOrdersList.stream()
                            .anyMatch(i -> Objects.equals(i.getOrderNo(), order.getOrderNo()));

                    if (exists) {
                        DialogUtils.showErrorMessage("Error", "Invoice already exists in the trip summary: " + order.getOrderNo());
                    } else {
                        availableOrdersList.remove(item);
                        selectedOrdersList.add(item);
                    }
                }

                success = true;
            }

            event.setDropCompleted(success);
            event.consume();
        });

    }

    private SalesOrder getSalesOrder(SalesOrder item) {
        return item;
    }

    DispatchPlanDAO dispatchPlanDAO = new DispatchPlanDAO();

    DispatchPlan dispatchPlan;

    public void createNewDispatchPlan() {
        dispatchPlan = new DispatchPlan();
        dispatchPlan.setDispatchNo(dispatchPlanDAO.generateDispatchNo());
        dispatchPlan.setStatus(DispatchStatus.PENDING);
        dispatchPlan.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
        dispatchPlan.setCreatedBy(UserSession.getInstance().getUser());
        docNoLabel.setText(dispatchPlan.getDispatchNo());

        statusField.setValue(dispatchPlan.getStatus());
        vehicleField.textProperty().addListener((observable, oldValue, newValue) -> {
            dispatchPlan.setVehicle(dispatchPlanListController.vehicles.stream().filter(vehicle -> vehicle.getVehiclePlate().equals(newValue)).findFirst().orElse(null));
            loadAvailableOrders();
        });
        clusterField.textProperty().addListener(((observable, oldValue, newValue) -> {
            dispatchPlan.setCluster(dispatchPlanListController.clusters.stream().filter(cluster -> cluster.getClusterName().equals(newValue)).findFirst().orElse(null));
            loadAvailableOrders();
        }));
        dateField.valueProperty().addListener(((observable, oldValue, newValue) -> {
            dispatchPlan.setDispatchDate(Timestamp.valueOf(newValue.atStartOfDay()));
            loadAvailableOrders();
        }));
        statusField.valueProperty().addListener(((observable, oldValue, newValue) -> dispatchPlan.setStatus(newValue)));
        totalAmountField.textProperty().addListener(((observable, oldValue, newValue) -> dispatchPlan.setTotalAmount(Double.parseDouble(newValue))));

        selectedOrdersList.addListener((ListChangeListener<SalesOrder>) c -> {
            dispatchPlan.setSalesOrders(selectedOrdersList);
            updateAmount();
        });

        confirmButton.setOnAction(event -> insertDispatchPlan());
    }


    private void insertDispatchPlan() {
        if (dispatchPlan.getTotalAmount() < dispatchPlan.getCluster().getMinimumAmount()) {
            DialogUtils.showErrorMessage("Error", "Cluster amount not met");
            return;
        }

        ConfirmationAlert confirmationAlert = new ConfirmationAlert("Create Dispatch Plan?", "Please confirm availablity of sales orders and vehicle", "Allotting vehicle for deployment will set it to For Loading", true);
        if (confirmationAlert.showAndWait()) {
            if (dispatchPlanDAO.saveDispatch(dispatchPlan)) {
                if (DialogUtils.showConfirmationDialog("Saved", "Close this window?")) {
                    dispatchPlanListController.newDispatchPlanStage.close();
                }
                dispatchPlanListController.loadDispatchPlanList();
                confirmButton.setDisable(true);
            }
        }
    }

    private void updateAmount() {
        double dispatchAmount = calculateDispatchAmount();
        totalAmountField.setText(String.format("%.2f", dispatchAmount));
    }

    private double calculateDispatchAmount() {
        return selectedOrdersList.stream().mapToDouble(SalesOrder::getTotalAmount).sum();
    }

    private void loadAvailableOrders() {
        if (dispatchPlan != null && dispatchPlan.getVehicle() != null && dispatchPlan.getCluster() != null && dispatchPlan.getDispatchDate() != null) {
            availableOrdersList.clear();
            availableOrdersList.addAll(dispatchPlanDAO.getAvailableOrders(dispatchPlan.getVehicle(), dispatchPlan.getCluster(), dispatchPlan.getDispatchDate()));
        }
    }


    @Setter
    DispatchPlanListController dispatchPlanListController;

    public void openDispatchPlan(DispatchPlan selectedItem) {
        this.dispatchPlan = selectedItem;
        docNoLabel.setText(dispatchPlan.getDispatchNo());
        vehicleField.setText(dispatchPlan.getVehicle().getVehiclePlate());
        clusterField.setText(dispatchPlan.getCluster().getClusterName());
        dateField.setValue(dispatchPlan.getDispatchDate().toLocalDateTime().toLocalDate());
        statusField.setValue(dispatchPlan.getStatus());
        selectedOrdersList.setAll(dispatchPlan.getSalesOrders());
        totalAmountField.setText(String.format("%.2f", dispatchPlan.getTotalAmount()));
        confirmButton.setText("Update");

        confirmButton.setOnAction(event -> {
            updateDispatch();
        });
    }

    private void updateDispatch() {
    }
}

