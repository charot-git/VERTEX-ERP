package com.vertex.vos;

import com.vertex.vos.Objects.*;
import com.vertex.vos.Utilities.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javafx.util.converter.DoubleStringConverter;
import lombok.Setter;
import org.controlsfx.control.textfield.TextFields;

import java.io.IOException;
import java.net.URL;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class CustomerMemoFormController implements Initializable {

    public Button addInvoice;
    public Button invoiceConfirm;
    public Button confirmButton;
    public Button addCollection;
    public Button collectionConfirm;
    public TableView<MemoCollectionApplication> collectionTable;
    public ComboBox<BalanceType> balanceTypeComboBox;
    public CheckBox isPending;
    public ComboBox<CustomerMemo.MemoStatus> statusComboBox;
    public SplitPane itemsSplitPane;
    @FXML
    private TableColumn<MemoCollectionApplication, Double> amountCol;

    @FXML
    private TextField amountField;

    @FXML
    private TableColumn<MemoInvoiceApplication, Double> amountInvoiceCol;

    @FXML
    private TextField appliedAmountField;

    @FXML
    private TableColumn<MemoCollectionApplication, String> cexNoCol;

    @FXML
    private TextField coaField;

    @FXML
    private TableView<?> collectionCol;

    @FXML
    private TextField customerCodeCol;

    @FXML
    private TableColumn<MemoCollectionApplication, Timestamp> dateCexCol;

    @FXML
    private TableColumn<MemoInvoiceApplication, Timestamp> dateInvoiceCol;

    @FXML
    private Label docNo;

    @FXML
    private TableColumn<MemoInvoiceApplication, String> invoiceNoCol;

    @FXML
    private TableView<MemoInvoiceApplication> invoiceTable;

    @FXML
    private TableColumn<MemoCollectionApplication, String> isPostedCexCol;

    @FXML
    private TableColumn<MemoInvoiceApplication, String> isPostedInvoiceCol;

    @FXML
    private TableColumn<MemoInvoiceApplication, String> orderNoInvoiceCol;

    @FXML
    private TextArea reasonField;

    @FXML
    private TextField salesmanCodeField;

    @FXML
    private TextField salesmanField;

    @FXML
    private TextField storeNameField;

    @FXML
    private TextField supplierField;

    ObservableList<MemoInvoiceApplication> invoicesForMemo = FXCollections.observableArrayList();
    ObservableList<MemoCollectionApplication> collectionsForMemo = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        invoiceTable.setItems(invoicesForMemo);
        collectionTable.setItems(collectionsForMemo);
        orderNoInvoiceCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getSalesInvoiceHeader().getOrderId()));
        invoiceNoCol.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getSalesInvoiceHeader().getInvoiceNo()));
        amountInvoiceCol.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getAmount()));
        amountInvoiceCol.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        amountInvoiceCol.setEditable(true);
        amountInvoiceCol.setOnEditCommit(event -> {
            MemoInvoiceApplication invoiceApplication = event.getRowValue();
            invoiceApplication.setAmount(event.getNewValue());
            updateAppliedAmount();
        });
        dateInvoiceCol.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getSalesInvoiceHeader().getInvoiceDate()));
        isPostedInvoiceCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getSalesInvoiceHeader().isPosted() ? "Yes" : "No"));

        cexNoCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getCollection().getDocNo()));
        amountCol.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getAmount()));
        amountCol.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        amountCol.setEditable(true);
        amountCol.setOnEditCommit(event -> {
            MemoCollectionApplication collectionApplication = event.getRowValue();
            collectionApplication.setAmount(event.getNewValue());
            updateAppliedAmount();
        });
        dateCexCol.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getCollection().getCollectionDate()));
        isPostedCexCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getCollection().getIsPosted() ? "Yes" : "No"));

        TextFieldUtils.addDoubleInputRestriction(amountField);
        TextFieldUtils.addDoubleInputRestriction(appliedAmountField);
        itemsSplitPane.setDisable(true);

        balanceTypeComboBox.setCellFactory(param -> new ListCell<BalanceType>() {
            @Override
            protected void updateItem(BalanceType item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getBalanceName());
            }
        });

        balanceTypeComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(BalanceType balanceType) {
                return balanceType == null ? "" : balanceType.getBalanceName();
            }

            @Override
            public BalanceType fromString(String string) {
                return balanceTypeComboBox.getItems().stream()
                        .filter(balance -> balance.getBalanceName().equals(string))
                        .findFirst()
                        .orElse(null);
            }
        });

        Platform.runLater(() -> {
            TextFields.bindAutoCompletion(coaField, chartOfAccounts.stream()
                    .map(ChartOfAccounts::getAccountTitle)
                    .collect(Collectors.toList()));
            TextFields.bindAutoCompletion(supplierField, suppliers.stream()
                    .map(Supplier::getSupplierName)
                    .collect(Collectors.toList()));
            TextFields.bindAutoCompletion(salesmanField, salesmen.stream()
                    .map(Salesman::getSalesmanName)
                    .collect(Collectors.toList()));
            TextFields.bindAutoCompletion(storeNameField, customers.stream()
                    .map(Customer::getStoreName)
                    .collect(Collectors.toList()));
            if (!customerMemo.getStatus().equals(CustomerMemo.MemoStatus.FOR_APPROVAL)) {
                addInvoice.setOnAction(actionEvent -> openInvoiceSelection());
                addCollection.setOnAction(actionEvent -> openCollectionSelection());
            }
        });

        invoicesForMemo.addListener((ListChangeListener<? super MemoInvoiceApplication>) change -> {
            while (change.next()) {
                updateAppliedAmount();
            }
        });
        collectionsForMemo.addListener((ListChangeListener<? super MemoCollectionApplication>) change -> {
            while (change.next()) {
                updateAppliedAmount();
            }
        });
    }

    private void openCollectionSelection() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("CollectionList.fxml"));
            Parent root = loader.load();
            CollectionListController controller = loader.getController();
            controller.loadForMemoSelection(customerMemo, this);
            Stage stage = new Stage();
            stage.setTitle("Collections");
            stage.setMaximized(true);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            DialogUtils.showErrorMessage("Error", "Unable to open receiving.");
            e.printStackTrace();
        }

    }

    private void openInvoiceSelection() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("SalesInvoices.fxml"));
            Parent root = loader.load();
            SalesInvoicesController controller = loader.getController();
            controller.setCustomerMemoFormController(this);
            controller.selectedCustomer = customerMemo.getCustomer();
            controller.selectedSalesman = customerMemo.getSalesman();
            controller.loadSalesInvoices();
            controller.loadForMemoSelection(customerMemo);
            Stage stage = new Stage();
            stage.setTitle("Sales Invoices");
            stage.setMaximized(true);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            DialogUtils.showErrorMessage("Error", "Unable to open receiving.");
            e.printStackTrace();
        }

    }

    @Setter
    BalanceType balanceType;
    CustomerMemoDAO customerMemoDAO = new CustomerMemoDAO();
    DocumentNumbersDAO documentNumbersDAO = new DocumentNumbersDAO();
    SalesmanDAO salesmanDAO = new SalesmanDAO();
    CustomerDAO customerDAO = new CustomerDAO();
    ChartOfAccountsDAO chartOfAccountsDAO = new ChartOfAccountsDAO();
    SupplierDAO supplierDAO = new SupplierDAO();

    CustomerMemo customerMemo;

    int initialMemoNo;

    public void createNewMemo() {
        customerMemo = new CustomerMemo();
        customerMemo.setBalanceType(balanceType);
        customerMemo.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
        customerMemo.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));
        customerMemo.setStatus(CustomerMemo.MemoStatus.FOR_APPROVAL);

        if (balanceType.getId() == 1) {
            initialMemoNo = documentNumbersDAO.getNextCustomerCreditNumber();
        } else if (balanceType.getId() == 2) {
            initialMemoNo = documentNumbersDAO.getNextCustomerDebitNumber();
        }
        balanceTypeComboBox.setValue(balanceType);
        docNo.setText(customerMemo.getMemoNumber());

        coaField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                chartOfAccounts.stream()
                        .filter(coa -> coa.getAccountTitle().equals(newValue))
                        .findFirst()
                        .ifPresent(selectedCOA -> {
                            customerMemo.setChartOfAccount(selectedCOA);
                        });
            }
        });
        supplierField.textProperty().addListener((observable, oldValue, newSupplierName) -> {
            if (newSupplierName != null && !newSupplierName.isEmpty()) {
                Supplier selectedSupplier = suppliers.stream()
                        .filter(supplier -> supplier.getSupplierName().equals(newSupplierName))
                        .findFirst()
                        .orElse(null);

                if (selectedSupplier != null) {
                    customerMemo.setSupplier(selectedSupplier);
                    customerMemo.setMemoNumber(selectedSupplier.getSupplierShortcut() + initialMemoNo);
                    docNo.setText(customerMemo.getMemoNumber());
                }
            }
        });
        salesmanField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                salesmen.stream().filter(param -> param.getSalesmanName().equals(newValue)).findFirst().ifPresent(selectedSalesman -> {
                    customerMemo.setSalesman(selectedSalesman);
                    salesmanCodeField.setText(selectedSalesman.getSalesmanCode());
                });
            }
        });
        storeNameField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                customers.stream().filter(param -> param.getStoreName().equals(newValue)).findFirst().ifPresent(selectedCustomer -> {
                    customerMemo.setCustomer(selectedCustomer);
                    customerCodeCol.setText(selectedCustomer.getCustomerCode());
                });
            }
        });

        amountField.textProperty().addListener((observable, oldVal, newVal) -> {
            customerMemo.setAmount(Double.parseDouble(newVal));
            updateAppliedAmount();
        });

        isPending.selectedProperty().addListener((observable, oldVal, newVal) -> {
            if (newVal != null) {
                customerMemo.setIsPending(newVal);
            }
        });

        reasonField.textProperty().addListener((observable, oldVal, newVal) -> {
            customerMemo.setReason(newVal);
        });

        confirmButton.setOnAction(actionEvent -> insertMemo(customerMemo));
    }

    private void updateAppliedAmount() {
        double totalAppliedAmount = invoicesForMemo.stream()
                .mapToDouble(MemoInvoiceApplication::getAmount)
                .sum() + collectionsForMemo.stream()
                .mapToDouble(MemoCollectionApplication::getAmount)
                .sum();

        customerMemo.setAppliedAmount(totalAppliedAmount);

        appliedAmountField.setText(String.valueOf(Math.max(totalAppliedAmount, 0)));
    }

    private void insertMemo(CustomerMemo customerMemo) {
        if (customerMemo == null || amountField == null || amountField.getText() == null || amountField.getText().trim().isEmpty()) {
            DialogUtils.showErrorMessage("Error", "Invalid input: Customer memo or amount field is empty.");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountField.getText().trim());
            if (amount <= 0) {
                amountField.requestFocus();
                DialogUtils.showErrorMessage("Error", "Please input a valid positive amount.");
                return;
            }
        } catch (NumberFormatException e) {
            amountField.requestFocus();
            DialogUtils.showErrorMessage("Error", "Amount must be a valid number.");
            return;
        }

        customerMemo.setIsPending(isPending.isSelected());
        boolean create = customerMemoDAO.createCustomerMemo(customerMemo);
        if (create) {
            DialogUtils.showCompletionDialog("Success", "Memo successfully created");
            customerCreditDebitListController.customerMemos.add(customerMemo);
            customerCreditDebitListController.memoTable.getSelectionModel().select(customerMemo);
            customerCreditDebitListController.memoStage.close();
        } else {
            DialogUtils.showErrorMessage("Error", "Memo creation failed, please contact your developer");
        }

    }


    ObservableList<ChartOfAccounts> chartOfAccounts = FXCollections.observableArrayList();
    ObservableList<Supplier> suppliers = FXCollections.observableArrayList();
    ObservableList<Salesman> salesmen = FXCollections.observableArrayList();
    ObservableList<Customer> customers = FXCollections.observableArrayList();

    public void initializeEncodingData() {
        chartOfAccounts.setAll(chartOfAccountsDAO.getAllChartOfAccounts());
        suppliers.setAll(supplierDAO.getAllActiveSuppliers());
        salesmen.setAll(salesmanDAO.getAllSalesmen());
        customers.setAll(customerDAO.getAllActiveCustomers());
    }

    @Setter
    CustomerCreditDebitListController customerCreditDebitListController;

    public void openExistingMemo(CustomerMemo selectedItem) {
        customerMemo = selectedItem;
        this.statusComboBox.setValue(customerMemo.getStatus());
        this.balanceTypeComboBox.setValue(customerMemo.getBalanceType());
        this.isPending.setSelected(customerMemo.getIsPending());
        this.docNo.setText(customerMemo.getMemoNumber());
        this.coaField.setText(customerMemo.getChartOfAccount().getAccountTitle());
        this.supplierField.setText(customerMemo.getSupplier().getSupplierName());
        this.salesmanField.setText(customerMemo.getSalesman().getSalesmanName());
        this.salesmanCodeField.setText(customerMemo.getSalesman().getSalesmanCode());
        this.storeNameField.setText(customerMemo.getCustomer().getStoreName());
        this.customerCodeCol.setText(customerMemo.getCustomer().getCustomerCode());
        this.amountField.setText(String.valueOf(customerMemo.getAmount()));
        this.appliedAmountField.setText(String.valueOf(customerMemo.getAppliedAmount()));
        this.reasonField.setText(customerMemo.getReason());

        if (customerMemo.getStatus().equals(CustomerMemo.MemoStatus.FOR_APPROVAL)) {
            confirmButton.setText("Approve");
            confirmButton.setOnAction(actionEvent -> approveMemo(customerMemo));
        } else if (customerMemo.getStatus().equals(CustomerMemo.MemoStatus.APPROVED)) {
            confirmButton.setText("Update");
            confirmButton.setOnAction(actionEvent -> updateMemo(customerMemo));
            itemsSplitPane.setDisable(false);
        }
    }

    private void updateMemo(CustomerMemo customerMemo) {
        if (customerMemo == null) {
            DialogUtils.showErrorMessage("Error", "No memo selected for update.");
            return;
        }
        if (customerMemo.getAppliedAmount() > customerMemo.getAmount()) {
            DialogUtils.showErrorMessage("Error", "You cant apply more than the memo amount");
            return;
        }

        try {
            double amount = Double.parseDouble(amountField.getText().trim());
            if (amount <= 0) {
                DialogUtils.showErrorMessage("Error", "Amount must be greater than zero.");
                return;
            }
            customerMemo.setAmount(amount);
        } catch (NumberFormatException e) {
            DialogUtils.showErrorMessage("Error", "Invalid amount format.");
            return;
        }

        customerMemo.setReason(reasonField.getText());
        customerMemo.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));
        customerMemo.setIsPending(isPending.isSelected());
        customerMemo.setChartOfAccount(chartOfAccounts.stream()
                .filter(coa -> coa.getAccountTitle().equals(coaField.getText()))
                .findFirst()
                .orElse(null));
        customerMemo.setCustomer(customers.stream()
                .filter(customer -> customer.getStoreName().equals(storeNameField.getText()) && customer.getCustomerCode().equals(customerCodeCol.getText()))
                .findFirst()
                .orElse(null));
        boolean updated = customerMemoDAO.updateCustomerMemo(customerMemo);
        if (updated) {
            DialogUtils.showCompletionDialog("Success", "Memo successfully updated.");
        } else {
            DialogUtils.showErrorMessage("Error", "Failed to update memo.");
        }
    }


    private void approveMemo(CustomerMemo customerMemo) {
        customerMemo.setStatus(CustomerMemo.MemoStatus.APPROVED);
        boolean approved = customerMemoDAO.updateCustomerMemo(customerMemo);
        if (approved) {
            DialogUtils.showCompletionDialog("Approved", "Memo approved");
        } else {
            DialogUtils.showErrorMessage("Error", "Approval did not proceed, please contact your system developer");
        }
        statusComboBox.setValue(customerMemo.getStatus());
        itemsSplitPane.setDisable(false);
    }
}
