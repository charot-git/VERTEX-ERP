package com.vertex.vos;

import com.vertex.vos.Constructors.ComboBoxFilterUtil;
import com.vertex.vos.Constructors.Product;
import com.vertex.vos.Utilities.DatabaseConnectionPool;
import com.vertex.vos.Utilities.TextFieldUtils;
import com.vertex.vos.Utilities.confirmationAlert;
import com.zaxxer.hikari.HikariDataSource;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class PurchaseOrderEntryController implements Initializable, DateSelectedCallback {

    private AnchorPane contentPane; // Declare contentPane variable
    @FXML
    private Label unitOfMeasurement;
    @FXML
    private TableView productsAddedTable;

    private double grandTotals = 0.0; // Variable to store the grand total
    private double vatTotals = 0.0;
    private double withholdingTotals = 0.0;

    @FXML
    private Label grandTotal;
    @FXML
    private CheckBox receiptCheckBox;
    @FXML
    private TableColumn value_added_tax_table;
    @FXML
    private TableColumn withholding_tax;
    @FXML
    private Label withholding;
    @FXML
    private Label vat;
    @FXML
    private HBox totalBox;
    @FXML
    private CheckBox overrideCheckBox;
    @FXML
    private VBox priceInputBox;

    public void setContentPane(AnchorPane contentPane) {
        this.contentPane = contentPane;
    }

    private String type;

    private double vatValue;
    private double withholdingValue;

    private double price;

    public void setPurchaseOrderType(String type) {
        this.type = type;
    }

    private final HistoryManager historyManager = new HistoryManager();

    private int currentNavigationId = -1; // Initialize to a default value

    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();
    @FXML
    private Label purchaseOrderNo;
    @FXML
    private ComboBox receivingType;
    @FXML
    private ComboBox branch;
    @FXML
    private ComboBox supplier;
    @FXML
    private ComboBox priceType;
    @FXML
    private TextField dateBought;
    @FXML
    private ComboBox product;
    @FXML
    private TextField quantity;
    @FXML
    private TextField priceFromSystem;
    @FXML
    private TextField priceInput;
    @FXML
    private Button addProductButton;
    @FXML
    private Button confirmButton;
    @FXML
    private TableColumn code_table;
    @FXML
    private TableColumn name_table;
    @FXML
    private TableColumn quantity_table;
    @FXML
    private TableColumn amount_table;
    @FXML
    private TableColumn total_table;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        unitOfMeasurement.setText("");
        priceType.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                String selectedProduct = (String) product.getSelectionModel().getSelectedItem();
                handleProductPrice(selectedProduct);
            }
        });

        Platform.runLater(() -> {
            if (type.equals("trade")) {
                purchaseOrderNo.setText("PURCHASE FOR TRADE");
            } else {
                purchaseOrderNo.setText("PURCHASE FOR NON-TRADE");
            }
            contentPane.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ENTER) {
                    // Handle Enter key press event (add to purchase)
                    addProductToTable(null);
                }
            });
        });
        populateReceivingType();
        setUpUtils();
        populatePriceType();
        initializeTable();
        initializeTaxes();

        dateBought.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                openCalendarView();
            }
        });

        overrideCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                confirmationAlert confirmationAlert = new confirmationAlert("Price Override",
                        "Are you sure you want to override the price from the database?",
                        "Override price for " + product.getSelectionModel().getSelectedItem() + "?");

                boolean userConfirmed = confirmationAlert.showAndWait();

                if (userConfirmed) {
                    priceInputBox.setVisible(true);
                    priceInput.setDisable(false);
                    priceInput.requestFocus();
                } else {
                    priceInputBox.setVisible(false);
                    priceInput.setDisable(true);
                    product.requestFocus();
                }
            }
        });

    }

    private void initializeTaxes() {
        String query = "SELECT WithholdingRate, VATRate FROM tax_rates WHERE TaxID = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            // Set the TaxID you want to retrieve
            int taxId = 1; // Replace this with the actual TaxID you want to retrieve

            preparedStatement.setInt(1, taxId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                withholdingValue = resultSet.getDouble("WithholdingRate");
                vatValue = resultSet.getDouble("VATRate");
            } else {
                // Handle the case where the specified TaxID was not found
                System.out.println("Tax rates not found for TaxID: " + taxId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle any SQL exceptions here
        }
    }

    private void initializeTable() {
        withholding.setVisible(false);
        vat.setVisible(false);
        totalBox.getChildren().remove(withholding);

        productsAddedTable.getColumns().removeAll(value_added_tax_table, withholding_tax);

        receiptCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                productsAddedTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
                productsAddedTable.getColumns().removeAll(code_table, name_table, amount_table, quantity_table, total_table, value_added_tax_table, withholding_tax);
                productsAddedTable.getColumns().addAll(code_table, name_table, amount_table, quantity_table, total_table, value_added_tax_table, withholding_tax);
                withholding.setVisible(true);
                vat.setVisible(true);

            } else {
                productsAddedTable.getColumns().removeAll(code_table, name_table, amount_table, quantity_table, total_table, value_added_tax_table, withholding_tax);
                productsAddedTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
                productsAddedTable.getColumns().addAll(code_table, name_table, amount_table, quantity_table, total_table);
                withholding.setVisible(false);
                vat.setVisible(false);
            }
        });
    }

    @FXML
    private void openCalendarViewOnClick(MouseEvent mouseEvent) {
        openCalendarView();
    }

    private void openCalendarView() {
        // Create a new instance of CalendarView
        CalendarView calendarView = new CalendarView(this);
        Stage stage = new Stage();
        calendarView.start(stage);
    }

    public void onDateSelected(LocalDate selectedDate) {
        dateBought.setText(selectedDate.toString());
        product.setDisable(false);
    }

    private void populateSupplierNames() {
        supplier.setDisable(false);
        // SQL query to fetch supplier names from the suppliers table
        String sqlQuery = "SELECT supplier_name FROM suppliers";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            ObservableList<String> supplierNames = FXCollections.observableArrayList();

            // Iterate through the result set and add supplier names to the ObservableList
            while (resultSet.next()) {
                String supplierName = resultSet.getString("supplier_name");
                supplierNames.add(supplierName);
            }
            // Set the ObservableList as the items for the supplierTypeComboBox
            supplier.setItems(supplierNames);


        } catch (SQLException e) {
            e.printStackTrace();
        }

        supplier.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            populateProduct(newValue.toString());
        });

    }

    private void populatePriceType() {
        priceType.setDisable(false);
        ObservableList<String> priceOptions = FXCollections.observableArrayList("PriceA", "PriceB", "PriceC");
        priceType.setItems(priceOptions);
    }

    private void populateReceivingType() {
        branch.setDisable(false);
        ObservableList<String> receiving = FXCollections.observableArrayList("Delivery", "Pick Up");
        receivingType.setItems(receiving);

        receivingType.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            populateBranch();
        });


    }

    private void populateBranch() {
        branch.setDisable(false);
        String sqlQuery = "SELECT branch_name FROM branches";

        try (Connection connection = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            ObservableList<String> branches = FXCollections.observableArrayList();

            // Iterate through the result set and add supplier names to the ObservableList
            while (resultSet.next()) {
                String branchName = resultSet.getString("branch_name");
                branches.add(branchName);
            }

            // Set the ObservableList as the items for the supplierTypeComboBox
            branch.setItems(branches);

        } catch (SQLException e) {
            e.printStackTrace();
            // Handle any SQL exceptions here
        }

        branch.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            populateSupplierNames();
        });
    }

    private ObservableList<String> getProductNames(String selectedSupplier) throws SQLException {
        ObservableList<String> productNames = FXCollections.observableArrayList();

        String sqlQuery = "SELECT product_name FROM products WHERE supplier_name = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {

            preparedStatement.setString(1, selectedSupplier);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String productName = resultSet.getString("product_name");
                productNames.add(productName);
            }
        }

        return productNames;
    }

    private void setProductComboBoxItems(String selectedSupplier) {
        ObservableList<String> productNames;
        try {
            productNames = getProductNames(selectedSupplier);
            product.setItems(productNames);
            ComboBoxFilterUtil.setupComboBoxFilter(product, productNames);
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle any SQL exceptions here
        }
    }

    private void addProductSelectionListener() {
        product.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            String selectedProduct = (String) newValue;
            handleProductPrice(selectedProduct);
            getUnitOfMeasurement(selectedProduct);
        });
    }

    private void getUnitOfMeasurement(String selectedProduct) {
        String sqlQuery = "SELECT unit_of_measurement FROM products WHERE product_name = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {

            preparedStatement.setString(1, selectedProduct);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String unit = resultSet.getString("unit_of_measurement");
                unitOfMeasurement.setText(unit);
            } else {
                // Handle the case where the specified product was not found
                System.out.println("Unit of measurement not found for product: " + selectedProduct);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle any SQL exceptions here
        }
    }


    private void populateProduct(String selectedSupplier) {
        priceType.setDisable(false);
        priceInput.setDisable(false);

        setProductComboBoxItems(selectedSupplier);
        addProductSelectionListener();
    }

    private void handleProductPrice(String selectedProduct) {
        String sqlQuery = "SELECT priceA, priceB, priceC, unit_of_measurement FROM products WHERE product_name = ?";
        String selectedPriceType = priceType.getSelectionModel().getSelectedItem().toString();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {

            preparedStatement.setString(1, selectedProduct);
            ResultSet resultSet = preparedStatement.executeQuery();


            if (resultSet.next()) {
                double selectedPrice = 0.0;
                // Determine the selected price based on the selectedPriceType
                if ("PriceA".equals(selectedPriceType)) {
                    selectedPrice = resultSet.getDouble("priceA");
                } else if ("PriceB".equals(selectedPriceType)) {
                    selectedPrice = resultSet.getDouble("priceB");
                } else if ("PriceC".equals(selectedPriceType)) {
                    selectedPrice = resultSet.getDouble("priceC");
                }
                priceFromSystem.setText(String.valueOf(selectedPrice));
            } else {

            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle any SQL exceptions here
        }
    }

    private void setUpUtils() {

        product.setDisable(true);
        priceType.setDisable(true);
        branch.setDisable(true);
        supplier.setDisable(true);
        priceInput.setDisable(true);

        TextFieldUtils.setComboBoxBehavior(receivingType);
        TextFieldUtils.setComboBoxBehavior(branch);
        TextFieldUtils.setComboBoxBehavior(supplier);
        TextFieldUtils.setComboBoxBehavior(product);
        TextFieldUtils.setComboBoxBehavior(priceType);

        TextFieldUtils.addDoubleInputRestriction(quantity);
        TextFieldUtils.addDoubleInputRestriction(priceInput);

    }


    @FXML
    private void addProductToTable(MouseEvent mouseEvent) {
        DecimalFormat decimalFormat = new DecimalFormat("###,###.##");
        String selectedProduct = (String) product.getSelectionModel().getSelectedItem();
        double quantityValue = Double.parseDouble(quantity.getText());


        if (priceInput.getText().isEmpty()){
            price = Double.parseDouble(priceFromSystem.getText());
        }
        else{
            price = Double.parseDouble(priceInput.getText());
        }

        Product productDetails = getProductDetails(selectedProduct);


        if (productDetails != null) {
            // Calculate total based on quantity and price from the system
            double total = quantityValue * price;

            double netRate = 1 + vatValue;
            double netAmount = total / netRate;
            System.out.println(netAmount);
            double vatAmount = netAmount * vatValue; //Value Added Tax
            double withholdingAmount = netAmount * withholdingValue; //Withholding Tax Value

            grandTotals += total;
            vatTotals += vatAmount;
            withholdingTotals += withholdingAmount;

            // 00.00 format
            String Price = decimalFormat.format(price);
            String Total = decimalFormat.format(total);
            String Vat = decimalFormat.format(vatAmount);
            String Withholding = decimalFormat.format(withholdingAmount);
            String GrandTotals = decimalFormat.format(grandTotals);
            String VatTotals = decimalFormat.format(vatTotals);
            String WithholdingTotals = decimalFormat.format(withholdingTotals);
            String Quantity = decimalFormat.format(quantityValue);
            // Set values for the specific columns in the table view
            code_table.setCellValueFactory(new PropertyValueFactory<>("productCode"));
            name_table.setCellValueFactory(new PropertyValueFactory<>("productName"));
            quantity_table.setCellValueFactory(param -> {
                SimpleStringProperty quantityProperty = new SimpleStringProperty(String.valueOf(Quantity));
                quantityProperty.addListener((observable, oldValue, newValue) -> {
                    // Handle changes to the quantity value if necessary
                });
                return quantityProperty;
            });
            amount_table.setCellValueFactory(param -> {
                SimpleStringProperty amountProperty = new SimpleStringProperty(Price);
                amountProperty.addListener((observable, oldValue, newValue) -> {
                });
                return amountProperty;
            });
            total_table.setCellValueFactory(param -> new SimpleStringProperty(String.valueOf(Total)));
            value_added_tax_table.setCellValueFactory(param -> new SimpleStringProperty(String.valueOf(Vat)));
            withholding_tax.setCellValueFactory(param -> new SimpleStringProperty(String.valueOf(Withholding)));

            productsAddedTable.getItems().add(productDetails);

            grandTotal.setText("Grand Total: " + GrandTotals);
            vat.setText("VAT Total: " + VatTotals);
            withholding.setText("EWT Total: " + WithholdingTotals);
            clearInputFields();
        }
    }


    private void clearInputFields() {
        quantity.clear();
        priceFromSystem.clear();
        priceInput.clear();
        product.getSelectionModel().clearSelection();
        product.requestFocus();
    }

    public Product getProductDetails(String productName) {
        Product product = null;
        String sqlQuery = "SELECT * FROM products WHERE product_name = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {

            preparedStatement.setString(1, productName);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                // Retrieve product details from the result set and create a Product object
                product = new Product();
                product.setProductId(resultSet.getInt("product_id"));
                product.setProductName(resultSet.getString("product_name"));
                product.setProductCode(resultSet.getString("product_code"));
                product.setCostPerUnit(resultSet.getDouble("cost_per_unit"));
                product.setPricePerUnit(resultSet.getDouble("price_per_unit"));
                product.setProductDiscount(resultSet.getDouble("product_discount"));
                product.setQuantityAvailable(resultSet.getInt("quantity_available"));
                product.setDescription(resultSet.getString("description"));
                product.setSupplierName(resultSet.getString("supplier_name"));
                product.setDateAdded(resultSet.getDate("date_added"));
                product.setPriceA(String.valueOf(resultSet.getDouble("priceA")));
                product.setPriceB(String.valueOf(resultSet.getDouble("priceB")));
                product.setPriceC(String.valueOf(resultSet.getDouble("priceC")));
                product.setProductUnitOfMeasurement(String.valueOf(resultSet.getString("unit_of_measurement")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle any SQL exceptions here
        }

        return product;
    }
}
