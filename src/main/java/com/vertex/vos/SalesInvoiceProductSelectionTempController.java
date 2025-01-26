package com.vertex.vos;

import com.vertex.vos.DAO.ProductSelectionTempDAO;
import com.vertex.vos.Objects.*;
import com.vertex.vos.Utilities.BrandDAO;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import lombok.Setter;
import org.controlsfx.control.textfield.TextFields;

import java.sql.SQLException;
import java.util.List;

public class SalesInvoiceProductSelectionTempController {

    public TableColumn<SalesInvoiceDetail, String> discountTypeCol;
    public TableColumn<SalesInvoiceDetail, Double> unitPriceCol;
    @FXML
    private TextField brandFilter;

    @FXML
    private Button confirmButton;

    @FXML
    private TableColumn<SalesInvoiceDetail, String> descriptionCol;

    @FXML
    private TextField descriptionFilter;

    @FXML
    private TableColumn<SalesInvoiceDetail, String> productCodeCol;

    @FXML
    private TableColumn<SalesInvoiceDetail, Integer> quantityCol;

    @FXML
    private TableView<SalesInvoiceDetail> salesInvoiceDetailsTableView;

    @FXML
    private TableColumn<SalesInvoiceDetail, String> unitCol;

    @FXML
    private ComboBox<String> unitComboBox;

    BrandDAO brandDAO = new BrandDAO();

    private final ProductSelectionTempDAO productSelectionTempDAO = new ProductSelectionTempDAO();
    private ObservableList<SalesInvoiceDetail> salesInvoiceDetails;
    private int page = 0; // Start at page 0
    private int limit = 35; // Number of records per page

    @Setter
    ObservableList<SalesInvoiceDetail> selectedItems = FXCollections.observableArrayList(); // List to hold selected items <SalesInvoiceDetail>

    @FXML
    public void initialize() {
        ProgressIndicator progressIndicator = new ProgressIndicator();
        salesInvoiceDetailsTableView.setPlaceholder(progressIndicator);
        configureTableColumns();

        ObservableList<String> brandNames = FXCollections.observableArrayList(brandDAO.getBrandNames());
        TextFields.bindAutoCompletion(brandFilter, brandNames);
        Platform.runLater(this::loadSalesInvoiceDetails);

        salesInvoiceDetailsTableView.setOnScroll(event -> {
            if (isScrollNearBottom()) {
                loadNextPage();
            }
        });

        // Add listener for filters
        brandFilter.textProperty().addListener((observable, oldValue, newValue) -> resetPageAndLoad());
        descriptionFilter.textProperty().addListener((observable, oldValue, newValue) -> resetPageAndLoad());
        unitComboBox.valueProperty().addListener((observable, oldValue, newValue) -> resetPageAndLoad());

        salesInvoiceDetailsTableView.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                SalesInvoiceDetail selectedProduct = salesInvoiceDetailsTableView.getSelectionModel().getSelectedItem();
                addSelectedProductToSalesInvoice(selectedProduct);
            }
        });

    }

    private void addSelectedProductToSalesInvoice(SalesInvoiceDetail selectedProduct) {
        // Remove the selected product from the list if it already exists
        salesInvoiceDetails.remove(selectedProduct);

        // Add the selected product to a different list (selectedItems), if necessary
        selectedItems.add(selectedProduct);

        // Set the initial quantity to 0
        selectedProduct.setQuantity(0);

        // Set the unitPrice based on the selected price type (A, B, C, D, or E)
        double price = switch (priceType) {
            case "A" -> selectedProduct.getProduct().getPriceA();
            case "B" -> selectedProduct.getProduct().getPriceB();
            case "C" -> selectedProduct.getProduct().getPriceC();
            case "D" -> selectedProduct.getProduct().getPriceD();
            case "E" -> selectedProduct.getProduct().getPriceE();
            default -> 0; // Default case, should ideally never happen if priceType is always valid
        };

        // Set the unit price of the selected product
        selectedProduct.setUnitPrice(price);

        // Add the selected product to the sales invoice
        salesInvoiceTemporaryController.addProductToSalesInvoice(selectedProduct);
    }

    private void configureTableColumns() {
        // Map TableColumn to SalesInvoiceDetail properties
        descriptionCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduct().getDescription()));
        productCodeCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduct().getProductCode()));
        quantityCol.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getAvailableQuantity()).asObject());
        unitCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduct().getUnitOfMeasurementString()));
        discountTypeCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduct().getDiscountType().getTypeName()));
        unitPriceCol.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getUnitPrice()).asObject());
    }

    private void loadSalesInvoiceDetails() {
        try {
            String brand = brandFilter.getText().trim();
            String description = descriptionFilter.getText().trim();
            String unit = unitComboBox.getValue();
            List<SalesInvoiceDetail> details = productSelectionTempDAO.getSalesInvoiceDetailsForBranch(branchCode, page * limit, limit, brand, description, unit, selectedCustomer);
            salesInvoiceDetails = FXCollections.observableArrayList(details);
            salesInvoiceDetailsTableView.refresh();
            salesInvoiceDetailsTableView.setItems(salesInvoiceDetails);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadNextPage() {
        // Increment the page counter
        page++;

        try {
            // Fetch the next page of data with filters
            String brand = brandFilter.getText().trim();
            String description = descriptionFilter.getText().trim();
            String unit = unitComboBox.getValue();

            List<SalesInvoiceDetail> details = productSelectionTempDAO.getSalesInvoiceDetailsForBranch(branchCode, page * limit, limit, brand, description, unit, selectedCustomer);

            // If no more data is returned, stop loading
            if (details.isEmpty()) {
                return;
            }

            salesInvoiceDetails.addAll(details);

        } catch (SQLException e) {
            e.printStackTrace(); // Handle database errors
            // Optionally, display an error message to the user
        }
    }

    private boolean isScrollNearBottom() {
        ScrollBar scrollBar = (ScrollBar) salesInvoiceDetailsTableView.lookup(".scroll-bar:vertical");
        return scrollBar != null && scrollBar.getValue() >= scrollBar.getMax() - scrollBar.getVisibleAmount();
    }

    // Reset the page and reload data when filters change
    private void resetPageAndLoad() {
        page = 0; // Reset the page to the first page
        loadSalesInvoiceDetails();
    }

    @Setter
    Stage stage;
    @Setter
    SalesInvoiceTemporaryController salesInvoiceTemporaryController;
    @Setter
    String priceType;

    int branchCode;

    public void setBranch(int branchCode) {
        this.branchCode = branchCode;
    }

    @Setter
    Customer selectedCustomer;
}
