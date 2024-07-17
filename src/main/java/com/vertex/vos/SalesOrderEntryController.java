package com.vertex.vos;

import com.vertex.vos.Objects.*;
import com.vertex.vos.Utilities.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;

import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class SalesOrderEntryController implements Initializable {
    @FXML
    public TableView<ProductsInTransact> productsInTransact;
    @FXML
    public VBox addProductButton;
    public DatePicker dateOrdered;
    private AnchorPane contentPane;

    public void setContentPane(AnchorPane contentPane) {
        this.contentPane = contentPane;
    }

    private final HistoryManager historyManager = new HistoryManager();

    private int currentNavigationId = -1; // Initialize to a default value
    @FXML
    private AnchorPane POAnchorPane;

    @FXML
    private VBox POBox;

    @FXML
    private VBox POContent;

    @FXML
    private HBox confirmBox;

    @FXML
    private Button confirmButton;

    @FXML
    private ComboBox<String> customer;

    @FXML
    private VBox customerBox;

    @FXML
    private Label customerErr;

    @FXML
    private Label date;

    @FXML
    private Label discounted;

    @FXML
    private Label grandTotal;

    @FXML
    private Label gross;

    @FXML
    private Label paymentTerms;

    @FXML
    private Label purchaseOrderNo;

    @FXML
    private CheckBox receiptCheckBox;

    @FXML
    private Label receivingTerms;

    @FXML
    private ComboBox<String> salesman;

    @FXML
    private VBox salesmanBox;

    @FXML
    private Label salesmanErr;

    @FXML
    private HBox statusBox;

    @FXML
    private ImageView statusImage;

    @FXML
    private Label statusLabel;

    @FXML
    private HBox totalBox;

    @FXML
    private VBox totalBoxLabels;

    @FXML
    private VBox totalVBox;

    @FXML
    private Label vat;

    @FXML
    private Label withholding;

    int userDepartment = UserSession.getInstance().getUserDepartment();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        TableColumn<ProductsInTransact, String> productDescriptionColumn = new TableColumn<>("Product Description");
        productDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        TableColumn<ProductsInTransact, String> productUnitColumn = new TableColumn<>("Product Unit");
        productUnitColumn.setCellValueFactory(new PropertyValueFactory<>("unit"));

        TableColumn<ProductsInTransact, Double> productPriceColumn = getProductPriceColumn();

        TableColumn<ProductsInTransact, Integer> productQuantityColumn = getProductQuantityColumn();

        TableColumn<ProductsInTransact, Double> totalColumn = new TableColumn<>("Total");
        totalColumn.setCellValueFactory(data -> {
            double total = data.getValue().getOrderedQuantity() * data.getValue().getUnitPrice();
            return new SimpleDoubleProperty(total).asObject();
        });
        totalColumn.setCellFactory(new NumericTableCellFactory<>(Locale.US)); // Example with US locale

        productsInTransact.getColumns().addAll(productDescriptionColumn, productUnitColumn, productPriceColumn, productQuantityColumn, totalColumn);
        updateGrandTotal();
    }

    public double calculateGrandTotal() {
        return productsInTransact.getItems().stream()
                .mapToDouble(ProductsInTransact::getTotalAmount)
                .sum();
    }

    public void updateGrandTotal() {
        double grandTotalValue = calculateGrandTotal();
        grandTotal.setText(String.format("%.2f", grandTotalValue));
    }

    private TableColumn<ProductsInTransact, Integer> getProductQuantityColumn() {
        TableColumn<ProductsInTransact, Integer> productQuantityColumn = new TableColumn<>("Quantity");
        productQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("orderedQuantity"));
        productQuantityColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        productQuantityColumn.setOnEditCommit(event -> {
            ProductsInTransact product = event.getRowValue();
            int newQuantity = event.getNewValue();

            int availableQuantity = product.getInventoryQuantity() - product.getReservedQuantity();

            if (newQuantity > availableQuantity) {
                ConfirmationAlert confirmationAlert = new ConfirmationAlert(
                        "Quantity Error",
                        product.getDescription() + " only has " + availableQuantity + " of available reservation",
                        "Continue ordering?",
                        false
                );
                boolean yes = confirmationAlert.showAndWait();
                productsInTransact.requestFocus();
                if (!yes) {
                    productsInTransact.refresh();
                    productsInTransact.requestFocus();
                    return;
                }
            }

            // Update the product quantity if valid or confirmed by the user
            product.setOrderedQuantity(newQuantity);
            productsInTransact.requestFocus();
            productsInTransact.refresh();
            updateGrandTotal();
            calculateGrandTotal();
        });
        return productQuantityColumn;
    }


    private TableColumn<ProductsInTransact, Double> getProductPriceColumn() {
        TableColumn<ProductsInTransact, Double> productPriceColumn = new TableColumn<>("Unit Price");
        productPriceColumn.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        productPriceColumn.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        productPriceColumn.setOnEditCommit(event -> {
            ProductsInTransact product = event.getRowValue();
            product.setUnitPrice(event.getNewValue());
            productsInTransact.requestFocus();
            productsInTransact.refresh();
            updateGrandTotal();
            calculateGrandTotal();
        });
        return productPriceColumn;
    }

    SalesmanDAO salesmanDAO = new SalesmanDAO();
    SalesOrderDAO salesDAO = new SalesOrderDAO();
    CustomerDAO customerDAO = new CustomerDAO();
    CreditTypeDAO creditTypeDAO = new CreditTypeDAO();


    public void createNewOrder() {
        int SO_NO = salesDAO.getNextSoNo();
        receiptCheckBox.setSelected(true);
        customer.requestFocus();
        purchaseOrderNo.setText("SO" + SO_NO);
        ObservableList<String> salesmanNames = salesmanDAO.getAllSalesmanNames();
        ObservableList<String> customerStoreNames = customerDAO.getCustomerStoreNames();
        salesman.setItems(salesmanNames);
        customer.setItems(customerStoreNames);
        ComboBoxFilterUtil.setupComboBoxFilter(salesman, salesmanNames);
        ComboBoxFilterUtil.setupComboBoxFilter(customer, customerStoreNames);
        addProductButton.setDisable(true);
        dateOrdered.setValue(LocalDate.now());

        SalesOrder salesOrder = new SalesOrder();
        salesOrder.setOrderID(String.valueOf(SO_NO));
        salesOrder.setPoStatus("Entry");
        salesOrder.setCreatedDate(Timestamp.valueOf(LocalDateTime.now()));
        salesOrder.setTotal(BigDecimal.valueOf(calculateGrandTotal()));

        dateOrdered.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                LocalDateTime localDateTime = newValue.atStartOfDay(); // Convert LocalDate to LocalDateTime
                Timestamp timestamp = Timestamp.valueOf(localDateTime); // Convert LocalDateTime to Timestamp
                salesOrder.setCreatedDate(timestamp);
            }
        });
        ;

        salesman.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                int salesmanId = salesmanDAO.getSalesmanIdByStoreName(newValue);
                salesOrder.setSalesMan(String.valueOf(salesmanId));
                addProductButton.setDisable(false);
            }
        });
        customer.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                int customerId = customerDAO.getCustomerIdByStoreName(newValue);
                salesOrder.setCustomerID(String.valueOf(customerId));
                Customer selectedCustomer = customerDAO.getCustomer(customerId);
                try {
                    paymentTerms.setText(creditTypeDAO.getCreditTypeNameById(selectedCustomer.getPaymentTerm()));
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        statusLabel.setText(salesOrder.getPoStatus());
        addProductButton.setOnMouseClicked(mouseEvent -> addProductToSales(salesOrder));
        productsInTransact.getItems().addListener((ListChangeListener<ProductsInTransact>) change -> {
            confirmButton.setDisable(productsInTransact.getItems().isEmpty());
            updateGrandTotal();
        });
        confirmButton.setDisable(productsInTransact.getItems().isEmpty());
        confirmButton.setOnMouseClicked(mouseEvent -> {
            try {
                salesOrder.setPoStatus("Pending");
                entrySO(salesOrder);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    InventoryDAO inventoryDAO = new InventoryDAO();

    private void entrySO(SalesOrder salesOrder) throws SQLException {
        ConfirmationAlert confirmationAlert = new ConfirmationAlert("Entry SO" + salesOrder.getOrderID(), "Please double check the items, quantities, and prices", "Double check values", true);
        boolean confirmed = confirmationAlert.showAndWait();

        if (confirmed) {
            boolean headerCreated = createSalesOrderHeader(salesOrder);

            if (headerCreated) { // Check if header creation was successful
                List<SalesOrder> orders = new ArrayList<>();
                for (ProductsInTransact product : productsInTransact.getItems()) {
                    SalesOrder order = new SalesOrder();
                    order.setOrderID(salesOrder.getOrderID());
                    order.setProductID(product.getProductId());
                    order.setDescription(product.getDescription());
                    order.setQty(product.getOrderedQuantity());
                    order.setPrice(BigDecimal.valueOf(product.getUnitPrice()));
                    order.setTotal(BigDecimal.valueOf(product.getUnitPrice() * product.getOrderedQuantity()));
                    order.setTabName(salesOrder.getTabName()); // Assuming tab name is common for all products
                    order.setCustomerID(salesOrder.getCustomerID()); // Assuming customer ID is common for all products
                    order.setCustomerName(salesOrder.getCustomerName()); // Assuming customer name is common for all products
                    order.setStoreName(salesOrder.getStoreName()); // Assuming store name is common for all products
                    order.setSalesMan(salesOrder.getSalesMan()); // Assuming salesman is common for all products
                    order.setCreatedDate(salesOrder.getCreatedDate()); // Assuming created date is common for all products
                    order.setPoStatus(salesOrder.getPoStatus()); // Assuming PO status is common for all products
                    order.setSourceBranchId(salesOrder.getSourceBranchId());
                    orders.add(order);
                }
                boolean allOrdersSuccessful = salesDAO.createOrderPerProduct(orders);

                // Update reserved quantity
                if (allOrdersSuccessful) {
                    inventoryDAO.addOrUpdateReservedQuantityBulk(orders);
                }

                if (allOrdersSuccessful) {
                    DialogUtils.showConfirmationDialog("Success", "SO has been requested");
                    confirmButton.setDisable(true);
                    tableManagerController.loadSalesOrderItems();
                } else {
                    DialogUtils.showErrorMessage("Error", "Please contact your System Developer");
                }
            } else {
                DialogUtils.showErrorMessage("Error", "Failed to create sales order header");
            }
        }
    }


    private boolean createSalesOrderHeader(SalesOrder salesOrder) {
        SalesOrderHeader salesOrderHeader = new SalesOrderHeader();
        salesOrderHeader.setCustomerName(salesOrder.getCustomerID());
        salesOrderHeader.setHeaderId(1);
        salesOrderHeader.setOrderId(Integer.parseInt(salesOrder.getOrderID()));
        salesOrderHeader.setAdminId(UserSession.getInstance().getUserId());
        salesOrderHeader.setOrderDate(salesOrder.getCreatedDate());
        salesOrderHeader.setInvoice(receiptCheckBox.isSelected());

        // Calculate total amount due from products in transaction
        double totalAmountDue = productsInTransact.getItems().stream()
                .mapToDouble(product -> product.getOrderedQuantity() * product.getUnitPrice())
                .sum();
        salesOrderHeader.setAmountDue(BigDecimal.valueOf(totalAmountDue));

        InetAddress localhost = null;
        try {
            localhost = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        String computerName = localhost.getHostName();
        salesOrderHeader.setPosNo(computerName);
        salesOrderHeader.setTerminalNo(computerName);
        salesOrderHeader.setStatus(salesOrder.getPoStatus());
        salesOrderHeader.setSalesmanId(Integer.parseInt(salesOrder.getSalesMan()));
        salesOrderHeader.setSourceBranchId(salesOrder.getSourceBranchId());

        try {
            salesDAO.createSalesOrderHeader(salesOrderHeader);
            return true; // Header creation successful
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    private void addProductToSales(SalesOrder salesOrder) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ProductSelectionBySupplier.fxml"));
            Parent root = loader.load();
            ProductSelectionPerSupplier controller = loader.getController();

            controller.addProductToTableForSalesOrder(salesOrder);
            controller.setSalesController(this);

            Stage stage = new Stage();
            stage.setTitle("Add Products");
            stage.setScene(new Scene(root));

            addProductButton.setDisable(true);
            stage.setOnHidden(event -> addProductButton.setDisable(false));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void addProductToSalesOrderTable(ProductsInTransact product) {
        productsInTransact.getItems().add(product);
    }

    TableManagerController tableManagerController;

    public void setTableManager(TableManagerController tableManagerController) {
        this.tableManagerController = tableManagerController;
    }

    public void initData(SalesOrderHeader rowData) {
        if (rowData.getStatus().equals("Allocation") || rowData.getStatus().equals("On-hold") || rowData.getStatus().equals("For Layout")) {
            productsInTransact.setEditable(false);
            addProductButton.setDisable(true);
            confirmBox.setDisable(true);
        }
        purchaseOrderNo.setText("SO" + rowData.getOrderId());
        Timestamp timestamp = rowData.getOrderDate();
        LocalDateTime dateTime = timestamp.toLocalDateTime();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"); // Define your desired date format
        String formattedDate = dateTime.format(formatter);
        receiptCheckBox.setSelected(rowData.isInvoice());
        date.setText(formattedDate);
        customer.setValue(rowData.getCustomerName());
        salesman.setValue(salesmanDAO.getSalesmanNameById(rowData.getSalesmanId()));
        dateOrdered.setValue(rowData.getOrderDate().toLocalDateTime().toLocalDate());
        statusLabel.setText(rowData.getStatus());
        ObservableList<ProductsInTransact> orderedProducts = salesDAO.fetchOrderedProducts(rowData.getOrderId());
        Platform.runLater(() -> {
            productsInTransact.setItems(orderedProducts);
            productsInTransact.refresh();
            updateGrandTotal();
        });

        if (userDepartment == 7) {
            approvalUIForAccounting(rowData);
        } else if (userDepartment == 10) {
            updateUIForEncoder(rowData);
        }

    }

    private void updateUIForEncoder(SalesOrderHeader rowData) {
        confirmButton.setText("Update");
    }

    private void approvalUIForAccounting(SalesOrderHeader rowData) {
        productsInTransact.setEditable(false);
        addProductButton.setDisable(true);

        Button holdButton = new Button("Hold");
        Button approveButton = new Button("Approve");
        confirmBox.getChildren().clear();
        confirmBox.setSpacing(5);
        confirmBox.getChildren().addAll(holdButton, approveButton);

        approveButton.setOnMouseClicked(mouseEvent -> {
            rowData.setStatus("Allocation");
            if (salesDAO.updateSalesOrderStatus(rowData)) {
                confirmBox.setDisable(true);
                DialogUtils.showConfirmationDialog("Approval", "Sales order approved successfully.");
                tableManagerController.loadSalesOrders();
            } else {
                DialogUtils.showErrorMessage("Error", "Failed to update sales order status.");
                tableManagerController.loadSalesOrders();

            }
        });

        holdButton.setOnMouseClicked(mouseEvent -> {
            rowData.setStatus("On-hold");
            if (salesDAO.updateSalesOrderStatus(rowData)) {
                confirmBox.setDisable(true);
                DialogUtils.showConfirmationDialog("Hold", "Sales order put on hold successfully.");
                tableManagerController.loadSalesOrders();

            } else {
                DialogUtils.showErrorMessage("Error", "Failed to update sales order status.");
                tableManagerController.loadSalesOrders();

            }
        });
    }

    public void initDataForConversion(SalesOrderHeader rowData) {
        purchaseOrderNo.setText("SO" + rowData.getOrderId());
        Timestamp timestamp = rowData.getOrderDate();
        LocalDateTime dateTime = timestamp.toLocalDateTime();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"); // Define your desired date format
        String formattedDate = dateTime.format(formatter);
        receiptCheckBox.setSelected(rowData.isInvoice());
        date.setText(formattedDate);
        customer.setValue(rowData.getCustomerName());
        salesman.setValue(salesmanDAO.getSalesmanNameById(rowData.getSalesmanId()));
        dateOrdered.setValue(rowData.getOrderDate().toLocalDateTime().toLocalDate());
        statusLabel.setText(rowData.getStatus());
        confirmButton.setText("Convert TO SI");
        ObservableList<ProductsInTransact> orderedProducts = salesDAO.fetchOrderedProducts(rowData.getOrderId());
        Platform.runLater(() -> {
            productsInTransact.setItems(orderedProducts);
            productsInTransact.refresh();
            updateGrandTotal();
        });

        confirmButton.setOnMouseClicked(mouseEvent -> {
            try {
                convertSOToSI(rowData);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void convertSOToSI(SalesOrderHeader rowData) throws SQLException {
        SalesInvoiceDAO salesInvoiceDAO = new SalesInvoiceDAO();
        ConfirmationAlert confirmationAlert = new ConfirmationAlert(
                "Convert SO to SI",
                "Are you sure you want to convert Sales Order " + rowData.getOrderId() + " to Sales Invoice?",
                "Proceed",
                true
        );
        boolean confirmed = confirmationAlert.showAndWait();

        if (confirmed) {
            SalesInvoice salesInvoice = new SalesInvoice();
            salesInvoice.setCustomerId(customerDAO.getCustomerIdByStoreName(rowData.getCustomerName())); // Assuming CustomerDAO exists
            salesInvoice.setOrderId(rowData.getOrderId());
            salesInvoice.setType("SO");
            salesInvoice.setTotalAmount(BigDecimal.valueOf(calculateGrandTotal()));
            salesInvoice.setSalesmanId(rowData.getSalesmanId());
            salesInvoice.setStatus("Pending");
            salesInvoice.setInvoiceDate(Timestamp.valueOf(LocalDateTime.now()));

            boolean invoiced = salesInvoiceDAO.createSalesInvoice(salesInvoice);

            if (invoiced) {
                List<ProductsInTransact> products = productsInTransact.getItems();
                boolean allInvoiceDetailsSuccessful = salesInvoiceDAO.createSalesInvoiceDetailsBulk(rowData.getOrderId(), products);

                if (allInvoiceDetailsSuccessful) {
                    rowData.setStatus("Invoiced");
                    salesDAO.updateSalesOrderStatus(rowData);

                    DialogUtils.showConfirmationDialog("Success", "Sales Invoice has been created");
                    tableManagerController.loadSalesInvoice();

                    // Close the window after the transaction is completed
                    Stage stage = (Stage) confirmButton.getScene().getWindow();
                    stage.close();
                } else {
                    DialogUtils.showErrorMessage("Error", "Failed to create Sales Invoice details");
                }
            } else {
                DialogUtils.showErrorMessage("Error", "Failed to create Sales Invoice Header");
            }
        }
    }


}
