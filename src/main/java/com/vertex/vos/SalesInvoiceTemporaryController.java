package com.vertex.vos;

import com.vertex.vos.DAO.SalesInvoiceTypeDAO;
import com.vertex.vos.Objects.Customer;
import com.vertex.vos.Objects.SalesInvoiceType;
import com.vertex.vos.Objects.Salesman;
import com.vertex.vos.Utilities.CustomerDAO;
import com.vertex.vos.Utilities.GenericSelectionWindow;
import com.vertex.vos.Utilities.SalesmanDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class SalesInvoiceTemporaryController {

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
    private TableColumn<?, ?> descriptionItemCol;

    @FXML
    private TableColumn<?, ?> descriptionReturnCol;

    @FXML
    private TableColumn<?, ?> discountItemCol;

    @FXML
    private TableColumn<?, ?> discountReturnCol;

    @FXML
    private DatePicker invoiceDate;

    @FXML
    private TableView<?> itemsTable;

    @FXML
    private Label lessSCPWDDiscount;

    @FXML
    private Label lessSCPWDDiscountValue;

    @FXML
    private Label lessVat;

    @FXML
    private Label lessVatValue;

    @FXML
    private TableColumn<?, ?> netAmountItemCol;

    @FXML
    private TableColumn<?, ?> netAmountReturnCol;

    @FXML
    private TableColumn<?, ?> priceItemCol;

    @FXML
    private TableColumn<?, ?> priceReturnCol;

    @FXML
    private TableColumn<?, ?> productCodeItemCol;

    @FXML
    private TableColumn<?, ?> productCodeReturnCol;

    @FXML
    private TableColumn<?, ?> quantityItemCol;

    @FXML
    private TableColumn<?, ?> quantityReturnCol;

    @FXML
    private ComboBox<String> receiptType;

    @FXML
    private TextField referenceNoTextField;

    @FXML
    private TableView<?> returnsTable;

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

    @FXML
    private TableColumn<?, ?> unitItemCol;

    @FXML
    private TableColumn<?, ?> unitReturnCol;

    @FXML
    private Label vatAmount;

    @FXML
    private Label vatAmountValue;

    @FXML
    private Label vatExemptSales;

    @FXML
    private Label vatExemptValue;

    @FXML
    private Label vatableSales;

    @FXML
    private Label vatableSalesValue;

    @FXML
    private Label zeroRatedSales;

    @FXML
    private Label zeroRatedSalesValue;

    ObservableList<String> salesTypeList = FXCollections.observableArrayList();

    SalesInvoiceTypeDAO salesInvoiceTypeDAO = new SalesInvoiceTypeDAO();
    CustomerDAO customerDAO = new CustomerDAO();
    private ObservableList<Customer> customers = FXCollections.observableArrayList();
    private ObservableList<Salesman> salesmen = FXCollections.observableArrayList();

    SalesmanDAO salesmanDAO = new SalesmanDAO();

    Customer selectedCustomer = null;
    Salesman selectedSalesman = null;


    public void createNewSalesEntry(Stage stage) {

        salesTypeList.add("Booking");
        salesTypeList.add("Van Sales");
        salesType.setItems(salesTypeList);
        ObservableList<SalesInvoiceType> salesInvoiceTypeList = salesInvoiceTypeDAO.getSalesInvoiceTypes();
        ObservableList<String> salesInvoiceTypeNames = FXCollections.observableArrayList();
        for (SalesInvoiceType salesInvoiceType : salesInvoiceTypeList) {
            salesInvoiceTypeNames.add(salesInvoiceType.getName());
        }
        receiptType.setItems(salesInvoiceTypeNames);


        customers.setAll(customerDAO.getAllActiveCustomers());
        salesmen.setAll(salesmanDAO.getAllSalesmen());

        invoiceDate.requestFocus();


        customerTextField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                GenericSelectionWindow<Customer> selectionWindow = new GenericSelectionWindow<>();
                customerTextField.setOnKeyPressed(event -> {
                    if (event.getCode() == KeyCode.DOWN) {
                        selectedCustomer = selectionWindow.showSelectionWindow(stage, "Select Customer", customers);

                        if (selectedCustomer != null){
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
                        selectedSalesman = selectionWindow.showSelectionWindow(stage, "Select Salesman" , salesmen);
                        if (selectedSalesman != null){
                            salesmanTextField.setText(selectedSalesman.getSalesmanName());
                        }
                    }
                });
            }
        });
    }
}
