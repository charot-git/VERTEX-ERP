package com.vertex.vos;

import com.vertex.vos.Constructors.ComboBoxFilterUtil;
import com.vertex.vos.Constructors.PurchaseOrder;
import com.vertex.vos.Constructors.Supplier;
import com.vertex.vos.Utilities.PurchaseOrderDAO;
import com.vertex.vos.Utilities.PurchaseOrderProductDAO;
import com.vertex.vos.Utilities.SupplierDAO;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class PurchaseOrdersPerSupplierForPaymentController implements Initializable {
    @FXML
    private ComboBox<String> supplier;
    @FXML
    private TableView<PurchaseOrder> purchaseOrdersForPayment;
    @FXML
    private TableColumn<PurchaseOrder, Integer> orderNo;
    @FXML
    private TableColumn<PurchaseOrder, BigDecimal> totalAmount;
    @FXML
    private TableColumn<PurchaseOrder, String> paymentStatus;
    @FXML
    private TableColumn<PurchaseOrder, LocalDate> placedOn;
    @FXML
    private TableColumn<PurchaseOrder, String> paymentDue;

    SupplierDAO supplierDAO = new SupplierDAO();
    PurchaseOrderDAO purchaseOrderDAO = new PurchaseOrderDAO();


    void loadPurchaseOrdersForPayment() {
        ObservableList<String> supplierNames = supplierDAO.getAllSupplierNames();
        ComboBoxFilterUtil.setupComboBoxFilter(supplier, supplierNames);
        supplier.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                purchaseOrdersForPayment.getItems().clear();
                int supplierId = supplierDAO.getSupplierIdByName(newValue);
                try {
                    List<PurchaseOrder> purchaseOrders = purchaseOrderDAO.getPurchaserOrdersForPaymentBySupplier(supplierId);
                    if (!purchaseOrders.isEmpty()) {
                        ObservableList<PurchaseOrder> purchaseOrderObservableList = FXCollections.observableList(purchaseOrders);
                        purchaseOrdersForPayment.setItems(purchaseOrderObservableList);
                    }

                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        });

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        orderNo.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getPurchaseOrderNo()).asObject());
        totalAmount.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getTotalAmount()));
        placedOn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getDateApproved().toLocalDate()));
        paymentStatus.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPaymentStatusString()));

        paymentDue.setCellValueFactory(cellData -> {
            LocalDate leadTimePayment = cellData.getValue().getLeadTimePayment();
            String leadTimePaymentString = (leadTimePayment != null) ? leadTimePayment.toString() : "Not set yet";
            return new SimpleStringProperty(leadTimePaymentString);
        });

        loadPurchaseOrdersForPayment();
    }

}
