package com.vertex.vos;

import com.vertex.vos.DAO.DispatchPlanDAO;
import com.vertex.vos.Enums.DispatchStatus;
import com.vertex.vos.Objects.Cluster;
import com.vertex.vos.Objects.DispatchPlan;
import com.vertex.vos.Objects.SalesOrder;
import com.vertex.vos.Objects.Vehicle;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import lombok.Setter;
import org.controlsfx.control.textfield.TextFields;

import java.sql.Timestamp;
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
    }

    DispatchPlanDAO dispatchPlanDAO = new DispatchPlanDAO();

    DispatchPlan dispatchPlan;

    public void createNewDispatchPlan() {
        dispatchPlan = new DispatchPlan();
        dispatchPlan.setDispatchNo(dispatchPlanDAO.generateDispatchNo());
        docNoLabel.setText(dispatchPlan.getDispatchNo());
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

    }

    private void loadAvailableOrders() {
        if (dispatchPlan != null && dispatchPlan.getVehicle() != null && dispatchPlan.getCluster() != null && dispatchPlan.getDispatchDate() != null) {
            availableOrdersList.clear();
            availableOrdersList.addAll(dispatchPlanDAO.getAvailableOrders(dispatchPlan.getVehicle(), dispatchPlan.getCluster(), dispatchPlan.getDispatchDate()));
        }
    }


    @Setter
    DispatchPlanListController dispatchPlanListController;
}

