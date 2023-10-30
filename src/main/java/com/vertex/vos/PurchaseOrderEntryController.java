package com.vertex.vos;

import com.zaxxer.hikari.HikariDataSource;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class PurchaseOrderEntryController implements Initializable, DateSelectedCallback {
    @FXML
    private ComboBox<String> branch;
    @FXML
    private Label branchErr;
    @FXML
    private ComboBox<String> supplier;
    @FXML
    private Label supplierErr;
    @FXML
    private TextField dateBought;
    @FXML
    private ComboBox<String> product;
    @FXML
    private Label productErr;
    @FXML
    private TextField quantity;
    @FXML
    private ComboBox<String> priceType;
    @FXML
    private Label priceTypeErr;
    @FXML
    private TextField totalAmount;
    @FXML
    private Button confirmButton;
    @FXML
    private Label confirmationLabel;

    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();
    @FXML
    private Label quantityErr;
    @FXML
    private Label totalAmountErr;

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
    }

    private void populateSupplierNames() {
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
    }

    private void populateBranch() {
        {
            // SQL query to fetch supplier names from the suppliers table
            String sqlQuery = "SELECT branch_code FROM branches";

            try (Connection connection = DatabaseConnectionPool.getDataSource().getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
                 ResultSet resultSet = preparedStatement.executeQuery()) {

                ObservableList<String> supplierNames = FXCollections.observableArrayList();

                // Iterate through the result set and add supplier names to the ObservableList
                while (resultSet.next()) {
                    String supplierName = resultSet.getString("branch_code");
                    supplierNames.add(supplierName);
                }

                // Set the ObservableList as the items for the supplierTypeComboBox
                branch.setItems(supplierNames);

            } catch (SQLException e) {
                e.printStackTrace();
                // Handle any SQL exceptions here
            }
        }
    }

    private void populateProduct(String selectedSupplier) {
        // SQL query to fetch product codes and quantity available from the products table based on the selected supplier
        String sqlQuery = "SELECT * FROM products WHERE supplier_name = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {

            preparedStatement.setString(1, selectedSupplier);
            ResultSet resultSet = preparedStatement.executeQuery();

            ObservableList<String> productCodes = FXCollections.observableArrayList();

            // Iterate through the result set and add product codes to the ObservableList
            while (resultSet.next()) {
                String productCode = resultSet.getString("product_code");
                productCodes.add(productCode);
            }

            product.setItems(productCodes);
            product.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    // When a new product is selected, fetch its details from the database
                    getProductDetails(newValue);
                } else {
                    // Handle the case when no product is selected (newValue is null)
                    // You might want to clear the UI elements or display a default message
                    quantityErr.setText("");
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle any SQL exceptions here
        }
    }

    private void getProductDetails(String selectedProductCode) {
        // SQL query to fetch product details (quantity_available, priceA, priceB, priceC) based on the selected product code
        String sqlQuery = "SELECT quantity_available, priceA, priceB, priceC FROM products WHERE product_code = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {

            preparedStatement.setString(1, selectedProductCode);
            ResultSet resultSet = preparedStatement.executeQuery();

            // Check if the result set has data (assuming selectedProductCode is the product code selected by the user)
            if (resultSet.next()) {
                int quantityAvailable = resultSet.getInt("quantity_available");
                double priceA = resultSet.getDouble("priceA");
                double priceB = resultSet.getDouble("priceB");
                double priceC = resultSet.getDouble("priceC");


                quantityErr.setText("Remaining quantity: " + quantityAvailable);
                populatePriceType();

                // Assuming priceType is a ComboBox<String> representing the price types
                priceType.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        // When a new price type is selected, perform calculations based on the selected price type
                        double total = 0.0;
                        try {
                            double quantityValue = Double.parseDouble(quantity.getText());

                            // Calculate total based on the selected price type
                            switch (newValue) {
                                case "priceA":
                                    total = priceA * quantityValue;
                                    break;
                                case "priceB":
                                    total = priceB * quantityValue;
                                    break;
                                case "priceC":
                                    total = priceC * quantityValue;
                                    break;
                                default:
                                    // Handle the case where none of the valid price types are selected
                                    totalAmountErr.setText("Invalid price type");
                                    return;
                            }

                            totalAmount.setText(String.valueOf(total));
                        } catch (NumberFormatException e) {
                            // Handle the case where the quantity is not a valid number
                            totalAmountErr.setText("Invalid quantity");
                        }
                    } else {
                        totalAmount.setText("");
                    }
                });

            }

            // Populate the price type ComboBox
            populatePriceType();

        } catch (SQLException e) {
            e.printStackTrace();
            // Handle any SQL exceptions here
        }

    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        confirmButton.isDefaultButton();
        confirmButton.setDisable(true);

        populateSupplierNames();
        populateBranch();

        dateBought.setPromptText(LocalDate.now().toString());

        TextFieldUtils.setComboBoxBehavior(branch);
        TextFieldUtils.setComboBoxBehavior(supplier);
        TextFieldUtils.setComboBoxBehavior(product);
        TextFieldUtils.setComboBoxBehavior(priceType);

        supplier.setOnAction(event -> {
            String selectedSupplier = supplier.getSelectionModel().getSelectedItem();
            populateProduct(selectedSupplier);
        });

        totalAmount.textProperty().addListener((observable, oldValue, newValue) -> {
            // Enable the button if totalAmount is not empty
            confirmButton.setDisable(newValue.isEmpty()); // Disable the button if totalAmount is empty
        });


        confirmButton.setOnMouseClicked(event -> {
            confirmationInitialization();
        });

        confirmButton.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                confirmationInitialization();
            }
        });

        priceType.setOnAction(event -> {
            String selectedPriceType = priceType.getSelectionModel().getSelectedItem();
            System.out.println("Selected Price Type: " + selectedPriceType);
            // You can do further processing with the selectedPriceType here
        });

    }

    private void confirmationInitialization() {
        String selectedProduct = product.getSelectionModel().getSelectedItem();
        String quantitySelected = quantity.getText();
        String priceTypeSelected = priceType.getSelectionModel().getSelectedItem();
        String totalAmountText = totalAmount.getText();
        confirmationAlert confirmationAlert = new confirmationAlert("Purchase Confirmation", "Confirm purchase of " + selectedProduct + "?", "Quantity: " + quantitySelected + "\nPrice Type: " + priceTypeSelected + "\nTotal Amount: " + totalAmountText);
        boolean userConfirmed = confirmationAlert.showAndWait();
        if (userConfirmed) {
            purchaseOrderConfirmed();
        }
    }


    private double fetchProductPrice(String productCode, String priceType) {
        // SQL query to fetch the price of the selected product based on the product code and price type
        String sqlQuery = "SELECT " + priceType + " FROM products WHERE product_code = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {

            preparedStatement.setString(1, productCode);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getDouble(priceType);
            } else {
                // Handle case when product price is not found
                System.out.println("Product price not found for Product Code: " + productCode);
                return 0.0;
            }

        } catch (SQLException e) {
            // Handle SQL exceptions (print debug info)
            e.printStackTrace();
            return 0.0;
        }
    }


    private void populatePriceType() {
        ObservableList<String> priceTypes = FXCollections.observableArrayList("priceA", "priceB", "priceC");
        priceType.setItems(priceTypes);
    }

    private void purchaseOrderConfirmed() {

    }

}
