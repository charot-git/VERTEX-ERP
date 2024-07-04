package com.vertex.vos;

import com.vertex.vos.Objects.CreditDebitMemo;
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
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

import java.util.HashSet;
import java.util.Set;

public class CreditDebitSelectorController {

    @FXML
    private TableColumn<CreditDebitMemo, String> documentNumberCol;

    @FXML
    private TableColumn<CreditDebitMemo, Double> amountCol;

    @FXML
    private TableColumn<CreditDebitMemo, String> reasonCol;

    @FXML
    private TableColumn<CreditDebitMemo, String> chartOfAccountCol;

    @FXML
    private Label creditLabel;

    @FXML
    private ComboBox<String> creditTarget;

    @FXML
    private TextField documentNumber;

    @FXML
    private Label header;

    @FXML
    private TableView<CreditDebitMemo> memoTable;

    private CreditDebitListController parentController;

    SupplierMemoDAO supplierMemoDAO = new SupplierMemoDAO();

    private CreditDebitMemo selectedMemo;
    private ObservableList<CreditDebitMemo> selectedMemos = FXCollections.observableArrayList();

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
                    addSelectedMemoToPayables(selectedMemo);
                }
            }
        });
    }

    private void setupTableEnterKeyHandler() {
        memoTable.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                selectedMemo = memoTable.getSelectionModel().getSelectedItem();
                if (selectedMemo != null && !selectedMemos.contains(selectedMemo)) {
                    addSelectedMemoToPayables(selectedMemo);
                }
            }
        });
    }

    public void addNewSupplierCreditMemoToAdjustment(PurchaseOrder selectedPurchaseOrder) {
        creditTarget.setValue(selectedPurchaseOrder.getSupplierNameString());
        ObservableList<CreditDebitMemo> supplierCreditMemos = FXCollections.observableArrayList();
        supplierCreditMemos.setAll(supplierMemoDAO.getSupplierCreditMemos(selectedPurchaseOrder.getSupplierName()));
        memoTable.setItems(supplierCreditMemos);
    }

    public void addNewSupplierDebitMemoToAdjustment(PurchaseOrder selectedPurchaseOrder) {
        creditTarget.setValue(selectedPurchaseOrder.getSupplierNameString());
        ObservableList<CreditDebitMemo> supplierDebitMemos = FXCollections.observableArrayList();
        supplierDebitMemos.setAll(supplierMemoDAO.getSupplierDebitMemos(selectedPurchaseOrder.getSupplierName()));
        memoTable.setItems(supplierDebitMemos);
    }

    public void setPayablesController(PayablesFormController payablesFormController) {
        this.payablesFormController = payablesFormController;
    }

    private PayablesFormController payablesFormController;

    private void addSelectedMemoToPayables(CreditDebitMemo memo) {
        if (!selectedMemos.contains(memo)) {
            ConfirmationAlert confirmationAlert = new ConfirmationAlert("Confirmation", "Add Memo to Payables?",
                    "Are you sure you want to add this memo to payables?", false);

            if (confirmationAlert.showAndWait()) {
                selectedMemos.add(memo);
                memoTable.getItems().remove(memo);
                payablesFormController.receiveSelectedMemo(memo);
            }
        } else {
            DialogUtils.showErrorMessage("Already Selected", "Memo already selected");
        }
    }


    public void setExistingMemoList(ObservableList<CreditDebitMemo> adjustmentMemos) {
        selectedMemos.addAll(adjustmentMemos);
    }
}
