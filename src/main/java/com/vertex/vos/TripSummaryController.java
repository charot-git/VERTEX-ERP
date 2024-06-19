package com.vertex.vos;

import com.vertex.vos.Constructors.Customer;
import com.vertex.vos.Constructors.SalesOrderHeader;
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
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TripSummaryController {

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

    private Set<Integer> movedOrderIds = new HashSet<>();

    @FXML
    public void initialize() {
        createNewTrip();
    }

    void createNewTrip() {
        statusLabel.setText("Entry");
        populateComboBoxes();
        initializeTableViewColumns();
        initializeLocationComboBoxes();
        setupDragAndDrop();
    }

    private void populateComboBoxes() {
        delivery.setItems(employeeDAO.getAllEmployeeNamesWhereDepartment(8));
        truck.setItems(branchDAO.getAllMovingTruckPlate());
    }

    private void initializeTableViewColumns() {
        addTableColumns(approvedSalesOrders);
        addTableColumns(salesOrderForTripSummary);
        updateSalesOrders();
    }

    private void addTableColumns(TableView<SalesOrderHeader> tableView) {
        tableView.getColumns().clear(); // Clear existing columns

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
                if (movedOrderIds.contains(order.getOrderId())) {
                    continue; // Skip orders that are already moved to salesOrderForTripSummary
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
                    movedOrderIds.add(item.getOrderId()); // Track moved order ID
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
}
