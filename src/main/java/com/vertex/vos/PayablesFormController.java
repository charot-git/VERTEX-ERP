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
import javafx.collections.ListChangeListener;
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
import java.math.RoundingMode;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

import static com.vertex.vos.Utilities.EWTCalculator.calculateWithholding;
import static com.vertex.vos.Utilities.VATCalculator.calculateVat;

public class PayablesFormController implements Initializable {

    public TextField paymentAmount;
    public TextField balance;
    public TextField paidAmountTextField;

    @FXML
    public Button holdButton;
    @FXML
    public TextField totalAmountTextField;
    public TextField inputTaxAmount;
    public TextField ewtAmount;
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
        TextFieldUtils.addDoubleInputRestriction(totalAmountTextField);
        TextFieldUtils.addDoubleInputRestriction(inputTaxAmount);
        TextFieldUtils.addDoubleInputRestriction(ewtAmount);

        paymentAmount.textProperty().addListener((observable, oldValue, newValue) -> {
            calculateTaxes(newValue);
        });

        balance.setEditable(false);
        paidAmountTextField.setEditable(false);
        totalAmountTextField.setEditable(false);
        inputTaxAmount.setEditable(false);
        ewtAmount.setEditable(false);
        adjustmentsTable.setItems(adjustmentMemos);
    }

    private void calculateTaxes(String newValue) {
        BigDecimal paymentAmount = new BigDecimal(newValue);
        BigDecimal withholdingAmount = calculateWithholding(paymentAmount);
        BigDecimal vatAmount = calculateVat(paymentAmount);

        inputTaxAmount.setText(vatAmount.toString());
        ewtAmount.setText(withholdingAmount.toString());
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
        leadTimePaymentDatePicker.setValue(selectedOrder.getLeadTimePayment());
        String chartOfAccountName = chartOfAccountsDAO.getChartOfAccountNameById(purchaseOrderPaymentDAO.getChartOfAccount(selectedOrder.getPurchaseOrderNo()));
        if (chartOfAccountName != null) {
            chartOfAccount.getSelectionModel().select(chartOfAccountName);
        }

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
                    checkIfPaid(selectedOrder);
                });
            }
            return products;
        }).thenAccept(products -> {
            if (products != null) {
                CompletableFuture.supplyAsync(() ->
                        purchaseOrderAdjustmentDAO.getAdjustmentsByPurchaseOrderId(selectedOrder.getPurchaseOrderId())
                ).thenAccept(adjustments -> Platform.runLater(() -> {
                    if (adjustments != null && !adjustments.isEmpty()) {
                        adjustmentMemos.addAll(adjustments);
                        calculatePayablesWithMemos(selectedOrder);
                    } else {
                        adjustmentsTable.setPlaceholder(new Label("No adjustments found."));
                    }
                })).exceptionally(ex -> {
                    Platform.runLater(() -> {
                        adjustmentsTable.setPlaceholder(new Label("Error loading adjustments."));
                    });
                    ex.printStackTrace();
                    return null;
                });
            }
        }).exceptionally(ex -> {
            Platform.runLater(() -> DialogUtils.showErrorMessage("Error", "An error occurred while fetching products: " + ex.getMessage()));
            return null;
        });

        holdButton.setOnMouseClicked(event -> {
            selectedOrder.setPaymentStatus(6);
            holdPayment(selectedOrder);
        });
    }


    private void calculatePayablesWithMemos(PurchaseOrder selectedOrder) {
        for (CreditDebitMemo memo : adjustmentMemos) {
            if (memo.getStatus().equals("Processing") || memo.getStatus().equals("Applied")) {
                if (memo.getType() == 1) { // Credit
                    selectedOrder.setTotalAmount(selectedOrder.getTotalAmount().add(BigDecimal.valueOf(memo.getAmount())));
                } else if (memo.getType() == 2) { // Debit
                    selectedOrder.setTotalAmount(selectedOrder.getTotalAmount().subtract(BigDecimal.valueOf(memo.getAmount())));
                }
                memo.setStatus("Processed");
            }
        }

        // Update balance amount
        selectedOrder.setBalanceAmount(selectedOrder.getTotalAmount().subtract(selectedOrder.getPaidAmount()));
        selectedOrder.setPaymentAmount(selectedOrder.getBalanceAmount());

        // Update UI fields
        Platform.runLater(() -> {
            totalAmountTextField.setText(selectedOrder.getTotalAmount().toString());
            balance.setText(selectedOrder.getBalanceAmount().toString());
            paymentAmount.setText(selectedOrder.getBalanceAmount().toString());
        });
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

    private void checkIfPaid(PurchaseOrder selectedOrder) {
        List<ProductsInTransact> products = productsTable.getItems();
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (ProductsInTransact product : products) {
            try {
                double unitPrice = calculateUnitPrice(product, selectedOrder);
                totalAmount = totalAmount.add(BigDecimal.valueOf(unitPrice * product.getReceivedQuantity()));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        selectedOrder.setTotalAmount(totalAmount.setScale(2, RoundingMode.HALF_UP));
        selectedOrder.setWithholdingTaxAmount(calculateWithholding(totalAmount).setScale(2, RoundingMode.HALF_UP));
        selectedOrder.setVatAmount(VATCalculator.calculateVat(totalAmount).setScale(2, RoundingMode.HALF_UP));

        BigDecimal paidAmountBigDecimal = purchaseOrderPaymentDAO.getTotalPaidAmountForPurchaseOrder(selectedOrder.getPurchaseOrderNo());
        if (paidAmountBigDecimal == null || paidAmountBigDecimal.compareTo(BigDecimal.ZERO) == 0) {
            selectedOrder.setBalanceAmount(selectedOrder.getTotalAmount());
            selectedOrder.setPaidAmount(BigDecimal.ZERO);
        } else {
            selectedOrder.setPaidAmount(paidAmountBigDecimal.setScale(2, RoundingMode.HALF_UP));
            selectedOrder.setBalanceAmount(selectedOrder.getTotalAmount().subtract(selectedOrder.getPaidAmount()).setScale(2, RoundingMode.HALF_UP));
        }
        selectedOrder.setPaymentAmount(selectedOrder.getBalanceAmount().setScale(2, RoundingMode.HALF_UP));
        Platform.runLater(() -> {
            totalAmountTextField.setText(String.format("%.2f", selectedOrder.getTotalAmount()));
            balance.setText(String.format("%.2f", selectedOrder.getBalanceAmount()));
            paidAmountTextField.setText(String.format("%.2f", selectedOrder.getPaidAmount()));
            paymentAmount.setText(String.format("%.2f", selectedOrder.getBalanceAmount()));
            confirmButton.setOnMouseClicked(event -> validateFields(selectedOrder));
        });

        adjustmentMemos.addListener((ListChangeListener<CreditDebitMemo>) change -> {
            while (change.next()) {
                calculatePayablesWithMemos(selectedOrder);
            }
        });

    }


    private void validateFields(PurchaseOrder order) {
        BigDecimal paymentAmount = getPaymentAmount();
        BigDecimal paidAmount = getPaidAmount();
        BigDecimal balance = getBalance();
        String selectedAccount = getSelectedChartOfAccount();
        LocalDate selectedDate = getSelectedLeadTimePaymentDate();
        String selectedSupplier = getSelectedSupplier();


        if (!isValidPaymentAmount(paymentAmount)) {
            showValidationError("Please enter a valid payment amount.");
            return;
        }


        if (!isValidChartOfAccount(selectedAccount)) {
            showValidationError("Please select a chart of account.");
            return;
        }


        if (!isValidLeadTimePaymentDate(selectedDate)) {
            showValidationError("Please select a lead time payment date.");
            return;
        }


        if (!isValidSupplier(selectedSupplier)) {
            showValidationError("Please select a supplier.");
            return;
        }

        order.setPaymentAmount(paymentAmount);
        order.setPaidAmount(paidAmount);
        order.setBalanceAmount(balance);

        processPayment(order);
    }

    private BigDecimal getPaymentAmount() {
        return BigDecimal.valueOf(Double.parseDouble(paymentAmount.getText()));
    }

    private BigDecimal getPaidAmount() {
        return BigDecimal.valueOf(Double.parseDouble(paidAmountTextField.getText()));
    }

    private BigDecimal getBalance() {
        return BigDecimal.valueOf(Double.parseDouble(balance.getText()));
    }

    private boolean isValidPaymentAmount(BigDecimal paymentAmount) {
        return paymentAmount.compareTo(BigDecimal.ZERO) != 0;
    }

    private boolean isValidChartOfAccount(String selectedAccount) {
        return selectedAccount != null && !selectedAccount.isEmpty();
    }

    private boolean isValidLeadTimePaymentDate(LocalDate selectedDate) {
        return selectedDate != null;
    }

    private boolean isValidSupplier(String selectedSupplier) {
        return selectedSupplier != null;
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
        selectedOrder.setBalanceAmount(selectedOrder.getBalanceAmount().subtract(selectedOrder.getPaymentAmount()));
        entryPayable(selectedOrder);
    }


    PurchaseOrderAdjustmentDAO purchaseOrderAdjustmentDAO = new PurchaseOrderAdjustmentDAO();
    PurchaseOrderDAO purchaseOrderDAO = new PurchaseOrderDAO();
    SupplierMemoDAO supplierMemoDAO = new SupplierMemoDAO();
    PurchaseOrderPaymentDAO purchaseOrderPaymentDAO = new PurchaseOrderPaymentDAO();

    private void entryPayable(PurchaseOrder selectedOrder) {
        ConfirmationAlert confirmationAlert = new ConfirmationAlert("Confirm Payment", "Are you sure you want to make the payment?", "", false);
        boolean confirmed = confirmationAlert.showAndWait();

        if (!confirmed) {
            return;
        }

        if (!adjustmentMemos.isEmpty()) {
            for (CreditDebitMemo memo : adjustmentMemos) {
                boolean adjusted = purchaseOrderAdjustmentDAO.insertAdjustment(selectedOrder.getPurchaseOrderId(), memo);
                if (adjusted) {
                    supplierMemoDAO.updateMemoStatus(Integer.parseInt(memo.getMemoNumber()), "Applied");
                }
            }
            for (CreditDebitMemo memo : adjustmentMemos) {
                if (memo.getType() == 1) { // Credit
                    selectedOrder.setTotalAmount(selectedOrder.getTotalAmount().add(BigDecimal.valueOf(memo.getAmount())));
                } else if (memo.getType() == 2) { // Debit
                    selectedOrder.setTotalAmount(selectedOrder.getTotalAmount().subtract(BigDecimal.valueOf(memo.getAmount())));
                }

            }
        }

        if (selectedOrder.getPaymentStatus() == 6) {
            DialogUtils.showErrorMessage("Error", "Payment is held.");
            return;
        }

        int chartOfAccountId = chartOfAccountsDAO.getChartOfAccountIdByName(chartOfAccount.getSelectionModel().getSelectedItem());
        if (selectedOrder.getBalanceAmount().equals(BigDecimal.ZERO) || selectedOrder.getBalanceAmount().compareTo(BigDecimal.ZERO) == 0.00) {
            selectedOrder.setPaymentStatus(4); // Fully Paid
        } else {
            selectedOrder.setPaymentStatus(3); // Partially Paid
            ConfirmationAlert partialPaymentAlert = new ConfirmationAlert("Partial Payment", "The payment is partial. Do you want to proceed?", "", false);
            boolean partialPaymentConfirmed = partialPaymentAlert.showAndWait();
            if (!partialPaymentConfirmed) {
                return;
            }
        }
        boolean paymentInserted = purchaseOrderPaymentDAO.insertPayment(selectedOrder.getPurchaseOrderNo(), selectedOrder.getSupplierName(), selectedOrder.getPaymentAmount(), chartOfAccountId);
        if (paymentInserted) {
            purchaseOrderDAO.updatePurchaseOrderPaymentStatus(selectedOrder.getPurchaseOrderId(), selectedOrder.getPaymentStatus());
            purchaseOrderDAO.updatePurchaseOrderLeadTimePayment(selectedOrder.getPurchaseOrderId(), getSelectedLeadTimePaymentDate());
            purchaseOrdersPerSupplierForPaymentController.loadItemsForPayment(getSelectedSupplier());
            DialogUtils.showConfirmationDialog("Confirmation", "Payment confirmed successfully!");

            if (selectedOrder.getPaymentStatus() == 4) {
                statusLabel.setText("Fully Paid");
            } else if (selectedOrder.getPaymentStatus() == 3) {
                statusLabel.setText("Partially Paid");
            }
            confirmButton.setDisable(true);
        } else {
            DialogUtils.showErrorMessage("Error", "Failed to insert payment record.");
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


        productsTable.getColumns().addAll(invoiceColumn, descriptionColumn, unitColumn, unitPriceColumn,
                receivedQuantityColumn, vatAmountColumn, totalAmountColumn);
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

    ProductDAO productDAO = new ProductDAO();

    private double calculateUnitPrice(ProductsInTransact product, PurchaseOrder selectedOrder) throws SQLException {
        // Fetch the parentId of the product, or use the product's own ID if it has no parent
        int productId = product.getProductId();
        int parentId = productDAO.getParentIdByProductId(productId);
        int discountTypeId;

        if (parentId != -1) {
            discountTypeId = discountDAO.getProductDiscountForProductTypeId(parentId, selectedOrder.getSupplierName());
        } else {
            discountTypeId = discountDAO.getProductDiscountForProductTypeId(productId, selectedOrder.getSupplierName());
        }

        // If no discount is applicable, return the original unit price
        if (discountTypeId == -1) {
            return product.getUnitPrice();
        }

        // Calculate the discounted price if a discount type is found
        BigDecimal listPrice = BigDecimal.valueOf(product.getUnitPrice());
        List<BigDecimal> lineDiscounts = discountDAO.getLineDiscountsByDiscountTypeId(discountTypeId);
        return DiscountCalculator.calculateDiscountedPrice(listPrice, lineDiscounts).doubleValue();
    }


    private static TableColumn<ProductsInTransact, Double> getTotalAmountColumn() {
        TableColumn<ProductsInTransact, Double> totalAmountColumn = new TableColumn<>("Total Amount");
        totalAmountColumn.setCellValueFactory(cellData -> {
            ProductsInTransact product = cellData.getValue();
            double unitPrice = product.getUnitPrice();
            int receivedQuantity = product.getReceivedQuantity();
            double totalAmount = (unitPrice * receivedQuantity);
            product.setTotalAmount(totalAmount);
            return new ReadOnlyObjectWrapper<>(product.getTotalAmount()); // Return the updated total amount directly
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
        memo.setStatus("Processing");
        adjustmentMemos.add(memo);
    }

}
