package com.vertex.vos;

import com.vertex.vos.Objects.SupplierCreditDebitMemo;
import com.vertex.vos.Objects.PurchaseOrder;
import com.vertex.vos.Utilities.DialogUtils;
import com.vertex.vos.Utilities.SupplierMemoDAO;
import com.vertex.vos.Utilities.ConfirmationAlert;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;

public class CreditDebitSelectorController {

    @FXML
    private TableColumn<SupplierCreditDebitMemo, String> documentNumberCol;

    @FXML
    private TableColumn<SupplierCreditDebitMemo, Double> amountCol;

    @FXML
    private TableColumn<SupplierCreditDebitMemo, String> reasonCol;

    @FXML
    private TableColumn<SupplierCreditDebitMemo, String> chartOfAccountCol;

    @FXML
    private Label creditLabel;

    @FXML
    private ComboBox<String> creditTarget;

    @FXML
    private TextField documentNumber;

    @FXML
    private Label header;

    @FXML
    private TableView<SupplierCreditDebitMemo> memoTable;

    private SupplierCreditDebitListController parentController;

    SupplierMemoDAO supplierMemoDAO = new SupplierMemoDAO();

    private SupplierCreditDebitMemo selectedMemo;
    private final ObservableList<SupplierCreditDebitMemo> selectedMemos = FXCollections.observableArrayList();

    public void initialize() {
        initializeTableColumns();
        setupTableDoubleClickHandler();
        setupTableEnterKeyHandler();
    }

    private void initializeTableColumns() {
        documentNumberCol.setCellValueFactory(new PropertyValueFactory<>("memoNumber"));
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        reasonCol.setCellValueFactory(new PropertyValueFactory<>("reason"));
        chartOfAccountCol.setCellValueFactory(new PropertyValueFactory<>("chartOfAccountName"));
    }

    private void setupTableDoubleClickHandler() {
        memoTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) { // Double-click detected
                selectedMemo = memoTable.getSelectionModel().getSelectedItem();
                if (selectedMemo != null && !selectedMemos.contains(selectedMemo)) {
                    addSelectedMemoToList(selectedMemo);
                }
            }
        });
    }

    private void setupTableEnterKeyHandler() {
        memoTable.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                selectedMemo = memoTable.getSelectionModel().getSelectedItem();
                if (selectedMemo != null && !selectedMemos.contains(selectedMemo)) {
                    addSelectedMemoToList(selectedMemo);
                }
            }
        });
    }

    public void addNewSupplierCreditMemoToAdjustment(PurchaseOrder selectedPurchaseOrder) {
        updateMemoAdjustment("Supplier Credit Memos", selectedPurchaseOrder, true);
    }

    public void addNewSupplierDebitMemoToAdjustment(PurchaseOrder selectedPurchaseOrder) {
        updateMemoAdjustment("Supplier Debit Memos", selectedPurchaseOrder, false);
    }

    private void updateMemoAdjustment(String headerText, PurchaseOrder selectedPurchaseOrder, boolean isCreditMemo) {
        header.setText(headerText);
        creditTarget.setValue(selectedPurchaseOrder.getSupplierNameString());
        ObservableList<SupplierCreditDebitMemo> supplierMemos;
        if (isCreditMemo) {
            supplierMemos = supplierMemoDAO.getSupplierCreditMemos(selectedPurchaseOrder.getSupplierName());
        } else {
            supplierMemos = supplierMemoDAO.getSupplierDebitMemos(selectedPurchaseOrder.getSupplierName());
        }
        memoTable.setItems(supplierMemos);
    }


    public void setPayablesController(PayablesFormController payablesFormController) {
        this.payablesFormController = payablesFormController;
    }

    private PayablesFormController payablesFormController;

    private void addSelectedMemoToList(SupplierCreditDebitMemo memo) {
        if (!selectedMemos.contains(memo)) {
            ConfirmationAlert confirmationAlert = new ConfirmationAlert("Confirmation", "Add Memo to List?",
                    "Are you sure you want to add this memo to list?", false);

            if (confirmationAlert.showAndWait()) {
                selectedMemos.add(memo);
                memoTable.getItems().remove(memo);
                if (payablesFormController != null) {
                    payablesFormController.receiveSelectedMemo(memo);
                }else if(voucherFormController != null){
                    voucherFormController.receiveSelectedMemo(memo);
                }
            }
        } else {
            DialogUtils.showErrorMessage("Already Selected", "Memo already selected");
        }
    }

    VoucherFormController voucherFormController;
    public void setVoucherController(VoucherFormController voucherFormController) {
        this.voucherFormController = voucherFormController;
    }
}
