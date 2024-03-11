package com.vertex.vos;

import com.vertex.vos.Constructors.Customer;
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
    private ComboBox <String> storeTypeComboBox;

    @FXML
    void onSupplierLogoClicked(MouseEvent event) {

    }

    CustomerDAO customerDAO = new CustomerDAO();
    CreditTypeDAO creditTypeDAO = new CreditTypeDAO();

    void customerRegistration() {
        confirmButton.setOnMouseClicked(event -> {
            try {
                registerCustomer();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void registerCustomer() throws SQLException {
        String customerCode = customerCodeTextField.getText();
        String customerName = customerNameTextField.getText();
        String customerImage = "TODO";
        String storeName = storeNameTextField.getText();
        String storeSignage = storeSignageTextField.getText();
        String brgy = baranggayComboBox.getSelectionModel().getSelectedItem();
        String city = cityComboBox.getSelectionModel().getSelectedItem();
        String province = provinceComboBox.getSelectionModel().getSelectedItem();
        int contactNo = Integer.parseInt(customerContactNoTextField.getText());
        String customerEmail = customerEmailTextField.getText();
        int telNo = Integer.parseInt(customerTelNoTextField.getText());
        int customerTin = Integer.parseInt(tinNumberTextField.getText());
        int paymentTerm = creditTypeDAO.getCreditTypeIdByName(creditTypeComboBox.getSelectionModel().getSelectedItem());

    }


    private void initializeAddress() {
        Map<String, String> provinceData = LocationCache.getProvinceData();
        Map<String, String> cityData = LocationCache.getCityData();
        Map<String, String> barangayData = LocationCache.getBarangayData();

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
        try {
            populateStoreTypes();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
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
}
