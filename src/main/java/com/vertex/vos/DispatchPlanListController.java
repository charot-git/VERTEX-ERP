package com.vertex.vos;

import com.vertex.vos.DAO.ClusterDAO;
import com.vertex.vos.DAO.DispatchPlanDAO;
import com.vertex.vos.Enums.DispatchStatus;
import com.vertex.vos.Objects.*;
import com.vertex.vos.Utilities.*;
import javafx.application.Platform;
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
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.stage.Stage;
import lombok.Getter;
import org.controlsfx.control.textfield.TextFields;

import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

public class DispatchPlanListController implements Initializable {

    @FXML
    private Button addDispatchButton;

    @FXML
    private TableColumn<DispatchPlan, String> clusterCol;

    @FXML
    private TextField clusterFilter;

    @FXML
    private DatePicker dateFromFilter;

    @FXML
    private DatePicker dateToFilter;

    @FXML
    private TableColumn<DispatchPlan, Double> dispatchAmountCol;

    @FXML
    private TableColumn<DispatchPlan, String> dispatchByCol;

    @FXML
    private TableColumn<DispatchPlan, String> dispatchDateCol;

    @FXML
    private TableColumn<DispatchPlan, String> dispatchNoCol;

    @FXML
    private TextField dispatchNoFilter;

    @FXML
    private TableView<DispatchPlan> dispatchPlanTableView;

    @FXML
    private TableColumn<DispatchPlan, String> statusCol;

    @FXML
    private TextField statusFilter;

    @FXML
    private TableColumn<DispatchPlan, String> driverCol;

    @FXML
    private TextField driverFilter;

    @Getter
    private final ObservableList<DispatchPlan> dispatchPlans = FXCollections.observableArrayList();
    private final DispatchPlanDAO dispatchPlanDAO = new DispatchPlanDAO();

    private int offset = 0;
    private final int limit = 35;

    private Cluster selectedCluster;
    private Vehicle selectedVehicle;
    private DispatchStatus selectedStatus;

    ObservableList<Cluster> clusters = FXCollections.observableArrayList();
    ObservableList<DispatchStatus> statuses = FXCollections.observableArrayList();
    ObservableList<User> drivers = FXCollections.observableArrayList();

    ClusterDAO clusterDAO = new ClusterDAO();
    EmployeeDAO employeeDAO = new EmployeeDAO();


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTableColumns();
        dispatchPlanTableView.setItems(dispatchPlans);
        clusters = FXCollections.observableArrayList(clusterDAO.getAllClusters());
        drivers = employeeDAO.getAllEmployeesWhereDepartment(8);
        statuses = FXCollections.observableArrayList(DispatchStatus.values());
        loadDispatchPlans();

