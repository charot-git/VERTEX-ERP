package com.vertex.vos;

import com.vertex.vos.DAO.SalesInvoiceDAO;
import com.vertex.vos.DAO.SalesInvoiceTypeDAO;
import com.vertex.vos.Objects.*;
import com.vertex.vos.Utilities.*;
import com.zaxxer.hikari.HikariDataSource;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;
import lombok.Setter;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class SalesInvoiceTemporaryController implements Initializable {

    public TextField invoiceNoTextField;
    public Label salesNo;
    public TableView<SalesInvoiceDetail> itemsTable;
    public TableColumn<SalesInvoiceDetail, String> productCodeItemCol;
    public TableColumn<SalesInvoiceDetail, String> descriptionItemCol;
    public TableColumn<SalesInvoiceDetail, String> unitItemCol;
    public TableColumn<SalesInvoiceDetail, Integer> quantityItemCol;
    public TableColumn<SalesInvoiceDetail, Double> priceItemCol;
    public TableColumn<SalesInvoiceDetail, Double> discountItemCol;
    public TableColumn<SalesInvoiceDetail, Double> netAmountItemCol;
    public ComboBox<String> priceType;
    public TextField salesmanLocationTextField;
    public TableColumn<SalesInvoiceDetail, Double> grossAmountCol;
    public Label grossAmount;
    public Label discountAmount;
    public Label netAmount;
    public Label paidAmount;
    public Label datePaid;
    public Label balanceAmount;
    public Label netOfVatAmount;
    public Label vatAmount;
    public TextField customerCodeTextField;
    public Button dispatchButton;
    public DatePicker dispatchDate;
    public TextArea remarks;
    public DatePicker dueDate;
    public Label transactionStatus;
    public Label paymentStatus;
    public BorderPane salesInvoiceBorderPane;
    @FXML
    private VBox addProductToItems;

    @FXML
    private VBox addProductToReturns;

    @FXML
    private TextField branchTextField;

    @FXML
    private Button confirmButton;

    @FXML
    private TextField customerTextField;

    @FXML
    private DatePicker invoiceDate;

    @FXML
    private ComboBox<String> receiptType;

    @FXML
    private ComboBox<String> salesType;

    @FXML
    private TextField salesmanTextField;


    private ObservableList<String> salesTypeList = FXCollections.observableArrayList();
    private ObservableList<Customer> customers = FXCollections.observableArrayList();
    private ObservableList<Salesman> salesmen = FXCollections.observableArrayList();

    SalesInvoiceTypeDAO salesInvoiceTypeDAO = new SalesInvoiceTypeDAO();
    CustomerDAO customerDAO = new CustomerDAO();
    SalesmanDAO salesmanDAO = new SalesmanDAO();

    Customer selectedCustomer = null;
    Salesman selectedSalesman = null;

    private Stage productSelectionStage = null; // Track the Product Selection stage

    SalesOrderDAO salesOrderDAO = new SalesOrderDAO();

    SalesInvoiceHeader salesInvoiceHeader = new SalesInvoiceHeader();

    ObservableList<SalesInvoiceDetail> salesInvoiceDetails = FXCollections.observableArrayList(); // List to hold sales invoice details>

    int soNo = 0;

    Stage stage;

    public void createNewSalesEntry(Stage stage) {
        this.stage = stage;
        soNo = salesOrderDAO.getNextSoNo();
        salesInvoiceHeader.setCreatedBy(UserSession.getInstance().getUserId());
        salesInvoiceHeader.setCreatedDate(new java.sql.Timestamp(System.currentTimeMillis()));
        salesInvoiceHeader.setModifiedBy(UserSession.getInstance().getUserId());
        salesInvoiceHeader.setOrderId("MEN-" + soNo);
        salesInvoiceHeader.setTransactionStatus("Encoding");
        salesInvoiceHeader.setPaymentStatus("Unpaid");
        transactionStatus.setText(salesInvoiceHeader.getTransactionStatus());
        paymentStatus.setText(salesInvoiceHeader.getPaymentStatus());
        salesNo.setText(salesInvoiceHeader.getOrderId());

        invoiceDate.setValue(LocalDate.now());

        salesType.setItems(salesTypeList);
        salesType.setValue("BOOKING");

        salesInvoiceHeader.setSalesType(1);

        receiptType.setValue(salesInvoiceTypeList.getFirst().getName());

        salesInvoiceHeader.setInvoiceType(salesInvoiceTypeList.getFirst());

        customers.setAll(customerDAO.getAllActiveCustomers());
        salesmen.setAll(salesmanDAO.getAllSalesmen());

        customerTextField.requestFocus();

        customerTextField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                GenericSelectionWindow<Customer> selectionWindow = new GenericSelectionWindow<>();
                customerTextField.setOnKeyPressed(event -> {
                    if (event.getCode() == KeyCode.DOWN) {
                        selectedCustomer = selectionWindow.showSelectionWindow(stage, "Select Customer", customers);
                        if (selectedCustomer != null) {
                            customerTextField.setText(selectedCustomer.getStoreName());
                            customerCodeTextField.setText(selectedCustomer.getCustomerCode());
                            salesInvoiceHeader.setCustomer(selectedCustomer);
                        }
                    }
                });
            }
        });

        salesmanTextField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                GenericSelectionWindow<Salesman> selectionWindow = new GenericSelectionWindow<>();
                salesmanTextField.setOnKeyPressed(event -> {
                    if (event.getCode() == KeyCode.DOWN) {
                        selectedSalesman = selectionWindow.showSelectionWindow(stage, "Select Salesman", salesmen);
                        if (selectedSalesman != null) {
                            salesInvoiceHeader.setOrderId(selectedSalesman.getSalesmanCode() + "-" + salesOrderDAO.getNextSoNo());
                            salesNo.setText(salesInvoiceHeader.getOrderId());
                            salesmanTextField.setText(selectedSalesman.getSalesmanName());
                            salesmanLocationTextField.setText(selectedSalesman.getSalesmanCode());
                            if (selectedSalesman.getPriceType() != null) {
                                priceType.setValue(selectedSalesman.getPriceType());
                            }

                            if (selectedSalesman.getOperation() != -1) {
                                if (selectedSalesman.getOperation() == 1) {
                                    salesType.getSelectionModel().select("BOOKING");
                                } else if (selectedSalesman.getOperation() == 2) {
                                    salesType.getSelectionModel().select("DISTRIBUTOR");
                                } else if (selectedSalesman.getOperation() == 3) {
                                    salesType.getSelectionModel().select("VAN SALES");

                                }
                            }

                            final Salesman salesman = selectedSalesman;
                            salesInvoiceHeader.setSalesman(salesman);
                            salesInvoiceHeader.setPriceType(salesman.getPriceType().charAt(0));
                            addProductToItems.setOnMouseClicked(mouseEvent -> openProductSelection(stage, salesman));

                            invoiceDate.requestFocus();
                            confirmButton.setOnMouseClicked(mouseEvent -> createSalesInvoice());
                        }
                    }
                });
            }
        });
    }

    private void updateInvoice() {
        Connection connection = null;
        try {
            // Get a connection from the data source
            connection = dataSource.getConnection();
            connection.setAutoCommit(false); // Disable auto-commit for transaction handling

            // Call the DAO method and pass the connection
            boolean result = salesInvoiceDAO.createSalesInvoiceWithDetails(salesInvoiceHeader, salesInvoiceDetails, connection);

            if (result) {
                connection.commit(); // Commit transaction if successful
                DialogUtils.showCompletionDialog(
                        "Sales Invoice Updated",
                        "Success " +
                                "Sales invoice updated successfully."
                );
            } else {
                connection.rollback(); // Rollback transaction on failure
                DialogUtils.showErrorMessage(
                        "Sales Invoice Update Failed",
                        "Update Failed " +
                                "An error occurred while updating the sales invoice. Changes have been rolled back."
                );
                throw new RuntimeException("Sales invoice update failed: DAO returned false.");
            }
        } catch (SQLException e) {
            // Rollback transaction if an exception occurs
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("Rollback failed: " + rollbackEx.getMessage());
                    rollbackEx.printStackTrace();
                }
            }
            // Log and rethrow the exception for further handling
            throw new RuntimeException("Error while updating sales invoice: " + e.getMessage(), e);
        } finally {
            // Ensure the connection is closed
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException closeEx) {
                    System.err.println("Failed to close connection: " + closeEx.getMessage());
                    closeEx.printStackTrace();
                }
            }
            salesInvoicesController.setUpTableData();
        }
    }

    private void createSalesInvoice() {
        salesInvoiceHeader.setSalesman(selectedSalesman);
        salesInvoiceHeader.setCustomer(selectedCustomer);
        salesInvoiceHeader.setTransactionStatus("Encoded");
        salesInvoiceHeader.setPaymentStatus("Unpaid");
        salesInvoiceHeader.setInvoiceDate(new java.sql.Timestamp(invoiceDate.getValue().atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()));
        salesInvoiceHeader.setDispatchDate(new java.sql.Timestamp(dispatchDate.getValue().atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()));
        salesInvoiceHeader.setDueDate(new java.sql.Timestamp(dueDate.getValue().atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()));
        salesInvoiceHeader.setPaymentTerms(selectedCustomer.getPaymentTerm());
        salesInvoiceHeader.setInvoiceNo(invoiceNoTextField.getText());
        salesInvoiceHeader.setModifiedDate(new java.sql.Timestamp(System.currentTimeMillis()));
        salesInvoiceHeader.setPostedBy(UserSession.getInstance().getUserId());
        salesInvoiceHeader.setPostedDate(new java.sql.Timestamp(System.currentTimeMillis()));
        salesInvoiceHeader.setRemarks(remarks.getText());
        salesInvoiceHeader.setPosted(false);
        salesInvoiceHeader.setDispatched(false);

        try {
            boolean result = salesInvoiceDAO.createSalesInvoiceWithDetails(salesInvoiceHeader, salesInvoiceDetails, dataSource.getConnection());
            if (result) {
                DialogUtils.showCompletionDialog("Sales Invoice Created", "Sales Invoice created successfully");
                stage.close();
            } else {
                DialogUtils.showErrorMessage("Sales Invoice Creation Failed", "Sales Invoice creation failed");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    SalesInvoiceDAO salesInvoiceDAO = new SalesInvoiceDAO();

    private void openProductSelection(Stage parentStage, Salesman salesman) {
        if (salesman == null) {
            return;
        }

        if (selectedCustomer == null) {
            return;
        }
        if (productSelectionStage == null || !productSelectionStage.isShowing()) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("SalesInvoiceProductSelectionTemp.fxml"));
                Parent root = loader.load();
                SalesInvoiceProductSelectionTempController controller = loader.getController();

                productSelectionStage = new Stage();
                productSelectionStage.setTitle("Product Selection Temporary");
                controller.setStage(productSelectionStage);
                controller.setSalesInvoiceTemporaryController(this);
                controller.setPriceType(priceType.getValue());
                controller.setBranch(salesman.getGoodBranchCode());
                controller.setSelectedCustomer(selectedCustomer);

                // Pass already selected items
                controller.setSelectedItems(FXCollections.observableArrayList(salesInvoiceDetails));

                productSelectionStage.setMaximized(true); // Maximize the window when opened
                productSelectionStage.setScene(new Scene(root));
                // Prevent the user from closing the window while on transact
                productSelectionStage.setOnCloseRequest(event -> {
                    if (salesInvoiceHeader.getTransactionStatus().equals("Encoding")) {
                        productSelectionStage.hide();
                    }
                });
                productSelectionStage.show();

                parentStage.setOnCloseRequest(event -> productSelectionStage.close());

            } catch (IOException e) {
                DialogUtils.showErrorMessage("Error", "Unable to open product selection.");
                e.printStackTrace();
            }
        } else {
            // If already open, bring it to the front and maximize it
            productSelectionStage.toFront();
            productSelectionStage.setMaximized(true);
        }
    }


    public void addProductToSalesInvoice(SalesInvoiceDetail selectedProduct) {
        selectedProduct.setOrderId(salesInvoiceHeader.getOrderId());
        salesInvoiceDetails.add(selectedProduct);
    }


    DiscountDAO discountDAO = new DiscountDAO();


    ObservableList<SalesInvoiceType> salesInvoiceTypeList = salesInvoiceTypeDAO.getSalesInvoiceTypes();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        TableViewFormatter.formatTableView(itemsTable);

        salesTypeList.add("BOOKING");
        salesTypeList.add("DISTRIBUTOR");
        salesTypeList.add("VAN SALES");

        salesType.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                switch (newValue) {
                    case "BOOKING":
                        salesInvoiceHeader.setSalesType(1);
                        break;
                    case "DISTRIBUTOR":
                        salesInvoiceHeader.setSalesType(2);
                        break;
                    case "VAN SALES":
                        salesInvoiceHeader.setSalesType(3);
                        break;
                }
            }
        });

        ObservableList<String> salesInvoiceTypeNames = FXCollections.observableArrayList();
        for (SalesInvoiceType salesInvoiceType : salesInvoiceTypeList) {
            salesInvoiceTypeNames.add(salesInvoiceType.getName());
        }

        receiptType.setItems(salesInvoiceTypeNames);


        // Set up price types in the combo box
        priceType.getItems().addAll("A", "B", "C", "D", "E");
        priceType.setValue("A"); // Default price type

        receiptType.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                salesInvoiceTypeList.stream().filter(type -> type.getName().equals(newValue)).findFirst().ifPresent(selectedType -> salesInvoiceHeader.setInvoiceType(selectedType));
                updateTotals();
            }
        });


        // Configure TableView columns
        productCodeItemCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduct().getProductCode()));
        descriptionItemCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduct().getDescription()));
        unitItemCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduct().getUnitOfMeasurementString()));

        // Make the quantity column editable
        quantityItemCol.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getQuantity()).asObject());
        quantityItemCol.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        quantityItemCol.setEditable(true); // Enable editing for the quantity column

        // Handle the quantity edit commit to update the quantity and recalculate net amount
        quantityItemCol.setOnEditCommit(event -> {
            SalesInvoiceDetail invoiceDetail = event.getRowValue();
            int newQuantity = event.getNewValue();

            if (newQuantity <= 0) {
                DialogUtils.showErrorMessage("Error", "Quantity must be greater than zero.");
            } else if (newQuantity > invoiceDetail.getAvailableQuantity()) {
                DialogUtils.showErrorMessage(
                        "Error",
                        invoiceDetail.getAvailableQuantity() + " available for " + invoiceDetail.getProduct().getDescription()
                );
            } else {
                invoiceDetail.setQuantity(newQuantity);
                updateAllAmounts(invoiceDetail); // Update all dependent amounts
            }

            itemsTable.requestFocus();
        });

        priceItemCol.setCellValueFactory(cellData -> {
            SalesInvoiceDetail invoiceDetail = cellData.getValue();
            return new SimpleDoubleProperty(invoiceDetail.getUnitPrice()).asObject();
        });
        priceItemCol.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        priceItemCol.setEditable(true); // Make the column editable
        priceItemCol.setOnEditCommit(event -> {
            SalesInvoiceDetail invoiceDetail = event.getRowValue();
            double newUnitPrice = event.getNewValue(); // Get the new unit price from user input
            invoiceDetail.setUnitPrice(newUnitPrice); // Update the unit price
            updateAllAmounts(invoiceDetail);
        });

        grossAmountCol.setCellValueFactory(cellData -> {
            SalesInvoiceDetail invoiceDetail = cellData.getValue();
            double grossAmount = invoiceDetail.getUnitPrice() * invoiceDetail.getQuantity();
            invoiceDetail.setGrossAmount(grossAmount);
            return new SimpleDoubleProperty(grossAmount).asObject();
        });
        ;

        discountItemCol.setCellValueFactory(cellData ->
                new SimpleDoubleProperty(cellData.getValue().getDiscountAmount()).asObject());
        // Calculate net amount (price * quantity)
        netAmountItemCol.setCellValueFactory(cellData -> {
            SalesInvoiceDetail invoiceDetail = cellData.getValue();
            double netAmount = invoiceDetail.getGrossAmount() - invoiceDetail.getDiscountAmount();
            invoiceDetail.setTotalPrice(netAmount);
            return new SimpleDoubleProperty(netAmount).asObject();
        });
        // Set the TableView's items to be the list of SalesInvoiceDetails
        itemsTable.setItems(salesInvoiceDetails);
    }

    private void updateAllAmounts(SalesInvoiceDetail invoiceDetail) {
        try {
            // Calculate gross amount
            double grossAmount = invoiceDetail.getUnitPrice() * invoiceDetail.getQuantity();
            invoiceDetail.setGrossAmount(grossAmount);
            if (invoiceDetail.getProduct().getDiscountType() != null) {
                List<BigDecimal> lineDiscounts = discountDAO.getLineDiscountsByDiscountTypeId(
                        invoiceDetail.getProduct().getDiscountType().getId());
                if (lineDiscounts != null && !lineDiscounts.isEmpty()) {
                    double discount = DiscountCalculator.calculateTotalDiscountAmount(
                            BigDecimal.valueOf(grossAmount), lineDiscounts).doubleValue();
                    invoiceDetail.setDiscountAmount(discount);
                } else {
                    invoiceDetail.setDiscountAmount(0); // No discounts available
                }
            }

            // Calculate net amount
            double netAmount = invoiceDetail.getGrossAmount() - invoiceDetail.getDiscountAmount();
            invoiceDetail.setTotalPrice(netAmount);
            itemsTable.refresh();
            itemsTable.requestFocus();
            updateTotals();

        } catch (Exception e) {
            DialogUtils.showErrorMessage("Error", "An error occurred while updating amounts: " + e.getMessage());
        }
    }

    private void updateTotals() {
        // Initialize total amounts
        BigDecimal totalGrossAmount = BigDecimal.ZERO;
        BigDecimal totalDiscountAmount = BigDecimal.ZERO;
        BigDecimal totalNetAmount = BigDecimal.ZERO;
        BigDecimal totalVatAmount = BigDecimal.ZERO;
        BigDecimal totalNetOfVatAmount;

        // Calculate totals from invoice details
        for (SalesInvoiceDetail detail : salesInvoiceDetails) {
            BigDecimal quantity = BigDecimal.valueOf(detail.getQuantity());
            BigDecimal price = BigDecimal.valueOf(detail.getTotalPrice()); // Total price per item
            BigDecimal discount = BigDecimal.valueOf(detail.getDiscountAmount());

            BigDecimal grossAmount = price; // Assuming totalPrice already accounts for quantity
            BigDecimal discountAmount = discount;
            BigDecimal netAmount = grossAmount.subtract(discountAmount);

            totalGrossAmount = totalGrossAmount.add(grossAmount);
            totalDiscountAmount = totalDiscountAmount.add(discountAmount);
            totalNetAmount = totalNetAmount.add(netAmount);
        }

        // VAT calculation and invoice type handling
        if (salesInvoiceHeader.getInvoiceType().getId() != 3) {
            totalVatAmount = VATCalculator.calculateVat(totalGrossAmount);
            totalNetOfVatAmount = totalNetAmount.add(totalVatAmount);
            salesInvoiceHeader.setReceipt(true);
        } else {
            totalNetOfVatAmount = totalNetAmount;
            salesInvoiceHeader.setReceipt(false);
        }

        // Update the UI with formatted values
        grossAmount.setText(formatAmount(totalGrossAmount));
        discountAmount.setText(formatAmount(totalDiscountAmount));
        netAmount.setText(formatAmount(totalNetAmount));
        vatAmount.setText(formatAmount(totalVatAmount));
        netOfVatAmount.setText(formatAmount(totalNetOfVatAmount));

        // Update SalesInvoiceHeader with calculated totals
        salesInvoiceHeader.setGrossAmount(totalGrossAmount.doubleValue());
        salesInvoiceHeader.setTotalAmount(totalNetOfVatAmount.doubleValue());
        salesInvoiceHeader.setVatAmount(totalVatAmount.doubleValue());
        salesInvoiceHeader.setDiscountAmount(totalDiscountAmount.doubleValue());
        salesInvoiceHeader.setNetAmount(totalNetAmount.doubleValue());
    }

    // Helper method to format monetary amounts
    private String formatAmount(BigDecimal amount) {
        return String.format("%.2f", amount);
    }

    @Setter
    SalesInvoicesController salesInvoicesController;

    public void initData(SalesInvoiceHeader salesInvoiceHeader) {
        this.salesInvoiceHeader = salesInvoiceHeader;
        this.salesInvoiceDetails = salesInvoiceDAO.getSalesInvoiceDetails(salesInvoiceHeader);
        this.selectedCustomer = salesInvoiceHeader.getCustomer();
        this.selectedSalesman = salesInvoiceHeader.getSalesman();
        this.invoiceDate.setValue(salesInvoiceHeader.getInvoiceDate().toLocalDateTime().toLocalDate());
        this.dispatchDate.setValue(salesInvoiceHeader.getDispatchDate().toLocalDateTime().toLocalDate());
        this.dueDate.setValue(salesInvoiceHeader.getDueDate().toLocalDateTime().toLocalDate());
        this.invoiceNoTextField.setText(salesInvoiceHeader.getInvoiceNo());
        this.remarks.setText(salesInvoiceHeader.getRemarks());
        this.customerTextField.setText(salesInvoiceHeader.getCustomer().getStoreName());
        this.salesmanTextField.setText(salesInvoiceHeader.getSalesman().getSalesmanName());
        this.salesType.setValue(salesInvoiceHeader.getSalesType() == 1 ? "BOOKING" : salesInvoiceHeader.getSalesType() == 2 ? "DISTRIBUTOR" : "VAN SALES");
        this.receiptType.setValue(salesInvoiceHeader.getInvoiceType().getName());
        this.salesmanLocationTextField.setText(salesInvoiceHeader.getSalesman().getSalesmanCode());
        this.customerCodeTextField.setText(salesInvoiceHeader.getCustomer().getCustomerCode());
        this.invoiceNoTextField.setText(salesInvoiceHeader.getInvoiceNo());
        this.grossAmount.setText(String.format("%.2f", salesInvoiceHeader.getGrossAmount()));
        this.vatAmount.setText(String.format("%.2f", salesInvoiceHeader.getVatAmount()));
        this.discountAmount.setText(String.format("%.2f", salesInvoiceHeader.getDiscountAmount()));
        this.netAmount.setText(String.format("%.2f", salesInvoiceHeader.getNetAmount()));
        this.netOfVatAmount.setText(String.format("%.2f", salesInvoiceHeader.getTotalAmount()));
        this.invoiceNoTextField.setText(salesInvoiceHeader.getInvoiceNo());
        this.itemsTable.setItems(salesInvoiceDetails);
        this.transactionStatus.setText(salesInvoiceHeader.getTransactionStatus());
        this.paymentStatus.setText(salesInvoiceHeader.getPaymentStatus());

        for (SalesInvoiceDetail detail : salesInvoiceDetails) {
            detail.setAvailableQuantity(inventoryDAO.getQuantityByBranchAndProductID(salesInvoiceHeader.getSalesman().getGoodBranchCode(), detail.getProduct().getProductId()));
        }

        if (salesInvoiceHeader.isPosted()) {
            confirmButton.setDisable(true);
            dispatchButton.setDisable(true);
        }

        if (salesInvoiceHeader.isDispatched()) {
            dispatchButton.setText("Undispatch");
            dispatchButton.setOnMouseClicked(mouseEvent -> undispatchInvoice());
        } else {
            dispatchButton.setText("Dispatch");
            dispatchButton.setOnMouseClicked(mouseEvent -> dispatchInvoice());
        }
        confirmButton.setText("Update");
        confirmButton.setOnMouseClicked(mouseEvent -> updateInvoice());
    }


    InventoryDAO inventoryDAO = new InventoryDAO();

    List<Inventory> inventoryList = new ArrayList<>();

    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    private void undispatchInvoice() {
        // Update sales invoice header for undispatch
        salesInvoiceHeader.setDispatched(false);
        salesInvoiceHeader.setTransactionStatus("For Dispatch");
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false); // Disable auto-commit to manage transaction manually
            // Update the sales invoice in the database
            boolean result = salesInvoiceDAO.createSalesInvoiceWithDetails(salesInvoiceHeader, salesInvoiceDetails, connection);
            if (!result) {
                connection.rollback(); // Rollback if invoice update fails
                DialogUtils.showErrorMessage("Error", "Failed to undispatch the invoice.");
                return;
            }

            // Create inventory updates to revert changes
            inventoryList.clear();
            for (SalesInvoiceDetail detail : salesInvoiceDetails) {
                Inventory inventory = new Inventory();
                inventory.setQuantity(detail.getQuantity()); // Re-add the dispatched quantities
                inventory.setProductId(detail.getProduct().getProductId());
                inventory.setBranchId(salesInvoiceHeader.getSalesman().getGoodBranchCode());
                inventoryList.add(inventory);
            }

            // Update inventory in bulk to reverse changes
            boolean result2 = inventoryDAO.updateInventoryBulk(inventoryList, connection);
            if (!result2) {
                connection.rollback(); // Rollback if inventory update fails
                DialogUtils.showErrorMessage("Error", "Failed to revert inventory changes.");
                return;
            }

            // Commit the transaction if everything is successful
            connection.commit();
            DialogUtils.showCompletionDialog("Success", "Invoice status reverted to '" + salesInvoiceHeader.getTransactionStatus() + "' successfully.");
            dispatchButton.setDisable(false); // Enable the dispatch button for re-dispatching
            confirmButton.setDisable(false);
        } catch (SQLException e) {
            // Handle SQL exceptions and rollback the transaction if an exception occurs
            try {
                Connection connection = dataSource.getConnection();
                connection.rollback(); // Rollback in case of an exception
            } catch (SQLException rollbackException) {
                rollbackException.printStackTrace(); // Log rollback failure
            }
            DialogUtils.showErrorMessage("Error", "An error occurred while reverting the dispatch: " + e.getMessage());
        }

        // Update UI to reflect reverted changes
        transactionStatus.setText(salesInvoiceHeader.getTransactionStatus());
        paymentStatus.setText(salesInvoiceHeader.getPaymentStatus());
        dispatchButton.setText("Dispatch");
        salesInvoicesController.setUpTableData();
        initData(salesInvoiceHeader);
    }


    private void dispatchInvoice() {
        salesInvoiceHeader.setDispatched(true);
        salesInvoiceHeader.setTransactionStatus("Dispatched");
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);  // Disable auto-commit to manually control the transaction
            salesInvoiceHeader.setDispatchDate(Timestamp.valueOf(dispatchDate.getValue().atStartOfDay().atZone(ZoneId.systemDefault()).toLocalDateTime()));            boolean result = salesInvoiceDAO.createSalesInvoiceWithDetails(salesInvoiceHeader, salesInvoiceDetails, connection);
            if (!result) {
                connection.rollback();  // Rollback if creating invoice details fails
                DialogUtils.showErrorMessage("Error", "Failed to dispatch the invoice.");
                return;
            }

            // Create inventory updates
            inventoryList.clear();
            for (SalesInvoiceDetail detail : salesInvoiceDetails) {
                Inventory inventory = new Inventory();
                inventory.setQuantity(-detail.getQuantity());
                inventory.setProductId(detail.getProduct().getProductId());
                inventory.setBranchId(salesInvoiceHeader.getSalesman().getGoodBranchCode());
                inventoryList.add(inventory);
            }

            // Update inventory in bulk
            boolean result2 = inventoryDAO.updateInventoryBulk(inventoryList, connection);
            if (!result2) {
                connection.rollback();  // Rollback if updating inventory fails
                DialogUtils.showErrorMessage("Error", "Failed to update inventory.");
                return;
            }

            // Commit the transaction if everything is successful
            connection.commit();
            DialogUtils.showCompletionDialog("Success", "Invoice " + salesInvoiceHeader.getTransactionStatus() + " successful.");
            dispatchButton.setDisable(true);
        } catch (SQLException e) {
            // Handle SQL exceptions and perform rollback if an exception occurs
            try {
                Connection connection = dataSource.getConnection();
                connection.rollback();  // Rollback in case of an exception
            } catch (SQLException rollbackException) {
                rollbackException.printStackTrace();  // Log rollback failure if any
            }
            DialogUtils.showErrorMessage("Error", "An error occurred while dispatching the invoice: " + e.getMessage());
        }
        transactionStatus.setText(salesInvoiceHeader.getTransactionStatus());
        paymentStatus.setText(salesInvoiceHeader.getPaymentStatus());
        dispatchButton.setText("Un Dispatch");
        salesInvoicesController.setUpTableData();
        initData(salesInvoiceHeader);
    }
}
