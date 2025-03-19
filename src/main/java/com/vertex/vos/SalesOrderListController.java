package com.vertex.vos;

import com.fasterxml.jackson.core.JsonParser;
import com.vertex.vos.Enums.SalesOrderStatus;
import com.vertex.vos.Objects.*;
import com.vertex.vos.Utilities.*;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.Getter;
import lombok.Setter;
import org.controlsfx.control.textfield.TextFields;

import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class SalesOrderListController implements Initializable {

    public TableColumn<SalesOrder, String> supplierCol;
    public TextField supplierFilter;
    public TextField branchFilter;
    public TableColumn<SalesOrder, String> branchNameCol;
    public BorderPane borderPane;
    public TableColumn<SalesOrder, String> poNoCol;
    @FXML
    private Button confirmButton;

    @FXML
    private TableColumn<SalesOrder, Timestamp> createdDateCol;

    @FXML
    private TableColumn<SalesOrder, String> customerCodeCol;

    @FXML
    private TableColumn<SalesOrder, Timestamp> orderDateCol;

    @FXML
    private DatePicker orderDateFromFilter;

    @FXML
    private DatePicker orderDateToFilter;

    @FXML
    private TableColumn<SalesOrder, String> orderNoCol;

    @FXML
    private TextField orderNoFilter;

    @FXML
    private TableView<SalesOrder> orderTable;

    @FXML
    private TableColumn<SalesOrder, String> receiptTypeCol;

    @FXML
    private TableColumn<SalesOrder, String> salesmanCodeCol;

    @FXML
    private TextField salesmanFilter;

    @FXML
    private TableColumn<SalesOrder, String> salesmanNameCol;

    @FXML
    private TableColumn<SalesOrder, String> statusCol;

    @FXML
    private ComboBox<SalesOrderStatus> statusFilter;

    @FXML
    private TableColumn<SalesOrder, String> storeNameCol;

    @FXML
    private TextField storeNameFilter;

    @FXML
    private TableColumn<SalesOrder, Double> totalAmountCol;

    ObservableList<SalesOrder> salesOrderList = FXCollections.observableArrayList();

    SalesOrderDAO salesOrderDAO = new SalesOrderDAO();

    private int currentPage = 0; // Keep track of the current page

    private static final int PAGE_SIZE = 35; // Number of items per page
    @FXML
    private TextField poNoFilter;

    Customer selectedCustomer;
    Salesman selectedSalesman;
    Supplier selectedSupplier;
    Branch selectedBranch;

    public void loadSalesOrder() {
        borderPane.setCursor(javafx.scene.Cursor.WAIT);
        orderTable.setPlaceholder(new ProgressIndicator());

        Task<ObservableList<SalesOrder>> task = new Task<>() {
            @Override
            protected ObservableList<SalesOrder> call() {
                String orderNo = orderNoFilter.getText();
                String customer = storeNameFilter.getText();
                String salesman = salesmanFilter.getText();
                String supplier = supplierFilter.getText();
                String branch = branchFilter.getText();
                String poNo = poNoFilter.getText();
                SalesOrderStatus status = statusFilter.getValue();
                Timestamp orderDateFrom = orderDateFromFilter.getValue() != null ? Timestamp.valueOf(orderDateFromFilter.getValue().atStartOfDay()) : null;
                Timestamp orderDateTo = orderDateToFilter.getValue() != null ? Timestamp.valueOf(orderDateToFilter.getValue().atTime(23, 59, 59)) : null;

                return FXCollections.observableArrayList(
                    salesOrderDAO.getAllSalesOrders(currentPage * PAGE_SIZE, PAGE_SIZE, selectedBranch, orderNo, poNo ,selectedCustomer, selectedSalesman, selectedSupplier, status, orderDateFrom, orderDateTo)
                );
            }

            @Override
            protected void succeeded() {
                borderPane.setCursor(javafx.scene.Cursor.DEFAULT);
                salesOrderList.clear();
                salesOrderList.addAll(getValue());

                if (salesOrderList.isEmpty()) {
                    orderTable.setPlaceholder(new Label("No orders found."));
                }

                confirmButton.setText("Add New");
                confirmButton.setOnAction(event -> addNewSalesOrder());
            }

            @Override
            protected void failed() {
                getException().printStackTrace();
            }
        };

        new Thread(task).start();
    }

   @Getter
   private Stage salesOrderFormStage;

   private void addNewSalesOrder() {
       if (salesOrderFormStage == null) {
           try {
               FXMLLoader loader = new FXMLLoader(getClass().getResource("SalesOrderForm.fxml"));
               Parent content = loader.load();
               SalesOrderFormController controller = loader.getController();
               controller.setSalesOrderListController(this);
               controller.createNewSalesOrder();
               controller.setGeneratedSONo(salesOrderDAO.getNextSoNo());
               salesOrderFormStage = new Stage();
               salesOrderFormStage.setTitle("Create New Sales Order");
               salesOrderFormStage.setScene(new Scene(content));
               salesOrderFormStage.setMaximized(true);
               salesOrderFormStage.showAndWait();
               salesOrderFormStage = null; // Reset the stage after closing
           } catch (Exception e) {
               e.printStackTrace();
           }
       } else {
           salesOrderFormStage.toFront();
       }
   }
    @Setter
    Stage SalesOrderListStage;

    CustomerDAO customerDAO = new CustomerDAO();
    SalesmanDAO salesmanDAO = new SalesmanDAO();
    SupplierDAO supplierDAO = new SupplierDAO();
    BranchDAO   branchDAO = new BranchDAO();

    ObservableList<Customer> customers  = FXCollections.observableArrayList(customerDAO.getAllActiveCustomers());
    ObservableList<Salesman> salesmen = FXCollections.observableArrayList(salesmanDAO.getAllActiveSalesmen());
    ObservableList<Supplier> suppliers = FXCollections.observableArrayList(supplierDAO.getAllActiveSuppliers());
    ObservableList<Branch> branches = FXCollections.observableArrayList(branchDAO.getAllActiveBranches());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        setupFilters();
        setupTableListeners();

        TableViewFormatter.formatTableView(orderTable);
    }

    /**
     * Sets up table column cell value factories.
     */
    private void setupTableColumns() {
        orderNoCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getOrderNo()));
        poNoCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPurchaseNo()));
        storeNameCol.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getCustomer() != null ? cellData.getValue().getCustomer().getStoreName() : ""));
        customerCodeCol.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getCustomer() != null ? cellData.getValue().getCustomer().getCustomerCode() : ""));
        salesmanNameCol.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getSalesman() != null ? cellData.getValue().getSalesman().getSalesmanName() : ""));
        salesmanCodeCol.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getSalesman() != null ? cellData.getValue().getSalesman().getSalesmanCode() : ""));
        orderDateCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getOrderDate()));
        supplierCol.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getSupplier() != null ? cellData.getValue().getSupplier().getSupplierName() : ""));
        createdDateCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getCreatedDate()));
        totalAmountCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getTotalAmount()));
        receiptTypeCol.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getInvoiceType() != null ? cellData.getValue().getInvoiceType().getName() : ""));
        statusCol.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getOrderStatus() != null ? cellData.getValue().getOrderStatus().getDbValue() : ""));
        branchNameCol.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getBranch() != null ? cellData.getValue().getBranch().getBranchName() : ""));

        statusFilter.setItems(FXCollections.observableArrayList(SalesOrderStatus.values()));
        orderTable.setItems(salesOrderList);
    }

    /**
     * Sets up filter listeners and auto-completion.
     */
    private void setupFilters() {
        TextFields.bindAutoCompletion(storeNameFilter, customers.stream().map(Customer::getStoreName).toList());
        TextFields.bindAutoCompletion(salesmanFilter, salesmen.stream().map(Salesman::getSalesmanName).toList());
        TextFields.bindAutoCompletion(supplierFilter, suppliers.stream().map(Supplier::getSupplierName).toList());
        TextFields.bindAutoCompletion(branchFilter, branches.stream().map(Branch::getBranchName).toList());

        // Debounce filter to avoid excessive calls
        PauseTransition filterDebounce = new PauseTransition(Duration.millis(300));
        filterDebounce.setOnFinished(event -> {
            currentPage = 0;
            loadSalesOrder();
        });

        Consumer<String> filterUpdate = (newValue) -> {
            filterDebounce.playFromStart();
        };

        orderNoFilter.textProperty().addListener((obs, oldVal, newVal) -> filterUpdate.accept(newVal));
        poNoFilter.textProperty().addListener((obs, oldVal, newVal) -> filterUpdate.accept(newVal));
        storeNameFilter.textProperty().addListener((obs, oldVal, newVal) -> {
            selectedCustomer = customers.stream()
                    .filter(customer -> customer.getStoreName().equalsIgnoreCase(newVal))
                    .findFirst().orElse(null);
            filterUpdate.accept(newVal);
        });
        salesmanFilter.textProperty().addListener((obs, oldVal, newVal) -> {
            selectedSalesman = salesmen.stream()
                    .filter(salesman -> salesman.getSalesmanName().equalsIgnoreCase(newVal))
                    .findFirst().orElse(null);
            filterUpdate.accept(newVal);
        });
        supplierFilter.textProperty().addListener((obs, oldVal, newVal) -> {
            selectedSupplier = suppliers.stream()
                    .filter(supplier -> supplier.getSupplierName().equalsIgnoreCase(newVal))
                    .findFirst().orElse(null);
            filterUpdate.accept(newVal);
        });
        branchFilter.textProperty().addListener((obs, oldVal, newVal) -> {
            selectedBranch = branches.stream()
                    .filter(branch -> branch.getBranchName().equalsIgnoreCase(newVal))
                    .findFirst().orElse(null);
            filterUpdate.accept(newVal);
        });
        statusFilter.valueProperty().addListener((obs, oldVal, newVal) -> filterUpdate.accept(null));

        // Handle Date Pickers
        orderDateFromFilter.valueProperty().addListener((obs, oldVal, newVal) -> filterUpdate.accept(null));
        orderDateToFilter.valueProperty().addListener((obs, oldVal, newVal) -> filterUpdate.accept(null));
    }

    /**
     * Sets up table listeners for scroll and click actions.
     */
    private void setupTableListeners() {
        orderTable.setOnScroll(event -> {
            if (isScrollNearBottom()) {
                currentPage++;
                loadSalesOrder();
            }
        });

        orderTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                openCardForSalesOrder(orderTable.getSelectionModel().getSelectedItem());
            }
        });

        orderTable.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                openCardForSalesOrder(orderTable.getSelectionModel().getSelectedItem());
            }
        });
    }


    private void openCardForSalesOrder(SalesOrder selectedItem) {
        if (selectedItem == null) {
            System.out.println("No sales order selected.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("SalesOrderCardPane.fxml"));
            Parent cardPane = loader.load();
            SalesOrderCardPaneController controller = loader.getController();
            controller.setData(selectedItem);
            controller.setSalesOrderListController(this);

            // Automatically resize based on content
            borderPane.setRight(cardPane);
            BorderPane.setAlignment(cardPane, Pos.TOP_RIGHT);

            // Allow shrinking/expanding dynamically
            cardPane.setManaged(true);
            cardPane.setVisible(true);
        } catch (IOException e) {
            System.err.println("Error loading Sales Order Card: " + e.getMessage());
            e.printStackTrace();
        }
    }


    // Check if the TableView is scrolled to the bottom
    private boolean isScrollNearBottom() {
        ScrollBar scrollBar = (ScrollBar) orderTable.lookup(".scroll-bar:vertical");
        if (scrollBar != null) {
            double value = scrollBar.getValue();
            double max = scrollBar.getMax();
            double visibleAmount = scrollBar.getVisibleAmount();
            return value >= max - visibleAmount;
        }
        return false;
    }

    @Getter
    Stage existingSalesOrderStage;

    public void openSalesOrder(SalesOrder selectedItem) {
        try {
            Platform.runLater(() -> {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("SalesOrderForm.fxml"));
                    Parent content = loader.load();
                    SalesOrderFormController controller = loader.getController();
                    controller.setSalesOrderListController(this);
                    controller.openSalesOrder(selectedItem);

                    if (existingSalesOrderStage == null) {
                        existingSalesOrderStage = new Stage();
                        existingSalesOrderStage.setMaximized(true);
                        existingSalesOrderStage.setOnCloseRequest(event -> existingSalesOrderStage = null);
                    }

                    existingSalesOrderStage.setTitle(selectedItem.getOrderNo());
                    existingSalesOrderStage.setScene(new Scene(content));
                    existingSalesOrderStage.showAndWait();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Getter
    Stage conversionStage;

    public void openSalesOrderForConversion(SalesOrder selectedItem) {
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("SalesOrderConversionForm.fxml"));
                Parent content = loader.load();
                SalesOrderConversionFormController controller = loader.getController();
                controller.setSalesOrderListController(this);
                controller.openSalesOrder(selectedItem);

                if (conversionStage == null) {
                    conversionStage = new Stage();
                    conversionStage.setMaximized(true);
                    conversionStage.setOnCloseRequest(event -> conversionStage = null);
                }

                conversionStage.setTitle(selectedItem.getOrderNo());
                conversionStage.setScene(new Scene(content));
                conversionStage.showAndWait();

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
