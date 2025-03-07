package com.vertex.vos;

import com.vertex.vos.DAO.SalesInvoiceTypeDAO;
import com.vertex.vos.Objects.*;
import com.vertex.vos.Utilities.*;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;
import javafx.util.StringConverter;
import lombok.Setter;
import org.controlsfx.control.textfield.TextFields;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class SalesOrderFormController implements Initializable {

    public BorderPane borderPane;
    @FXML
    private Label orderNo;

    @FXML
    private Label statusLabel;

    @FXML
    private Label discountTotalLabel;

    @FXML
    private Label grossTotalLabel;

    @FXML
    private Label netTotalLabel;

    @FXML
    private Label saleTotalLabel;

    @FXML
    private Label vatTotalLabel;

    @FXML
    private TableView<SalesOrderDetails> salesOrderTableView;

    @FXML
    private TableColumn<SalesOrderDetails, Double> discountCol;

    @FXML
    private TableColumn<SalesOrderDetails, String> discountTypeCol;

    @FXML
    private TableColumn<SalesOrderDetails, Double> grossCol;

    @FXML
    private TableColumn<SalesOrderDetails, Double> netCol;

    @FXML
    private TableColumn<SalesOrderDetails, Integer> orderedQuantityCol;

    @FXML
    private TableColumn<SalesOrderDetails, Double> priceCol;

    @FXML
    private TableColumn<SalesOrderDetails, String> productCodeCol;

    @FXML
    private TableColumn<SalesOrderDetails, String> productNameCol;

    @FXML
    private TableColumn<SalesOrderDetails, String> productUnitCol;

    @FXML
    private TableColumn<SalesOrderDetails, Integer> servedQuantityCol;

    @FXML
    private Button confirmButton;

    @FXML
    private ComboBox<SalesInvoiceType> invoiceField;

    @FXML
    private DatePicker dateCreatedField;

    @FXML
    private DatePicker deliveryDateField;

    @FXML
    private DatePicker dueDateField;

    @FXML
    private DatePicker orderDateField;

    @FXML
    private TextArea remarksField;

    @FXML
    private TextField branchField;

    @FXML
    private TextField customerCodeField;

    @FXML
    private TextField salesmanCode;

    @FXML
    private TextField salesmanNameField;

    @FXML
    private TextField storeNameField;

    @FXML
    private TextField supplierField;

    @Setter
    SalesOrderListController salesOrderListController;

    SalesOrderDAO salesOrderDAO = new SalesOrderDAO();

    @FXML
    private Button selectButton;

    ObservableList<SalesOrderDetails> salesOrderDetails = FXCollections.observableArrayList();

    SalesOrder salesOrder;

    public void createNewSalesOrder() {
        salesOrder = new SalesOrder();
        salesOrder.setOrderNo("SO" + salesOrderDAO.getNextSoNo());
        salesOrder.setCreatedBy(UserSession.getInstance().getUser());
        salesOrder.setCreatedDate(new java.sql.Timestamp(System.currentTimeMillis()));
        salesOrder.setOrderDate(new java.sql.Timestamp(System.currentTimeMillis()));
        salesOrder.setOrderStatus(SalesOrder.SalesOrderStatus.FOR_APPROVAL);

        statusLabel.setText(salesOrder.getOrderStatus().name());
        orderNo.setText(salesOrder.getOrderNo());
    }

    SalesInvoiceTypeDAO salesInvoiceTypeDAO = new SalesInvoiceTypeDAO();

    ObservableList<SalesInvoiceType> salesInvoiceTypes = salesInvoiceTypeDAO.getSalesInvoiceTypes();

    SupplierDAO supplierDAO = new SupplierDAO();
    SalesmanDAO salesmanDAO = new SalesmanDAO();
    BranchDAO branchDAO = new BranchDAO();
    CustomerDAO customerDAO = new CustomerDAO();

    ObservableList<Supplier> suppliers = supplierDAO.getAllActiveSuppliers();
    ObservableList<Salesman> salesmen = FXCollections.observableArrayList(salesmanDAO.getAllActiveSalesmen());
    ObservableList<Branch> branches = FXCollections.observableArrayList(branchDAO.getAllActiveBranches());
    ObservableList<Customer> customers = FXCollections.observableArrayList(customerDAO.getAllActiveCustomers());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dateCreatedField.setPromptText(LocalDate.now().toString());
        deliveryDateField.setPromptText(LocalDate.now().toString());
        dueDateField.setPromptText(LocalDate.now().toString());
        orderDateField.setPromptText(LocalDate.now().toString());
        setupTableView();
        setupReceiptType();

        selectButton.setOnAction(event -> openProductSelection());

        Platform.runLater(this::setupTextFields);

    }

    private void setupTextFields() {
        TextFields.bindAutoCompletion(supplierField, suppliers.stream().map(Supplier::getSupplierName).collect(Collectors.toList()));
        TextFields.bindAutoCompletion(branchField, branches.stream().map(Branch::getBranchName).collect(Collectors.toList()));
        TextFields.bindAutoCompletion(storeNameField, customers.stream().map(Customer::getStoreName).collect(Collectors.toList()));
        TextFields.bindAutoCompletion(salesmanNameField, salesmen.stream().filter(s -> s.getOperation() == 1).map(Salesman::getSalesmanName).collect(Collectors.toList()));
        supplierField.textProperty().addListener((observable, oldValue, newValue) -> {
            suppliers.stream().filter(s -> s.getSupplierName().equals(newValue)).findFirst().ifPresent(supplier -> salesOrder.setSupplier(supplier));
        });

        salesmanNameField.textProperty().addListener(((observable, oldValue, newValue) -> {
            salesmen.stream().filter(s -> s.getSalesmanName().equals(newValue)).findFirst().ifPresent(salesman -> {
                salesOrder.setSalesman(salesman);
                salesmanCode.setText(salesman.getSalesmanCode());
                String branchName = branches.stream()
                        .filter(branch -> branch.getId() == salesman.getGoodBranchCode())
                        .findFirst()
                        .map(Branch::getBranchName)
                        .orElse("");
                branchField.setText(branchName);
            });
        }));

        branchField.textProperty().addListener(((observable, oldValue, newValue) -> {
            branches.stream().filter(b -> b.getBranchName().equals(newValue)).findFirst().ifPresent(branch -> salesOrder.setBranch(branch));
        }));

        storeNameField.textProperty().addListener(((observable, oldValue, newValue) -> {
            customers.stream().filter(c -> c.getStoreName().equals(newValue)).findFirst().ifPresent(customer -> {
                salesOrder.setCustomer(customer);
                customerCodeField.setText(customer.getCustomerCode());
            });
        }));

        invoiceField.valueProperty().addListener(((observable, oldValue, newValue) -> {
            salesOrder.setInvoiceType(newValue);
        }));
    }

    private boolean isProductSelectionOpen = false; // Track if the panel is open
    private Parent productSelectionView; // Store reference to avoid reloading

    private void openProductSelection() {
        if (isProductSelectionOpen) {
            closeProductSelection();
            return;
        }

        if (salesOrder.getSupplier() == null) {
            DialogUtils.showErrorMessage("Error", "Please select a supplier");
            supplierField.requestFocus();
            return;
        }

        if (salesOrder.getBranch() == null) {
            DialogUtils.showErrorMessage("Error", "Please select a branch");
            branchField.requestFocus();
            return;
        }

        if (salesOrder.getSalesman() == null) {
            DialogUtils.showErrorMessage("Error", "Please select a salesman");
            salesmanNameField.requestFocus();
            return;
        }

        if (salesOrder.getInvoiceType() == null){
            DialogUtils.showErrorMessage("Error" , "Please select a receipt");
            invoiceField.requestFocus();
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("SalesOrderProductSelection.fxml"));
            productSelectionView = loader.load();
            SalesOrderProductSelectionController controller = loader.getController();
            controller.setSalesOrderFormController(this);
            controller.initializeNewDetail();

            borderPane.setLeft(productSelectionView);

            // Wait for layout to be applied before getting actual width
            Platform.runLater(() -> {
                double panelWidth = productSelectionView.getBoundsInParent().getWidth();

                // Initially position off-screen
                productSelectionView.setTranslateX(-panelWidth);

                // Animate sliding in
                TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), productSelectionView);
                slideIn.setFromX(-panelWidth);
                slideIn.setToX(0);
                slideIn.play();

                isProductSelectionOpen = true;
            });

        } catch (IOException e) {
            DialogUtils.showErrorMessage("Error", "Unable to open product selection.");
            e.printStackTrace();
        }
    }


    private void closeProductSelection() {
        if (productSelectionView == null) return;

        double panelWidth = productSelectionView.getBoundsInParent().getWidth();

        TranslateTransition slideOut = new TranslateTransition(Duration.millis(300), productSelectionView);
        slideOut.setFromX(0);
        slideOut.setToX(-panelWidth);
        slideOut.setOnFinished(event -> borderPane.setLeft(null));

        slideOut.play();
        isProductSelectionOpen = false;
    }


    private void setupTableView() {
        productCodeCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduct().getProductCode()));
        productNameCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduct().getProductName()));
        productUnitCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduct().getUnitOfMeasurementString()));
        orderedQuantityCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>((cellData.getValue().getOrderedQuantity())));
        servedQuantityCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>((cellData.getValue().getServedQuantity())));
        discountTypeCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDiscountType().getTypeName()));
        priceCol.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getUnitPrice()).asObject());
        grossCol.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getGrossAmount()).asObject());
        discountCol.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getDiscountAmount()).asObject());
        netCol.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getNetAmount()).asObject());

        salesOrderTableView.setItems(salesOrderDetails);
    }

    private void setupReceiptType() {
        invoiceField.setItems(salesInvoiceTypes);
        invoiceField.setConverter(new StringConverter<>() {
            @Override
            public String toString(SalesInvoiceType type) {
                return (type != null) ? type.getName() : "";
            }

            @Override
            public SalesInvoiceType fromString(String string) {
                return salesInvoiceTypes.stream()
                        .filter(type -> type.getName().equals(string))
                        .findFirst().orElse(null);
            }
        });
        invoiceField.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                salesOrder.setInvoiceType(newValue);
                updateTotals();
            }
        });
    }

    private void updateTotals() {
    }
}
