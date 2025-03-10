package com.vertex.vos;

import com.vertex.vos.DAO.SalesInvoiceTypeDAO;
import com.vertex.vos.Objects.*;
import com.vertex.vos.Utilities.*;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.util.Duration;
import javafx.util.StringConverter;
import lombok.Setter;
import org.controlsfx.control.textfield.TextFields;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Objects;
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
    @FXML
    private Label itemSizeLabel;

    @Setter
    SalesOrderListController salesOrderListController;

    SalesOrderDAO salesOrderDAO = new SalesOrderDAO();

    @FXML
    private Button selectButton;

    ObservableList<SalesOrderDetails> salesOrderDetails = FXCollections.observableArrayList();

    SalesOrder salesOrder;

    int salesOrderNo;

    public void setGeneratedSONo(int nextSoNo) {
        this.salesOrderNo = nextSoNo;
    }

    public void createNewSalesOrder() {
        salesOrder = new SalesOrder();
        salesOrder.setOrderNo("SO" + salesOrderNo);
        salesOrder.setCreatedBy(UserSession.getInstance().getUser());
        salesOrder.setCreatedDate(new java.sql.Timestamp(System.currentTimeMillis()));
        salesOrder.setOrderDate(new java.sql.Timestamp(System.currentTimeMillis()));
        salesOrder.setOrderStatus(SalesOrder.SalesOrderStatus.FOR_APPROVAL);
        statusLabel.setText(salesOrder.getOrderStatus().name());
        orderNo.setText(salesOrder.getOrderNo());
        dateCreatedField.setValue(salesOrder.getCreatedDate().toLocalDateTime().toLocalDate());
        orderDateField.setValue(salesOrder.getOrderDate().toLocalDateTime().toLocalDate());

        confirmButton.setText("Create Order");

        confirmButton.setOnAction(actionEvent -> {
            createSalesOrder();
        });
    }

    private void createSalesOrder() {
        salesOrder.setSalesOrderDetails(salesOrderDetails);
        salesOrder.setDiscountAmount(calculateTotalDiscount());
        salesOrder.setTotalAmount(calculateTotalAmount());
        salesOrder.setNetAmount(calculateTotalNet());
        salesOrder.setRemarks(remarksField.getText());


        ConfirmationAlert confirmationAlert = new ConfirmationAlert("Create SO",
                "Create SO" + salesOrder.getOrderNo() + "?",
                "Please verify your sales order for " + salesOrder.getCustomer().getStoreName(),
                true);

        boolean confirmed = confirmationAlert.showAndWait();
        if (confirmed) {
            boolean created = salesOrderDAO.addSalesOrder(salesOrder);
            if (created) {
                confirmButton.setDisable(true);
                if (DialogUtils.showConfirmationDialog("SO Created", "Close this window?")){
                    salesOrderListController.getSalesOrderFormStage().close();
                }
                salesOrderListController.loadSalesOrder();

            } else {
                DialogUtils.showErrorMessage("Error", "SO not created, please contact your administrator");
            }
        }
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

        salesOrderDetails.addListener((ListChangeListener<SalesOrderDetails>) observable -> {
            calculateTotals();
        });

        salesOrderTableView.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.DELETE) {
                salesOrderDetails.remove(salesOrderTableView.getSelectionModel().getSelectedItem());
            }
        });
        salesOrderTableView.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                editProduct(salesOrderTableView.getSelectionModel().getSelectedItem());
            }
        });
        salesOrderTableView.setOnMouseClicked(mouseEvent -> {
            editProduct(salesOrderTableView.getSelectionModel().getSelectedItem());
        });
    }

    private void calculateTotals() {
        grossTotalLabel.setText(String.format("%.2f", calculateTotalGross()));
        discountTotalLabel.setText(String.format("%.2f", calculateTotalDiscount()));
        netTotalLabel.setText(String.format("%.2f", calculateTotalNet()));
        vatTotalLabel.setText(String.format("%.2f", calculateTotalVat()));
        saleTotalLabel.setText(String.format("%.2f", calculateTotalAmount()));
        itemSizeLabel.setText(String.valueOf(salesOrderDetails.size()));
    }

    private double calculateTotalGross() {
        return salesOrderDetails.stream().mapToDouble(SalesOrderDetails::getGrossAmount).sum();
    }

    private double calculateTotalNet() {
        return salesOrderDetails.stream().mapToDouble(SalesOrderDetails::getNetAmount).sum();
    }

    private double calculateTotalDiscount() {
        return salesOrderDetails.stream().mapToDouble(SalesOrderDetails::getDiscountAmount).sum();
    }

    private double calculateTotalAmount() {
        return calculateTotalNet() + calculateTotalVat();
    }

    private double calculateTotalVat() {
        return VATCalculator.calculateVat(BigDecimal.valueOf(calculateTotalNet())).doubleValue();
    }

    OperationDAO operationDAO = new OperationDAO();

    private void setupTextFields() {
        TextFields.bindAutoCompletion(supplierField, suppliers.stream().map(Supplier::getSupplierName).collect(Collectors.toList()));
        TextFields.bindAutoCompletion(branchField, branches.stream().map(Branch::getBranchName).collect(Collectors.toList()));
        TextFields.bindAutoCompletion(storeNameField, customers.stream().map(Customer::getStoreName).collect(Collectors.toList()));
        TextFields.bindAutoCompletion(salesmanNameField, salesmen.stream().filter(s -> s.getOperation() == 1).map(Salesman::getSalesmanName).collect(Collectors.toList()));
        supplierField.textProperty().addListener((observable, oldValue, newValue) -> {
            suppliers.stream().filter(s -> s.getSupplierName().equals(newValue)).findFirst().ifPresent(supplier -> {
                salesOrder.setSupplier(supplier);
                salesOrder.setOrderNo(supplier.getSupplierShortcut() + salesOrderNo);
                orderNo.setText(salesOrder.getOrderNo());
            });
        });

        salesmanNameField.textProperty().addListener(((observable, oldValue, newValue) -> {
            salesmen.stream().filter(s -> s.getSalesmanName().equals(newValue)).findFirst().ifPresent(salesman -> {
                salesOrder.setSalesman(salesman);
                salesOrder.setSalesType(operationDAO.getOperationById(salesman.getOperation()));
                salesmanCode.setText(salesman.getSalesmanCode());
                String branchName = branches.stream()
                        .filter(branch -> branch.getId() == salesman.getGoodBranchCode())
                        .findFirst()
                        .map(Branch::getBranchName)
                        .orElse("");
                branchField.setText(branchName);
                salesOrder.setBranch(branches.stream().filter(branch -> branch.getId() == salesman.getGoodBranchCode()).findFirst().orElse(null));
            });
        }));

        branchField.textProperty().addListener(((observable, oldValue, newValue) -> {
            branches.stream().filter(b -> b.getBranchName().equals(newValue)).findFirst().ifPresent(branch -> salesOrder.setBranch(branch));
        }));

        storeNameField.textProperty().addListener(((observable, oldValue, newValue) -> {
            customers.stream().filter(c -> c.getStoreName().equals(newValue)).findFirst().ifPresent(customer -> {
                salesOrder.setCustomer(customer);
                salesOrder.setPaymentTerms((int) customer.getPaymentTerm());
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

        if (salesOrder.getInvoiceType() == null) {
            DialogUtils.showErrorMessage("Error", "Please select a receipt");
            invoiceField.requestFocus();
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("SalesOrderProductSelection.fxml"));
            productSelectionView = loader.load();
            SalesOrderProductSelectionController controller = loader.getController();
            controller.setSalesOrderFormController(this);
            controller.initializeNewDetail();

            borderPane.setLeft(productSelectionView);

            Platform.runLater(() -> {
                double panelWidth = productSelectionView.getBoundsInParent().getWidth();

                productSelectionView.setTranslateX(-panelWidth);

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

    private void editProduct(SalesOrderDetails selectedItem) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("SalesOrderProductSelection.fxml"));
            productSelectionView = loader.load();
            SalesOrderProductSelectionController controller = loader.getController();
            controller.setSalesOrderFormController(this);
            controller.initializeItemForUpdate(selectedItem);

            borderPane.setLeft(productSelectionView);

            Platform.runLater(() -> {
                double panelWidth = productSelectionView.getBoundsInParent().getWidth();

                productSelectionView.setTranslateX(-panelWidth);

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
        discountTypeCol.setCellValueFactory(cellData -> {
            String discountName = cellData.getValue().getDiscountType() == null ? "No Discount" : cellData.getValue().getDiscountType().getTypeName();
            return new SimpleStringProperty(discountName);
        });
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
            }
        });
    }

    public void openSalesOrder(SalesOrder selectedItem) {
        salesOrder = selectedItem;
        orderNo.setText(selectedItem == null ? null : selectedItem.getOrderNo());
        supplierField.setText(selectedItem == null ? null : selectedItem.getSupplier() == null ? null : selectedItem.getSupplier().getSupplierName());
        invoiceField.setValue(selectedItem == null ? null : selectedItem.getInvoiceType());
        dateCreatedField.setValue(selectedItem == null ? null : selectedItem.getCreatedDate() == null ? null : selectedItem.getCreatedDate().toLocalDateTime().toLocalDate());
        orderDateField.setValue(selectedItem == null ? null : selectedItem.getOrderDate() == null ? null : selectedItem.getOrderDate().toLocalDateTime().toLocalDate());
        deliveryDateField.setValue(selectedItem == null ? null : selectedItem.getDeliveryDate() == null ? null : selectedItem.getDeliveryDate().toLocalDateTime().toLocalDate());
        dueDateField.setValue(selectedItem == null ? null : selectedItem.getDueDate() == null ? null : selectedItem.getDueDate().toLocalDateTime().toLocalDate());
        storeNameField.setText(selectedItem == null ? null : selectedItem.getCustomer() == null ? null : selectedItem.getCustomer().getStoreName());
        customerCodeField.setText(selectedItem == null ? null : selectedItem.getCustomer() == null ? null : selectedItem.getCustomer().getCustomerCode());
        salesmanNameField.setText(selectedItem == null ? null : selectedItem.getSalesman() == null ? null : selectedItem.getSalesman().getSalesmanName());
        salesmanCode.setText(selectedItem == null ? null : selectedItem.getSalesman() == null ? null : selectedItem.getSalesman().getSalesmanCode());
        branchField.setText(selectedItem == null ? null : selectedItem.getBranch() == null ? null : selectedItem.getBranch().getBranchName());
        statusLabel.setText(selectedItem == null ? null : selectedItem.getOrderStatus() == null ? null : selectedItem.getOrderStatus().name());
        remarksField.setText(selectedItem == null ? null : selectedItem.getRemarks() == null ? null : selectedItem.getRemarks());
        salesOrderDetails.setAll(selectedItem == null ? new ArrayList<>() : selectedItem.getSalesOrderDetails());

        confirmButton.setText("Update");

        confirmButton.setOnMouseClicked(mouseEvent -> {
            updateSalesOrder();
        });
    }

    private void updateSalesOrder() {
        salesOrder.setOrderNo(orderNo.getText());
        salesOrder.setSupplier(suppliers.stream().filter(supplier -> supplier.getSupplierName().equals(supplierField.getText())).findFirst().orElse(null));
        salesOrder.setInvoiceType(invoiceField.getSelectionModel().getSelectedItem());
        salesOrder.setCreatedDate(dateCreatedField.getValue() == null ? null : Timestamp.valueOf(dateCreatedField.getValue().atStartOfDay()));
        salesOrder.setOrderDate(orderDateField.getValue() == null ? null : Timestamp.valueOf(orderDateField.getValue().atStartOfDay()));
        salesOrder.setDeliveryDate(deliveryDateField.getValue() == null ? null : Timestamp.valueOf(deliveryDateField.getValue().atStartOfDay()));
        salesOrder.setDueDate(dueDateField.getValue() == null ? null : Timestamp.valueOf(dueDateField.getValue().atStartOfDay()));
        salesOrder.setModifiedBy(UserSession.getInstance().getUser());
        salesOrder.setModifiedDate(Timestamp.valueOf(LocalDateTime.now()));
        salesOrder.setCustomer(customers.stream().filter(customer -> customer.getStoreName().equals(storeNameField.getText())).findFirst().orElse(null));
        salesOrder.setSalesman(salesmen.stream().filter(salesman -> salesman.getSalesmanName().equals(salesmanNameField.getText())).findFirst().orElse(null));
        salesOrder.setBranch(branches.stream().filter(branch -> branch.getBranchName().equals(branchField.getText())).findFirst().orElse(null));
        salesOrder.setNetAmount(calculateTotalNet());
        salesOrder.setDiscountAmount(calculateTotalDiscount());
        salesOrder.setTotalAmount(calculateTotalAmount());
        salesOrder.setRemarks(remarksField.getText());
        salesOrder.setOrderStatus(salesOrder.getOrderStatus());
        salesOrder.setSalesOrderDetails(salesOrderDetails);

        ConfirmationAlert confirmationAlert = new ConfirmationAlert("Update Sales Order", "Please verify before updating", "Update " + salesOrder.getOrderNo() + "?", true);

        boolean confirmed = confirmationAlert.showAndWait();
        if (confirmed) {
            boolean updated = salesOrderDAO.updateSalesOrder(salesOrder);
            if (updated) {
                DialogUtils.showCompletionDialog("Sales Order Updated", "Sales order updated successfully.");
                salesOrderListController.loadSalesOrder();
            } else {
                DialogUtils.showErrorMessage("Error", "Update error, please contact your administrator.");
            }
        }
    }
}
