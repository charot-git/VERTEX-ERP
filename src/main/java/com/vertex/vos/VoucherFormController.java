package com.vertex.vos;

import com.vertex.vos.DAO.PurchaseOrderPaymentDAO;
import com.vertex.vos.DAO.PurchaseOrderVoucherDAO;
import com.vertex.vos.Objects.*;
import com.vertex.vos.Utilities.*;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleDoubleProperty;
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
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Timestamp;
import java.util.List;
import java.util.ResourceBundle;

public class VoucherFormController implements Initializable {

    public Label paymentStatus;
    public Label transactionStatus;
    public TextField accountNumberTextField;
    public VBox referenceBox;
    public TextField referenceNoTextField;
    public TableView<PurchaseOrderVoucher> voucherTableView;
    public TableColumn<PurchaseOrderVoucher, String> coaColumn;
    public TableColumn<PurchaseOrderVoucher, String> bankColumn;
    public TableColumn<PurchaseOrderVoucher, String> refNoColumn;
    public TableColumn<PurchaseOrderVoucher, Double> amountColumn;
    public TableColumn<PurchaseOrderVoucher, Timestamp> voucherDate;
    public Button addVoucher;
    public TableColumn<PurchaseOrderVoucher, String> statusColumn;
    @FXML
    private HBox addCreditMemo;

    @FXML
    private HBox addDebitMemo;

    @FXML
    private Label addProductLabel;

    @FXML
    private Label addProductLabel1;

    @FXML
    private Tab adjustmentHistoryTab;

    @FXML
    private TableView<CreditDebitMemo> adjustmentHistoryTable;

    @FXML
    private Label amountInWords;

    @FXML
    private VBox bankBox;

    @FXML
    private HBox voucherHBox;

    @FXML
    private Label businesTypeLabel11;

    @FXML
    private ComboBox<?> chartOfAccount1;

    @FXML
    private Button confirmButton;

    @FXML
    private AnchorPane header;

    @FXML
    private VBox leadTimePaymentBox;

    @FXML
    private DatePicker leadTimePaymentDatePicker;

    @FXML
    private Label orderNo;

    @FXML
    private Tab paymentHistoryTab;

    @FXML
    private TableView<PurchaseOrderPayment> paymentHistoryTable;

    @FXML
    private Label paymentTerms;

    @FXML
    private ComboBox<String> paymentType;

    @FXML
    private VBox paymentTypeBox;

    @FXML
    private CheckBox receiptCheckBox;

    @FXML
    private Label receivingTerms;

    @FXML
    private ComboBox<String> supplier;

    @FXML
    private VBox supplierBox;

    @FXML
    private Tab voucherHistory;

    @FXML
    private AnchorPane voucherHistoryTab;

    @FXML
    private TextField voucherPaymentAmount;

    @FXML
    private Tab voucheringTab;

    PurchaseOrdersPerSupplierForPaymentController purchaseOrdersPerSupplierForPaymentController;

    public void setPurchaseOrderPaymentList(PurchaseOrdersPerSupplierForPaymentController purchaseOrdersPerSupplierForPaymentController) {
        this.purchaseOrdersPerSupplierForPaymentController = purchaseOrdersPerSupplierForPaymentController;
    }

    SupplierMemoDAO supplierMemoDAO = new SupplierMemoDAO();

    public void initData(PurchaseOrder selectedOrder) {
        orderNo.setText("Order #" + selectedOrder.getPurchaseOrderNo());
        supplier.setValue(selectedOrder.getSupplierNameString());
        receiptCheckBox.setSelected(selectedOrder.getReceiptRequired());
        leadTimePaymentDatePicker.setValue(selectedOrder.getLeadTimePayment());
        transactionStatus.setText(selectedOrder.getInventoryStatusString());
        paymentStatus.setText(selectedOrder.getPaymentStatusString());


        setUpPaymentHistoryTable();
        setPaymentHistoryData(selectedOrder);

        setUpAdjustmentHistoryTable();
        setAdjustmentHistoryData(selectedOrder);

        setUpVoucherTable();
        setUpVoucherData(selectedOrder);

        addCreditMemo.setOnMouseClicked(mouseEvent -> addCreditMemoToAdjustment(selectedOrder));
        addDebitMemo.setOnMouseClicked(mouseEvent -> addDebitMemoToAdjustment(selectedOrder));
        addVoucher.setOnMouseClicked(mouseEvent -> addVoucherToPurchaseOrder(selectedOrder));

        //add listener to changes in payment history and adjustment history table
        paymentHistoryTable.getItems().addListener((ListChangeListener<PurchaseOrderPayment>) c -> {
            double totalAmountForVoucher = totalPaidAmounts + totalAdjustmentAmounts;
            voucherPaymentAmount.setText(String.valueOf(totalAmountForVoucher));
        });
        adjustmentHistoryTable.getItems().addListener((ListChangeListener<CreditDebitMemo>) c -> {
            double totalAmountForVoucher = totalPaidAmounts + totalAdjustmentAmounts;
            voucherPaymentAmount.setText(String.valueOf(totalAmountForVoucher));
        });

    }

