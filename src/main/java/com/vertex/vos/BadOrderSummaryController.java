package com.vertex.vos;

import com.vertex.vos.DAO.SalesReturnDAO;
import com.vertex.vos.Objects.*;
import com.vertex.vos.Utilities.SalesmanDAO;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import org.controlsfx.control.textfield.TextFields;

import java.net.URL;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

public class BadOrderSummaryController implements Initializable {

    public TableView<SalesReturnDetail> boSummaryTable;
    public TableColumn<SalesReturnDetail, String> productNameCol;
    public TableColumn<SalesReturnDetail, String> productUnitCol;
    public TableColumn<SalesReturnDetail, Timestamp> dateOfTransactionCol;
    public TableColumn<SalesReturnDetail, String> documentNoCol;
    public TableColumn<SalesReturnDetail, String> customerNameCol;
    public TableColumn<SalesReturnDetail, Integer> quantityCol;
    public TableColumn<SalesReturnDetail, String> productNameSummaryCol;
    public TableColumn<SalesReturnDetail, String> productUnitSummaryCol;
    public TableColumn<SalesReturnDetail, Integer> sumQuantityCol;
    public TableView<SalesReturnDetail> boSummarySumTable;
    public Label salesmanNameHeader;
    public DatePicker dateFromChecking;
    public DatePicker dateToChecking;
    public TableView<SalesReturnDetail> badProductSummaryPerProductTableView;
    public TableColumn<SalesReturnDetail, String> badProductSummaryPerProductNameCol;
    public TableColumn<SalesReturnDetail, String> badProductSummaryPerProductUnitCol;
    public TableColumn<SalesReturnDetail, Integer> badProductSummaryPerProductQuantityCol;
    public TableView<StockTransfer> badProductTransferEntriesTableView;
    public TableColumn<StockTransfer, String> badProductTransferEntriesNameCol;
    public TableColumn<StockTransfer, String> badProductTransferEntriesUnitCol;
    public TableColumn<StockTransfer, Integer> badProductTransferEntriesQuantityCol;
    public TableView<Inventory> badProductInventoryTableView;
    public TableColumn<Inventory, String> badProductInventoryNameCol;
    public TableColumn<Inventory, String> badProductInventoryUnitCol;
    public TableColumn<Inventory, Integer> badProductInventoryQuantityCol;
    public TableColumn<Inventory, String> badProductInventoryFindingsCol;
    public TableView<String> findingsTableView;
    public TableColumn<String, Integer> varianceCol;
    public TableColumn<String, String> statusCol;
    @FXML
    private Tab badOrderCheckingTab;

    @FXML
    private Tab badOrderSummaryTab;

    @FXML
    private BorderPane boCheckingBorderPane;

    @FXML
    private DatePicker dateFromFilter;

    @FXML
    private DatePicker dateToFilter;

    @FXML
    private TextField salesmanFilter;


    @FXML
    private TabPane tabPane;

    ObservableList<SalesReturnDetail> salesReturnDetails = FXCollections.observableArrayList();
    ObservableList<SalesReturnDetail> salesReturnSummed = FXCollections.observableArrayList();
    ObservableList<StockTransfer> stockTransferList = FXCollections.observableArrayList(); //badProductTransferEntriesTableView
    ObservableList<Inventory> inventoryList = FXCollections.observableArrayList();

    SalesmanDAO salesmanDAO = new SalesmanDAO();

    ObservableList<Salesman> salesmanList = FXCollections.observableArrayList(salesmanDAO.getAllSalesmen());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumnsForBadOrderSummary();
        boSummaryTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        badOrderCheckingTab.setDisable(true);

