package com.vertex.vos;

import com.vertex.vos.Objects.ComboBoxFilterUtil;
import com.vertex.vos.Objects.CreditDebitMemo;
import com.vertex.vos.Objects.ProductsInTransact;
import com.vertex.vos.Objects.PurchaseOrder;
import com.vertex.vos.Utilities.*;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

import static com.vertex.vos.Utilities.VATCalculator.calculateVat;

public class PayablesFormController implements Initializable {

    public TextField paymentAmount;
    public TextField balance;
    public TextField paidAmountTextField;

    @FXML
    public Button holdButton;
    @FXML
    private AnchorPane contentPane;

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
    private TableView<CreditDebitMemo> adjustmentsTable;

    private final ObservableList<CreditDebitMemo> adjustmentMemos = FXCollections.observableArrayList();

    private final ObservableList<ProductsInTransact> productsInTransacts = FXCollections.observableArrayList();

    private PurchaseOrdersPerSupplierForPaymentController purchaseOrdersPerSupplierForPaymentController;
    private final ChartOfAccountsDAO chartOfAccountsDAO = new ChartOfAccountsDAO();

    ObservableList<String> chartOfAccountNames = chartOfAccountsDAO.getAllAccountNames();

    private final PaymentTermsDAO paymentTermsDAO = new PaymentTermsDAO();
    private final DeliveryTermsDAO deliveryTermsDAO = new DeliveryTermsDAO();
    private final PurchaseOrderProductDAO purchaseOrderProductDAO = new PurchaseOrderProductDAO();
    private final DiscountDAO discountDAO = new DiscountDAO();


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        chartOfAccount.setItems(chartOfAccountNames);
        ComboBoxFilterUtil.setupComboBoxFilter(chartOfAccount, chartOfAccountNames);
        TableViewFormatter.formatTableView(productsTable);
        TableViewFormatter.formatTableView(adjustmentsTable);
        setUpAdjustmentsTable();
        TextFieldUtils.addDoubleInputRestriction(paymentAmount);
        TextFieldUtils.addDoubleInputRestriction(paidAmountTextField);
        TextFieldUtils.addDoubleInputRestriction(balance);
        balance.setEditable(false);
        paidAmountTextField.setEditable(false);
        adjustmentsTable.setItems(adjustmentMemos);
    }

    void setContentPane(AnchorPane contentPane) {
        this.contentPane = contentPane;
    }

    void setPurchaseOrderPaymentList(PurchaseOrdersPerSupplierForPaymentController purchaseOrdersPerSupplierForPaymentController) {
        this.purchaseOrdersPerSupplierForPaymentController = purchaseOrdersPerSupplierForPaymentController;
    }

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
        addCreditMemo.setOnMouseClicked(mouseEvent -> addCreditMemoToAdjustment(selectedOrder));
        addDebitMemo.setOnMouseClicked(mouseEvent -> addDebitMemoToAdjustment(selectedOrder));
        paidAmountTextField.setEditable(false);

        chartOfAccount.getSelectionModel().select(chartOfAccountsDAO.getChartOfAccountNameById(purchaseOrderPaymentDAO.getChartOfAccount(selectedOrder.getPurchaseOrderNo())));
        leadTimePaymentDatePicker.setValue(purchaseOrderDAO.getPurchaseOrderleadTimePayment(selectedOrder.getPurchaseOrderId()));

        CompletableFuture.supplyAsync(() -> {
            if (selectedOrder.getPaymentType() == 1) {
                return purchaseOrderProductDAO.getProductsForPaymentForCashOnDelivery(selectedOrder.getPurchaseOrderNo());
            } else if (selectedOrder.getPaymentType() == 2) {
                return purchaseOrderProductDAO.getProductsForPaymentForCashWithOrder(selectedOrder.getPurchaseOrderNo());
            }
            return null;
        }).thenApply(products -> {
            if (products != null) {
                Platform.runLater(() -> {
                    productsInTransacts.setAll(products);
                    productsTable.setItems(productsInTransacts);
                });
            }
            return products;
        }).thenAccept(products -> {
            if (products != null) {
                Platform.runLater(() -> checkIfPaid(selectedOrder, products));
            }
        }).exceptionally(ex -> {
            Platform.runLater(() -> DialogUtils.showErrorMessage("Error", "An error occurred while fetching products: " + ex.getMessage()));
            return null;
        });

        CompletableFuture.supplyAsync(() ->
                        purchaseOrderAdjustmentDAO.getAdjustmentsByPurchaseOrderId(selectedOrder.getPurchaseOrderId()))
                .thenAccept(adjustments ->
                        Platform.runLater(() -> {
                            if (adjustments != null && !adjustments.isEmpty()) {
                                adjustmentMemos.addAll(adjustments);
                                for (CreditDebitMemo adjustment : adjustments) {
                                    if (adjustment.getStatus().equals("Applied")) {
                                        if (adjustment.getType() == 1) {
                                            BigDecimal adjustmentAmount = BigDecimal.valueOf(adjustment.getAmount());
                                            selectedOrder.setPaidAmount(selectedOrder.getPaidAmount().add(adjustmentAmount));
                                        } else if (adjustment.getType() == 2) {
                                            BigDecimal adjustmentAmount = BigDecimal.valueOf(adjustment.getAmount());
                                            selectedOrder.setPaidAmount(selectedOrder.getPaidAmount().add(adjustmentAmount));
                                        }
                                    }
                                }
                            } else {
                                adjustmentsTable.setPlaceholder(new Label("No adjustments found."));
                            }
                        }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        adjustmentsTable.setPlaceholder(new Label("Error loading adjustments."));
                    });
                    ex.printStackTrace();
                    return null;
                });

        holdButton.setOnMouseClicked(event -> {
            selectedOrder.setPaymentStatus(6);
            holdPayment(selectedOrder);
        });
        confirmButton.setOnMouseClicked(event -> validateFields(selectedOrder));
    }

    private void holdPayment(PurchaseOrder selectedOrder) {
        ConfirmationAlert confirmationAlert = new ConfirmationAlert("Hold Payment", "Are you sure you want to hold payment?", "Please double check", true);
        boolean isConfirmed = confirmationAlert.showAndWait();
        if (isConfirmed) {
            boolean updated = purchaseOrderDAO.updatePurchaseOrderPaymentStatus(selectedOrder.getPurchaseOrderId(), selectedOrder.getPaymentStatus());
            if (updated) {
                purchaseOrdersPerSupplierForPaymentController.loadItemsForPayment(getSelectedSupplier());
                DialogUtils.showConfirmationDialog("Confirmation", "Payment held successfully!");
            }
        }

    }

    private void checkIfPaid(PurchaseOrder selectedOrder, List<ProductsInTransact> products) {
        try {
            selectedOrder.setBalanceAmount(BigDecimal.ZERO);
            BigDecimal paidAmountBigDecimal = purchaseOrderPaymentDAO.getTotalPaidAmountForPurchaseOrder(selectedOrder.getPurchaseOrderNo());
            if (paidAmountBigDecimal == null || paidAmountBigDecimal.equals(BigDecimal.ZERO)) {
                paidAmountTextField.setText("NO PAYMENTS YET");
                double productTotal = 0;
                for (ProductsInTransact product : products) {
                    productTotal += product.getTotalAmount();
                }
                selectedOrder.setBalanceAmount(BigDecimal.valueOf(productTotal));
                balance.setText(selectedOrder.getBalanceAmount().toString());
                selectedOrder.setPaidAmount(BigDecimal.ZERO);
                selectedOrder.setPaymentAmount(selectedOrder.getBalanceAmount());
                paymentAmount.setText(String.valueOf(selectedOrder.getPaymentAmount()));
                paidAmountTextField.setText("0.00");
            } else {
                selectedOrder.setPaidAmount(BigDecimal.ZERO);
                selectedOrder.setPaidAmount(selectedOrder.getPaidAmount().add(paidAmountBigDecimal));
                BigDecimal balancedAmountForPaidPO = selectedOrder.getTotalAmount().subtract(paidAmountBigDecimal);
                if (balancedAmountForPaidPO.equals(BigDecimal.ZERO)) {
                    selectedOrder.setBalanceAmount(BigDecimal.ZERO);
                } else {
                    selectedOrder.setBalanceAmount(selectedOrder.getTotalAmount().subtract(paidAmountBigDecimal));
                }
                selectedOrder.setBalanceAmount(selectedOrder.getBalanceAmount().subtract(paidAmountBigDecimal));
                String balanceAmount = selectedOrder.getBalanceAmount().toString();
                balance.setText(balanceAmount);
                paymentAmount.setText(String.valueOf(selectedOrder.getPaymentAmount()));
                paidAmountTextField.setText(paidAmountBigDecimal.toString());
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void validateFields(PurchaseOrder order) {
        String selectedAccount = getSelectedChartOfAccount();
        LocalDate selectedDate = getSelectedLeadTimePaymentDate();
        String selectedSupplier = getSelectedSupplier();

        if (selectedAccount == null || selectedAccount.isEmpty()) {
            showValidationError("Please select a chart of account.");
            return;
        }

        if (selectedDate == null) {
            showValidationError("Please select a lead time payment date.");
            return;
        }

        if (selectedSupplier == null) {
            showValidationError("Please select a supplier.");
            return;
        }
        processPayment(order);
    }

    private String getSelectedChartOfAccount() {
        return chartOfAccount.getSelectionModel().getSelectedItem();
    }

    private LocalDate getSelectedLeadTimePaymentDate() {
        return leadTimePaymentDatePicker.getValue();
    }

    private String getSelectedSupplier() {
        return supplier.getSelectionModel().getSelectedItem();
    }

    private void showValidationError(String message) {
        DialogUtils.showErrorMessageForValidation("Validation Error", "", message);
    }

    private void processPayment(PurchaseOrder selectedOrder) {
        BigDecimal paymentAmount = BigDecimal.valueOf(Double.parseDouble(this.paymentAmount.getText()));
        selectedOrder.setPaymentAmount(paymentAmount);
        entryPayable(selectedOrder);
    }


    PurchaseOrderAdjustmentDAO purchaseOrderAdjustmentDAO = new PurchaseOrderAdjustmentDAO();
    PurchaseOrderDAO purchaseOrderDAO = new PurchaseOrderDAO();
    SupplierMemoDAO supplierMemoDAO = new SupplierMemoDAO();
    PurchaseOrderPaymentDAO purchaseOrderPaymentDAO = new PurchaseOrderPaymentDAO();

    private void entryPayable(PurchaseOrder selectedOrder) {
        ConfirmationAlert confirmationAlert = new ConfirmationAlert("Confirm Payment", "Are you sure you want to make the payment?", "", false);
        boolean confirmed = confirmationAlert.showAndWait();

        if (confirmed) {
            for (CreditDebitMemo memo : adjustmentMemos) {
                boolean adjusted = purchaseOrderAdjustmentDAO.insertAdjustment(selectedOrder.getPurchaseOrderId(), Integer.parseInt(memo.getMemoNumber()), memo.getType());
                if (adjusted) {
                    supplierMemoDAO.updateMemoStatus(Integer.parseInt(memo.getMemoNumber()), "Applied");
                }
            }
            if (selectedOrder.getPaymentStatus() == 6) {
                DialogUtils.showErrorMessage("Error", "Payment is held.");
                return;
            }

            if (selectedOrder.getBalanceAmount().doubleValue() == 0) {
                selectedOrder.setPaymentStatus(4);
            } else {
                DialogUtils.showConfirmationDialog("Partial Payment", "Payment is partial. Please confirm payment.");
                selectedOrder.setPaymentStatus(3);
            }

            int chartOfAccountId = chartOfAccountsDAO.getChartOfAccountIdByName(chartOfAccount.getSelectionModel().getSelectedItem());

            double totalAmount = selectedOrder.getPaymentAmount().doubleValue();
            for (CreditDebitMemo memo : adjustmentMemos) {
                if (memo.getType() == 1) { // Credit
                    totalAmount += memo.getAmount();
                } else if (memo.getType() == 2) { // Debit
                    totalAmount -= memo.getAmount();
                }
            }

            boolean paymentInserted = purchaseOrderPaymentDAO.insertPayment(selectedOrder.getPurchaseOrderNo(), selectedOrder.getSupplierName(), totalAmount, chartOfAccountId);
            if (paymentInserted) {
                purchaseOrderDAO.updatePurchaseOrderPaymentStatus(selectedOrder.getPurchaseOrderId(), selectedOrder.getPaymentStatus());
                purchaseOrderDAO.updatePurchaseOrderLeadTimePayment(selectedOrder.getPurchaseOrderId(), getSelectedLeadTimePaymentDate());
                purchaseOrdersPerSupplierForPaymentController.loadItemsForPayment(getSelectedSupplier());
                DialogUtils.showConfirmationDialog("Confirmation", "Payment confirmed successfully!");
                statusLabel.setText("Paid");
                confirmButton.setDisable(true);
            } else {
                DialogUtils.showErrorMessage("Error", "Failed to insert payment record.");
            }
        }
    }


    private void setUpProductTable(PurchaseOrder selectedOrder) {
        ProgressIndicator progressIndicator = new ProgressIndicator();
        productsTable.setPlaceholder(progressIndicator);
        TableColumn<ProductsInTransact, String> invoiceColumn = new TableColumn<>("Invoice");
        invoiceColumn.setCellValueFactory(new PropertyValueFactory<>("receiptNo"));

        TableColumn<ProductsInTransact, String> descriptionColumn = new TableColumn<>("Description");
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        TableColumn<ProductsInTransact, String> unitColumn = new TableColumn<>("Unit");
        unitColumn.setCellValueFactory(new PropertyValueFactory<>("unit"));

        TableColumn<ProductsInTransact, Double> unitPriceColumn = getUnitPriceForPayment(selectedOrder);

        TableColumn<ProductsInTransact, Integer> receivedQuantityColumn = new TableColumn<>("Received Quantity");
        receivedQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("receivedQuantity"));

        TableColumn<ProductsInTransact, Double> vatAmountColumn = getVatAmountColumn();

        TableColumn<ProductsInTransact, Double> totalAmountColumn = getTotalAmountColumn();

        TableColumn<ProductsInTransact, Double> amountToPayColumn = getPayablesAmountColumn();

        productsTable.getColumns().addAll(invoiceColumn, descriptionColumn, unitColumn, unitPriceColumn,
                receivedQuantityColumn, vatAmountColumn, totalAmountColumn, amountToPayColumn);
    }

    private TableColumn<ProductsInTransact, Double> getVatAmountColumn() {
        TableColumn<ProductsInTransact, Double> vatAmountColumn = new TableColumn<>("VAT Amount");
        vatAmountColumn.setCellValueFactory(cellData -> {
            ProductsInTransact product = cellData.getValue();
            double totalAmount = product.getTotalAmount();
            BigDecimal vatAmount = calculateVat(BigDecimal.valueOf(totalAmount));
            return new ReadOnlyObjectWrapper<>(vatAmount.doubleValue());
        });
        vatAmountColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%,.2f", item));
                }
            }
        });
        return vatAmountColumn;
    }


    private TableColumn<ProductsInTransact, Double> getUnitPriceForPayment(PurchaseOrder selectedOrder) {
        TableColumn<ProductsInTransact, Double> unitPriceColumn = new TableColumn<>("Unit Price");
        unitPriceColumn.setCellValueFactory(cellData -> {
            ProductsInTransact product = cellData.getValue();
            double calculatedUnitPrice = 0;
            try {
                calculatedUnitPrice = calculateUnitPrice(product, selectedOrder);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            product.setUnitPrice(calculatedUnitPrice);
            return new ReadOnlyObjectWrapper<>(calculatedUnitPrice);
        });
        unitPriceColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%,.2f", item));
                }
            }
        });
        return unitPriceColumn;
    }


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
            double totalAmount = (unitPrice * receivedQuantity);
            product.setTotalAmount(totalAmount);
            return new ReadOnlyObjectWrapper<>(totalAmount);
        });
        totalAmountColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%,.2f", item));
                }
            }
        });
        return totalAmountColumn;
    }


    private static TableColumn<ProductsInTransact, Double> getPayablesAmountColumn() {
        TableColumn<ProductsInTransact, Double> amountToPayColumn = new TableColumn<>("Amount To Pay");
        amountToPayColumn.setCellValueFactory(cellData -> {
            ProductsInTransact product = cellData.getValue();
            BigDecimal totalAmount = BigDecimal.valueOf(product.getTotalAmount());
            BigDecimal vatAmount = calculateVat(totalAmount);
            BigDecimal amountToPay = totalAmount.add(vatAmount);
            product.setPaymentAmount(amountToPay.doubleValue());
            return new ReadOnlyObjectWrapper<>(amountToPay.doubleValue());
        });
        amountToPayColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%,.2f", item));
                }
            }
        });
        return amountToPayColumn;
    }


    private void setUpAdjustmentsTable() {
        TableColumn<CreditDebitMemo, String> typeColumn = new TableColumn<>("Type");
        typeColumn.setCellValueFactory(cellData -> {
            CreditDebitMemo memo = cellData.getValue();
            return new SimpleObjectProperty<>(memo.getType() == 1 ? "Credit" : (memo.getType() == 2 ? "Debit" : ""));
        });

        TableColumn<CreditDebitMemo, String> memoNumberColumn = new TableColumn<>("Memo Number");
        memoNumberColumn.setCellValueFactory(new PropertyValueFactory<>("memoNumber"));

        TableColumn<CreditDebitMemo, String> reasonColumn = new TableColumn<>("Reason");
        reasonColumn.setCellValueFactory(new PropertyValueFactory<>("reason"));

        TableColumn<CreditDebitMemo, Double> amountColumn = new TableColumn<>("Amount");
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));

        adjustmentsTable.getColumns().addAll(memoNumberColumn, typeColumn, reasonColumn, amountColumn);
    }

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
            controller.setPayablesController(this);

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

    public void receiveSelectedMemo(CreditDebitMemo memo) {
        adjustmentMemos.add(memo);
    }

}