    PurchaseOrderVoucherDAO voucherDAO = new PurchaseOrderVoucherDAO();


    private void setUpVoucherData(PurchaseOrder selectedOrder) {
        ObservableList<PurchaseOrderVoucher> initialVouchers = voucherDAO.getVouchersByOrderId(selectedOrder.getPurchaseOrderId());
        purchaseOrderVouchers.addAll(initialVouchers);
        voucherTableView.setItems(purchaseOrderVouchers);
    }

    private void setUpVoucherTable() {
        coaColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getCoa().getAccountTitle()));
        bankColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getBankAccount().getAccountNumber()));
        refNoColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getRefNo()));
        amountColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getAmount().doubleValue()).asObject());
        voucherDate.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getCreatedAt()));
        statusColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getStatus()));
    }

    private void addVoucherToPurchaseOrder(PurchaseOrder selectedOrder) {
        // Confirmation dialog to add a voucher
        ConfirmationAlert confirmationAlert = new ConfirmationAlert(
                "Voucher",
                "Add Voucher?",
                "Add voucher with the amount " + voucherPaymentAmount.getText(),
                true
        );

        boolean confirmed = confirmationAlert.showAndWait();
        if (confirmed) {
            if (!observableMemoList.isEmpty()) {
                processMemos(selectedOrder);
            }

            PurchaseOrderVoucher voucher = createVoucher(selectedOrder);

            if (voucherDAO.create(voucher)) {
                DialogUtils.showConfirmationDialog("Success", "Voucher added successfully");
                purchaseOrderVouchers.add(voucher);
            } else {
                DialogUtils.showErrorMessage("Error", "Failed to add voucher");
            }
        } else {
            DialogUtils.showConfirmationDialog("Cancelled", "Voucher addition cancelled");
        }
    }

    private void processMemos(PurchaseOrder selectedOrder) {
        for (CreditDebitMemo memo : observableMemoList) {
            if ("Processing".equals(memo.getStatus())) {
                boolean adjusted = purchaseOrderAdjustmentDAO.insertAdjustment(selectedOrder.getPurchaseOrderId(), memo);
                if (adjusted) {
                    supplierMemoDAO.updateMemoStatus(Integer.parseInt(memo.getMemoNumber()), "Applied");
                    memo.setStatus("Applied");
                }
            }
        }
    }


    private boolean validateVoucherFields() {
        try {
            // Validate payment type
            String paymentTypeString = paymentType.getValue();
            if (paymentTypeString == null || paymentTypeString.isEmpty()) {
                DialogUtils.showErrorMessage("Validation Error", "Payment type is required.");
                return false;
            }

            // Validate account number
            int accountNoInt = Integer.parseInt(accountNumberTextField.getText());
            if (accountNoInt <= 0) {
                DialogUtils.showErrorMessage("Validation Error", "Invalid account number.");
                return false;
            }

            // Validate reference number
            int referenceNoInt = Integer.parseInt(referenceNoTextField.getText());
            if (referenceNoInt <= 0) {
                DialogUtils.showErrorMessage("Validation Error", "Invalid reference number.");
                return false;
            }

            // Validate voucher payment amount
            double voucherPaymentAmountDouble = Double.parseDouble(voucherPaymentAmount.getText());
            if (voucherPaymentAmountDouble <= 0) {
                DialogUtils.showErrorMessage("Validation Error", "Voucher payment amount must be greater than zero.");
                return false;
            }
            return true;
        } catch (NumberFormatException e) {
            DialogUtils.showErrorMessage("Validation Error", "Please enter valid numbers.");
            return false;
        }
    }

    private PurchaseOrderVoucher createVoucher(PurchaseOrder selectedOrder) {
        // Create a new PurchaseOrderVoucher object and populate it with data
        PurchaseOrderVoucher voucher = new PurchaseOrderVoucher();
        voucher.setPurchaseOrder(selectedOrder);
        voucher.setSupplierId(selectedOrder.getSupplierName()); // Fetch or get the selected supplier
        voucher.setCoaId(getSelectedChartOfAccountId());
        voucher.setAmount(BigDecimal.valueOf(Double.parseDouble(voucherPaymentAmount.getText())));
        voucher.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        voucher.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        voucher.setRefNo(referenceNoTextField.getText());
        voucher.setBankAccount(getSelectedBankAccount()); // Fetch or get the selected bank account
        voucher.setCreatedBy(UserSession.getInstance().getUserId()); // Set the user who created the voucher
        voucher.setStatus("Pending");

        return voucher;
    }


    private int getSelectedChartOfAccountId() {
        String paymentTypeValue = paymentType.getValue();
        return chartOfAccountsDAO.getChartOfAccountIdByName(paymentTypeValue);
    }

    private BankAccount getSelectedBankAccount() {
        return selectedBank;
    }


    private double totalPaidAmounts;
    private double totalAdjustmentAmounts;


    private void addDebitMemoToAdjustment(PurchaseOrder selectedOrder) {
        openCreditDebitMemoSelector(selectedOrder, "Add Supplier Debit Memo", "debit", Screen.getPrimary().getVisualBounds().getMaxX() - 650, Screen.getPrimary().getVisualBounds().getMinY() + 50);
    }

    private void addCreditMemoToAdjustment(PurchaseOrder selectedOrder) {
        openCreditDebitMemoSelector(selectedOrder, "Add Supplier Credit Memo", "credit", Screen.getPrimary().getVisualBounds().getMinX() + 50, Screen.getPrimary().getVisualBounds().getMinY() + 50);
    }

    private void openCreditDebitMemoSelector(PurchaseOrder selectedOrder, String title, String type, double xPosition, double yPosition) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("CreditDebitSelector.fxml"));
            Parent root = loader.load();

            CreditDebitSelectorController controller = loader.getController();
            if (type.equals("credit")) {
                controller.addNewSupplierCreditMemoToAdjustment(selectedOrder);
            } else if (type.equals("debit")) {
                controller.addNewSupplierDebitMemoToAdjustment(selectedOrder);
            } else {
                throw new IllegalArgumentException("Invalid type: " + type);
            }
            controller.setVoucherController(this);

            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.setX(xPosition);
            stage.setY(yPosition);

            stage.showAndWait();
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
    }


    PurchaseOrderAdjustmentDAO purchaseOrderAdjustmentDAO = new PurchaseOrderAdjustmentDAO();

    ObservableList<CreditDebitMemo> observableMemoList = FXCollections.observableArrayList();
    ObservableList<PurchaseOrderVoucher> purchaseOrderVouchers = FXCollections.observableArrayList();


    private void setAdjustmentHistoryData(PurchaseOrder selectedOrder) {
        List<CreditDebitMemo> memoList = purchaseOrderAdjustmentDAO.getAdjustmentsByPurchaseOrderId(selectedOrder.getPurchaseOrderId());
        observableMemoList.addAll(memoList);
        adjustmentHistoryTable.setItems(observableMemoList);

        observableMemoList.addListener((ListChangeListener<CreditDebitMemo>) c -> {
            totalAdjustmentAmounts = 0.0;
            for (CreditDebitMemo memo : observableMemoList) {
                if (memo.getType() == 1) {
                    totalAdjustmentAmounts -= memo.getAmount();
                } else if (memo.getType() == 2) {
                    totalAdjustmentAmounts += memo.getAmount();
                }
            }
        });
    }

    private void setUpAdjustmentHistoryTable() {
        adjustmentHistoryTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        TableColumn<CreditDebitMemo, String> typeCol = new TableColumn<>("Memo Type");
        typeCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTypeName()));
        TableColumn<CreditDebitMemo, String> chartOfAccountCol = new TableColumn<>("Chart Of Account");
        chartOfAccountCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getChartOfAccountName()));
        TableColumn<CreditDebitMemo, String> reasonCol = new TableColumn<>("Reason");
        reasonCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getReason()));
        TableColumn<CreditDebitMemo, Double> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getAmount()).asObject());
        TableColumn<CreditDebitMemo, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus()));
        adjustmentHistoryTable.getColumns().addAll(typeCol, chartOfAccountCol, reasonCol, amountCol, statusCol);
    }


    PurchaseOrderPaymentDAO purchaseOrderPaymentDAO = new PurchaseOrderPaymentDAO();

    private void setPaymentHistoryData(PurchaseOrder selectedOrder) {
        ObservableList<PurchaseOrderPayment> purchaseOrderPayments = purchaseOrderPaymentDAO.getSupplierPayments(selectedOrder.getPurchaseOrderNo());
        paymentHistoryTable.setItems(purchaseOrderPayments);
        totalPaidAmounts = 0.0;
        for (PurchaseOrderPayment purchaseOrderPayment : purchaseOrderPayments) {
            totalPaidAmounts += purchaseOrderPayment.getPaidAmount();
        }
    }

    private void setUpPaymentHistoryTable() {
        TableColumn<PurchaseOrderPayment, String> chartOfAccountCol = new TableColumn<>("Chart Of Account");
        chartOfAccountCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getChartOfAccountName()));
        TableColumn<PurchaseOrderPayment, Double> paidAmountCol = new TableColumn<>("Paid Amount");
        paidAmountCol.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getPaidAmount()).asObject());
        TableColumn<PurchaseOrderPayment, Timestamp> createdAtCol = new TableColumn<>("Paid At");
        createdAtCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getCreatedAt()));
        paymentHistoryTable.getColumns().addAll(chartOfAccountCol, paidAmountCol, createdAtCol);

        paymentHistoryTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
    }

    ChartOfAccountsDAO chartOfAccountsDAO = new ChartOfAccountsDAO();
    BankAccountDAO bankAccountDAO = new BankAccountDAO();
    ObservableList<String> paymentTypeList = FXCollections.observableArrayList();
    ObservableList<BankAccount> bankAccountList = FXCollections.observableArrayList();
    BankAccount selectedBank;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        TableViewFormatter.formatTableView(adjustmentHistoryTable);
        TableViewFormatter.formatTableView(paymentHistoryTable);
        TextFieldUtils.addDoubleInputRestriction(voucherPaymentAmount);
        TextFieldUtils.addBillionRestriction(voucherPaymentAmount);
        voucherHBox.getChildren().remove(bankBox);
        paymentTypeList.setAll(chartOfAccountsDAO.getAllAccountNames());
        bankAccountList.setAll(bankAccountDAO.getAllBankAccounts());
        paymentType.setItems(paymentTypeList);
        ComboBoxFilterUtil.setupComboBoxFilter(paymentType, paymentTypeList);

        paymentType.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                if (newValue.equals("Cash in Bank")) {
                    GenericSelectionWindow<BankAccount> selectionWindow = new GenericSelectionWindow<>();

                    Stage primaryStage = (Stage) voucherHBox.getScene().getWindow();
                    selectedBank = selectionWindow.showSelectionWindow(primaryStage, "Select a Bank Account", bankAccountList);

                    if (selectedBank != null) {
                        accountNumberTextField.setText(selectedBank.getAccountNumber());
                    }
                    int desiredIndex = 1; // Or calculate based on your specific layout requirements

                    // Add bankBox at the desired index
                    if (desiredIndex > voucherHBox.getChildren().size()) {
                        voucherHBox.getChildren().add(bankBox); // Add to the end if index is out of bounds
                    } else {
                        voucherHBox.getChildren().add(desiredIndex, bankBox);
                    }
                } else {
                    selectedBank = null;
                    voucherHBox.getChildren().remove(bankBox);
                }
            }
        });


        voucherPaymentAmount.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                BigDecimal amount = new BigDecimal(newValue);
                if (amount.compareTo(BigDecimal.ZERO) < 0) {
                    amountInWords.setText("No Amount");
                } else {
                    amountInWords.setText(AmountsToWord.convertToWords(amount));
                }
            }
        });

    }

    public void receiveSelectedMemo(CreditDebitMemo memo) {
        memo.setStatus("Processing");
        observableMemoList.add(memo);
    }

    public void openVoucher(SupplierAccounts selectedAccount) {
        PurchaseOrderVoucher voucher = voucherDAO.getById(Integer.parseInt(selectedAccount.getDocumentNumber()));
        initData(voucher.getPurchaseOrder());
    }
}
