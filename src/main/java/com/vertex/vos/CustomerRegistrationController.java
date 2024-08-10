package com.vertex.vos;

import com.vertex.vos.Objects.Customer;
import com.vertex.vos.Objects.UserSession;
import com.vertex.vos.Utilities.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

import java.net.URL;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class CustomerRegistrationController implements Initializable {

    @FXML
    private Label bankDetailsErr, baranggayErr, priceTypeErr, businessTypeLabel1, cityErr, companyCodeErr;
    @FXML
    private Label confirmationLabel, creditTypeErr, customerCodeErr, customerContactNoErr, customerEmailErr;
    @FXML
    private Label customerNameErr, customerTelNoErr, dateAddedErr, dateOfFormationLabel1, discountTypeErr;
    @FXML
    private Label otherDetailsErr, provinceErr, storeNameErr, storeSignageErr, tinNumberErr, storeName;

    @FXML
    private TextField bankDetailsTextField, customerCodeTextField, customerContactNoTextField;
    @FXML
    private TextField customerEmailTextField, customerNameTextField, customerTelNoTextField, storeNameTextField;
    @FXML
    private TextField storeSignageTextField, tinNumberTextField;

    @FXML
    private ComboBox<String> baranggayComboBox, priceTypeComboBox, cityComboBox, companyCodeComboBox;
    @FXML
    private ComboBox<String> creditTypeComboBox, discountTypeComboBox, provinceComboBox, storeTypeComboBox;

    @FXML
    private Button chooseLogoButton, confirmButton;

    @FXML
    private TextArea otherDetailsTextArea;
    @FXML
    private ImageView customerLogo;
    @FXML
    private DatePicker dateAddedDatePicker;
    @FXML
    private CheckBox isVat, isWithholding;

    private final CustomerDAO customerDAO = new CustomerDAO();
    private final CompanyDAO companyDAO = new CompanyDAO();
    private final CreditTypeDAO creditTypeDAO = new CreditTypeDAO();
    private final StoreTypeDAO storeTypeDAO = new StoreTypeDAO();
    private final DiscountDAO discountDAO = new DiscountDAO();

    private TableManagerController tableManagerController;

    @FXML
    void onSupplierLogoClicked(MouseEvent event) {
        // Handle supplier logo click event
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    private void initializeComboBoxes() {
        LocationComboBoxUtil locationComboBoxUtil = new LocationComboBoxUtil(provinceComboBox, cityComboBox, baranggayComboBox);
        locationComboBoxUtil.initializeComboBoxes();

        try {
            creditTypeComboBox.setItems(creditTypeDAO.getAllCreditTypeNames());
        } catch (SQLException e) {
            handleError(e);
        }
        priceTypeComboBox.setItems(FXCollections.observableArrayList("A", "B", "C", "D", "E"));
        populateDiscountTypes();
        populateCompany();
        try {
            populateStoreTypes();
        } catch (SQLException e) {
            handleError(e);
        }
    }


    private void populateCompany() {
        companyCodeComboBox.setItems(companyDAO.getAllCompanyNames());
    }

    private void populateStoreTypes() throws SQLException {
        storeTypeComboBox.setItems(storeTypeDAO.getAllStoreTypes());
    }

    private void populateDiscountTypes() {
        try {
            discountTypeComboBox.setItems(FXCollections.observableArrayList(discountDAO.getAllDiscountTypeNames()));
        } catch (SQLException e) {
            handleError(e);
        }
    }

    private void initiateRegistration(int id) {
        ConfirmationAlert confirmationAlert = new ConfirmationAlert("Customer Registration", "Register this customer?", "", true);
        if (confirmationAlert.showAndWait()) {
            String errorMessage = validateFields();
            if (errorMessage.isEmpty()) {
                try {
                    registerCustomer(id);
                } catch (SQLException e) {
                    handleError(e);
                }
            } else {
                DialogUtils.showErrorMessageForValidation("Error", "Please correct the following fields", errorMessage);
            }
        }
    }

    private void registerCustomer(int id) throws SQLException {
        Customer customer = buildCustomer(id);
        if (customerDAO.createCustomer(customer)) {
            DialogUtils.showConfirmationDialog("Customer registration successful", customer.getCustomerName() + " is now one of your customers");
            tableManagerController.loadCustomerTable();
        } else {
            DialogUtils.showErrorMessage("Error", "Please contact your system administrator");
        }
    }

    private Customer buildCustomer(int id) throws SQLException {
        Customer customer = new Customer();
        customer.setCustomerId(id);
        customer.setCustomerCode(customerCodeTextField.getText());
        customer.setCustomerName(customerNameTextField.getText());
        customer.setCustomerImage("TODO");
        String initialStoreName = storeNameTextField.getText();
        storeNameTextField.setText(initialStoreName + ", " +cityComboBox.getSelectionModel().getSelectedItem());
        customer.setStoreName(storeNameTextField.getText());
        customer.setStoreSignage(storeSignageTextField.getText());
        customer.setBrgy(getSelectedBarangay());
        customer.setCity(getSelectedCity());
        customer.setProvince(getSelectedProvince());
        customer.setContactNumber(customerContactNoTextField.getText());
        customer.setCustomerEmail(customerEmailTextField.getText());
        customer.setTelNumber(customerTelNoTextField.getText());
        customer.setCustomerTin(tinNumberTextField.getText());
        customer.setPriceType(priceTypeComboBox.getSelectionModel().getSelectedItem());
        customer.setPaymentTerm((byte) creditTypeDAO.getCreditTypeIdByName(creditTypeComboBox.getSelectionModel().getSelectedItem()));
        customer.setStoreType(storeTypeDAO.getStoreTypeIdByName(storeTypeComboBox.getSelectionModel().getSelectedItem()));
        customer.setDiscountId(discountDAO.getDiscountTypeIdByName(discountTypeComboBox.getSelectionModel().getSelectedItem()));
        customer.setEncoderId(UserSession.getInstance().getUserId());
        customer.setCreditType((byte) creditTypeDAO.getCreditTypeIdByName(creditTypeComboBox.getSelectionModel().getSelectedItem()));
        customer.setCompanyCode((byte) companyDAO.getCompanyIdByName(companyCodeComboBox.getSelectionModel().getSelectedItem()));
        customer.setDateEntered(Timestamp.valueOf(dateAddedDatePicker.getValue().atStartOfDay()));
        customer.setActive(true);
        customer.setVAT(isVat.isSelected());
        customer.setEWT(isWithholding.isSelected());
        customer.setOtherDetails(otherDetailsTextArea.getText());
        return customer;
    }

    private String validateFields() {
        StringBuilder errorMessage = new StringBuilder();
        validateField(errorMessage, "Store name", storeNameTextField.getText().trim());
        validateField(errorMessage, "Store signage", storeSignageTextField.getText().trim());
        validateField(errorMessage, "Customer contact number", customerContactNoTextField.getText().trim());
        validateField(errorMessage, "Province", getSelectedProvince());
        validateField(errorMessage, "City", getSelectedCity());
        validateField(errorMessage, "Brgy", getSelectedBarangay());
        return errorMessage.toString();
    }

    private void validateField(StringBuilder errorMessage, String fieldName, String fieldValue) {
        if (fieldValue == null || fieldValue.isEmpty()) {
            errorMessage.append(fieldName).append(" is required.\n");
        }
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

    public void setTableManager(TableManagerController tableManagerController) {
        this.tableManagerController = tableManagerController;
    }

    public void initData(Customer customer) throws SQLException {
        Customer selectedCustomer = customerDAO.getCustomer(customer.getCustomerId());
        if (selectedCustomer != null) {
            populateCustomerFields(selectedCustomer);
            LocationComboBoxUtil locationComboBoxUtil = new LocationComboBoxUtil(provinceComboBox, cityComboBox, baranggayComboBox);
            locationComboBoxUtil.initializeComboBoxes();
            confirmButton.setOnMouseClicked(mouseEvent -> {
                try {
                    initializeUpdate(selectedCustomer);
                } catch (SQLException e) {
                    handleError(e);
                }
            });
        } else {
            DialogUtils.showErrorMessage("Error", "Customer not found.");
        }
    }

    private void populateCustomerFields(Customer selectedCustomer) throws SQLException {
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
        priceTypeComboBox.getSelectionModel().select(selectedCustomer.getPriceType());
        companyCodeComboBox.getSelectionModel().select(companyDAO.getCompanyNameById(selectedCustomer.getCompanyCode()));
        discountTypeComboBox.getSelectionModel().select(discountDAO.getDiscountTypeById(selectedCustomer.getDiscountId()));
        creditTypeComboBox.getSelectionModel().select(creditTypeDAO.getCreditTypeNameById(selectedCustomer.getCreditType()));
        storeTypeComboBox.getSelectionModel().select(storeTypeDAO.getStoreTypeNameById(selectedCustomer.getStoreType()));
        dateAddedDatePicker.setValue(selectedCustomer.getDateEntered().toLocalDateTime().toLocalDate());
        isVat.setSelected(selectedCustomer.isVAT());
        isWithholding.setSelected(selectedCustomer.isEWT());
        otherDetailsTextArea.setText(selectedCustomer.getOtherDetails());
    }

    private void loadCustomerImage(String customerImage) {
        // Logic to load the customer image
    }

    private void initializeUpdate(Customer selectedCustomer) throws SQLException {
        String errorMessage = validateFields();
        if (errorMessage.isEmpty()) {
            try {
                updateCustomer(selectedCustomer);
            } catch (SQLException e) {
                handleError(e);
            }
        } else {
            DialogUtils.showErrorMessageForValidation("Error", "Please correct the following fields", errorMessage);
        }
    }

    private void updateCustomer(Customer selectedCustomer) throws SQLException {
        Customer customer = buildCustomer(selectedCustomer.getCustomerId());
        if (customerDAO.updateCustomer(customer)) {
            DialogUtils.showConfirmationDialog("Customer update successful", customer.getCustomerName() + " has been updated.");
            tableManagerController.loadCustomerTable();
        } else {
            DialogUtils.showErrorMessage("Error", "Please contact your system administrator.");
        }
    }

    private void handleError(SQLException e) {
        DialogUtils.showErrorMessage("SQL Error", e.getMessage());
    }

    void customerRegistration() {
        initializeComboBoxes();
        confirmButton.setOnMouseClicked(event -> {
            int id = Integer.parseInt(String.valueOf(customerDAO.getNextCustomerID()));
            storeName.setText("Customer Registration (" + id + ")");
            dateAddedDatePicker.setValue(LocalDate.now());
            initiateRegistration(id);
        });
    }
}
