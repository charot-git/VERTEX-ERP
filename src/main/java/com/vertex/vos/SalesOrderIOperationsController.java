package com.vertex.vos;

import com.vertex.vos.Constructors.ComboBoxFilterUtil;
import com.vertex.vos.Constructors.Customer;
import com.vertex.vos.Constructors.ProductsInTransact;
import com.vertex.vos.Constructors.SalesOrder;
import com.vertex.vos.Utilities.*;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class SalesOrderIOperationsController implements Initializable {
    @FXML
    public TableView<ProductsInTransact> productsInTransact;
    @FXML
    public VBox addProductButton;
    public DatePicker dateOrdered;
    private AnchorPane contentPane;

    public void setContentPane(AnchorPane contentPane) {
        this.contentPane = contentPane;
    }

    private final HistoryManager historyManager = new HistoryManager();

    private int currentNavigationId = -1; // Initialize to a default value
    @FXML
    private AnchorPane POAnchorPane;

    @FXML
    private VBox POBox;

    @FXML
    private VBox POContent;

    @FXML
    private HBox confirmBox;

    @FXML
    private Button confirmButton;

    @FXML
    private ComboBox<String> customer;

    @FXML
    private VBox customerBox;

    @FXML
    private Label customerErr;

    @FXML
    private Label date;

    @FXML
    private Label discounted;

    @FXML
    private Label grandTotal;

    @FXML
    private Label gross;

    @FXML
    private Label paymentTerms;

    @FXML
    private Label purchaseOrderNo;

    @FXML
    private CheckBox receiptCheckBox;

    @FXML
    private Label receivingTerms;

    @FXML
    private ComboBox<String> salesman;

    @FXML
    private VBox salesmanBox;

    @FXML
    private Label salesmanErr;

    @FXML
    private HBox statusBox;

    @FXML
    private ImageView statusImage;

    @FXML
    private Label statusLabel;

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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        TableColumn<ProductsInTransact, String> productDescriptionColumn = new TableColumn<>("Product Description");
        productDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        TableColumn<ProductsInTransact, String> productUnitColumn = new TableColumn<>("Product Unit");
        productUnitColumn.setCellValueFactory(new PropertyValueFactory<>("unit"));

        TableColumn<ProductsInTransact, Double> productPriceColumn = getProductPriceColumn();

        TableColumn<ProductsInTransact, Integer> productQuantityColumn = getProductQuantityColumn();

        productsInTransact.getColumns().addAll(productDescriptionColumn, productUnitColumn, productPriceColumn, productQuantityColumn);
    }

    private TableColumn<ProductsInTransact, Integer> getProductQuantityColumn() {
        TableColumn<ProductsInTransact, Integer> productQuantityColumn = new TableColumn<>("Quantity");
        productQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("orderedQuantity"));
        productQuantityColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        productQuantityColumn.setOnEditCommit(event -> {
            ProductsInTransact product = event.getRowValue();
            product.setOrderedQuantity(event.getNewValue());
            productsInTransact.requestFocus();

        });
        return productQuantityColumn;
    }

    private TableColumn<ProductsInTransact, Double> getProductPriceColumn() {
        TableColumn<ProductsInTransact, Double> productPriceColumn = new TableColumn<>("Unit Price");
        productPriceColumn.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        productPriceColumn.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        productPriceColumn.setOnEditCommit(event -> {
            ProductsInTransact product = event.getRowValue();
            product.setUnitPrice(event.getNewValue());
            productsInTransact.requestFocus();
        });
        return productPriceColumn;
    }

    SalesmanDAO salesmanDAO = new SalesmanDAO();
    SalesDAO salesDAO = new SalesDAO();
    CustomerDAO customerDAO = new CustomerDAO();
    CreditTypeDAO creditTypeDAO = new CreditTypeDAO();


    public void createNewOrder() {
        receiptCheckBox.setSelected(true);
        int SO_NO = salesDAO.getNextSoNo();
        purchaseOrderNo.setText("SO" + SO_NO);
        ObservableList<String> salesmanNames = salesmanDAO.getAllSalesmanNames();
        ObservableList<String> customerStoreNames = customerDAO.getCustomerStoreNames();
        salesman.setItems(salesmanNames);
        customer.setItems(customerStoreNames);
        ComboBoxFilterUtil.setupComboBoxFilter(salesman, salesmanNames);
        ComboBoxFilterUtil.setupComboBoxFilter(customer, customerStoreNames);
        addProductButton.setDisable(true);
        dateOrdered.setValue(LocalDate.now());

        SalesOrder salesOrder = new SalesOrder();
        salesOrder.setOrderID(String.valueOf(SO_NO));
        salesOrder.setPoStatus("ENTRY");
        dateOrdered.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                LocalDateTime localDateTime = newValue.atStartOfDay(); // Convert LocalDate to LocalDateTime
                Timestamp timestamp = Timestamp.valueOf(localDateTime); // Convert LocalDateTime to Timestamp
                salesOrder.setCreatedDate(timestamp);
            }
        });
