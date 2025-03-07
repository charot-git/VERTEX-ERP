package com.vertex.vos;

import com.vertex.vos.DAO.ProductPerCustomerDAO;
import com.vertex.vos.Objects.*;
import com.vertex.vos.Utilities.*;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import lombok.Setter;
import org.controlsfx.control.textfield.TextFields;

import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class SalesOrderProductSelectionController implements Initializable {

    @FXML
    private Button addProduct;
    @FXML
    private Label availableQuantityLabel, discountTypeLabel, grossAmountLabel, netAmountLabel, priceLabel, totalAmountLabel;
    @FXML
    private TextField orderedQuantityField, productNameField;
    @FXML
    private ComboBox<String> uomField;
    @FXML
    private ImageView productImage;

    @Setter
    private SalesOrderFormController salesOrderFormController;

    private final ProductDAO productDAO = new ProductDAO();
    private final InventoryDAO inventoryDAO = new InventoryDAO();
    private final ProductPerCustomerDAO productPerCustomerDAO = new ProductPerCustomerDAO();

    private Product selectedProduct;
    private SalesOrderDetails salesOrderDetail;
    private final Image placeholderImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/vertex/vos/assets/icons/package.png")));

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(this::initializeAutoCompletion);
        TextFieldUtils.addNumericInputRestriction(orderedQuantityField);
    }

    private void initializeAutoCompletion() {
        Task<List<String>> productNamesTask = new Task<>() {
            @Override
            protected List<String> call() {
                return productDAO.getProductNamesWithInventoryPerSupplier(
                        salesOrderFormController.salesOrder.getBranch().getId(),
                        salesOrderFormController.salesOrder.getSupplier().getId()
                );
            }
        };

        productNamesTask.setOnSucceeded(e -> {
            List<String> productNames = productNamesTask.getValue();
            TextFields.bindAutoCompletion(productNameField, productNames);

            productNameField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.isEmpty()) {
                    uomField.setItems(productDAO.getProductUnitsWithInventory(
                            salesOrderFormController.salesOrder.getBranch().getId(), newValue
                    ));
                    uomField.getSelectionModel().selectFirst();
                }
            });

            uomField.valueProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.isEmpty()) {
                    initializeProductData();
                }
            });

            orderedQuantityField.textProperty().addListener(((observable, oldValue, newValue) -> {
                if (!newValue.isEmpty()) {
                    if (selectedProduct != null) {
                        quantityValidation();
                    }
                }
            }));
        });

        new Thread(productNamesTask).start();
    }

    private void quantityValidation() {
        try {
            int orderedQuantity = Integer.parseInt(orderedQuantityField.getText());
            int availableQuantity = Integer.parseInt(availableQuantityLabel.getText());
            if (orderedQuantity > availableQuantity) {
                orderedQuantityField.setStyle("-fx-border-color: red");
                PauseTransition pauseTransition = new PauseTransition(Duration.millis(1500));
                pauseTransition.setOnFinished(actionEvent -> orderedQuantityField.setStyle(null));
                pauseTransition.play();
            } else {
                calculateTotals();
            }
        } catch (NumberFormatException e) {
            DialogUtils.showErrorMessage("Error", e.getMessage());
        }
    }

    DiscountDAO discountDAO = new DiscountDAO();
    ObservableList<BigDecimal> lineDiscountList = FXCollections.observableArrayList();

    private void calculateTotals() {
        grossAmountLabel.setText(String.valueOf(calculateGross()));
        netAmountLabel.setText(String.valueOf(calculateNetAmount()));
        totalAmountLabel.setText(String.valueOf(calculateTotalAmount()));
    }


    private double calculateTotalAmount() {
        if (salesOrderFormController.salesOrder.getInvoiceType().getId() != 3) {
            return calculateNetAmount() + calculateVat();
        } else {
            return calculateNetAmount();
        }
    }

    private double calculateVat() {
        return VATCalculator.calculateVat(BigDecimal.valueOf(calculateNetAmount())).doubleValue();
    }

    private double calculateTotalDiscountAmount() {
        return DiscountCalculator.calculateTotalDiscountAmount(BigDecimal.valueOf(calculateGross()), lineDiscountList).doubleValue();
    }

    private double calculateNetAmount() {
        return calculateGross() - calculateTotalDiscountAmount();
    }

    private double calculateGross() {
        return salesOrderDetail.getUnitPrice() * Integer.parseInt(orderedQuantityField.getText());
    }

    private void initializeProductData() {
        selectedProduct = productDAO.getProductByNameAndUnit(
                productNameField.getText(),
                uomField.getSelectionModel().getSelectedItem()
        );
        populateProductData(selectedProduct, salesOrderFormController.salesOrder.getBranch(),
                salesOrderFormController.salesOrder.getCustomer());
    }

    private void populateProductData(Product selectedProduct, Branch branch, Customer customer) {
        int availableQuantity = inventoryDAO.getQuantityByBranchAndProductID(branch.getId(), selectedProduct.getProductId());
        availableQuantityLabel.setText(String.valueOf(availableQuantity));

        Product customerProduct = productPerCustomerDAO.getCustomerProductByCustomerAndProduct(selectedProduct, customer);
        loadImage(selectedProduct);

        if (customerProduct != null) {
            setCustomerProductPricing(customerProduct);
        } else {
            setSalesmanPricing();
        }
    }

    private void setCustomerProductPricing(Product customerProduct) {
        lineDiscountList.clear();
        priceLabel.setText(String.valueOf(customerProduct.getPricePerUnit()));
        discountTypeLabel.setText(customerProduct.getDiscountType() != null ?
                customerProduct.getDiscountType().getTypeName() : "No Discount");
        selectedProduct.setDiscountType(customerProduct.getDiscountType());
        salesOrderDetail.setUnitPrice(customerProduct.getPricePerUnit());
        lineDiscountList.setAll(FXCollections.observableArrayList(discountDAO.getLineDiscountsByDiscountTypeId(customerProduct.getDiscountType().getId())));
    }

    private void setSalesmanPricing() {
        if (salesOrderDetail == null) salesOrderDetail = new SalesOrderDetails();

        switch (salesOrderFormController.salesOrder.getSalesman().getPriceType()) {
            case "A" -> salesOrderDetail.setUnitPrice(selectedProduct.getPriceA());
            case "B" -> salesOrderDetail.setUnitPrice(selectedProduct.getPriceB());
            case "C" -> salesOrderDetail.setUnitPrice(selectedProduct.getPriceC());
            case "D" -> salesOrderDetail.setUnitPrice(selectedProduct.getPriceD());
            case "E" -> salesOrderDetail.setUnitPrice(selectedProduct.getPriceE());
        }

        discountTypeLabel.setText("No Discount");
        salesOrderDetail.setDiscountType(null);
    }

    private void loadImage(Product product) {
        productImage.setImage(placeholderImage);
        String imageUrl = product.getProductImage();

        if (imageUrl != null) {
            Task<Image> imageLoadTask = new Task<>() {
                @Override
                protected Image call() {
                    try {
                        return new Image(new File(imageUrl).toURI().toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                        return placeholderImage;
                    }
                }
            };

            imageLoadTask.setOnSucceeded(e -> productImage.setImage(imageLoadTask.getValue()));
            imageLoadTask.setOnFailed(e -> productImage.setImage(placeholderImage));

            new Thread(imageLoadTask).start();
        }
    }

    public void initializeNewDetail() {
        salesOrderDetail = new SalesOrderDetails();
    }
}
