package com.vertex.vos;

import com.vertex.vos.DAO.ClusterDAO;
import com.vertex.vos.DAO.DispatchPlanDAO;
import com.vertex.vos.Enums.DispatchStatus;
import com.vertex.vos.Objects.Cluster;
import com.vertex.vos.Objects.DispatchPlan;
import com.vertex.vos.Objects.Vehicle;
import com.vertex.vos.Utilities.DialogUtils;
import com.vertex.vos.Utilities.VehicleDAO;
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
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.time.LocalDate;
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
    private TableColumn<DispatchPlan, String> vehicleCol;

    @FXML
    private TextField vehicleFilter;

    private final ObservableList<DispatchPlan> dispatchPlans = FXCollections.observableArrayList();
    private final DispatchPlanDAO dispatchPlanDAO = new DispatchPlanDAO();

    private int offset = 0;
    private final int limit = 35;

    private Cluster selectedCluster;
    private Vehicle selectedVehicle;
    private DispatchStatus selectedStatus;

    ObservableList<Cluster> clusters = FXCollections.observableArrayList();
    ObservableList<Vehicle> vehicles = FXCollections.observableArrayList();
    ObservableList<DispatchStatus> statuses = FXCollections.observableArrayList();

    ClusterDAO clusterDAO = new ClusterDAO();
    VehicleDAO vehicleDAO = new VehicleDAO();


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTableColumns();
        dispatchPlanTableView.setItems(dispatchPlans);

        clusters = FXCollections.observableArrayList(clusterDAO.getAllClusters());
        vehicles = vehicleDAO.getAllVehicles();
        statuses = FXCollections.observableArrayList(DispatchStatus.values());


        loadDispatchPlans();
    }

    private void setupTableColumns() {
        dispatchNoCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDispatchNo()));

        dispatchDateCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDispatchDate() != null ? cellData.getValue().getDispatchDate().toString() : "N/A"));

        clusterCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getCluster() != null ? cellData.getValue().getCluster().getClusterName() : "N/A"));

        vehicleCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getVehicle() != null ? cellData.getValue().getVehicle().getVehiclePlate() : "N/A"));

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

        CompletableFuture.runAsync(() -> {
            ObservableList<DispatchPlan> newDispatchPlans = FXCollections.observableArrayList(dispatchPlanDAO.getAllDispatchPlans(
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
                } else {
                    dispatchPlanTableView.setPlaceholder(null);
                }
            });
        });

        dispatchPlanTableView.setPlaceholder(new ProgressIndicator());
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
                controller.createNewDispatchPlan();
                controller.setDispatchPlanListController(this);
                newDispatchPlanStage = new Stage();
                newDispatchPlanStage.setTitle("New Dispatch Plan");
                newDispatchPlanStage.setMaximized(true);
                newDispatchPlanStage.setScene(new Scene(root));
                newDispatchPlanStage.show();
            } catch (IOException e) {
                DialogUtils.showErrorMessage("Error", "Unable to open.");
                e.printStackTrace();
            }
        } else {
            newDispatchPlanStage.toFront();
        }
    }
}
