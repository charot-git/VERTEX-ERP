package com.vertex.vos;

import com.vertex.vos.Constructors.*;
import com.vertex.vos.Utilities.*;
import com.zaxxer.hikari.HikariDataSource;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class PurchaseOrderEntryController implements Initializable {
    @FXML
    private Label unitOfMeasurement;
    @FXML
    private Label date;
    @FXML
    private TableView productsAddedTable;
    @FXML
    VBox totalBoxLabels;
    @FXML
    private Label grandTotal;
    @FXML
    private CheckBox receiptCheckBox;
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
    @FXML
    private VBox productsAddBox;
    @FXML
    private VBox POBox;
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
    @FXML
    private TableColumn value_added_tax_table;
    @FXML
    private TableColumn withholding_tax;
    private String type;
    private double vatValue;
    private double withholdingValue;
    private double price;
    private double grandTotals = 0.0; // Variable to store the grand total
    private double vatTotals = 0.0;
    private double withholdingTotals = 0.0;
    private AnchorPane contentPane; // Declare contentPane variable
    private PurchaseOrder selectedPurchaseOrder;

    public void setPurchaseOrderType(String type) {
        this.type = type;
    }

    public void setContentPane(AnchorPane contentPane) {
        this.contentPane = contentPane;
    }

    private final HistoryManager historyManager = new HistoryManager();
    private int currentNavigationId = -1; // Initialize to a default value
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();
    private ObservableList<ProductsInTransact> productsList = FXCollections.observableArrayList();


    private int getNextPurchaseOrderId() {
        String query = "SELECT MAX(purchase_order_id) FROM purchase_order";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            if (resultSet.next()) {
                return resultSet.getInt(1) + 1;
            } else {
                // If no records are found, start from 1
                return 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle any SQL exceptions here
            return -1; // Return an error value
        }
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Platform.runLater(() -> {
            if (type == null) {
                setPurchaseOrder(selectedPurchaseOrder);
            } else {
                encoderUI();
            }
        });
    }

    private void encoderUI() {
        unitOfMeasurement.setText("");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss a");
        LocalDateTime currentDateTime = LocalDateTime.now();
        String formattedDateTime = currentDateTime.format(formatter);

        date.setText(formattedDateTime);
        int nextPurchaseOrderId = getNextPurchaseOrderId();
        if (nextPurchaseOrderId != -1) {
            purchaseOrderNo.setText("PURCHASE ORDER ID " + nextPurchaseOrderId);
        } else {
            // Handle the case where the next ID couldn't be retrieved
            purchaseOrderNo.setText("ERROR: Unable to generate purchase order ID");
        }

        contentPane.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                // Handle Enter key press event (add to purchase)
                addProductToTable(null);
            }
        });
        priceType.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                String selectedProduct = (String) product.getSelectionModel().getSelectedItem();
                handleProductPrice(selectedProduct);
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
        populateReceivingType();
        setUpUtils();
        populatePriceType();
        initializeTable();
        initializeTaxes();
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
        totalBoxLabels.getChildren().removeAll(withholding, vat);

        productsAddedTable.getColumns().removeAll(value_added_tax_table, withholding_tax);

        receiptCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                productsAddedTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
                productsAddedTable.getColumns().removeAll(code_table, name_table, amount_table, quantity_table, total_table, value_added_tax_table, withholding_tax);
                productsAddedTable.getColumns().addAll(code_table, name_table, amount_table, quantity_table, total_table, value_added_tax_table, withholding_tax);
                totalBoxLabels.getChildren().addAll(withholding, vat);

            } else {
                productsAddedTable.getColumns().removeAll(code_table, name_table, amount_table, quantity_table, total_table, value_added_tax_table, withholding_tax);
                productsAddedTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
                productsAddedTable.getColumns().addAll(code_table, name_table, amount_table, quantity_table, total_table);
                totalBoxLabels.getChildren().removeAll(withholding, vat);
            }
        });
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
        product.setDisable(false);
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

    public Product getProductDetails(String productName) {
        ProductDAO productDAO = new ProductDAO();
        return productDAO.getProductDetails(productName);
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


        if (priceInput.getText().isEmpty()) {
            price = Double.parseDouble(priceFromSystem.getText());
        } else {
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

    @FXML
    private void onConfirmButtonClick(MouseEvent mouseEvent) {
        if (confirmButton.getText().equals("Confirm")) {
            entryPO();
        } else if (confirmButton.getText().equals("Approve")) {
            confirmationAlert confirmationAlert = new confirmationAlert("PO" + getNextPurchaseOrderId(), "APPROVE THIS PO??", grandTotal.getText().trim());
            boolean userConfirmed = confirmationAlert.showAndWait();

            if (userConfirmed) {
                approvePO();
            } else {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Cancelled");
                alert.setHeaderText(null);
                alert.setContentText("You have cancelled the purchase order entry");
                alert.showAndWait();
            }
        }
    }

    private void entryPO() {
        confirmationAlert confirmationAlert = new confirmationAlert("PO" + getNextPurchaseOrderId(), "CREATE NEW ENTRY?", grandTotal.getText().trim());
        boolean userConfirmed = confirmationAlert.showAndWait();
        // Get the next purchase order ID
        int nextPurchaseOrderId = getNextPurchaseOrderId();
        // Get values from UI components
        String receiving_type = String.valueOf(receivingType.getSelectionModel().getSelectedItem());
        boolean isInvoiceReceipt = receiptCheckBox.isSelected();
        String branch_name = String.valueOf(branch.getSelectionModel().getSelectedItem());
        String supplier_name = String.valueOf(supplier.getSelectionModel().getSelectedItem());
        String price_type = String.valueOf(priceType.getSelectionModel().getSelectedItem());
        boolean priceOverride = overrideCheckBox.isSelected();

        // Create an instance of PurchaseOrderDAO
        PurchaseOrderDAO purchaseOrderDAO = new PurchaseOrderDAO();
        if (userConfirmed) {
            try {
                purchaseOrderDAO.insertPurchaseOrder(String.valueOf(nextPurchaseOrderId), receiving_type, supplier_name,
                        branch_name, price_type, isInvoiceReceipt, vatTotals, withholdingTotals, grandTotals, type);

                PurchaseOrderProductDAO purchaseOrderProductDAO = new PurchaseOrderProductDAO();

                // Insert purchase order products into the database
                purchaseOrderProductDAO.insertPurchaseOrderProducts(nextPurchaseOrderId, productsList);

            } catch (SQLException e) {
                e.printStackTrace();
                // Handle database exception
                // You can show an error message to the user if the insertion fails
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Cancelled");
            alert.setHeaderText(null);
            alert.setContentText("You have cancelled the purchase order entry");
            alert.showAndWait();
        }
    }

    private void approvePO() {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("UPDATE purchase_order SET approver_id = ?, date_approved = ?, status = ? WHERE purchase_order_no = ?")) {

            int currentUserId = UserSession.getInstance().getUserId(); // Implement this method to get the current user's ID
            preparedStatement.setInt(1, currentUserId);

            LocalDateTime currentDate = LocalDateTime.now();
            preparedStatement.setTimestamp(2, Timestamp.valueOf(currentDate));

            preparedStatement.setString(3, "PENDING");

            preparedStatement.setString(4, purchaseOrderNo.getText());

            int rowsUpdated = preparedStatement.executeUpdate();
            if (rowsUpdated > 0) {
                // Successfully updated the database
                Scene scene = confirmButton.getScene();

                // Close the window associated with the scene
                Stage stage = (Stage) scene.getWindow();
                stage.close();
            } else {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("ERROR");
                alert.setHeaderText(null);
                alert.setContentText("Something went wrong, please try again");
                alert.showAndWait();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            // Handle database exception
        }
    }

    public void setPurchaseOrder(PurchaseOrder selectedPurchaseOrder) {
        this.selectedPurchaseOrder = selectedPurchaseOrder;

        setUpUneditableUtils();

        int poId = selectedPurchaseOrder.getId();

        if (poId != -1) {
            try (Connection connection = dataSource.getConnection()) {
                String query = "SELECT * FROM purchase_order WHERE purchase_order_id = ?";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setInt(1, poId);
                ResultSet resultSet = statement.executeQuery();

                if (resultSet.next()) {
                    // Retrieve all columns from the ResultSet
                    int purchaseOrderId = resultSet.getInt("purchase_order_id");
                    String purchase_order_no = resultSet.getString("purchase_order_no");
                    String supplier_name = resultSet.getString("supplier_name");
                    String receiving_type = resultSet.getString("receiving_type");
                    String branch_name = resultSet.getString("branch_name");
                    String price_type = resultSet.getString("price_type");
                    boolean receipt_required = resultSet.getBoolean("receipt_required");
                    BigDecimal vat_amount = resultSet.getBigDecimal("vat_amount");
                    BigDecimal withholding_tax_amount = resultSet.getBigDecimal("withholding_tax_amount");
                    Timestamp date_encoded = resultSet.getTimestamp("date_encoded");
                    BigDecimal total_amount = resultSet.getBigDecimal("total_amount");
                    int encoder_id = resultSet.getInt("encoder_id");
                    int approver_id = resultSet.getInt("approver_id");
                    String transaction_type = resultSet.getString("transaction_type");
                    Timestamp date_approved = resultSet.getTimestamp("date_approved");
                    Timestamp date_received = resultSet.getTimestamp("date_received");
                    boolean isOverride = resultSet.getBoolean("isOverride");
                    String status = resultSet.getString("status");

                    purchaseOrderNo.setText(purchase_order_no);
                    receivingType.setValue(receiving_type);
                    branch.setValue(branch_name);
                    supplier.setValue(supplier_name);
                    priceType.setValue(price_type);
                    initializeTableForChecking(receipt_required);
                    overrideCheckBox.setSelected(isOverride);

                    populateTableOfProductsPerPO(poId);

                    grandTotal.setText("Grand Total: " + total_amount);
                    vat.setText("VAT Total: " + vat_amount);
                    withholding.setText("EWT Total: " + withholding_tax_amount);
                    confirmButton.setText("Approve");
                } else {
                    // Handle the case where no record was found for the given purchase order ID
                    System.out.println("No purchase order found for ID: " + poId);
                }
            } catch (SQLException e) {
                e.printStackTrace(); // Handle the exception according to your needs
            }
        }

    }

    private void populateTableOfProductsPerPO(int poId) {
        ObservableList<ProductsInTransact> productList = FXCollections.observableArrayList();

        try (Connection connection = dataSource.getConnection()) {
            String query = "SELECT * FROM purchase_order_products WHERE purchase_order_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, poId);
            ResultSet resultSet = statement.executeQuery();

            // Process the resultSet and populate the productList
            while (resultSet.next()) {
                int productId = resultSet.getInt("product_id");
                double quantity = resultSet.getDouble("ordered_quantity");
                double vatAmount = resultSet.getDouble("vat_amount");
                double withholdingAmount = resultSet.getDouble("withholding_amount");
                double totalAmount = resultSet.getDouble("total_amount");

                // Get product name based on product ID using the getProductName method
                String productName = getProductName(productId);

            }

            // Bind the productList to your TableView
            productsAddedTable.setItems(productList);

            // Bind the columns to the corresponding properties of the ProductsInTransact class
            code_table.setCellValueFactory(new PropertyValueFactory<>("productId"));
            name_table.setCellValueFactory(new PropertyValueFactory<>("productOrderNo"));
            quantity_table.setCellValueFactory(new PropertyValueFactory<>("quantity"));
            amount_table.setCellValueFactory(new PropertyValueFactory<>("vatAmount"));
            total_table.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
            value_added_tax_table.setCellValueFactory(new PropertyValueFactory<>("vatAmount"));
            withholding_tax.setCellValueFactory(new PropertyValueFactory<>("withholdingAmount"));

        } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception according to your needs
        }
    }


    private String getProductName(int productId) {
        String productName = null;

        try (Connection connection = dataSource.getConnection()) {
            String query = "SELECT product_name FROM products WHERE product_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, productId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                productName = resultSet.getString("product_name");
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception according to your needs
        }

        return productName;
    }


    private void initializeTableForChecking(boolean b) {
        withholding.setVisible(false);
        vat.setVisible(false);
        totalBox.getChildren().remove(withholding);

        productsAddedTable.getColumns().removeAll(value_added_tax_table, withholding_tax);

        if (b) {
            receiptCheckBox.setSelected(true);
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
    }

    private void setUpUneditableUtils() {
        POBox.getChildren().remove(productsAddBox);

        receivingType.setDisable(true);
        receiptCheckBox.setDisable(true);
        branch.setDisable(true);
        supplier.setDisable(true);
        priceType.setDisable(true);
        overrideCheckBox.setDisable(true);
    }
}
