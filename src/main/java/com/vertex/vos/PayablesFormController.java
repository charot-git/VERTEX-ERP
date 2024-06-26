package com.vertex.vos;

import com.vertex.vos.Constructors.PurchaseOrder;
import com.vertex.vos.Constructors.Supplier;
import com.vertex.vos.Utilities.ChartOfAccountsDAO;
import com.vertex.vos.Utilities.DeliveryTermsDAO;
import com.vertex.vos.Utilities.PaymentTermsDAO;
import com.vertex.vos.Utilities.SupplierDAO;
import javafx.scene.layout.AnchorPane;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.time.LocalDate;

public class PayablesFormController {
    AnchorPane contentPane;
    @FXML
    private VBox POContent;

    @FXML
    private VBox addBoxes;

    @FXML
    private HBox addCreditMemo;

    @FXML
    private HBox addDebitMemo;

    @FXML
    private Label addProductLabel;

    @FXML
    private Label addProductLabel1;

    @FXML
    private Label businessTypeLabel;

    @FXML
    private Label businessTypeLabel1;

    @FXML
    private ComboBox<String> chartOfAccount;

    @FXML
    private Label chartOfAccountErr;

    @FXML
    private HBox confirmBox;

    @FXML
    private Button confirmButton;

    @FXML
    private Label discounted;

    @FXML
    private Label grandTotal;

    @FXML
    private Label gross;

    @FXML
    private HBox leadTimeBox;

    @FXML
    private VBox leadTimePaymentBox;

    @FXML
    private DatePicker leadTimePaymentDatePicker;

    @FXML
    private Label paymentTerms;

    @FXML
    private CheckBox receiptCheckBox;

    @FXML
    private Label receivingTerms;

    @FXML
    private HBox statusBox;

    @FXML
    private ImageView statusImage;

    @FXML
    private Label statusLabel;

    @FXML
    private ComboBox<String> supplier;

    @FXML
    private VBox supplierBox;

    @FXML
    private Label supplierErr;

    @FXML
    private HBox totalBox;

    @FXML
    private VBox totalBoxLabels;

    @FXML
    private VBox totalVBox;

    @FXML
    private Label vat;

    @FXML
    private Label withholding;
    @FXML
    private Label orderNo;

    void setContentPane(AnchorPane contentPane) {
        this.contentPane = contentPane;
    }

    PurchaseOrdersPerSupplierForPaymentController purchaseOrdersPerSupplierForPaymentController;

    void setPurchaseOrderPaymentList(PurchaseOrdersPerSupplierForPaymentController purchaseOrdersPerSupplierForPaymentController) {
        this.purchaseOrdersPerSupplierForPaymentController = purchaseOrdersPerSupplierForPaymentController;
    }

    ChartOfAccountsDAO chartOfAccountsDAO = new ChartOfAccountsDAO();
    PaymentTermsDAO paymentTermsDAO = new PaymentTermsDAO();
    DeliveryTermsDAO deliveryTermsDAO = new DeliveryTermsDAO();

    void initializePayment(PurchaseOrder selectedOrder) throws SQLException {
        orderNo.setText("ORDER#" + selectedOrder.getPurchaseOrderNo());

        paymentTerms.setText(paymentTermsDAO.getPaymentTermNameById(selectedOrder.getPaymentType()));
        receivingTerms.setText(deliveryTermsDAO.getDeliveryNameById(selectedOrder.getReceivingType()));
        supplier.setValue(selectedOrder.getSupplierNameString());
        supplier.setDisable(true);
        leadTimePaymentDatePicker.setPromptText(String.valueOf(LocalDate.now()));
        chartOfAccount.setItems(chartOfAccountsDAO.getAllAccountNames());
        receiptCheckBox.setSelected(selectedOrder.getReceiptRequired());
        statusLabel.setText(selectedOrder.getPaymentStatusString());

    }
}
