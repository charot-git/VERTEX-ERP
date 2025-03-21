package com.vertex.vos;

import com.vertex.vos.DAO.DispatchPlanDAO;
import com.vertex.vos.Enums.DispatchStatus;
import com.vertex.vos.Objects.*;
import com.vertex.vos.Utilities.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.TransferMode;
import lombok.Setter;
import org.controlsfx.control.textfield.TextFields;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class DispatchPlanFormController {

    public TableColumn <SalesOrder, String> provinceCol;
    public TableColumn<SalesOrder, String> cityCol;
    public TableColumn <SalesOrder, String>selectedProvinceCol;
    public TableColumn<SalesOrder, String> selectedCityCol;
    @FXML
    private TableView<SalesOrder> availableOrdersTable;
    private final ObservableList<SalesOrder> availableOrdersList = FXCollections.observableArrayList();

    @FXML
    private TableView<SalesOrder> selectedOrdersTable;
    private final ObservableList<SalesOrder> selectedOrdersList = FXCollections.observableArrayList();

    @FXML
    private TableColumn<SalesOrder, String> customerCol, supplierCol;

    @FXML
    private TableColumn<SalesOrder, Double> totalAmountCol;

    @FXML
    private TableColumn<SalesOrder, String> selectedCustomerCol, selectedSupplierCol;

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
    private TextField clusterField, driverField;

    @FXML
    private ComboBox<DispatchStatus> statusField;

    @FXML
    public void initialize() {
        TableViewFormatter.formatTableView(availableOrdersTable);
        TableViewFormatter.formatTableView(selectedOrdersTable);
        statusField.setItems(FXCollections.observableArrayList(DispatchStatus.values()));

        availableOrdersTable.setItems(availableOrdersList);
        selectedOrdersTable.setItems(selectedOrdersList);

        statusField.setEditable(false);

        // Set up TableColumn bindings using standard Java getters
        provinceCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCustomer().getProvince()));
        cityCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCustomer().getCity()));
        customerCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCustomer().getStoreName()));
        supplierCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getSupplier().getSupplierName()));
        totalAmountCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getTotalAmount()));

        selectedProvinceCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCustomer().getProvince()));
        selectedCityCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCustomer().getCity()));
        selectedCustomerCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCustomer().getStoreName()));
        selectedSupplierCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getSupplier().getSupplierName()));
        selectedTotalAmountCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getTotalAmount()));

        Platform.runLater(() -> {
            if (preDispatchPlanListController != null) {
                addListeners();
            }

            if (dispatchPlan.getStatus() == DispatchStatus.PICKING) {
                selectedOrdersTable.setDisable(true);
                availableOrdersTable.setDisable(true);
                confirmButton.setDisable(true);
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

        selectedOrdersList.addListener((ListChangeListener.Change<? extends SalesOrder> c) -> {
            while (c.next()) {
                updateAmount();
            }
        });

        selectedOrdersTable.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.DELETE) {
                availableOrdersList.add(selectedOrdersTable.getSelectionModel().getSelectedItem());
                selectedOrdersList.remove(selectedOrdersTable.getSelectionModel().getSelectedItem());
            }
        });


    }

    private void addListeners() {

        TextFields.bindAutoCompletion(driverField, preDispatchPlanListController.drivers.stream().map(driver -> driver.getUser_fname() + " " + driver.getUser_lname()).toList());
        TextFields.bindAutoCompletion(clusterField, preDispatchPlanListController.clusters.stream().map(Cluster::getClusterName).collect(Collectors.toList()));
        statusField.setItems(FXCollections.observableArrayList(DispatchStatus.values()));
        driverField.textProperty().addListener((observable, oldValue, newValue) -> {
            dispatchPlan.setDriver(preDispatchPlanListController.drivers.stream().filter(driver -> (driver.getUser_fname() + " " + driver.getUser_lname()).equals(newValue)).findFirst().orElse(null));
            loadAvailableOrders();
        });
        clusterField.textProperty().addListener(((observable, oldValue, newValue) -> {
            dispatchPlan.setCluster(preDispatchPlanListController.clusters.stream().filter(cluster -> cluster.getClusterName().equals(newValue)).findFirst().orElse(null));
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

        confirmButton.setOnAction(event -> insertDispatchPlan());
    }


    private void insertDispatchPlan() {
        ConfirmationAlert confirmationAlert = new ConfirmationAlert("Create Dispatch Plan?", "Please confirm availability of sales orders and vehicle", "This will update the sales order to For Consolidation", true);
        if (confirmationAlert.showAndWait()) {
            if (dispatchPlanDAO.saveDispatch(dispatchPlan)) {
                if (DialogUtils.showConfirmationDialog("Saved", "Close this window?")) {
                    preDispatchPlanListController.newDispatchPlanStage.close();
                }
                preDispatchPlanListController.loadDispatchPlanList();
                confirmButton.setDisable(true);
            }
        }
    }

    private void updateAmount() {
        double dispatchAmount = calculateDispatchAmount();
        totalAmountField.setText(String.format("%.2f", dispatchAmount));
    }

    private double calculateDispatchAmount() {
        return selectedOrdersTable.getItems().stream().mapToDouble(SalesOrder::getTotalAmount).sum();
    }

    private void loadAvailableOrders() {
        if (dispatchPlan != null && dispatchPlan.getDriver() != null && dispatchPlan.getCluster() != null && dispatchPlan.getDispatchDate() != null) {
            availableOrdersList.clear();
            availableOrdersList.addAll(dispatchPlanDAO.getAvailableOrders(dispatchPlan.getDriver(), dispatchPlan.getCluster(), dispatchPlan.getDispatchDate()));
        }
    }


    @Setter
    PreDispatchPlanListController preDispatchPlanListController;

    public void openDispatchPlan(DispatchPlan selectedItem) {
        this.dispatchPlan = selectedItem;
        docNoLabel.setText(dispatchPlan.getDispatchNo());
        driverField.setText(dispatchPlan.getDriver() == null ? "" : dispatchPlan.getDriver().getUser_fname() + " " + dispatchPlan.getDriver().getUser_lname());
        clusterField.setText(dispatchPlan.getCluster() == null ? "" : dispatchPlan.getCluster().getClusterName());
        dateField.setValue(dispatchPlan.getDispatchDate().toLocalDateTime().toLocalDate());
        statusField.setValue(dispatchPlan.getStatus());
        selectedOrdersList.setAll(dispatchPlan.getSalesOrders());
        totalAmountField.setText(String.format("%.2f", dispatchPlan.getTotalAmount()));
        confirmButton.setText("Update");

        loadAvailableOrders();
        confirmButton.setOnAction(event -> {
            updateDispatch();
        });
    }

    private void updateDispatch() {
        ConfirmationAlert confirmationAlert = new ConfirmationAlert("Update Dispatch Plan?", "Please confirm availability of sales orders and staff", "This will update the sales order to For Consolidation", true);
        if (confirmationAlert.showAndWait()) {
            if (dispatchPlanDAO.updateDispatch(dispatchPlan)) {
                if (DialogUtils.showConfirmationDialog("Updated", "Close this window?")) {
                    preDispatchPlanListController.dispatchPlanStage.close();
                    preDispatchPlanListController.dispatchPlanStage = null;
                }
                preDispatchPlanListController.loadDispatchPlanList();
                confirmButton.setDisable(true);
            }
        }
    }
}

