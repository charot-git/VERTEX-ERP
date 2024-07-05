package com.vertex.vos;

import com.vertex.vos.Objects.ComboBoxFilterUtil;
import com.vertex.vos.Objects.PurchaseOrder;
import com.vertex.vos.Utilities.PurchaseOrderDAO;
import com.vertex.vos.Utilities.SupplierDAO;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import java.io.IOException;
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
        supplier.setItems(supplierNames);
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

        purchaseOrdersForPayment.setRowFactory(tv -> {
            TableRow<PurchaseOrder> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    PurchaseOrder selectedOrder = row.getItem();
                    openPurchaseOrderForPayment(selectedOrder);
                }
            });
            return row;
        });

    }

    private void openPurchaseOrderForPayment(PurchaseOrder selectedOrder) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("PayablesForm.fxml"));
            Parent content = loader.load();
            PayablesFormController controller = loader.getController();

            controller.setPurchaseOrderPaymentList(this);
            controller.initializePayment(selectedOrder);

            Stage stage = new Stage();
            stage.setTitle("Pay Order#" + selectedOrder.getPurchaseOrderNo());
            stage.setResizable(true);
            stage.setMaximized(true);
            stage.setScene(new Scene(content));
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception according to your needs
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        orderNo.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getPurchaseOrderNo()).asObject());
        totalAmount.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getTotalAmount()));
        placedOn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getDateApproved().toLocalDate()));
        paymentStatus.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPaymentStatusString()));

        paymentDue.setCellValueFactory(cellData -> {
            LocalDate leadTimePayment = cellData.getValue().getLeadTimePayment();
            String leadTimePaymentString = (leadTimePayment != null) ? leadTimePayment.toString() : "TBD";
            return new SimpleStringProperty(leadTimePaymentString);
        });

        loadPurchaseOrdersForPayment();
    }

}
