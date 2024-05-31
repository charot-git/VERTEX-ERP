package com.vertex.vos;

import com.vertex.vos.Constructors.Customer;
import com.vertex.vos.Constructors.UserSession;
import com.vertex.vos.Utilities.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

import java.net.URL;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class CustomerRegistrationController implements Initializable {

    @FXML
    private Label bankDetailsErr;

    @FXML
    private TextField bankDetailsTextField;

    @FXML
    private ComboBox<String> baranggayComboBox;

    @FXML
    private Label baranggayErr;

    @FXML
    private ComboBox<String> priceTypeComboBox;

    @FXML
    private Label priceTypeErr;

    @FXML
    private Label businessTypeLabel1;

    @FXML
    private Button chooseLogoButton;

    @FXML
    private ComboBox<String> cityComboBox;

    @FXML
    private Label cityErr;

    @FXML
    private ComboBox<String> companyCodeComboBox;

    @FXML
    private Label companyCodeErr;

    @FXML
    private Button confirmButton;

    @FXML
    private Label confirmationLabel;

    @FXML
    private ComboBox<String> creditTypeComboBox;

    @FXML
    private Label creditTypeErr;

    @FXML
    private Label customerCodeErr;

    @FXML
    private TextField customerCodeTextField;

    @FXML
    private Label customerContactNoErr;

    @FXML
    private TextField customerContactNoTextField;

    @FXML
    private Label customerEmailErr;

    @FXML
    private TextField customerEmailTextField;

    @FXML
    private ImageView customerLogo;

    @FXML
    private Label customerNameErr;

    @FXML
    private TextField customerNameTextField;

    @FXML
    private Label customerTelNoErr;

    @FXML
    private TextField customerTelNoTextField;

    @FXML
    private DatePicker dateAddedDatePicker;

    @FXML
    private Label dateAddedErr;

    @FXML
    private Label dateOfFormationLabel1;

    @FXML
    private ComboBox<String> discountTypeComboBox;

    @FXML
    private Label discountTypeErr;

    @FXML
    private Label otherDetailsErr;

    @FXML
    private TextArea otherDetailsTextArea;

    @FXML
    private ComboBox<String> provinceComboBox;

    @FXML
    private Label provinceErr;

    @FXML
    private Label storeName;

    @FXML
    private Label storeNameErr;

    @FXML
    private TextField storeNameTextField;

    @FXML
    private Label storeSignageErr;

    @FXML
    private TextField storeSignageTextField;

    @FXML
    private Label tinNumberErr;

    @FXML
    private TextField tinNumberTextField;
    @FXML
    private CheckBox isVat;
    @FXML
    private CheckBox isWithholding;
    @FXML
    private ComboBox<String> storeTypeComboBox;

    CustomerDAO customerDAO = new CustomerDAO();

    CompanyDAO companyDAO = new CompanyDAO();

    @FXML
    void onSupplierLogoClicked(MouseEvent event) {

    }

    private String validateFields() {
        StringBuilder errorMessage = new StringBuilder();

        // Validate store name
        String storeName = storeNameTextField.getText().trim();
        if (storeName.isEmpty()) {
            errorMessage.append("Store name is required.\n");
        }

        // Validate store signage
        String storeSignage = storeSignageTextField.getText().trim();
        if (storeSignage.isEmpty()) {
            errorMessage.append("Store signage is required.\n");
        }

        // Validate customer contact number
        String customerNumber = customerContactNoTextField.getText().trim();
        if (customerNumber.isEmpty()) {
            errorMessage.append("Customer contact number is required.\n");
        }

        // Validate province
        String province = provinceComboBox.getSelectionModel().getSelectedItem();
        if (province == null || province.isEmpty()) {
            errorMessage.append("Province is required.\n");
        }

        // Validate city
        String city = cityComboBox.getSelectionModel().getSelectedItem();
        if (city == null || city.isEmpty()) {
            errorMessage.append("City is required.\n");
        }
        String brgy = baranggayComboBox.getSelectionModel().getSelectedItem();
        if (brgy == null || brgy.isEmpty()) {
            errorMessage.append("Brgy is required.\n");
        }

        return errorMessage.toString();
    }


    CreditTypeDAO creditTypeDAO = new CreditTypeDAO();

    void customerRegistration() {
        int id = Integer.parseInt(String.valueOf(customerDAO.getNextCustomerID()));
        storeName.setText("Customer Registration (" + id + ")");
        dateAddedDatePicker.setValue(LocalDate.now());
        confirmButton.setOnMouseClicked(event -> initiateRegistration(id));
    }

    private void initiateRegistration(int id) {
        ConfirmationAlert confirmationAlert = new ConfirmationAlert("Customer Registration", "Register this customer?", "", true);
        if (confirmationAlert.showAndWait()) {
            String errorMessage = validateFields();
            if (errorMessage.isEmpty()) {
                try {
                    registerCustomer(id);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            } else {
                DialogUtils.showErrorMessageForValidation("Error", "Please correct the following fields", errorMessage);
            }
        }
    }

    private void registerCustomer(int id) throws SQLException {
        String customerCode = customerCodeTextField.getText();
        String customerName = customerNameTextField.getText();
        String customerImage = "TODO"; // Update this if you have logic to set the customer image
        String storeName = storeNameTextField.getText();
        String storeSignage = storeSignageTextField.getText();
        String brgy = baranggayComboBox.getSelectionModel().getSelectedItem();
        String city = cityComboBox.getSelectionModel().getSelectedItem();
        String province = provinceComboBox.getSelectionModel().getSelectedItem();
        String contactNo = customerContactNoTextField.getText();
        String customerEmail = customerEmailTextField.getText();
        String telNo = customerTelNoTextField.getText();
        String customerTin = tinNumberTextField.getText();
        String priceType = priceTypeComboBox.getSelectionModel().getSelectedItem();
        byte paymentTerm = (byte) creditTypeDAO.getCreditTypeIdByName(creditTypeComboBox.getSelectionModel().getSelectedItem());
        int storeType = storeTypeDAO.getStoreTypeIdByName(storeTypeComboBox.getSelectionModel().getSelectedItem());
        int discountId = discountDAO.getDiscountTypeIdByName(discountTypeComboBox.getSelectionModel().getSelectedItem());
        int encoderId = UserSession.getInstance().getUserId();
        byte creditType = (byte) creditTypeDAO.getCreditTypeIdByName(creditTypeComboBox.getSelectionModel().getSelectedItem());
        byte companyCode = (byte) companyDAO.getCompanyIdByName(companyCodeComboBox.getSelectionModel().getSelectedItem());
        Timestamp dateEntered = null;
        LocalDate selectedDate = dateAddedDatePicker.getValue();
        if (selectedDate != null) {
            dateEntered = Timestamp.valueOf(selectedDate.atStartOfDay());
        }
        boolean isActive = true;
        boolean vatBoolean = isVat.isSelected();
        boolean ewtBoolean = isWithholding.isSelected();
        String otherDetails = otherDetailsTextArea.getText();

        Customer customer = new Customer();
        customer.setCustomerId(id);
        customer.setCustomerCode(customerCode);
        customer.setCustomerName(customerName);
        customer.setCustomerImage(customerImage);
        customer.setStoreName(storeName);
        customer.setStoreSignage(storeSignage);
        customer.setBrgy(brgy);
        customer.setCity(city);
        customer.setProvince(province);
        customer.setContactNumber(contactNo);
        customer.setCustomerEmail(customerEmail);
        customer.setTelNumber(telNo);
        customer.setCustomerTin(customerTin);
        customer.setPaymentTerm(paymentTerm);
        customer.setStoreType(storeType);
        customer.setDateEntered(dateEntered);
        customer.setPriceType(priceType);
        customer.setDiscountId(discountId);
        customer.setEncoderId(encoderId);
        customer.setDateEntered(dateEntered);
        customer.setCreditType(creditType);
        customer.setCompanyCode(companyCode);
        customer.setActive(isActive);
        customer.setVAT(vatBoolean);
        customer.setEWT(ewtBoolean);
        customer.setOtherDetails(otherDetails);
        boolean registerCustomer = customerDAO.createCustomer(customer);

        if (registerCustomer) {
            DialogUtils.showConfirmationDialog("Customer registration successful", customerName + " is now one of your customers");
            tableManagerController.loadCustomerTable();
        } else {
            DialogUtils.showErrorMessage("Error", "Please contact your system administrator");
        }
    }


    private void initializeAddress() {
        Map<String, String> provinceData = LocationCache.getProvinceData();
        Map<String, String> cityData = LocationCache.getCityData();
        Map<String, String> barangayData = LocationCache.getBarangayData();

        TextFieldUtils.addNumericInputRestriction(tinNumberTextField);
        TextFieldUtils.addNumericInputRestriction(customerContactNoTextField);
        TextFieldUtils.addNumericInputRestriction(customerTelNoTextField);
        provinceComboBox.setItems(FXCollections.observableArrayList(provinceData.values()));

        provinceComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            String selectedProvinceCode = getKeyFromValue(provinceData, newValue);
            List<String> citiesInProvince = filterLocationsByParentCode(cityData, selectedProvinceCode);
            cityComboBox.setItems(FXCollections.observableArrayList(citiesInProvince));
        });

        cityComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            String selectedCityCode = getKeyFromValue(cityData, newValue);
            List<String> barangaysInCity = filterLocationsByParentCode(barangayData, selectedCityCode);
            baranggayComboBox.setItems(FXCollections.observableArrayList(barangaysInCity));
        });
    }

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
        if (code.length() == 9) {
            return code.substring(0, 6);
        } else if (code.length() == 6) {
            return code.substring(0, 4);
        } else {
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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeAddress();
        ObservableList<String> creditType = FXCollections.observableArrayList("CASH", "DEBT");
        ObservableList<String> priceType = FXCollections.observableArrayList("A", "B", "C", "D", "E");
        priceTypeComboBox.setItems(priceType);
        creditTypeComboBox.setItems(creditType);
        populateDiscountTypes();
        populateCompany();
        try {
            populateStoreTypes();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void populateCompany() {
        companyCodeComboBox.setItems(companyDAO.getAllCompanyNames());
    }

    StoreTypeDAO storeTypeDAO = new StoreTypeDAO();

    private void populateStoreTypes() throws SQLException {
        storeTypeComboBox.setItems(storeTypeDAO.getAllStoreTypes());
    }

    DiscountDAO discountDAO = new DiscountDAO();

    private void populateDiscountTypes() {
        try {
            List<String> discountTypeNames = discountDAO.getAllDiscountTypeNames();
            ObservableList<String> observableDiscountTypeNames = FXCollections.observableArrayList(discountTypeNames);
            discountTypeComboBox.setItems(observableDiscountTypeNames);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    TableManagerController tableManagerController;

    public void setTableManager(TableManagerController tableManagerController) {
        this.tableManagerController = tableManagerController;
    }

    public void initData(Customer customer) throws SQLException {
        Customer selectedCustomer = customerDAO.getCustomer(customer.getCustomerId());

        if (selectedCustomer != null) {
            customerCodeTextField.setText(selectedCustomer.getCustomerCode());
            customerNameTextField.setText(selectedCustomer.getCustomerName());
            loadCustomerImage(selectedCustomer.getCustomerImage());
            storeNameTextField.setText(selectedCustomer.getStoreName());
            storeSignageTextField.setText(selectedCustomer.getStoreSignage());
            baranggayComboBox.getSelectionModel().select(selectedCustomer.getBrgy());
            cityComboBox.getSelectionModel().select(selectedCustomer.getCity());
            provinceComboBox.getSelectionModel().select(selectedCustomer.getProvince());
            customerContactNoTextField.setText(selectedCustomer.getContactNumber());
            customerEmailTextField.setText(selectedCustomer.getCustomerEmail());
            customerTelNoTextField.setText(selectedCustomer.getTelNumber());
            tinNumberTextField.setText(selectedCustomer.getCustomerTin());
            creditTypeComboBox.setValue(creditTypeDAO.getCreditTypeNameById(selectedCustomer.getCreditType()));
            storeTypeComboBox.getSelectionModel().select(storeTypeDAO.getStoreTypeNameById(selectedCustomer.getStoreType()));
            priceTypeComboBox.setValue(selectedCustomer.getPriceType());
            discountTypeComboBox.setValue(discountDAO.getDiscountTypeById(selectedCustomer.getDiscountId()));
            companyCodeComboBox.setValue(companyDAO.getCompanyNameById(selectedCustomer.getCompanyCode()));
            dateAddedDatePicker.setValue(selectedCustomer.getDateEntered().toLocalDateTime().toLocalDate());
            isVat.setSelected(selectedCustomer.isVAT());
            isWithholding.setSelected(selectedCustomer.isEWT());
            otherDetailsTextArea.setText(selectedCustomer.getOtherDetails());

            confirmButton.setOnMouseClicked(mouseEvent -> {
                try {
                    initializeUpdate(selectedCustomer);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });


        } else {
            DialogUtils.showErrorMessage("Error", "Customer not found.");
        }
    }

    private void initializeUpdate(Customer selectedCustomer) throws SQLException {
        ConfirmationAlert confirmationAlert = new ConfirmationAlert("Customer Update", "Update " + selectedCustomer.getStoreName() + "?", "", true);
        if (confirmationAlert.showAndWait()) {
            updateCustomer(selectedCustomer.getCustomerId());
        }
    }

    private void updateCustomer(int id) throws SQLException {
        // Populate customer details from form fields
        String customerCode = customerCodeTextField.getText();
        String customerName = customerNameTextField.getText();
        String customerImage = "TODO"; // Update this if you have logic to set the customer image
        String storeName = storeNameTextField.getText();
        String storeSignage = storeSignageTextField.getText();
        String brgy = baranggayComboBox.getSelectionModel().getSelectedItem();
        String city = cityComboBox.getSelectionModel().getSelectedItem();
        String province = provinceComboBox.getSelectionModel().getSelectedItem();
        String contactNo = customerContactNoTextField.getText();
        String customerEmail = customerEmailTextField.getText();
        String telNo = customerTelNoTextField.getText();
        String customerTin = tinNumberTextField.getText();
        String priceType = priceTypeComboBox.getSelectionModel().getSelectedItem();
        byte paymentTerm = (byte) creditTypeDAO.getCreditTypeIdByName(creditTypeComboBox.getSelectionModel().getSelectedItem());
        int storeType = storeTypeDAO.getStoreTypeIdByName(storeTypeComboBox.getSelectionModel().getSelectedItem());
        int discountId = discountDAO.getDiscountTypeIdByName(discountTypeComboBox.getSelectionModel().getSelectedItem());
        int encoderId = UserSession.getInstance().getUserId();
        byte creditType = (byte) creditTypeDAO.getCreditTypeIdByName(creditTypeComboBox.getSelectionModel().getSelectedItem());
        byte companyCode = (byte) companyDAO.getCompanyIdByName(companyCodeComboBox.getSelectionModel().getSelectedItem());
        Timestamp dateEntered = null;
        LocalDate selectedDate = dateAddedDatePicker.getValue();
        if (selectedDate != null) {
            dateEntered = Timestamp.valueOf(selectedDate.atStartOfDay());
        }
        boolean isActive = true;
        boolean vatBoolean = isVat.isSelected();
        boolean ewtBoolean = isWithholding.isSelected();
        String otherDetails = otherDetailsTextArea.getText();

        Customer customer = new Customer();
        customer.setCustomerId(id);
        customer.setCustomerCode(customerCode);
        customer.setCustomerName(customerName);
        customer.setCustomerImage(customerImage);
        customer.setStoreName(storeName);
        customer.setStoreSignage(storeSignage);
        customer.setBrgy(brgy);
        customer.setCity(city);
        customer.setProvince(province);
        customer.setContactNumber(contactNo);
        customer.setCustomerEmail(customerEmail);
        customer.setTelNumber(telNo);
        customer.setCustomerTin(customerTin);
        customer.setPaymentTerm(paymentTerm);
        customer.setStoreType(storeType);
        customer.setDateEntered(dateEntered);
        customer.setPriceType(priceType);
        customer.setDiscountId(discountId);
        customer.setEncoderId(encoderId);
        customer.setDateEntered(dateEntered);
        customer.setCreditType(creditType);
        customer.setCompanyCode(companyCode);
        customer.setActive(isActive);
        customer.setVAT(vatBoolean);
        customer.setEWT(ewtBoolean);
        customer.setOtherDetails(otherDetails);

        boolean updateCustomer = customerDAO.updateCustomer(customer);

        if (updateCustomer) {
            DialogUtils.showConfirmationDialog("Customer update successful", customerName + " details have been updated");
            tableManagerController.loadCustomerTable();
        } else {
            DialogUtils.showErrorMessage("Error", "Failed to update customer. Please contact your system administrator");
        }
    }

    private void loadCustomerImage(String customerImage) {
    }
}
