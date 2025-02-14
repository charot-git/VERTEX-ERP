package com.vertex.vos;

import com.vertex.vos.DAO.SalesInvoiceDAO;
import com.vertex.vos.Objects.SalesInvoiceHeader;
import com.vertex.vos.Utilities.DialogUtils;
import com.vertex.vos.Utilities.SalesmanDAO;
import com.vertex.vos.Utilities.ToDoAlert;
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
import lombok.Setter;
import org.controlsfx.control.textfield.TextFields;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class SalesInvoicesController implements Initializable {
    public TextField salesInvoiceNumberFilter;
    public Button addButton;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setUpTable();

    }

    private void setUpSelection() {
        //tranverse table and if enter key press, open sales invoice
        salesInvoiceTable.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
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
            stage.setTitle("Order#" + salesInvoiceHeader.getOrderId() + " - " + salesInvoiceHeader.getCustomerName());
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
    private ComboBox<String> customerFilter;

    @FXML
    public TableView<SalesInvoiceHeader> salesInvoiceTable;

    @FXML
    private ComboBox<String> salesTypeFilter;

    @FXML
    private ComboBox<String> salesmanFilter;
    SalesmanDAO salesmanDAO = new SalesmanDAO();

    @Setter
    AnchorPane contentPane;
    ObservableList<SalesInvoiceHeader> salesInvoices = FXCollections.observableArrayList();

    public void loadSalesInvoices() {
        salesmanFilter.setItems(salesmanDAO.getAllSalesmanNames());

        addButton.setDefaultButton(true);
        List<String> invoiceNumbers = salesInvoiceDAO.getAllInvoiceNumbers();
        TextFields.bindAutoCompletion(salesInvoiceNumberFilter, invoiceNumbers);
        salesInvoices.setAll(salesInvoiceDAO.loadSalesInvoices());
        setUpSelection();
        addButton.setOnAction(event -> addNewSalesInvoice());

        salesInvoiceNumberFilter.textProperty().addListener((observable, oldValue, newValue) -> {
            salesInvoiceTable.getItems().clear();
            salesInvoiceTable.getItems().setAll(salesInvoiceDAO.loadSalesInvoicesByInvoiceNo(newValue));
        });

        salesInvoiceTable.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                SalesInvoiceHeader salesInvoiceHeader = salesInvoiceTable.getSelectionModel().getSelectedItem();
                if (salesInvoiceHeader != null) {
                    openSalesInvoice(salesInvoiceHeader);
                }
            }
            if (event.getCode() == KeyCode.DELETE) {
                SalesInvoiceHeader salesInvoiceHeader = salesInvoiceTable.getSelectionModel().getSelectedItem();
                if (salesInvoiceHeader != null) {
                    ToDoAlert.showToDoAlert();
                }
            }
        });
        salesInvoiceTable.setOnMousePressed(event -> {
            SalesInvoiceHeader salesInvoiceHeader = salesInvoiceTable.getSelectionModel().getSelectedItem();
            if (event.getClickCount() == 2 && salesInvoiceHeader != null) {
                openSalesInvoice(salesInvoiceHeader);
            }
        });

        salesmanFilter.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            salesInvoices.setAll(salesInvoiceDAO.loadSalesInvoicesBySalesmanName(salesmanDAO.getSalesmanIdBySalesmanName(newValue)));
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
        salesmanFilter.setValue(collectionFormController.salesman.getSalesmanName());
        salesmanFilter.setEditable(false);

        List<String> customerNames = salesInvoiceDAO.getAllCustomerNamesForUnpaidInvoicesOfSalesman(collectionFormController.getSalesman());
        customerFilter.getItems().addAll(customerNames);

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
