package com.vertex.vos;

import com.vertex.vos.Constructors.ProductsInTransact;
import com.vertex.vos.Constructors.PurchaseOrder;
import com.vertex.vos.Utilities.*;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;
import org.w3c.dom.Text;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;

public class ReceivingIOperationsController implements Initializable {

    @FXML
    public TableColumn<ProductsInTransact, Double> receivedUnitPrice;
    @FXML
    public TableColumn<ProductsInTransact, Double> discount;
    @FXML
    public TableColumn<ProductsInTransact, Double> netPrice;
    @FXML
    public TableColumn<ProductsInTransact, Double> netAmount;

    private AnchorPane contentPane;
    @FXML
    private TableView<ProductsInTransact> productTableView;

    public void setContentPane(AnchorPane contentPane) {
        this.contentPane = contentPane;
    }

    private final HistoryManager historyManager = new HistoryManager();

    private int currentNavigationId = -1; // Initialize to a default value

    @FXML
    private ComboBox<String> branchComboBox;

    @FXML
    private Label branchErr;

    @FXML
    private Label companyNameHeaderLabel;

    @FXML
    private Button confirmButton;

    @FXML
    private TableColumn<ProductsInTransact, Integer> orderedQuantity;

    @FXML
    private Label poErr;

    @FXML
    private ComboBox<String> poNumberTextField;

    @FXML
    private TableColumn<ProductsInTransact, String> productDescription;

    @FXML
    private TableColumn<ProductsInTransact, String> productUnit;

    @FXML
    private TableColumn<ProductsInTransact, Integer> receivedQuantity;

    PurchaseOrderDAO purchaseOrderDAO = new PurchaseOrderDAO();
    BranchDAO branchDAO = new BranchDAO();
    PurchaseOrderProductDAO purchaseOrderProductDAO = new PurchaseOrderProductDAO();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        TextFieldUtils.setComboBoxBehavior(poNumberTextField);
        TextFieldUtils.setComboBoxBehavior(branchComboBox);
        productTableView.setFocusTraversable(true);

        poNumberTextField.setItems(purchaseOrderDAO.getAllPOForReceiving());
        poNumberTextField.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            try {
                PurchaseOrder purchaseOrder = purchaseOrderDAO.getPurchaseOrderByOrderNo(Integer.parseInt(poNumberTextField.getSelectionModel().getSelectedItem()));
                populateBranchPerPoId(purchaseOrder);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void populateBranchPerPoId(PurchaseOrder purchaseOrder) throws SQLException {
        int poId = purchaseOrder.getPurchaseOrderNo();
        ObservableList<String> branchNames = purchaseOrderDAO.getBranchNamesForPurchaseOrder(poId);
        branchComboBox.setItems(branchNames);

        branchComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            int branchId = branchDAO.getBranchIdByName(newValue);
            try {
                loadProductsForPoPerBranch(purchaseOrder, branchId);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

        });

    }

    DiscountDAO discountDAO = new DiscountDAO();

