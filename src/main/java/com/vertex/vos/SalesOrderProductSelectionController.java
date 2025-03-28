package com.vertex.vos;

import com.vertex.vos.DAO.ProductPerCustomerDAO;
import com.vertex.vos.Objects.*;
import com.vertex.vos.Utilities.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import lombok.Setter;
import org.controlsfx.control.textfield.TextFields;

import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

public class SalesOrderProductSelectionController implements Initializable {

    public TextField allocatedQuantityField;
    public Label totalAmountLabelAllocated;
    public Label netAmountLabelAllocated;
    public Label grossAmountLabelAllocated;
    public Label grossAmountLabelTotal;
    public Label netAmountLabelTotal;
    public Label totalAmountLabelTotal;
    @FXML
    private Button addProduct;
    @FXML
    private Label availableQuantityLabel, discountTypeLabel, priceLabel;
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
    @FXML
    private ButtonBar buttonBar;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(this::initializeAutoCompletion);
        TextFieldUtils.addNumericInputRestriction(orderedQuantityField);
        TextFieldUtils.addNumericInputRestriction(allocatedQuantityField);
        Platform.runLater(() -> productNameField.requestFocus());
    }

    private void addProductToSalesOrder() {
        if (salesOrderDetail != null && !orderedQuantityField.getText().isEmpty() && !allocatedQuantityField.getText().isEmpty()) {
            if (salesOrderFormController.salesOrderDetails.stream().noneMatch(detail -> detail.getProduct().getProductId() == selectedProduct.getProductId())) {
                salesOrderDetail.setProduct(selectedProduct);
                salesOrderDetail.setSalesOrder(salesOrderFormController.salesOrder);
                salesOrderDetail.setUnitPrice(salesOrderDetail.getUnitPrice());
                salesOrderDetail.setOrderedQuantity(Integer.parseInt(orderedQuantityField.getText()));
                salesOrderDetail.setAllocatedQuantity(Integer.parseInt(allocatedQuantityField.getText()));
                salesOrderDetail.setServedQuantity(0);
                salesOrderDetail.setDiscountType(selectedProduct.getDiscountType());
                salesOrderDetail.setDiscountAmount(calculateTotalDiscountAmountTotal());
                salesOrderDetail.setGrossAmount(calculateGrossTotal());
                salesOrderDetail.setNetAmount(calculateNetAmountTotal());
                salesOrderDetail.setRemarks("");
                salesOrderDetail.setCreatedDate(Timestamp.valueOf(LocalDateTime.now()));
                salesOrderDetail.setModifiedDate(Timestamp.valueOf(LocalDateTime.now()));
                salesOrderDetail.setAllocatedAmount(calculateTotalAmountAllocation());

                salesOrderFormController.salesOrderDetails.add(salesOrderDetail);
                resetForm();
            } else {
                DialogUtils.showErrorMessage("Error", "This product is already added to the order.");
            }
        }
    }

    private void updateProductToSalesOrder() {
        if (salesOrderDetail != null && !orderedQuantityField.getText().isEmpty()) {
            int existingIndex = salesOrderFormController.salesOrderDetails.indexOf(salesOrderDetail);
            if (existingIndex != -1) {
                salesOrderDetail.setOrderedQuantity(Integer.parseInt(orderedQuantityField.getText()));
                salesOrderDetail.setDiscountAmount(calculateTotalDiscountAmountTotal());
                salesOrderDetail.setGrossAmount(calculateGrossTotal());
                salesOrderDetail.setNetAmount(calculateNetAmountTotal());
                salesOrderDetail.setModifiedDate(Timestamp.valueOf(LocalDateTime.now()));
                salesOrderDetail.setAllocatedQuantity(Integer.parseInt(allocatedQuantityField.getText()));
                salesOrderDetail.setAllocatedAmount(calculateTotalAmountAllocation());

                salesOrderFormController.salesOrderDetails.set(existingIndex, salesOrderDetail);
                resetForm();
            } else {
                DialogUtils.showErrorMessage("Error", "Error, please contact system administrator.");
            }
        }
    }

    private void resetForm() {
        orderedQuantityField.clear();
        productNameField.clear();
        uomField.getItems().clear();
        uomField.setValue("");
        availableQuantityLabel.setText("");
        allocatedQuantityField.setText("");
        grossAmountLabelTotal.setText("");
        netAmountLabelTotal.setText("");
        totalAmountLabelTotal.setText("");
        grossAmountLabelAllocated.setText("");
        netAmountLabelAllocated.setText("");
        totalAmountLabelAllocated.setText("");
        priceLabel.setText("");
        discountTypeLabel.setText("");
        productImage.setImage(placeholderImage);
        salesOrderDetail = null;
        selectedProduct = null;
        lineDiscountList.clear();
        productNameField.requestFocus();
        salesOrderDetail = new SalesOrderDetails();
    }

    private void initializeAutoCompletion() {
        Task<List<String>> productNamesTask = new Task<>() {
            @Override
            protected List<String> call() {
                return productDAO.getProductNamesPerSupplier(
                        salesOrderFormController.salesOrder.getSupplier().getId()
                );
            }
        };

        productNamesTask.setOnSucceeded(e -> {
            List<String> productNames = productNamesTask.getValue();
            TextFields.bindAutoCompletion(productNameField, productNames);

            productNameField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.isEmpty()) {
                    uomField.setItems(productDAO.getProductUnits(newValue
                    ));
                    uomField.getSelectionModel().selectFirst();
                }
            });

            uomField.valueProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    initializeProductData();
                    calculateTotals();
                }
            });

            orderedQuantityField.textProperty().addListener(((observable, oldValue, newValue) -> {
                if (!newValue.isEmpty()) {
                    if (selectedProduct != null) {
                        calculateTotals();
                    }
                }
            }));
            allocatedQuantityField.textProperty().addListener(((observable, oldValue, newValue) -> {
                if (!newValue.isEmpty()) {
                    if (selectedProduct != null) {
                        if (Integer.parseInt(allocatedQuantityField.getText()) > Integer.parseInt(orderedQuantityField.getText())) {
                            allocatedQuantityField.setStyle("-fx-text-fill: red;");
                            Platform.runLater(() -> {
                                try {
                                    Thread.sleep(1000); // Wait for 1 second
                                } catch (InterruptedException ex) {
                                    ex.printStackTrace();
                                }
                                allocatedQuantityField.setText("");
                                allocatedQuantityField.setStyle(""); // Reset style
                            });
                        } else {
                            calculateTotals();
                        }
                    }
                }
            }));
        });

        new Thread(productNamesTask).start();
    }

    DiscountDAO discountDAO = new DiscountDAO();
    ObservableList<BigDecimal> lineDiscountList = FXCollections.observableArrayList();

    private void calculateTotals() {
        grossAmountLabelTotal.setText(String.valueOf(calculateGrossTotal()));
        netAmountLabelTotal.setText(String.valueOf(calculateNetAmountTotal()));
        totalAmountLabelTotal.setText(String.valueOf(calculateTotalAmountTotal()));
        grossAmountLabelAllocated.setText(String.valueOf(calculateGrossAllocation()));
        netAmountLabelAllocated.setText(String.valueOf(calculateNetAmountAllocation()));
        totalAmountLabelAllocated.setText(String.valueOf(calculateTotalAmountAllocation()));
    }

    private double calculateTotalAmountAllocation() {
        if (salesOrderFormController.salesOrder.getInvoiceType().getId() != 3) {
            return calculateNetAmountAllocation() + calculateVatOnAllocation();
        } else {
            return calculateNetAmountAllocation();
        }
    }

    private double calculateGrossAllocation() {
        if (salesOrderDetail != null && allocatedQuantityField.getText() != null && !allocatedQuantityField.getText().isEmpty()) {
            try {
                int allocatedQuantity = Integer.parseInt(allocatedQuantityField.getText());
                return salesOrderDetail.getUnitPrice() * allocatedQuantity;
            } catch (NumberFormatException e) {
                DialogUtils.showErrorMessage("Error", "Invalid allocated quantity entered");
                allocatedQuantityField.setText("0");
            }
        }
        return 0.0;
    }

    private double calculateDiscountOnAllocation() {
        BigDecimal grossAllocation = BigDecimal.valueOf(calculateGrossAllocation());
        return DiscountCalculator.calculateTotalDiscountAmount(grossAllocation, lineDiscountList).doubleValue();
    }

    private double calculateNetAmountAllocation() {
        return calculateGrossAllocation() - calculateDiscountOnAllocation();
    }

    private double calculateVatOnAllocation() {
        return VATCalculator.calculateVat(BigDecimal.valueOf(calculateNetAmountAllocation())).doubleValue();
    }

    private void calculateAllocationTotals() {
        grossAmountLabelAllocated.setText(String.valueOf(calculateGrossAllocation()));
        netAmountLabelAllocated.setText(String.valueOf(calculateNetAmountAllocation()));
        totalAmountLabelAllocated.setText(String.valueOf(calculateTotalAmountAllocation()));
    }

    private double calculateTotalAmountTotal() {
        if (salesOrderFormController.salesOrder.getInvoiceType().getId() != 3) {
            return calculateNetAmountTotal() + calculateVat();
        } else {
            return calculateNetAmountTotal();
        }
    }

    private double calculateVat() {
        return VATCalculator.calculateVat(BigDecimal.valueOf(calculateNetAmountTotal())).doubleValue();
    }

    private double calculateTotalDiscountAmountTotal() {
        return DiscountCalculator.calculateTotalDiscountAmount(BigDecimal.valueOf(calculateGrossTotal()), lineDiscountList).doubleValue();
    }

    private double calculateNetAmountTotal() {
        return calculateGrossTotal() - calculateTotalDiscountAmountTotal();
    }

    private double calculateGrossTotal() {
        try {
            if (salesOrderDetail != null && orderedQuantityField.getText() != null && !orderedQuantityField.getText().isEmpty()) {
                return salesOrderDetail.getUnitPrice() * Integer.parseInt(orderedQuantityField.getText());
            }
        } catch (NumberFormatException e) {
            DialogUtils.showErrorMessage("Error", "Invalid quantity entered");
            orderedQuantityField.setText("0");
        }
        return 0.0;
    }

    private void initializeProductData() {
        CompletableFuture.runAsync(() -> {
            selectedProduct = productDAO.getProductByNameAndUnit(
                    productNameField.getText(),
                    uomField.getSelectionModel().getSelectedItem()
            );
            Platform.runLater(() -> populateProductData(selectedProduct,
                    salesOrderFormController.salesOrder.getBranch(),
                    salesOrderFormController.salesOrder.getCustomer()));
        });
    }

    private void populateProductData(Product selectedProduct, Branch branch, Customer customer) {
        if (selectedProduct != null) {
            int availableQuantity = inventoryDAO.getQuantityByBranchAndProductID(branch.getId(), selectedProduct.getProductId());
            availableQuantityLabel.setText(String.valueOf(availableQuantity));

            if (availableQuantity <= 0) {
                availableQuantityLabel.setStyle("-fx-text-fill: red;");
                availableQuantityLabel.setText("Out of Stock");
            }

            Product customerProduct = productPerCustomerDAO.getCustomerProductByCustomerAndProduct(selectedProduct, customer);
            loadImage(selectedProduct);

            if (customerProduct != null) {
                setCustomerProductPricing(customerProduct);
            } else {
                setSalesmanPricing();
            }
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

        priceLabel.setText(String.format("%.2f", salesOrderDetail.getUnitPrice()));
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
        addProduct.setOnAction(actionEvent -> addProductToSalesOrder());
    }

    public void initializeItemForUpdate(SalesOrderDetails selectedItem) {
        salesOrderDetail = selectedItem;
        selectedProduct = selectedItem.getProduct();
        populateProductData(selectedProduct, salesOrderDetail.getSalesOrder().getBranch(), salesOrderDetail.getSalesOrder().getCustomer());
        productNameField.setText(selectedProduct.getProductName());
        uomField.getSelectionModel().select(selectedProduct.getUnitOfMeasurementString());
        orderedQuantityField.setText(String.valueOf(selectedItem.getOrderedQuantity()));
        allocatedQuantityField.setText(String.valueOf(selectedItem.getAllocatedQuantity()));
        calculateTotals();
        addProduct.setText("Update");
        addProduct.setOnAction(actionEvent -> updateProductToSalesOrder());

        Button deleteButton = new Button("Delete");
        deleteButton.setStyle("-fx-border-color: red; -fx-text-fill: red;");
        buttonBar.getButtons().add(deleteButton);

        deleteButton.setOnAction(actionEvent -> deleteProductFromSalesOrder(selectedItem));
    }

    private void deleteProductFromSalesOrder(SalesOrderDetails selectedItem) {
        salesOrderFormController.salesOrderDetails.remove(selectedItem);
        resetForm();
        initializeNewDetail();
    }
}
