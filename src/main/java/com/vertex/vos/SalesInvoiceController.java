package com.vertex.vos;

import com.vertex.vos.DAO.SalesInvoiceDAO;
import com.vertex.vos.Objects.*;
import com.vertex.vos.Utilities.*;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
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
import java.util.*;
import java.util.stream.Collectors;

public class SalesInvoiceController implements Initializable {

    public TableView<CreditDebitMemo> adjustmentTable;
    public TableView<SalesReturn> salesReturnTable;
    public Label transactionStatus;
    public Label paymentStatus;
    public Button paidingButton;
    public Button approveButton;
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
        branch.setValue(branchDAO.getBranchNameById(selectedInvoice.getSalesman().getBranchCode()));
        customer.setValue(selectedInvoice.getCustomer().getStoreName());
        invoiceTypeComboBox.setValue(invoiceTypeDAO.getInvoiceTypeById(selectedInvoice.getType()));
        deliveryDate.setValue(selectedInvoice.getPostedDate().toLocalDateTime().toLocalDate());
        purchaseOrderNo.setText(selectedInvoice.getOrderId());
        dateOrdered.setValue(selectedInvoice.getCreatedDate().toLocalDateTime().toLocalDate());
        transactionStatus.setText(selectedInvoice.getTransactionStatus());
        paymentStatus.setText(selectedInvoice.getPaymentStatus());
        paymentDueDate.setValue(selectedInvoice.getDueDate().toLocalDate());
        paymentTerms.setText(paymentTermsDAO.getPaymentTermNameById(selectedInvoice.getPaymentTerms()));
        invoiceDate.setValue(selectedInvoice.getInvoiceDate().toLocalDate());
        date.setText(selectedInvoice.getInvoiceDate().toString());

        setButtonAccess(selectedInvoice);

        setProductsInTransact(selectedInvoice);

        paidingButton.setOnMouseClicked(mouseEvent -> {
            initiatePaiding(selectedInvoice);
        });

    }

    private void setButtonAccess(SalesInvoiceHeader selectedInvoice) {
        if (selectedInvoice.getPaymentStatus().equals("PAID")) {
            paidingButton.setDisable(true);
        }

        if (selectedInvoice.getTransactionStatus().equals("APPROVED")) {
            approveButton.setDisable(true);
        }

    }

    private void initiatePaiding(SalesInvoiceHeader selectedInvoice) {
        ConfirmationAlert confirmationDialog = new ConfirmationAlert("Paying Invoice?", "Please double check values", "", false);
        boolean isConfirmed = confirmationDialog.showAndWait();
        if (isConfirmed) {
            boolean isPaid = salesInvoiceDAO.paidOrder(selectedInvoice);
            if (isPaid) {
                DialogUtils.showConfirmationDialog("Payment Successful", selectedInvoice.getOrderId() + " has been paid successfully");
                selectedInvoice.setPaymentStatus("PAID");
                paidingButton.setDisable(true);
                paymentStatus.setText(selectedInvoice.getPaymentStatus());
            } else {
                DialogUtils.showErrorMessage("Payment Unsuccessful", selectedInvoice.getOrderId() + " could not be paid");
            }
        }
    }

    private Map<String, ObservableList<ProductsInTransact>> groupProductsByInvoiceNo(ObservableList<ProductsInTransact> products) {
        Map<String, ObservableList<ProductsInTransact>> groupedProducts = new HashMap<>();

        for (ProductsInTransact product : products) {
            String invoiceNo = product.getInvoiceNo();
            groupedProducts.computeIfAbsent(invoiceNo, k -> FXCollections.observableArrayList()).add(product);
        }

        return groupedProducts;
    }

    private void setProductsInTransact(SalesInvoiceHeader selectedInvoice) {
        ObservableList<ProductsInTransact> orderProducts = salesInvoiceDAO.loadSalesInvoiceProducts(selectedInvoice.getOrderId());

        if (orderProducts != null) {
            Map<String, ObservableList<ProductsInTransact>> groupedProducts = groupProductsByInvoiceNo(orderProducts);

            salesOrderTab.getTabs().clear(); // Clear existing tabs

            for (Map.Entry<String, ObservableList<ProductsInTransact>> entry : groupedProducts.entrySet()) {
                String invoiceNo = entry.getKey();
                ObservableList<ProductsInTransact> products = entry.getValue();

                TableView<ProductsInTransact> table = new TableView<>();

                table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

                TableColumn<ProductsInTransact, String> invoiceNoColumn = new TableColumn<>("Invoice No");
                TableColumn<ProductsInTransact, String> productDescriptionColumn = new TableColumn<>("Product Description");
                TableColumn<ProductsInTransact, Integer> quantityColumn = new TableColumn<>("Quantity");
                TableColumn<ProductsInTransact, String> unitColumn = new TableColumn<>("Unit");
                TableColumn<ProductsInTransact, Double> unitPriceColumn = new TableColumn<>("Unit Price");
                TableColumn<ProductsInTransact, Double> totalColumn = new TableColumn<>("Total");

                invoiceNoColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getInvoiceNo()));
                productDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
                quantityColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getOrderedQuantity()).asObject());
                unitColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUnit()));
                unitPriceColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getUnitPrice()).asObject());
                totalColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getTotalAmount()).asObject());

                table.getColumns().addAll(invoiceNoColumn, productDescriptionColumn, quantityColumn, unitColumn, unitPriceColumn, totalColumn);
                table.setItems(products);

                Tab tab = new Tab(invoiceNo, table);
                salesOrderTab.getTabs().add(tab);
            }
        }
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
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
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

    }
}