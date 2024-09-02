package com.vertex.vos;

import com.vertex.vos.Objects.*;
import com.vertex.vos.Utilities.*;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class SalesInvoiceController implements Initializable {

    public TableView<CreditDebitMemo> adjustmentTable;
    public TableView<SalesReturn> salesReturnTable;
    public Tab salesOrderProducts;
    @FXML
    private HBox addBoxes;

    @FXML
    private HBox addCreditMemo;

    @FXML
    private HBox addDebitMemo;

    @FXML
    private Label addProductLabel;

    @FXML
    private Label addProductLabel1;

    @FXML
    private ComboBox<String> branch;

    @FXML
    private HBox confirmBox;

    @FXML
    private Button confirmButton;

    @FXML
    private ComboBox<String> customer;

    @FXML
    private VBox customerBox;

    @FXML
    private Label date;

    @FXML
    private DatePicker dateOrdered;

    @FXML
    private VBox dateOrderedBox;

    @FXML
    private VBox dateOrderedBox1;

    @FXML
    private DatePicker deliveryDate;

    @FXML
    private VBox deliveryDateBox;

    @FXML
    private Label deliveryDateErr;

    @FXML
    private Label discounted;

    @FXML
    private Label grandTotal;

    @FXML
    private Label gross;

    @FXML
    private DatePicker invoiceDate;

    @FXML
    private VBox invoiceTypeBox;

    @FXML
    private ComboBox<String> invoiceTypeComboBox;

    @FXML
    private DatePicker paymentDueDate;

    @FXML
    private VBox paymentDueDateBox;

    @FXML
    private Label paymentTerms;

    @FXML
    private Button printButton;

    @FXML
    private Label purchaseOrderNo;

    @FXML
    private TabPane salesOrderTab;

    @FXML
    private ComboBox<String> salesman;

    @FXML
    private VBox salesmanBox;

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

    PaymentTermsDAO paymentTermsDAO = new PaymentTermsDAO();

    public void initData(SalesInvoiceHeader selectedInvoice) {
        salesman.setValue(selectedInvoice.getSalesman().getSalesmanName());
        branch.setValue(selectedInvoice.getSalesman().getTruckPlate());
        customer.setValue(selectedInvoice.getStoreName());
        invoiceTypeComboBox.setValue(invoiceTypeDAO.getInvoiceTypeById(selectedInvoice.getType()));
        deliveryDate.setValue(selectedInvoice.getPostedDate().toLocalDate());
        purchaseOrderNo.setText(selectedInvoice.getOrderId());
        dateOrdered.setValue(selectedInvoice.getCreatedDate().toLocalDateTime().toLocalDate());
        statusLabel.setText(selectedInvoice.getTransactionStatus());
        paymentDueDate.setValue(selectedInvoice.getDueDate().toLocalDate());
        paymentTerms.setText(paymentTermsDAO.getPaymentTermNameById(selectedInvoice.getPaymentTerms()));
        invoiceDate.setValue(selectedInvoice.getInvoiceDate().toLocalDate());
        date.setText(selectedInvoice.getInvoiceDate().toString());

        populateProductsTable(selectedInvoice);

    }

    private void populateProductsTable(SalesInvoiceHeader selectedInvoice) {
        ObservableList<ProductsInTransact> productsForInvoice = salesInvoiceDAO.loadSalesInvoiceProducts(selectedInvoice.getOrderId());
        productTable.setItems(productsForInvoice);
    }

    TableManagerController tableManagerController;
    SalesInvoiceDAO salesInvoiceDAO = new SalesInvoiceDAO();
    SalesOrderDAO salesOrderDAO = new SalesOrderDAO();
    TripSummaryDetailsDAO tripSummaryDetailsDAO = new TripSummaryDetailsDAO();


    public void setTableManager(TableManagerController tableManagerController) {
        this.tableManagerController = tableManagerController;
    }

    public void initDataForConversion(SalesOrderHeader rowData) {
        populateInvoiceTypeComboBox();
        SalesInvoiceHeader salesInvoice = new SalesInvoiceHeader();

        deliveryDate.setDisable(true);

        String tripId = tripSummaryDetailsDAO.getTripIdByOrderId(rowData.getOrderId());
        salesInvoice.setOrderId(rowData.getOrderId());
        salesInvoice.setSalesmanId(rowData.getSalesmanId());
        salesInvoice.setSalesman(salesmanDAO.getSalesmanDetails(rowData.getSalesmanId()));
        salesInvoice.setCustomerCode(rowData.getCustomerId());
        salesInvoice.setStoreName(rowData.getCustomerName());
        salesInvoice.setCreatedDate(rowData.getOrderDate());
        salesInvoice.setTransactionStatus(rowData.getStatus());

        paymentDueDate.valueProperty().addListener((observable, oldValue, newValue) -> {
            salesInvoice.setDueDate(Date.valueOf(newValue));
        });
        invoiceDate.valueProperty().addListener((observable, oldValue, newValue) -> {
            salesInvoice.setInvoiceDate(Date.valueOf(newValue));
        });
        invoiceTypeComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            salesInvoice.setType(invoiceTypeDAO.getInvoiceIdByType(newValue));
            setItemsPerInvoiceByInvoiceType(salesInvoice);
        });

        setValues(salesInvoice);

    }


    public void setItemsPerInvoiceByInvoiceType(SalesInvoiceHeader salesInvoice) {
        int maxSizeOfTable = getMaxTableSizeBasedOnInvoiceType(salesInvoice.getType());
        if (maxSizeOfTable == 0) return;

        ObservableList<ProductsInTransact> productsForInvoice = salesOrderDAO.fetchOrderedProducts(salesInvoice.getOrderId());
        salesOrderTab.getTabs().clear();

        int totalTabs = (int) Math.ceil((double) productsForInvoice.size() / maxSizeOfTable);

        for (int i = 0; i < totalTabs; i++) {
            int fromIndex = i * maxSizeOfTable;
            int toIndex = Math.min(fromIndex + maxSizeOfTable, productsForInvoice.size());
            ObservableList<ProductsInTransact> subList = FXCollections.observableArrayList(productsForInvoice.subList(fromIndex, toIndex));

            Tab tab = new Tab("Tab " + (i + 1));
            tab.setContent(createProductTable(subList));
            tab.setContextMenu(createContextMenuForTab(tab));

            salesOrderTab.getTabs().add(tab);
        }
    }

    private int getMaxTableSizeBasedOnInvoiceType(int invoiceType) {
        return switch (invoiceType) {
            case 1, 2 -> 11;
            case 3 -> 29;
            default -> 0;
        };
    }

    private ContextMenu createContextMenuForTab(Tab tab) {
        MenuItem editTitle = new MenuItem("Assign Invoice Number");
        editTitle.setOnAction(event -> {
            TextInputDialog dialog = new TextInputDialog(tab.getText());
            dialog.setTitle("Edit Invoice Number");
            dialog.setHeaderText("Edit Invoice Title");
            dialog.setContentText("Please enter the new invoice number:");
            dialog.showAndWait().ifPresent(tab::setText);
        });

        return new ContextMenu(editTitle);
    }

    private TableView<ProductsInTransact> createProductTable(ObservableList<ProductsInTransact> products) {
        TableView<ProductsInTransact> tableView = new TableView<>();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        TableColumn<ProductsInTransact, String> productDescriptionColumn = new TableColumn<>("Product Description");
        productDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        TableColumn<ProductsInTransact, String> productUnitColumn = new TableColumn<>("Product Unit");
        productUnitColumn.setCellValueFactory(new PropertyValueFactory<>("unit"));

        TableColumn<ProductsInTransact, Double> productPriceColumn = new TableColumn<>("Price");
        productPriceColumn.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));

        TableColumn<ProductsInTransact, Integer> productQuantityColumn = new TableColumn<>("Quantity");
        productQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("orderedQuantity"));

        TableColumn<ProductsInTransact, Double> totalColumn = new TableColumn<>("Total");
        totalColumn.setCellValueFactory(data -> {
            double total = data.getValue().getOrderedQuantity() * data.getValue().getUnitPrice();
            return new SimpleDoubleProperty(total).asObject();
        });
        totalColumn.setCellFactory(new NumericTableCellFactory<>(Locale.US)); // Example with US locale

        tableView.getColumns().addAll(productDescriptionColumn, productUnitColumn, productPriceColumn, productQuantityColumn, totalColumn);
        tableView.setItems(products);
        return tableView;
    }


    private void setValues(SalesInvoiceHeader salesInvoice) {
        salesman.setValue(salesInvoice.getSalesman().getSalesmanName());
        customer.setValue(salesInvoice.getStoreName());
        invoiceTypeComboBox.setValue(invoiceTypeDAO.getInvoiceTypeById(salesInvoice.getType()));
        deliveryDate.setValue(LocalDate.now());
        purchaseOrderNo.setText(salesInvoice.getOrderId());
        dateOrdered.setValue(salesInvoice.getCreatedDate().toLocalDateTime().toLocalDate());
        statusLabel.setText(salesInvoice.getTransactionStatus());
    }

    InvoiceTypeDAO invoiceTypeDAO = new InvoiceTypeDAO();

    private void populateInvoiceTypeComboBox() {
        InvoiceTypeDAO invoiceTypeDAO = new InvoiceTypeDAO();
        List<InvoiceType> invoiceTypeList = invoiceTypeDAO.getAllInvoiceTypes();

        // Extract just the type names to display in the ComboBox
        ObservableList<String> invoiceTypes = FXCollections.observableArrayList();
        for (InvoiceType invoiceType : invoiceTypeList) {
            invoiceTypes.add(invoiceType.getType());
        }
        TextFieldUtils.setComboBoxBehavior(invoiceTypeComboBox);
        ComboBoxFilterUtil.setupComboBoxFilter(invoiceTypeComboBox, invoiceTypes);
        invoiceTypeComboBox.setItems(invoiceTypes);
        invoiceTypeComboBox.getSelectionModel().selectFirst();
    }

    BranchDAO branchDAO = new BranchDAO();
    CustomerDAO customerDAO = new CustomerDAO();
    SalesmanDAO salesmanDAO = new SalesmanDAO();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setUpProductsTable();
    }

    TableView<ProductsInTransact> productTable = new TableView<>();


    private void setUpProductsTable() {


        productTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        TableColumn<ProductsInTransact, String> invoiceNoColumn = new TableColumn<>("Invoice");
        invoiceNoColumn.setCellValueFactory(new PropertyValueFactory<>("invoiceNo"));

        TableColumn<ProductsInTransact, String> productDescriptionColumn = new TableColumn<>("Product Description");
        productDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        TableColumn<ProductsInTransact, String> productUnitColumn = new TableColumn<>("Product Unit");
        productUnitColumn.setCellValueFactory(new PropertyValueFactory<>("unit"));


        TableColumn<ProductsInTransact, Double> productPriceColumn = new TableColumn<>("Price");
        productPriceColumn.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));


        TableColumn<ProductsInTransact, Integer> productQuantityColumn = new TableColumn<>("Quantity");
        productQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("orderedQuantity"));


        TableColumn<ProductsInTransact, Double> totalColumn = new TableColumn<>("Total");
        totalColumn.setCellValueFactory(data -> {
            double total = data.getValue().getOrderedQuantity() * data.getValue().getUnitPrice();
            return new SimpleDoubleProperty(total).asObject();
        });
        totalColumn.setCellFactory(new NumericTableCellFactory<>(Locale.US)); // Example with US locale

        productTable.getColumns().addAll(invoiceNoColumn, productDescriptionColumn, productUnitColumn, productPriceColumn, productQuantityColumn, totalColumn);

        salesOrderProducts.setContent(productTable);

    }
}

