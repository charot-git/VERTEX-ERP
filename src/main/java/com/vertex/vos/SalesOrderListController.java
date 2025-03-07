package com.vertex.vos;

import com.vertex.vos.Objects.SalesOrder;
import com.vertex.vos.Utilities.SalesOrderDAO;
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
import lombok.Setter;

import java.net.URL;
import java.sql.Timestamp;
import java.util.ResourceBundle;

public class SalesOrderListController implements Initializable {

    public TableColumn<SalesOrder, String> supplierCol;
    public TextField supplierFilter;
    public TextField branchFilter;
    public TableColumn<SalesOrder, String> branchNameCol;
    @FXML
    private Button confirmButton;

    @FXML
    private TableColumn<SalesOrder, Timestamp> createdDateCol;

    @FXML
    private TableColumn<SalesOrder, String> customerCodeCol;

    @FXML
    private TableColumn<SalesOrder, Timestamp> orderDateCol;

    @FXML
    private DatePicker orderDateFromFilter;

    @FXML
    private DatePicker orderDateToFilter;

    @FXML
    private TableColumn<SalesOrder, String> orderNoCol;

    @FXML
    private TextField orderNoFilter;

    @FXML
    private TableView<SalesOrder> orderTable;

    @FXML
    private TableColumn<SalesOrder, String> receiptTypeCol;

    @FXML
    private TableColumn<SalesOrder, String> salesmanCodeCol;

    @FXML
    private TextField salesmanFilter;

    @FXML
    private TableColumn<SalesOrder, String> salesmanNameCol;

    @FXML
    private TableColumn<SalesOrder, SalesOrder.SalesOrderStatus> statusCol;

    @FXML
    private ComboBox<SalesOrder.SalesOrderStatus> statusFilter;

    @FXML
    private TableColumn<SalesOrder, String> storeNameCol;

    @FXML
    private TextField storeNameFilter;

    @FXML
    private TableColumn<SalesOrder, Double> totalAmountCol;

    ObservableList<SalesOrder> salesOrderList = FXCollections.observableArrayList();

    SalesOrderDAO salesOrderDAO = new SalesOrderDAO();

    private int currentPage = 0; // Keep track of the current page

    private static final int PAGE_SIZE = 35; // Number of items per page

    public void loadSalesOrder() {
        String orderNo = orderNoFilter.getText();
        String customer = storeNameFilter.getText();
        String salesman = salesmanFilter.getText();
        String supplier = supplierFilter.getText();
        String branch = branchFilter.getText();
        SalesOrder.SalesOrderStatus status = statusFilter.getValue();
        Timestamp orderDateFrom = orderDateFromFilter.getValue() != null ? Timestamp.valueOf(orderDateFromFilter.getValue().atStartOfDay()) : null;
        Timestamp orderDateTo = orderDateToFilter.getValue() != null ? Timestamp.valueOf(orderDateToFilter.getValue().atTime(23, 59, 59)) : null;

        salesOrderList.clear();
        salesOrderList.addAll(salesOrderDAO.getAllSalesOrders(currentPage * PAGE_SIZE, PAGE_SIZE, branch, orderNo, customer, salesman, supplier, status, orderDateFrom, orderDateTo));

        if (salesOrderList.isEmpty()) {
            orderTable.setPlaceholder(new Label("No orders found."));
        }

        confirmButton.setText("Add New");
        confirmButton.setOnAction(event -> addNewSalesOrder());
    }

    private Stage salesOrderFormStage;

    private void addNewSalesOrder() {
        if (salesOrderFormStage == null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("SalesOrderForm.fxml"));
                Parent content = loader.load();
                SalesOrderFormController controller = loader.getController();
                controller.setSalesOrderListController(this);
                controller.createNewSalesOrder();
                salesOrderFormStage = new Stage();
                salesOrderFormStage.setTitle("Create New Sales Order");
                salesOrderFormStage.setScene(new Scene(content));
                salesOrderStage.isMaximized();
                salesOrderFormStage.showAndWait();
                salesOrderFormStage.setOnCloseRequest(event -> salesOrderFormStage = null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            salesOrderFormStage.toFront();
        }
    }

    @Setter
    Stage salesOrderStage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize columns with appropriate values
        orderNoCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getOrderNo()));
        storeNameCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCustomer().getStoreName()));
        customerCodeCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCustomer().getCustomerCode()));
        salesmanNameCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getSalesman().getSalesmanName()));
        salesmanCodeCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getSalesman().getSalesmanCode()));
        orderDateCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getOrderDate()));
        createdDateCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getCreatedDate()));
        totalAmountCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getTotalAmount()));
        receiptTypeCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getInvoiceType().getName()));
        statusCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getOrderStatus()));
        branchNameCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getBranch().getBranchName()));
        statusFilter.setItems(FXCollections.observableArrayList(SalesOrder.SalesOrderStatus.values()));

        // Set initial data for table
        orderTable.setItems(salesOrderList);

        // Add a scroll listener to the TableView to implement infinite scrolling
        orderTable.setOnScroll(event -> {
            if (isScrollNearBottom()) {
                currentPage++; // Move to the next page
                loadSalesOrder(); // Load the next batch of items
            }
        });
    }

    // Check if the TableView is scrolled to the bottom
    private boolean isScrollNearBottom() {
        ScrollBar scrollBar = (ScrollBar) orderTable.lookup(".scroll-bar:vertical");
        if (scrollBar != null) {
            double value = scrollBar.getValue();
            double max = scrollBar.getMax();
            double visibleAmount = scrollBar.getVisibleAmount();
            return value >= max - visibleAmount;
        }
        return false;
    }
}
