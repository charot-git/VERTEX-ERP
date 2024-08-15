package com.vertex.vos;

import com.vertex.vos.DAO.AccountTypeDAO;
import com.vertex.vos.Objects.ChartOfAccounts;
import com.vertex.vos.Utilities.*;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

import java.net.URL;
import java.util.ResourceBundle;

public class ChartOfAccountFormController implements Initializable {

    @FXML
    private TextField accountTextField;

    @FXML
    private ComboBox<String> accountTypeComboBox;

    @FXML
    private ComboBox<String> bsisComboBox;

    @FXML
    private TextField descriptionComboBox;

    @FXML
    private TextField glCodeTextField;

    @FXML
    private AnchorPane header;

    @FXML
    private ComboBox<String> memoType;

    @FXML
    private Button confirmButton; // Button

    ChartOfAccountsDAO chartOfAccountsDAO = new ChartOfAccountsDAO();
    BSISDAo bSISDAo = new BSISDAo();
    AccountTypeDAO accountTypeDAO = new AccountTypeDAO();
    BalanceTypeDAO balanceTypeDAO = new BalanceTypeDAO();

    public void chartOfAccountRegistration() {
        confirmButton.setOnMouseClicked(mouseEvent -> initiateInsert());
    }

    private void initiateInsert() {
        ConfirmationAlert confirmationAlert = new ConfirmationAlert("Insert " + accountTextField.getText(), "Are you sure you want to insert this account?", "Please double check your entries", true);
        boolean confirmed = confirmationAlert.showAndWait();
        if (confirmed) {
            ChartOfAccounts newAccount = new ChartOfAccounts();
            newAccount.setGlCode(glCodeTextField.getText());
            newAccount.setAccountTitle(accountTextField.getText());
            newAccount.setAccountTypeId(accountTypeDAO.getAccountTypeIdString(accountTypeComboBox.getValue()));
            newAccount.setBsisCodeId(bSISDAo.getBSISCodeByString(bsisComboBox.getValue()));
            newAccount.setBalanceTypeId(balanceTypeDAO.getBalanceTypeByString(memoType.getValue()));
            newAccount.setDescription(descriptionComboBox.getText());
            boolean success = chartOfAccountsDAO.addAccount(newAccount);
            if (success) {
                DialogUtils.showConfirmationDialog("Success", "Account Created Successfully");
                tableManagerController.loadChartOfAccountsTable();
            } else {
                DialogUtils.showErrorMessage("Error", "Account Creation Failed");
            }
        }
    }

    public void initData(ChartOfAccounts selectedAccount) {
        if (selectedAccount != null) {
            glCodeTextField.setText(selectedAccount.getGlCode());
            accountTextField.setText(selectedAccount.getAccountTitle());
            accountTypeComboBox.setValue(selectedAccount.getAccountTypeString());
            bsisComboBox.setValue(selectedAccount.getBsisCodeString());
            memoType.setValue(selectedAccount.getBalanceTypeString());
            descriptionComboBox.setText(selectedAccount.getDescription());

            confirmButton.setText("Update");

            confirmButton.setOnMouseClicked(mouseEvent -> initiateUpdate(selectedAccount));
        }
    }

    private void initiateUpdate(ChartOfAccounts selectedAccount) {
        ConfirmationAlert confirmationAlert = new ConfirmationAlert("Update " + selectedAccount.getAccountTitle(), "Are you sure you want to update this account?", "Please double check your entries", true);
        boolean confirmed = confirmationAlert.showAndWait();

        if (confirmed) {
            selectedAccount.setGlCode(glCodeTextField.getText());
            selectedAccount.setAccountTitle(accountTextField.getText());
            selectedAccount.setAccountTypeId(accountTypeDAO.getAccountTypeIdString(accountTypeComboBox.getValue()));
            selectedAccount.setBsisCodeId(bSISDAo.getBSISCodeByString(bsisComboBox.getValue()));
            selectedAccount.setBalanceTypeId(balanceTypeDAO.getBalanceTypeByString(memoType.getValue()));
            selectedAccount.setDescription(descriptionComboBox.getText());
            boolean updated = chartOfAccountsDAO.updateAccount(selectedAccount);

            if (updated) {
                DialogUtils.showConfirmationDialog("Success", "Account Updated Successfully");
                tableManagerController.loadChartOfAccountsTable();
            } else {
                DialogUtils.showErrorMessage("Error", "Failed to Update Account");
            }

        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ObservableList<String> accountTypes = accountTypeDAO.getAllAccountTypes();
        ObservableList<String> bsisCodes = bSISDAo.getAllBSISCodes();
        ObservableList<String> balanceTypes = balanceTypeDAO.getAllBalanceTypes();
        accountTypeComboBox.setItems(accountTypes);
        bsisComboBox.setItems(bsisCodes);
        memoType.setItems(balanceTypes);
    }

    TableManagerController tableManagerController;

    public void setTableManagerController(TableManagerController tableManagerController) {
        this.tableManagerController = tableManagerController;
    }
}
