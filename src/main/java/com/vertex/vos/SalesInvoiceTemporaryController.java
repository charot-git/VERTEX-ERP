package com.vertex.vos;

import com.vertex.vos.DAO.SalesInvoiceDAO;
import com.vertex.vos.DAO.SalesInvoiceTypeDAO;
import com.vertex.vos.DAO.SalesReturnDAO;
import com.vertex.vos.Objects.*;
import com.vertex.vos.Utilities.*;
import com.zaxxer.hikari.HikariDataSource;
import javafx.application.ConditionalFeature;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
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
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;
import lombok.Getter;
import lombok.Setter;
import org.controlsfx.control.textfield.TextFields;

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
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class SalesInvoiceTemporaryController implements Initializable {

    public TextField invoiceNoTextField;
    public Label salesNo;
    @Getter
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
    public Tab returnTab;
    public TableView<SalesReturnDetail> returnsTable;
    public TableColumn<SalesReturnDetail, String> productCodeReturnCol;
    public TableColumn<SalesReturnDetail, String> descriptionReturnCol;
    public TableColumn<SalesReturnDetail, String> unitReturnCol;
    public TableColumn<SalesReturnDetail, Integer> quantityReturnCol;
    public TableColumn<SalesReturnDetail, SalesReturnType> returnTypeCol;
    public TableColumn<SalesReturnDetail, Double> priceReturnCol;
    public TableColumn<SalesReturnDetail, Double> discountReturnCol;
    public TableColumn<SalesReturnDetail, Double> netAmountReturnCol;
    public TableColumn<SalesInvoiceDetail, String> discountTypeCol;
    public Button deleteButton;
    public VBox createSalesReturn;
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
    private ComboBox<SalesInvoiceType> receiptType;

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

    SalesInvoiceHeader salesInvoiceHeader;

    ObservableList<SalesInvoiceDetail> salesInvoiceDetails = FXCollections.observableArrayList(); // List to hold sales invoice details>
    ObservableList<SalesInvoiceDetail> deletedSalesInvoiceDetails = FXCollections.observableArrayList();
    ObservableList<SalesReturnDetail> salesReturnDetails = FXCollections.observableArrayList(); // List to hold sales invoice details>

    int soNo = 0;

    @Setter
    Stage stage;

    public void createNewSalesEntry(Stage stage) {
        salesInvoiceHeader = new SalesInvoiceHeader();
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

        salesInvoiceHeader.setInvoiceType(salesInvoiceTypeList.getFirst());

        customers.setAll(customerDAO.getAllActiveCustomers());
        salesmen.setAll(salesmanDAO.getAllSalesmen());
        customerTextField.requestFocus();
        TextFields.bindAutoCompletion(customerTextField, customers.stream().map(Customer::getStoreName).toList());
        customerTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            selectedCustomer = customers.stream()
                    .filter(customer -> customer.getStoreName().equals(newValue))
                    .findFirst()
                    .orElse(null);

            if (selectedCustomer != null) {
                // Set customer details
                customerCodeTextField.setText(selectedCustomer.getCustomerCode());
                salesInvoiceHeader.setCustomer(selectedCustomer);
            }
        });
        ;

        TextFields.bindAutoCompletion(salesmanTextField, salesmen.stream().map(Salesman::getSalesmanName).toList());

        salesmanTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            selectedSalesman = salesmen.stream()
                    .filter(salesman -> salesman.getSalesmanName().equals(newValue))
                    .findFirst()
                    .orElse(null);

            if (selectedSalesman != null) {
                // Set Salesman details
                salesInvoiceHeader.setOrderId(selectedSalesman.getSalesmanCode() + "-" + salesOrderDAO.getNextSoNo());
                salesNo.setText(salesInvoiceHeader.getOrderId());
                salesmanLocationTextField.setText(selectedSalesman.getSalesmanCode());

                if (selectedSalesman.getPriceType() != null) {
                    priceType.setValue(selectedSalesman.getPriceType());
                }

                if (selectedSalesman.getOperation() != -1) {
                    switch (selectedSalesman.getOperation()) {
                        case 1 -> salesType.getSelectionModel().select("BOOKING");
                        case 2 -> salesType.getSelectionModel().select("DISTRIBUTOR");
                        case 3 -> salesType.getSelectionModel().select("VAN SALES");
                    }
                }

                salesInvoiceHeader.setSalesman(selectedSalesman);
                salesInvoiceHeader.setPriceType(selectedSalesman.getPriceType());
            }
        });

        invoiceNoTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            salesInvoiceHeader.setInvoiceNo(newValue);
        });
        createSalesReturn.setOnMouseClicked(mouseEvent -> createSalesReturnForSalesTransaction());

        confirmButton.setOnMouseClicked(mouseEvent -> createSalesInvoice());
    }

    private void createSalesReturnForSalesTransaction() {
        if (selectedCustomer == null) {
            DialogUtils.showErrorMessage("Missing Customer", "Please select a customer.");
            customerTextField.requestFocus();
            return;
        }
        if (selectedSalesman == null) {
            DialogUtils.showErrorMessage("Missing Salesman", "Please select a salesman.");
            salesmanTextField.requestFocus();
            return;
        }
        if (salesInvoiceHeader == null) {
            DialogUtils.showErrorMessage("Missing Invoice", "Sales invoice data is missing.");
            return;
        }
        if (salesInvoiceHeader.getInvoiceNo() == null || salesInvoiceHeader.getInvoiceNo().isEmpty()) {
            DialogUtils.showErrorMessage("Missing Invoice Number", "Invoice number cannot be empty.");
            invoiceNoTextField.requestFocus();
            return;
        }
        if (salesInvoiceHeader.getOrderId() == null || salesInvoiceHeader.getOrderId().isEmpty()) {
            DialogUtils.showErrorMessage("Missing Order ID", "Order ID cannot be empty.");
            salesNo.requestFocus();
            return;
        }

        openSalesReturnCreation();
    }


    private boolean isSalesReturnCreationOpen = false;

    private void openSalesReturnCreation() {
        if (isSalesReturnCreationOpen) {
            return;
        }
        isSalesReturnCreationOpen = true;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("SalesReturnForm.fxml"));
            Parent root = loader.load();
            SalesReturnFormController salesReturnFormController = loader.getController();
            Stage returnStage = new Stage();
            salesReturnFormController.createNewSalesReturn(returnStage, salesReturnDAO.generateSalesReturnNo(), null);
            salesReturnFormController.setInitialDataForSalesInvoice(selectedSalesman, selectedCustomer, salesInvoiceHeader, invoiceDate.getValue(), this);
            returnStage.setTitle("Create Sales Return");
            returnStage.setScene(new Scene(root));
            returnStage.show();
            returnStage.setOnCloseRequest(event -> isSalesReturnCreationOpen = false);
        } catch (IOException e) {
            DialogUtils.showErrorMessage("Error", "Unable to open sales return creation.");
            e.printStackTrace();
        }
    }

    private void isTouchScreen(Stage stage, Salesman salesman) {
        if (Platform.isSupported(ConditionalFeature.INPUT_TOUCH) && Platform.isSupported(ConditionalFeature.INPUT_METHOD)) {
            openProductSelectionTouchScreen(stage, salesman);
        } else {
            openProductSelectionKeyboard(stage, salesman);
        }
    }

    private void openProductSelectionKeyboard(Stage stage, Salesman salesman) {
        if (salesman == null) {
            return;
        }

        if (selectedCustomer == null) {
            return;
        }
        if (productSelectionStage == null || !productSelectionStage.isShowing()) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("SalesInvoiceProductSelectionKeyboard.fxml"));
                Parent root = loader.load();
                SalesInvoiceProductSelectionKeyboardController controller = loader.getController();

                productSelectionStage = new Stage();
                productSelectionStage.setTitle("Product Selection");
                controller.setProductSelectionStage(productSelectionStage);
                controller.setSalesInvoiceTemporaryController(this);
                controller.setPriceType(priceType.getValue());
                controller.setBranchCode(salesman.getGoodBranchCode());
                controller.setSelectedCustomer(selectedCustomer);
                controller.setOrderId(salesInvoiceHeader.getOrderId());
                controller.processProductSelection();

                productSelectionStage.setScene(new Scene(root));
                productSelectionStage.setOnCloseRequest(event -> {
                    if (salesInvoiceHeader.getTransactionStatus().equals("Encoding")) {
                        productSelectionStage.hide();
                    }
                });
                productSelectionStage.show();

                stage.setOnCloseRequest(event -> productSelectionStage.close());

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

    private static final Logger LOGGER = Logger.getLogger(SalesInvoiceTemporaryController.class.getName());

    private void updateInvoice() {
        Connection connection = null;
        try {
            LOGGER.info("Starting updateInvoice process.");

            // Ensure sales invoice header exists before proceeding
            if (salesInvoiceHeader == null) {
                LOGGER.warning("Sales invoice data is missing.");
                DialogUtils.showErrorMessage("Error", "Sales invoice data is missing.");
                return;
            }

            // Get a connection from the data source
            connection = dataSource.getConnection();
            LOGGER.info("Database connection established.");

            boolean deleteDetails = true;

            // Attempt deletion if there are deleted items
            if (deletedSalesInvoiceDetails != null && !deletedSalesInvoiceDetails.isEmpty()) {
                LOGGER.info("Attempting to remove deleted sales invoice details.");
                deleteDetails = salesInvoiceDAO.removeSalesInvoiceDetails(deletedSalesInvoiceDetails, connection);
                LOGGER.info("Deleted details status: " + deleteDetails);
            }

            // Update Sales Invoice and details
            LOGGER.info("Updating sales invoice and details.");
            salesInvoiceHeader = salesInvoiceDAO.createSalesInvoiceWithDetails(salesInvoiceHeader, salesInvoiceDetails, connection);
            LOGGER.info("Sales invoice updated with ID: " + salesInvoiceHeader.getInvoiceId());

            // Link Sales Return if applicable
            boolean linkSuccess = true;
            if (salesReturn != null) {
                LOGGER.info("Linking sales return.");
                linkSuccess = salesInvoiceDAO.linkSalesInvoiceSalesReturn(salesInvoiceHeader, salesReturn, connection);
                LOGGER.info("Sales return link status: " + linkSuccess);
            }

            // Validate all operations before finishing
            if (salesInvoiceHeader.getInvoiceId() > 0 && deleteDetails && linkSuccess) {
                LOGGER.info("Sales invoice update successful.");
                DialogUtils.showCompletionDialog("Sales Invoice Updated", "Success! Sales invoice updated successfully.");
            } else {
                throw new SQLException("Unexpected failure in sales invoice update.");
            }

        } catch (SQLException e) {
            LOGGER.severe("Error while updating sales invoice: " + e.getMessage());
            DialogUtils.showErrorMessage("Database Error", "Error while updating sales invoice: " + e.getMessage());
        } finally {
            // Ensure the connection is closed
            if (connection != null) {
                try {
                    connection.close();
                    LOGGER.info("Database connection closed.");
                } catch (SQLException closeEx) {
                    LOGGER.warning("Failed to close connection: " + closeEx.getMessage());
                }
            }
        }

        salesInvoicesController.reloadSalesInvoices();
        LOGGER.info("Sales invoices reloaded.");
    }


    private void createSalesInvoice() {
        if (selectedCustomer == null) {
            customerTextField.setTooltip(new Tooltip("Please select a customer"));
            customerTextField.requestFocus();
            DialogUtils.showErrorMessage("Missing Data", "Please select a customer.");
            return;
        }
        if (selectedSalesman == null) {
            salesmanTextField.setTooltip(new Tooltip("Please select a salesman"));
            salesmanTextField.requestFocus();
            DialogUtils.showErrorMessage("Missing Data", "Please select a salesman.");
            return;
        }
        if (invoiceDate.getValue() == null) {
            invoiceDate.setTooltip(new Tooltip("Please select an invoice date"));
            invoiceDate.requestFocus();
            DialogUtils.showErrorMessage("Missing Data", "Please select an invoice date.");
            return;
        }
        if (dispatchDate.getValue() == null) {
            dispatchDate.setTooltip(new Tooltip("Please select a dispatch date"));
            dispatchDate.requestFocus();
            DialogUtils.showErrorMessage("Missing Data", "Please select a dispatch date.");
            return;
        }
        if (dueDate.getValue() == null) {
            dueDate.setTooltip(new Tooltip("Please select a due date"));
            dueDate.requestFocus();
            DialogUtils.showErrorMessage("Missing Data", "Please select a due date.");
            return;
        }

        String invoiceNumber = invoiceNoTextField.getText().trim();
        if (invoiceNumber.isEmpty()) {
            invoiceNoTextField.setTooltip(new Tooltip("Invoice number is required"));
            invoiceNoTextField.requestFocus();
            DialogUtils.showErrorMessage("Missing Invoice Number", "Invoice number is required.");
            return;
        }

        salesInvoiceHeader.setSalesman(selectedSalesman);
        salesInvoiceHeader.setCustomer(selectedCustomer);
        salesInvoiceHeader.setTransactionStatus("Encoded");
        salesInvoiceHeader.setPaymentStatus("Unpaid");
        salesInvoiceHeader.setCreatedBy(UserSession.getInstance().getUserId());

        salesInvoiceHeader.setInvoiceDate(Timestamp.valueOf(invoiceDate.getValue().atStartOfDay()));
        salesInvoiceHeader.setDispatchDate(Timestamp.valueOf(dispatchDate.getValue().atStartOfDay()));
        salesInvoiceHeader.setDueDate(Timestamp.valueOf(dueDate.getValue().atStartOfDay()));
        salesInvoiceHeader.setInvoiceType(receiptType.getValue());
        salesInvoiceHeader.setModifiedBy(UserSession.getInstance().getUserId());

        salesInvoiceHeader.setPaymentTerms(selectedCustomer.getPaymentTerm());
        salesInvoiceHeader.setInvoiceNo(invoiceNumber);
        salesInvoiceHeader.setModifiedDate(new Timestamp(System.currentTimeMillis()));
        salesInvoiceHeader.setPostedBy(UserSession.getInstance().getUserId());
        salesInvoiceHeader.setPostedDate(new Timestamp(System.currentTimeMillis()));
        salesInvoiceHeader.setRemarks(remarks.getText() != null ? remarks.getText().trim() : "");
        salesInvoiceHeader.setPriceType(priceType.getValue());

        salesInvoiceHeader.setPosted(false);
        salesInvoiceHeader.setDispatched(salesInvoiceHeader.getSalesType() == 3);

        updateTotals();

        try (Connection connection = dataSource.getConnection()) {
            if (salesInvoiceDAO.invoiceExists(invoiceNumber, connection)) {
                DialogUtils.showErrorMessage("Duplicate Invoice", "Invoice number already exists.");
                return;
            }

            if (salesInvoiceHeader.getSalesType() == 3) {
                List<Inventory> inventoryList = inventoriesForDispatch();

                boolean inventoryUpdated = inventoryDAO.updateInventoryBulk(inventoryList, connection);
                if (!inventoryUpdated) {
                    DialogUtils.showErrorMessage("Error", "Failed to update inventory.");
                    return;
                }
                salesInvoiceHeader.setTransactionStatus("Dispatched");
            }

            salesInvoiceHeader = salesInvoiceDAO.createSalesInvoiceWithDetails(salesInvoiceHeader, salesInvoiceDetails, connection);
            if (salesInvoiceHeader == null || salesInvoiceHeader.getInvoiceId() <= 0) {
                DialogUtils.showErrorMessage("Sales Invoice Creation Failed", "Failed to create sales invoice.");
                return;
            }

            if (salesReturn != null) {
                try (Connection linkConnection = dataSource.getConnection()) {
                    boolean linkSuccess = salesInvoiceDAO.linkSalesInvoiceSalesReturn(salesInvoiceHeader, salesReturn, linkConnection);
                    if (linkSuccess) {
                        DialogUtils.showCompletionDialog("Sales Return Linked", "Sales return successfully linked to invoice.");
                    } else {
                        DialogUtils.showErrorMessage("Sales Return Linking Failed", "Failed to link sales return.");
                    }
                } catch (SQLException linkException) {
                    DialogUtils.showErrorMessage("Database Error", "An error occurred while linking the sales return.");
                }
            }

            if (salesInvoicesController != null) {
                salesInvoicesController.reloadSalesInvoices();
                salesInvoicesController.salesInvoiceTable.getSelectionModel().select(salesInvoiceHeader);
            }
            stage.close();
        } catch (SQLException e) {
            DialogUtils.showErrorMessage("Database Error", "An error occurred while saving the sales invoice.");
            e.printStackTrace();
        }
    }

    private List<Inventory> inventoriesForDispatch() {
        List<Inventory> inventoryList = new ArrayList<>();
        for (SalesInvoiceDetail detail : salesInvoiceDetails) {
            Inventory inventory = new Inventory();
            inventory.setQuantity(-detail.getQuantity()); // Deduct quantity
            inventory.setProductId(detail.getProduct().getProductId());
            inventory.setBranchId(salesInvoiceHeader.getSalesman().getGoodBranchCode());
            inventoryList.add(inventory);
        }
        return inventoryList;
    }


    SalesInvoiceDAO salesInvoiceDAO = new SalesInvoiceDAO();

    private void openProductSelectionTouchScreen(Stage parentStage, Salesman salesman) {
        if (salesman == null) {
            return;
        }

        if (selectedCustomer == null) {
            return;
        }
        if (productSelectionStage == null || !productSelectionStage.isShowing()) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("SalesInvoiceProductSelectionTouchScreen.fxml"));
                Parent root = loader.load();
                SalesInvoiceProductSelectionTouchscreenController controller = loader.getController();

                productSelectionStage = new Stage();
                productSelectionStage.setTitle("Product Selection");
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
        System.out.println("check");
        selectedProduct.setOrderId(salesInvoiceHeader.getOrderId());
        salesInvoiceDetails.add(selectedProduct);
        updateTotals();
    }


    DiscountDAO discountDAO = new DiscountDAO();


    ObservableList<SalesInvoiceType> salesInvoiceTypeList = salesInvoiceTypeDAO.getSalesInvoiceTypes();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        addProductToItems.setOnMouseClicked(mouseEvent -> checkIfValidForProductSelection());

        TableViewFormatter.formatTableView(itemsTable);
        TableViewFormatter.formatTableView(returnsTable);
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

        receiptType.setItems(salesInvoiceTypeList);


        // Set up price types in the combo box
        priceType.getItems().addAll("A", "B", "C", "D", "E");
        priceType.setValue("A"); // Default price type

        //receiptType display name only
        receiptType.setConverter(new StringConverter<>() {
            @Override
            public String toString(SalesInvoiceType salesInvoiceType) {
                return (salesInvoiceType != null) ? salesInvoiceType.getName() : "";
            }

            @Override
            public SalesInvoiceType fromString(String string) {
                return salesInvoiceTypeList.stream()
                        .filter(type -> type.getName().equals(string))
                        .findFirst()
                        .orElse(null);
            }
        });

        receiptType.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                salesInvoiceTypeList.stream().filter(type -> false).findFirst().ifPresent(selectedType -> salesInvoiceHeader.setInvoiceType(selectedType));
                updateTotals();
            }
        });


        // Configure TableView columns
        productCodeItemCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduct().getProductCode()));
        descriptionItemCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduct().getDescription()));
        unitItemCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduct().getUnitOfMeasurementString()));
        discountTypeCol.setCellValueFactory(cellData -> {
            DiscountType discountType = cellData.getValue().getDiscountType();
            String typeName = discountType != null ? discountType.getTypeName() : "No Discount";
            return new SimpleStringProperty(typeName);
        });
        List<DiscountType> discountTypes = discountDAO.getAllDiscountTypes();

        List<String> discountTypeNames = discountTypes.stream()
                .map(DiscountType::getTypeName)
                .collect(Collectors.toList());

        discountTypeCol.setCellFactory(ComboBoxTableCell.forTableColumn(FXCollections.observableArrayList(discountTypeNames)));
        discountTypeCol.setOnEditCommit(event -> {
            SalesInvoiceDetail invoiceDetail = event.getRowValue();
            String newDiscountTypeName = event.getNewValue();

            // Find the corresponding DiscountType object
            DiscountType newDiscountType = discountTypes.stream()
                    .filter(dt -> dt.getTypeName().equals(newDiscountTypeName))
                    .findFirst()
                    .orElse(null); // Handle the case where no match is found

            if (newDiscountType != null) {
                invoiceDetail.getProduct().setDiscountType(newDiscountType);
                invoiceDetail.setDiscountType(newDiscountType);
                updateAllAmounts(invoiceDetail); // Update all dependent amounts
            } else {
                DialogUtils.showErrorMessage("Error", "Invalid discount type selected.");
            }
        });

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

        discountItemCol.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getDiscountAmount()).asObject());

        // Calculate net amount (price * quantity)
        netAmountItemCol.setCellValueFactory(cellData -> {
            SalesInvoiceDetail invoiceDetail = cellData.getValue();
            double netAmount = invoiceDetail.getGrossAmount() - invoiceDetail.getDiscountAmount();
            invoiceDetail.setTotalPrice(netAmount);
            return new SimpleDoubleProperty(netAmount).asObject();
        });
        itemsTable.setItems(salesInvoiceDetails);

        initializeMappings();

        productCodeReturnCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduct().getProductCode()));
        descriptionReturnCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduct().getDescription()));
        unitReturnCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduct().getUnitOfMeasurementString()));
        quantityReturnCol.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getQuantity()).asObject());
        priceReturnCol.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getUnitPrice()).asObject());
        returnTypeCol.setCellValueFactory(cellData -> {
            SalesReturnType returnType = cellData.getValue().getSalesReturnType();
            return new SimpleObjectProperty<>(returnType);
        });
        discountReturnCol.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getDiscountAmount()).asObject());
        netAmountReturnCol.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getTotalAmount()).asObject());

        returnsTable.setItems(salesReturnDetails);
        addProductToReturns.setOnMouseClicked(event -> {
            if (selectedSalesman != null && selectedCustomer != null && salesInvoiceHeader.getOrderId() != null && salesInvoiceHeader.getInvoiceNo() != null) {
                openSalesReturnSelection(selectedSalesman, selectedCustomer, salesInvoiceHeader);
            }
        });

        itemsTable.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.DELETE) {
                SalesInvoiceDetail invoiceDetail = itemsTable.getSelectionModel().getSelectedItem();
                if (invoiceDetail != null) {
                    salesInvoiceDetails.remove(invoiceDetail);
                    deletedSalesInvoiceDetails.add(invoiceDetail);
                }
            }
        });
    }

    private void checkIfValidForProductSelection() {
        if (salesInvoiceHeader.getCustomer() == null || salesInvoiceHeader.getSalesman() == null) {
            DialogUtils.showErrorMessage("Error", "Please select a customer and salesman first.");
        } else {
            isTouchScreen(stage, salesInvoiceHeader.getSalesman());
        }
    }

    private Map<Integer, String> typeIdToNameMap;

    private void initializeMappings() {
        // Fetch mappings from the database
        typeIdToNameMap = SalesReturnDAO.getTypeIdToNameMap();
        Map<String, Integer> typeNameToIdMap = SalesReturnDAO.getTypeNameToIdMap();
        ObservableList<String> salesReturnTypes = FXCollections.observableArrayList(typeIdToNameMap.values());
    }

    SalesReturn salesReturn = null;

    private void openSalesReturnSelection(Salesman selectedSalesman, Customer selectedCustomer, SalesInvoiceHeader salesInvoiceHeader) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("SalesReturns.fxml"));
            Parent root = loader.load();
            SalesReturnsListController controller = loader.getController();
            controller.loadSalesReturnForSelection(selectedSalesman, selectedCustomer, salesInvoiceHeader, this);
            Scene scene = new Scene(root);
            Stage returnStage = new Stage();
            returnStage.setScene(scene);
            controller.setStage(returnStage);
            returnStage.initModality(Modality.APPLICATION_MODAL);
            returnStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void updateAllAmounts(SalesInvoiceDetail invoiceDetail) {
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

        for (SalesReturnDetail detail : salesReturnDetails) {
            totalDiscountAmount = totalDiscountAmount.subtract(BigDecimal.valueOf(detail.getDiscountAmount()));
            totalGrossAmount = totalGrossAmount.subtract(BigDecimal.valueOf(detail.getTotalAmount()));
            totalNetAmount = totalNetAmount.subtract(BigDecimal.valueOf(detail.getTotalAmount()));
        }
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

    SalesReturnDAO salesReturnDAO = new SalesReturnDAO();

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
        this.receiptType.setValue(salesInvoiceHeader.getInvoiceType());
        this.salesmanLocationTextField.setText(salesInvoiceHeader.getSalesman().getSalesmanCode());
        this.customerCodeTextField.setText(salesInvoiceHeader.getCustomer().getCustomerCode());
        this.invoiceNoTextField.setText(salesInvoiceHeader.getInvoiceNo());
        this.grossAmount.setText(String.format("%.2f", salesInvoiceHeader.getGrossAmount()));
        this.vatAmount.setText(String.format("%.2f", salesInvoiceHeader.getVatAmount()));
        this.priceType.setValue(String.valueOf(salesInvoiceHeader.getPriceType()));
        this.discountAmount.setText(String.format("%.2f", salesInvoiceHeader.getDiscountAmount()));
        this.netAmount.setText(String.format("%.2f", salesInvoiceHeader.getNetAmount()));
        this.netOfVatAmount.setText(String.format("%.2f", salesInvoiceHeader.getTotalAmount()));
        this.invoiceNoTextField.setText(salesInvoiceHeader.getInvoiceNo());
        this.itemsTable.setItems(salesInvoiceDetails);
        this.transactionStatus.setText(salesInvoiceHeader.getTransactionStatus());
        this.paymentStatus.setText(salesInvoiceHeader.getPaymentStatus());


        salesReturn = salesReturnDAO.getLinkedSalesReturn(salesInvoiceHeader.getInvoiceId());

        if (salesReturn != null) {
            returnTab.setText("Returns (" + salesReturn.getReturnNumber() + ")");
            loadSalesReturnDetails();
            deleteButton.setDisable(true);
        }

        try {
            for (SalesInvoiceDetail detail : salesInvoiceDetails) {
                detail.setAvailableQuantity(
                        inventoryDAO.getQuantityByBranchAndProductID(
                                salesInvoiceHeader.getSalesman().getGoodBranchCode(),
                                detail.getProduct().getProductId()
                        )
                );
            }
        } catch (Exception e) {
            LOGGER.warning("Failed to load available quantities: " + e.getMessage());
        }

        if (salesInvoiceHeader.isPosted()) {
            confirmButton.setDisable(true);
            dispatchButton.setDisable(true);
            deleteButton.setDisable(true);
        }

        if (salesInvoiceHeader.isDispatched()) {
            dispatchButton.setText("Undispatch");
            dispatchButton.setOnMouseClicked(mouseEvent -> undispatchInvoice());
            deleteButton.setDisable(true);
        } else {
            dispatchButton.setText("Dispatch");
            dispatchButton.setOnMouseClicked(mouseEvent -> dispatchInvoice());
        }


        confirmButton.setText("Update");
        confirmButton.setOnMouseClicked(mouseEvent -> updateInvoice());

        deleteButton.setOnAction(event -> deleteInvoice());
    }

    private void deleteInvoice() {
        ConfirmationAlert confirmationAlert = new ConfirmationAlert("Delete Invoice", "Are you sure you want to delete this invoice?", salesInvoiceHeader.getInvoiceNo(), true);
        boolean confirmed = confirmationAlert.showAndWait();
        if (confirmed) {
            boolean result = salesInvoiceDAO.deleteSalesInvoice(salesInvoiceHeader);
            if (result) {
                salesInvoicesController.reloadSalesInvoices();
                stage.close();
                DialogUtils.showCompletionDialog("Success", "Invoice deleted successfully.");
            } else {
                DialogUtils.showErrorMessage("Error", "Failed to delete the invoice.");
            }
        }
    }


    InventoryDAO inventoryDAO = new InventoryDAO();

    List<Inventory> inventoryList = new ArrayList<>();

    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    private void undispatchInvoice() {
        salesInvoiceHeader.setDispatched(false);
        salesInvoiceHeader.setTransactionStatus("For Dispatch");
        try (Connection connection = dataSource.getConnection()) {
            salesInvoiceHeader = salesInvoiceDAO.createSalesInvoiceWithDetails(salesInvoiceHeader, salesInvoiceDetails, connection);
            if (salesInvoiceHeader == null) {
                DialogUtils.showErrorMessage("Error", "Failed to undispatch the invoice.");
                return;
            }
            inventoryList.clear();
            for (SalesInvoiceDetail detail : salesInvoiceDetails) {
                Inventory inventory = new Inventory();
                inventory.setQuantity(detail.getQuantity());
                inventory.setProductId(detail.getProduct().getProductId());
                inventory.setBranchId(salesInvoiceHeader.getSalesman().getGoodBranchCode());
                inventoryList.add(inventory);
            }
            boolean inventoryUpdated = inventoryDAO.updateInventoryBulk(inventoryList, connection);
            if (!inventoryUpdated) {
                DialogUtils.showErrorMessage("Error", "Failed to revert inventory changes.");
                return;
            }
            DialogUtils.showCompletionDialog("Success", "Invoice status reverted to '" + salesInvoiceHeader.getTransactionStatus() + "' successfully.");
            transactionStatus.setText(salesInvoiceHeader.getTransactionStatus());
            paymentStatus.setText(salesInvoiceHeader.getPaymentStatus());
            dispatchButton.setText("Dispatch");
            dispatchButton.setDisable(false);
            confirmButton.setDisable(false);
            salesInvoicesController.reloadSalesInvoices();
            initData(salesInvoiceHeader);
        } catch (SQLException e) {
            DialogUtils.showErrorMessage("Error", "An error occurred while reverting the dispatch: " + e.getMessage());
        }
    }


    private void dispatchInvoice() {
        if (salesInvoiceHeader.isDispatched()) {
            DialogUtils.showErrorMessage("Already Dispatched", "This invoice has already been dispatched.");
            return;
        }

        salesInvoiceHeader.setDispatched(true);
        salesInvoiceHeader.setTransactionStatus("Dispatched");

        try (Connection connection = dataSource.getConnection()) {
            if (dispatchDate.getValue() == null) {
                dispatchDate.setValue(LocalDate.now()); // Set today's date if dispatch date is null
            }
            salesInvoiceHeader.setDispatchDate(Timestamp.valueOf(dispatchDate.getValue().atStartOfDay()));

            // Save sales invoice
            salesInvoiceHeader = salesInvoiceDAO.createSalesInvoiceWithDetails(salesInvoiceHeader, salesInvoiceDetails, connection);
            if (salesInvoiceHeader == null) {
                DialogUtils.showErrorMessage("Error", "Failed to dispatch the invoice.");
                return;
            }

            // ✅ Deduct inventory only if not already deducted
            inventoryList.clear();
            for (SalesInvoiceDetail detail : salesInvoiceDetails) {
                Inventory inventory = new Inventory();
                inventory.setQuantity(-Math.abs(detail.getQuantity())); // Ensure proper deduction
                inventory.setProductId(detail.getProduct().getProductId());
                inventory.setBranchId(salesInvoiceHeader.getSalesman().getGoodBranchCode());
                inventoryList.add(inventory);
            }

            boolean inventoryUpdated = inventoryDAO.updateInventoryBulk(inventoryList, connection);
            if (!inventoryUpdated) {
                DialogUtils.showErrorMessage("Error", "Failed to update inventory.");
                return;
            }

            // ✅ Dispatch Successful
            DialogUtils.showCompletionDialog("Success", "Invoice successfully dispatched.");
            transactionStatus.setText(salesInvoiceHeader.getTransactionStatus());
            paymentStatus.setText(salesInvoiceHeader.getPaymentStatus());
            dispatchButton.setText("Un Dispatch");
            dispatchButton.setDisable(true);
            salesInvoicesController.reloadSalesInvoices();
            initData(salesInvoiceHeader);
        } catch (SQLException e) {
            DialogUtils.showErrorMessage("Error", "An error occurred while dispatching the invoice: " + e.getMessage());
        }
    }


    public ObservableList<SalesInvoiceDetail> getSalesInvoiceDetailList() {
        return salesInvoiceDetails;
    }

    public void loadSalesReturnDetails() {
        salesReturnDetails.clear();
        salesReturnDetails.setAll(salesReturn.getSalesReturnDetails());
        updateTotals();

    }
}
