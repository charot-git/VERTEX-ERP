package com.vertex.vos;

import com.vertex.vos.Objects.*;
import com.vertex.vos.Utilities.DialogUtils;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import lombok.Setter;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class SalesOrderConversionFormController implements Initializable {

    public TabPane tabPane;
    @FXML
    private Label orderNo;
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
    private DatePicker dateCreatedField;
    @FXML
    private DatePicker deliveryDateField;
    @FXML
    private DatePicker dueDateField;
    @FXML
    private DatePicker orderDateField;
    @FXML
    private Button addSales;
    @FXML
    private Button convertButton;
    @FXML
    private ComboBox<SalesInvoiceType> invoiceField;
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
    private TitledPane tiltedPane;
    @FXML
    private ButtonBar buttonBar;
    @FXML
    private TextField productNameFilter;

    @Setter
    SalesOrderListController salesOrderListController;

    SalesOrder salesOrder;

    ObservableList<SalesOrderDetails> salesOrderDetails = FXCollections.observableArrayList();
    ObservableList<SalesInvoiceDetail> salesInvoiceDetails = FXCollections.observableArrayList();

    public void openSalesOrder(SalesOrder selectedItem) {
        this.salesOrder = selectedItem;
        salesOrderDetails.addAll(salesOrder.getSalesOrderDetails());
        tiltedPane.setText("Sales Order Products: " + salesOrder.getSalesOrderDetails().size());
        int availableSize = salesInvoiceDetails.size() - salesOrderDetails.size();
        addSales.setText("Add Sales " + "(" + availableSize + ")");
        buttonBar.getButtons().add(addSales);
        addSales.setOnAction(actionEvent -> {
            addSalesInvoice();
        });

        productNameFilter.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                // Handle empty input (e.g., reset table data)
                salesOrderTableView.setItems(salesOrderDetails);
            } else {
                // Apply filtering based on the input text
                FilteredList<SalesOrderDetails> filteredList = new FilteredList<>(salesOrderDetails, product ->
                        product.getProduct().getProductName().toLowerCase().contains(newValue.toLowerCase())
                );
                salesOrderTableView.setItems(filteredList);
            }
        });
    }


    private void addSalesInvoice() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("SalesInvoiceTemporary.fxml"));
            Parent root = loader.load();
            SalesInvoiceTemporaryController salesInvoiceTemporaryController = loader.getController();
            salesInvoiceTemporaryController.setInitialDataForSalesOrder(salesOrder, salesOrderDetails, this);
            Tab tab = new Tab("New Sales Transaction");
            tab.setContent(root);
            tabPane.getTabs().add(tab);
            tabPane.getSelectionModel().select(tab);
        } catch (IOException e) {
            DialogUtils.showErrorMessage("Error", "Unable to open sales invoice creation.");
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableView();
        buttonBar.getButtons().removeAll(buttonBar.getButtons());
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
}
