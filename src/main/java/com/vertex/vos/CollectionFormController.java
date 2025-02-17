package com.vertex.vos;

import com.vertex.vos.Objects.*;
import com.vertex.vos.Utilities.DialogUtils;
import com.vertex.vos.Utilities.EmployeeDAO;
import com.vertex.vos.Utilities.SalesmanDAO;
import com.vertex.vos.Utilities.TableViewFormatter;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
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
import javafx.stage.Stage;
import lombok.Getter;
import org.controlsfx.control.textfield.TextFields;

import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

public class CollectionFormController implements Initializable {

    public ButtonBar buttonBar;
    public TableColumn<CollectionDetail, String> typeCollectionDetailCol;
    public TableColumn<CollectionDetail, String> bankCollectionDetailCol;
    public TableColumn<CollectionDetail, String> checkNoCollectionDetailCol;
    public TableColumn<CollectionDetail, Timestamp> checkDateCollectionDetailCol;
    public TableColumn<CollectionDetail, Double> amountCollectionDetailCol;
    public TableColumn<CollectionDetail, String> remarksCollectionDetailCol;

    public TableColumn<SalesInvoiceHeader, String> paidAmountInvCol;
    @FXML
    private Button addAdjustmentButton;

    @FXML
    private Button addInvoiceButton;

    @FXML
    private Button addMemoButton;

    @FXML
    private Button addPaymentButton;

    @FXML
    private Button addReturnsButton;

    @FXML
    private TableColumn<SalesInvoiceHeader, Double> amountColInv;

    @FXML
    private TableColumn<?, ?> amountColMem;

    @FXML
    private DatePicker collectionDateDatePicker;

    @FXML
    private TableView<CollectionDetail> collectionDetailsTableView;

    @FXML
    private TextField collectorNameTextField;

    @FXML
    private TableColumn<?, ?> customeCodeColMem;

    @FXML
    private TableColumn<SalesInvoiceHeader, String> customerCodeColInv;

    @FXML
    private TableColumn<?, ?> customerCodeColRet;

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
    private TableColumn<?, ?> memoDateColMem;

    @FXML
    private TableColumn<?, ?> memoNumberColMem;

    @FXML
    private Tab memoTab;

    @FXML
    private TableView<?> memoTable;

    @FXML
    private TableColumn<?, ?> memoTypeColMem;

    @FXML
    private Label paymentBalance;

    @FXML
    private TableColumn<?, ?> pendingColMem;

    @FXML
    private Button postButton;

    @FXML
    private TableColumn<?, ?> reasonColMem;

    @FXML
    private TextArea remarks;

    @FXML
    private TableColumn<SalesInvoiceHeader, String> remarksColInv;

    @FXML
    private TableColumn<SalesInvoiceHeader, String> returnNoColRet;

    @FXML
    private Tab returnsTab;

    @FXML
    private TableView<?> returnsTable;

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
    private TableColumn<SalesInvoiceHeader, String> storeNameColMem;

    @FXML
    private TableColumn<SalesInvoiceHeader, String> storeNameColRet;

    @FXML
    private TableColumn<SalesInvoiceHeader, String> supplierColMem;

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
                collection.setSalesmanId(salesman);
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

        addPaymentButton.setOnMouseClicked(event -> {
            openPaymentForm();
        });

        addAdjustmentButton.setOnMouseClicked(event -> {
            openAdjustmentForm();
        });
    }

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

    Stage paymentStage = null; // A field to keep track of the open payment form stage

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
        setUpInvoiceTable();
        setUpCollectionDetailTable();

        salesmanNameTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            boolean disableButtons = newValue.isEmpty();
            addInvoiceButton.setDisable(disableButtons);
            addReturnsButton.setDisable(disableButtons);
            addPaymentButton.setDisable(disableButtons);
            addAdjustmentButton.setDisable(disableButtons);
            addMemoButton.setDisable(disableButtons);
        });
    }

    ObservableList<SalesInvoiceHeader> salesInvoices = FXCollections.observableArrayList();

    private void setUpInvoiceTable() {
        TableViewFormatter.formatTableView(salesInvoiceTable);
        salesInvoiceTable.setItems(salesInvoices);
        invoiceTypeColInv.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getInvoiceType().getShortcut()));
        docNoColInv.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getOrderId()));
        invoiceNoColInv.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getInvoiceNo()));
        storeNameColInv.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCustomer().getStoreName()));
        customerCodeColInv.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCustomer().getCustomerCode()));
        invoiceDateColInv.setCellValueFactory(cellData -> {
            Date date = new Date(cellData.getValue().getInvoiceDate().getTime());
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            return new SimpleStringProperty(dateFormat.format(date));
        });        remarksColInv.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getRemarks()));
        amountColInv.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getTotalAmount()).asObject());
        paidAmountInvCol.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getSalesInvoicePayments().stream()
                .mapToDouble(SalesInvoicePayment::getPaidAmount)
                .sum())));

        addInvoiceButton.setOnMouseClicked(event -> {
            openInvoicesSelection();
        });

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
        collectionDetailsTableView.setItems(collectionDetails);

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

            double amount = detail.getAmount();
            return new SimpleDoubleProperty(detail.isPayment() ? amount : -amount).asObject();
        });

        remarksCollectionDetailCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getRemarks()));
    }


    public LocalDate getCollectionDate() {
        return collectionDateDatePicker.getValue();
    }
}
