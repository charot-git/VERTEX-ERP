package com.vertex.vos;

import com.vertex.vos.DAO.SalesInvoiceTypeDAO;
import com.vertex.vos.Objects.*;
import com.vertex.vos.Utilities.*;
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
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class SalesInvoiceTemporaryController implements Initializable {

    public TextField invoiceNoTextField;
    public TextField referenceNoTextField;
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
    @FXML
    private VBox addProductToItems;

    @FXML
    private VBox addProductToReturns;

    @FXML
    private Label addVAT;

    @FXML
    private Label addVatValue;

    @FXML
    private Label amountDue;

    @FXML
    private Label amountDueValue;

    @FXML
    private Label amountNetOfVat;

    @FXML
    private Label amountNetOfVatValue;

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

    @FXML
    private Label totalAmountDue;

    @FXML
    private Label totalAmountDueValue;

    @FXML
    private Label totalSales;

    @FXML
    private Label totalSalesVatInclusiveValue;

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

    public void createNewSalesEntry(Stage stage) {

        salesInvoiceHeader.setCreatedBy(UserSession.getInstance().getUserId());
        salesInvoiceHeader.setOrderId("PRLE-" + salesOrderDAO.getNextSoNo());

        salesNo.setText(salesInvoiceHeader.getOrderId());

        invoiceDate.setValue(LocalDate.now());

        salesTypeList.add("Booking");
        salesTypeList.add("Van Sales");
        salesType.setItems(salesTypeList);

        salesType.setValue("Booking");
        ObservableList<SalesInvoiceType> salesInvoiceTypeList = salesInvoiceTypeDAO.getSalesInvoiceTypes();
        ObservableList<String> salesInvoiceTypeNames = FXCollections.observableArrayList();
        for (SalesInvoiceType salesInvoiceType : salesInvoiceTypeList) {
            salesInvoiceTypeNames.add(salesInvoiceType.getName());
        }
        receiptType.setItems(salesInvoiceTypeNames);

        receiptType.setValue(salesInvoiceTypeList.get(0).getName());

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
                            salesmanTextField.setText(selectedSalesman.getSalesmanName());
                        }
                    }
                });
            }
        });

        addProductToItems.setOnMouseClicked(mouseEvent -> openProductSelection(stage));
    }

    private void openProductSelection(Stage parentStage) {
        // Check if the product selection window is already open
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
                productSelectionStage.setMaximized(true); // Maximize the window when opened
                productSelectionStage.setScene(new Scene(root));
                productSelectionStage.show();

                // Close the product selection window when the parent stage is closed
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
        salesInvoiceDetails.add(selectedProduct);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // Set up price types in the combo box
        priceType.getItems().addAll("A", "B", "C", "D", "E");
        priceType.setValue("A"); // Default price type

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
            int newQuantity = event.getNewValue(); // Get the new quantity from the user input
            invoiceDetail.setQuantity(newQuantity); // Update the quantity
            updateNetAmount(invoiceDetail);
            itemsTable.requestFocus();
        });

        // Calculate price based on the selected price type
        // Enable unitPrice column editing
        priceItemCol.setCellValueFactory(cellData -> {
            SalesInvoiceDetail invoiceDetail = cellData.getValue();
            return new SimpleDoubleProperty(invoiceDetail.getUnitPrice()).asObject();
        });
        priceItemCol.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        priceItemCol.setEditable(true); // Make the column editable

// Handle unitPrice edit commit to update unit price and recalculate net amount
        priceItemCol.setOnEditCommit(event -> {
            SalesInvoiceDetail invoiceDetail = event.getRowValue();
            double newUnitPrice = event.getNewValue(); // Get the new unit price from user input
            invoiceDetail.setUnitPrice(newUnitPrice); // Update the unit price

            // Recalculate the net amount based on the new unit price and quantity
            updateNetAmount(invoiceDetail); // This function already recalculates net amount and refreshes the table
            itemsTable.requestFocus(); // Optional: Keeps the focus on the table
        });
        ;

        // Calculate net amount (price * quantity)
        netAmountItemCol.setCellValueFactory(cellData -> {
            SalesInvoiceDetail invoiceDetail = cellData.getValue();
            // Calculate the net amount (price * quantity) and update it in the SalesInvoiceDetail
            double netAmount = invoiceDetail.getUnitPrice() * invoiceDetail.getQuantity();
            invoiceDetail.setTotalPrice(netAmount); // Set the net amount in the invoice detail
            return new SimpleDoubleProperty(netAmount).asObject();
        });
        // Set the TableView's items to be the list of SalesInvoiceDetails
        itemsTable.setItems(salesInvoiceDetails);
    }

    private void updateNetAmount(SalesInvoiceDetail invoiceDetail) {
        // Recalculate the net amount based on the updated price and quantity
        double netAmount = invoiceDetail.getUnitPrice() * invoiceDetail.getQuantity();
        invoiceDetail.setTotalPrice(netAmount); // Update the net amount in the invoice detail
        itemsTable.refresh(); // Refresh the table to show updated net amount
    }
}
