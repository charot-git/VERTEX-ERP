package com.vertex.vos;

import com.vertex.vos.Objects.ComboBoxFilterUtil;
import com.vertex.vos.Objects.InvoiceType;
import com.vertex.vos.Objects.SalesInvoice;
import com.vertex.vos.Objects.SalesOrderHeader;
import com.vertex.vos.Utilities.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ResourceBundle;

public class SalesInvoiceController implements Initializable {

    @FXML
    private VBox addBoxes;

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

    public void initData(SalesInvoice selectedInvoice) {

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
        SalesInvoice salesInvoice = new SalesInvoice();
        System.out.println(rowData.getOrderId());

        String tripId = tripSummaryDetailsDAO.getTripIdByOrderId(rowData.getOrderId());
        LocalDate tripDate = tripSummaryDetailsDAO.getTripDateByTripNo(tripId);
        salesInvoice.setOrderId(rowData.getOrderId());
        salesInvoice.setSalesmanId(rowData.getSalesmanId());
        salesInvoice.setSourceBranchId(rowData.getSourceBranchId());
        salesInvoice.setCustomerCode(rowData.getCustomerId());
        salesInvoice.setInvoiceDate(Timestamp.valueOf(LocalDateTime.now()));
        salesInvoice.setDeliveryDate(tripDate);
        invoiceTypeComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            salesInvoice.setInvoiceType(invoiceTypeDAO.getInvoiceIdByType(newValue));
        });

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
