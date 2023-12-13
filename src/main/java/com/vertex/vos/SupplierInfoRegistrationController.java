package com.vertex.vos;

import com.vertex.vos.Constructors.DiscountType;
import com.vertex.vos.Constructors.Product;
import com.vertex.vos.Constructors.Supplier;
import com.vertex.vos.Utilities.*;
import com.zaxxer.hikari.HikariDataSource;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;


import java.io.*;
import java.net.URL;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

import static com.vertex.vos.Utilities.TextFieldUtils.addNumericInputRestriction;

public class SupplierInfoRegistrationController implements Initializable, DateSelectedCallback {

    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    DiscountDAO discountDAO = new DiscountDAO();

    private String selectedFilePath;

    private Supplier selectedSupplier;

    private boolean logoPicked = false;

    @FXML
    private TextField dateAddedTextField;
    @FXML
    private ImageView supplierLogo;
    @FXML
    private Label supplierNameHeaderLabel;
    @FXML
    private TextField supplierNameTextField;
    @FXML
    private Label supplierNameErr;
    @FXML
    private Label businessTypeLabel;
    @FXML
    private TextField supplierContactPersonTextField;
    @FXML
    private Label supplierContactPersonErr;
    @FXML
    private TextField supplierEmailTextField;
    @FXML
    private Label supplierEmailErr;
    @FXML
    private TextField supplierContactNoTextField;
    @FXML
    private Label supplierContactNoErr;
    @FXML
    private Label provinceErr;
    @FXML
    private Label cityErr;
    @FXML
    private Label baranggayErr;
    @FXML
    private TextField postalCodeTextField;
    @FXML
    private Label postalCodeErr;
    @FXML
    private Label dateAddedErr;
    @FXML
    private TextField supplierTypeTextField;
    @FXML
    private Label supplierTypeErr;
    @FXML
    private TextField tinNumberTextField;
    @FXML
    private Label tinNumberErr;
    @FXML
    private TextField bankDetailsTextField;
    @FXML
    private Label bankDetailsErr;
    @FXML
    private TextField productAndServicesTextField;
    @FXML
    private Label productAndServicesErr;
    @FXML
    private Label paymentTermsErr;
    @FXML
    private Label deliveryTermsErr;
    @FXML
    private TextField agreementContractTextField;
    @FXML
    private Label agreementContractErr;
    @FXML
    private Button chooseLogoButton;
    @FXML
    private Label preferredCommunicationMethodErr;
    @FXML
    private TextField preferredCommunicationMethodTextField;
    @FXML
    private TextField notesOrCommentsTextField;
    @FXML
    private Label notesOrCommentsErr;
    @FXML
    private Button confirmButton;
    @FXML
    private Label confirmationLabel;
    @FXML
    private ComboBox<String> provinceComboBox;
    @FXML
    private ComboBox<String> cityComboBox;
    @FXML
    private ComboBox<String> baranggayComboBox;
    @FXML
    private ComboBox<String> supplierTypeComboBox;
    @FXML
    private ComboBox<String> paymentTermsComboBox;
    @FXML
    private ComboBox<String> deliveryTermsComboBox;
    @FXML
    private VBox addProduct;
    @FXML
    private TableView productList;
    @FXML
    private ComboBox discountTypeComboBox;
    @FXML
    private Label discountTypeErr;


    @FXML
    private void openCalendarView() {
        // Create a new instance of CalendarView
        CalendarView calendarView = new CalendarView(this);
        Stage stage = new Stage();
        calendarView.start(stage);
    }

    public void openCalendarViewOnClick(MouseEvent mouseEvent) {
        openCalendarView();
    }

    public void onDateSelected(LocalDate selectedDate) {
        dateAddedTextField.setText(selectedDate.toString());
    }

