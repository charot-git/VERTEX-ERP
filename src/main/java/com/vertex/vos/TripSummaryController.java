package com.vertex.vos;

import com.vertex.vos.Objects.*;
import com.vertex.vos.Utilities.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;

public class TripSummaryController {

    public Label date;
    public Tab mis;
    public Tab logistics;
    public Tab trip_staff;
    public TabPane uacTabPane;

    @FXML
    private VBox Delivery;
    @FXML
    private VBox Trucks;
    @FXML
    private VBox addBoxes;
    @FXML
    private VBox addProductButton;
    @FXML
    private Label addProductLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private TableView<SalesOrderHeader> approvedSalesOrders;
    @FXML
    private Button confirmButton;
    @FXML
    private DatePicker dateOfDispatch;
    @FXML
    private ComboBox<String> delivery;
    @FXML
    private TableView<SalesOrderHeader> salesOrderForTripSummary;
    @FXML
    private ComboBox<String> truck;
    @FXML
    private ComboBox<String> baranggay;
    @FXML
    private ComboBox<String> city;
    @FXML
    private ComboBox<String> province;

    private final EmployeeDAO employeeDAO = new EmployeeDAO();
    private final BranchDAO branchDAO = new BranchDAO();
    private final SalesOrderDAO salesDAO = new SalesOrderDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();
    private LocationComboBoxUtil locationComboBoxUtil;

    @FXML
    private Label tripNo;
    @FXML
    private HBox addHelper;
    @FXML
    private TableView<TripSummaryStaff> logisticsTable;
    @FXML
    private TableColumn<TripSummaryStaff, String> logisticName;

    @FXML
    private TableColumn<TripSummaryStaff, String> logisticRole;
    @FXML
    private AnchorPane tripPane;
    @FXML
    private AnchorPane allocationPane;
    @FXML
    private SplitPane orderSplitPane;

