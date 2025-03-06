package com.vertex.vos;

import com.vertex.vos.Objects.*;
import com.vertex.vos.Utilities.*;
import com.zaxxer.hikari.HikariDataSource;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.Setter;
import org.apache.commons.lang3.RandomStringUtils;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Date;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.vertex.vos.Objects.Salesman;


public class TableManagerController implements Initializable {

    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    private final HistoryManager historyManager = new HistoryManager();
    public ToggleButton toggleButton;
    public AnchorPane defaultContent;

    @Setter
    private PurchaseOrderEntryController purchaseOrderEntryController;
    @FXML
    private AnchorPane tableAnchor;
    private final TilePane tilePane = new TilePane();
    @FXML
    private VBox addButton;


    private final SupplierDAO supplierDAO = new SupplierDAO();
    BrandDAO brandDAO = new BrandDAO();
    DiscountDAO discountDAO = new DiscountDAO();
    CategoriesDAO categoriesDAO = new CategoriesDAO();
    SegmentDAO segmentDAO = new SegmentDAO();
    SectionsDAO sectionsDAO = new SectionsDAO();
    ProductsPerSupplierDAO productsPerSupplierDAO = new ProductsPerSupplierDAO();
    @Setter
    private String registrationType;
    @FXML
    private TextField searchBar;
    @FXML
    private TextField categoryBar;

    @Setter
    private AnchorPane contentPane; // Declare contentPane variable


    private ObservableList<Map<String, String>> brandData;
    private final ObservableList<Map<String, String>> classData = FXCollections.observableArrayList();
    private final ObservableList<Map<String, String>> segmentData = FXCollections.observableArrayList();
    private final ObservableList<Map<String, String>> categoryData = FXCollections.observableArrayList();
    private final ObservableList<Map<String, String>> natureData = FXCollections.observableArrayList();
    private final ObservableList<Map<String, String>> sectionData = FXCollections.observableArrayList();
    private final ObservableList<Map<String, String>> unitData = FXCollections.observableArrayList();

    @FXML
    private ImageView tableImg, addImage;
    @FXML
    private Label tableHeader;
    @FXML
    private TableView defaultTable;
    @FXML
    private TableColumn column1;
    @FXML
    private Label columnHeader1;
    @FXML
    private TableColumn column2;
    @FXML
    private Label columnHeader2;
    @FXML
    private TableColumn column3;
    @FXML
    private Label columnHeader3;
    @FXML
    private TableColumn column4;
    @FXML
    private Label columnHeader4;
    @FXML
    private TableColumn column5;
    @FXML
    private Label columnHeader5;
    @FXML
    private TableColumn column6;
    @FXML
    private Label columnHeader6;
    @FXML
    private TableColumn column7;
    @FXML
    private Label columnHeader7;
    @FXML
    private TableColumn column8;
    @FXML
    private Label columnHeader8;

    UnitDAO unitDAO = new UnitDAO();


    private void populateProductsPerSupplierTable(List<Product> products) {
        defaultTable.getItems().clear();
        defaultTable.getItems().addAll(products);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        defaultTable.setVisible(false);
        tilePane.setHgap(10);
        tilePane.setVgap(10);
        tilePane.setPadding(new Insets(10));

        ProgressIndicator progressIndicator = new ProgressIndicator();
        defaultTable.setPlaceholder(progressIndicator);

        Platform.runLater(() -> {

            if (!registrationType.contains("employee")) {
                Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/Add.png")));
                addImage.setImage(image);
            }

            switch (registrationType) {
                case "company" -> loadCompanyTable();
                case "branch" -> loadBranchTable();
                case "employee" -> loadEmployeeTable();
                case "product" -> loadProductTable();
                case "product_supplier" -> tableHeader.setText("Add a product to supplier");
                case "supplier" -> loadSupplierTable();
                case "system_employee" -> loadSystemEmployeeTable();
                case "industry" -> loadIndustryTable();
                case "division" -> loadDivisionTable();
                case "department" -> loadDepartmentTable();
                case "category" -> loadCategoryTable();
                case "customer" -> loadCustomerTable();
                case "vehicles" -> loadVehicleTable();
                case "brand" -> loadBrandTable();
                case "segment" -> loadSegmentTable();
                case "delivery_terms" -> loadDeliveryTerms();
                case "payment_terms" -> loadPaymentTerms();
                case "class" -> loadClassTable();
                case "nature" -> loadNatureTable();
                case "logistics_dispatch" -> {
                    try {
                        loadLogisticsDispatchTable();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
                case "section" -> loadSectionTable();
                case "unit" -> loadUnitTable();
                case "chart_of_accounts" -> loadChartOfAccountsTable();
                case "purchase_order_products" -> tableHeader.setText("Select products");
                case "branch_selection_po" -> loadBranchForPOTable();
                case "discount_type" -> loadDiscountTypeTable();
                case "line_discount" -> loadLineDiscountTable();
                case "assets_and_equipments" -> loadAssetsAndEquipmentTable();
                case "salesman" -> loadSalesmanTable();
                case "bank" -> loadBankTable();
                case "trip_summary" -> {
                    try {
                        loadTripSummary();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
                case "so_to_si" -> tableHeader.setText("Select SO to convert");
                case "stock_transfer_products" -> tableHeader.setText("Select products for stock transfer");
                case "sales_order" -> loadSalesOrders();
                case "stock_transfer" -> {
                    try {
                        loadStockTransfer();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
                /*case "sales_invoice" -> loadSalesInvoice();*/

                default -> tableHeader.setText("Unknown Type");
            }
            defaultTable.setVisible(true);
        });

        defaultTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) { // Check for double-click
                if (registrationType.equals("product_supplier")) {
                    System.out.println(registrationType);
                } else if (registrationType.equals("purchase_order_products")) {
                    System.out.println(registrationType);
                } else {
                    handleTableDoubleClick(defaultTable.getSelectionModel().getSelectedItem());
                }
            }
        });
    }

    BankAccountDAO bankAccountDAO = new BankAccountDAO();
    ObservableList<BankAccount> bankAccountList = FXCollections.observableArrayList();

    public void loadBankTable() {
        // Set table header and image
        tableHeader.setText("Bank Accounts");
        Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/icons8-bank-48.png")));
        tableImg.setImage(image);

        // Clear and load bank account data
        bankAccountList.clear();
        bankAccountList.addAll(bankAccountDAO.getAllBankAccounts());

        // Clear existing columns and items in the table
        defaultTable.getColumns().clear();
        defaultTable.getItems().clear();

        // Set the items to display in the table
        defaultTable.setItems(bankAccountList);

        // Define columns for the table
        TableColumn<BankAccount, String> accountNumberCol = new TableColumn<>("Account Number");
        accountNumberCol.setCellValueFactory(new PropertyValueFactory<>("accountNumber"));

        TableColumn<BankAccount, String> bankDescriptionCol = new TableColumn<>("Description");
        bankDescriptionCol.setCellValueFactory(new PropertyValueFactory<>("bankDescription"));

        TableColumn<BankAccount, String> bankNameCol = new TableColumn<>("Bank Name");
        bankNameCol.setCellValueFactory(new PropertyValueFactory<>("bankName"));

        TableColumn<BankAccount, String> branchCol = new TableColumn<>("Branch");
        branchCol.setCellValueFactory(new PropertyValueFactory<>("branch"));

        // Add columns to the table
        defaultTable.getColumns().addAll(accountNumberCol, bankDescriptionCol, bankNameCol, branchCol);

        // Optional: Handle adding a new bank account
        addButton.setOnMouseClicked(event -> addNewBank());

        defaultTable.setRowFactory(tv -> {
            TableRow<BankAccount> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    BankAccount selectedAccount = row.getItem();
                    // Here you can open details of selectedAccount
                    openBankDetails(selectedAccount);
                }
            });
            return row;
        });
    }

