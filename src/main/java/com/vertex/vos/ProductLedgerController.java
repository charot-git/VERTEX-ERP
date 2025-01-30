package com.vertex.vos;

import com.vertex.vos.DAO.ProductLedgerDAO;
import com.vertex.vos.Objects.Branch;
import com.vertex.vos.Objects.Product;
import com.vertex.vos.Objects.ProductLedger;
import com.vertex.vos.Utilities.BranchDAO;
import com.vertex.vos.Utilities.ProductDAO;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import org.controlsfx.control.textfield.TextFields;

import java.net.URL;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;
import java.util.ResourceBundle;

public class ProductLedgerController implements Initializable {
    public Button searchButton;
    public TableView<ProductLedger> ledgerTableView;
    @FXML
    private TableView<Product> productConfig;
    @FXML
    private TableColumn<Product, String> barcodeCol;
    @FXML
    private TableColumn<Product, String> descriptionCol;
    @FXML
    private TableColumn<Product, String> UOMCol;
    @FXML
    private TableColumn<Product, Integer> countCol;
    @FXML
    private TableColumn<ProductLedger, Integer> quantityCol;

    @FXML
    private PieChart balancePieChart;
    @FXML
    private TextField branchCodeFilter;
    @FXML
    private TableColumn<ProductLedger, Date> dateCol;
    @FXML
    private DatePicker dateFrom;
    @FXML
    private DatePicker dateTo;
    @FXML
    private TableColumn<ProductLedger, String> documentNoCol;
    @FXML
    private TableColumn<ProductLedger, String> documentTypeCol;
    @FXML
    private TableColumn<ProductLedger, Integer> inCol;
    @FXML
    private TableColumn<ProductLedger, Integer> outCol;
    @FXML
    private TextField productNameFilter;
    @FXML
    private TableColumn<ProductLedger, String> transferToCol;
    @FXML
    private TableColumn<ProductLedger, Integer> uomBreakdown;
    @FXML
    private TableColumn<ProductLedger, String> uomCol;

    private final ProductLedgerDAO productLedgerDAO = new ProductLedgerDAO();
    private final BranchDAO branchDAO = new BranchDAO();
    private final ProductDAO productDAO = new ProductDAO();

    private Branch selectedBranch;
    private Product parentProduct;
    private final ObservableList<Product> productWithChildren = FXCollections.observableArrayList();
    private final ObservableList<ProductLedger> ledger = FXCollections.observableArrayList();
    private final ProgressIndicator progressIndicator = new ProgressIndicator();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        List<String> productNames = productLedgerDAO.getAllProductNames();
        List<Branch> branches = branchDAO.getAllBranches();
        List<String> branchCodes = branches.stream().map(Branch::getBranchCode).toList();

        TextFields.bindAutoCompletion(branchCodeFilter, branchCodes);
        TextFields.bindAutoCompletion(productNameFilter, productNames);

        branchCodeFilter.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                selectedBranch = branches.stream().filter(branch -> branch.getBranchCode().equals(newValue)).findFirst().orElse(null);
            }
        });

        productNameFilter.textProperty().addListener((observable, oldValue, newValue) -> {
            productConfig.setPlaceholder(progressIndicator);
            if (newValue != null && !newValue.isEmpty()) {
                parentProduct = productDAO.getProductByName(newValue);
                if (parentProduct != null) {
                    loadProductsByParentId(parentProduct.getProductId());
                } else {
                    productWithChildren.clear(); // Clear if no parent product found
                }
            } else {
                productWithChildren.clear(); // Clear if input is empty
            }
        });

        searchButton.setOnMouseClicked(event -> getProductLedger());

        initializeProductTable();
        initializeLedgerTable();
    }

    private void initializeLedgerTable() {
        dateCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(new java.sql.Date(cellData.getValue().getDocumentDate().getTime())));
        documentNoCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDocumentNo()));
        documentTypeCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDocumentType()));
        inCol.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getIn()).asObject());
        outCol.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getOut()).asObject());
        transferToCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDocumentDescription()));
        quantityCol.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getQuantity()).asObject());
        uomCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduct().getUnitOfMeasurementString()));
        uomBreakdown.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getProduct().getUnitOfMeasurementCount()).asObject());
        ledgerTableView.setItems(ledger);
    }

    private void getProductLedger() {
        if (selectedBranch == null || parentProduct == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Please select a branch and a product.");
            alert.showAndWait();
            return;
        }

        if (dateFrom.getValue() == null || dateTo.getValue() == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Please select both start and end dates.");
            alert.showAndWait();
            return;
        }

        Timestamp startDate = Timestamp.valueOf(dateFrom.getValue().atStartOfDay());
        Timestamp endDate = Timestamp.valueOf(dateTo.getValue().atStartOfDay());

        if (startDate.after(endDate)) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Start date must be before end date.");
            alert.showAndWait();
            return;
        }

        ledger.setAll(productLedgerDAO.getProductLedger(startDate, endDate, productWithChildren, selectedBranch));
        updatePieChart();
    }

    private void updatePieChart() {
        balancePieChart.getData().clear();

        // Calculate total In and Out
        int totalIn = ledger.stream().mapToInt(ProductLedger::getIn).sum();
        int totalOut = ledger.stream().mapToInt(ProductLedger::getOut).sum();

        // Calculate total physical count (sum of quantities from physical inventory)
        int totalPhysicalCount = 0;

        for (ProductLedger ledgerItem : ledger) {
            if (ledgerItem.getDocumentType().equals("Physical Inventory")) {
                totalPhysicalCount += (ledgerItem.getQuantity() * ledgerItem.getProduct().getUnitOfMeasurementCount());
            }
        }

        // Calculate balance
        int balance = totalIn - totalOut;

        // Create pie chart data
        PieChart.Data inData = new PieChart.Data("In (" + totalIn + ")", totalIn);
        PieChart.Data outData = new PieChart.Data("Out (" + totalOut + ")", totalOut);
        PieChart.Data physicalCountData = new PieChart.Data("Physical Count (" + totalPhysicalCount + ")", totalPhysicalCount);
        PieChart.Data balanceData = new PieChart.Data("Balance (" + balance + ")", balance); // Add balance data

        // Add data to the pie chart
        balancePieChart.getData().add(inData);
        balancePieChart.getData().add(outData);
        balancePieChart.getData().add(physicalCountData); // Add physical count data
        balancePieChart.getData().add(balanceData); // Add balance data

        balancePieChart.setLegendVisible(true);
    }

    private void loadProductsByParentId(int parentId) {
        Task<ObservableList<Product>> task = productDAO.getProductsByParentIdTask(parentId);
        task.setOnSucceeded(event -> {
            productWithChildren.setAll(task.getValue());
            productConfig.setPlaceholder(null); // Remove progress indicator
        });
        task.setOnFailed(event -> {
            productConfig.setPlaceholder(null); // Remove progress indicator
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to load products: " + task.getException().getMessage());
            alert.showAndWait();
        });
        new Thread(task).start(); // Run the task in a new thread
    }

    private void initializeProductTable() {
        descriptionCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDescription()));
        UOMCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUnitOfMeasurementString()));
        barcodeCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getBarcode()));
        countCol.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getUnitOfMeasurementCount()).asObject());

        productConfig.setItems(productWithChildren);
        productConfig.setFixedCellSize(30);
        productConfig.prefHeightProperty().bind(productConfig.fixedCellSizeProperty().multiply(Bindings.size(productConfig.getItems()).add(1.01)));
        productConfig.minHeightProperty().bind(productConfig.prefHeightProperty());
        productConfig.maxHeightProperty().bind(productConfig.prefHeightProperty());
    }
}