package com.vertex.vos;

import com.vertex.vos.DAO.SalesInvoiceDAO;
import com.vertex.vos.Objects.SalesInvoiceHeader;
import com.vertex.vos.Utilities.DialogUtils;
import com.vertex.vos.Utilities.ToDoAlert;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
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

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Timestamp;
import java.util.Date;
import java.util.ResourceBundle;

public class SalesInvoicesController implements Initializable {
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setUpTable();
        setUpTableData();
        setUpSelection();
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
            stage.setMaximized(true);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            DialogUtils.showErrorMessage("Error", "Unable to open document.");
            e.printStackTrace();
        }

    }

    SalesInvoiceDAO salesInvoiceDAO = new SalesInvoiceDAO();

    public void setUpTableData() {
        salesInvoices.setAll(salesInvoiceDAO.loadSalesInvoices());
    }

    private void setUpTable() {
        salesInvoiceTable.setItems(salesInvoices);
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

        salesmanNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getSalesman().getSalesmanName()));
        statusColumn.getColumns().addAll(transactionStatus, paymentStatusColumn);
        customerColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCustomer().getStoreName()));
        paymentStatusColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPaymentStatus()));
        transactionStatus.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTransactionStatus()));
        createdDateColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCreatedDate().toString()));
        totalAmount.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getTotalAmount()).asObject());

        salesInvoiceTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        salesInvoiceTable.getColumns().addAll(orderNoColumn, salesmanNameColumn, customerColumn, invoiceType, createdDateColumn, totalAmount, statusColumn);

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
    }

    @FXML
    private ComboBox<String> customerFilter;

    @FXML
    private TableView<SalesInvoiceHeader> salesInvoiceTable;

    @FXML
    private ComboBox<String> salesTypeFilter;

    @FXML
    private ComboBox<String> salesmanFilter;
    @Setter
    AnchorPane contentPane;
    ObservableList<SalesInvoiceHeader> salesInvoices = FXCollections.observableArrayList();

}
