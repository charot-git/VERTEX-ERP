package com.vertex.vos;

import com.vertex.vos.Objects.ComboBoxFilterUtil;
import com.vertex.vos.Objects.CreditDebitMemo;
import com.vertex.vos.Utilities.*;
import com.zaxxer.hikari.HikariDataSource;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class CreditDebitFormController implements Initializable {

    public TextField reason;
    public TextField amount;
    private String registrationType;

    @FXML
    private VBox account;

    @FXML
    private ComboBox<String> accountComboBox;

    @FXML
    private Label accountErr;

    @FXML
    private VBox addProductButton;

    @FXML
    private Label addProductLabel;

    @FXML
    private HBox confirmBox;

    @FXML
    private Button confirmButton;

    @FXML
    private Label date;

    @FXML
    private Label docNoLabel;

    @FXML
    private VBox glCOABox;

    @FXML
    private ComboBox<String> glCOAComboBox;

    @FXML
    private Label glCOAErr;

    @FXML
    private Label grandTotal;

    @FXML
    private VBox memoDateBox;

    @FXML
    private DatePicker memoDateDatePicker;

    @FXML
    private Label memoDateErr;

    @FXML
    private ImageView statusImage;

    @FXML
    private Label statusLabel;

    @FXML
    private HBox totalBox;

    @FXML
    private VBox totalBoxLabels;

    @FXML
    private VBox totalVBox;

    @FXML
    private VBox transactionTypeBox;

    @FXML
    private Label typeErr;

    @FXML
    private Label vat;

    @FXML
    private Label withholding;
    @FXML
    private Label documentTypeLabel;
    @FXML
    private Label accountLabel;

    public void setRegistrationType(String registrationType) {
        this.registrationType = registrationType;
    }

    TransactionTypeDAO transactionTypeDAO = new TransactionTypeDAO();
    ChartOfAccountsDAO chartOfAccountsDAO = new ChartOfAccountsDAO();

    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();


    private void comboBoxUtils() {
        TextFieldUtils.setComboBoxBehavior(glCOAComboBox);
        TextFieldUtils.setComboBoxBehavior(accountComboBox);
        TextFieldUtils.addDoubleInputRestriction(amount);
    }

    CreditDebitListController creditDebitListController;

    public void setCreditDebitListController(CreditDebitListController creditDebitListController) {
        this.creditDebitListController = creditDebitListController;
    }

    DocumentNumbersDAO numbersDAO = new DocumentNumbersDAO();
    SupplierDAO supplierDAO = new SupplierDAO();

    public void addNewSupplierCreditMemo() {
        int documentNumber = numbersDAO.getNextSupplierCreditNumber();
        ObservableList<String> supplierNames = FXCollections.observableArrayList(supplierDAO.getAllSupplierNames());
        ObservableList<String> accountNames = FXCollections.observableArrayList(chartOfAccountsDAO.getAllCreditAccountTitles());

        setupComboBoxFilter(accountComboBox, supplierNames);
        setupComboBoxFilter(glCOAComboBox, accountNames);

        accountComboBox.setItems(supplierNames);
        glCOAComboBox.setItems(accountNames);

        accountLabel.setText("Supplier Name");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy h:mm a");
        date.setText(LocalDateTime.now().format(formatter));

        CreditDebitMemo creditMemo = createCreditMemo(documentNumber);

        docNoLabel.setText("Supplier Credit Memo #" + creditMemo.getMemoNumber());
        statusLabel.setText(creditMemo.getStatus());
        documentTypeLabel.setText("Credit");

        confirmButton.setOnMouseClicked(event -> processCreditMemo(creditMemo));
    }

    private void setupComboBoxFilter(ComboBox<String> comboBox, ObservableList<String> items) {
        ComboBoxFilterUtil.setupComboBoxFilter(comboBox, items);
    }

    private CreditDebitMemo createCreditMemo(int documentNumber) {
        CreditDebitMemo creditMemo = new CreditDebitMemo();
        creditMemo.setMemoNumber(String.valueOf(documentNumber));
        creditMemo.setStatus("Memo Entry");
        creditMemo.setType(1);
        return creditMemo;
    }

    SupplierMemoDAO supplierMemoDAO = new SupplierMemoDAO();

    private void processCreditMemo(CreditDebitMemo creditMemo) {
        creditMemo.setAmount(amount.getText().isEmpty() ? 0.0 : Double.parseDouble(amount.getText()));
        creditMemo.setReason(reason.getText());
        creditMemo.setDate(Date.valueOf(memoDateDatePicker.getValue()));
        creditMemo.setStatus("Available");
        creditMemo.setChartOfAccount(chartOfAccountsDAO.getChartOfAccountIdByName(glCOAComboBox.getSelectionModel().getSelectedItem()));
        creditMemo.setTargetId(supplierDAO.getSupplierIdByName(accountComboBox.getSelectionModel().getSelectedItem()));

        boolean success = supplierMemoDAO.addSupplierMemo(creditMemo);
        if (success){
            DialogUtils.showConfirmationDialog("Success", "Credit Memo Successfully Added!");
            confirmButton.setDisable(true);
        } else {
            DialogUtils.showErrorMessage("Error", "Credit Memo Not Added!");
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        comboBoxUtils();
    }
}