    @FXML
    private void onSupplierLogoClicked(MouseEvent event) {
        // Handle the click event for the supplierLogo ImageView
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif"));

        // Show open file dialog
        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            // Store the file path of the selected image
            selectedFilePath = selectedFile.getAbsolutePath();

            // Load the selected image and apply it to the supplierLogo ImageView
            Image image = new Image(selectedFile.toURI().toString());
            supplierLogo.setImage(image);
            logoPicked = true;
        }
    }

    private void setErrorMessage(Label errorLabel, String errorMessage) {
        errorLabel.setText(errorMessage);
        errorLabel.setTextFill(Color.RED); // Set text color to red
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setComboBoxBehaviours();
        populateComboBoxes();

        initializeAddress();

        addNumericInputRestriction(supplierContactNoTextField);
        addNumericInputRestriction(tinNumberTextField);
        addNumericInputRestriction(postalCodeTextField);
        addNumericInputRestriction(bankDetailsTextField);


        dateAddedTextField.setPromptText(LocalDate.now().toString());

        supplierNameTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            supplierNameHeaderLabel.setText(newValue);
        });


        confirmButton.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                initiateRegistration();
            }
        });

        confirmButton.setOnMouseClicked(event -> {
            if (confirmButton.getText().equals("Update Supplier")) {
                initiateUpdate();
            } else {
                initiateRegistration();
            }
        });
    }

    private void initiateUpdate() {
        String errorMessage = validateFields();

        if (errorMessage.isEmpty()) {
            ConfirmationAlert confirmationAlert = new ConfirmationAlert(selectedSupplier.getSupplierType(), "Update this supplier?", "Yes or No?");

            boolean userConfirmed = confirmationAlert.showAndWait();

            if (userConfirmed) {
                updateSupplier();
            } else {
                ToDoAlert.showToDoAlert();
            }
        } else {
            System.out.println("Validation Errors:\n" + errorMessage);
        }
    }

    private void updateSupplier() {
        String updateQuery = "UPDATE suppliers SET " +
                "supplier_image = ?, " +
                "date_added = ?, " +
                "agreement_or_contract = ?, " +
                "notes_or_comments = ?, " +
                "discount_type = ?, " + //
                "address = ?, " +
                "bank_details = ?, " +
                "brgy = ?, " +
                "city = ?, " +
                "contact_person = ?, " +
                "country = ?, " +
                "delivery_terms = ?, " +
                "email_address = ?, " +
                "payment_terms = ?, " +
                "phone_number = ?, " +
                "postal_code = ?, " +
                "preferred_communication_method = ?, " +
                "state_province = ?, " +
                "supplier_name = ?, " +
                "supplier_type = ? " +
                "WHERE id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)) {

            File imageFile = new File(selectedFilePath);
            FileInputStream fis = new FileInputStream(imageFile);
            String address = provinceComboBox.getSelectionModel().getSelectedItem() + " " + cityComboBox.getSelectionModel().getSelectedItem() + " " + baranggayComboBox.getSelectionModel().getSelectedItem();
            // Set parameters for the update query
            preparedStatement.setBinaryStream(1, fis, (int) imageFile.length()); // Set supplier image as a blob
            preparedStatement.setDate(2, Date.valueOf(dateAddedTextField.getText()));
            preparedStatement.setString(3, agreementContractTextField.getText());
            preparedStatement.setString(4, notesOrCommentsTextField.getText());
            preparedStatement.setInt(5, discountDAO.getDiscountTypeIdByName((String) discountTypeComboBox.getSelectionModel().getSelectedItem()));
            preparedStatement.setString(6, address);
            preparedStatement.setString(7, bankDetailsTextField.getText());
            preparedStatement.setString(8, baranggayComboBox.getSelectionModel().getSelectedItem());
            preparedStatement.setString(9, cityComboBox.getSelectionModel().getSelectedItem());
            preparedStatement.setString(10, supplierContactPersonTextField.getText());
            preparedStatement.setString(11, "Philippines");
            preparedStatement.setString(12, deliveryTermsComboBox.getSelectionModel().getSelectedItem());
            preparedStatement.setString(13, supplierEmailTextField.getText());
            preparedStatement.setString(14, paymentTermsComboBox.getSelectionModel().getSelectedItem());
            preparedStatement.setString(15, supplierContactNoTextField.getText());
            preparedStatement.setString(16, postalCodeTextField.getText());
            preparedStatement.setString(17, preferredCommunicationMethodTextField.getText());
            preparedStatement.setString(18, provinceComboBox.getSelectionModel().getSelectedItem());
            preparedStatement.setString(19, supplierNameTextField.getText());
            preparedStatement.setString(20, supplierTypeComboBox.getSelectionModel().getSelectedItem());
            preparedStatement.setInt(21, selectedSupplier.getId()); // Assuming idTextField contains the supplier ID

            // Execute the update query
            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                confirmationLabel.setText("Supplier updated successfully!");
                Stage stage = (Stage) confirmationLabel.getScene().getWindow();
                stage.close();
            } else {
                confirmationLabel.setText("Failed to update supplier. Please check the ID and try again.");
            }

        } catch (SQLException | FileNotFoundException | NumberFormatException e) {
            e.printStackTrace();
            confirmationLabel.setText("Error occurred while updating supplier.");
        }

    }


    private void setComboBoxBehaviours() {
        TextFieldUtils.setComboBoxBehavior(supplierTypeComboBox);
        TextFieldUtils.setComboBoxBehavior(provinceComboBox);
        TextFieldUtils.setComboBoxBehavior(cityComboBox);
        TextFieldUtils.setComboBoxBehavior(baranggayComboBox);
        TextFieldUtils.setComboBoxBehavior(deliveryTermsComboBox);
        TextFieldUtils.setComboBoxBehavior(paymentTermsComboBox);
        TextFieldUtils.setComboBoxBehavior(discountTypeComboBox);
    }

    private void populateComboBoxes() {
        //initializeAddress();
        populatePaymentTerms();
        populateDeliveryTerms();
        populateSupplierTypes();
        populateDiscountTypes();
    }

    private void populateDiscountTypes() {
        try {
            List<String> discountTypeNames = discountDAO.getAllDiscountTypeNames();
            ObservableList<String> observableDiscountTypeNames = FXCollections.observableArrayList(discountTypeNames);
            discountTypeComboBox.setItems(observableDiscountTypeNames);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    private void initiateRegistration() {
        String errorMessage = validateFields();

        if (errorMessage.isEmpty()) {
            ConfirmationAlert confirmationAlert = new ConfirmationAlert("Registration Confirmation", "Register " + supplierNameTextField.getText() + " ?", "todo");
            boolean userConfirmed = confirmationAlert.showAndWait();
            if (userConfirmed) {
                registerSupplier();
            }
        } else {
            // Display the error message to the user (for example, in a dialog box)
            System.out.println("Validation Errors:\n" + errorMessage);
        }

    }

    private void populateSupplierTypes() {
        // SQL query to fetch supplier types from the categories table
        String sqlQuery = "SELECT supplier_type FROM suppliers_categories";

        try (Connection connection = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            ObservableList<String> supplierTypes = FXCollections.observableArrayList();

            // Iterate through the result set and add supplier types to the ObservableList
            while (resultSet.next()) {
                String supplierType = resultSet.getString("supplier_type");
                supplierTypes.add(supplierType);
            }

            // Set the ObservableList as the items for the supplierTypeComboBox
            supplierTypeComboBox.setItems(supplierTypes);

        } catch (SQLException e) {
            e.printStackTrace();
            // Handle any SQL exceptions here
        }
    }

    private void populatePaymentTerms() {
        // SQL query to fetch supplier types from the categories table
        String sqlQuery = "SELECT * FROM payment_terms";

        try (Connection connection = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            ObservableList<String> paymentTypes = FXCollections.observableArrayList();

            while (resultSet.next()) {
                String paymentName = resultSet.getString("payment_name");
                paymentTypes.add(paymentName);
            }
            paymentTermsComboBox.setItems(paymentTypes);

        } catch (SQLException e) {
            e.printStackTrace();
            // Handle any SQL exceptions here
        }
    }

    private void populateDeliveryTerms() {
        // SQL query to fetch supplier types from the categories table
        String sqlQuery = "SELECT * FROM delivery_terms";

        try (Connection connection = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            ObservableList<String> paymentTypes = FXCollections.observableArrayList();

            // Iterate through the result set and add supplier types to the ObservableList
            while (resultSet.next()) {
                String deliveryName = resultSet.getString("delivery_name");
                paymentTypes.add(deliveryName);
            }

            // Set the ObservableList as the items for the supplierTypeComboBox
            deliveryTermsComboBox.setItems(paymentTypes);

        } catch (SQLException e) {
            e.printStackTrace();
            // Handle any SQL exceptions here
        }
    }

    private void initializeAddress() {
        Map<String, String> provinceData = LocationCache.getProvinceData();
        Map<String, String> cityData = LocationCache.getCityData();
        Map<String, String> barangayData = LocationCache.getBarangayData();

        // Populate provinceComboBox with province codes and names
        provinceComboBox.setItems(FXCollections.observableArrayList(provinceData.values()));

        // Add listener to populate cityComboBox based on selected province
        provinceComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            String selectedProvinceCode = getKeyFromValue(provinceData, newValue);
            List<String> citiesInProvince = filterLocationsByParentCode(cityData, selectedProvinceCode);
            cityComboBox.setItems(FXCollections.observableArrayList(citiesInProvince));
        });

        // Add listener to populate baranggayComboBox based on selected city
        cityComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            String selectedCityCode = getKeyFromValue(cityData, newValue);
            List<String> barangaysInCity = filterLocationsByParentCode(barangayData, selectedCityCode);
            baranggayComboBox.setItems(FXCollections.observableArrayList(barangaysInCity));
        });
    }

    // Helper method to get the key (code) corresponding to a specific value (name) from the map
    private String getKeyFromValue(Map<String, String> map, String value) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (entry.getValue().equals(value)) {
                return entry.getKey();
            }
        }
        return null;
    }

    // Helper method to filter locations based on the parent code
    private List<String> filterLocationsByParentCode(Map<String, String> locationData, String parentCode) {
        List<String> filteredLocations = new ArrayList<>();
        for (Map.Entry<String, String> entry : locationData.entrySet()) {
            String code = entry.getKey();
            String parentCodeOfLocation = getParentCode(code);
            if (parentCodeOfLocation.equals(parentCode)) {
                filteredLocations.add(entry.getValue());
            }
        }
        return filteredLocations;
    }

    // Helper method to extract the parent code from the location code (assuming a specific format)
    private String getParentCode(String code) {
        // Check the length of the input code to determine if it's a barangay or city code
        if (code.length() == 9) {
            // Barangay code format: "104213016" (province_code + city_code + barangay_code)
            return code.substring(0, 6); // Extract city code from the barangay code
        } else if (code.length() == 6) {
            // City code format: "054112" (province_code + city_code)
            return code.substring(0, 4); // Extract province code from the city code
        } else {
            // Handle unknown code format, or throw an exception if appropriate
            return null;
        }
    }


    private String getAddress() {
        String province = getSelectedProvince();
        String city = getSelectedCity();
        String barangay = getSelectedBarangay();
        return province + ", " + city + ", " + barangay;
    }

    private String getSelectedProvince() {
        return provinceComboBox.getSelectionModel().getSelectedItem();
    }

    private String getSelectedCity() {
        return cityComboBox.getSelectionModel().getSelectedItem();
    }

    private String getSelectedBarangay() {
        return baranggayComboBox.getSelectionModel().getSelectedItem();
    }

    private String validateFields() {
        StringBuilder errorMessage = new StringBuilder();

        String supplierName = supplierNameTextField.getText().trim();
        String contactPerson = supplierContactPersonTextField.getText().trim();
        String emailAddress = supplierEmailTextField.getText().trim();
        String contactNo = supplierContactNoTextField.getText().trim();
        String province = getSelectedProvince();
        String city = getSelectedCity();
        String baranggay = getSelectedBarangay();
        String address = getAddress();
        String postalCode = postalCodeTextField.getText().trim();
        String dateAdded = dateAddedTextField.getText().trim();
        String category = supplierTypeComboBox.getSelectionModel().getSelectedItem();
        String tin = tinNumberTextField.getText().trim();

        if (!logoPicked) {
            chooseLogoButton.setTextFill(Color.RED);
            errorMessage.append("Logo is required.\n");
        }

        if (!TextFieldUtils.isNumeric(tin)) {
            errorMessage.append("TIN should be numerical.\n");
            setErrorMessage(tinNumberErr, "TIN should be numerical");
            tinNumberTextField.requestFocus();
        }
        if (!TextFieldUtils.isNumeric(contactNo)) {
            errorMessage.append("Contact Number should be numerical.\n");
            setErrorMessage(supplierContactNoErr, "Contact Number should be numerical");
            supplierContactNoTextField.requestFocus();
        }
        if (!TextFieldUtils.isNumeric(postalCode)) {
            errorMessage.append("Postal Code should be numerical.\n");
            setErrorMessage(postalCodeErr, "Postal Code should be numerical");
            postalCodeTextField.requestFocus();
        }

        if (supplierName.isEmpty()) {
            errorMessage.append("Supplier Name is required.\n");
            setErrorMessage(supplierNameErr, "Supplier Name is required");
            supplierNameTextField.requestFocus();
        }

        if (contactPerson.isEmpty()) {
            errorMessage.append("Contact Person is required.\n");
            setErrorMessage(supplierContactPersonErr, "Contact Person is required");
            supplierContactPersonTextField.requestFocus();
        }

// Validate email address format
        if (!emailAddress.matches("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")) {
            errorMessage.append("Invalid Email Address.\n");
            setErrorMessage(supplierEmailErr, "Invalid Email Address");
            supplierEmailTextField.requestFocus();
        }
        if (province == null || province.trim().isEmpty()) {
            errorMessage.append("Province is required.\n");
            setErrorMessage(provinceErr, "Province is required");
            if (provinceComboBox.getSelectionModel().isEmpty()) {
                provinceComboBox.requestFocus();
            }
        }
        if (city == null || city.trim().isEmpty()) {
            errorMessage.append("City is required.\n");
            setErrorMessage(cityErr, "City is required");
            if (cityComboBox.getSelectionModel().isEmpty()) {
                cityComboBox.requestFocus();
            }
        }
        if (baranggay == null || baranggay.trim().isEmpty()) {
            errorMessage.append("Baranggay is required.\n");
            setErrorMessage(baranggayErr, "Baranggay is required");
            if (baranggayComboBox.getSelectionModel().isEmpty()) {
                baranggayComboBox.requestFocus();
            }
        }

// Validate phone number format
        if (!contactNo.matches("\\d{11}")) {
            errorMessage.append("Invalid Contact Number.\n");
            setErrorMessage(supplierContactNoErr, "Invalid Contact Number");
            supplierContactNoTextField.requestFocus();
        }

// Validate date format (assuming MM/DD/YYYY format)
        if (!TextFieldUtils.isValidDate(dateAdded)) {
            errorMessage.append("Invalid Date Format. Use YYYY-MM-DD.\n");
            setErrorMessage(dateAddedErr, "Invalid Date Format. Use YYYY-MM-DD");
            dateAddedTextField.requestFocus();
        }

        if (postalCode.isEmpty()) {
            errorMessage.append("Postal Code is required.\n");
            setErrorMessage(postalCodeErr, "Postal Code is required");
            postalCodeTextField.requestFocus();
        }

        if (category == null || category.trim().isEmpty()) {
            errorMessage.append("Category is required.\n");
            setErrorMessage(supplierTypeErr, "Category is required");
            if (supplierTypeComboBox.getSelectionModel().isEmpty()) {
                supplierTypeComboBox.requestFocus();
            }
        }

        if (tin.isEmpty()) {
            errorMessage.append("TIN is required.\n");
            setErrorMessage(tinNumberErr, "TIN is required");
            tinNumberTextField.requestFocus();
        }

// Additional validations can be added here for other fields if needed

        return errorMessage.toString();

    }


    private void registerSupplier() {
        String insertQuery = "INSERT INTO suppliers (supplier_image, date_added, agreement_or_contract, notes_or_comments, " +
                "products_or_services, address, bank_details, brgy, city, contact_person, country, delivery_terms, " +
                "email_address, payment_terms, phone_number, postal_code, preferred_communication_method, state_province, " +
                "supplier_name, supplier_type, tin_number) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {

            // Set values for the SQL query parameters
            File imageFile = new File(selectedFilePath);
            FileInputStream fis = new FileInputStream(imageFile);
            preparedStatement.setBinaryStream(1, fis, (int) imageFile.length()); // Set supplier image as a blob

            preparedStatement.setDate(2, java.sql.Date.valueOf(LocalDate.parse(dateAddedTextField.getText()))); // Assuming dateAddedTextField contains a valid date string in the format "YYYY-MM-DD"
            preparedStatement.setString(3, agreementContractTextField.getText());
            preparedStatement.setString(4, notesOrCommentsTextField.getText());
            preparedStatement.setString(5, productAndServicesTextField.getText());
            preparedStatement.setString(6, getSelectedProvince() + ", " + getSelectedCity() + ", " + getSelectedBarangay());
            preparedStatement.setString(7, bankDetailsTextField.getText());
            preparedStatement.setString(8, getSelectedBarangay());
            preparedStatement.setString(9, getSelectedCity());
            preparedStatement.setString(10, supplierContactPersonTextField.getText());
            preparedStatement.setString(11, "Philippines"); // Set country if available
            preparedStatement.setString(12, deliveryTermsComboBox.getSelectionModel().getSelectedItem());
            preparedStatement.setString(13, supplierEmailTextField.getText());
            preparedStatement.setString(14, paymentTermsComboBox.getSelectionModel().getSelectedItem());
            preparedStatement.setString(15, supplierContactNoTextField.getText());
            preparedStatement.setString(16, postalCodeTextField.getText());
            preparedStatement.setString(17, preferredCommunicationMethodTextField.getText());
            preparedStatement.setString(18, getSelectedProvince());
            preparedStatement.setString(19, supplierNameTextField.getText());
            preparedStatement.setString(20, supplierTypeComboBox.getSelectionModel().getSelectedItem());
            preparedStatement.setString(21, tinNumberTextField.getText());

            // Execute the query
            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                confirmationLabel.setText("Supplier registered successfully!");
                Stage stage = (Stage) confirmationLabel.getScene().getWindow();
                stage.close();
                // You can perform additional actions upon successful registration if needed
            } else {
                confirmationLabel.setText("Failed to register supplier. Please try again.");
            }

        } catch (SQLException | IOException e) {
            e.printStackTrace();
            confirmationLabel.setText("Error occurred while registering supplier.");
        }
    }


    public void initData(Supplier selectedSupplier) {
        this.selectedSupplier = selectedSupplier;
        if (selectedSupplier != null) {
            // Load existing product data into the form fields for editing
            loadSelectedSupplier(selectedSupplier);
            // Change the confirm button text to indicate update
            confirmButton.setText("Update Supplier");
        }

        addProduct.setOnMouseClicked(mouseEvent -> addProductToSupplierTable(selectedSupplier.getSupplierName()));
        populateSupplierProducts(selectedSupplier.getId());
    }

    private void populateSupplierProducts(int supplierId) {
        ProductsPerSupplierDAO productsPerSupplierDAO = new ProductsPerSupplierDAO();
        List<Integer> supplierProducts = productsPerSupplierDAO.getProductsForSupplier(supplierId);

        TableColumn<Product, Integer> productIdColumn = new TableColumn<>("Product ID");
        productIdColumn.setCellValueFactory(new PropertyValueFactory<>("productId"));

        TableColumn<Product, String> productNameColumn = new TableColumn<>("Product Name");
        productNameColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));

        TableColumn<Product, String> productDescriptionColumn = new TableColumn<>("Description");
        productDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        TableColumn<Product, String> productShortDescriptionColumn = new TableColumn<>("Short Description");
        productShortDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("shortDescription"));

        TableColumn<Product, String> productBrandStringColumn = new TableColumn<>("Brand");
        productBrandStringColumn.setCellValueFactory(new PropertyValueFactory<>("productBrandString"));

        TableColumn<Product, String> productCategoryStringColumn = new TableColumn<>("Category");
        productCategoryStringColumn.setCellValueFactory(new PropertyValueFactory<>("productCategoryString"));

        TableColumn<Product, String> productClassStringColumn = new TableColumn<>("Class");
        productClassStringColumn.setCellValueFactory(new PropertyValueFactory<>("productClassString"));

        TableColumn<Product, String> productSegmentStringColumn = new TableColumn<>("Segment");
        productSegmentStringColumn.setCellValueFactory(new PropertyValueFactory<>("productSegmentString"));

        TableColumn<Product, String> productNatureStringColumn = new TableColumn<>("Nature");
        productNatureStringColumn.setCellValueFactory(new PropertyValueFactory<>("productNatureString"));

        TableColumn<Product, String> productSectionStringColumn = new TableColumn<>("Section");
        productSectionStringColumn.setCellValueFactory(new PropertyValueFactory<>("productSectionString"));

        TableColumn<Product, String> productDiscountColumn = getProductDiscountColumn();


        productList.getColumns().addAll(productNameColumn, productDescriptionColumn, productShortDescriptionColumn, productBrandStringColumn,
                productCategoryStringColumn,
                productClassStringColumn,
                productSegmentStringColumn,
                productNatureStringColumn,
                productSectionStringColumn,
                productDiscountColumn);

        ObservableList<Product> productsData = FXCollections.observableArrayList();
        ProductDAO productDAO = new ProductDAO();
        for (Integer productId : supplierProducts) {
            Product product = productDAO.getProductDetails(productId);
            if (product != null) {
                productsData.add(product);
            }
        }
        productList.setItems(productsData);
    }

    private TableColumn<Product, String> getProductDiscountColumn() {
        TableColumn<Product, String> productDiscountColumn = new TableColumn<>("Discount Type");
        productDiscountColumn.setCellFactory(column -> {
            return new TableCell<Product, String>() {
                private final ComboBox<String> comboBox = new ComboBox<>();

                {
                    try {
                        List<String> discountTypeNames = discountDAO.getAllDiscountTypeNames();
                        comboBox.getItems().addAll(discountTypeNames);

                        comboBox.setOnAction(event -> {
                            String selectedDiscountType = comboBox.getSelectionModel().getSelectedItem();
                            Product product = getTableView().getItems().get(getIndex());
                            product.setDiscountTypeString(selectedDiscountType);

                            try {
                                int selectedDiscountTypeId = discountDAO.getDiscountTypeIdByName(selectedDiscountType);
                                boolean success = discountDAO.updateProductDiscount(product.getProductId(), selectedSupplier.getId(), selectedDiscountTypeId);

                                if (success) {
                                    setStyle("-fx-background-color: lightgreen; -fx-background-insets: 0, 0 0 5 0;");
                                    Timeline timeline = new Timeline(
                                            new KeyFrame(Duration.seconds(0), new KeyValue(opacityProperty(), 1)),
                                            new KeyFrame(Duration.seconds(1), new KeyValue(opacityProperty(), 0.8)),
                                            new KeyFrame(Duration.seconds(2), new KeyValue(opacityProperty(), 0.6)),
                                            new KeyFrame(Duration.seconds(3), e -> setStyle(""))); // Remove style after 3 seconds
                                    timeline.play();
                                }
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }
                        });


                    } catch (SQLException e) {
                        // Handle the SQL exception appropriately
                        e.printStackTrace();
                    }
                }

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        Product product = getTableView().getItems().get(getIndex());
                        int productId = product.getProductId();
                        int supplierId = selectedSupplier.getId();
                        try {
                            int existingDiscountId = discountDAO.getProductDiscountForProductTypeId(productId, supplierId);
                            String discountTypeName = discountDAO.getDiscountTypeById(existingDiscountId);

                            // Check if the comboBox value is different from the retrieved discount type
                            if (!Objects.equals(discountTypeName, comboBox.getValue())) {
                                comboBox.setValue(discountTypeName);
                            }
                            setGraphic(comboBox);
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }

            };
        });
        return productDiscountColumn;
    }


    private void addProductToSupplierTable(String supplierName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("tableManager.fxml"));
            Parent content = loader.load();

            TableManagerController controller = loader.getController();
            controller.setRegistrationType("product_supplier");
            controller.loadProductParentsTable(supplierName);

            // Create a new stage (window) for company registration
            Stage stage = new Stage();
            stage.setTitle("Add new product to " + supplierName); // Set the title of the new stage
            stage.setScene(new Scene(content)); // Set the scene with the loaded content
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception according to your needs
            System.err.println("Error loading companyRegistration.fxml: " + e.getMessage());
        }

    }

    private void loadSelectedSupplier(Supplier selectedSupplier) {

        Image image = new Image(new ByteArrayInputStream(selectedSupplier.getSupplierImage()));
        supplierLogo.setImage(image);


        supplierNameTextField.setText(selectedSupplier.getSupplierName());
        supplierContactPersonTextField.setText(selectedSupplier.getContactPerson());
        supplierEmailTextField.setText(selectedSupplier.getEmailAddress());
        supplierContactNoTextField.setText(selectedSupplier.getPhoneNumber());
        provinceComboBox.setValue(selectedSupplier.getStateProvince());
        cityComboBox.setValue(selectedSupplier.getCity());
        baranggayComboBox.setValue(selectedSupplier.getBarangay());
        postalCodeTextField.setText(selectedSupplier.getPostalCode());
        dateAddedTextField.setText(String.valueOf(selectedSupplier.getDateAdded()));
        supplierTypeComboBox.setValue(selectedSupplier.getSupplierType());
        tinNumberTextField.setText(selectedSupplier.getTinNumber());
        bankDetailsTextField.setText(selectedSupplier.getBankDetails());
        paymentTermsComboBox.setValue(selectedSupplier.getPaymentTerms());
        deliveryTermsComboBox.setValue(selectedSupplier.getDeliveryTerms());
        agreementContractTextField.setText(selectedSupplier.getAgreementOrContract());
        try {
            discountTypeComboBox.setValue(discountDAO.getDiscountTypeById(selectedSupplier.getDiscountType()));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        preferredCommunicationMethodTextField.setText(selectedSupplier.getPreferredCommunicationMethod());
        notesOrCommentsTextField.setText(selectedSupplier.getNotesOrComments());
    }
}
