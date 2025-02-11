package com.vertex.vos;

import com.vertex.vos.DAO.ProductPerCustomerDAO;
import com.vertex.vos.Objects.*;
import com.vertex.vos.Utilities.*;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javafx.util.converter.DoubleStringConverter;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CustomerRegistrationController implements Initializable {

    public CheckBox isActive;
    public TableView<Product> productListTableView;
    public TableColumn<Product, String> productNameCol;
    public TableColumn<Product, String> productUnitCol;
    public TableColumn<Product, Double> unitPriceCol;
    public TableColumn<Product, String> discountTypeCol;
    public Button addButton;
    public TableColumn<Product, String> brandCol;
    public TableColumn<Product, String> categoryCol;
    public ComboBox<DiscountType> discountTypePerItemComboBox, discountTypeComboBox;
    public Button updateButton;
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
    private ComboBox<String> creditTypeComboBox, provinceComboBox, storeTypeComboBox;

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
        initializeComboBoxes();

        productNameCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProductName()));
        productUnitCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUnitOfMeasurementString()));

        // Make unit price editable
        unitPriceCol.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getPricePerUnit()).asObject());
        unitPriceCol.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter())); // Make the cell editable

        unitPriceCol.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        unitPriceCol.setOnEditCommit(event -> {
            Product product = event.getRowValue();
            double newUnitPrice = event.getNewValue();
            if (product.getPricePerUnit() != newUnitPrice) {
                product.setPricePerUnit(newUnitPrice);
                modifiedProducts.add(product);  // Mark as modified
            }
        });

        discountTypeCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDiscountType().getTypeName()));
        brandCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProductBrandString()));
        categoryCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProductCategoryString()));

        productListTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
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
            // Fetch all discount types from the database
            List<DiscountType> discountTypes = discountDAO.getAllDiscountTypes();

            // Set the items for the combo box using the full DiscountType objects
            discountTypeComboBox.setItems(FXCollections.observableArrayList(discountTypes));
            discountTypePerItemComboBox.setItems(FXCollections.observableArrayList(discountTypes));

            // Use a StringConverter to display only the typeName in the combo box
            discountTypeComboBox.setConverter(new StringConverter<DiscountType>() {
                @Override
                public String toString(DiscountType discountType) {
                    return discountType != null ? discountType.getTypeName() : "";
                }

                @Override
                public DiscountType fromString(String string) {
                    return null; // Not necessary to handle in this case
                }
            });

            discountTypePerItemComboBox.setConverter(new StringConverter<DiscountType>() {
                @Override
                public String toString(DiscountType discountType) {
                    return discountType != null ? discountType.getTypeName() : "";
                }

                @Override
                public DiscountType fromString(String string) {
                    return null; // Not necessary to handle in this case
                }
            });

            // Add listener to handle selection changes and retrieve the corresponding DiscountType object
            discountTypeComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    DiscountType selectedDiscountType = newValue; // Full object is now selected
                    System.out.println("Selected Discount Type: " + selectedDiscountType.getTypeName() + " | ID: " + selectedDiscountType.getId());
                    // You can now use the selected DiscountType object
                }
            });

            discountTypePerItemComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    List<Product> selectedProducts = productListTableView.getSelectionModel().getSelectedItems();
                    selectedProducts.forEach(product -> {
                        product.setDiscountType(newValue);
                        modifiedProducts.add(product);
                    });

                    productListTableView.refresh();
                }
            });

        } catch (SQLException e) {
            handleError(e);
        }
    }


    private void initiateRegistration(int id) {
        if (storeSignageTextField.getText().isEmpty()) {
            storeSignageTextField.setText(storeNameTextField.getText());
        }
        if (customerNameTextField.getText().isEmpty()) {
            customerNameTextField.setText(storeNameTextField.getText());
        }
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
            DialogUtils.showCompletionDialog("Customer registration successful", customer.getCustomerName() + " is now one of your customers");
            tableManagerController.populateCustomerTable();
        } else {
            DialogUtils.showErrorMessage("Error", "Please contact your system administrator");
        }
    }

    private Customer buildCustomer(int id) throws SQLException {
        Customer customer = new Customer();
        customer.setCustomerId(id);
        customer.setCustomerCode(customerCodeTextField.getText());
        customer.setCustomerName(customerNameTextField.getText());
        customer.setCustomerImage("");
        String initialStoreName = storeNameTextField.getText();
        String city = getSelectedCity();
        if (!initialStoreName.contains(city)) {
            storeNameTextField.setText(initialStoreName + ", " + city);
        }
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
        customer.setEncoderId(UserSession.getInstance().getUserId());
        customer.setCreditType((byte) creditTypeDAO.getCreditTypeIdByName(creditTypeComboBox.getSelectionModel().getSelectedItem()));
        customer.setCompanyCode((byte) companyDAO.getCompanyIdByName(companyCodeComboBox.getSelectionModel().getSelectedItem()));
        customer.setDateEntered(Timestamp.valueOf(dateAddedDatePicker.getValue().atStartOfDay()));
        customer.setActive(isActive.isSelected());
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
            LocationComboBoxUtil locationComboBoxUtil = new LocationComboBoxUtil(provinceComboBox, cityComboBox, baranggayComboBox);
            locationComboBoxUtil.initializeComboBoxes();
            populateCustomerFields(selectedCustomer);
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

        loadCustomerProducts(selectedCustomer);

        Customer passedCustomer = selectedCustomer;

        addButton.setOnMouseClicked(mouseEvent -> openProductSelectionWindow(passedCustomer));
    }

    ObservableList<Product> productList = FXCollections.observableArrayList();

    Set<Product> modifiedProducts = new HashSet<>();


    public void loadCustomerProducts(Customer selectedCustomer) {
        productList.setAll(FXCollections.observableList(productPerCustomerDAO.getProductsForCustomer(selectedCustomer)));
        productListTableView.setItems(productList);
        updateButton.setOnMouseClicked(mouseEvent -> {
            boolean result = productPerCustomerDAO.updateProductsForCustomer(selectedCustomer, new ArrayList<>(modifiedProducts));
            if (result) {
                DialogUtils.showCompletionDialog("Update successful", "Customer products updated successfully.");
            } else {
                DialogUtils.showErrorMessage("Error", "Failed to update customer products.");
            }
        });
    }

    private Stage productSelectionStage = null; // Track the Product Selection stage

    ProductPerCustomerDAO productPerCustomerDAO = new ProductPerCustomerDAO();

    private void openProductSelectionWindow(Customer passedCustomer) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ProductSelection.fxml"));
            Parent root = loader.load();
            ProductSelectionController controller = loader.getController();

            productSelectionStage = new Stage();
            productSelectionStage.setTitle("Product Selection ");
            controller.setProductSelectionStage(productSelectionStage);
            controller.setCustomerController(this);
            controller.setPassedCustomer(passedCustomer);

            productSelectionStage.setMaximized(true);
            productSelectionStage.setScene(new Scene(root));
            productSelectionStage.show();

            // Close the product selection window when the parent stage is closed
            parentStage.setOnCloseRequest(event -> productSelectionStage.close());

        } catch (IOException e) {
            DialogUtils.showErrorMessage("Error", "Unable to open product selection.");
            e.printStackTrace();
        }
    }


    private void populateCustomerFields(Customer selectedCustomer) throws SQLException {
        customerCodeTextField.setText(selectedCustomer.getCustomerCode());
        customerNameTextField.setText(selectedCustomer.getCustomerName());
        loadCustomerImage(selectedCustomer.getCustomerImage());
        storeNameTextField.setText(selectedCustomer.getStoreName());
        storeSignageTextField.setText(selectedCustomer.getStoreSignage());
        provinceComboBox.setValue(selectedCustomer.getProvince());
        cityComboBox.setValue(selectedCustomer.getCity());
        baranggayComboBox.setValue(selectedCustomer.getBrgy());
        customerContactNoTextField.setText(selectedCustomer.getContactNumber());
        customerEmailTextField.setText(selectedCustomer.getCustomerEmail());
        customerTelNoTextField.setText(selectedCustomer.getTelNumber());
        isActive.setSelected(selectedCustomer.isActive());
        tinNumberTextField.setText(selectedCustomer.getCustomerTin());
        priceTypeComboBox.getSelectionModel().select(selectedCustomer.getPriceType());
        companyCodeComboBox.getSelectionModel().select(companyDAO.getCompanyNameById(selectedCustomer.getCompanyCode()));
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
            DialogUtils.showCompletionDialog("Customer update successful", customer.getCustomerName() + " has been updated.");
            tableManagerController.populateCustomerTable();
        } else {
            DialogUtils.showErrorMessage("Error", "Please contact your system administrator.");
        }
    }

    private void handleError(SQLException e) {
        DialogUtils.showErrorMessage("SQL Error", e.getMessage());
    }

    void customerRegistration() {
        customerCodeTextField.setDisable(true);
        int id = Integer.parseInt(String.valueOf(customerDAO.getNextCustomerID()));
        storeName.setText("Customer Registration (" + id + ")");
        customerCodeTextField.setText(String.valueOf("MAIN - " + id));
        dateAddedDatePicker.setValue(LocalDate.now());


        confirmButton.setOnMouseClicked(event -> {
            initiateRegistration(id);
        });
    }

    Stage parentStage;

    public void setStage(Stage newStage) {
        this.parentStage = newStage;
    }
}