;

        salesman.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                int salesmanId = salesmanDAO.getSalesmanIdByStoreName(newValue);
                salesOrder.setSalesMan(String.valueOf(salesmanId));
                addProductButton.setDisable(false);
            }
        });
        customer.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                int customerId = customerDAO.getCustomerIdByStoreName(newValue);
                salesOrder.setCustomerID(String.valueOf(customerId));
                Customer selectedCustomer = customerDAO.getCustomer(customerId);
                try {
                    paymentTerms.setText(creditTypeDAO.getCreditTypeNameById(selectedCustomer.getPaymentTerm()));
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        statusLabel.setText(salesOrder.getPoStatus());
        addProductButton.setOnMouseClicked(mouseEvent -> addProductToSales(salesOrder));
        productsInTransact.getItems().addListener((ListChangeListener<ProductsInTransact>) change -> {
            confirmButton.setDisable(productsInTransact.getItems().isEmpty());
        });

        confirmButton.setDisable(productsInTransact.getItems().isEmpty());
        confirmButton.setOnMouseClicked(mouseEvent -> {
            try {
                entrySO(salesOrder);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });

    }

    private void entrySO(SalesOrder salesOrder) throws SQLException {
        ConfirmationAlert confirmationAlert = new ConfirmationAlert("Entry SO" + salesOrder.getOrderID(), "Please double check the items, quantities, and prices", "Double check values", true);
        boolean confirmed = confirmationAlert.showAndWait();

        if (confirmed) {
            List<SalesOrder> orders = new ArrayList<>();
            for (ProductsInTransact product : productsInTransact.getItems()) {
                SalesOrder order = new SalesOrder();
                order.setOrderID(salesOrder.getOrderID());
                order.setProductID(product.getProductId());
                order.setDescription(product.getDescription());
                order.setQty(product.getOrderedQuantity());
                order.setPrice(BigDecimal.valueOf(product.getUnitPrice()));
                order.setTabName(salesOrder.getTabName()); // Assuming tab name is common for all products
                order.setCustomerID(salesOrder.getCustomerID()); // Assuming customer ID is common for all products
                order.setCustomerName(salesOrder.getCustomerName()); // Assuming customer name is common for all products
                order.setStoreName(salesOrder.getStoreName()); // Assuming store name is common for all products
                order.setSalesMan(salesOrder.getSalesMan()); // Assuming salesman is common for all products
                order.setCreatedDate(salesOrder.getCreatedDate()); // Assuming created date is common for all products
                order.setTotal(salesOrder.getTotal()); // Assuming total is common for all products
                order.setPoStatus(salesOrder.getPoStatus()); // Assuming PO status is common for all products
                orders.add(order);
            }
            boolean allOrdersSuccessful = salesDAO.createOrder(orders);
            if (allOrdersSuccessful) {
                DialogUtils.showConfirmationDialog("Success", "SO has been requested");
            } else {
                DialogUtils.showErrorMessage("Error", "Please contact your System Developer");
            }
        }
    }


    private void addProductToSales(SalesOrder salesOrder) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ProductSelectionBySupplier.fxml"));
            Parent root = loader.load();
            ProductSelectionPerSupplier controller = loader.getController();

            controller.addProductToTableForSalesOrder(salesOrder);
            controller.setSalesController(this);

            Stage stage = new Stage();
            stage.setTitle("Add Products");
            stage.setScene(new Scene(root));

            // Disable the button when the window is shown
            addProductButton.setDisable(true);

            // Re-enable the button when the window is closed
            stage.setOnHidden(event -> addProductButton.setDisable(false));

            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void addProductToSalesOrderTable(ProductsInTransact product) {
        productsInTransact.getItems().add(product);
    }
}
