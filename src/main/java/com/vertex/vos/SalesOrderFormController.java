package com.vertex.vos;

import com.vertex.vos.DAO.SalesInvoiceTypeDAO;
import com.vertex.vos.Enums.SalesOrderStatus;
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
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.StringConverter;
import lombok.Setter;
import org.controlsfx.control.textfield.TextFields;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class SalesOrderFormController implements Initializable {

    public BorderPane borderPane;
    public TextField poNoField;
    public Label allocatedTotalLabel;

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
    private TableColumn<SalesOrderDetails, String> discountTypeCol;


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
    private TableColumn<SalesOrderDetails, Integer> allocatedQuantityCol;

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
    @FXML
    private TableColumn<SalesOrderDetails, String> productBrandCol;
    @FXML
    TableColumn<SalesOrderDetails, String> productCategoryCol;

    public void setGeneratedSONo(int nextSoNo) {
        this.salesOrderNo = nextSoNo;
    }

    public void createNewSalesOrder() {
        salesOrder = new SalesOrder();
        salesOrder.setOrderNo("SO" + salesOrderNo);
        salesOrder.setCreatedBy(UserSession.getInstance().getUser());
        salesOrder.setCreatedDate(new java.sql.Timestamp(System.currentTimeMillis()));
        salesOrder.setOrderDate(Date.valueOf(LocalDate.now()));
        salesOrder.setOrderStatus(SalesOrderStatus.FOR_APPROVAL);
        statusLabel.setText(salesOrder.getOrderStatus().name());
        orderNo.setText(salesOrder.getOrderNo());
        dateCreatedField.setValue(salesOrder.getCreatedDate().toLocalDateTime().toLocalDate());
        orderDateField.setValue(salesOrder.getOrderDate().toLocalDate());

        confirmButton.setText("Create Order");

        confirmButton.setOnAction(actionEvent -> {
            createSalesOrder();
        });

    }

    private void createSalesOrder() {
        if (poNoField.getText().isEmpty()) {
            DialogUtils.showErrorMessage("Error", "Please enter a PO number");
            return;
        }

        // Prepare sales order details
        salesOrder.setSalesOrderDetails(salesOrderDetails);
        salesOrder.setDiscountAmount(calculateTotalDiscount());
        salesOrder.setTotalAmount(calculateTotalAmount());
        salesOrder.setNetAmount(calculateTotalNet());
        salesOrder.setRemarks(remarksField.getText());
        salesOrder.setPurchaseNo(poNoField.getText());
        salesOrder.setForApprovalAt(Timestamp.valueOf(LocalDateTime.now()));
        salesOrder.setAllocatedAmount(calculateTotalAllocatedAmount());

        // Check if customer exists before accessing store name
        boolean confirmed = createSalesOrderMethod();
        if (!confirmed) return;

        // Show loading indicator
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setPrefSize(20, 20);
        confirmButton.setGraphic(progressIndicator);
        confirmButton.setDisable(true); // Prevent multiple clicks

        // Run database operation in a background thread
        Thread createThread = getInsertThread();
        createThread.start();
    }

    private boolean createSalesOrderMethod() {
        String customerName = (salesOrder.getCustomer() != null) ? salesOrder.getCustomer().getStoreName() : "Unknown Customer";

        ConfirmationAlert confirmationAlert = new ConfirmationAlert(
                "Create SO",
                "Create SO " + (salesOrder.getOrderNo() != null ? salesOrder.getOrderNo() : "Unknown") + "?",
                "Please verify your sales order for " + customerName,
                true
        );

        return confirmationAlert.showAndWait();
    }

    private Thread getInsertThread() {
        Task<Boolean> createTask = new Task<>() {
            @Override
            protected Boolean call() {
                try {
                    return salesOrderDAO.addSalesOrder(salesOrder);
                } catch (Exception e) {
                    e.printStackTrace(); // Log error
                    return false;
                }
            }
        };

        createTask.setOnSucceeded(event -> {
            boolean created = createTask.getValue();
            Platform.runLater(() -> {
                // Remove loading indicator
                confirmButton.setGraphic(null);
                confirmButton.setDisable(false);

                if (created) {
                    if (DialogUtils.showConfirmationDialog("SO Created", "Close this window?")) {
                        Stage stage = salesOrderListController.getSalesOrderFormStage();
                        stage.close();
                    }
                    salesOrderListController.loadSalesOrder();
                } else {
                    DialogUtils.showErrorMessage("Error", "SO not created, please contact your administrator");
                }
            });
        });

        createTask.setOnFailed(event -> Platform.runLater(() -> {
            // Remove loading indicator and enable button
            confirmButton.setGraphic(null);
            confirmButton.setDisable(false);
            DialogUtils.showErrorMessage("Error", "SO creation failed due to an unexpected error.");
        }));

        Thread createThread = new Thread(createTask);
        createThread.setDaemon(true);
        return createThread;
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

        Platform.runLater(() -> {
            if (salesOrderListController != null) {
                Stage stage = null;
                if (salesOrderListController.getSalesOrderFormStage() != null) {
                    stage = salesOrderListController.getSalesOrderFormStage();
                } else if (salesOrderListController.getExistingSalesOrderStage() != null) {
                    stage = salesOrderListController.getExistingSalesOrderStage();
                }

                if (EnumSet.of(SalesOrderStatus.FOR_INVOICING, SalesOrderStatus.FOR_LOADING, SalesOrderStatus.FOR_SHIPPING, SalesOrderStatus.DELIVERED, SalesOrderStatus.CANCELLED, SalesOrderStatus.ON_HOLD).contains(salesOrder.getOrderStatus())) {
                    confirmButton.setDisable(true);
                    selectButton.setDisable(true);
                    confirmButton.setTooltip(new Tooltip("This status of this order is not editable"));
                }
                if (stage != null) {
                    stage.addEventFilter(KeyEvent.KEY_PRESSED, keyEvent -> {
                        if (keyEvent.isControlDown() && keyEvent.getCode() == KeyCode.ENTER) {
                            selectButton.fire();
                        }
                    });
                }
            }
        });
    }

    private void calculateTotals() {
        grossTotalLabel.setText(String.format("%.2f", calculateTotalGross()));
        discountTotalLabel.setText(String.format("%.2f", calculateTotalDiscount()));
        netTotalLabel.setText(String.format("%.2f", calculateTotalNet()));
        vatTotalLabel.setText(String.format("%.2f", calculateTotalVat()));
        saleTotalLabel.setText(String.format("%.2f", calculateTotalAmount()));
        allocatedTotalLabel.setText(String.format("%.2f", calculateTotalAllocatedAmount()));
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

    private Double calculateTotalAllocatedAmount() {
        return salesOrderDetails.stream().mapToDouble(SalesOrderDetails::getAllocatedAmount).sum();
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
        productBrandCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduct().getProductBrandString()));
        productCategoryCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduct().getProductCategoryString()));
        productCodeCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduct().getProductCode()));
        productNameCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getProduct().getProductName())
        );
        productNameCol.setCellFactory(col -> new TableCell<SalesOrderDetails, String>() {
            private final Text text = new Text();

            {
                text.wrappingWidthProperty().bind(col.widthProperty().subtract(10)); // Adjust padding if needed
                setGraphic(text);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    text.setText(item);
                    setGraphic(text);
                }
            }
        });


        productUnitCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduct().getUnitOfMeasurementString()));
        orderedQuantityCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getOrderedQuantity()));
        allocatedQuantityCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getAllocatedQuantity()));
        servedQuantityCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getServedQuantity()));
        discountTypeCol.setCellValueFactory(cellData -> {
            String discountName = (cellData.getValue().getDiscountType() == null) ? "No Discount" : cellData.getValue().getDiscountType().getTypeName();
            return new SimpleStringProperty(discountName);
        });
        priceCol.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getUnitPrice()).asObject());

        salesOrderTableView.setItems(salesOrderDetails);

        // Set default sorting order
        productCategoryCol.setSortType(TableColumn.SortType.ASCENDING);
        productBrandCol.setSortType(TableColumn.SortType.ASCENDING);
        salesOrderTableView.getSortOrder().setAll(productBrandCol, productCategoryCol);

        // Ensure sorting is applied when new items are added
        salesOrderDetails.addListener((ListChangeListener<SalesOrderDetails>) change -> {
            while (change.next()) {
                if (change.wasAdded() || change.wasRemoved()) {
                    salesOrderTableView.sort();
                    if (!salesOrderDetails.isEmpty()) {
                        supplierField.setDisable(true);
                        branchField.setDisable(true);
                    }
                }
            }
        });

        TableViewFormatter.formatTableView(salesOrderTableView);
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
        poNoField.setText(selectedItem == null ? null : selectedItem.getPurchaseNo());
        supplierField.setText(selectedItem == null ? null : selectedItem.getSupplier() == null ? null : selectedItem.getSupplier().getSupplierName());
        invoiceField.setValue(selectedItem == null ? null : selectedItem.getInvoiceType());
        dateCreatedField.setValue(selectedItem == null ? null : selectedItem.getCreatedDate() == null ? null : selectedItem.getCreatedDate().toLocalDateTime().toLocalDate());
        orderDateField.setValue(selectedItem == null ? null : selectedItem.getOrderDate() == null ? null : selectedItem.getOrderDate().toLocalDate());
        deliveryDateField.setValue(selectedItem == null ? null : selectedItem.getDeliveryDate() == null ? null : selectedItem.getDeliveryDate().toLocalDateTime().toLocalDate());
        dueDateField.setValue(selectedItem == null ? null : selectedItem.getDueDate() == null ? null : selectedItem.getDueDate().toLocalDateTime().toLocalDate());
        storeNameField.setText(selectedItem == null ? null : selectedItem.getCustomer() == null ? null : selectedItem.getCustomer().getStoreName());
        customerCodeField.setText(selectedItem == null ? null : selectedItem.getCustomer() == null ? null : selectedItem.getCustomer().getCustomerCode());
        salesmanNameField.setText(selectedItem == null ? null : selectedItem.getSalesman() == null ? null : selectedItem.getSalesman().getSalesmanName());
        salesmanCode.setText(selectedItem == null ? null : selectedItem.getSalesman() == null ? null : selectedItem.getSalesman().getSalesmanCode());
        branchField.setText(selectedItem == null ? null : selectedItem.getBranch() == null ? null : selectedItem.getBranch().getBranchName());
        statusLabel.setText(selectedItem == null ? null : selectedItem.getOrderStatus() == null ? null : selectedItem.getOrderStatus().name());
        remarksField.setText(selectedItem == null ? null : selectedItem.getRemarks() == null ? null : selectedItem.getRemarks());
        allocatedTotalLabel.setText(String.valueOf(selectedItem == null ? null : selectedItem.getAllocatedAmount() == null ? null : selectedItem.getAllocatedAmount()));
        salesOrderDetails.setAll(selectedItem == null ? new ArrayList<>() : selectedItem.getSalesOrderDetails());

        confirmButton.setText("Update");

        if (!salesOrder.getOrderStatus().equals(SalesOrderStatus.FOR_APPROVAL)) {
            confirmButton.setDisable(true);
        }

        confirmButton.setOnMouseClicked(mouseEvent -> {
            updateSalesOrder();
        });
    }

    private void updateSalesOrder() {
        ProgressIndicator progressIndicator = new ProgressIndicator();
        confirmButton.setGraphic(progressIndicator);
        salesOrder.setOrderNo(orderNo.getText());
        salesOrder.setPurchaseNo(poNoField.getText());
        salesOrder.setSupplier(suppliers.stream().filter(supplier -> supplier.getSupplierName().equals(supplierField.getText())).findFirst().orElseThrow());
        salesOrder.setInvoiceType(invoiceField.getSelectionModel().getSelectedItem());
        salesOrder.setCreatedDate(dateCreatedField.getValue() == null ? null : Timestamp.valueOf(dateCreatedField.getValue().atStartOfDay()));
        salesOrder.setOrderDate(orderDateField.getValue() == null ? null : Date.valueOf(orderDateField.getValue()));
        salesOrder.setDeliveryDate(deliveryDateField.getValue() == null ? null : Timestamp.valueOf(deliveryDateField.getValue().atStartOfDay()));
        salesOrder.setDueDate(dueDateField.getValue() == null ? null : Timestamp.valueOf(dueDateField.getValue().atStartOfDay()));
        salesOrder.setModifiedBy(UserSession.getInstance().getUser());
        salesOrder.setModifiedDate(Timestamp.valueOf(LocalDateTime.now()));
        salesOrder.setCustomer(customers.stream().filter(customer -> customer.getStoreName().equals(storeNameField.getText())).findFirst().orElseThrow());
        salesOrder.setSalesman(salesmen.stream().filter(salesman -> salesman.getSalesmanName().equals(salesmanNameField.getText())).findFirst().orElseThrow());
        salesOrder.setBranch(branches.stream().filter(branch -> branch.getBranchName().equals(branchField.getText())).findFirst().orElseThrow());
        salesOrder.setNetAmount(calculateTotalNet());
        salesOrder.setDiscountAmount(calculateTotalDiscount());
        salesOrder.setTotalAmount(calculateTotalAmount());
        salesOrder.setRemarks(remarksField.getText());
        salesOrder.setOrderStatus(salesOrder.getOrderStatus());
        salesOrder.setSalesOrderDetails(salesOrderDetails);
        salesOrder.setAllocatedAmount(calculateTotalAllocatedAmount());

        Thread updateThread = getUpdateThread();
        updateThread.start();

    }


    private Thread getUpdateThread() {
        Task<Boolean> updateTask = new Task<>() {
            @Override
            protected Boolean call() {
                return salesOrderDAO.updateSalesOrder(salesOrder);
            }
        };

        updateTask.setOnSucceeded(event -> {
            boolean updateSuccess = updateTask.getValue();
            Platform.runLater(() -> {
                // Remove loading indicator
                confirmButton.setGraphic(null);

                if (updateSuccess) {
                    boolean closeWindow = DialogUtils.showConfirmationDialog(
                            "Sales Order Updated", "Sales order updated successfully, close window?"
                    );

                    salesOrderListController.loadSalesOrder(); // Ensure it always refreshes

                    if (closeWindow && salesOrderListController.existingSalesOrderStage != null) {
                        salesOrderListController.existingSalesOrderStage.close();
                        salesOrderListController.existingSalesOrderStage = null;
                    }
                } else {
                    DialogUtils.showErrorMessage("Error", "Update error, please contact your administrator.");
                }
            });
        });

        updateTask.setOnFailed(event -> Platform.runLater(() -> {
            // Remove loading indicator
            confirmButton.setGraphic(null);
            DialogUtils.showErrorMessage("Error", "Update failed due to an unexpected error.");
        }));

        Thread updateThread = new Thread(updateTask);
        updateThread.setDaemon(true);
        return updateThread;
    }

}
