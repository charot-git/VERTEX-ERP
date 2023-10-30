package com.vertex.vos;

import com.vertex.vos.Constructors.ComboBoxFilterUtil;
import com.vertex.vos.Utilities.DatabaseConnectionPool;
import com.vertex.vos.Utilities.LocationCache;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;


import java.io.*;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static com.vertex.vos.Utilities.TextFieldUtils.addNumericInputRestriction;

public class SupplierInfoRegistrationController implements Initializable, DateSelectedCallback {

    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    private String selectedFilePath;

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
    private TextField paymentTermsTextField;
    @FXML
    private Label paymentTermsErr;
    @FXML
    private TextField deliveryTermsTextField;
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

        TextFieldUtils.setComboBoxBehavior(provinceComboBox);
        TextFieldUtils.setComboBoxBehavior(cityComboBox);
        TextFieldUtils.setComboBoxBehavior(baranggayComboBox);
        TextFieldUtils.setComboBoxBehavior(supplierTypeComboBox);

        initializeAddress();
        populateSupplierTypes();

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
            initiateRegistration();
        });
    }


    private void initiateRegistration() {
        String errorMessage = validateFields();

        if (errorMessage.isEmpty()) {
            confirmationAlert confirmationAlert = new confirmationAlert("Registration Confirmation", "Register " + supplierNameTextField.getText() + " ?", "todo");
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

    private void initializeAddress() {
        Map<String, String> provinceData = LocationCache.getProvinceData();
        Map<String, String> cityData = LocationCache.getCityData();
        Map<String, String> barangayData = LocationCache.getBarangayData();

        // Populate provinceComboBox with province codes and names
        provinceComboBox.setItems(FXCollections.observableArrayList(provinceData.values()));
        ComboBoxFilterUtil.setupComboBoxFilter(provinceComboBox, FXCollections.observableArrayList(provinceData.values()));

        // Add listener to populate cityComboBox based on selected province
        provinceComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            String selectedProvinceCode = getKeyFromValue(provinceData, newValue);
            List<String> citiesInProvince = filterLocationsByParentCode(cityData, selectedProvinceCode);
            cityComboBox.setItems(FXCollections.observableArrayList(citiesInProvince));
            ComboBoxFilterUtil.setupComboBoxFilter(cityComboBox, FXCollections.observableArrayList(citiesInProvince));
        });

        // Add listener to populate baranggayComboBox based on selected city
        cityComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            String selectedCityCode = getKeyFromValue(cityData, newValue);
            List<String> barangaysInCity = filterLocationsByParentCode(barangayData, selectedCityCode);
            ComboBoxFilterUtil.setupComboBoxFilter(cityComboBox, FXCollections.observableArrayList(barangaysInCity));
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
        clearErrorLabels(); // Clear previous error messages
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

    private void clearErrorLabels() {
        supplierNameErr.setText(""); // Clear the error label associated with supplierNameTextField
        supplierContactPersonErr.setText(""); // Clear the error label associated with supplierContactPersonTextField
        supplierEmailErr.setText(""); // Clear the error label associated with supplierEmailTextField
        supplierContactNoErr.setText(""); // Clear the error label associated with supplierContactNoTextField
        provinceErr.setText(""); // Clear the error label associated with provinceTextField
        cityErr.setText(""); // Clear the error label associated with cityTextField
        baranggayErr.setText(""); // Clear the error label associated with baranggayTextField
        postalCodeErr.setText(""); // Clear the error label associated with postalCodeTextField
        dateAddedErr.setText(""); // Clear the error label associated with dateAddedTextField
        supplierTypeErr.setText(""); // Clear the error label associated with supplierTypeTextField
        tinNumberErr.setText(""); // Clear the error label associated with tinNumberTextField
        bankDetailsErr.setText(""); // Clear the error label associated with bankDetailsTextField
        productAndServicesErr.setText(""); // Clear the error label associated with productAndServicesTextField
        paymentTermsErr.setText(""); // Clear the error label associated with paymentTermsTextField
        deliveryTermsErr.setText(""); // Clear the error label associated with deliveryTermsTextField
        agreementContractErr.setText(""); // Clear the error label associated with agreementContractTextField
        preferredCommunicationMethodErr.setText(""); // Clear the error label associated with preferredCommunicationMethodTextField
        notesOrCommentsErr.setText(""); // Clear the error label associated with notesOrCommentsTextField
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
            preparedStatement.setString(12, deliveryTermsTextField.getText());
            preparedStatement.setString(13, supplierEmailTextField.getText());
            preparedStatement.setString(14, paymentTermsTextField.getText());
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
                // You can perform additional actions upon successful registration if needed
            } else {
                confirmationLabel.setText("Failed to register supplier. Please try again.");
            }

        } catch (SQLException | IOException e) {
            e.printStackTrace();
            confirmationLabel.setText("Error occurred while registering supplier.");
        }
    }


}