    private void loadProductsForPoPerBranch(PurchaseOrder purchaseOrder, int branchId) throws SQLException {
        List<ProductsInTransact> products = purchaseOrderProductDAO.getProductsInTransactForBranch(purchaseOrder, branchId);
        ObservableList<ProductsInTransact> productsObservableList = FXCollections.observableArrayList(products);
        poNumberTextField.setDisable(true);
        productDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        orderedQuantity.setCellValueFactory(new PropertyValueFactory<>("orderedQuantity"));
        receivedQuantity.setCellValueFactory(new PropertyValueFactory<>("receivedQuantity"));
        receivedQuantity.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        receivedQuantity.setOnEditCommit(event -> {
            branchComboBox.setDisable(true);
            ProductsInTransact product = event.getRowValue();
            product.setReceivedQuantity(event.getNewValue());
            productTableView.refresh();
        });

        receivedUnitPrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        receivedUnitPrice.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        receivedUnitPrice.setOnEditCommit(event -> {
            ProductsInTransact product = event.getRowValue();
            product.setUnitPrice(event.getNewValue());
            productTableView.refresh();
        });

        netPrice.setCellValueFactory(cellData -> {
            ProductsInTransact product = cellData.getValue();
            int discountTypeId = product.getDiscountTypeId();

            try {
                List<BigDecimal> lineDiscounts = discountDAO.getLineDiscountsByDiscountTypeId(discountTypeId);

                double receivedUnitPrice = product.getUnitPrice();

                BigDecimal listPrice = BigDecimal.valueOf(receivedUnitPrice);
                BigDecimal discountAmount = DiscountCalculator.calculateDiscountedPrice(listPrice, lineDiscounts);

                // Round to 2 decimal places
                return new SimpleDoubleProperty(discountAmount.setScale(2, RoundingMode.HALF_UP).doubleValue()).asObject();
            } catch (SQLException e) {
                e.printStackTrace();
                return new SimpleDoubleProperty(0).asObject();
            }
        });

        productUnit.setCellValueFactory(new PropertyValueFactory<>("unit"));

        discount.setCellValueFactory(cellData -> {
            ProductsInTransact product = cellData.getValue();
            double receivedUnitPrice = product.getUnitPrice();
            double calculatedDiscount = netPrice.getCellObservableValue(product).getValue();

            // Calculate net price
            double netPriceValue = receivedUnitPrice - calculatedDiscount;

            // Round to 2 decimal places
            return new SimpleDoubleProperty(BigDecimal.valueOf(netPriceValue).setScale(2, RoundingMode.HALF_UP).doubleValue()).asObject();
        });

        netAmount.setCellValueFactory(cellData -> {
            ProductsInTransact product = cellData.getValue();
            double calculatedNetPrice = netPrice.getCellObservableValue(product).getValue();
            int receivedQuantity = product.getReceivedQuantity();
            double totalNetAmount = calculatedNetPrice * receivedQuantity;

            // Round to 2 decimal places
            return new SimpleDoubleProperty(BigDecimal.valueOf(totalNetAmount).setScale(2, RoundingMode.HALF_UP).doubleValue()).asObject();
        });


        productTableView.setItems(productsObservableList);
        productTableView.setEditable(true);

        productsObservableList.addListener((ListChangeListener.Change<? extends ProductsInTransact> change) -> {
            Platform.runLater(() -> totalsCalculations(productsObservableList));
        });
        confirmButton.disableProperty().bind(Bindings.createBooleanBinding(() ->
                productsObservableList.stream().anyMatch(product -> product.getReceivedQuantity() != 0), productsObservableList));

        confirmButton.setOnMouseClicked(event -> receivePurchaseOrderForBranch(products, purchaseOrder));
    }

    private void totalsCalculations(ObservableList<ProductsInTransact> productsObservableList) {
        System.out.println("Calculating totals");

        double totalNetAmount = productsObservableList.stream()
                .mapToDouble(product -> {
                    Double value = netAmount.getCellObservableValue(product).getValue();
                    System.out.println("Net Amount for " + product.getDescription() + ": " + value);
                    return value != null ? value : 0.0;
                })
                .sum();

        System.out.println("Total Net Amount: " + totalNetAmount);

        vatable.setText(String.format("%.2f", totalNetAmount)); // Format to two decimal places
    }

    private void receivePurchaseOrderForBranch(List<ProductsInTransact> products, PurchaseOrder purchaseOrder) {
        boolean allReceived = true; // Flag to track if all products were received successfully
        HashSet<Integer> processedProducts = new HashSet<>(); // Set to store processed product IDs
        for (ProductsInTransact product : products) {
            if (!processedProducts.contains(product.getPurchaseOrderProductId())) {
                try {
                    boolean received = purchaseOrderProductDAO.receivePurchaseOrderProduct(product, purchaseOrder);
                    if (!received) {
                        allReceived = false;
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    allReceived = false; // Set the flag to false in case of an exception
                }
                processedProducts.add(product.getPurchaseOrderProductId());
            }
        }
        if (allReceived) {
            String branchName = branchDAO.getBranchNameById(products.get(0).getBranchId());
            updateInventory(products, purchaseOrder);
            DialogUtils.showConfirmationDialog("Success", "Products for " + branchName + " have been received");
            resetInputs();
            try {
                populateBranchPerPoId(purchaseOrder);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            DialogUtils.showErrorMessage("Error", "Something went wrong, please contact your system developer.");
        }
    }


    InventoryDAO inventoryDAO = new InventoryDAO();

    private void updateInventory(List<ProductsInTransact> products, PurchaseOrder purchaseOrder) {
        for (ProductsInTransact product : products) {
            inventoryDAO.addOrUpdateInventory(product);
        }
    }

    private void resetInputs() {
        branchComboBox.getSelectionModel().clearSelection();
        branchComboBox.getItems().clear();
        productTableView.getItems().clear();
        branchComboBox.setDisable(false);
        poNumberTextField.setDisable(false);
    }
    @FXML
    private VBox totalBoxLabels;

    @FXML
    private Label vat;

    @FXML
    private Label vatExempt;

    @FXML
    private Label vatZeroRated;

    @FXML
    private Label vatable;

    @FXML
    private Label amountPayable;
}
