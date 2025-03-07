package com.vertex.vos;

import com.vertex.vos.DAO.ProductPerCustomerDAO;
import com.vertex.vos.Objects.Customer;
import com.vertex.vos.Objects.Product;
import com.vertex.vos.Objects.SalesInvoiceDetail;
import com.vertex.vos.Utilities.*;
import javafx.animation.PauseTransition;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.Setter;
import org.controlsfx.control.textfield.TextFields;

import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class SalesInvoiceProductSelectionKeyboardController implements Initializable {

    public Label discountAmount;
    public Label discountType;
    public Label totalAmount;
    public Label unitPrice;
    public Label availableQuantity;
    public Button addButton;
    public ComboBox<String> uomComboBox;

    @FXML
    private TextField orderQuantityTextField;

    @FXML
    private ImageView productImage;

    @FXML
    private TextField productNameTextField;

    @Setter
    private Stage productSelectionStage;

    @Setter
    private SalesInvoiceTemporaryController salesInvoiceTemporaryController;

    @Setter
    private String priceType;

    private int branchCode;

    @Setter
    private Customer selectedCustomer;

    @Setter
    private String orderId;

    private ProductDAO productDAO = new ProductDAO();
    private InventoryDAO inventoryDAO = new InventoryDAO();
    private DiscountDAO discountDAO = new DiscountDAO();
    private ProductPerCustomerDAO productPerCustomerDAO = new ProductPerCustomerDAO();
    private SalesInvoiceDetail salesInvoiceDetail;

    List<String> productNames = new ArrayList<>();

    public void setBranchCode(int branchCode) {
        this.branchCode = branchCode;
        productNames = productDAO.getProductNamesWithInventory(branchCode);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        TextFieldUtils.addNumericInputRestriction(orderQuantityTextField);
    }

    public void processProductSelection() {
        // Bind auto-completion for product names
        TextFields.bindAutoCompletion(productNameTextField, productNames);

        salesInvoiceDetail = new SalesInvoiceDetail();
        // Product Name Change Listener
        productNameTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                uomComboBox.setItems(productDAO.getProductUnitsWithInventory(branchCode, newValue));
            }
        });

        // Unit of Measurement Change Listener
        uomComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                Product selectedProduct = productDAO.getProductByNameAndUnit(productNameTextField.getText(), newValue);

                if (selectedProduct != null) {
                    salesInvoiceDetail.setProduct(selectedProduct);

                    if (salesInvoiceTemporaryController.getSalesInvoiceDetailList().stream()
                            .anyMatch(detail -> detail.getProduct().getProductId() == selectedProduct.getProductId())) {
                        productSelectionStage.hide();
                        salesInvoiceTemporaryController.getItemsTable().getSelectionModel().select(
                                salesInvoiceTemporaryController.getSalesInvoiceDetailList().stream()
                                        .filter(detail -> detail.getProduct().getProductId() == selectedProduct.getProductId())
                                        .findFirst().orElse(null)
                        );
                        return;
                    }

                    populateProductData(selectedProduct, branchCode, selectedCustomer);
                    orderQuantityTextField.textProperty().addListener((observableValue, oldVal, newVal) -> {
                        if (newVal != null && !newVal.isEmpty()) {
                            int newQuantity = Integer.parseInt(newVal);
                            if (newQuantity > salesInvoiceDetail.getAvailableQuantity()) {
                                orderQuantityTextField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                                addButton.setDisable(true);
                                PauseTransition pause = new PauseTransition(Duration.seconds(2));
                                pause.setOnFinished(e -> {
                                    addButton.setDisable(false);
                                    orderQuantityTextField.setStyle("");
                                    orderQuantityTextField.setText(oldVal);
                                });
                                pause.play();
                            } else {
                                salesInvoiceDetail.setQuantity(newQuantity);
                                updateAmount();

                            }
                        }
                    });
                }
            }
        });
    }

    private void clearFields() {
        productNameTextField.clear();
        uomComboBox.getItems().clear();
        orderQuantityTextField.clear();
        availableQuantity.setText("");
        unitPrice.setText("");
        discountType.setText("");
        discountAmount.setText("");
        totalAmount.setText("");
        productNameTextField.requestFocus();
    }

    private void updateAmount() {
        if (salesInvoiceDetail == null) {
            return;
        }
        try {
            salesInvoiceDetail.setGrossAmount(salesInvoiceDetail.getUnitPrice() * salesInvoiceDetail.getQuantity());
            if (salesInvoiceDetail.getProduct().getDiscountType() != null) {
                List<BigDecimal> lineDiscounts = discountDAO.getLineDiscountsByDiscountTypeId(
                        salesInvoiceDetail.getProduct().getDiscountType().getId());
                if (lineDiscounts != null && !lineDiscounts.isEmpty()) {
                    double discount = DiscountCalculator.calculateTotalDiscountAmount(
                            BigDecimal.valueOf(salesInvoiceDetail.getGrossAmount()), lineDiscounts).doubleValue();
                    salesInvoiceDetail.setDiscountType(salesInvoiceDetail.getProduct().getDiscountType());
                    salesInvoiceDetail.setDiscountAmount(discount);
                } else {
                    salesInvoiceDetail.setDiscountAmount(0); // No discounts available
                }
            }
            salesInvoiceDetail.setTotalPrice(salesInvoiceDetail.getGrossAmount() - salesInvoiceDetail.getDiscountAmount());

            discountAmount.setText(String.valueOf(salesInvoiceDetail.getDiscountAmount()));
            totalAmount.setText(String.valueOf(salesInvoiceDetail.getTotalPrice()));

        } catch (NullPointerException e) {
            DialogUtils.showErrorMessage("Error", "An error occurred while updating amounts: " + e.getMessage());
        }
    }

    Product productToAdd;

    private void populateProductData(Product selectedProduct, int branchCode, Customer selectedCustomer) {
        int availableQuantityForProductInBranch = inventoryDAO.getQuantityByBranchAndProductID(branchCode, selectedProduct.getProductId());
        salesInvoiceDetail.setAvailableQuantity(availableQuantityForProductInBranch);
        availableQuantity.setText(String.valueOf(availableQuantityForProductInBranch));

        Product customerProduct = productPerCustomerDAO.getCustomerProductByCustomerAndProduct(selectedProduct, selectedCustomer);
        if (customerProduct != null) {
            unitPrice.setText(String.valueOf(customerProduct.getPricePerUnit()));
            discountType.setText(customerProduct.getDiscountType() != null ? customerProduct.getDiscountType().getTypeName() : "No Discount");
            salesInvoiceDetail.setUnitPrice(customerProduct.getPricePerUnit());
            selectedProduct.setDiscountType(customerProduct.getDiscountType());
        } else {
            switch (priceType) {
                case "A" -> salesInvoiceDetail.setUnitPrice(selectedProduct.getPriceA());
                case "B" -> salesInvoiceDetail.setUnitPrice(selectedProduct.getPriceB());
                case "C" -> salesInvoiceDetail.setUnitPrice(selectedProduct.getPriceC());
                case "D" -> salesInvoiceDetail.setUnitPrice(selectedProduct.getPriceD());
                case "E" -> salesInvoiceDetail.setUnitPrice(selectedProduct.getPriceE());
            }
            discountType.setText("No Discount");
            salesInvoiceDetail.setDiscountType(null);
        }

        productToAdd = selectedProduct;
    }

    public void setButtonAction() {
        addButton.setOnAction(event -> {
            salesInvoiceDetail.setProduct(productToAdd);
            salesInvoiceTemporaryController.addProductToSalesInvoice(salesInvoiceDetail);
            processProductSelection();
            clearFields();
        });

    }
}
