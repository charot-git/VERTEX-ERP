package com.vertex.vos;

import com.vertex.vos.DAO.SalesInvoiceDAO;
import com.vertex.vos.Objects.Customer;
import com.vertex.vos.Objects.Operation;
import com.vertex.vos.Objects.SalesInvoiceHeader;
import com.vertex.vos.Objects.Salesman;
import com.vertex.vos.Utilities.DialogUtils;
import com.vertex.vos.Utilities.OperationDAO;
import com.vertex.vos.Utilities.SalesmanDAO;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import lombok.Setter;
import org.controlsfx.control.textfield.TextFields;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class SalesInvoicesController implements Initializable {
    public TextField salesInvoiceNumberFilter;
    public Button addButton;
    public CheckBox isDispatched;
    public CheckBox isPaid;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setUpTable();
    }

    private void setUpSelection() {
        salesInvoiceTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                SalesInvoiceHeader salesInvoiceHeader = salesInvoiceTable.getSelectionModel().getSelectedItem();
                if (salesInvoiceHeader != null) {
                    openSalesInvoice(salesInvoiceHeader);
                }
            }
        });
    }

    private void openSalesInvoice(SalesInvoiceHeader salesInvoiceHeader) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("SalesInvoiceTemporary.fxml"));
            Parent root = loader.load();
            SalesInvoiceTemporaryController controller = loader.getController();
            controller.initData(salesInvoiceHeader);
            controller.setSalesInvoicesController(this);
            Stage stage = new Stage();
            stage.setTitle("Order#" + salesInvoiceHeader.getOrderId() + " - " + salesInvoiceHeader.getCustomer().getStoreName());
            controller.setStage(stage);
            stage.setMaximized(true);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            DialogUtils.showErrorMessage("Error", "Unable to open document.");
            e.printStackTrace();
        }

    }

    SalesInvoiceDAO salesInvoiceDAO = new SalesInvoiceDAO();


    private void setUpTable() {
        salesInvoiceTable.setItems(salesInvoices);
        TableColumn<SalesInvoiceHeader, String> salesInvoiceNumber = new TableColumn<>("Sales Invoice No.");
        TableColumn<SalesInvoiceHeader, Double> totalAmount = new TableColumn<>("Total Amount");
        TableColumn<SalesInvoiceHeader, String> createdDateColumn = new TableColumn<>("Created Date");
        TableColumn<SalesInvoiceHeader, String> salesmanNameColumn = new TableColumn<>("Salesman");
        TableColumn<SalesInvoiceHeader, String> orderNoColumn = new TableColumn<>("Order No");
        TableColumn<SalesInvoiceHeader, String> invoiceType = new TableColumn<>("Invoice Type");
        invoiceType.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getInvoiceType().getName()));
        TableColumn<SalesInvoiceHeader, String> customerColumn = new TableColumn<>("Customer");
        TableColumn<SalesInvoiceHeader, String> statusColumn = new TableColumn<>("Status");
        TableColumn<SalesInvoiceHeader, String> paymentStatusColumn = new TableColumn<>("Payment Status");
        TableColumn<SalesInvoiceHeader, String> transactionStatus = new TableColumn<>("Transaction Status");
        orderNoColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getOrderId()));

        salesInvoiceNumber.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getInvoiceNo()));
        salesmanNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getSalesman().getSalesmanName()));
        statusColumn.getColumns().addAll(transactionStatus, paymentStatusColumn);
        customerColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCustomer().getStoreName()));
        paymentStatusColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPaymentStatus()));
        transactionStatus.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTransactionStatus()));
        createdDateColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCreatedDate().toString()));
        totalAmount.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getTotalAmount()).asObject());

        salesInvoiceTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        salesInvoiceTable.getColumns().addAll(salesInvoiceNumber, orderNoColumn, salesmanNameColumn, customerColumn, invoiceType, createdDateColumn, totalAmount, statusColumn);
    }


    @FXML
    public TableView<SalesInvoiceHeader> salesInvoiceTable;

    @FXML
    private ComboBox<Operation> salesTypeFilter;

    @FXML
    private ComboBox<Salesman> salesmanFilter;
    SalesmanDAO salesmanDAO = new SalesmanDAO();

    @Setter
    AnchorPane contentPane;
    ObservableList<SalesInvoiceHeader> salesInvoices = FXCollections.observableArrayList();

    @FXML
    private TextField customerFilter;


    int offset = 0;  // Default pagination offset
    int limit = 30; // Example limit, adjust as needed


    Salesman selectedSalesman;
    Customer selectedCustomer;
    Operation selectedOperation;

    OperationDAO operationDAO = new OperationDAO();


    public void loadSalesInvoices() {
        addButton.setDefaultButton(true);

        // Autocomplete for invoice number filter
        List<String> invoiceNumbers = salesInvoiceDAO.getAllInvoiceNumbers();
        TextFields.bindAutoCompletion(salesInvoiceNumberFilter, invoiceNumbers);

        // Load Salesman and Customer lists
        List<Salesman> salesmanWithSalesInvoices = salesInvoiceDAO.salesmanWithSalesInvoices();
        List<Customer> storeNamesWithSalesInvoices = salesInvoiceDAO.customersWithSalesInvoices();
        List<Operation> operationList = operationDAO.getAllOperations();

        salesTypeFilter.setItems(FXCollections.observableArrayList(operationList));
        customerFilter.setPromptText("Store Name");
        TextFields.bindAutoCompletion(customerFilter, storeNamesWithSalesInvoices.stream().map(Customer::getStoreName).toList());
        // Set items in ComboBoxes
        salesmanFilter.setItems(FXCollections.observableArrayList(salesmanWithSalesInvoices));
        TextFields.bindAutoCompletion(customerFilter, storeNamesWithSalesInvoices.stream().map(Customer::getStoreName).toList());

        salesInvoiceNumberFilter.textProperty().addListener((observable, oldValue, newValue) -> {
            reloadSalesInvoices();
        });

        salesTypeFilter.setConverter(new StringConverter<Operation>() {
            @Override
            public String toString(Operation operation) {
                return (operation != null) ? operation.getOperationName() : "";
            }

            @Override
            public Operation fromString(String string) {
                return salesTypeFilter.getItems().stream()
                        .filter(operation -> operation.getOperationName().equals(string))
                        .findFirst()
                        .orElse(null);
            }
        });

        salesmanFilter.setConverter(new StringConverter<Salesman>() {
            @Override
            public String toString(Salesman salesman) {
                return (salesman != null) ? salesman.getSalesmanName() : "";
            }

            @Override
            public Salesman fromString(String string) {
                return salesmanFilter.getItems().stream()
                        .filter(salesman -> salesman.getSalesmanName().equals(string))
                        .findFirst()
                        .orElse(null);
            }
        });


        isPaid.selectedProperty().addListener((observable, oldValue, newValue) -> reloadSalesInvoices());
        isDispatched.selectedProperty().addListener((observable, oldValue, newValue) -> reloadSalesInvoices());
        salesmanFilter.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            selectedSalesman = newValue;
            reloadSalesInvoices();
        });

        customerFilter.textProperty().addListener((observable, oldValue, newValue) -> {
            selectedCustomer = storeNamesWithSalesInvoices.stream().filter(customer -> customer.getStoreName().equals(newValue)).findFirst().orElse(null);
            reloadSalesInvoices();
        });

        salesTypeFilter.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            selectedOperation = newValue;
            reloadSalesInvoices();
        });

        setUpSelection();
        setUpInfiniteScroll();

        addButton.setOnAction(event -> addNewSalesInvoice());
    }

    public void reloadSalesInvoices() {
        salesInvoices.clear();  // Clear previous entries
        offset = 0;  // Reset pagination

        String customerCode = (selectedCustomer != null) ? selectedCustomer.getCustomerCode() : null;
        Integer salesmanId = (selectedSalesman != null) ? selectedSalesman.getId() : null;
        Integer salesTypeId = (selectedOperation != null) ? selectedOperation.getId() : null;
        Boolean dispatched = isDispatched.isSelected();
        Boolean paid = isPaid.isSelected();

        List<SalesInvoiceHeader> invoices = salesInvoiceDAO.loadSalesInvoices(customerCode, salesInvoiceNumberFilter.getText(), salesmanId, salesTypeId, dispatched, paid, offset, limit);

        if (!invoices.isEmpty()) {
            salesInvoices.addAll(invoices);
            offset += limit;
        }
    }


    private void setUpInfiniteScroll() {
        salesInvoiceTable.skinProperty().addListener((obs, oldSkin, newSkin) -> {
            if (newSkin != null) {
                ScrollBar scrollBar = (ScrollBar) salesInvoiceTable.lookup(".scroll-bar:vertical");
                if (scrollBar != null) {
                    scrollBar.valueProperty().addListener((observable, oldValue, newValue) -> {
                        if (newValue.doubleValue() >= 1.0) { // Bottom of the table
                            offset += limit;
                            loadSalesInvoices();
                        }
                    });
                }
            }
        });
    }


    private void addNewSalesInvoice() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("SalesInvoiceTemporary.fxml"));
            Parent root = loader.load();
            SalesInvoiceTemporaryController controller = loader.getController();


            Stage stage = new Stage();
            stage.setTitle("Sales Encoding");
            controller.createNewSalesEntry(stage);
            controller.setSalesInvoicesController(this);
            stage.setMaximized(true);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            DialogUtils.showErrorMessage("Error", "Unable to open.");
            e.printStackTrace();
        }
    }

    public void openInvoicesSelection(Stage parentStage, CollectionFormController collectionFormController) {
        salesmanFilter.setValue(collectionFormController.salesman);
        salesmanFilter.setEditable(false);

        List<String> customerNames = salesInvoiceDAO.getAllCustomerNamesForUnpaidInvoicesOfSalesman(collectionFormController.getSalesman());
        TextFields.bindAutoCompletion(customerFilter, customerNames);

        salesInvoices.setAll(salesInvoiceDAO.loadUnpaidSalesInvoicesBySalesman(collectionFormController.getSalesman()));

        salesInvoiceTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        addButton.setOnAction(event -> {
            List<SalesInvoiceHeader> selectedInvoices = salesInvoiceTable.getSelectionModel().getSelectedItems();
            selectedInvoices.removeIf(collectionFormController.salesInvoices::contains);
            if (!selectedInvoices.isEmpty()) {
                collectionFormController.salesInvoices.addAll(selectedInvoices);
            }
        });
    }

}