    private void openBankDetails(BankAccount selectedAccount) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("BankAccountForm.fxml"));
            Parent content = loader.load();
            BankAccountFormController controller = loader.getController();

            controller.setTableManager(this);
            controller.initData(selectedAccount);

            Stage stage = new Stage();
            stage.setTitle("Register Bank");
            stage.setResizable(true);
            stage.setScene(new Scene(content));
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception according to your needs
        }

    }

    private void addNewBank() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("BankAccountForm.fxml"));
            Parent content = loader.load();
            BankAccountFormController controller = loader.getController();

            controller.setTableManager(this);
            controller.registerBank();

            Stage stage = new Stage();
            stage.setTitle("Register Bank");
            stage.setResizable(true);
            stage.setScene(new Scene(content));
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception according to your needs
        }
    }


    VehicleDAO vehicleDAO = new VehicleDAO();

    TripSummaryDAO tripSummaryDAO = new TripSummaryDAO();

    public void loadLogisticsDispatchTable() throws SQLException {
        tableHeader.setText("Logistics"); // Update with your header text
        Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/Delivery.png"))); // Update with your image path
        tableImg.setImage(image); // Update with your ImageView or similar component

        defaultTable.getColumns().clear(); // Clear existing columns
        defaultTable.getItems().clear(); // Clear existing items

        TableColumn<TripSummary, String> tripNoCol = new TableColumn<>("Trip No");
        tripNoCol.setCellValueFactory(new PropertyValueFactory<>("tripNo"));

        TableColumn<TripSummary, Date> tripDateCol = new TableColumn<>("Trip Date");
        tripDateCol.setCellValueFactory(new PropertyValueFactory<>("tripDate"));

        TableColumn<TripSummary, String> vehicleIdCol = new TableColumn<>("Vehicle ID");
        vehicleIdCol.setCellValueFactory(new PropertyValueFactory<>("vehicleId"));

        TableColumn<TripSummary, Integer> totalSalesOrdersCol = new TableColumn<>("Total Sales Orders");
        totalSalesOrdersCol.setCellValueFactory(new PropertyValueFactory<>("totalSalesOrders"));

        TableColumn<TripSummary, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Add columns to the table
        defaultTable.getColumns().addAll(tripNoCol, tripDateCol, vehicleIdCol, totalSalesOrdersCol, statusCol);

        // Set row factory to handle double-click event
        defaultTable.setRowFactory(tv -> {
            TableRow<TripSummary> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    TripSummary selectedTrip = row.getItem();
                    openTripSummaryForLogistics(selectedTrip);
                }
            });
            return row;
        });
        defaultTable.setItems(tripSummaryDAO.getAllTripSummaries()); // Assumes a method in TripSummaryDAO that returns all trip summaries
    }

    public void loadTripSummary() throws SQLException {
        tableHeader.setText("Trip Summary"); // Update with your header text
        Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/Delivery.png"))); // Update with your image path
        tableImg.setImage(image); // Update with your ImageView or similar component

        defaultTable.getColumns().clear();
        defaultTable.getItems().clear();

        TableColumn<TripSummary, String> tripNoCol = new TableColumn<>("Trip No");
        tripNoCol.setCellValueFactory(new PropertyValueFactory<>("tripNo"));

        TableColumn<TripSummary, Date> tripDateCol = new TableColumn<>("Trip Date");
        tripDateCol.setCellValueFactory(new PropertyValueFactory<>("tripDate"));

        TableColumn<TripSummary, String> vehicleIdCol = new TableColumn<>("Vehicle ID");
        vehicleIdCol.setCellValueFactory(new PropertyValueFactory<>("vehicleId"));

        TableColumn<TripSummary, Integer> totalSalesOrdersCol = new TableColumn<>("Total Sales Orders");
        totalSalesOrdersCol.setCellValueFactory(new PropertyValueFactory<>("totalSalesOrders"));

        TableColumn<TripSummary, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Add columns to the table
        defaultTable.getColumns().addAll(tripNoCol, tripDateCol, vehicleIdCol, totalSalesOrdersCol, statusCol);

        // Set row factory to handle double-click event
        defaultTable.setRowFactory(tv -> {
            TableRow<TripSummary> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    TripSummary selectedTrip = row.getItem();
                    openTripSummary(selectedTrip);
                }
            });
            return row;
        });
        ObservableList<TripSummary> tripSummaries = tripSummaryDAO.getAllTripSummaries();
        if (!tripSummaries.isEmpty()) {
            defaultTable.setItems(tripSummaries);
        } else {
            defaultTable.setPlaceholder(new Label("No trip summaries found"));
        }
    }

    private void openTripSummaryForLogistics(TripSummary selectedTrip) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("logisticsTripSummary.fxml"));
            Parent root = loader.load();

            LogisticsTripSummaryController controller = loader.getController();
            controller.initData(selectedTrip);
            controller.setTableManager(this);

            Stage stage = new Stage();
            stage.setTitle("Trip#" + selectedTrip.getTripNo());
            stage.setMaximized(true);
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }


    }

    private void openTripSummary(TripSummary selectedTrip) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("tripSummary.fxml"));
            Parent root = loader.load();

            TripSummaryController controller = loader.getController();
            controller.initData(selectedTrip);
            controller.setTableManager(this);

            Stage stage = new Stage();
            stage.setTitle("Trip#" + selectedTrip.getTripNo());
            stage.setMaximized(true);
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }


    public void loadVehicleTable() {
        tableHeader.setText("Vehicles"); // Update with your header text
        Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/Semi Truck.png"))); // Update with your image path
        tableImg.setImage(image); // Update with your ImageView or similar component

        defaultTable.getColumns().clear(); // Clear existing columns
        defaultTable.getItems().clear(); // Clear existing items

        TableColumn<Vehicle, String> vehicleTypeCol = new TableColumn<>("Vehicle Type");
        vehicleTypeCol.setCellValueFactory(new PropertyValueFactory<>("vehicleTypeString"));

        TableColumn<Vehicle, String> vehiclePlateCol = new TableColumn<>("Vehicle Plate");
        vehiclePlateCol.setCellValueFactory(new PropertyValueFactory<>("vehiclePlate"));

        TableColumn<Vehicle, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Add columns to the table
        defaultTable.getColumns().addAll(vehiclePlateCol, vehicleTypeCol, statusCol);

        // Set row factory to handle double-click event
        defaultTable.setRowFactory(tv -> {
            TableRow<Vehicle> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Vehicle selectedVehicle = row.getItem();
                    openVehicle(selectedVehicle);
                }
            });
            return row;
        });
        defaultTable.setItems(vehicleDAO.getAllVehicles());
    }

    private void openVehicle(Vehicle selectedVehicle) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Vehicle.fxml"));
            Parent root = loader.load();

            // Access the controller and call a method
            VehicleController controller = loader.getController();
            controller.initData(selectedVehicle);
            controller.setTableManager(this);

            Stage stage = new Stage();
            stage.setTitle("Vehicle Details");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*public void loadSalesInvoice() {
        tableHeader.setText("Sales Invoices");
        Image image = new Image(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/Invoice.png"));
        tableImg.setImage(image);

        defaultTable.getColumns().clear();
        defaultTable.getItems().clear();

        //create columns
        TableColumn<SalesInvoice, String> orderIdCol = new TableColumn<>("Order ID");
        TableColumn<SalesInvoice, String> storeNameCol = new TableColumn<>("Store Name");
        TableColumn<SalesInvoice, String> salesmanNameCol = new TableColumn<>("Salesman");
        TableColumn<SalesInvoice, Object> invoiceDateCol = new TableColumn<>("Invoice Date");
        TableColumn<SalesInvoice, String> paymentStatusCol = new TableColumn<>("Payment Status");
        TableColumn<SalesInvoice, String> transactionStatusCol = new TableColumn<>("Transaction Status");
        TableColumn<SalesInvoice, Object> totalAmountCol = new TableColumn<>("Total Amount");
        TableColumn<SalesInvoice, Object> typeCol = new TableColumn<>("Type");

        orderIdCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getOrderId()));
        storeNameCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCustomer().getStoreName()));
        salesmanNameCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getSalesman().getSalesmanName()));
        invoiceDateCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getInvoiceDate()));
        paymentStatusCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPaymentStatus()));
        transactionStatusCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTransactionStatus()));
        totalAmountCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getTotalAmount()));
        typeCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getType()));

        defaultTable.setRowFactory(tv -> {
            TableRow<SalesInvoice> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    SalesInvoice selectedInvoice = row.getItem();
                    openSalesInvoice(selectedInvoice);
                }
            });
            return row;
        });

        List<SalesInvoice> invoices = salesInvoiceDAO.loadSalesInvoices();
        defaultTable.getItems().setAll(invoices);
    }*/

    private static TableColumn<SalesInvoiceHeader, Integer> getSalesInvoiceType() {
        TableColumn<SalesInvoiceHeader, Integer> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));

        typeCol.setCellFactory(col -> new TableCell<SalesInvoiceHeader, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    switch (item) {
                        case 1:
                            setText("Charge Sales Invoice");
                            break;
                        case 2:
                            setText("Cash Sales Invoice");
                            break;
                        case 3:
                            setText("Delivery Receipt");
                            break;
                        default:
                            setText("Unknown Type");
                            break;
                    }
                }
            }
        });
        return typeCol;
    }


    StockTransferDAO stockTransferDAO = new StockTransferDAO();

    public void loadStockTransfer() throws SQLException {
        tableHeader.setText("Stock Transfer");
        Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/Inventory Flow.png")));
        tableImg.setImage(image);
        defaultTable.getItems().clear();
        defaultTable.getColumns().clear();

        // Create columns
        TableColumn<StockTransfer, String> orderNoCol = new TableColumn<>("Order No");
        orderNoCol.setCellValueFactory(new PropertyValueFactory<>("orderNo"));

        TableColumn<StockTransfer, String> sourceBranchCol = new TableColumn<>("Source Branch");
        sourceBranchCol.setCellValueFactory(cellData -> {
            try {
                return new SimpleStringProperty(getBranchNameById(cellData.getValue().getSourceBranch()));
            } catch (SQLException e) {
                e.printStackTrace();
                return new SimpleStringProperty("");
            }
        });

        TableColumn<StockTransfer, String> targetBranchCol = new TableColumn<>("Target Branch");
        targetBranchCol.setCellValueFactory(cellData -> {
            try {
                return new SimpleStringProperty(getBranchNameById(cellData.getValue().getTargetBranch()));
            } catch (SQLException e) {
                e.printStackTrace();
                return new SimpleStringProperty("");
            }
        });

        TableColumn<StockTransfer, Date> leadDateCol = new TableColumn<>("Lead Date");
        leadDateCol.setCellValueFactory(new PropertyValueFactory<>("leadDate"));

        TableColumn<StockTransfer, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Add columns to the table
        defaultTable.getColumns().addAll(orderNoCol, sourceBranchCol, targetBranchCol, leadDateCol, statusCol);

        // Set the retrieved stock transfers to the defaultTable
        setStockTransfersToTable(stockTransferDAO.getAllGoodStockTransferHeader());

        defaultTable.setRowFactory(tv -> {
            TableRow<StockTransfer> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    StockTransfer selectedTransfer = row.getItem();
                    String orderNo = selectedTransfer.getOrderNo();
                }
            });
            return row;
        });
    }




    private String getBranchNameById(int branchId) throws SQLException {
        return branchDAO.getBranchNameById(branchId);
    }

    public void setStockTransfersToTable(List<StockTransfer> stockTransfers) {
        ObservableList<StockTransfer> data = FXCollections.observableArrayList(stockTransfers);
        if (data.isEmpty()) {
            Label label = new Label("No stock transfers found.");
            defaultTable.setPlaceholder(label);
        } else {
            defaultTable.setItems(data);
        }
    }


    SalesOrderDAO salesDAO = new SalesOrderDAO();

    public void loadSalesOrders() {
        tableHeader.setText("Sales Orders");
        Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/Create Order.png")));
        tableImg.setImage(image);

        defaultTable.getColumns().clear();
        defaultTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        TableColumn<SalesOrderHeader, Integer> orderIDColumn = new TableColumn<>("Order ID");
        orderIDColumn.setCellValueFactory(new PropertyValueFactory<>("orderId"));

        TableColumn<SalesOrderHeader, String> customerNameColumn = new TableColumn<>("Customer Name");
        customerNameColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));

        TableColumn<SalesOrderHeader, String> salesmanNameColumn = new TableColumn<>("Salesman");
        salesmanNameColumn.setCellValueFactory(cellData -> {
            int salesmanId = cellData.getValue().getSalesmanId();
            String salesmanName = salesmanDAO.getSalesmanNameById(salesmanId);
            return new SimpleStringProperty(salesmanName);
        });

        TableColumn<SalesOrderHeader, LocalDateTime> createdDateColumn = new TableColumn<>("Created Date");
        createdDateColumn.setCellValueFactory(new PropertyValueFactory<>("orderDate"));

        TableColumn<SalesOrderHeader, BigDecimal> totalColumn = new TableColumn<>("Total");
        totalColumn.setCellValueFactory(new PropertyValueFactory<>("amountDue"));

        TableColumn<SalesOrderHeader, String> poStatusColumn = new TableColumn<>("SO Status");
        poStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        defaultTable.getColumns().addAll(orderIDColumn, customerNameColumn, salesmanNameColumn, createdDateColumn, totalColumn, poStatusColumn);
        defaultTable.setRowFactory(tv -> {
            TableRow<SalesOrderHeader> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    SalesOrderHeader rowData = row.getItem();
                    openSalesOrder(rowData);
                }
            });
            return row;
        });
        loadSalesOrderItems();
    }

    private void openSalesOrder(SalesOrderHeader rowData) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("salesOrder.fxml"));
            Parent root = loader.load();
            SalesOrderEntryController controller = loader.getController();
            controller.setTableManager(this);
            controller.initData(rowData);
            Scene scene = new Scene(root);
            Stage newStage = new Stage();
            newStage.setResizable(true);
            newStage.setTitle("SO" + rowData.getOrderId());
            newStage.setScene(scene);
            newStage.setMaximized(true);
            newStage.show();
        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception appropriately
        }

    }


    public void loadSalesOrderItems() {
        try {
            List<SalesOrderHeader> orders = salesDAO.getAllOrders();
            if (orders.isEmpty()) {
                defaultTable.setPlaceholder(new Label("No orders found."));
            } else {
                defaultTable.getItems().setAll(orders);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    SalesmanDAO salesmanDAO = new SalesmanDAO();

    public void loadSalesmanTable() {
        tableHeader.setText("Salesmen");
        Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/Salesman.png")));
        tableImg.setImage(image);
        // Clear existing columns
        defaultTable.getColumns().clear();

        TableColumn<Salesman, Integer> employeeIdCol = new TableColumn<>("Employee ID");
        employeeIdCol.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getEmployeeId()).asObject());

        TableColumn<Salesman, String> salesmanCodeCol = new TableColumn<>("Salesman Code");
        salesmanCodeCol.setCellValueFactory(new PropertyValueFactory<>("salesmanCode"));

        TableColumn<Salesman, String> salesmanNameCol = new TableColumn<>("Salesman Name");
        salesmanNameCol.setCellValueFactory(new PropertyValueFactory<>("salesmanName"));

        TableColumn<Salesman, String> truckPlateCol = new TableColumn<>("Truck Plate");
        truckPlateCol.setCellValueFactory(new PropertyValueFactory<>("truckPlate"));

        TableColumn<Salesman, String> priceTypeCol = new TableColumn<>("Price Type");
        priceTypeCol.setCellValueFactory(new PropertyValueFactory<>("priceType"));

        defaultTable.getColumns().addAll(employeeIdCol, salesmanCodeCol, salesmanNameCol, truckPlateCol, priceTypeCol);
        populateSalesmanTable();
        defaultTable.setRowFactory(tv -> {
            TableRow<Salesman> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Salesman rowData = row.getItem();
                    openSalesman(rowData);
                }
            });
            return row;
        });
    }

    private void openSalesman(Salesman rowData) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("salesmanRegistration.fxml"));
            Parent root = loader.load();

            // Access the controller of the loaded FXML file if needed
            SalesmanRegistrationController controller = loader.getController();
            controller.setTableManager(this);
            controller.setSalesmanData(rowData);

            Scene scene = new Scene(root);
            Stage newStage = new Stage();
            newStage.setResizable(true);
            newStage.setTitle(rowData.getSalesmanName());
            newStage.setScene(scene);
            newStage.show();
        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception appropriately
        }

    }

    private void populateSalesmanTable() {
        List<Salesman> salesmen = salesmanDAO.getAllSalesmen();
        defaultTable.getItems().setAll(salesmen);
    }


    CustomerDAO customerDAO = new CustomerDAO();

    public void loadCustomerTable() {
        tableHeader.setText("Customers");
        Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/Customer.png")));
        tableImg.setImage(image);

        defaultTable.getColumns().clear();
        defaultTable.getItems().clear();

        TableColumn<Customer, String> storeNameColumn = new TableColumn<>("Store Name");
        storeNameColumn.setCellValueFactory(new PropertyValueFactory<>("storeName"));

        TableColumn<Customer, String> signageNameColumn = new TableColumn<>("Signage Name");
        signageNameColumn.setCellValueFactory(new PropertyValueFactory<>("storeSignage"));

        TableColumn<Customer, String> provinceColumn = new TableColumn<>("Province");
        provinceColumn.setCellValueFactory(new PropertyValueFactory<>("province"));

        TableColumn<Customer, String> cityColumn = new TableColumn<>("City");
        cityColumn.setCellValueFactory(new PropertyValueFactory<>("city"));

        TableColumn<Customer, String> brgyColumn = new TableColumn<>("Barangay");
        brgyColumn.setCellValueFactory(new PropertyValueFactory<>("brgy"));
        TableColumn<Customer, ?> addressColumn = new TableColumn<>("Address");
        addressColumn.getColumns().addAll(provinceColumn, cityColumn, brgyColumn);

        defaultTable.getColumns().addAll(storeNameColumn, signageNameColumn, addressColumn);

        populateCustomerTable();

        defaultTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);


        defaultTable.setRowFactory(tv -> {
            TableRow<Customer> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Customer rowData = row.getItem();
                    initializeCustomer(rowData);
                }
            });
            return row;
        });
    }

    public void populateCustomerTable() {
        ObservableList<Customer> customers = customerDAO.getAllCustomers();
        defaultTable.setItems(customers);
    }

    private void initializeCustomer(Customer customer) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("customerRegistration.fxml"));
            Parent root = loader.load();

            // Access the controller of the loaded FXML file if needed
            CustomerRegistrationController controller = loader.getController();
            controller.setTableManager(this);
            controller.initData(customer);

            Scene scene = new Scene(root);
            Stage newStage = new Stage();
            newStage.setResizable(true);
            newStage.setMaximized(true);
            newStage.setTitle(customer.getStoreName());
            controller.setStage(newStage);
            newStage.setScene(scene);
            newStage.show();
        } catch (IOException | SQLException e) {
            e.printStackTrace(); // Handle the exception appropriately
        }

    }

    private final AssetsAndEquipmentDAO assetsAndEquipmentDAO = new AssetsAndEquipmentDAO();

    public void loadAssetsAndEquipmentTable() {
        tableHeader.setText("Assets And Equipments");
        Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/assets.png")));
        tableImg.setImage(image);

        defaultTable.getColumns().clear();

        // Define your table columns
        TableColumn<AssetsAndEquipment, String> itemNameColumn = new TableColumn<>("Item Name");
        itemNameColumn.setCellValueFactory(new PropertyValueFactory<>("itemName"));

        TableColumn<AssetsAndEquipment, ImageView> itemImageColumn = new TableColumn<>("Item Image");
        itemImageColumn.setCellValueFactory(new PropertyValueFactory<>("itemImage"));

        TableColumn<AssetsAndEquipment, Integer> quantityColumn = new TableColumn<>("Quantity");
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        TableColumn<AssetsAndEquipment, String> departmentColumn = new TableColumn<>("Department");
        departmentColumn.setCellValueFactory(new PropertyValueFactory<>("department"));

        TableColumn<AssetsAndEquipment, String> assigneeColumn = new TableColumn<>("Assignee");
        assigneeColumn.setCellValueFactory(new PropertyValueFactory<>("employee"));

        TableColumn<AssetsAndEquipment, Double> totalColumn = new TableColumn<>("Total");

        defaultTable.getColumns().addAll(itemNameColumn, itemImageColumn, departmentColumn, quantityColumn, assigneeColumn, totalColumn);

        // Fetch data from the database using DAO
        ObservableList<AssetsAndEquipment> assetsList = FXCollections.observableArrayList();

        List<AssetsAndEquipment> assets = assetsAndEquipmentDAO.getAllAssetsAndEquipment();

        assetsList.addAll(assets);

        // Populate the table with the fetched data
        defaultTable.setItems(assetsList);
    }

    private void loadDiscountTypeTable() {
        tableHeader.setText("Discount Types");
        Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/Line Discount.png")));
        tableImg.setImage(image);
        defaultContent.getChildren().remove(defaultTable);
        defaultContent.getChildren().add(tilePane);

        tilePane.getChildren().clear();

        List<DiscountType> discountTypeList = null;
        discountTypeList = discountDAO.getAllDiscountTypes();

        for (DiscountType discountType : discountTypeList) {
            String typeName = discountType.getTypeName(); // Get the type name
            VBox tile = createTile(typeName);
            tile.setOnMouseClicked(mouseEvent -> openDiscountLink(typeName));
            tilePane.getChildren().add(tile);
        }
    }


    private void openDiscountLink(String discountType) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("DiscountType.fxml"));
            Parent root = loader.load();

            // Access the controller of the loaded FXML file if needed
            DiscountTypeController controller = loader.getController();
            controller.setDiscountType(discountType);

            Scene scene = new Scene(root);
            Stage newStage = new Stage();
            newStage.setResizable(false);
            newStage.setTitle(discountType);
            newStage.setScene(scene);
            newStage.show();
        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception appropriately
        }
    }

    private void loadLineDiscountTable() {
        tableHeader.setText("Line Discount");
        Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/Discount Type.png")));
        tableImg.setImage(image);
        defaultTable.setBackground(Background.fill(Color.TRANSPARENT));
        defaultContent.getChildren().remove(defaultTable);
        defaultContent.getChildren().add(tilePane);

        List<LineDiscount> lineDiscountsList = null;
        try {
            lineDiscountsList = discountDAO.getAllLineDiscounts();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        for (LineDiscount lineDiscount : lineDiscountsList) {
            String discountInfo = lineDiscount.getLineDiscount() + " - " + lineDiscount.getPercentage() + "%";
            VBox tile = createTile(discountInfo);
            tilePane.getChildren().add(tile);
        }
    }

    private Consumer<Event> discountChangeEventConsumer;

    public void setDiscountChangeEventConsumer(Consumer<Event> consumer) {
        this.discountChangeEventConsumer = consumer;
    }

    public void loadLineDiscountTableForLink(String discountName) throws SQLException {
        tableHeader.setText("Select line discount for " + discountName);
        Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/Discount Type.png")));
        tableImg.setImage(image);
        defaultTable.setBackground(Background.fill(Color.TRANSPARENT));
        defaultContent.getChildren().remove(defaultTable);
        defaultContent.getChildren().add(tilePane);

        List<LineDiscount> lineDiscountsList = null;
        try {
            lineDiscountsList = discountDAO.getAllLineDiscounts();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        for (LineDiscount lineDiscount : lineDiscountsList) {
            String discountInfo = lineDiscount.getLineDiscount() + " - " + lineDiscount.getPercentage() + "%";
            VBox tile = createTile(discountInfo);
            int discountId = lineDiscount.getId();
            int discountTypeId = discountDAO.getDiscountTypeIdByName(discountName);


            if (!discountDAO.isLineDiscountLinkedWithType(discountId, discountTypeId)) {
                tile.setOnMouseClicked(event -> {
                    try {
                        if (!discountDAO.isLineDiscountLinkedWithType(discountId, discountTypeId)) {
                            boolean registered = discountDAO.linkLineDiscountWithType(discountId, discountTypeId);
                            if (registered) {
                                DialogUtils.showCompletionDialog("Link Success", discountInfo + " successfully linked to " + discountName);
                                discountChangeEventConsumer.accept(new DiscountChangeEvent());
                            } else {
                                DialogUtils.showErrorMessage("Failed", "Linking failed for " + discountInfo);
                            }
                        } else {
                            DialogUtils.showErrorMessage("Error", "Line discount already linked to the discount type");
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                });
                tilePane.getChildren().add(tile);
            }
        }

    }

    @FXML
    private void loadContent(String fxmlFileName, String registrationType) {
        System.out.println("Loading content: " + fxmlFileName + " for registration type: " + registrationType); // Debug statement
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFileName));
            Parent content = loader.load();

            // Set the controller for the loaded FXML file
            if (fxmlFileName.equals("tableManager.fxml")) {
                TableManagerController controller = loader.getController();
                controller.setRegistrationType(registrationType);
                controller.setContentPane(contentPane);
            }
            String sessionId = UserSession.getInstance().getSessionId();
            int currentNavigationId = historyManager.addEntry(sessionId, fxmlFileName);

            ContentManager.setContent(contentPane, content); // Assuming contentPane is your AnchorPane
        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception according to your needs
            System.err.println("Error loading " + fxmlFileName + ": " + e.getMessage());
        }
    }


    private static final String LABEL_STYLE = "-fx-font-size: 14px;\n" +
            "    -fx-text-fill: #3E4756;\n" +
            "    -fx-font-weight: 500;";

    private VBox createTile(String tileContent) {
        VBox tile = new VBox(); // Create a new VBox
        tile.setPrefSize(100, 50);
        tile.setPadding(new Insets(5));
        tile.setBackground(new Background(new BackgroundFill(Color.valueOf("#f0f0f0"), new CornerRadii(10), Insets.EMPTY)));
        Label label = new Label(tileContent);
        label.setStyle(LABEL_STYLE);
        tile.getChildren().add(label);
        new HoverAnimation(tile);
        return tile;
    }

    ObservableList<Product> products = FXCollections.observableArrayList();

    public void loadProductParentsTable(String supplierName, ObservableList<Product> existingProducts) {
        tableHeader.setText("Add product for " + supplierName);
        Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/package.png")));
        tableImg.setImage(image);

        columnHeader3.setText("Description");
        columnHeader5.setText("Brand");
        columnHeader6.setText("Category");
        columnHeader7.setText("Segment");
        columnHeader8.setText("Section");

        defaultTable.getColumns().removeAll(column1, column2, column4);

        BrandDAO brandDAO = new BrandDAO();
        CategoriesDAO categoriesDAO = new CategoriesDAO();
        SegmentDAO segmentDAO = new SegmentDAO();
        SectionsDAO sectionsDAO = new SectionsDAO();

        defaultTable.setRowFactory(tv -> new TableRow<Product>() {
            @Override
            protected void updateItem(Product item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                } else {
                    if (item.getParentId() == 0) {
                        setStyle("-fx-background-color: #5A90CF;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });

        column1.setCellValueFactory(new PropertyValueFactory<>("productName"));
        column2.setCellValueFactory(new PropertyValueFactory<>("productCode"));
        column3.setCellValueFactory(new PropertyValueFactory<>("description"));
        column4.setCellValueFactory(new PropertyValueFactory<>("productImage"));

        column4.setCellFactory(param -> new TableCell<Product, String>() {
            private final ImageView imageView = new ImageView();

            {
                ImageCircle.circular(imageView);
                imageView.setFitHeight(50);
                imageView.setFitWidth(50);
                setGraphic(imageView);
                setContentDisplay(ContentDisplay.CENTER);
            }

            @Override
            protected void updateItem(String imagePath, boolean empty) {
                super.updateItem(imagePath, empty);
                if (empty || imagePath == null) {
                    imageView.setImage(null);
                } else {
                    Image image = new Image(new File(imagePath).toURI().toString());
                    imageView.setImage(image);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        column5.setCellValueFactory(new PropertyValueFactory<>("productBrandString"));
        column6.setCellValueFactory(new PropertyValueFactory<>("productCategoryString"));
        column7.setCellValueFactory(new PropertyValueFactory<>("productSegmentString"));
        column8.setCellValueFactory(new PropertyValueFactory<>("productSectionString"));

        String query = "SELECT * FROM products WHERE (parent_id = 0 OR parent_id IS NULL) AND isActive = 1 ORDER BY product_name";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            defaultTable.getItems().clear();

            while (resultSet.next()) {
                Product product = new Product();
                product.setProductName(resultSet.getString("product_name"));
                product.setProductCode(resultSet.getString("product_code"));
                product.setDescription(resultSet.getString("description"));
                product.setProductImage(resultSet.getString("product_image"));
                product.setProductBrandString(brandDAO.getBrandNameById(resultSet.getInt("product_brand")));
                product.setProductCategoryString(categoriesDAO.getCategoryNameById(resultSet.getInt("product_category")));
                product.setProductSegmentString(segmentDAO.getSegmentNameById(resultSet.getInt("product_segment")));
                product.setProductSectionString(sectionsDAO.getSectionNameById(resultSet.getInt("product_section")));
                product.setParentId(resultSet.getInt("parent_id"));
                product.setProductId(resultSet.getInt("product_id"));

                products.add(product);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        for (Product existingProduct : existingProducts) {
            for (Product product : products) {
                if (product.getProductId() == existingProduct.getProductId()) {
                    products.remove(product);
                    break;
                }
            }
        }

        defaultTable.setRowFactory(tv -> {
            TableRow<Product> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty()) {
                    Product rowData = row.getItem();
                    addNewProductToSupplier(supplierName, rowData);
                }
            });
            return row;
        });


        defaultTable.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                Product selectedProduct = (Product) defaultTable.getSelectionModel().getSelectedItem();
                addNewProductToSupplier(supplierName, selectedProduct);
            }
        });

        defaultTable.setOnKeyPressed(event -> {
            if (event.isControlDown() && event.getCode() == KeyCode.A) {
                addAllProductsToSupplier(supplierName);
            }
        });

        defaultTable.setItems(products);

        searchBar.setPromptText("Search");
        searchBar.setVisible(true);
        searchBar.textProperty().addListener((observable, oldValue, newValue) -> {
            Comparator<Product> comparator = Comparator.comparing(product ->
                    product.getDescription().toLowerCase().indexOf(newValue.toLowerCase())
            );
            defaultTable.getItems().sort(comparator.reversed());
        });

    }

    private void addAllProductsToSupplier(String supplierName) {
        ObservableList<Product> items = defaultTable.getItems();

        int supplierId = supplierDAO.getSupplierIdByName(supplierName);

        // If the list is empty, there is no need to proceed.
        if (items.isEmpty()) {
            return;
        }

        String confirmationMessage = String.format("Add %d products", items.size());
        ConfirmationAlert confirmationAlert = new ConfirmationAlert("Confirmation",
                confirmationMessage,
                "Are you sure you want to add all the products to " + supplierName + " ?",
                false);

        if (confirmationAlert.showAndWait()) {
            // Create a copy of the items list to iterate over safely.
            List<Product> productsCopy = new ArrayList<>(items);

            // Use the bulk add method to add all products at once
            addProductsToSupplierInBulk(supplierId, productsCopy);
        }
    }


    public void loadChartOfAccountsTable() {
        ChartOfAccountsDAO chartOfAccountsDAO = new ChartOfAccountsDAO();
        tableHeader.setText("Chart Of Accounts");

        defaultTable.getColumns().removeAll(column1, column8, column7);

        // Set column headers
        columnHeader1.setText("GLCode");
        columnHeader2.setText("Account Title");
        columnHeader3.setText("BS/IS");
        columnHeader4.setText("Account Type");
        columnHeader5.setText("Balance Type");
        columnHeader6.setText("Description");

        column1.setCellValueFactory(new PropertyValueFactory<>("glCode"));
        column2.setCellValueFactory(new PropertyValueFactory<>("accountTitle"));
        column3.setCellValueFactory(new PropertyValueFactory<>("bsisCodeString"));
        column4.setCellValueFactory(new PropertyValueFactory<>("accountTypeString"));
        column5.setCellValueFactory(new PropertyValueFactory<>("balanceType"));
        column6.setCellValueFactory(new PropertyValueFactory<>("description"));

        ObservableList<ChartOfAccounts> chartOfAccounts = chartOfAccountsDAO.getAllChartOfAccounts();
        defaultTable.setItems(chartOfAccounts);

        chartOfAccountOnClick();
    }

    private void chartOfAccountOnClick() {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem reviewAccount = new MenuItem("Review Account");
        MenuItem updateAccount = new MenuItem("Update Account");
        contextMenu.getItems().addAll(reviewAccount, updateAccount);
        reviewAccount.setOnAction(event -> reviewAccountAction());
        updateAccount.setOnAction(event -> updateAccountAction());
        defaultTable.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                contextMenu.show(defaultTable, event.getScreenX(), event.getScreenY());
            } else {
                contextMenu.hide();
            }
        });
    }

    private void updateAccountAction() {
        ChartOfAccounts selectedAccount = (ChartOfAccounts) defaultTable.getSelectionModel().getSelectedItem();
        if (selectedAccount != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("ChartOfAccountForm.fxml"));
                Parent content = loader.load();

                ChartOfAccountFormController controller = loader.getController();
                controller.initData(selectedAccount);
                controller.setTableManagerController(this);

                Stage stage = new Stage();
                stage.setTitle(selectedAccount.getAccountTitle()); // Set the title of the new stage
                stage.setResizable(false);
                stage.setScene(new Scene(content)); // Set the scene with the loaded content
                stage.showAndWait();
            } catch (IOException e) {
                e.printStackTrace(); // Handle the exception according to your needs
                System.err.println("Error loading companyRegistration.fxml: " + e.getMessage());
            }

        }
    }

    private void reviewAccountAction() {
        ChartOfAccounts selectedAccount = (ChartOfAccounts) defaultTable.getSelectionModel().getSelectedItem();
        if (selectedAccount != null) {
            // Handle the Review Account action
            System.out.println("Review Account: " + selectedAccount.getGlCode());
        }
    }


    @FXML
    private void addNew(MouseEvent mouseEvent) {
        ConfirmationAlert confirmationAlert = new ConfirmationAlert("Add New", "Are you sure you want to add new?", "Please double check", true);
        boolean isConfirmed = confirmationAlert.showAndWait();
        if (isConfirmed) {
            handleAddNew(registrationType);
        }
    }

    private void handleAddNew(String type) {
        switch (type) {
            case "company" -> addNewCompany();
            case "branch" -> addNewBranch();
            case "employee" -> addNewEmployee();
            case "supplier" -> addNewSupplier();
            case "product" -> addNewProduct();
            case "product_supplier" -> System.out.println(type);
            case "system_employee" -> addNewSystemEmployeeTable();
            case "industry" -> addNewIndustry();
            case "division" -> addNewDivision();
            case "department" -> addNewDepartment();
            case "category" -> addNewCategory();
            case "customer" -> addNewCustomer();
            case "brand" -> addNewBrand();
            case "segment" -> addNewSegment();
            case "class" -> addNewClass();
            case "section" -> addNewSection();
            case "unit" -> addNewUnit();
            case "vehicles" -> addNewVehicle();
            case "chart_of_accounts" -> addNewChartOfAccounts();
            case "assets_and_equipments" -> addNewAsset();
            case "salesman" -> addNewSalesman();
            case "discount_type" -> addNewDiscountType();
            case "line_discount" -> addNewLineDiscount();
            case "stock_transfer" -> addNewStockTransfer();
            case "sales_order" -> addNewSalesOrder();
            case "sales_invoice" -> addNewSalesInvoice();
            case "trip_summary" -> addNewTripSummary();
            default -> tableHeader.setText("Unknown Type");
        }
    }

    private void addNewTripSummary() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("tripSummary.fxml"));
            Parent content = loader.load();
            TripSummaryController controller = loader.getController();

            controller.setTableManager(this);
            controller.createNewTrip();

            Stage stage = new Stage();
            stage.setTitle("Create trip summary");
            stage.setResizable(true);
            stage.setMaximized(true);
            stage.setScene(new Scene(content));
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception according to your needs
        }

    }

    private void addNewVehicle() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Vehicle.fxml"));
            Parent content = loader.load();
            VehicleController controller = loader.getController();

            controller.setTableManager(this);
            controller.addNewVehicle();

            Stage stage = new Stage();
            stage.setTitle("Register Vehicle");
            stage.setResizable(true);
            stage.setScene(new Scene(content));
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception according to your needs
        }

    }

    private void addNewSalesInvoice() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("tableManager.fxml"));
            Parent content = loader.load();
            TableManagerController controller = loader.getController();

            controller.setRegistrationType("so_to_si");
            controller.loadSalesForSI();

            Stage stage = new Stage();
            stage.setTitle("Sales Order to Invoice"); // Set the title of the new stage
            stage.setResizable(true);
            stage.setMaximized(true);
            stage.setScene(new Scene(content)); // Set the scene with the loaded content
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception according to your needs
        }

    }

    private void loadSalesForSI() {
        tableHeader.setText("Sales Orders For Invoicing");
        Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/Create Order.png")));
        tableImg.setImage(image);

        addImage.setVisible(false);
        defaultTable.getColumns().clear();
        defaultTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        TableColumn<SalesOrderHeader, Integer> orderIDColumn = new TableColumn<>("Order ID");
        orderIDColumn.setCellValueFactory(new PropertyValueFactory<>("orderId"));

        TableColumn<SalesOrderHeader, String> customerNameColumn = new TableColumn<>("Customer Name");
        customerNameColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));

        TableColumn<SalesOrderHeader, String> salesmanNameColumn = new TableColumn<>("Salesman");
        salesmanNameColumn.setCellValueFactory(cellData -> {
            int salesmanId = cellData.getValue().getSalesmanId();
            String salesmanName = salesmanDAO.getSalesmanNameById(salesmanId);
            return new SimpleStringProperty(salesmanName);
        });

        TableColumn<SalesOrderHeader, LocalDateTime> createdDateColumn = new TableColumn<>("Created Date");
        createdDateColumn.setCellValueFactory(new PropertyValueFactory<>("orderDate"));

        TableColumn<SalesOrderHeader, BigDecimal> totalColumn = new TableColumn<>("Total");
        totalColumn.setCellValueFactory(new PropertyValueFactory<>("amountDue"));

        TableColumn<SalesOrderHeader, String> poStatusColumn = new TableColumn<>("SO Status");
        poStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        defaultTable.getColumns().addAll(orderIDColumn, customerNameColumn, salesmanNameColumn, createdDateColumn, totalColumn, poStatusColumn);
        defaultTable.setRowFactory(tv -> {
            TableRow<SalesOrderHeader> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    SalesOrderHeader rowData = row.getItem();
                    openSalesOrderForConversion(rowData);
                }
            });
            return row;
        });
        try {
            ObservableList<SalesOrderHeader> salesToInvoice = salesDAO.getSalesOrderPerStatus("For Invoice");
            defaultTable.setItems(salesToInvoice);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void openSalesOrderForConversion(SalesOrderHeader rowData) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("SalesInvoice.fxml"));
            Parent root = loader.load();

            SalesInvoiceController controller = loader.getController();
            controller.setTableManager(this);
            controller.initDataForConversion(rowData);

            Scene scene = new Scene(root);
            Stage newStage = new Stage();
            newStage.setResizable(true);
            newStage.setTitle("SO" + rowData.getOrderId());
            newStage.setScene(scene);
            newStage.setMaximized(true);
            newStage.show();
        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception appropriately
        }
    }

    private void addNewSalesOrder() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("salesOrder.fxml"));
            Parent content = loader.load();
            SalesOrderEntryController controller = loader.getController();
            controller.createNewOrder();
            controller.setTableManager(this);

            Stage stage = new Stage();
            stage.setTitle("Create New Sales Order");
            stage.setResizable(true);
            stage.setMaximized(true);
            stage.setScene(new Scene(content));
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addNewLineDiscount() {
        String lineDiscountName = EntryAlert.showEntryAlert("Line Discount Registration", "Please enter line discount to be registered", "Line Discount: ");

        if (!lineDiscountName.isEmpty()) {
            double percentage = Double.parseDouble(EntryAlert.showEntryAlert("Percentage Registration", "Please enter percentage for the line discount", "Percentage: "));

            try {
                if (discountDAO.lineDiscountCreate(lineDiscountName, percentage)) {
                    DialogUtils.showCompletionDialog("Success", "Line discount created successfully: " + lineDiscountName);
                } else {
                    DialogUtils.showErrorMessage("Error", "Failed to create line discount: " + lineDiscountName);
                }
            } catch (Exception e) {
                DialogUtils.showErrorMessage("Error", "An error occurred: " + e.getMessage());
            }
        } else {
            DialogUtils.showErrorMessage("Error", "Line discount name is empty or null. Line discount creation canceled.");
        }
    }

    public void addNewDiscountType() {
        String discountType = EntryAlert.showEntryAlert("Discount Type Registration", "Please enter discount type to be registered", "Discount Type: ");

        if (!discountType.isEmpty()) {
            try {
                if (discountDAO.discountTypeCreate(discountType)) {
                    DialogUtils.showCompletionDialog("Success", "Discount type created successfully: " + discountType);
                    loadDiscountTypeTable();
                } else {
                    DialogUtils.showErrorMessage("Error", "Failed to create discount type: " + discountType);
                }
            } catch (Exception e) {
                DialogUtils.showErrorMessage("Error", "An error occurred: " + e.getMessage());
            }
        } else {
            DialogUtils.showErrorMessage("Error", "Discount type name is empty or null. Discount type creation canceled.");
        }
    }


    private void addNewSalesman() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("salesmanRegistration.fxml"));
            Parent content = loader.load();
            SalesmanRegistrationController controller = loader.getController();
            controller.salesmanRegistration();
            controller.setTableManager(this);

            Stage stage = new Stage();
            stage.setTitle("Add new salesman"); // Set the title of the new stage
            stage.setResizable(true);
            stage.setScene(new Scene(content)); // Set the scene with the loaded content
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception according to your needs
            System.err.println("Error loading salesmanRegistration.fxml: " + e.getMessage());
        }
    }

    private void addNewCustomer() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("customerRegistration.fxml"));
            Parent content = loader.load();
            CustomerRegistrationController controller = loader.getController();
            controller.customerRegistration();
            controller.setTableManager(this);

            Stage stage = new Stage();
            stage.setTitle("Add new customer"); // Set the title of the new stage
            stage.setResizable(true);
            stage.setMaximized(true);
            stage.setScene(new Scene(content)); // Set the scene with the loaded content
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception according to your needs
            System.err.println("Error loading customerRegistration.fxml: " + e.getMessage());
        }
    }

    private void addNewAsset() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("assetsEquipmentsRegistration.fxml"));
            Parent content = loader.load();

            AssetsEquipmentsController controller = loader.getController();
            controller.assetRegistration();

            Stage stage = new Stage();
            stage.setTitle("Add new asset"); // Set the title of the new stage
            stage.setResizable(false);
            stage.setScene(new Scene(content)); // Set the scene with the loaded content
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception according to your needs
            System.err.println("Error loading companyRegistration.fxml: " + e.getMessage());
        }
    }

    private Stage stockTransferStage = null;

    private void addNewStockTransfer() {
        if (stockTransferStage != null && stockTransferStage.isShowing()) {
            stockTransferStage.toFront(); // Bring the existing window to the front
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("stockTransfer.fxml"));
            Parent content = loader.load();

            StockTransferController controller = loader.getController();
            controller.setContentPane(contentPane);
            controller.createNewGoodStockTransfer();

            stockTransferStage = new Stage();
            stockTransferStage.setTitle("Create New Stock Transfer");
            stockTransferStage.setScene(new Scene(content));
            stockTransferStage.setMaximized(true);
            controller.setStockTransferStage(stockTransferStage);

            // Clear the reference when the window is closed
            stockTransferStage.setOnCloseRequest(event -> stockTransferStage = null);

            stockTransferStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    ProductDAO productDAO = new ProductDAO();

    private void addProductsToSupplierInBulk(int supplierName, List<Product> products) {
        ProductsPerSupplierDAO perSupplierDAO = new ProductsPerSupplierDAO();

        List<Product> failedProducts = new ArrayList<>();

        for (Product product : products) {
            try {
                int id = perSupplierDAO.addProductForSupplier(supplierName, product.getProductId());
                if (id == -1) {
                    failedProducts.add(product); // Keep track of products that failed to add
                }
            } catch (Exception e) {
                DialogUtils.showErrorMessage(
                        "Error",
                        "Failed to add " + product.getDescription() + " to " + supplierName + ". Error: " + e.getMessage()
                );
            }
        }

        // Refresh supplier product list if any products were successfully added
        if (failedProducts.size() < products.size()) {
            supplierInfoRegistrationController.populateSupplierProducts(supplierName);
            products.removeAll(failedProducts); // Remove successfully added products from the list
        }

        // Display error message for any failed products
        if (!failedProducts.isEmpty()) {
            StringBuilder failedProductNames = new StringBuilder();
            for (Product product : failedProducts) {
                failedProductNames.append(product.getDescription()).append(", ");
            }
            // Remove the last comma and space
            failedProductNames.setLength(failedProductNames.length() - 2);

            DialogUtils.showErrorMessage(
                    "Error",
                    "Failed to add the following products to " + supplierName + ": " + failedProductNames + ". Duplicate entry?"
            );
        }
    }


    private void addNewProductToSupplier(String supplierName, Product product) {
        ProductsPerSupplierDAO perSupplierDAO = new ProductsPerSupplierDAO();
        SupplierDAO supplierDAO = new SupplierDAO();
        int supplierId = supplierDAO.getSupplierIdByName(supplierName);

        try {
            int id = perSupplierDAO.addProductForSupplier(supplierId, product.getProductId());
            if (id != -1) {
                supplierInfoRegistrationController.populateSupplierProducts(supplierId);
                products.remove(product);
            } else {
                DialogUtils.showErrorMessage(
                        "Error",
                        "Failed to add " + product.getDescription() + " to " + supplierName + ". Duplicate entry?"
                );
            }
        } catch (Exception e) {
            DialogUtils.showErrorMessage(
                    "Error",
                    "Failed to add " + product.getDescription() + " to " + supplierName + ". Error: " + e.getMessage()
            );
        }
    }


    private void addNewChartOfAccounts() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ChartOfAccountForm.fxml"));
            Parent content = loader.load();

            ChartOfAccountFormController controller = loader.getController();
            controller.chartOfAccountRegistration();
            controller.setTableManagerController(this);

            Stage stage = new Stage();
            stage.setTitle("Add new account"); // Set the title of the new stage
            stage.setResizable(false);
            stage.setScene(new Scene(content)); // Set the scene with the loaded content
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception according to your needs
            System.err.println("Error loading companyRegistration.fxml: " + e.getMessage());
        }
    }

    public void addNewUnit() {
        String newUnitName = EntryAlert.showEntryAlert("Unit Registration", "Please enter unit to be registered", "Unit: ");
        if (!newUnitName.isEmpty()) {
            UnitDAO unitDAO = new UnitDAO(); // Assuming UnitDAO is your class handling unit operations
            boolean unitAdded = unitDAO.createUnit(newUnitName);
            if (unitAdded) {
                DialogUtils.showCompletionDialog("Success", "Unit created successfully: " + newUnitName);
            } else {
                DialogUtils.showErrorMessage("Error", "Failed to create unit: " + newUnitName);
            }
        } else {
            DialogUtils.showErrorMessage("Error", "Unit name is empty or null. Unit creation canceled.");
        }

        try {
            loadUnitData();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    private void addNewClass() {
        String productClass = EntryAlert.showEntryAlert("Class Registration", "Please enter class to be registered", "Class : ");
        ProductClassDAO productClassDAO = new ProductClassDAO();
        if (!productClass.isEmpty()) {
            boolean natureRegistered = productClassDAO.createProductClass(productClass);
            if (natureRegistered) {
                DialogUtils.showCompletionDialog("Class Created", "Class created successfully: " + productClass);
            } else {
                DialogUtils.showErrorMessage("Class Creation Failed", "Failed to create class: " + productClass);
            }
        } else {
            DialogUtils.showErrorMessage("Invalid Class", "Class name is empty or null. Class creation canceled.");
        }
        try {
            loadClassData();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void addNewSection() {
        String sectionName = EntryAlert.showEntryAlert("Section Registration", "Please enter section to be registered", "Section: ");
        SectionsDAO sectionsDAO = new SectionsDAO(); // Assuming you have a DAO class for handling sections

        if (sectionName != null && !sectionName.isEmpty()) {
            boolean sectionAdded = sectionsDAO.addSection(sectionName);
            if (sectionAdded) {
                DialogUtils.showCompletionDialog("Section Created", "Section created successfully: " + sectionName);
                // Additional actions upon successful section creation
            } else {
                DialogUtils.showErrorMessage("Section Creation Failed", "Failed to create section: " + sectionName);
                // Handle the case where section creation failed
            }
        } else {
            DialogUtils.showErrorMessage("Invalid Section", "Section name is empty or null. Section creation canceled.");
            // Handle the case where the section name is empty or null
        }
        try {
            loadSectionData();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void addNewIndustry() {
        ToDoAlert.showToDoAlert();
    }

    private void addNewDivision() {
        ToDoAlert.showToDoAlert();
    }

    private void addNewDepartment() {
        ToDoAlert.showToDoAlert();
    }

    private void addNewCategory() {
        String productCategory = EntryAlert.showEntryAlert("Category Registration", "Please enter category to be registered", "Category : ");
        CategoriesDAO categoriesDAO = new CategoriesDAO();
        if (!productCategory.isEmpty()) {
            boolean categoryRegistered = categoriesDAO.createCategory(productCategory);
            if (categoryRegistered) {
                DialogUtils.showCompletionDialog("Category Created", "Category created successfully: " + productCategory);
                // The category was created successfully, perform additional actions if needed
            } else {
                DialogUtils.showErrorMessage("Category Creation Failed", "Failed to create category: " + productCategory);
                // Handle the case where category creation failed
            }
        } else {
            DialogUtils.showErrorMessage("Invalid Category", "Category name is empty or null. Category creation canceled.");
        }
        try {
            loadCategoryData();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void addNewBrand() {
        String productBrand = EntryAlert.showEntryAlert("Brand Registration", "Please enter brand to be registered", "Brand : ");
        BrandDAO brandDAO = new BrandDAO();
        boolean brandRegistered = brandDAO.createBrand(productBrand);

        if (brandRegistered) {
            DialogUtils.showCompletionDialog("Brand registration", productBrand + " successfully registered");
        } else {
            DialogUtils.showErrorMessage("Brand registration failed", "Registration of brand " + productBrand + " has failed, please try again later.");
        }

        loadBrandData();
    }

    private void addNewSegment() {
        String productSegment = EntryAlert.showEntryAlert("Segment Registration", "Please enter segment to be registered", "Segment : ");
        SegmentDAO segmentDAO = new SegmentDAO();
        boolean segmentRegistered = segmentDAO.createSegment(productSegment);
        if (segmentRegistered) {
            DialogUtils.showCompletionDialog("Segment registration", productSegment + " successfully registered");
        } else {
            DialogUtils.showErrorMessage("Segment registration failed", "Registration of segment " + productSegment + " has failed, please try again later.");
        }

        try {
            loadSegmentData();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    private void addNewSystemEmployeeTable() {
        User selectedEmployee = (User) defaultTable.getSelectionModel().getSelectedItem();
        if (selectedEmployee != null) {
            ConfirmationAlert confirmationAlert = new ConfirmationAlert("Add Employee to System?",
                    "Add " + selectedEmployee.getUser_fname() + " to the system?", "Add employee to system?", false);

            boolean userConfirmed = confirmationAlert.showAndWait();

            if (userConfirmed) {
                String generatedPassword = RandomStringUtils.randomAlphanumeric(8);
                selectedEmployee.setUser_password(generatedPassword);

                // Update the password in the database
                String updateQuery = "UPDATE user SET user_password = ? WHERE user_id = ?";
                try (Connection connection = dataSource.getConnection();
                     PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)) {
                    preparedStatement.setString(1, generatedPassword);
                    preparedStatement.setInt(2, selectedEmployee.getUser_id());
                    preparedStatement.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace(); // Print the stack trace for debugging purposes
                    // You can also show an error message to the user if the update fails
                }

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Generated Password");
                alert.setHeaderText("Password generated for " + selectedEmployee.getUser_fname());
                alert.setContentText("Generated Password: " + generatedPassword);
                alert.showAndWait();
            }
        } else {
            // Handle the case where no employee is selected
            System.out.println("No employee selected.");
        }
    }

    private void addNewProduct() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("registerProduct.fxml"));
            Parent content = loader.load();

            RegisterProductController controller = loader.getController();
            controller.addNewParentProduct();
            controller.setTableManager(this);

            // Create a new stage (window) for company registration
            Stage stage = new Stage();
            stage.setTitle("Product Registration"); // Set the title of the new stage
            stage.setScene(new Scene(content)); // Set the scene with the loaded content
            stage.setMaximized(true);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception according to your needs
            System.err.println("Error loading companyRegistration.fxml: " + e.getMessage());
        }
    }

    private void addNewSupplier() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("supplierInfoRegistration.fxml"));
            Parent content = loader.load();

            SupplierInfoRegistrationController controller = loader.getController();
            controller.setTableManagerController(this);
            controller.initializeRegistration();
            Stage stage = new Stage();
            stage.setTitle("Supplier Registration"); // Set the title of the new stage
            stage.setScene(new Scene(content)); // Set the scene with the loaded content
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception according to your needs
            System.err.println("Error loading companyRegistration.fxml: " + e.getMessage());
        }
    }

    private void addNewEmployee() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("employeeDetails.fxml"));
            Parent content = loader.load();

            EmployeeDetailsController controller = loader.getController();
            controller.registerNewEmployee();
            controller.setTableManager(this);

            // Create a new stage (window) for company registration
            Stage stage = new Stage();
            stage.setTitle("Register new employee"); // Set the title of the new stage
            stage.setMaximized(true);
            stage.setScene(new Scene(content)); // Set the scene with the loaded content
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception according to your needs
            System.err.println("Error loading companyRegistration.fxml: " + e.getMessage());
        }
    }

    private void addNewBranch() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("branchRegistration.fxml"));
            Parent content = loader.load();

            BranchRegistrationController controller = loader.getController();
            controller.tableManagerController(this);
            controller.addNewBranch();
            Stage stage = new Stage();
            stage.setTitle("Supplier Registration"); // Set the title of the new stage
            stage.setScene(new Scene(content)); // Set the scene with the loaded content
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception according to your needs
            System.err.println("Error loading companyRegistration.fxml: " + e.getMessage());
        }
    }

    private void addNewCompany() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("companyRegistration.fxml"));
            Parent content = loader.load();

            CompanyRegistrationController controller = loader.getController();
            controller.createCompany();

            // Create a new stage (window) for company registration
            Stage stage = new Stage();
            stage.setTitle("Company Registration"); // Set the title of the new stage
            stage.setScene(new Scene(content)); // Set the scene with the loaded content
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception according to your needs
            System.err.println("Error loading companyRegistration.fxml: " + e.getMessage());
        }
    }

    private void loadNatureTable() {
        tableHeader.setText("Nature");
        Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/Product Nature.png")));
        tableImg.setImage(image);

        columnHeader1.setText("Nature Name");

        TableColumn<Map<String, String>, String> column1 = new TableColumn<>("Nature Name");
        column1.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("nature_name")));

        try {
            loadNatureData(); // Load data into the 'natureData' ObservableList

            defaultTable.setItems(natureData); // Set items from 'natureData' to the table
        } catch (SQLException e) {
            e.printStackTrace();
        }

        defaultTable.getColumns().clear(); // Clear existing columns
        defaultTable.getColumns().add(column1);
    }

    private void loadNatureData() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            String query = "SELECT * FROM nature";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query);
                 ResultSet resultSet = preparedStatement.executeQuery()) {

                natureData.clear(); // Clear existing data before loading new data

                while (resultSet.next()) {
                    Map<String, String> natureRow = new HashMap<>();
                    natureRow.put("nature_name", resultSet.getString("nature_name"));
                    natureData.add(natureRow);
                }
            }
        }
    }

    private void loadSectionTable() {
        tableHeader.setText("Section");
        Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/section.png")));
        tableImg.setImage(image);

        columnHeader1.setText("Section Name");

        TableColumn<Map<String, String>, String> column1 = new TableColumn<>("Section Name");
        column1.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("section_name")));

        try {
            loadSectionData(); // Load data into the 'sectionData' ObservableList

            defaultTable.setItems(sectionData); // Set items from 'sectionData' to the table
        } catch (SQLException e) {
            e.printStackTrace();
        }

        defaultTable.getColumns().clear(); // Clear existing columns
        defaultTable.getColumns().add(column1);
    }

    private void loadSectionData() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            String query = "SELECT * FROM sections";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query);
                 ResultSet resultSet = preparedStatement.executeQuery()) {

                sectionData.clear(); // Clear existing data before loading new data

                while (resultSet.next()) {
                    Map<String, String> sectionRow = new HashMap<>();
                    sectionRow.put("section_name", resultSet.getString("section_name"));
                    sectionData.add(sectionRow);
                }
            }
        }
    }

    private void loadClassTable() {
        tableHeader.setText("Class");
        Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/Prduct Class.png")));
        tableImg.setImage(image);

        columnHeader1.setText("Class Name");

        TableColumn<Map<String, String>, String> column1 = new TableColumn<>("Class Name");
        column1.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("class_name")));

        try {
            loadClassData(); // Load data into the 'classData' ObservableList

            defaultTable.setItems(classData); // Set items from 'classData' to the table
        } catch (SQLException e) {
            e.printStackTrace();
        }

        defaultTable.getColumns().clear(); // Clear existing columns
        defaultTable.getColumns().add(column1);
    }

    private void loadClassData() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            String query = "SELECT * FROM classes";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query);
                 ResultSet resultSet = preparedStatement.executeQuery()) {

                classData.clear(); // Clear existing data before loading new data

                while (resultSet.next()) {
                    Map<String, String> classRow = new HashMap<>();
                    classRow.put("class_name", resultSet.getString("class_name"));
                    classData.add(classRow);
                }
            }
        }
    }

    private void loadDiscountSetUpTable() {
        ToDoAlert.showToDoAlert();
        tableHeader.setText("Discount Set Up");
        Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/Discount.png")));
        tableImg.setImage(image);

        columnHeader1.setText("Discount Name");

        TableColumn<Map<String, String>, String> column1 = new TableColumn<>("Discount Name");
        column1.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("segment_name")));

        try {
            ObservableList<Map<String, String>> segmentData = FXCollections.observableArrayList();

            try (Connection connection = dataSource.getConnection()) {
                String query = "SELECT * FROM segment";
                try (PreparedStatement preparedStatement = connection.prepareStatement(query);
                     ResultSet resultSet = preparedStatement.executeQuery()) {

                    while (resultSet.next()) {
                        Map<String, String> segmentRow = new HashMap<>();
                        segmentRow.put("segment_name", resultSet.getString("segment_name"));
                        segmentData.add(segmentRow);
                    }
                }
            }

            defaultTable.setItems(segmentData);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        defaultTable.getColumns().clear(); // Clear existing columns
        defaultTable.getColumns().add(column1);
    }

    private void loadPaymentTerms() {
        tableHeader.setText("Payment Terms");
        Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/Card Payment.png")));
        tableImg.setImage(image);

        columnHeader1.setText("Payment Term Names");

        TableColumn<Map<String, String>, String> column1 = new TableColumn<>("Payment Names");
        column1.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("payment_name")));

        try {
            ObservableList<Map<String, String>> paymentData = FXCollections.observableArrayList();

            try (Connection connection = dataSource.getConnection()) {
                String query = "SELECT * FROM payment_terms";
                try (PreparedStatement preparedStatement = connection.prepareStatement(query);
                     ResultSet resultSet = preparedStatement.executeQuery()) {

                    while (resultSet.next()) {
                        Map<String, String> paymentRow = new HashMap<>();
                        paymentRow.put("payment_name", resultSet.getString("payment_name"));
                        paymentData.add(paymentRow);
                    }
                }
            }

            defaultTable.setItems(paymentData);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        defaultTable.getColumns().clear(); // Clear existing columns
        defaultTable.getColumns().add(column1);

    }

    private void loadDeliveryTerms() {
        tableHeader.setText("Delivery Terms");
        Image image = new Image(Objects.requireNonNull(Objects.requireNonNull(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/Delivery.png"))));
        tableImg.setImage(image);

        TableColumn<Map<String, String>, String> column1 = new TableColumn<>("Delivery Terms");
        column1.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("delivery_name")));

        try {
            ObservableList<Map<String, String>> deliveryData = FXCollections.observableArrayList();

            try (Connection connection = dataSource.getConnection()) {
                String query = "SELECT * FROM delivery_terms";
                try (PreparedStatement preparedStatement = connection.prepareStatement(query);
                     ResultSet resultSet = preparedStatement.executeQuery()) {

                    while (resultSet.next()) {
                        Map<String, String> deliveryRow = new HashMap<>();
                        deliveryRow.put("delivery_terms", resultSet.getString("delivery_terms"));
                        deliveryData.add(deliveryRow);
                    }
                }
            }

            defaultTable.setItems(deliveryData);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        defaultTable.getColumns().clear(); // Clear existing columns
        defaultTable.getColumns().add(column1);

    }

    private void loadSegmentTable() {
        tableHeader.setText("Segment");
        Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/Sorting Category.png")));
        tableImg.setImage(image);

        columnHeader1.setText("Segment Name");

        TableColumn<Map<String, String>, String> column1 = new TableColumn<>("Segment Name");
        column1.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("segment_name")));

        try {
            loadSegmentData(); // Load data into the 'segmentData' ObservableList

            defaultTable.setItems(segmentData); // Set items from 'segmentData' to the table
        } catch (SQLException e) {
            e.printStackTrace();
        }

        defaultTable.getColumns().clear(); // Clear existing columns
        defaultTable.getColumns().add(column1);
    }

    private void loadSegmentData() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            String query = "SELECT * FROM segment";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query);
                 ResultSet resultSet = preparedStatement.executeQuery()) {

                segmentData.clear(); // Clear existing data before loading new data

                while (resultSet.next()) {
                    Map<String, String> segmentRow = new HashMap<>();
                    segmentRow.put("segment_name", resultSet.getString("segment_name"));
                    segmentData.add(segmentRow);
                }
            }
        }
    }

    private void loadUnitTable() {
        tableHeader.setText("Unit");
        Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/unit.png")));
        tableImg.setImage(image);

        columnHeader1.setText("Unit Name");

        TableColumn<Map<String, String>, String> column1 = new TableColumn<>("Unit Name");
        column1.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("unit_name")));

        try {
            loadUnitData(); // Load data into the 'unitData' ObservableList

            defaultTable.setItems(unitData); // Set items from 'unitData' to the table
        } catch (SQLException e) {
            e.printStackTrace();
        }

        defaultTable.getColumns().clear(); // Clear existing columns
        defaultTable.getColumns().add(column1);
    }

    private void loadUnitData() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            String query = "SELECT * FROM units";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query);
                 ResultSet resultSet = preparedStatement.executeQuery()) {

                unitData.clear(); // Clear existing data before loading new data

                while (resultSet.next()) {
                    Map<String, String> unitRow = new HashMap<>();
                    unitRow.put("unit_name", resultSet.getString("unit_name"));
                    unitData.add(unitRow);
                }
            }
        }
    }


    private void loadBrandTable() {
        tableHeader.setText("Brand");
        Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/brand.png")));
        tableImg.setImage(image);

        columnHeader1.setText("Brand Name");

        TableColumn<Map<String, String>, String> column1 = new TableColumn<>("Brand Name");
        column1.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("brand_name")));

        brandData = FXCollections.observableArrayList();
        loadBrandData();

        defaultTable.setItems(brandData);
        defaultTable.getColumns().clear(); // Clear existing columns
        defaultTable.getColumns().add(column1);
    }

    public void loadBrandData() {
        try (Connection connection = dataSource.getConnection()) {
            String query = "SELECT * FROM brand";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query);
                 ResultSet resultSet = preparedStatement.executeQuery()) {

                brandData.clear(); // Clear existing data before loading new data

                while (resultSet.next()) {
                    Map<String, String> brandRow = new HashMap<>();
                    brandRow.put("brand_name", resultSet.getString("brand_name"));
                    brandData.add(brandRow);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle SQL exceptions here
        }
    }


    private void loadCategoryTable() {
        tableHeader.setText("Category");
        Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/categorization.png")));
        tableImg.setImage(image);

        columnHeader1.setText("Category Name");

        TableColumn<Map<String, String>, String> column1 = new TableColumn<>("Category Name");
        column1.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("category_name")));

        try {
            loadCategoryData(); // Load data into the 'categoryData' ObservableList

            defaultTable.setItems(categoryData); // Set items from 'categoryData' to the table
        } catch (SQLException e) {
            e.printStackTrace();
        }

        defaultTable.getColumns().clear(); // Clear existing columns
        defaultTable.getColumns().add(column1);
    }

    private void loadCategoryData() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            String query = "SELECT * FROM categories";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query);
                 ResultSet resultSet = preparedStatement.executeQuery()) {

                categoryData.clear(); // Clear existing data before loading new data

                while (resultSet.next()) {
                    Map<String, String> categoryRow = new HashMap<>();
                    categoryRow.put("category_name", resultSet.getString("category_name"));
                    categoryData.add(categoryRow);
                }
            }
        }
    }

    private void loadDepartmentTable() {
        tableHeader.setText("Department");
        Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/Organization Chart People.png")));
        tableImg.setImage(image);

        defaultTable.getColumns().removeAll(column7, column8);

        columnHeader1.setText("Division");
        columnHeader2.setText("Department Name");
        columnHeader3.setText("Department Head");
        columnHeader4.setText("Department Description");
        columnHeader5.setText("Date Added");
        columnHeader6.setText("Tax ID");

        column1.setCellValueFactory(new PropertyValueFactory<>("parentDivision"));
        column2.setCellValueFactory(new PropertyValueFactory<>("departmentName"));
        column3.setCellValueFactory(new PropertyValueFactory<>("departmentHead"));
        column4.setCellValueFactory(new PropertyValueFactory<>("departmentDescription"));
        column5.setCellValueFactory(new PropertyValueFactory<>("dateAdded"));
        column6.setCellValueFactory(new PropertyValueFactory<>("taxId"));

        String query = "SELECT * FROM department";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            defaultTable.getItems().clear();
            while (resultSet.next()) {
                Department department = new Department(
                        resultSet.getInt("department_id"),
                        resultSet.getString("parent_division"),
                        resultSet.getString("department_name"),
                        resultSet.getString("department_head"),
                        resultSet.getString("department_description"),
                        resultSet.getInt("tax_id"),
                        resultSet.getDate("date_added")

                );
                defaultTable.getItems().add(department);

            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


    }

    private void loadDivisionTable() {
        tableHeader.setText("Division");
        Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/division.png")));
        tableImg.setImage(image);

        defaultTable.getColumns().removeAll(column5, column6, column7, column8);

        columnHeader1.setText("Division Name");
        columnHeader2.setText("Division Head");
        columnHeader3.setText("Division Description");
        columnHeader4.setText("Date Added");
        column1.setCellValueFactory(new PropertyValueFactory<>("divisionName"));
        column2.setCellValueFactory(new PropertyValueFactory<>("divisionHead"));
        column3.setCellValueFactory(new PropertyValueFactory<>("divisionDescription"));
        column4.setCellValueFactory(new PropertyValueFactory<>("dateAdded"));

        String query = "SELECT * FROM division";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            defaultTable.getItems().clear();
            while (resultSet.next()) {
                Division division = new Division(
                        resultSet.getInt("division_id"),
                        resultSet.getString("division_name"),
                        resultSet.getString("division_head"),
                        resultSet.getString("division_description"),
                        resultSet.getString("division_code"),
                        resultSet.getDate("date_added")
                );
                defaultTable.getItems().add(division);

            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadIndustryTable() {
        tableHeader.setText("Industries");
        Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/Manufacturing.png")));
        tableImg.setImage(image);

        columnHeader1.setText("Industry Name");
        columnHeader2.setText("Industry Head");
        columnHeader3.setText("Industry Description");
        columnHeader4.setText("Date Added");
        columnHeader5.setText("Tax ID");

        defaultTable.getColumns().removeAll(column3, column6, column7, column8);

        column1.setCellValueFactory(new PropertyValueFactory<>("industryName"));
        column2.setCellValueFactory(new PropertyValueFactory<>("industryHead"));
        column3.setCellValueFactory(new PropertyValueFactory<>("industryDescription"));
        column4.setCellValueFactory(new PropertyValueFactory<>("dateAdded"));
        column5.setCellValueFactory(new PropertyValueFactory<>("taxId"));

        String query = "SELECT * FROM industry"; // Exclude users without passwords
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            // Clear existing items in the table
            defaultTable.getItems().clear();
            while (resultSet.next()) {
                Industry industry = new Industry(
                        resultSet.getInt("id"),
                        resultSet.getString("industry_name"),
                        resultSet.getString("industry_head"),
                        resultSet.getString("industry_description"),
                        resultSet.getDate("date_added"),
                        resultSet.getInt("tax_id")
                );
                defaultTable.getItems().add(industry);

            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public void loadSystemEmployeeTable() {
        tableHeader.setText("System Employees");

        // Set column headers
        columnHeader1.setText("Employee ID");
        columnHeader2.setText("First Name");
        columnHeader3.setText("Middle Name");
        columnHeader4.setText("Last Name");
        columnHeader5.setText("Email");
        columnHeader6.setText("Contact");
        columnHeader7.setText("Position");
        columnHeader8.setText("Department");

        // Set cell value factories for table columns
        column1.setCellValueFactory(new PropertyValueFactory<>("user_id"));
        column2.setCellValueFactory(new PropertyValueFactory<>("user_fname"));
        column3.setCellValueFactory(new PropertyValueFactory<>("user_mname"));
        column4.setCellValueFactory(new PropertyValueFactory<>("user_lname"));
        column5.setCellValueFactory(new PropertyValueFactory<>("user_email"));
        column6.setCellValueFactory(new PropertyValueFactory<>("user_contact"));
        column7.setCellValueFactory(new PropertyValueFactory<>("user_position"));
        column8.setCellValueFactory(new PropertyValueFactory<>("user_department"));

        // Set row factory to apply row-specific styles
        defaultTable.setRowFactory(tv -> new TableRow<User>() {
            @Override
            protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    setStyle(""); // Reset to default style for empty rows
                } else {
                    String password = item.getUser_password();
                    if (password == null || password.isEmpty()) {
                        setStyle("-fx-background-color: orange;");
                    } else {
                        setStyle(""); // Default style for rows with non-empty password
                    }
                }
            }
        });

        // Handle double-clicks globally for the table
        defaultTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) { // Check for double-click
                User selectedUser = (User) defaultTable.getSelectionModel().getSelectedItem();
                if (selectedUser != null) {
                    String password = selectedUser.getUser_password();
                    if (password == null || password.isEmpty()) {
                        addNewSystemEmployeeTable(); // Open form for new system employee
                    } else {
                        openEmployeeDetails(selectedUser); // Open employee details
                    }
                }
            }
        });

        EmployeeDAO employeeDAO = new EmployeeDAO();
        ObservableList<User> employees = employeeDAO.getAllEmployees();
        defaultTable.setItems(employees);

        employeeFilter(employees);
    }

    private void employeeFilter(ObservableList<User> employees) {
        searchBar.setVisible(true);
        searchBar.setPromptText("Search employees...");

        FilteredList<User> filteredData = new FilteredList<>(employees, p -> true);
        searchBar.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(user -> {
                // If filter text is empty, display all employees
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();

                // Filter matching first name, last name, email, or position
                if (user.getUser_fname().toLowerCase().contains(lowerCaseFilter) ||
                        user.getUser_lname().toLowerCase().contains(lowerCaseFilter) ||
                        user.getUser_email().toLowerCase().contains(lowerCaseFilter) ||
                        user.getUser_position().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                return false; // Does not match
            });
        });

        SortedList<User> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(defaultTable.comparatorProperty());

        defaultTable.setItems(sortedData);
    }


    public void loadSupplierTable() {
        tableHeader.setText("Loading suppliers");
        Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/Supplier Info.png")));
        tableImg.setImage(image);
        columnHeader1.setText("Supplier Name");
        columnHeader2.setText("Logo");
        columnHeader3.setText("Contact Person");
        columnHeader4.setText("Email Address");
        columnHeader5.setText("Phone Number");
        columnHeader6.setText("Province");
        columnHeader7.setText("City");
        columnHeader8.setText("Baranggay");

        // Set cell value factories for columns
        column1.setCellValueFactory(new PropertyValueFactory<>("supplierName"));
        column2.setCellValueFactory(new PropertyValueFactory<>("supplierImage"));
        column3.setCellValueFactory(new PropertyValueFactory<>("contactPerson"));
        column4.setCellValueFactory(new PropertyValueFactory<>("emailAddress"));
        column5.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        column6.setCellValueFactory(new PropertyValueFactory<>("stateProvince"));
        column7.setCellValueFactory(new PropertyValueFactory<>("city"));
        column8.setCellValueFactory(new PropertyValueFactory<>("barangay"));

        // Set custom cell factory for image column
        column2.setCellFactory(param -> new TableCell<Supplier, String>() {
            private final ImageView imageView = new ImageView();

            {
                setAlignment(Pos.CENTER);
                ImageCircle.circular(imageView);
                imageView.setFitWidth(50);
                imageView.setFitHeight(50);
            }

            @Override
            protected void updateItem(String imagePath, boolean empty) {
                super.updateItem(imagePath, empty);
                if (empty || imagePath == null || imagePath.isEmpty()) {
                    setGraphic(null);
                } else {
                    try {
                        File file = new File(imagePath);
                        if (file.exists()) {
                            Image image = new Image(file.toURI().toString());
                            imageView.setImage(image);
                            setGraphic(imageView);
                        } else {
                            setGraphic(null);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        setGraphic(null);
                    }
                }
            }
        });

        // Use CompletableFuture for asynchronous task
        CompletableFuture.supplyAsync(() -> {
                    try {
                        return supplierDAO.getAllSuppliers(); // Ensure this method is truly asynchronous
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                })
                .thenAccept(suppliers -> Platform.runLater(() -> {
                    defaultTable.getItems().clear();
                    defaultTable.setItems(FXCollections.observableArrayList(suppliers));
                    tableHeader.setText("Suppliers");
                    defaultTable.setPlaceholder(null); // Clear the placeholder after loading
                }))
                .exceptionally(e -> {
                    Platform.runLater(() -> {
                        DialogUtils.showErrorMessage("Failed to load suppliers", e.getMessage());
                        defaultTable.setPlaceholder(new Label("Failed to load suppliers")); // Show a failure message
                    });
                    return null;
                });
        searchBar.setPromptText("Search Supplier");
        searchBar.setVisible(true);
        searchBar.textProperty().addListener((observable, oldValue, newValue) -> {
            Comparator<Supplier> comparator = Comparator.comparing(supplier ->
                    supplier.getSupplierName().toLowerCase().indexOf(newValue.toLowerCase())
            );
            defaultTable.getItems().sort(comparator.reversed());
        });
    }


    public void loadProductTable() {
        tableHeader.setText("Loading products...");
        Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/package.png")));
        tableImg.setImage(image);
        columnHeader1.setText("Product Name");
        columnHeader2.setText("Product Code");
        columnHeader5.setText("Brand");
        columnHeader6.setText("Category");
        columnHeader7.setText("Segment");
        columnHeader8.setText("Image");

        column1.setCellValueFactory(new PropertyValueFactory<>("productName"));
        column2.setCellValueFactory(new PropertyValueFactory<>("productCode"));
        column5.setCellValueFactory(new PropertyValueFactory<>("productBrandString"));
        column6.setCellValueFactory(new PropertyValueFactory<>("productCategoryString"));
        column7.setCellValueFactory(new PropertyValueFactory<>("productSegmentString"));
        column8.setCellValueFactory(new PropertyValueFactory<>("productImage"));
        column8.setCellFactory(param -> new TableCell<Product, String>() {
            private final ImageView imageView = new ImageView();

            {
                imageView.setFitHeight(50);
                imageView.setFitWidth(50);
                setGraphic(imageView);
                setContentDisplay(ContentDisplay.CENTER);
            }

            @Override
            protected void updateItem(String imagePath, boolean empty) {
                super.updateItem(imagePath, empty);

                if (empty || imagePath == null || imagePath.isEmpty()) {
                    imageView.setImage(null);
                } else {
                    Task<Image> imageLoadTask = new Task<>() {
                        @Override
                        protected Image call() {
                            return new Image(new File(imagePath).toURI().toString());
                        }

                        @Override
                        protected void succeeded() {
                            imageView.setImage(getValue());
                        }

                        @Override
                        protected void failed() {
                            imageView.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/package.png"))));
                        }
                    };
                    new Thread(imageLoadTask).start();
                }
            }
        });

        defaultTable.getItems().clear();
        defaultTable.getColumns().removeAll(column3, column4);

        // Initial load of products
        loadMoreProducts();

        // Add listener for infinite scroll
        defaultTable.setOnScroll(event -> {
            if (isScrollNearBottom()) {
                loadMoreProducts();
            }
        });

        searchingSetUp();
    }

    // Helper method to check if the scroll is near the bottom
    private boolean isScrollNearBottom() {
        ScrollBar scrollBar = (ScrollBar) defaultTable.lookup(".scroll-bar:vertical");
        return scrollBar != null && scrollBar.getValue() >= scrollBar.getMax() - scrollBar.getVisibleAmount();
    }

    // Method to load more products
    private boolean isLoading = false;

    private void loadMoreProducts() {
        if (isLoading) {
            return; // Don't load next if already loading
        }

        isLoading = true;
        Task<ObservableList<Product>> task = productDAO.getMoreParentProductsTask();
        task.setOnSucceeded(event -> {
            ObservableList<Product> products = task.getValue();
            defaultTable.getItems().addAll(products);
            tableHeader.setText("Products" + " (" + defaultTable.getItems().size() + ")");
            isLoading = false; // Reset flag after loading is complete
        });
        task.setOnFailed(event -> {
            task.getException().printStackTrace();
            isLoading = false; // Reset flag after loading fails
        });

        new Thread(task).start();
    }


    private void searchingSetUp() {
        searchBar.setPromptText("Enter Description");
        toggleButton.setText("Description");
        toggleButton.setVisible(true);
        searchBar.setVisible(true);
        searchBar.requestFocus();
        toggleButton.setSelected(true);
        final StringBuilder barcodeBuilder = new StringBuilder();
        final PauseTransition pauseTransition = getPauseTransition(barcodeBuilder);
        final AtomicBoolean processingBarcode = new AtomicBoolean(false);

        toggleButton.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                toggleButton.setText("Description");
                searchBar.setPromptText("Search by Description");
                searchBar.textProperty().addListener((observableValue, oldSearchValue, newSearchValue) -> {
                    if (newSearchValue != null && !newSearchValue.trim().isEmpty()) {
                        handleDescriptionSearch(newSearchValue);
                    }
                });
            } else {
                toggleButton.setText("Barcode");
                searchBar.setPromptText("Enter Barcode");
            }
        });

        searchBar.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                if (toggleButton.isSelected()) {
                    handleDescriptionSearch(searchBar.getText());
                } else {
                    handleBarcodeScan(searchBar.getText());
                }
            } else if (isValidBarcodeCharacter(event.getText())) {
                processingBarcode.set(true);
                pauseTransition.playFromStart();
                barcodeBuilder.append(event.getText());
            }
        });

        pauseTransition.setOnFinished(event -> {
            processingBarcode.set(false);
            barcodeBuilder.setLength(0);
        });
    }

    private int batchSize = 200; // number of products to fetch in each batch
    private int offset = 0; // current offset for pagination
    private String searchQuery = "";

    private void handleDescriptionSearch(String searchValue) {
        defaultTable.getItems().clear(); // Clear current results
        offset = 0; // Reset offset for new search
        searchQuery = searchValue.trim(); // Store the search term

        loadMoreSearchResults(); // Load the first batch of search results
    }

    private void loadMoreSearchResults() {
        Task<ObservableList<Product>> searchTask = productDAO.searchParentProductsTask(searchQuery, batchSize, offset);
        searchTask.setOnSucceeded(event -> {
            ObservableList<Product> products = searchTask.getValue();
            if (products.isEmpty()) {
                defaultTable.setPlaceholder(new Label("No results found for " + searchQuery));
            }
            else {
                defaultTable.getItems().addAll(products);
            }
            tableHeader.setText("Search Results" + " (" + defaultTable.getItems().size() + ")");
            offset += batchSize;
        });
        searchTask.setOnFailed(event -> {
            searchTask.getException().printStackTrace();
        });

        new Thread(searchTask).start();
    }


    private PauseTransition getPauseTransition(StringBuilder barcodeBuilder) {
        final PauseTransition pauseTransition = new PauseTransition(Duration.millis(500)); // Set the duration as needed

        pauseTransition.setOnFinished(event -> {
            String barcode = barcodeBuilder.toString();
            if (!barcode.isEmpty()) {
                handleBarcodeScan(barcode);
                barcodeBuilder.setLength(0); // Clear the barcode builder
                searchBar.clear(); // Clear the search bar text
            }
        });
        return pauseTransition;
    }

    private void handleBarcodeScan(String barcode) {
        int productId = productDAO.getProductIdByBarcode(barcode);
        String description = productDAO.getProductDescriptionByBarcode(barcode);
        if (productId != -1 && !description.isEmpty()) {
            openProductDetails(productId);
        } else {
            promptProductRegistration(barcode);
        }
    }

    private Stage productDetailsStage = null;
    ErrorUtilities errorUtilities = new ErrorUtilities();

    private void openProductDetails(int productId) {
        Platform.runLater(() -> {
            if (productDetailsStage == null) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("registerProduct.fxml"));
                    Parent root = loader.load();
                    RegisterProductController controller = loader.getController();
                    controller.initData(productId);

                    productDetailsStage = new Stage();
                    productDetailsStage.setMaximized(true);
                    productDetailsStage.setTitle("Product Details");
                    productDetailsStage.setScene(new Scene(root));
                    productDetailsStage.setOnCloseRequest(event -> productDetailsStage = null);
                    productDetailsStage.show();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                // If the window is already open, shake it instead of opening a new one
                errorUtilities.shakeWindow(productDetailsStage);
            }
        });
    }


    private boolean isPromptProductRegistrationRunning = false; // Flag to track whether the method is already running

    private void promptProductRegistration(String barcode) {
        if (!isPromptProductRegistrationRunning) { // Check if the method is not already running
            isPromptProductRegistrationRunning = true; // Set the flag to indicate that the method is running
            Platform.runLater(() -> {
                ConfirmationAlert confirmationAlert = new ConfirmationAlert("Product registration", "No product found", barcode + " has no associated product in the system, would you like to add it?", false);
                boolean confirm = confirmationAlert.showAndWait();
                if (confirm) {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("initialProductRegistration.fxml"));
                        Parent root = loader.load();
                        InitialProductRegistrationController controller = loader.getController();
                        controller.initializeProduct(barcode);
                        controller.setTableManagerController(this); // Pass the TableManagerController reference

                        Stage stage = new Stage();
                        stage.setTitle("Create new product");
                        stage.setScene(new Scene(root));
                        stage.setOnHidden(event -> isPromptProductRegistrationRunning = false); // Reset the flag when the stage is closed
                        stage.show();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    isPromptProductRegistrationRunning = false; // Reset the flag if the user cancels
                }
            });
        }
    }

    private boolean isValidBarcodeCharacter(String character) {
        return character.matches("[0-9]");
    }

    private void handleTableDoubleClick(Object selectedItem) {
        if (selectedItem instanceof Product selectedProduct) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("registerProduct.fxml"));
                Parent root = loader.load();

                int parentId = selectedProduct.getParentId();
                RegisterProductController controller = loader.getController();
                controller.initData(selectedProduct.getProductId());
                controller.setTableManager(this);

                Stage stage = new Stage();
                stage.setMaximized(true);
                stage.setTitle("Product Details");
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else if (selectedItem instanceof Supplier selectedSupplier) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("supplierInfoRegistration.fxml"));
                Parent root = loader.load();
                SupplierInfoRegistrationController controller = loader.getController();
                Platform.runLater(() -> controller.initData(selectedSupplier.getId()));
                controller.setTableManagerController(this);
                Stage stage = new Stage();
                stage.setTitle("Supplier Details");
                stage.setMaximized(true);
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                e.printStackTrace(); // Handle the exception according to your needs
            }
        }
    }


    EmployeeDAO employeeDAO = new EmployeeDAO();

    public void loadEmployeeTable() {
        tableHeader.setText("Employees");

        columnHeader1.setText("Employee ID");
        columnHeader2.setText("First Name");
        columnHeader3.setText("Middle Name");
        columnHeader4.setText("Last Name");
        columnHeader5.setText("Email");
        columnHeader6.setText("Contact");
        columnHeader7.setText("Position");
        columnHeader8.setText("Department");

        column1.setCellValueFactory(new PropertyValueFactory<>("user_id"));
        column2.setCellValueFactory(new PropertyValueFactory<>("user_fname"));
        column3.setCellValueFactory(new PropertyValueFactory<>("user_mname"));
        column4.setCellValueFactory(new PropertyValueFactory<>("user_lname"));
        column5.setCellValueFactory(new PropertyValueFactory<>("user_email"));
        column6.setCellValueFactory(new PropertyValueFactory<>("user_contact"));
        column7.setCellValueFactory(new PropertyValueFactory<>("user_position"));
        column8.setCellValueFactory(new PropertyValueFactory<>("userDepartmentString"));
        defaultTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                openEmployeeDetails((User) defaultTable.getSelectionModel().getSelectedItem());
            }
        });
        ObservableList<User> employees = employeeDAO.getAllEmployees();
        defaultTable.setItems(employees);

        employeeFilter(employees);

    }

    private void openEmployeeDetails(User selectedItem) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("employeeDetails.fxml"));
            Parent root = loader.load();

            EmployeeDetailsController controller = loader.getController();
            controller.initData(selectedItem);
            controller.setTableManager(this);

            Stage stage = new Stage();
            stage.setTitle("Employee Details");
            stage.setMaximized(true);
            stage.setResizable(false);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception according to your needs
        }
    }

    private final List<Branch> branchList = new ArrayList<>();

    private void loadBranchForPOTable() {
        tableHeader.setText("Select branch");
        addImage.setVisible(false);
        Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/Franchise.png")));

        tableImg.setImage(image);

        columnHeader2.setText("Description");
        columnHeader3.setText("Branch Name");
        columnHeader4.setText("Branch Head");
        columnHeader5.setText("Branch Code");
        columnHeader6.setText("State/Province");
        columnHeader7.setText("City");
        columnHeader8.setText("Barangay");

        column2.setCellValueFactory(new PropertyValueFactory<>("branchDescription"));
        column3.setCellValueFactory(new PropertyValueFactory<>("branchName"));
        column4.setCellValueFactory(new PropertyValueFactory<>("branchHeadName"));  // Updated to branchHeadName
        column5.setCellValueFactory(new PropertyValueFactory<>("branchCode"));
        column6.setCellValueFactory(new PropertyValueFactory<>("stateProvince"));
        column7.setCellValueFactory(new PropertyValueFactory<>("city"));
        column8.setCellValueFactory(new PropertyValueFactory<>("brgy"));

        defaultTable.getColumns().remove(column1);

        List<Branch> branches = new BranchDAO().getAllNonMovingNonReturnBranches();
        defaultTable.getItems().clear();
        branchList.clear();
        branchList.addAll(branches);
        defaultTable.getItems().addAll(branches);

        defaultTable.setRowFactory(tv -> {
            TableRow<Branch> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Branch rowData = row.getItem();
                    ConfirmationAlert confirmationAlert = new ConfirmationAlert("Add branch to PO", "Add " + rowData.getBranchDescription() + " to the PO?",
                            "You are adding " + rowData.getBranchDescription() + " to the purchase", false);

                    boolean userConfirmed = confirmationAlert.showAndWait();
                    if (userConfirmed) {
                        int branchId = rowData.getId();
                        purchaseOrderEntryController.addBranchToTable(branchId);
                        branchList.remove(rowData);

                        populateBranchForPO(branchList);
                    } else {
                        DialogUtils.showErrorMessage("Cancelled", "You have cancelled adding " + rowData.getBranchDescription() + " to your PO");
                    }
                }
            });
            return row;
        });
    }


    private void populateBranchForPO(List<Branch> branchList) {
        defaultTable.getItems().clear();
        defaultTable.getItems().addAll(branchList);
    }

    public void loadBranchTable() {
        defaultTable.getColumns().clear();
        tableHeader.setText("Branches");
        Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/Franchise.png")));
        tableImg.setImage(image);
        columnHeader1.setText("Branch ID");
        columnHeader2.setText("Description");
        columnHeader3.setText("Branch Name");
        columnHeader4.setText("Branch Head");
        columnHeader5.setText("Branch Code");
        columnHeader6.setText("State/Province");
        columnHeader7.setText("City");
        columnHeader8.setText("Barangay");
        TableColumn<Branch, ImageView> column9 = new TableColumn<>("Type");
        column1.setCellValueFactory(new PropertyValueFactory<>("id"));
        column2.setCellValueFactory(new PropertyValueFactory<>("branchDescription"));
        column3.setCellValueFactory(new PropertyValueFactory<>("branchName"));
        column4.setCellValueFactory(new PropertyValueFactory<>("branchHeadName"));
        column5.setCellValueFactory(new PropertyValueFactory<>("branchCode"));
        column6.setCellValueFactory(new PropertyValueFactory<>("stateProvince"));
        column7.setCellValueFactory(new PropertyValueFactory<>("city"));
        column8.setCellValueFactory(new PropertyValueFactory<>("brgy"));
        column9.setCellValueFactory(param -> {
            ImageView imageView = new ImageView();
            boolean isMoving = param.getValue().isMoving();

            if (isMoving) {
                imageView.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/Truck.png"))));
            } else {
                imageView.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/warehouse.png"))));
            }
            imageView.setFitHeight(20);
            imageView.setFitWidth(20);
            return new ReadOnlyObjectWrapper<>(imageView);
        });

        BranchDAO branchDAO = new BranchDAO();
        List<Branch> branches = branchDAO.getBranchesWithNamesHead();
        defaultTable.getItems().clear();
        defaultTable.getItems().addAll(branches);

        defaultTable.setRowFactory(tv -> {
            TableRow<Branch> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Branch selectedBranch = row.getItem();
                    openBranchDetails(selectedBranch.getId());
                }
            });
            return row;
        });

        searchBar.setVisible(true);
        searchBar.setPromptText("Search branch description");
        searchBar.textProperty().addListener((observable, oldValue, newValue) -> {
            Comparator<Branch> comparator = Comparator.comparing(branch ->
                    branch.getBranchDescription().toLowerCase().indexOf(newValue.toLowerCase())
            );
            defaultTable.getItems().sort(comparator.reversed());
        });


        defaultTable.getColumns().addAll(column2, column3, column4, column6, column7, column8, column9);
    }


    private void openBranchDetails(int id) {
        try {
            // Load the branch details FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("branchRegistration.fxml"));
            Parent root = loader.load();

            BranchRegistrationController controller = loader.getController();
            controller.initData(id);
            controller.setTableManager(this);

            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setTitle("Branch Details");
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void loadCompanyTable() {
        ObservableList<Company> companies = FXCollections.observableArrayList();

        Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/business-and-trade.png")));

        tableImg.setImage(image);

        tableHeader.setText("Companies");
        columnHeader1.setText("Company ID");
        columnHeader2.setText("Company Name");
        columnHeader3.setText("Logo");
        columnHeader4.setText("Company Code");
        columnHeader5.setText("Company Type");
        columnHeader6.setText("First Address");
        columnHeader7.setText("Registration Number");
        columnHeader8.setText("TIN");
        // Set cell value factories for table columns
        column1.setCellValueFactory(new PropertyValueFactory<>("companyId"));
        column2.setCellValueFactory(new PropertyValueFactory<>("companyName"));
        column3.setCellValueFactory(new PropertyValueFactory<>("companyLogo"));
        column3.setCellFactory(param -> new TableCell<Company, String>() {
            private final ImageView imageView = new ImageView();

            {
                setAlignment(Pos.CENTER);
                ImageCircle.circular(imageView);
            }

            @Override
            protected void updateItem(String imagePath, boolean empty) {
                super.updateItem(imagePath, empty);
                if (empty || imagePath == null || imagePath.isEmpty()) {
                    setGraphic(null);
                } else {
                    try {
                        File file = new File(imagePath);
                        Image image = new Image(file.toURI().toString());
                        imageView.setImage(image);
                        imageView.setFitWidth(50);
                        imageView.setFitHeight(50);
                        setGraphic(imageView);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        column4.setCellValueFactory(new PropertyValueFactory<>("companyCode"));
        column5.setCellValueFactory(new PropertyValueFactory<>("companyType"));
        column6.setCellValueFactory(new PropertyValueFactory<>("companyFirstAddress"));
        column7.setCellValueFactory(new PropertyValueFactory<>("companyRegistrationNumber"));
        column8.setCellValueFactory(new PropertyValueFactory<>("companyTIN"));
        String query = "SELECT * FROM company";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            defaultTable.getItems().clear();
            while (resultSet.next()) {
                Company company = new Company(
                        resultSet.getInt("company_id"),
                        resultSet.getString("company_name"),
                        resultSet.getString("company_type"),
                        resultSet.getString("company_code"),
                        resultSet.getString("company_firstAddress"),
                        resultSet.getString("company_secondAddress"),
                        resultSet.getString("company_registrationNumber"),
                        resultSet.getString("company_tin"),
                        resultSet.getDate("company_dateAdmitted"),
                        resultSet.getString("company_contact"),
                        resultSet.getString("company_email"),
                        resultSet.getString("company_department"),
                        resultSet.getString("company_logo"),
                        resultSet.getString("company_tags")
                );
                companies.add(company);
                defaultTable.setItems(companies);
            }
            defaultTable.setRowFactory(tv -> {
                TableRow<Company> row = new TableRow<>();
                row.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2 && !row.isEmpty()) {
                        Company selectedCompany = row.getItem();
                        openCompany(selectedCompany);
                    }
                });
                return row;
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void openCompany(Company selectedCompany) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("companyRegistration.fxml"));
            Parent root = loader.load();

            CompanyRegistrationController controller = loader.getController();
            controller.initData(selectedCompany);
            controller.setTableManager(this);

            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setTitle(selectedCompany.getCompanyName());
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private SupplierInfoRegistrationController supplierInfoRegistrationController;

    void setSupplierController(SupplierInfoRegistrationController supplierInfoRegistrationController) {
        this.supplierInfoRegistrationController = supplierInfoRegistrationController;
    }

    BranchDAO branchDAO = new BranchDAO();
    InventoryDAO inventoryDAO = new InventoryDAO();
    @Setter
    StockTransferController stockTransferController;

    public void loadBranchProductsTable(int sourceBranchId) {
        tableHeader.setText("Product transfer list");
        defaultTable.getColumns().clear();
        addImage.setVisible(false);
        tableImg.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/package.png"))));
        searchBar.setVisible(true);
        categoryBar.setVisible(true);
        searchBar.setPromptText("Search product description");
        categoryBar.setPromptText("Search specifics");

        InventoryDAO inventoryDAO = new InventoryDAO();
        ProductDAO productDAO = new ProductDAO();
        ;

        CompletableFuture<AbstractMap.SimpleEntry<List<Inventory>, Map<Integer, Product>>> future = CompletableFuture.supplyAsync(() -> inventoryDAO.getInventoryItemsByBranch(sourceBranchId))
                .thenComposeAsync(filteredInventoryItems -> {
                    List<Integer> productIds = filteredInventoryItems.stream()
                            .map(Inventory::getProductId)
                            .collect(Collectors.toList());

                    return CompletableFuture.completedFuture(new AbstractMap.SimpleEntry<>(filteredInventoryItems, productDAO.getProductsByIds(productIds).stream()
                            .collect(Collectors.toMap(Product::getProductId, Function.identity()))));
                });

        future.thenAcceptAsync(result -> {
            Platform.runLater(() -> {
                try {
                    TableColumn<Inventory, String> productDescriptionColumn = new TableColumn<>("Product Description");
                    productDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("productDescription"));

                    TableColumn<Inventory, String> unitColumn = new TableColumn<>("Unit");
                    unitColumn.setCellValueFactory(cellData -> {
                        Product product = result.getValue().get(cellData.getValue().getProductId());
                        return new SimpleStringProperty(product != null ? product.getUnitOfMeasurementString() : "");
                    });

                    TableColumn<Inventory, Integer> quantityColumn = new TableColumn<>("Quantity");
                    quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));

                    TableColumn<Inventory, String> productNameColumn = new TableColumn<>("Product Name");
                    productNameColumn.setCellValueFactory(cellData -> {
                        Product product = result.getValue().get(cellData.getValue().getProductId());
                        return new SimpleStringProperty(product != null ? product.getProductName() : "");
                    });

                    TableColumn<Inventory, String> brandColumn = new TableColumn<>("Brand");
                    brandColumn.setCellValueFactory(cellData -> {
                        Product product = result.getValue().get(cellData.getValue().getProductId());
                        return new SimpleStringProperty(product != null ? product.getProductBrandString() : "");
                    });

                    TableColumn<Inventory, String> categoryColumn = new TableColumn<>("Category");
                    categoryColumn.setCellValueFactory(cellData -> {
                        Product product = result.getValue().get(cellData.getValue().getProductId());
                        return new SimpleStringProperty(product != null ? product.getProductCategoryString() : "");
                    });

                    defaultTable.getColumns().addAll(
                            productDescriptionColumn,
                            unitColumn,
                            quantityColumn,
                            brandColumn,
                            categoryColumn
                    );

                    defaultTable.setRowFactory(tv -> {
                        TableRow<Inventory> row = new TableRow<>();
                        row.setOnMouseClicked(event -> {
                            if (event.getClickCount() == 2 && !row.isEmpty()) {
                                Inventory rowData = row.getItem();
                                int productId = rowData.getProductId();
                                stockTransferController.addProductToBranchTables(productId);
                                defaultTable.getItems().remove(rowData);
                            }
                        });
                        return row;
                    });
                    defaultTable.setOnKeyPressed(event -> {
                        if (event.getCode() == KeyCode.ENTER) {
                            Inventory rowData = (Inventory) defaultTable.getSelectionModel().getSelectedItem();
                            if (rowData != null) {
                                int productId = rowData.getProductId();
                                stockTransferController.addProductToBranchTables(productId);
                                defaultTable.getItems().remove(rowData);
                            }
                        }
                    });

                    FilteredList<Inventory> filteredList = new FilteredList<>(FXCollections.observableArrayList(result.getKey()), p -> true);
                    defaultTable.setItems(filteredList);
                    searchBar.textProperty().addListener((observable, oldValue, newValue) -> {
                        filteredList.setPredicate(p -> {
                            // If both search fields have values, filter by both
                            boolean matchesDescription = newValue == null || newValue.isEmpty() || p.getProductDescription().toLowerCase().contains(newValue.toLowerCase());
                            boolean matchesUnit = categoryBar.getText() == null || categoryBar.getText().isEmpty() || p.getUnit().toLowerCase().contains(categoryBar.getText().toLowerCase());

                            return matchesDescription && matchesUnit;
                        });
                    });

                    // Filter by category or unit
                    categoryBar.textProperty().addListener((observable, oldValue, newValue) -> {
                        filteredList.setPredicate(p -> {
                            boolean matchesDescription = searchBar.getText() == null || searchBar.getText().isEmpty() || p.getProductDescription().toLowerCase().contains(searchBar.getText().toLowerCase());
                            boolean matchesUnit = newValue == null || newValue.isEmpty() || p.getUnit().toLowerCase().contains(newValue.toLowerCase());

                            return matchesDescription && matchesUnit;
                        });
                    });
                } catch (Exception e) {
                    DialogUtils.showErrorMessage("Error", "An error occurred while loading product data.");
                    e.printStackTrace();
                }
            });
        });
    }


    private void selectAllProductForStockTransfer() {
        // Get all items from the TableView
        ObservableList<Inventory> items = defaultTable.getItems();

        // Select all items
        defaultTable.getSelectionModel().selectAll();

        // Iterate over selected items and add them to the branch
        for (Inventory item : items) {
            int productId = item.getProductId();
            stockTransferController.addProductToBranchTables(productId);
        }

        // Optionally, clear the table if needed
        defaultTable.getItems().clear();
    }


}
