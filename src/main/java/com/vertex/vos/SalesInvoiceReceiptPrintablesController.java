package com.vertex.vos;

import com.vertex.vos.Constructors.Company;
import com.vertex.vos.Constructors.Customer;
import com.vertex.vos.Constructors.ProductsInTransact;
import com.vertex.vos.Constructors.SalesInvoice;
import com.vertex.vos.Utilities.CompanyDAO;
import com.vertex.vos.Utilities.CustomerDAO;
import com.vertex.vos.Utilities.VatCalculator;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.File;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;


public class SalesInvoiceReceiptPrintablesController {

    public Label customerName;
    public Label customerAddress;
    public Label date;
    @FXML
    private TableColumn<?, ?> amount;

    @FXML
    private ImageView barcode;

    @FXML
    private VBox containerBox;

    @FXML
    private TableColumn<?, ?> description;

    @FXML
    private HBox headerBox;

    @FXML
    private Label headerCompanyAdditionalDetails;

    @FXML
    private Label headerCompanyAddress;

    @FXML
    private Label headerCompanyText;

    @FXML
    private ImageView headerLogo;

    @FXML
    private Label invoiceType;

    @FXML
    private Label lessAdditionalDiscount;

    @FXML
    private Label lessTotalDiscount;

    @FXML
    private Label lessVolumeDiscount;

    @FXML
    private Label netInvoiceAmount;

    @FXML
    private Label number;

    @FXML
    private TableColumn<?, ?> quantity;

    @FXML
    private Label subTotal;

    @FXML
    private TableView<ProductsInTransact> tableView;

    @FXML
    private Label totalGross;

    @FXML
    private TableColumn<?, ?> unit;

    @FXML
    private TableColumn<?, ?> unitPrice;

    public void printSalesInvoice(SalesInvoice selectedInvoice, ObservableList<ProductsInTransact> salesInvoiceProducts, String salesInvoiceType) {// Populate invoice amounts
        initializeHeader(selectedInvoice, salesInvoiceType);

        initializeProducts(salesInvoiceProducts);

        initializeFooter(selectedInvoice);
    }

    private void initializeProducts(ObservableList<ProductsInTransact> salesInvoiceProducts) {
        description.setCellValueFactory(new PropertyValueFactory<>("description"));
        quantity.setCellValueFactory(new PropertyValueFactory<>("orderedQuantity"));
        unit.setCellValueFactory(new PropertyValueFactory<>("unit"));
        unitPrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        amount.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        tableView.setItems(salesInvoiceProducts);
    }

    private void initializeFooter(SalesInvoice selectedInvoice) {
        BigDecimal subtotalAmount = selectedInvoice.getTotalAmount() != null ? selectedInvoice.getTotalAmount() : BigDecimal.ZERO;
        BigDecimal totalGrossAmount = selectedInvoice.getTotalGross() != null ? selectedInvoice.getTotalGross() : BigDecimal.ZERO;
        BigDecimal volumeDiscountAmount = selectedInvoice.getVolumeDiscount() != null ? selectedInvoice.getVolumeDiscount() : BigDecimal.ZERO;
        BigDecimal totalDiscountAmount = selectedInvoice.getDiscountAmount() != null ? selectedInvoice.getDiscountAmount() : BigDecimal.ZERO;
        BigDecimal additionalDiscountAmount = selectedInvoice.getAdditionalDiscount() != null ? selectedInvoice.getAdditionalDiscount() : BigDecimal.ZERO;

        // Calculate VAT using VatCalculator based on subtotal
        BigDecimal netAmountValue = VatCalculator.calculateVat(selectedInvoice.getTotalAmount());


        subTotal.setText(String.format("Subtotal: %.2f", subtotalAmount));
        totalGross.setText(String.format("Total Gross: %.2f", totalGrossAmount));
        lessVolumeDiscount.setText(String.format("Volume Discount: %.2f", volumeDiscountAmount));
        lessTotalDiscount.setText(String.format("Total Discount: %.2f", totalDiscountAmount));
        lessAdditionalDiscount.setText(String.format("Additional Discount: %.2f", additionalDiscountAmount));
        netInvoiceAmount.setText(String.format("Net Invoice Amount: %.2f", netAmountValue));
    }


    private void initializeHeader(SalesInvoice selectedInvoice, String salesInvoiceType) {
        invoiceType.setText(salesInvoiceType);
        number.setText("INVOICE NO: " + selectedInvoice.getOrderId());

        // Extract the LocalDate from LocalDateTime
        LocalDateTime invoiceDate = selectedInvoice.getInvoiceDate().toLocalDateTime();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd"); // Customize the date format as needed
        date.setText(invoiceDate.format(formatter));

        initializeCompany();
        initializeCustomer(selectedInvoice);
    }

    private void initializeCustomer(SalesInvoice selectedInvoice) {
        CustomerDAO customerDAO = new CustomerDAO();
        try {
            Customer selectedCustomer = customerDAO.getCustomer(selectedInvoice.getCustomerId());
            if (selectedCustomer != null) {
                customerName.setText("Customer Name: " + selectedCustomer.getCustomerName());
                customerAddress.setText(String.format("Customer Address: %s, %s, %s",
                        selectedCustomer.getProvince(),
                        selectedCustomer.getCity(),
                        selectedCustomer.getBrgy()));
            } else {
                customerName.setText("Customer Name: Not Available");
                customerAddress.setText("Customer Address: Not Available");
            }
        } catch (Exception e) {
            customerName.setText("Customer Name: Error fetching data");
            customerAddress.setText("Customer Address: Error fetching data");
            e.printStackTrace();
        }
    }


    CompanyDAO companyDAO = new CompanyDAO();

    private void initializeCompany() {
        Company company = companyDAO.getCompanyById(9);
        String companyLogoURL = company.getCompanyLogo();
        Image companyImage;
        if (companyLogoURL != null && !companyLogoURL.isEmpty()) {
            companyImage = new Image(new File(companyLogoURL).toURI().toString());
        } else {
            companyImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/business-and-trade.png")));
        }
        headerCompanyText.setText(company.getCompanyName());
        headerCompanyAddress.setText(company.getCompanyFirstAddress());
        headerCompanyAdditionalDetails.setText(company.getCompanyContact());
        headerLogo.setImage(companyImage);
    }
}
