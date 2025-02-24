package com.vertex.vos;

import com.vertex.vos.Objects.ComboBoxFilterUtil;
import com.vertex.vos.Objects.SupplierCreditDebitMemo;
import com.vertex.vos.Objects.UserSession;
import com.vertex.vos.Utilities.*;
import com.zaxxer.hikari.HikariDataSource;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class SupplierCreditDebitFormController implements Initializable {

    public TextField reason;
    public TextField amount;
    public CheckBox isPendingCheckBox;

    @FXML
    private VBox account;

    @FXML
    private ComboBox<String> accountComboBox;

    @FXML
    private Label accountErr;

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
    private Label documentTypeLabel;

    @FXML
    private Label accountLabel;

    @FXML
    private DatePicker memoDateDatePicker;

    TransactionTypeDAO transactionTypeDAO = new TransactionTypeDAO();
    ChartOfAccountsDAO chartOfAccountsDAO = new ChartOfAccountsDAO();
    DocumentNumbersDAO numbersDAO = new DocumentNumbersDAO();
    SupplierDAO supplierDAO = new SupplierDAO();
    SupplierMemoDAO supplierMemoDAO = new SupplierMemoDAO();

    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    private void comboBoxUtils() {
        TextFieldUtils.setComboBoxBehavior(glCOAComboBox);
        TextFieldUtils.setComboBoxBehavior(accountComboBox);
        TextFieldUtils.addDoubleInputRestriction(amount);
    }

    private void setupComboBoxFilter(ComboBox<String> comboBox, ObservableList<String> items) {
        ComboBoxFilterUtil.setupComboBoxFilter(comboBox, items);
    }

    private SupplierCreditDebitMemo createCreditMemo(int documentNumber) {
        SupplierCreditDebitMemo creditMemo = new SupplierCreditDebitMemo();
        creditMemo.setMemoNumber(String.valueOf(documentNumber));
        creditMemo.setStatus("Memo Entry");
        creditMemo.setType(1);
        return creditMemo;
    }

    private SupplierCreditDebitMemo createDebitMemo(int documentNumber) {
        SupplierCreditDebitMemo debitMemo = new SupplierCreditDebitMemo();
        debitMemo.setMemoNumber(String.valueOf(documentNumber));
        debitMemo.setStatus("Memo Entry");
        debitMemo.setType(2);
        return debitMemo;
    }

    public void addNewSupplierCreditMemo() {
        int documentNumber = numbersDAO.getNextSupplierCreditNumber();
        ObservableList<String> supplierNames = FXCollections.observableArrayList(supplierDAO.getAllSupplierNames());
        ObservableList<String> accountNames = FXCollections.observableArrayList(chartOfAccountsDAO.getAllAccountNames());

        setupComboBoxFilter(accountComboBox, supplierNames);
        setupComboBoxFilter(glCOAComboBox, accountNames);

        accountComboBox.setItems(supplierNames);
        glCOAComboBox.setItems(accountNames);

        accountLabel.setText("Supplier Name");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy h:mm a");
        date.setText(LocalDateTime.now().format(formatter));

        SupplierCreditDebitMemo creditMemo = createCreditMemo(documentNumber);

        docNoLabel.setText("Supplier Credit Memo #" + creditMemo.getMemoNumber());
        documentTypeLabel.setText("Credit");

        confirmButton.setOnMouseClicked(event -> processSupplierCreditMemo(creditMemo));
    }

    public void addNewSupplierDebitMemo() {
        int documentNumber = numbersDAO.getNextSupplierDebitNumber();
        ObservableList<String> supplierNames = FXCollections.observableArrayList(supplierDAO.getAllSupplierNames());
        ObservableList<String> accountNames = FXCollections.observableArrayList(chartOfAccountsDAO.getAllAccountNames());

        setupComboBoxFilter(accountComboBox, supplierNames);
        setupComboBoxFilter(glCOAComboBox, accountNames);

        memoDateDatePicker.setValue(LocalDate.now());

        accountComboBox.setItems(supplierNames);
        glCOAComboBox.setItems(accountNames);

        accountLabel.setText("Supplier Name");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy h:mm a");
        date.setText(LocalDateTime.now().format(formatter));

        SupplierCreditDebitMemo debitMemo = createDebitMemo(documentNumber);

        docNoLabel.setText("Supplier Debit Memo #" + debitMemo.getMemoNumber());
        documentTypeLabel.setText("Debit");

        confirmButton.setOnMouseClicked(event -> processSupplierDebitMemo(debitMemo));
    }

    private void processSupplierCreditMemo(SupplierCreditDebitMemo creditMemo) {
        processMemo(creditMemo);
    }

    private void processSupplierDebitMemo(SupplierCreditDebitMemo debitMemo) {
        processMemo(debitMemo);
    }

    private void processMemo(SupplierCreditDebitMemo memo) {
        memo.setAmount(amount.getText().isEmpty() ? 0.0 : Double.parseDouble(amount.getText()));
        memo.setReason(reason.getText());
        memo.setDate(Date.valueOf(memoDateDatePicker.getValue()));
        memo.setStatus("Available");
        memo.setChartOfAccount(chartOfAccountsDAO.getChartOfAccountIdByName(glCOAComboBox.getSelectionModel().getSelectedItem()));
        memo.setTargetId(supplierDAO.getSupplierIdByName(accountComboBox.getSelectionModel().getSelectedItem()));
        memo.setEncoderId(UserSession.getInstance().getUserId());
        memo.setPending(isPendingCheckBox.isSelected());

        boolean success = supplierMemoDAO.addSupplierMemo(memo);
        if (success) {
            DialogUtils.showCompletionDialog("Success", "Supplier Memo Successfully Added!");
            confirmButton.setDisable(true);
        } else {
            DialogUtils.showErrorMessage("Error", "Supplier Memo Not Added!");
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        comboBoxUtils();
    }
    SupplierCreditDebitListController supplierCreditDebitListController;

    public void setCreditDebitListController(SupplierCreditDebitListController supplierCreditDebitListController) {
        this.supplierCreditDebitListController = supplierCreditDebitListController;
    }
}