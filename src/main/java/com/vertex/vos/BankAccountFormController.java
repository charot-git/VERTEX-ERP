package com.vertex.vos;

import com.vertex.vos.Objects.BankAccount;
import com.vertex.vos.Objects.UserSession;
import com.vertex.vos.Utilities.BankAccountDAO;
import com.vertex.vos.Utilities.DialogUtils;
import com.vertex.vos.Utilities.LocationComboBoxUtil;
import com.vertex.vos.Utilities.TextFieldUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

public class BankAccountFormController implements Initializable {

    @FXML
    private TextField accountNumberTextField;

    @FXML
    private CheckBox activeCheckBox;

    @FXML
    private TextField bankNameTextField;

    @FXML
    private ComboBox<String> baranggayComboBox;

    @FXML
    private TextField branchTextField;

    @FXML
    private ComboBox<String> cityComboBox;

    @FXML
    private Button confirmButton;

    @FXML
    private TextField contactPersonTextField;

    @FXML
    private TextField emailTextField;

    @FXML
    private TextField ifscCodeTextField;

    @FXML
    private TextField mobileNoTextField;

    @FXML
    private TextField openingBalanceTextField;

    @FXML
    private ComboBox<String> provinceComboBox;

    @FXML
    private TextField bankDescriptionTextField;

    private TableManagerController tableManagerController;
    private BankAccountDAO bankAccountDAO = new BankAccountDAO();

    void setTableManager(TableManagerController tableManagerController) {
        this.tableManagerController = tableManagerController;
    }

    // This method can be called from another controller to initiate the registration process
    public void registerBank() {
        activeCheckBox.setSelected(true);
        // Set up event handler for the confirmation button to validate fields and register bank account
        confirmButton.setOnMouseClicked(mouseEvent -> validateAndRegister());
    }

    private void validateAndRegister() {
        if (validateFields()) {
            registerBankAccount();
        }
    }

    private boolean validateFields() {
        StringBuilder validationErrors = new StringBuilder();

        String accountNumber = accountNumberTextField.getText();
        String bankName = bankNameTextField.getText();
        String branch = branchTextField.getText();
        String ifscCode = ifscCodeTextField.getText();
        String openingBalanceStr = openingBalanceTextField.getText();
        String contactPerson = contactPersonTextField.getText();
        String email = emailTextField.getText();
        String mobileNo = mobileNoTextField.getText();
        String bankDescription = bankDescriptionTextField.getText();

        // Validate required fields
        if (accountNumber.isEmpty() || bankName.isEmpty() || branch.isEmpty() || ifscCode.isEmpty() ||
                openingBalanceStr.isEmpty() || bankDescription.isEmpty()) {
            validationErrors.append("All fields are required.\n");
        }

        // Validate opening balance format
        try {
            BigDecimal openingBalance = new BigDecimal(openingBalanceStr);
            if (openingBalance.compareTo(BigDecimal.ZERO) < 0) {
                validationErrors.append("Opening balance cannot be negative.\n");
            }
        } catch (NumberFormatException e) {
            validationErrors.append("Invalid opening balance format.\n");
        }

        // If there are validation errors, display them and return false
        if (validationErrors.length() > 0) {
            DialogUtils.showErrorMessage("Validation Error", validationErrors.toString().trim());
            return false;
        }

        return true;
    }

    private void registerBankAccount() {
        String accountNumber = accountNumberTextField.getText();
        String bankName = bankNameTextField.getText();
        String branch = branchTextField.getText();
        String ifscCode = ifscCodeTextField.getText();
        String openingBalanceStr = openingBalanceTextField.getText();
        String contactPerson = contactPersonTextField.getText();
        String email = emailTextField.getText();
        String mobileNo = mobileNoTextField.getText();
        String bankDescription = bankDescriptionTextField.getText();

        BigDecimal openingBalance = new BigDecimal(openingBalanceStr);
        boolean isActive = activeCheckBox.isSelected();

        BankAccount bankAccount = new BankAccount();
        bankAccount.setAccountNumber(accountNumber);
        bankAccount.setBankName(bankName);
        bankAccount.setBranch(branch);
        bankAccount.setIfscCode(ifscCode);
        bankAccount.setOpeningBalance(openingBalance);
        bankAccount.setContactPerson(contactPerson);
        bankAccount.setEmail(email);
        bankAccount.setMobileNo(mobileNo);
        bankAccount.setActive(isActive);
        bankAccount.setBankDescription(bankDescription);
        bankAccount.setProvince(provinceComboBox.getSelectionModel().getSelectedItem());
        bankAccount.setCity(cityComboBox.getSelectionModel().getSelectedItem());
        bankAccount.setBaranggay(baranggayComboBox.getSelectionModel().getSelectedItem());
        bankAccount.setCreatedBy(UserSession.getInstance().getUserId());
        bankAccount.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));

        boolean registered = bankAccountDAO.addBankAccount(bankAccount);

        if (registered) {
            DialogUtils.showConfirmationDialog("Success", "Bank account registered successfully");
            tableManagerController.loadBankTable();
            confirmButton.setDisable(true);
        } else {
            DialogUtils.showErrorMessage("Error", "Failed to register bank account");
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        LocationComboBoxUtil locationComboBoxUtil = new LocationComboBoxUtil(provinceComboBox, cityComboBox, baranggayComboBox);
        locationComboBoxUtil.initializeComboBoxes();
        TextFieldUtils.addDoubleInputRestriction(openingBalanceTextField);
        TextFieldUtils.addNumericInputRestriction(accountNumberTextField);
        TextFieldUtils.addNumericInputRestriction(ifscCodeTextField);
        TextFieldUtils.addNumericInputRestriction(mobileNoTextField);
    }

    public void initData(BankAccount selectedAccount) {
        if (selectedAccount != null) {
            accountNumberTextField.setText(selectedAccount.getAccountNumber());
            bankNameTextField.setText(selectedAccount.getBankName());
            branchTextField.setText(selectedAccount.getBranch());
            contactPersonTextField.setText(selectedAccount.getContactPerson());
            emailTextField.setText(selectedAccount.getEmail());
            ifscCodeTextField.setText(selectedAccount.getIfscCode());
            mobileNoTextField.setText(selectedAccount.getMobileNo());
            openingBalanceTextField.setText(String.valueOf(selectedAccount.getOpeningBalance()));
            activeCheckBox.setSelected(selectedAccount.isActive());
            bankDescriptionTextField.setText(selectedAccount.getBankDescription());
            provinceComboBox.setValue(selectedAccount.getProvince());
            cityComboBox.setValue(selectedAccount.getCity());
            baranggayComboBox.setValue(selectedAccount.getBaranggay());
        }
    }
}
