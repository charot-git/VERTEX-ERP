package com.vertex.vos;

import com.vertex.vos.Objects.PurchaseOrder;
import com.vertex.vos.Utilities.AmountsToWord;
import com.vertex.vos.Utilities.TextFieldUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.net.URL;
import java.util.ResourceBundle;

public class VoucherFormController implements Initializable {

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
    private TableView<?> adjustmentHistoryTable;

    @FXML
    private Label amountInWords;

    @FXML
    private VBox bankBox;

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
    private TableView<?> paymentHistoryTable;

    @FXML
    private Label paymentTerms;

    @FXML
    private ComboBox<?> paymentType;

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

    public void initData(PurchaseOrder selectedOrder) {
        orderNo.setText("Order #" + selectedOrder.getPurchaseOrderNo());
        supplier.setValue(selectedOrder.getSupplierNameString());
        receiptCheckBox.setSelected(selectedOrder.getReceiptRequired());
        leadTimePaymentDatePicker.setValue(selectedOrder.getLeadTimePayment());
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        TextFieldUtils.addDoubleInputRestriction(voucherPaymentAmount);
        TextFieldUtils.addBillionRestriction(voucherPaymentAmount);
        voucherPaymentAmount.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                BigDecimal amount = new BigDecimal(newValue);
                if (amount.compareTo(BigDecimal.ZERO) < 0) {
                    amountInWords.setText("No Amount");
                }
                else {
                    amountInWords.setText(AmountsToWord.convertToWords(amount));                }
            }
        });

    }
}
