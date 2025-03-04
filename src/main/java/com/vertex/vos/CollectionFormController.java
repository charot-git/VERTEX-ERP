package com.vertex.vos;

import com.vertex.vos.DAO.CollectionDAO;
import com.vertex.vos.Objects.*;
import com.vertex.vos.Objects.Collection;
import com.vertex.vos.Utilities.*;
import javafx.application.Platform;
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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import lombok.Getter;
import org.controlsfx.control.textfield.TextFields;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class CollectionFormController implements Initializable {

    public ButtonBar buttonBar;
    public TableColumn<CollectionDetail, String> typeCollectionDetailCol;
    public TableColumn<CollectionDetail, String> bankCollectionDetailCol;
    public TableColumn<CollectionDetail, String> checkNoCollectionDetailCol;
    public TableColumn<CollectionDetail, Timestamp> checkDateCollectionDetailCol;
    public TableColumn<CollectionDetail, Double> amountCollectionDetailCol;
    public TableColumn<CollectionDetail, String> remarksCollectionDetailCol;

    public TableColumn<SalesInvoiceHeader, String> paidAmountInvCol;
    public Label collectionBalance;
    public Label paidBalance;
    public TableColumn<CollectionDetail, String> customerCollectionCol;
    public Label totalAdjustmentAmount;
    public TableColumn<SalesReturn, Timestamp> returnDateCol;
    public TableColumn<SalesReturn, String> returnRemarksCol;
    public TableColumn<SalesReturn, Double> returnAmountCol;
    public Label collectionAmount;
    public BorderPane collectionPane;
    @FXML
    private Button addAdjustmentButton;

    @FXML
    private Button addInvoiceButton;

    @FXML
    private Button addPaymentButton;

    @FXML
    private Button addReturnsButton;

    @FXML
    private TableColumn<SalesInvoiceHeader, Double> amountColInv;

    @FXML
    private TableColumn<CustomerMemo, Double> amountColMem;

    @FXML
    private DatePicker collectionDateDatePicker;

    @FXML
    private TableView<CollectionDetail> collectionDetailsTableView;

    @FXML
    private TextField collectorNameTextField;

    @FXML
    private TableColumn<CustomerMemo, String> customeCodeColMem;

    @FXML
    private TableColumn<SalesInvoiceHeader, String> customerCodeColInv;

    @FXML
    private TableColumn<SalesReturn, String> customerCodeColRet;

    @FXML
    private DatePicker dateEncodedDatePicker;

    @FXML
    private Label docNo;

    @FXML
    private TableColumn<SalesInvoiceHeader, String> docNoColInv;

    @FXML
    private TableColumn<SalesInvoiceHeader, String> invoiceDateColInv;

    @FXML
    private TableColumn<SalesInvoiceHeader, String> invoiceNoColInv;

    @FXML
    private TableColumn<SalesInvoiceHeader, String> invoiceTypeColInv;

    @FXML
    private TableColumn<CustomerMemo, Date> memoDateColMem;

    @FXML
    private TableColumn<CustomerMemo, String> memoNumberColMem;

    @FXML
    private Tab memoTab;

    @FXML
    private TableView<CustomerMemo> memoTable;

    @FXML
    private TableColumn<CustomerMemo, String> memoTypeColMem;

    @FXML
    private Label paymentBalance;

    @FXML
    private TableColumn<CustomerMemo, String> pendingColMem;

    @FXML
    private Button postButton;

    @FXML
    private TableColumn<CustomerMemo, String> reasonColMem;

    @FXML
    private TextArea remarks;

    @FXML
    private TableColumn<SalesInvoiceHeader, String> remarksColInv;

    @FXML
    private TableColumn<SalesReturn, String> returnNoColRet;

    @FXML
    private Tab returnsTab;

    @FXML
    private TableView<SalesReturn> returnsTable;

    @FXML
    private Tab salesInvoiceTab;

    @FXML
    public TableView<SalesInvoiceHeader> salesInvoiceTable;

    @FXML
    private TextField salesmanNameTextField;

    @FXML
    private Button saveButton;

    @FXML
    private TableColumn<SalesInvoiceHeader, String> storeNameColInv;

    @FXML
    private TableColumn<CustomerMemo, String> storeNameColMem;

    @FXML
    private TableColumn<SalesReturn, String> storeNameColRet;

    @FXML
    private TableColumn<CustomerMemo, String> supplierColMem;

    @FXML
    private Label transactionBalance;

    @FXML
    private TabPane transactionTabPane;

    SalesmanDAO salesmanDAO = new SalesmanDAO();
    EmployeeDAO employeeDAO = new EmployeeDAO();

    @Getter
    Salesman salesman;
    User selectedEmployee;
    Collection collection;

    CollectionListController collectionListController;

    Stage parentStage;

    ObservableList<CollectionDetail> collectionDetails = FXCollections.observableArrayList();

    public void createCollection(Stage stage, int collectionNumber, CollectionListController collectionListController) {
        collection = new Collection();

        collection.setDocNo("CEX-" + collectionNumber);
        docNo.setText(collection.getDocNo());
        dateEncodedDatePicker.setValue(LocalDate.now());

        this.collectionListController = collectionListController;
        this.parentStage = stage;
        List<Salesman> salesmen = salesmanDAO.getAllSalesmen();
        List<String> salesmanNames = salesmen.stream().map(Salesman::getSalesmanName).toList();

        List<User> employees = employeeDAO.getAllEmployees();
        List<String> employeeNames = employees.stream()
                .map(e -> e.getUser_fname() + " " + e.getUser_lname())
                .toList();

        TextFields.bindAutoCompletion(salesmanNameTextField, salesmanNames);
        TextFields.bindAutoCompletion(collectorNameTextField, employeeNames);

        buttonBar.getButtons().remove(postButton);

        salesmanNameTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            salesman = salesmen.stream()
                    .filter(s -> s.getSalesmanName().equals(salesmanNameTextField.getText()))
                    .findFirst()
                    .orElse(null);
            if (salesman != null) {
                collection.setSalesman(salesman);
                collectorNameTextField.setText(salesman.getSalesmanName());
            }
        });
        collectorNameTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            selectedEmployee = employees.stream()
                    .filter(e -> (e.getUser_fname() + " " + e.getUser_lname()).equals(collectorNameTextField.getText()))
                    .findFirst()
                    .orElse(null);
            assert selectedEmployee != null;
            collection.setCollectedBy(selectedEmployee);
        });

        collectionDateDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                collection.setCollectionDate(Timestamp.valueOf(newValue.atStartOfDay()));
            }
        });

    }

    Stage salesInvoiceStage = null;

    private void openSalesInvoicePayment(SalesInvoiceHeader salesInvoiceHeader) {
        if (salesInvoiceHeader != null && salesInvoiceStage == null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("SalesInvoicePayment.fxml"));
                Parent root = loader.load();
                SalesInvoicePaymentController controller = loader.getController();
                salesInvoiceStage = new Stage();  // Create a new stage reference
                salesInvoiceStage.setTitle("Payment for " + salesInvoiceHeader.getOrderId() + " - " + salesInvoiceHeader.getInvoiceNo());
                salesInvoiceStage.setMaximized(false);
                salesInvoiceStage.setResizable(false);
                controller.settlePayment(salesInvoiceHeader, this);

                salesInvoiceStage.setScene(new Scene(root));
                salesInvoiceStage.show();

                // Reset reference when the stage is closed
                salesInvoiceStage.setOnCloseRequest(event -> salesInvoiceStage = null);

            } catch (IOException e) {
                DialogUtils.showErrorMessage("Error", "Unable to open collection.");
                e.printStackTrace();
            }
        }
    }

    ObservableList<SalesInvoiceHeader> deletedInvoices = FXCollections.observableArrayList();
    ObservableList<CollectionDetail> deletedCollectionDetails = FXCollections.observableArrayList();

    Stage adjustmentStage = null;

    private void openAdjustmentForm() {
        // Check if the payment form is already open
        if (adjustmentStage == null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("CollectionPaymentForm.fxml"));
                Parent root = loader.load();
                CollectionPaymentFormController controller = loader.getController();
                adjustmentStage = new Stage();  // Create a new stage reference
                adjustmentStage.setTitle("Collection Adjustment for " + collection.getDocNo());
                adjustmentStage.setMaximized(false);
                adjustmentStage.setResizable(false);
                controller.createNewAdjustment(parentStage, adjustmentStage, collection, this);

                adjustmentStage.setScene(new Scene(root));
                adjustmentStage.show();

                // Reset reference when the stage is closed
                adjustmentStage.setOnCloseRequest(event -> adjustmentStage = null);

            } catch (IOException e) {
                DialogUtils.showErrorMessage("Error", "Unable to open collection.");
                e.printStackTrace();
            }
        } else {
            // Bring the already opened stage to front if it's not null
            adjustmentStage.toFront();
        }
    }

    BankAccountDAO bankAccountDAO = new BankAccountDAO();
    Stage paymentStage = null; // A field to keep track of the open payment form stage

    ObservableList<BankName> bankNames = FXCollections.observableArrayList(bankAccountDAO.getBankNames());

    List<String> bankNamesList = bankNames.stream().map(BankName::getName).collect(Collectors.toList());

    ChartOfAccountsDAO coaDAO = new ChartOfAccountsDAO();

    ObservableList<ChartOfAccounts> chartOfAccounts = FXCollections.observableArrayList(coaDAO.getAllChartOfAccounts());

    List<String> chartOfAccountsNames = chartOfAccounts.stream().map(ChartOfAccounts::getAccountTitle).collect(Collectors.toList());


    private void openPaymentForm() {
        // Check if the payment form is already open
        if (paymentStage == null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("CollectionPaymentForm.fxml"));
                Parent root = loader.load();
                CollectionPaymentFormController controller = loader.getController();
                paymentStage = new Stage();  // Create a new stage reference
                paymentStage.setTitle("Collection Payment for " + collection.getDocNo());
                paymentStage.setMaximized(false);
                paymentStage.setResizable(false);
                controller.createNewCollectionPayment(parentStage, paymentStage, collection, this);

                paymentStage.setScene(new Scene(root));
                paymentStage.show();

                // Reset reference when the stage is closed
                paymentStage.setOnCloseRequest(event -> paymentStage = null);

            } catch (IOException e) {
                DialogUtils.showErrorMessage("Error", "Unable to open collection.");
                e.printStackTrace();
            }
        } else {
            // Bring the already opened stage to front if it's not null
            paymentStage.toFront();
        }
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        String cssPath = getClass().getResource("/com/vertex/vos/assets/table.css").toExternalForm();
        System.out.println("CSS File Path: " + cssPath);
        salesInvoiceTable.getStylesheets().add(cssPath);

        salesInvoiceTable.setItems(salesInvoices);
        collectionDetailsTableView.setItems(collectionDetails);
        memoTable.setItems(customerSupplierCreditDebitMemos);
        returnsTable.setItems(salesReturns);

        Platform.runLater(this::setUpHotKeys);

        salesmanNameTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            boolean disableButtons = (newValue == null || newValue.trim().isEmpty());
            addInvoiceButton.setDisable(disableButtons);
            addReturnsButton.setDisable(disableButtons);
            addPaymentButton.setDisable(disableButtons);
            addAdjustmentButton.setDisable(disableButtons);
        });

        // Table setup
        setUpInvoiceTable();
        setUpMemoTable();
        setUpReturnsTable();
        setUpCollectionDetailTable();
        setUpTableListeners();
        saveButton.setOnAction(event -> saveCollection());
    }

    private void setUpHotKeys() {
        parentStage.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.isControlDown() && event.getCode() == KeyCode.ENTER) {
                saveButton.fire();
            }
            if (event.isControlDown() && event.getCode() == KeyCode.DIGIT1) {
                addInvoiceButton.fire();
            }
            if (event.isControlDown() && event.getCode() == KeyCode.DIGIT2) {
                addReturnsButton.fire();
            }
        });

    }

    private void setUpReturnsTable() {
        returnNoColRet.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getReturnNumber()));
        storeNameColRet.setCellValueFactory(salesReturnStringCellDataFeatures -> new SimpleStringProperty(salesReturnStringCellDataFeatures.getValue().getCustomer().getStoreName()));
        customerCodeColRet.setCellValueFactory(salesReturnStringCellDataFeatures -> new SimpleStringProperty(salesReturnStringCellDataFeatures.getValue().getCustomer().getCustomerCode()));
        returnDateCol.setCellValueFactory(salesReturnTimestampCellDataFeatures -> new SimpleObjectProperty<>(salesReturnTimestampCellDataFeatures.getValue().getReturnDate()));
        returnRemarksCol.setCellValueFactory(salesReturnStringCellDataFeatures -> new SimpleStringProperty(salesReturnStringCellDataFeatures.getValue().getRemarks()));
        returnAmountCol.setCellValueFactory(salesReturnDoubleCellDataFeatures -> new SimpleObjectProperty<>(salesReturnDoubleCellDataFeatures.getValue().getTotalAmount()));
        returnsTable.setRowFactory(tv -> new TableRow<SalesReturn>() {
            @Override
            protected void updateItem(SalesReturn returnItem, boolean empty) {
                super.updateItem(returnItem, empty);

                if (empty || returnItem == null) {
                    setTooltip(null);
                } else {
                    Tooltip tooltip = new Tooltip();
                    if (collection.getSalesReturns().contains(returnItem)) {
                        tooltip.setText("Applied to CEX");
                    }
                    for (SalesInvoiceHeader salesInvoiceHeader : collection.getSalesInvoiceHeaders()) {
                        if (salesInvoiceHeader.getSalesReturns().contains(returnItem)) {
                            tooltip.setText("Applied to: " + salesInvoiceHeader.getInvoiceNo());
                        }
                    }
                    setTooltip(tooltip);
                }
            }
        });

    }

    private void setUpMemoTable() {
        memoTypeColMem.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getBalanceType().getBalanceName()));
        memoNumberColMem.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getMemoNumber()));
        supplierColMem.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getSupplier().getSupplierName()));
        storeNameColMem.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCustomer().getStoreName()));
        customeCodeColMem.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCustomer().getCustomerCode()));
        reasonColMem.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getReason()));
        pendingColMem.setCellValueFactory(cellDataFeatures ->
                new SimpleStringProperty(cellDataFeatures.getValue().getIsPending() ? "Yes" : "No"));

        memoDateColMem.setCellValueFactory(cellDataFeatures -> {
            CustomerMemo memo = cellDataFeatures.getValue();
            Timestamp date;

            if (!memo.getCollectionApplications().isEmpty()) {
                date = memo.getCollectionApplications().getFirst().getDateLinked();
            } else if (!memo.getInvoiceApplications().isEmpty()) {
                date = memo.getInvoiceApplications().getFirst().getDateApplied();
            } else {
                date = memo.getCreatedAt();
            }

            return new SimpleObjectProperty<>(date);
        });

        amountColMem.setCellValueFactory(cellDataFeatures -> {
            CustomerMemo memo = cellDataFeatures.getValue();
            double appliedAmount = 0.0;

            if (!memo.getCollectionApplications().isEmpty()) {
                appliedAmount = memo.getCollectionApplications().stream()
                        .mapToDouble(MemoCollectionApplication::getAmount)
                        .sum();
            } else if (!memo.getInvoiceApplications().isEmpty()) {
                appliedAmount = memo.getInvoiceApplications().stream()
                        .mapToDouble(MemoInvoiceApplication::getAmount)
                        .sum();
            }

            return new SimpleObjectProperty<>(appliedAmount);
        });

        // **Set Row Factory to Show Tooltip on Hover**
        memoTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(CustomerMemo memo, boolean empty) {
                super.updateItem(memo, empty);
                if (memo == null || empty) {
                    setTooltip(null);
                } else {
                    Tooltip tooltip = new Tooltip(getMemoApplicationDetails(memo));
                    setTooltip(tooltip);
                }
            }
        });
    }

    /**
     * Get Memo Application Details (Where the Memo was Applied)
     */
    private String getMemoApplicationDetails(CustomerMemo memo) {
        StringBuilder details = new StringBuilder("Applied To:\n");

        // Check Collection Applications (CEX)
        if (!memo.getCollectionApplications().isEmpty()) {
            details.append("• CEX\n");
        }

        // Check Invoice Applications
        for (MemoInvoiceApplication invoiceApp : memo.getInvoiceApplications()) {
            details.append("• Invoice No: ").append(invoiceApp.getSalesInvoiceHeader().getInvoiceNo()).append("\n");
        }

        return details.toString().trim();
    }


    private void setUpTableListeners() {
        salesInvoiceTable.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.DELETE) {
                deletedInvoices.add(salesInvoiceTable.getSelectionModel().getSelectedItem());
                salesInvoices.remove(salesInvoiceTable.getSelectionModel().getSelectedItem());
            }
        });

        salesInvoiceTable.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                SalesInvoiceHeader salesInvoiceHeader = salesInvoiceTable.getSelectionModel().getSelectedItem();
                openSalesInvoicePayment(salesInvoiceHeader);
            }
        });

        salesInvoiceTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                SalesInvoiceHeader salesInvoiceHeader = salesInvoiceTable.getSelectionModel().getSelectedItem();
                openSalesInvoicePayment(salesInvoiceHeader);
            }
        });

        salesInvoiceTable.setContextMenu(new ContextMenu());

        MenuItem generateAdjustment = new MenuItem("Generate Adjustment");
        generateAdjustment.setOnAction(event -> {
            SalesInvoiceHeader selectedInvoice = salesInvoiceTable.getSelectionModel().getSelectedItem();
            if (selectedInvoice != null) {
                openAdjustmentForm(collection, selectedInvoice);
            }
        });
        salesInvoiceTable.getContextMenu().getItems().add(generateAdjustment);

        collectionDetailsTableView.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.DELETE) {
                deletedCollectionDetails.add(collectionDetailsTableView.getSelectionModel().getSelectedItem());
                collectionDetails.remove(collectionDetailsTableView.getSelectionModel().getSelectedItem());
            }
        });

        collectionDetailsTableView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                openCollectionDetails(collectionDetailsTableView.getSelectionModel().getSelectedItem());
            }
        });
    }

    private void openAdjustmentForm(Collection collection, SalesInvoiceHeader selectedInvoice) {
        // Check if the payment form is already open
        if (adjustmentStage == null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("CollectionPaymentForm.fxml"));
                Parent root = loader.load();
                CollectionPaymentFormController controller = loader.getController();
                adjustmentStage = new Stage();  // Create a new stage reference
                adjustmentStage.setTitle("Collection Adjustment for " + collection.getDocNo());
                adjustmentStage.setMaximized(false);
                adjustmentStage.setResizable(false);
                controller.createNewAdjustment(parentStage, adjustmentStage, collection, this);
                controller.setDataForInvoiceAdjustment(selectedInvoice);

                adjustmentStage.setScene(new Scene(root));
                adjustmentStage.show();

                // Reset reference when the stage is closed
                adjustmentStage.setOnCloseRequest(event -> adjustmentStage = null);

            } catch (IOException e) {
                DialogUtils.showErrorMessage("Error", "Unable to open collection.");
                e.printStackTrace();
            }
        } else {
            // Bring the already opened stage to front if it's not null
            adjustmentStage.toFront();
        }
    }

    Stage collectionDetailStage;

    private void openCollectionDetails(CollectionDetail selectedItem) {
        if (collectionDetailStage != null) {
            collectionDetailStage.close();
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("CollectionPaymentForm.fxml"));
            Parent root = loader.load();
            CollectionPaymentFormController controller = loader.getController();
            controller.setCollectionDetail(selectedItem, this);
            collectionDetailStage = new Stage();
            collectionDetailStage.setTitle("Collection Detail");
            collectionDetailStage.setMaximized(false);
            collectionDetailStage.setResizable(false);
            collectionDetailStage.setScene(new Scene(root));
            collectionDetailStage.show();
            collectionDetailStage.setOnCloseRequest(event -> collectionDetailStage = null);
            controller.setStage(collectionDetailStage);

        } catch (IOException e) {
            DialogUtils.showErrorMessage("Error", "Unable to open collection detail.");
            e.printStackTrace();
        }
    }

    CollectionDAO collectionDAO = new CollectionDAO();

    ObservableList<MemoCollectionApplication> deletedMemo = FXCollections.observableArrayList();
    ObservableList<SalesReturn> deletedReturns = FXCollections.observableArrayList();

    private void saveCollection() {
        collection.setSalesman(salesman);
        collection.setCollectedBy(selectedEmployee);
        collection.setDateEncoded(Timestamp.valueOf(dateEncodedDatePicker.getValue().atStartOfDay()));
        collection.setCollectionDate(Timestamp.valueOf(collectionDateDatePicker.getValue().atStartOfDay()));
        collection.setTotalAmount(calculateCollectedAmount());
        collection.setRemarks(remarks.getText());
        collection.setIsPosted(false);
        collection.setIsCancelled(false);
        collection.setEncoderId(collection.getEncoderId() == null ? UserSession.getInstance().getUser() : collection.getEncoderId());
        collection.setSalesInvoiceHeaders(salesInvoiceTable.getItems());
        collection.setSalesReturns(collection.getSalesReturns());
        collection.setCollectionDetails(collectionDetailsTableView.getItems());
        updateLabelAmounts();
        try {
            if (collectionDAO.insertCollection(collection, deletedCollectionDetails, deletedInvoices, deletedReturns, deletedMemo)) {
                DialogUtils.showCompletionDialog("Success", "Collection saved successfully.");
                parentStage.close();
                collectionListController.loadCollections();
            } else {
                DialogUtils.showErrorMessage("Error", "Failed to save collection.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private double calculateAdjustmentTotalAmount() {
        double totalAmount = 0;

        if (collection.getCollectionDetails() != null) {
            for (CollectionDetail detail : collection.getCollectionDetails()) {
                if (!detail.isPayment()) {
                    if (detail.getBalanceType() != null) {
                        // Add amount if BalanceType is 1
                        if (detail.getBalanceType().getId() == 1) {
                            totalAmount -= detail.getAmount();
                        }
                        // Subtract amount if BalanceType is 2
                        else if (detail.getBalanceType().getId() == 2) {
                            totalAmount += detail.getAmount();
                        }
                    }
                }
            }
        }

        return totalAmount;
    }


    private double calculateTotalBalance() {
        return calculatePayablesTotalAmount() - calculateCollectedAmount() - calculateAdjustmentTotalAmount();
    }

    private double calculateCollectedAmount() {
        double totalAmount = 0;

        if (collection.getCollectionDetails() != null) {
            for (CollectionDetail detail : collection.getCollectionDetails()) {
                if (detail.isPayment()) {
                    if (detail.getBalanceType() != null) {
                        // Add amount if BalanceType is 1
                        if (detail.getBalanceType().getId() == 1) {
                            totalAmount += detail.getAmount();
                        }
                        // Subtract amount if BalanceType is 2
                        else if (detail.getBalanceType().getId() == 2) {
                            totalAmount -= detail.getAmount();
                        }
                    }
                }
            }
        }

        return totalAmount;
    }


    private double calculatePayablesTotalAmount() {
        double totalAmount = 0;

        // Add Sales Invoice Amounts
        if (collection.getSalesInvoiceHeaders() != null) {
            for (SalesInvoiceHeader invoice : collection.getSalesInvoiceHeaders()) {
                totalAmount += invoice.getTotalAmount();
            }
        }

        if (customerSupplierCreditDebitMemos != null) {
            for (CustomerMemo customerMemo : customerSupplierCreditDebitMemos) {
                if (!customerMemo.getCollectionApplications().isEmpty()) {
                    if (customerMemo.getBalanceType().getId() == 1) {
                        totalAmount -= customerMemo.getCollectionApplications().stream()
                                .mapToDouble(MemoCollectionApplication::getAmount)
                                .sum();
                    } else {
                        totalAmount += customerMemo.getCollectionApplications().stream().mapToDouble(MemoCollectionApplication::getAmount).sum();
                    }
                }
            }
        }
        // Subtract Sales Returns
        if (collection.getSalesReturns() != null) {
            for (SalesReturn salesReturn : collection.getSalesReturns()) {
                totalAmount -= salesReturn.getTotalAmount();
            }
        }

        return totalAmount;
    }

    public void updateLabelAmounts() {
        double totalAmount = calculatePayablesTotalAmount();
        double paidAmount = calculatePaidAmount();
        double adjustmentAmount = calculateAdjustmentTotalAmount();
        double totalCollectedAmount = calculateCollectedAmount();
        double collectionTotalAmount = calculateCollectionBalance();
        double balance = calculateTotalBalance();
        // Define Philippine Peso (₱) format with 4 decimal places
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("en", "PH"));
        symbols.setCurrencySymbol("₱");
        symbols.setGroupingSeparator(',');
        symbols.setDecimalSeparator('.');

        DecimalFormat pesoFormat = new DecimalFormat("₱#,##0.0000", symbols);

        transactionBalance.setText(pesoFormat.format(totalAmount));
        paymentBalance.setText(pesoFormat.format(totalCollectedAmount));
        totalAdjustmentAmount.setText(pesoFormat.format(adjustmentAmount));
        paidBalance.setText(pesoFormat.format(paidAmount));
        collectionAmount.setText(pesoFormat.format(collectionTotalAmount));
        collectionBalance.setText(pesoFormat.format(balance));
    }

    private double calculateCollectionBalance() {
        return (calculateCollectedAmount() + calculateAdjustmentTotalAmount());
    }


    private double calculatePaidAmount() {
        return salesInvoiceTable.getItems().stream()
                .flatMap(salesInvoice -> salesInvoice.getSalesInvoicePayments().stream())
                .mapToDouble(SalesInvoicePayment::getPaidAmount)
                .sum();
    }

    ObservableList<SalesInvoiceHeader> salesInvoices = FXCollections.observableArrayList();
    ObservableList<SalesReturn> salesReturns = FXCollections.observableArrayList();
    ObservableList<CustomerMemo> customerSupplierCreditDebitMemos = FXCollections.observableArrayList();

    private void setUpInvoiceTable() {
        TableViewFormatter.formatTableView(salesInvoiceTable);

        docNoColInv.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getOrderId()));
        invoiceNoColInv.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getInvoiceNo()));
        storeNameColInv.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCustomer().getStoreName()));
        customerCodeColInv.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCustomer().getCustomerCode()));

        invoiceDateColInv.setCellValueFactory(cellData -> {
            Date date = new Date(cellData.getValue().getInvoiceDate().getTime());
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            return new SimpleStringProperty(dateFormat.format(date));
        });

        invoiceTypeColInv.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getInvoiceType().getShortcut()));
        remarksColInv.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getRemarks()));
        amountColInv.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getTotalAmount()));

        amountColInv.setCellFactory(tc -> new TableCell<SalesInvoiceHeader, Double>() {
            private final StringConverter<Double> converter = new StringConverter<Double>() {
                @Override
                public String toString(Double object) {
                    return String.format("%.2f", object);
                }

                @Override
                public Double fromString(String string) {
                    return Double.parseDouble(string);
                }
            };

            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(converter.toString(item));
                }
            }
        });
        paidAmountInvCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(String.format("%.2f", cellData.getValue().getSalesInvoicePayments().stream()
                .mapToDouble(SalesInvoicePayment::getPaidAmount)
                .sum())));

        // ✅ Set row color based on balance
        salesInvoiceTable.setRowFactory(tv -> new TableRow<SalesInvoiceHeader>() {
            @Override
            protected void updateItem(SalesInvoiceHeader item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    setStyle(""); // Reset style if row is empty
                    return;
                }

                BigDecimal totalAmount = BigDecimal.valueOf(item.getTotalAmount()).setScale(2, RoundingMode.HALF_UP);
                BigDecimal paidAmount = item.getSalesInvoicePayments().stream()
                        .map(payment -> BigDecimal.valueOf(payment.getPaidAmount()).setScale(2, RoundingMode.HALF_UP))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal balance = totalAmount.subtract(paidAmount).setScale(2, RoundingMode.HALF_UP);

                BigDecimal disbalanceThreshold = new BigDecimal("1.0");

                BigDecimal tolerance = new BigDecimal("0.0001"); // Small tolerance

                // If balance is greater than or equal to 1, or less than or equal to -1, apply styles
                if (balance.abs().compareTo(disbalanceThreshold) >= 0) {
                    if (balance.compareTo(BigDecimal.ZERO) > 0) {
                        setStyle("-fx-background-color: #D3212C; -fx-text-fill: white;"); // Unpaid (RED)
                    } else if (balance.compareTo(BigDecimal.ZERO) == 0) {
                        setStyle("-fx-background-color: #069C56; -fx-text-fill: white;"); // Fully Paid (GREEN)
                    } else {
                        setStyle("-fx-background-color: #FF680E; -fx-text-fill: black;"); // Overpaid (ORANGE)
                    }
                }
            }
        });

        salesInvoiceTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        addInvoiceButton.setOnAction(event -> openInvoicesSelection());
    }


    private Stage invoiceStage;

    private void openInvoicesSelection() {
        if (invoiceStage == null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("SalesInvoices.fxml"));
                Parent root = loader.load();
                SalesInvoicesController controller = loader.getController();
                controller.openInvoicesSelection(parentStage, this);

                invoiceStage = new Stage();
                invoiceStage.setTitle("Invoice Selection");
                invoiceStage.setScene(new Scene(root));
                invoiceStage.initOwner(parentStage);
                invoiceStage.show();

                // Reset reference when the stage is closed
                invoiceStage.setOnCloseRequest(event -> invoiceStage = null);

            } catch (IOException e) {
                DialogUtils.showErrorMessage("Error", "Unable to open invoice form.");
                e.printStackTrace();
            }
        } else {
            invoiceStage.toFront();
        }
    }

    private void setUpCollectionDetailTable() {
        TableViewFormatter.formatTableView(collectionDetailsTableView);
        typeCollectionDetailCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getType().getAccountTitle()));

        bankCollectionDetailCol.setCellValueFactory(cellData -> {
            String typeTitle = cellData.getValue().getType().getAccountTitle();
            return switch (typeTitle) {
                case "Post Dated Check", "Dated Check", "Cash in Bank" ->
                        new SimpleStringProperty(cellData.getValue().getBank().getName());
                default -> new SimpleStringProperty(null);
            };
        });

        customerCollectionCol.setCellValueFactory(cellData -> {
            Customer customer = cellData.getValue().getCustomer();
            String storeName = (customer != null) ? customer.getStoreName() : ""; // Return empty string if null
            return new SimpleStringProperty(storeName);
        });


        checkNoCollectionDetailCol.setCellValueFactory(cellData -> {
            String typeTitle = cellData.getValue().getType().getAccountTitle();
            return switch (typeTitle) {
                case "Post Dated Check", "Dated Check", "Cash in Bank" ->
                        new SimpleStringProperty(cellData.getValue().getCheckNo());
                default -> new SimpleObjectProperty<>(null);
            };
        });

        checkDateCollectionDetailCol.setCellValueFactory(cellData -> {
            String typeTitle = cellData.getValue().getType().getAccountTitle();
            return switch (typeTitle) {
                case "Post Dated Check", "Dated Check", "Cash in Bank" ->
                        new SimpleObjectProperty<>(cellData.getValue().getCheckDate());
                default -> new SimpleObjectProperty<>(null);
            };
        });

        amountCollectionDetailCol.setCellValueFactory(cellData -> {
            CollectionDetail detail = cellData.getValue();
            if (detail == null) {
                return new SimpleDoubleProperty(0.0).asObject();
            }

            double collectionAmount = detail.getAmount();
            int balanceTypeId = detail.getBalanceType().getId();

            if (balanceTypeId == 1) {
                collectionAmount = Math.abs(collectionAmount);
            } else if (balanceTypeId == 2) {
                collectionAmount = -Math.abs(collectionAmount);
            }

            return new SimpleDoubleProperty(collectionAmount).asObject();
        });
        remarksCollectionDetailCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getRemarks()));

        addPaymentButton.setOnAction(event -> {
            openPaymentForm();
        });

        addAdjustmentButton.setOnAction(event -> {
            openAdjustmentForm();
        });
        addReturnsButton.setOnAction(mouseEvent -> openReturnForm());
    }

    Stage returnStage;

    private void openReturnForm() {
        if (returnStage == null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("SalesReturns.fxml"));
                Parent root = loader.load();
                SalesReturnsListController controller = loader.getController();
                controller.openReturnSelection(parentStage, this);

                returnStage = new Stage();
                returnStage.setTitle("Returns Selection");
                returnStage.setScene(new Scene(root));
                returnStage.initOwner(parentStage);
                returnStage.show();

                // Reset reference when the stage is closed
                returnStage.setOnCloseRequest(event -> returnStage = null);

            } catch (IOException e) {
                DialogUtils.showErrorMessage("Error", "Unable to open return list.");
                e.printStackTrace();
            }
        }
    }


    public LocalDate getCollectionDate() {
        return collectionDateDatePicker.getValue();
    }

    public void editCollection(Stage stage, Collection collection, CollectionListController
            collectionListController) {
        this.parentStage = stage;
        this.collection = collection;
        this.collectionListController = collectionListController;
        salesInvoices.setAll(collection.getSalesInvoiceHeaders());
        collectionDetails.setAll(collection.getCollectionDetails());
        collectionDateDatePicker.setValue(collection.getCollectionDate().toLocalDateTime().toLocalDate());
        salesman = collection.getSalesman();
        selectedEmployee = collection.getCollectedBy();

        if (!collection.getCustomerMemos().isEmpty()) {
            for (MemoCollectionApplication collectionMemos : collection.getCustomerMemos()) {
                collectionMemos.getCustomerMemo().getCollectionApplications().add(collectionMemos);
                customerSupplierCreditDebitMemos.add(collectionMemos.getCustomerMemo());
            }
        }
        if (collection.getIsPosted()) {
            collectionPane.setDisable(true);
        }


        if (!collection.getSalesReturns().isEmpty()) {
            salesReturns.addAll(collection.getSalesReturns());
        }

        for (SalesInvoiceHeader salesInvoiceHeader : collection.getSalesInvoiceHeaders()) {
            for (MemoInvoiceApplication invoiceMemos : salesInvoiceHeader.getCustomerMemos()) {
                invoiceMemos.getCustomerMemo().getInvoiceApplications().add(invoiceMemos);
                customerSupplierCreditDebitMemos.add(invoiceMemos.getCustomerMemo());
                double totalAmount = salesInvoiceHeader.getTotalAmount();
                double memoAmount = invoiceMemos.getAmount();
                if (invoiceMemos.getCustomerMemo().getBalanceType().getId() == 1) {
                    salesInvoiceHeader.setTotalAmount(totalAmount - memoAmount);
                } else if (invoiceMemos.getCustomerMemo().getBalanceType().getId() == 2) {
                    salesInvoiceHeader.setTotalAmount(totalAmount + memoAmount);
                }
                Logger.getLogger(CollectionFormController.class.getName()).log(Level.INFO, "Added memo amount {0} to invoice {1}", new Object[]{memoAmount, salesInvoiceHeader.getInvoiceNo()});
            }

            for (SalesReturn salesReturn : salesInvoiceHeader.getSalesReturns()) {
                salesInvoiceHeader.setTotalAmount(salesInvoiceHeader.getTotalAmount() - salesReturn.getTotalAmount());
                salesReturns.add(salesReturn);
            }
            updateLabelAmounts();
        }
        salesmanNameTextField.setText(salesman.getSalesmanName());
        collectorNameTextField.setText(selectedEmployee.getUser_fname() + " " + selectedEmployee.getUser_lname());
        remarks.setText(collection.getRemarks());
        dateEncodedDatePicker.setValue(collection.getDateEncoded().toLocalDateTime().toLocalDate());
        docNo.setText(collection.getDocNo());

        postButton.setOnAction(mouseEvent -> {
            checkCollectionForPosting();
        });
    }

    private void checkCollectionForPosting() {
        double collectionBalance = calculateTotalBalance();

        collectionBalance = Math.round(collectionBalance * 10000.0) / 10000.0;

        if (collectionBalance != 0) {
            DialogUtils.showErrorMessage("Error", "You still have " + collectionBalance + " collection balance");
            return;
        }

        for (CustomerMemo customerMemo : customerSupplierCreditDebitMemos) {
            if (customerMemo.getIsPending()) {
                DialogUtils.showErrorMessage("Error", customerMemo.getMemoNumber() + " is still pending");
                return;
            }
        }

        boolean confirmed = DialogUtils.showConfirmationDialog("Confirmation", "Are you sure you want to post this collection?");
        if (confirmed) {
            postCollection();
        }
        else {
            return;
        }
    }

    private void postCollection() {
        boolean collectionPosted = collectionDAO.postCollection(collection);
        if (collectionPosted) {
            DialogUtils.showCompletionDialog("Success", "Collection has been posted.");
            collectionListController.loadCollections();
            collectionPane.setDisable(true);
        }
    }

    public void addCollectionReturnToReturnTable() {
        salesReturns.addAll(collection.getSalesReturns());
        updateLabelAmounts();
    }
}