    private final ObservableList<TripSummaryStaff> logisticsStaffList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupDragAndDrop();
    }

    DocumentNumbersDAO numbersDAO = new DocumentNumbersDAO();
    TripSummaryDAO tripSummaryDAO = new TripSummaryDAO();
    TripSummaryDetailsDAO tripSummaryDetailsDAO = new TripSummaryDetailsDAO();
    TripSummaryStaffDAO tripSummaryStaffDAO = new TripSummaryStaffDAO();

    private final ObservableList<SalesOrderHeader> approvedSalesOrderList = FXCollections.observableArrayList();

    void createNewTrip() {
        initializeTrip();
        populateComboBoxes();
        initializeTableViewColumns();
        initializeLocationComboBoxes();
        initializeLogistics();
        setupDragAndDrop();
        confirmButton.setText("Entry");
    }

    private void initializeTrip() {
        TripSummary tripSummary = new TripSummary();
        tripSummary.setTripNo(String.valueOf(numbersDAO.getNextTripNumber()));
        tripSummary.setStatus("Entry");
        tripNo.setText("TRIP SUMMARY #" + tripSummary.getTripNo());
        statusLabel.setText(tripSummary.getStatus());
        confirmButton.setOnMouseClicked(mouseEvent -> {
            try {
                saveTrip(tripSummary);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void saveTrip(TripSummary tripSummary) throws SQLException {
        ConfirmationAlert confirmationAlert = new ConfirmationAlert("Trip creation", "Create trip " + tripSummary.getTripNo() + "?", "Please double check first", true);
        boolean confirmed = confirmationAlert.showAndWait();
        if (confirmed) {
            tripSummary.setStatus("Pending");
            tripSummary.setCreatedBy(UserSession.getInstance().getUserId());
            tripSummary.setTotalSalesOrders(approvedSalesOrderList.size());
            if (tripSummaryDAO.saveTripSummary(tripSummary)) {
                if (tripSummaryDetailsDAO.saveTripSummaryDetails(approvedSalesOrderList, Integer.parseInt(tripSummary.getTripNo()))) {
                    confirmButton.setDisable(true);
                    DialogUtils.showConfirmationDialog("Success", "Trip successfully saved");
                    for (SalesOrderHeader item : approvedSalesOrderList) {
                        item.setStatus("For Layout");
                    }
                }
            }
        }
    }

    private void initializeLogistics() {
        initializeLogisticsTableColumns();
        delivery.getEditor().textProperty().addListener((obs, oldVal, newVal) -> {
            TripSummaryStaff existingDriver = null;
            for (TripSummaryStaff staff : logisticsStaffList) {
                if ("Driver".equalsIgnoreCase(staff.getRole())) {
                    existingDriver = staff;
                    break;
                }
            }

            if (existingDriver != null) {
                logisticsStaffList.remove(existingDriver);
            }

            TripSummaryStaff newDriver = new TripSummaryStaff();
            newDriver.setStaffName(newVal);
            newDriver.setRole("Driver");
            logisticsStaffList.add(newDriver);
            logisticsTable.setItems(FXCollections.observableArrayList(logisticsStaffList));
        });

        addHelper.setOnMouseClicked(mouseEvent -> addHelperForTrip());
    }

    private void initializeLogisticsTableColumns() {
        logisticsTable.getColumns().clear();

        TableColumn<TripSummaryStaff, String> logisticNameCol = new TableColumn<>("Logistic Name");
        logisticNameCol.setCellValueFactory(new PropertyValueFactory<>("staffName"));

        TableColumn<TripSummaryStaff, String> logisticRoleCol = new TableColumn<>("Logistic Role");
        logisticRoleCol.setCellValueFactory(new PropertyValueFactory<>("role"));

        logisticsTable.getColumns().addAll(logisticNameCol, logisticRoleCol);
        logisticsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private final ObservableList<String> logisticEmployees = FXCollections.observableArrayList();

    @FXML
    private void addHelperForTrip() {
        String selectedEmployee = EntryAlert.showEntryComboBox(
                "Add Helper",
                "Add helper for the trip",
                "Please make sure of the staff's availability before entry",
                logisticEmployees,
                new StringConverter<String>() {
                    @Override
                    public String toString(String object) {
                        return object; // Display employee names in ComboBox
                    }

                    @Override
                    public String fromString(String string) {
                        return string;
                    }
                }
        );

        if (selectedEmployee != null) {
            TripSummaryStaff staff = new TripSummaryStaff();
            staff.setStaffName(selectedEmployee);
            staff.setRole("Helper");
            logisticsStaffList.add(staff);
            logisticEmployees.remove(selectedEmployee);
            logisticsTable.setItems(FXCollections.observableArrayList(logisticsStaffList));
        }
    }

    VehicleDAO vehicleDAO = new VehicleDAO();
    ObservableList<String> truckPlates = vehicleDAO.getAllVehicleTruckPlates();

    private void populateComboBoxes() {
        logisticEmployees.addAll(employeeDAO.getAllEmployeeNamesWhereDepartment(8));
        delivery.setItems(logisticEmployees);
        truck.setItems(truckPlates);
        ComboBoxFilterUtil.setupComboBoxFilter(delivery, logisticEmployees);
        ComboBoxFilterUtil.setupComboBoxFilter(truck, truckPlates);
    }

    private void initializeTableViewColumns() {
        addTableColumns(approvedSalesOrders);
        addTableColumns(salesOrderForTripSummary);
        updateSalesOrders();
    }

    private void addTableColumns(TableView<SalesOrderHeader> tableView) {
        tableView.getColumns().clear();

        TableColumn<SalesOrderHeader, Integer> orderIdCol = new TableColumn<>("Order ID");
        orderIdCol.setCellValueFactory(new PropertyValueFactory<>("orderId"));

        TableColumn<SalesOrderHeader, String> customerNameCol = new TableColumn<>("Customer Name");
        customerNameCol.setCellValueFactory(new PropertyValueFactory<>("customerName"));

        TableColumn<SalesOrderHeader, Timestamp> orderDateCol = new TableColumn<>("Order Date");
        orderDateCol.setCellValueFactory(new PropertyValueFactory<>("orderDate"));

        TableColumn<SalesOrderHeader, BigDecimal> amountDueCol = new TableColumn<>("Amount Due");
        amountDueCol.setCellValueFactory(new PropertyValueFactory<>("amountDue"));

        TableColumn<SalesOrderHeader, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        tableView.getColumns().addAll(orderIdCol, customerNameCol, orderDateCol, amountDueCol, statusCol);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
    }

    private void initializeLocationComboBoxes() {
        locationComboBoxUtil = new LocationComboBoxUtil(province, city, baranggay);
        locationComboBoxUtil.initializeComboBoxes();

        province.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> updateSalesOrders());
        city.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> updateSalesOrders());
        baranggay.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> updateSalesOrders());
    }

    private ObservableList<SalesOrderHeader> fetchSalesOrders() {
        ObservableList<SalesOrderHeader> salesOrders = FXCollections.observableArrayList();
        try {
            List<SalesOrderHeader> fetchedOrders = salesDAO.getSalesOrderPerStatus("Allocation");

            String selectedProvince = province.getSelectionModel().getSelectedItem();
            String selectedCity = city.getSelectionModel().getSelectedItem();
            String selectedBarangay = baranggay.getSelectionModel().getSelectedItem();

            for (SalesOrderHeader order : fetchedOrders) {
                if (approvedSalesOrderList.stream().anyMatch(o -> o.getOrderId() == order.getOrderId())) {
                    continue;
                }

                Customer customer = customerDAO.getCustomer(order.getCustomerId());
                if (customer != null && matchesLocationCriteria(customer, selectedProvince, selectedCity, selectedBarangay)) {
                    salesOrders.add(order);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return salesOrders;
    }

    private boolean matchesLocationCriteria(Customer customer, String selectedProvince, String selectedCity, String selectedBarangay) {
        return (selectedProvince == null || selectedProvince.equals(customer.getProvince())) &&
                (selectedCity == null || selectedCity.equals(customer.getCity())) &&
                (selectedBarangay == null || selectedBarangay.equals(customer.getBrgy()));
    }

    private void setupDragAndDrop() {
        approvedSalesOrders.setRowFactory(tv -> {
            TableRow<SalesOrderHeader> row = new TableRow<>();
            row.setOnDragDetected(event -> {
                if (!row.isEmpty()) {
                    Dragboard db = row.startDragAndDrop(TransferMode.COPY);
                    ClipboardContent content = new ClipboardContent();
                    ObservableList<SalesOrderHeader> selectedItems = approvedSalesOrders.getSelectionModel().getSelectedItems();
                    content.put(DataFormat.PLAIN_TEXT, String.valueOf(selectedItems.size())); // Placeholder content
                    db.setContent(content);
                }
                event.consume();
            });
            return row;
        });

        salesOrderForTripSummary.setOnDragOver(event -> {
            if (event.getGestureSource() != salesOrderForTripSummary && event.getDragboard().hasContent(DataFormat.PLAIN_TEXT)) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });

        salesOrderForTripSummary.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasContent(DataFormat.PLAIN_TEXT)) {
                ObservableList<SalesOrderHeader> selectedItems = approvedSalesOrders.getSelectionModel().getSelectedItems();
                salesOrderForTripSummary.getItems().addAll(selectedItems);
                for (SalesOrderHeader item : selectedItems) {
                    item.setStatus("Allocating");
                    approvedSalesOrderList.add(item); // Add to approvedSalesOrderList
                }
                approvedSalesOrders.getItems().removeAll(selectedItems);

                success = true;
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }

    private void updateSalesOrders() {
        approvedSalesOrders.setItems(fetchSalesOrders());
    }

    private TableManagerController tableManagerController;

    public void setTableManager(TableManagerController tableManagerController) {
        this.tableManagerController = tableManagerController;
    }

    int department = UserSession.getInstance().getUserDepartment();

    void initData(TripSummary selectedTrip) throws SQLException {
        initializeTableViewColumns();
        statusLabel.setText(selectedTrip.getStatus());
        orderSplitPane.getItems().remove(allocationPane);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateCreated = formatter.format(selectedTrip.getCreatedAt());
        date.setText(dateCreated);
        if (department == 8) {
            loadLogisticsUI(selectedTrip);
        } else if (department == 7) {
            loadMISUI(selectedTrip);
        }
        ObservableList<Integer> ordersForTrip = tripSummaryDetailsDAO.getDetailsByTripId(Integer.parseInt(selectedTrip.getTripNo()));
        for (int orderId : ordersForTrip) {
            salesOrderForTripSummary.getItems().add(salesDAO.getOrderHeaderById(orderId));
        }
        uacTabPane.getTabs().remove(mis);
    }

    private void loadMISUI(TripSummary selectedTrip) throws SQLException {

    }

    private void loadLogisticsUI(TripSummary selectedTrip) {
        initializeLogistics();
        addHelper.setOnMouseClicked(mouseEvent -> addHelperForTrip());
    }
}
