package com.vertex.vos;

import com.vertex.vos.Objects.SalesOrder;
import com.vertex.vos.Objects.UserSession;
import com.vertex.vos.Objects.TripSummary;
import com.vertex.vos.Objects.TripSummaryStaff;
import com.vertex.vos.Utilities.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class LogisticsTripSummaryController {

    public ComboBox<String> truckPlate;
    public DatePicker dispatchDate;
    @FXML
    private Button confirmButton;

    @FXML
    private Label date;

    @FXML
    private TableView<TripSummaryStaff> logisticsTable;

    @FXML
    private TableView<SalesOrder> ordersTable;

    @FXML
    private Button selectDriver;

    @FXML
    private Button selectHelper;

    @FXML
    private HBox statusBox;

    @FXML
    private ImageView statusImage;

    @FXML
    private Label statusLabel;

    @FXML
    private Label tripNo;

    TripSummaryDetailsDAO tripSummaryDetailsDAO = new TripSummaryDetailsDAO();
    SalesOrderDAO salesDAO = new SalesOrderDAO();

    ObservableList<TripSummaryStaff> tripSummaryStaffs = FXCollections.observableArrayList();
    VehicleDAO vehicleDAO = new VehicleDAO();

    public void initData(TripSummary selectedTrip) throws SQLException {
        initializeTableViewColumns();

        if (selectedTrip.getStatus().equals("Dispatched")) {
            loadDataForDispatchedTrip(selectedTrip);
        } else if (selectedTrip.getStatus().equals("Pending")) {
            confirmButton.setOnMouseClicked(mouseEvent -> {
                processLogistics(selectedTrip);
            });
        }

        tripNo.setText("TRIP# " + selectedTrip.getTripNo());
        statusLabel.setText(selectedTrip.getStatus());
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateCreated = formatter.format(selectedTrip.getCreatedAt());
        date.setText(dateCreated);

        truckPlate.setItems(FXCollections.observableArrayList(vehicleDAO.getAllVehicleTruckPlatesByStatus("Active")));
        truckPlate.setValue(vehicleDAO.getTruckPlateById(selectedTrip.getVehicleId()));
        dispatchDate.setPromptText(formatter.format(new Timestamp(System.currentTimeMillis())));

        populateOrdersTable(selectedTrip.getTripNo());
        logisticsProcess(selectedTrip.getTripNo());

    }

    private void processLogistics(TripSummary selectedTrip) {
        ConfirmationAlert confirmationAlert = new ConfirmationAlert("Logistics", "Dispatch trip " + selectedTrip.getTripNo() + "?", "Please double check first", true);
        boolean confirmed = confirmationAlert.showAndWait();
        if (confirmed) {
            boolean allPicked = true;
            for (SalesOrder order : ordersTable.getItems()) {
                allPicked = false;
                break;
            }
            if (allPicked) {
                try {
                    saveLogisticsDetails(selectedTrip);
                } catch (SQLException e) {
                    DialogUtils.showErrorMessage("Error", e.getMessage());
                }
            } else {
                DialogUtils.showErrorMessage("Error", "Please pick all items before dispatching");
            }
        }

    }

    private void loadDataForDispatchedTrip(TripSummary selectedTrip) {
        tripSummaryStaffs = tripSummaryDetailsDAO.getTripSummaryStaff(selectedTrip.getTripId());
        logisticsTable.setItems(tripSummaryStaffs);
        truckPlate.setValue(vehicleDAO.getTruckPlateById(selectedTrip.getVehicleId()));
        dispatchDate.setValue(selectedTrip.getTripDate().toLocalDate());
        confirmButton.setDisable(true);
        selectHelper.setDisable(true);
        selectDriver.setDisable(true);
    }

    private void saveLogisticsDetails(TripSummary trip) throws SQLException {
        trip.setDispatchBy(UserSession.getInstance().getUserId());
        trip.setTripDate(Date.valueOf(dispatchDate.getValue()));
        trip.setVehicleId(vehicleDAO.getVehicleIdByName(truckPlate.getSelectionModel().getSelectedItem()));
        trip.setStatus("Dispatched");
        if (tripSummaryDetailsDAO.saveLogisticsDetails(trip)) {
            if (tripSummaryDetailsDAO.saveLogisticsStaff(trip, tripSummaryStaffs)) {
                DialogUtils.showCompletionDialog("Success", "Logistics details successfully saved");
                trip.setStatus("Dispatched");
                statusLabel.setText("Dispatched");
                confirmButton.setDisable(true);
                tableManagerController.loadTripSummary();
            }
        }
    }

    EmployeeDAO employeeDAO = new EmployeeDAO();

    private void logisticsProcess(String tripNo) {

        logisticEmployees.addAll(employeeDAO.getAllEmployeeNamesWhereDepartment(8));

        selectHelper.setOnMouseClicked(mouseEvent -> addHelperForTrip(tripNo));
        selectDriver.setOnMouseClicked(mouseEvent -> addDriverForTrip(tripNo));
    }

    private final ObservableList<String> logisticEmployees = FXCollections.observableArrayList();

    private void addDriverForTrip(String tripNo) {
        String selectedDriver = EntryAlert.showEntryComboBox(
                "Add Driver",
                "Select Driver for the Trip",
                "Please select a driver for the trip",
                logisticEmployees,
                new StringConverter<String>() {
                    @Override
                    public String toString(String object) {
                        return object; // Display driver names in ComboBox
                    }

                    @Override
                    public String fromString(String string) {
                        return string;
                    }
                }
        );

        if (selectedDriver != null) {
            TripSummaryStaff driver = new TripSummaryStaff();
            driver.setStaffName(selectedDriver);
            driver.setRole("Driver");
            tripSummaryStaffs.add(driver);
            logisticEmployees.remove(selectedDriver);
            selectDriver.setDisable(true);
            logisticsTable.setItems(FXCollections.observableArrayList(tripSummaryStaffs));
        }
    }

    private void addHelperForTrip(String tripNo) {
        String selectedDriver = EntryAlert.showEntryComboBox(
                "Add Helper",
                "Select Helper for the Trip",
                "Please select a helper for the trip",
                logisticEmployees,
                new StringConverter<String>() {
                    @Override
                    public String toString(String object) {
                        return object; // Display driver names in ComboBox
                    }

                    @Override
                    public String fromString(String string) {
                        return string;
                    }
                }
        );

        if (selectedDriver != null) {
            TripSummaryStaff driver = new TripSummaryStaff();
            driver.setStaffName(selectedDriver);
            driver.setRole("Helper");
            tripSummaryStaffs.add(driver);
            logisticEmployees.remove(selectedDriver);
            logisticsTable.setItems(FXCollections.observableArrayList(tripSummaryStaffs));
        }
    }

    private void populateOrdersTable(String tripNo) throws SQLException {

    }

    TableManagerController tableManagerController;

    public void setTableManager(TableManagerController tableManagerController) {
        this.tableManagerController = tableManagerController;
    }

    private void initializeTableViewColumns() {
        addColumnsForLogistics(logisticsTable);
    }

    private void addColumnsForLogistics(TableView<TripSummaryStaff> logisticsTable) {
        logisticsTable.getColumns().clear();

        TableColumn<TripSummaryStaff, String> staffNameCol = new TableColumn<>("Staff Name");
        staffNameCol.setCellValueFactory(new PropertyValueFactory<>("staffName"));

        TableColumn<TripSummaryStaff, String> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));

        logisticsTable.getColumns().addAll(staffNameCol, roleCol);
    }

}
