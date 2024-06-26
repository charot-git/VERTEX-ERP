package com.vertex.vos;

import com.vertex.vos.Constructors.PurchaseOrder;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.time.LocalDate;

public class PurchaseOrdersPerSupplierForPaymentController {
    @FXML
    private ComboBox<String> supplier;
    @FXML
    private TableView<PurchaseOrder> purchaseOrdersForPayment;
    @FXML
    private TableColumn<PurchaseOrder, Integer> orderNo;
    @FXML
    private TableColumn<PurchaseOrder, Double> totalAmount;
    @FXML
    private TableColumn<PurchaseOrder, String> paymentStatus;
    @FXML
    private TableColumn<PurchaseOrder, LocalDate> paymentDue;

    void loadPurchaseOrdersForPayment() {
    }
}
