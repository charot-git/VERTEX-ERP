package com.vertex.vos;

import com.vertex.vos.DAO.SalesInvoiceDAO;
import com.vertex.vos.DAO.SalesInvoicePaymentsDAO;
import com.vertex.vos.DAO.SalesReturnDAO;
import com.vertex.vos.Objects.*;
import com.vertex.vos.Utilities.CustomerMemoDAO;
import com.vertex.vos.Utilities.DialogUtils;
import com.vertex.vos.Utilities.OperationDAO;
import com.vertex.vos.Utilities.SalesmanDAO;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import lombok.Setter;
import org.controlsfx.control.textfield.TextFields;

import java.io.IOException;
import java.net.URL;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.TimerTask;

public class SalesInvoicesController implements Initializable {
    public TextField salesInvoiceNumberFilter;
    public Button addButton;
    public CheckBox isDispatched;
    public CheckBox isPaid;
    public Label header;
    public DatePicker dateFrom;
    public DatePicker dateTo;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        dateFrom.setValue(java.time.LocalDate.now().minusMonths(1));
        dateTo.setValue(java.time.LocalDate.now());
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
        TableColumn<SalesInvoiceHeader, String> transactionDate = new TableColumn<>("Transaction Date");
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
        transactionDate.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getInvoiceDate().toString()));
        totalAmount.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getTotalAmount()).asObject());

        salesInvoiceTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        salesInvoiceTable.getColumns().addAll(salesInvoiceNumber, orderNoColumn, salesmanNameColumn, customerColumn, invoiceType, transactionDate, totalAmount, statusColumn);
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
    int limit = 35; // Example limit, adjust as needed


    Salesman selectedSalesman;
    Customer selectedCustomer;
    Operation selectedOperation;

    OperationDAO operationDAO = new OperationDAO();


    public void loadSalesInvoices() {
        addButton.setDefaultButton(true);
        // Autocomplete for invoice number filter
        List<Salesman> salesmanWithSalesInvoices = salesInvoiceDAO.salesmanWithSalesInvoices();
        List<Customer> storeNamesWithSalesInvoices = salesInvoiceDAO.customersWithSalesInvoices();
        List<Operation> operationList = operationDAO.getAllOperations();

        if (customerMemoFormController != null) {
            customerFilter.setText(selectedCustomer.getStoreName());
            salesmanFilter.setValue(selectedSalesman);
            selectedOperation = operationList.stream()
                    .filter(operation -> operation.getId() == selectedSalesman.getOperation()) // replace someId with your actual condition
                    .findFirst()
                    .orElse(null);
            salesTypeFilter.setValue(selectedOperation);

        }
        List<String> invoiceNumbers = salesInvoiceDAO.getAllInvoiceNumbers();
        TextFields.bindAutoCompletion(salesInvoiceNumberFilter, invoiceNumbers);

        // Load Salesman and Customer lists


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

        dateTo.valueProperty().addListener((observable, oldValue, newValue) -> {
            reloadSalesInvoices();
        });
        dateFrom.valueProperty().addListener((observable, oldValue, newValue) -> {
            reloadSalesInvoices();
        });


        setUpInfiniteScroll();

        header.setText("Sales Invoices " + salesInvoiceTable.getItems().size() + " entries");

        Platform.runLater(this::reloadSalesInvoices);
    }

    public void reloadSalesInvoices() {
        salesInvoices.clear();  // Clear previous entries
        offset = 0;  // Reset pagination

        String customerCode = (selectedCustomer != null) ? selectedCustomer.getCustomerCode() : null;
        Integer salesmanId = (selectedSalesman != null) ? selectedSalesman.getId() : null;
        Integer salesTypeId = (selectedOperation != null) ? selectedOperation.getId() : null;
        Boolean dispatched = isDispatched.isSelected();
        Boolean paid = isPaid.isSelected();
        LocalDate fromDate = dateFrom.getValue();
        LocalDate toDate = dateTo.getValue();

        List<SalesInvoiceHeader> invoices = salesInvoiceDAO.loadSalesInvoices(customerCode, salesInvoiceNumberFilter.getText(), salesmanId, salesTypeId, dispatched, paid, fromDate, toDate, offset, limit);

        if (!invoices.isEmpty()) {
            salesInvoices.addAll(invoices);
            offset += limit;
        }

        header.setText("Sales Invoices " + salesInvoiceTable.getItems().size() + " entries");

    }


    private void setUpInfiniteScroll() {
        salesInvoiceTable.skinProperty().addListener((obs, oldSkin, newSkin) -> {
            if (newSkin != null) {
                Platform.runLater(() -> {
                    ScrollBar scrollBar = findVerticalScrollBar();
                    if (scrollBar != null) {
                        scrollBar.valueProperty().addListener((observable, oldValue, newValue) -> {
                            if (newValue.doubleValue() >= 0.98) { // Close to bottom
                                if (!isLoading) { // Prevent duplicate loading
                                    isLoading = true;
                                    loadMoreInvoices();
                                }
                            }
                        });
                    }
                });
            }
        });
    }

    private ScrollBar findVerticalScrollBar() {
        for (Node node : salesInvoiceTable.lookupAll(".scroll-bar")) {
            if (node instanceof ScrollBar scrollBar && scrollBar.getOrientation() == Orientation.VERTICAL) {
                return scrollBar;
            }
        }
        return null;
    }

    private boolean isLoading = false;


    private void loadMoreInvoices() {
        header.setText("Loading...");

        new Thread(() -> {
            String customerCode = (selectedCustomer != null) ? selectedCustomer.getCustomerCode() : null;
            Integer salesmanId = (selectedSalesman != null) ? selectedSalesman.getId() : null;
            Integer salesTypeId = (selectedOperation != null) ? selectedOperation.getId() : null;
            Boolean dispatched = isDispatched.isSelected();
            Boolean paid = isPaid.isSelected();
            LocalDate fromDate = dateFrom.getValue();
            LocalDate toDate = dateTo.getValue();

            List<SalesInvoiceHeader> newInvoices = salesInvoiceDAO.loadSalesInvoices(
                    customerCode, salesInvoiceNumberFilter.getText(), salesmanId, salesTypeId, dispatched, paid, fromDate, toDate, offset, limit);

            Platform.runLater(() -> {
                if (!newInvoices.isEmpty()) {
                    salesInvoices.addAll(newInvoices);
                    offset += limit;
                }
                header.setText("Sales Invoices " + salesInvoiceTable.getItems().size() + " entries");
                isLoading = false; // Reset flag
            });
        }).start();
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
        addButton.setDefaultButton(true);
        salesmanFilter.setValue(collectionFormController.salesman);
        salesmanFilter.setEditable(false);

        Timestamp dateFromTimestamp = Timestamp.valueOf(collectionFormController.getCollectionDate().atStartOfDay());
        Timestamp dateToTimestamp = Timestamp.valueOf(collectionFormController.getCollectionDate().atStartOfDay());

        loadDataForSelection(collectionFormController.getSalesman(), dateFromTimestamp, dateToTimestamp);

        dateFrom.setValue(collectionFormController.getCollectionDate());
        dateTo.setValue(collectionFormController.getCollectionDate());

        dateFrom.valueProperty().addListener((observable, oldVal, newVal) -> {
            if (newVal != null) {
                loadDataForSelection(collectionFormController.getSalesman(),
                        Timestamp.valueOf(newVal.atStartOfDay()),
                        Timestamp.valueOf(dateTo.getValue().atStartOfDay()));
            }
        });

        dateTo.valueProperty().addListener((observable, oldVal, newVal) -> {
            if (newVal != null) {
                loadDataForSelection(collectionFormController.getSalesman(),
                        Timestamp.valueOf(dateFrom.getValue().atStartOfDay()),
                        Timestamp.valueOf(newVal.atStartOfDay()));
            }
        });

        List<String> customerNames = salesInvoiceDAO.getAllCustomerNamesForUnpaidInvoicesOfSalesman(collectionFormController.getSalesman());
        TextFields.bindAutoCompletion(customerFilter, customerNames);

        // Remove already selected invoices from available options
        salesInvoices.removeAll(collectionFormController.salesInvoiceTable.getItems());

        isPaid.setSelected(false);

        salesInvoiceTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        salesInvoiceTable.setItems(salesInvoices);

        addButton.setOnAction(event -> {
            List<SalesInvoiceHeader> selectedInvoices = new ArrayList<>(salesInvoiceTable.getSelectionModel().getSelectedItems());
            selectedInvoices.removeAll(collectionFormController.salesInvoices); // Ensures no duplicates

            if (!selectedInvoices.isEmpty()) {
                for (SalesInvoiceHeader selectedInvoice : selectedInvoices) {
                    selectedInvoice.setSalesInvoicePayments(
                            FXCollections.observableArrayList(salesInvoicePaymentsDAO.getPaymentsByInvoice(selectedInvoice.getInvoiceId()))
                    );
                    selectedInvoice.setSalesReturns(FXCollections.observableArrayList(salesReturnDAO.getSalesReturnByInvoice(selectedInvoice.getInvoiceId())));
                    selectedInvoice.setCustomerMemos(
                            FXCollections.observableArrayList(
                                    customerMemoDAO.getCustomerMemoByInvoiceId(selectedInvoice)));
                }
                collectionFormController.salesInvoices.addAll(selectedInvoices);
                salesInvoices.removeAll(selectedInvoices);
            }
            collectionFormController.updateLabelAmounts();
        });
    }

    SalesReturnDAO salesReturnDAO = new SalesReturnDAO();

    private void loadDataForSelection(Salesman salesman, Timestamp timestamp, Timestamp valueOf) {
        salesInvoices.setAll(salesInvoiceDAO.loadUnpaidAndUnlinkedSalesInvoicesBySalesman(salesman, timestamp, valueOf));
    }

    SalesInvoicePaymentsDAO salesInvoicePaymentsDAO = new SalesInvoicePaymentsDAO();
    CustomerMemoDAO customerMemoDAO = new CustomerMemoDAO();
    @Setter
    CustomerMemoFormController customerMemoFormController;

    public void invoiceDisplay() {
        setUpSelection();
        addButton.setOnAction(event -> addNewSalesInvoice());
    }

    public void loadForMemoSelection(CustomerMemo customerMemo) {
        salesInvoiceTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        addButton.setOnAction(actionEvent -> {
            ObservableList<SalesInvoiceHeader> selectedItems = FXCollections.observableArrayList(salesInvoiceTable.getSelectionModel().getSelectedItems());

            if (!selectedItems.isEmpty()) {
                for (SalesInvoiceHeader salesInvoiceHeader : selectedItems) {
                    MemoInvoiceApplication memoInvoiceApplication = new MemoInvoiceApplication();
                    memoInvoiceApplication.setSalesInvoiceHeader(salesInvoiceHeader);
                    memoInvoiceApplication.setCustomerMemo(customerMemo);
                    memoInvoiceApplication.setDateApplied(Timestamp.from(Instant.now()));
                    memoInvoiceApplication.setAmount(0);
                    customerMemoFormController.invoicesForMemo.add(memoInvoiceApplication);
                    salesInvoices.remove(salesInvoiceHeader); // Remove from original list
                }
            } else {
                DialogUtils.showErrorMessage("Error", "Please select items to add to memo");
            }
        });
    }
}