        TextFields.bindAutoCompletion(driverFilter, drivers.stream().map(driver -> driver.getUser_fname() + " " + driver.getUser_lname()).toList());
        TextFields.bindAutoCompletion(clusterFilter, clusters.stream().map(Cluster::getClusterName).toList());
    }

    Stage dispatchPlanStage;

    private void openSelectedDispatchPlan(DispatchPlan selectedItem) {
        if (dispatchPlanStage == null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("DispatchPlanForm.fxml"));
                Parent root = loader.load();
                DispatchPlanFormController controller = loader.getController();
                selectedItem.setSalesOrders(dispatchPlanDAO.getSalesOrdersForDispatchPlan(selectedItem.getDispatchId()));
                controller.openDispatchPlan(selectedItem);
                controller.setDispatchPlanListController(this);
                dispatchPlanStage = new Stage();
                dispatchPlanStage.setTitle("New Dispatch Plan");
                dispatchPlanStage.setMaximized(true);
                dispatchPlanStage.setScene(new Scene(root));
                dispatchPlanStage.show();
                dispatchPlanStage.setOnCloseRequest(event -> dispatchPlanStage = null);
            } catch (IOException e) {
                DialogUtils.showErrorMessage("Error", "Unable to open.");
                e.printStackTrace();
            }
        } else {
            dispatchPlanStage.toFront();
        }
    }

    private void setupTableColumns() {
        TableViewFormatter.formatTableView(dispatchPlanTableView);
        dispatchNoCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDispatchNo()));

        dispatchDateCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDispatchDate() != null ? cellData.getValue().getDispatchDate().toString() : "N/A"));

        clusterCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getCluster() != null ? cellData.getValue().getCluster().getClusterName() : "N/A"));

        driverCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDriver() != null ? cellData.getValue().getDriver().getUser_fname() + " " + cellData.getValue().getDriver().getUser_lname() : "N/A"));

        dispatchAmountCol.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getTotalAmount()));

        dispatchByCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        (cellData.getValue().getCreatedBy() != null) ?
                                cellData.getValue().getCreatedBy().getUser_fname() + " " + cellData.getValue().getCreatedBy().getUser_lname()
                                : "N/A"
                ));

        statusCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getStatus() != null ? cellData.getValue().getStatus().name() : "N/A"));
    }

    private void loadDispatchPlans() {
        Timestamp fromDate = dateFromFilter.getValue() != null ? Timestamp.valueOf(dateFromFilter.getValue().atStartOfDay()) : null;
        Timestamp toDate = dateToFilter.getValue() != null ? Timestamp.valueOf(dateToFilter.getValue().atStartOfDay()) : null;

        dispatchPlanTableView.setPlaceholder(new ProgressIndicator()); // Show loading indicator

        CompletableFuture.runAsync(() -> {
            try {
                ObservableList<DispatchPlan> newDispatchPlans = FXCollections.observableArrayList(
                        dispatchPlanDAO.getAllDispatchPlans(
                                offset, limit,
                                dispatchNoFilter.getText(),
                                selectedCluster,
                                selectedVehicle,
                                selectedStatus,
                                fromDate,
                                toDate
                        ));

                Platform.runLater(() -> {
                    dispatchPlans.setAll(newDispatchPlans);

                    if (dispatchPlans.isEmpty()) {
                        dispatchPlanTableView.setPlaceholder(new Label("No dispatch plans found."));
                    }
                });

            } catch (Exception e) {
                e.printStackTrace(); // Log the error
                Platform.runLater(() -> {
                    DialogUtils.showErrorMessage("Error", "Failed to load dispatch plans.");
                    dispatchPlanTableView.setPlaceholder(new Label("Failed to load data."));
                });
            }
        });
    }


    public void loadDispatchPlanList() {
        loadDispatchPlans();
        addDispatchButton.setOnAction(event -> addNewDispatchPlan());
    }

    Stage newDispatchPlanStage;

    private void addNewDispatchPlan() {
        if (newDispatchPlanStage == null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("DispatchPlanForm.fxml"));
                Parent root = loader.load();
                DispatchPlanFormController controller = loader.getController();
                controller.setDispatchPlanListController(this);
                controller.createNewDispatchPlan();
                newDispatchPlanStage = new Stage();
                newDispatchPlanStage.setTitle("New Dispatch Plan");
                newDispatchPlanStage.setMaximized(true);
                newDispatchPlanStage.setScene(new Scene(root));
                newDispatchPlanStage.show();
                newDispatchPlanStage.setOnCloseRequest(event -> newDispatchPlanStage = null);
            } catch (IOException e) {
                DialogUtils.showErrorMessage("Error", "Unable to open.");
                e.printStackTrace();
            }
        } else {
            newDispatchPlanStage.toFront();
        }
    }

    public void setConsolidation(Consolidation consolidation) {
        selectedStatus = DispatchStatus.PENDING;
        loadDispatchPlans();
        setupDragAndDrop();
    }

    private void setupDragAndDrop() {
        dispatchPlanTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        dispatchPlanTableView.setOnDragDetected(event -> {
            if (!dispatchPlanTableView.getSelectionModel().isEmpty()) {
                Dragboard db = dispatchPlanTableView.startDragAndDrop(TransferMode.MOVE);
                ClipboardContent content = new ClipboardContent();
                DragDropDataStore.setDraggedItems(dispatchPlanTableView.getSelectionModel().getSelectedItems());
                content.putString("dragged");

                db.setContent(content);
                event.consume();
            }
        });
    }

    public void setConsolidationSubModulesController(ConsolidationSubModulesController consolidationSubModulesController) {
        dispatchPlanTableView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                openSelectedDispatchPlan(dispatchPlanTableView.getSelectionModel().getSelectedItem());
            }
        });
    }
}
