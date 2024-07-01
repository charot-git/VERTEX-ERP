package com.vertex.vos;

import com.vertex.vos.Objects.ComboBoxFilterUtil;
import com.vertex.vos.Objects.ProductsInTransact;
import com.vertex.vos.Objects.PurchaseOrder;
import com.vertex.vos.Utilities.*;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class PayablesFormController implements Initializable {
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
    @FXML
    private TableView<ProductsInTransact> productsTable;
    @FXML
    private TableView<?> adjustmentsTable;

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
        setUpProductTable(selectedOrder);
        orderNo.setText("ORDER#" + selectedOrder.getPurchaseOrderNo());
        paymentTerms.setText(paymentTermsDAO.getPaymentTermNameById(selectedOrder.getPaymentType()));
        receivingTerms.setText(deliveryTermsDAO.getDeliveryNameById(selectedOrder.getReceivingType()));
        supplier.setValue(selectedOrder.getSupplierNameString());
        supplier.setDisable(true);
        leadTimePaymentDatePicker.setPromptText(String.valueOf(LocalDate.now()));
        receiptCheckBox.setSelected(selectedOrder.getReceiptRequired());
        statusLabel.setText(selectedOrder.getPaymentStatusString());

        if (selectedOrder.getPaymentType() == 1) {
            loadPayableProductsForCashOnDelivery(selectedOrder);
        } else if (selectedOrder.getPaymentType() == 2) {
            loadPayableProductsForCashWithOrder(selectedOrder);
        }

    }

    private void loadPayableProductsForCashWithOrder(PurchaseOrder selectedOrder) {
    }

    PurchaseOrderProductDAO purchaseOrderProductDAO = new PurchaseOrderProductDAO();

    private void loadPayableProductsForCashOnDelivery(PurchaseOrder selectedOrder) throws SQLException {
        ObservableList<ProductsInTransact> codProducts = FXCollections.observableList(purchaseOrderProductDAO.getProductsForPayment(selectedOrder.getPurchaseOrderNo()));
        productsTable.setItems(codProducts);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ComboBoxFilterUtil.setupComboBoxFilter(chartOfAccount, chartOfAccountsDAO.getAllAccountNames());
        TableViewFormatter.formatTableView(productsTable);
    }

    private void setUpProductTable(PurchaseOrder selectedOrder) {
        TableColumn<ProductsInTransact, String> invoiceColumn = new TableColumn<>("Invoice");
        invoiceColumn.setCellValueFactory(new PropertyValueFactory<>("receiptNo"));

        TableColumn<ProductsInTransact, String> descriptionColumn = new TableColumn<>("Description");
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        TableColumn<ProductsInTransact, String> unitColumn = new TableColumn<>("Unit");
        unitColumn.setCellValueFactory(new PropertyValueFactory<>("unit"));

        TableColumn<ProductsInTransact, Double> unitPriceColumn = getUnitPriceForPayment(selectedOrder);

        TableColumn<ProductsInTransact, Integer> receivedQuantityColumn = new TableColumn<>("Received Quantity");
        receivedQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("receivedQuantity"));

        TableColumn<ProductsInTransact, Double> vatAmountColumn = new TableColumn<>("VAT Amount");
        vatAmountColumn.setCellValueFactory(new PropertyValueFactory<>("vatAmount"));

        TableColumn<ProductsInTransact, Double> totalAmountColumn = getTotalAmountColumn();

        TableColumn<ProductsInTransact, Double> amountToPayColumn = getPayablesAmountColumn();

        productsTable.getColumns().addAll(invoiceColumn, descriptionColumn, unitColumn, unitPriceColumn, receivedQuantityColumn, vatAmountColumn, totalAmountColumn, amountToPayColumn);
    }

    private static TableColumn<ProductsInTransact, Double> getPayablesAmountColumn() {
        TableColumn<ProductsInTransact, Double> amountToPayColumn = new TableColumn<>("Amount To Pay");
        amountToPayColumn.setCellValueFactory(cellData -> {
            ProductsInTransact product = cellData.getValue();
            BigDecimal totalAmount = BigDecimal.valueOf(product.getTotalAmount());
            BigDecimal vatAmount = VATCalculator.calculateVat(totalAmount);
            BigDecimal amountToPay = totalAmount.add(vatAmount);
            return new SimpleObjectProperty<>(amountToPay.doubleValue());
        });
        return amountToPayColumn;
    }

    private TableColumn<ProductsInTransact, Double> getUnitPriceForPayment(PurchaseOrder selectedOrder) {
        TableColumn<ProductsInTransact, Double> unitPriceColumn = new TableColumn<>("Unit Price");
        unitPriceColumn.setCellValueFactory(cellData -> {
            ProductsInTransact product = cellData.getValue();

            double calculatedUnitPrice = 0;
            try {
                calculatedUnitPrice = calculateUnitPrice(product, selectedOrder);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            product.setUnitPrice(calculatedUnitPrice);

            // Return the calculated value
            return new SimpleDoubleProperty(calculatedUnitPrice).asObject();
        });
        return unitPriceColumn;
    }

    DiscountDAO discountDAO = new DiscountDAO();

    private double calculateUnitPrice(ProductsInTransact product, PurchaseOrder selectedOrder) throws SQLException {
        int discountTypeId = discountDAO.getProductDiscountForProductTypeId(product.getProductId(), selectedOrder.getSupplierName());
        if (discountTypeId == -1) {
            return product.getUnitPrice();
        } else {
            BigDecimal listPrice = BigDecimal.valueOf(product.getUnitPrice());
            List<BigDecimal> lineDiscounts = discountDAO.getLineDiscountsByDiscountTypeId(discountTypeId);

            return DiscountCalculator.calculateDiscountedPrice(listPrice, lineDiscounts).doubleValue();
        }
    }

    private static TableColumn<ProductsInTransact, Double> getTotalAmountColumn() {
        TableColumn<ProductsInTransact, Double> totalAmountColumn = new TableColumn<>("Total Amount");
        totalAmountColumn.setCellValueFactory(cellData -> {
            ProductsInTransact product = cellData.getValue();
            double unitPrice = product.getUnitPrice();
            int receivedQuantity = product.getReceivedQuantity();
            double vatAmount = product.getVatAmount();

            double totalAmount = (unitPrice * receivedQuantity) + vatAmount;
            return new SimpleDoubleProperty(totalAmount).asObject();
        });
        totalAmountColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f", item));
                }
            }
        });
        return totalAmountColumn;
    }

}