        badOrderCheckingTab.setOnSelectionChanged(event -> {
            if (badOrderCheckingTab.isSelected()) {
                loadDataForChecking();
            }
        });
    }

    private void loadDataForChecking() {

    }

    private void setupTableColumnsForBadOrderSummary() {
        productNameCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getProduct().getProductName()));
        productUnitCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getProduct().getUnitOfMeasurementString()));
        dateOfTransactionCol.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getSalesReturn().getReturnDate()));
        documentNoCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getSalesReturn().getReturnNumber()));
        customerNameCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getSalesReturn().getCustomer().getStoreName()));
        quantityCol.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getQuantity()));
        productNameSummaryCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getProduct().getProductName()));
        productUnitSummaryCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getProduct().getUnitOfMeasurementString()));
        sumQuantityCol.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getQuantity()));
        badProductSummaryPerProductNameCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getProduct().getProductName()));
        badProductSummaryPerProductUnitCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getProduct().getUnitOfMeasurementString()));
        badProductSummaryPerProductQuantityCol.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getQuantity()));
        badProductTransferEntriesNameCol.setCellValueFactory(param -> {
            int productId = param.getValue().getProductId();
            Product product = salesReturnDetails.stream()
                    .map(SalesReturnDetail::getProduct)
                    .filter(p -> p.getProductId() == productId)
                    .findFirst()
                    .orElse(null);
            return new SimpleStringProperty(product != null ? product.getProductName() : "");
        });
        badProductTransferEntriesUnitCol.setCellValueFactory(param -> {
            int productId = param.getValue().getProductId();
            Product product = salesReturnDetails.stream()
                    .map(SalesReturnDetail::getProduct)
                    .filter(p -> p.getProductId() == productId)
                    .findFirst()
                    .orElse(null);
            return new SimpleStringProperty(product != null ? product.getUnitOfMeasurementString() : "");
        });
        badProductTransferEntriesQuantityCol.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getOrderedQuantity()));
        badProductInventoryNameCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getProduct().getProductName()));
        badProductInventoryUnitCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getProduct().getUnitOfMeasurementString()));
        badProductInventoryQuantityCol.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getQuantity()));
        badProductInventoryFindingsCol.setCellValueFactory(param ->
                new SimpleObjectProperty<>(param.getValue().getQuantity() == 0 ? "TRUE" : "FALSE")
        );

        badProductInventoryFindingsCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle(""); // Reset style
                } else {
                    setText(item);
                    if ("TRUE".equals(item)) {
                        setStyle("-fx-background-color: green; -fx-text-fill: white;");
                    } else {
                        setStyle("-fx-background-color: red; -fx-text-fill: white;");
                    }
                }
            }
        });


        boSummaryTable.setItems(salesReturnDetails);
        boSummarySumTable.setItems(salesReturnSummed);
        badProductSummaryPerProductTableView.setItems(salesReturnSummed);
        badProductTransferEntriesTableView.setItems(stockTransferList);
        badProductInventoryTableView.setItems(inventoryList);


        boSummarySumTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                int selectedProductId = newValue.getProduct().getProductId();

                // Sort the boSummaryTable by productId
                boSummaryTable.getItems().sort((detail1, detail2) -> {
                    if (detail1.getProduct().getProductId() == selectedProductId) {
                        return -1; // Move selected product to top
                    } else if (detail2.getProduct().getProductId() == selectedProductId) {
                        return 1;
                    } else {
                        return Integer.compare(detail1.getProduct().getProductId(), detail2.getProduct().getProductId());
                    }
                });

                // Find the first matching row and select it
                for (SalesReturnDetail detail : boSummaryTable.getItems()) {
                    if (detail.getProduct().getProductId() == selectedProductId) {
                        boSummaryTable.getSelectionModel().clearSelection();
                        boSummaryTable.getSelectionModel().select(detail);
                        boSummaryTable.scrollTo(detail); // Scroll to the selected row
                        break;
                    }
                }
            }
        });


    }

    public void initializeDataForBadProducts() {

        TextFields.bindAutoCompletion(salesmanFilter, salesmanList.stream().map(Salesman::getSalesmanName).toList());

        salesmanFilter.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                checkFieldsForLoadingData();
                selectedSalesman = salesmanList.stream().filter(salesman -> salesman.getSalesmanName().equals(newValue)).findFirst().orElse(null);
            }
        });
        dateFromFilter.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                checkFieldsForLoadingData();
            }
        });
        dateToFilter.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                checkFieldsForLoadingData();
            }
        });
    }

    SalesReturnDAO salesReturnDAO = new SalesReturnDAO();
    Salesman selectedSalesman;

    private void checkFieldsForLoadingData() {
        if (selectedSalesman != null && dateFromFilter.getValue() != null && dateToFilter.getValue() != null) {
            boSummaryTable.setPlaceholder(new ProgressIndicator());
            boSummarySumTable.setPlaceholder(new ProgressIndicator());
            Timestamp dateFrom = Timestamp.valueOf(dateFromFilter.getValue().atStartOfDay());
            Timestamp dateTo = Timestamp.valueOf(dateToFilter.getValue().atStartOfDay());
            salesmanNameHeader.setText(selectedSalesman.getSalesmanName());
            dateToChecking.setValue(dateToFilter.getValue());
            dateFromChecking.setValue(dateFromFilter.getValue());

            CompletableFuture.supplyAsync(() -> salesReturnDAO.getBadOrderDetails(selectedSalesman, dateFrom, dateTo))
                    .thenAcceptAsync(details -> {
                        salesReturnDetails.setAll(details);

                        // Summing quantities by Product ID while keeping track of the product itself
                        Map<Integer, SalesReturnDetail> summaryMap = new HashMap<>();
                        for (SalesReturnDetail detail : details) {
                            int productId = detail.getProduct().getProductId();
                            if (!summaryMap.containsKey(productId)) {
                                // Create a new SalesReturnDetail with the product set, but quantity 0
                                SalesReturnDetail summaryDetail = new SalesReturnDetail();
                                summaryDetail.setProduct(detail.getProduct()); // Set product details
                                summaryDetail.setQuantity(0);
                                summaryMap.put(productId, summaryDetail);
                            }
                            // Add the quantity
                            summaryMap.get(productId).setQuantity(summaryMap.get(productId).getQuantity() + detail.getQuantity());
                        }

                        badOrderCheckingTab.setDisable(false);

                        salesReturnSummed.setAll(FXCollections.observableArrayList(summaryMap.values()));
                    });
        }
    }
}

