package com.vertex.vos;

import com.vertex.vos.Constructors.Products;
import com.vertex.vos.Constructors.UserSession;
import com.vertex.vos.Utilities.AuditTrailDatabaseConnectionPool;
import com.vertex.vos.Utilities.DatabaseConnectionPool;
import com.vertex.vos.Utilities.TextFieldUtils;
import com.vertex.vos.Utilities.confirmationAlert;
import com.zaxxer.hikari.HikariDataSource;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class RegisterProductTemplateController implements Initializable, DateSelectedCallback {

    private static final String INSERT_PRODUCT_QUERY = "INSERT INTO products " +
            "(product_name, product_code, price_per_unit, cost_per_unit, description, " +
            "product_discount, quantity_available, date_added, supplier_name, " +
            "priceA, priceB, priceC, product_brand, product_category, unit_of_measurement) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";


    private static final String UPDATE_PRODUCT_QUERY = "UPDATE products SET\n" +
            "    product_name=?,           -- 1\n" +
            "    product_code=?,           -- 2\n" +
            "    price_per_unit=?,         -- 3\n" +
            "    cost_per_unit=?,          -- 4\n" +
            "    description=?,            -- 5\n" +
            "    product_discount=?,       -- 6\n" +
            "    quantity_available=?,     -- 7\n" +
            "    date_added=?,             -- 8\n" +
            "    supplier_name=?,          -- 9\n" +
            "    priceA=?,                 -- 10\n" +
            "    priceB=?,                 -- 11\n" +
            "    priceC=?,                 -- 12\n" +
            "    product_brand=?,          -- 13\n" +
            "    product_category=?,       -- 14\n" +
            "    unit_of_measurement=?     -- 15\n" +
            "WHERE product_id=?            -- 16\n";

    private static final String AUDIT_TRAIL_QUERY = "INSERT INTO audit_trail_table " +
            "(user_id, action, timestamp, table_name, record_id, field_name, old_value, new_value) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    private final Map<String, String> initialFieldValues = new HashMap<>();


    private Products selectedProduct; // Hold the selected product for editing

    @FXML
    private TextField productNameTextField;
    @FXML
    private Label productNameErr;
    @FXML
    private TextField productCodeTextField;
    @FXML
    private Label productCodeErr;
    @FXML
    private TextField pricePerUnitTextField;
    @FXML
    private Label pricePerUnitErr;
    @FXML
    private TextField costPerUnitTextField;
    @FXML
    private Label costPerUnitErr;
    @FXML
    private TextField productDescriptionTextField;
    @FXML
    private Label productDescriptionErr;
    @FXML
    private TextField productDiscountTextField;
    @FXML
    private Label productDiscountErr;
    @FXML
    private TextField quantityAvailableTextField;
    @FXML
    private Label quantityAvailableErr;
    @FXML
    private TextField dateAddedTextField;
    @FXML
    private ImageView datePickerButton;
    @FXML
    private Label dateAddedErr;
    @FXML
    private Label supplierNameErr;
    @FXML
    private Button confirmButton;

    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    private final HikariDataSource auditTrailSource = AuditTrailDatabaseConnectionPool.getDataSource();

    @FXML
    private Label confirmationLabel;
    @FXML
    private TextField priceATextField;
    @FXML
    private TextField priceBTextField;
    @FXML
    private TextField priceCTextField;
    @FXML
    private Label priceAErr;
    @FXML
    private Label priceBErr;
    @FXML
    private Label priceCErr;
    @FXML
    private ComboBox<String> supplierNameComboBox;
    @FXML
    private ComboBox<String> unitOfMeasurementComboBox;
    @FXML
    private ComboBox<String> brandComboBox;
    @FXML
    private ComboBox<String> categoryComboBox;


    public void openCalendarViewOnClick(MouseEvent mouseEvent) {
        openCalendarView();
    }

    public void onDateSelected(LocalDate selectedDate) {
        dateAddedTextField.setText(selectedDate.toString());
    }

    public void initData(Products selectedProduct) {
        this.selectedProduct = selectedProduct;
        if (selectedProduct != null) {
            // Load existing product data into the form fields for editing
            loadProductData(selectedProduct);
            // Change the confirm button text to indicate update
            confirmButton.setText("Update Product");
        }
    }

    private void logAuditTrailEntry(String action, String tableName, int recordId, Map<String, String> columnValues) {
        try (Connection connection = auditTrailSource.getConnection()) {
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());

            for (Map.Entry<String, String> entry : columnValues.entrySet()) {
                String columnName = entry.getKey();
                String newValue = entry.getValue();
                String oldValue = initialFieldValues.get(columnName);

                // Compare old and new values, skip if they are the same
                if (oldValue == null && newValue == null) {
                    continue;
                }
                if (oldValue != null && oldValue.equals(newValue)) {
                    continue;
                }

                try (PreparedStatement preparedStatement = connection.prepareStatement(AUDIT_TRAIL_QUERY)) {
                    preparedStatement.setInt(1, UserSession.getInstance().getUserId());
                    preparedStatement.setString(2, action);
                    preparedStatement.setTimestamp(3, timestamp);
                    preparedStatement.setString(4, tableName);
                    preparedStatement.setInt(5, recordId); // Set the record ID
                    preparedStatement.setString(6, columnName);
                    preparedStatement.setString(7, oldValue);
                    preparedStatement.setString(8, newValue);

                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception appropriately, e.g., log it or show an error message
        }
    }


    private void openCalendarView() {
        // Create a new instance of CalendarView
        CalendarView calendarView = new CalendarView(this);
        Stage stage = new Stage();
        calendarView.start(stage);
    }

    private void populateSupplierNames() {
        // SQL query to fetch supplier names from the suppliers table
        String sqlQuery = "SELECT supplier_name FROM suppliers";

        try (Connection connection = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            ObservableList<String> supplierNames = FXCollections.observableArrayList();

            // Iterate through the result set and add supplier names to the ObservableList
            while (resultSet.next()) {
                String supplierName = resultSet.getString("supplier_name");
                supplierNames.add(supplierName);
            }

            // Set the ObservableList as the items for the supplierTypeComboBox
            supplierNameComboBox.setItems(supplierNames);

        } catch (SQLException e) {
            e.printStackTrace();
            // Handle any SQL exceptions here
        }
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        dateAddedTextField.setPromptText(LocalDate.now().toString());
        populateSupplierNames();
        TextFieldUtils.setComboBoxBehavior(supplierNameComboBox);

        TextFieldUtils.addDoubleInputRestriction(priceATextField);
        TextFieldUtils.addDoubleInputRestriction(priceBTextField);
        TextFieldUtils.addDoubleInputRestriction(priceCTextField);
        TextFieldUtils.addDoubleInputRestriction(costPerUnitTextField);
        TextFieldUtils.addDoubleInputRestriction(pricePerUnitTextField);
        TextFieldUtils.addDoubleInputRestriction(quantityAvailableTextField);
        TextFieldUtils.addDoubleInputRestriction(productDiscountTextField);

        confirmButton.setOnMouseClicked(event -> {
            // Reset error labels
            clearErrorLabels();
            // Validate input fields
            if (validateFields()) {
                confirmationAlert confirmationAlert = new confirmationAlert("Registration Confirmation", "Register " + productNameTextField.getText() + " ?", "todo");
                boolean userConfirmed = confirmationAlert.showAndWait();

                if (userConfirmed) {
                    if (selectedProduct == null) {
                        // Insert a new product
                        if (registerProduct()) {
                            confirmationLabel.setText("Product Registration Successful");
                            confirmationLabel.setTextFill(Color.GREEN);
                            Stage stage = (Stage) confirmationLabel.getScene().getWindow();
                            stage.close();
                        } else {
                            confirmationLabel.setText("Product Registration Failed");
                            confirmationLabel.setTextFill(Color.RED);
                        }
                    } else {
                        // Update an existing product
                        if (updateProduct()) {
                            confirmationLabel.setText("Product Update Successful");
                            confirmationLabel.setTextFill(Color.GREEN);
                            Stage stage = (Stage) confirmationLabel.getScene().getWindow();
                            stage.close();
                        } else {
                            confirmationLabel.setText("Product Update Failed");
                            confirmationLabel.setTextFill(Color.RED);
                        }
                    }
                }
            }
        });

    }

    private boolean validateFields() {
        boolean isValid = true;

        // Product Name validation
        if (productNameTextField.getText().isEmpty()) {
            productNameErr.setText("Product name is required");
            isValid = false;
        }

        // Product Code validation
        if (productCodeTextField.getText().isEmpty()) {
            productCodeErr.setText("Product code is required");
            isValid = false;
        }

        // Price Per Unit validation
        if (pricePerUnitTextField.getText().isEmpty()) {
            pricePerUnitErr.setText("Price per unit is required");
            isValid = false;
        }
        if (priceATextField.getText().isEmpty()) {
            priceAErr.setText("Price A is required");
            isValid = false;
        }
        if (priceBTextField.getText().isEmpty()) {
            priceBErr.setText("Price B is required");
            isValid = false;
        }
        if (priceCTextField.getText().isEmpty()) {
            priceCErr.setText("Price C is required");
            isValid = false;
        }
        // Cost Per Unit validation
        if (costPerUnitTextField.getText().isEmpty()) {
            costPerUnitErr.setText("Cost per unit is required");
            isValid = false;
        }

        // Product Description validation
        if (productDescriptionTextField.getText().isEmpty()) {
            productDescriptionErr.setText("Product description is required");
            isValid = false;
        }

        // Product Discount validation
        if (productDiscountTextField.getText().isEmpty()) {
            productDiscountErr.setText("Product discount is required");
            isValid = false;
        }

        // Quantity Available validation
        if (quantityAvailableTextField.getText().isEmpty()) {
            quantityAvailableErr.setText("Quantity available is required");
            isValid = false;
        }

        // Date Added validation
        if (dateAddedTextField.getText().isEmpty()) {
            dateAddedErr.setText("Date added is required");
            isValid = false;
        }

        // Supplier Name validation
        if (supplierNameComboBox.getSelectionModel().getSelectedItem().isEmpty()) {
            supplierNameErr.setText("Supplier name is required");
            isValid = false;
        }


        return isValid;
    }


    private void clearErrorLabels() {
        productNameErr.setText("");
        productCodeErr.setText("");
        costPerUnitErr.setText("");
        pricePerUnitErr.setText("");
        dateAddedErr.setText("");
        productDescriptionErr.setText("");
        quantityAvailableErr.setText("");
        supplierNameErr.setText("");
        productDiscountErr.setText("");
        priceAErr.setText("");
        priceBErr.setText("");
        priceCErr.setText("");
        // Clear other error labels similarly
    }

    private void loadProductData(Products product) {

        initialFieldValues.put("product_name", product.getProductName());
        initialFieldValues.put("product_code", product.getProductCode());
        initialFieldValues.put("price_per_unit", String.valueOf(product.getPricePerUnit()));
        initialFieldValues.put("cost_per_unit", String.valueOf(product.getCostPerUnit()));
        initialFieldValues.put("description", product.getDescription());
        initialFieldValues.put("product_discount", String.valueOf(product.getProductDiscount()));
        initialFieldValues.put("quantity_available", String.valueOf(product.getQuantityAvailable()));
        initialFieldValues.put("date_added", product.getDateAdded().toString());
        initialFieldValues.put("supplier_name", product.getSupplierName());
        initialFieldValues.put("priceA", String.valueOf(product.getPriceA()));
        initialFieldValues.put("priceB", String.valueOf(product.getPriceB()));
        initialFieldValues.put("priceC", String.valueOf(product.getPriceC()));
        initialFieldValues.put("product_brand", String.valueOf(product.getProductBrand()));
        initialFieldValues.put("product_category", String.valueOf(product.getProductCategory()));
        initialFieldValues.put("unit_of_measurement", String.valueOf(product.getUnitOfMeasurement()));


        productNameTextField.setText(product.getProductName());
        productCodeTextField.setText(product.getProductCode());
        pricePerUnitTextField.setText(String.valueOf(product.getPricePerUnit()));
        costPerUnitTextField.setText(String.valueOf(product.getCostPerUnit()));
        productDescriptionTextField.setText(product.getDescription());
        productDiscountTextField.setText(String.valueOf(product.getProductDiscount()));
        quantityAvailableTextField.setText(String.valueOf(product.getQuantityAvailable()));
        dateAddedTextField.setText(product.getDateAdded().toString());
        supplierNameComboBox.setValue(product.getSupplierName());
        priceATextField.setText(String.valueOf(product.getPriceA()));
        priceBTextField.setText(String.valueOf(product.getPriceB()));
        priceCTextField.setText(String.valueOf(product.getPriceC()));
        brandComboBox.setValue(product.getProductBrand());
        categoryComboBox.setValue(product.getProductCategory());
        unitOfMeasurementComboBox.setValue(product.getUnitOfMeasurement());
    }

    private boolean registerProduct() {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(INSERT_PRODUCT_QUERY)) {
            preparedStatement.setString(1, productNameTextField.getText());
            preparedStatement.setString(2, productCodeTextField.getText());
            preparedStatement.setDouble(3, Double.parseDouble(pricePerUnitTextField.getText()));
            preparedStatement.setDouble(4, Double.parseDouble(costPerUnitTextField.getText()));
            preparedStatement.setString(5, productDescriptionTextField.getText());
            preparedStatement.setDouble(6, Double.parseDouble(productDiscountTextField.getText()));
            preparedStatement.setInt(7, Integer.parseInt(quantityAvailableTextField.getText()));
            preparedStatement.setDate(8, java.sql.Date.valueOf(dateAddedTextField.getText()));
            preparedStatement.setString(9, supplierNameComboBox.getSelectionModel().getSelectedItem());
            preparedStatement.setDouble(10, Double.parseDouble(priceATextField.getText())); // priceA
            preparedStatement.setDouble(11, Double.parseDouble(priceBTextField.getText())); // priceB
            preparedStatement.setDouble(12, Double.parseDouble(priceCTextField.getText())); // priceC
            preparedStatement.setString(13, brandComboBox.getSelectionModel().getSelectedItem());
            preparedStatement.setString(14, categoryComboBox.getSelectionModel().getSelectedItem());
            preparedStatement.setString(15, unitOfMeasurementComboBox.getSelectionModel().getSelectedItem());

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected == 1) {
                Map<String, String> columnValues = getStringMap();

                logAuditTrailEntry("PRODUCT REGISTRATION", "products", selectedProduct.getProductId(), columnValues);

                return true;
            } else {
                return false;
            }

        } catch (SQLException e) {
            e.printStackTrace(); // Log the exception for debugging purposes
            return false;
        }
    }

    private boolean updateProduct() {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_PRODUCT_QUERY)) {
            preparedStatement.setString(1, productNameTextField.getText().trim());           // product_name
            preparedStatement.setString(2, productCodeTextField.getText().trim());           // product_code
            preparedStatement.setDouble(3, Double.parseDouble(pricePerUnitTextField.getText().trim()));          // price_per_unit
            preparedStatement.setDouble(4, Double.parseDouble(costPerUnitTextField.getText().trim()));           // cost_per_unit
            preparedStatement.setString(5, productDescriptionTextField.getText().trim());    // description
            preparedStatement.setDouble(6, Double.parseDouble(productDiscountTextField.getText().trim()));       // product_discount
            preparedStatement.setInt(7, Integer.parseInt(quantityAvailableTextField.getText().trim()));       // quantity_available
            preparedStatement.setDate(8, java.sql.Date.valueOf(dateAddedTextField.getText())); // date_added
            preparedStatement.setString(9, supplierNameComboBox.getSelectionModel().getSelectedItem());          // supplier_name
            preparedStatement.setDouble(10, Double.parseDouble(priceATextField.getText().trim()));               // priceA
            preparedStatement.setDouble(11, Double.parseDouble(priceBTextField.getText().trim()));              // priceB
            preparedStatement.setDouble(12, Double.parseDouble(priceCTextField.getText().trim()));                // priceC
            preparedStatement.setString(13, brandComboBox.getSelectionModel().getSelectedItem());         // product_brand
            preparedStatement.setString(14, categoryComboBox.getSelectionModel().getSelectedItem());     // product_category
            preparedStatement.setString(15, unitOfMeasurementComboBox.getSelectionModel().getSelectedItem());   // unit_of_measurement
            preparedStatement.setInt(16, selectedProduct.getProductId());              // product_id (for WHERE clause)


            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected == 1) {
                Map<String, String> columnValues = getStringMap();
                logAuditTrailEntry("PRODUCT_UPDATE", "products", selectedProduct.getProductId(), columnValues);
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Log the exception for debugging purposes
            return false;
        }
    }

    private Map<String, String> getStringMap() {
        Map<String, String> columnValues = new HashMap<>();
        columnValues.put("product_name", productNameTextField.getText());
        columnValues.put("product_code", productCodeTextField.getText());
        columnValues.put("price_per_unit", pricePerUnitTextField.getText());
        columnValues.put("cost_per_unit", costPerUnitTextField.getText());
        columnValues.put("description", productDescriptionTextField.getText());
        columnValues.put("product_discount", productDiscountTextField.getText());
        columnValues.put("quantity_available", quantityAvailableTextField.getText());
        columnValues.put("date_added", dateAddedTextField.getText());
        columnValues.put("supplier_name", supplierNameComboBox.getSelectionModel().getSelectedItem());
        columnValues.put("priceA", priceATextField.getText());
        columnValues.put("priceB", priceBTextField.getText());
        columnValues.put("priceC", priceCTextField.getText());
        columnValues.put("product_brand", brandComboBox.getSelectionModel().getSelectedItem());
        columnValues.put("product_category", categoryComboBox.getSelectionModel().getSelectedItem());
        columnValues.put("unit_of_measurement", unitOfMeasurementComboBox.getSelectionModel().getSelectedItem());
        return columnValues;
    }
}
