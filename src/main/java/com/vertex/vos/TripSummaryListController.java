package com.vertex.vos;

import com.vertex.vos.Objects.TripSummary;
import com.vertex.vos.DAO.TripSummaryDAO;
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
import lombok.Getter;

import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ResourceBundle;

public class TripSummaryListController implements Initializable {

    @FXML
    private Button addButton;

    @FXML
    private DatePicker dateFromFilter;
    @FXML
    private DatePicker dateToFilter;

    @FXML
    private TableColumn<TripSummary, String> clusterCol;
    @FXML
    private TableColumn<TripSummary, String> dispatchByCol;
    @FXML
    private TableColumn<TripSummary, String> statusCol;
    @FXML
    private TableColumn<TripSummary, Double> tripAmountCol;
    @FXML
    private TableColumn<TripSummary, Timestamp> tripDateCol;
    @FXML
    private TableColumn<TripSummary, String> tripNoCol;
    @FXML
    private TableColumn<TripSummary, String> vehicleCol;

    @FXML
    private TableView<TripSummary> tripSummaryTableView;

    @FXML
    private TextField clusterFilter;
    @FXML
    private TextField statusFilter;
    @FXML
    private TextField tripNoFilter;
    @FXML
    private TextField vehicleFilter;

    ObservableList<TripSummary> tripSummaryList = FXCollections.observableArrayList();

    TripSummaryDAO tripSummaryDAO = new TripSummaryDAO();

    public void loadTripSummaryList() {
        String tripNo = tripNoFilter.getText().trim();
        String cluster = clusterFilter.getText().trim();
        String status = statusFilter.getText().trim();
        String vehicle = vehicleFilter.getText().trim();
        Timestamp dateFrom = dateFromFilter.getValue() != null ? Timestamp.valueOf(dateFromFilter.getValue().atStartOfDay()) : null;
        Timestamp dateTo = dateToFilter.getValue() != null ? Timestamp.valueOf(dateToFilter.getValue().atTime(23, 59, 59)) : null;

        TripSummaryDAO tripSummaryDAO = new TripSummaryDAO();
        tripSummaryList.setAll(tripSummaryDAO.getFilteredTripSummaries(tripNo, cluster, status, vehicle, dateFrom, dateTo));

        if (tripSummaryList.isEmpty()) {
            tripSummaryTableView.setPlaceholder(new Label("No trip summaries found."));
        }

        addButton.setOnAction(event -> addTripSummary());
    }

    @Getter
    private Stage newTripSummaryStage;

    private void addTripSummary() {
        if (newTripSummaryStage == null || !newTripSummaryStage.isShowing()) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("TripSummaryForm.fxml"));
                Parent root = loader.load();  // Load FXML before getting controller

                TripSummaryFormController controller = loader.getController();
                controller.createNewTripSummary();
                controller.setTripSummaryListController(this);

                newTripSummaryStage = new Stage();
                newTripSummaryStage.setTitle("Add Trip Summary");
                newTripSummaryStage.setScene(new Scene(root));
                newTripSummaryStage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            newTripSummaryStage.toFront();  // Bring the existing stage to the front
        }
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        tripSummaryTableView.setItems(tripSummaryList);
        tripNoCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTripNo()));
        tripDateCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getTripDate()));
        clusterCol.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getCluster() != null ? cellData.getValue().getCluster().getClusterName() : "N/A"
        ));
        vehicleCol.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getVehicle() != null ? cellData.getValue().getVehicle().getVehiclePlate() : "N/A"
        ));
        tripAmountCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getTripAmount()));
        dispatchByCol.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getDispatchBy() != null ?
                        cellData.getValue().getDispatchBy().getUser_fname() + " " + cellData.getValue().getDispatchBy().getUser_lname()
                        : "N/A"
        ));
        statusCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus().name()));
    }
}
