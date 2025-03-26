package com.vertex.vos;

import com.vertex.vos.Enums.SalesOrderStatus;
import com.vertex.vos.Objects.SalesOrder;
import com.vertex.vos.Utilities.BranchDAO;
import com.vertex.vos.Utilities.SalesOrderDAO;
import com.vertex.vos.Utilities.TableViewFormatter;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class DispatchSalesOrderListController implements Initializable {

    @FXML
    private TableColumn<SalesOrder, String> forApprovalCityCol;

    @FXML
    private TableColumn<SalesOrder, String> forApprovalClusterCol;

    @FXML
    private TableColumn<SalesOrder, String> forApprovalCustomerCol;

    @FXML
    private GridPane forApprovalGridPane;

    @FXML
    private TableColumn<SalesOrder, String> forApprovalProvinceCol;

    @FXML
    private TableColumn<SalesOrder, String> forApprovalBranchSourceCol;

    @FXML
    private TableView<SalesOrder> forApprovalTableView;

    @FXML
    private TableColumn<SalesOrder, Double> forApprovalTotalCol;

    @FXML
    private TableColumn<SalesOrder, String> forConsolidationCityCol;

    @FXML
    private TableColumn<SalesOrder, String> forConsolidationClusterCol;

    @FXML
    private TableColumn<SalesOrder, String> forConsolidationCustomerCol;

    @FXML
    private GridPane forConsolidationGridPane;

    @FXML
    private TableColumn<SalesOrder, String> forConsolidationProvinceCol;

    @FXML
    private TableColumn<SalesOrder, String> forConsolidationBranchSourceCol;

    @FXML
    private TableView<SalesOrder> forConsolidationTableView;

    @FXML
    private TableColumn<SalesOrder, String> forConsolidationTotalCol;

    SalesOrderDAO salesOrderDAO = new SalesOrderDAO();

    ObservableList<SalesOrder> salesOrderList = salesOrderDAO.getAlLPendingSalesOrders();

    BranchDAO branchDAO = new BranchDAO();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        TableViewFormatter.formatTableView(forApprovalTableView);
        TableViewFormatter.formatTableView(forConsolidationTableView);

        forApprovalCityCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCustomer().getCity()));
        forApprovalClusterCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCustomer().getCluster()));
        forApprovalCustomerCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCustomer().getCustomerName()));
        forApprovalProvinceCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCustomer().getProvince()));
        forApprovalBranchSourceCol.setCellValueFactory(cellData -> new SimpleStringProperty(branchDAO.getBranchNameById(cellData.getValue().getSalesman().getGoodBranchCode())));
        forApprovalTotalCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getTotalAmount()));

        forConsolidationCityCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCustomer().getCity()));
        forConsolidationClusterCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCustomer().getCluster()));
        forConsolidationCustomerCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCustomer().getCustomerName()));
        forConsolidationProvinceCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCustomer().getProvince()));
        forConsolidationBranchSourceCol.setCellValueFactory(cellData -> new SimpleStringProperty(branchDAO.getBranchNameById(cellData.getValue().getSalesman().getGoodBranchCode())));
        forConsolidationTotalCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTotalAmount().toString()));

        forApprovalGridPane.getChildren().clear();
        forConsolidationGridPane.getChildren().clear();

        setupTables();
        calculateAndDisplayClusterTotals();
    }

    private void setupTables() {
        ObservableList<SalesOrder> salesOrderListForApproval = salesOrderList.filtered(salesOrder ->
                salesOrder.getOrderStatus().equals(SalesOrderStatus.FOR_APPROVAL));

        ObservableList<SalesOrder> salesOrderListForConsolidation = salesOrderList.filtered(salesOrder ->
                salesOrder.getOrderStatus().equals(SalesOrderStatus.FOR_CONSOLIDATION));

        forApprovalTableView.setItems(salesOrderListForApproval);
        forConsolidationTableView.setItems(salesOrderListForConsolidation);
    }

    private void calculateAndDisplayClusterTotals() {
        Map<String, Double> approvalTotals = new HashMap<>();
        Map<String, Double> consolidationTotals = new HashMap<>();

        for (SalesOrder order : salesOrderList) {
            String cluster = order.getCustomer().getCluster();
            double amount = order.getTotalAmount();

            if (order.getOrderStatus() == SalesOrderStatus.FOR_APPROVAL) {
                approvalTotals.put(cluster, approvalTotals.getOrDefault(cluster, 0.0) + amount);
            } else if (order.getOrderStatus() == SalesOrderStatus.FOR_CONSOLIDATION) {
                consolidationTotals.put(cluster, consolidationTotals.getOrDefault(cluster, 0.0) + amount);
            }
        }

        displayTotalsInGrid(approvalTotals, forApprovalGridPane);
        displayTotalsInGrid(consolidationTotals, forConsolidationGridPane);
    }

    private void displayTotalsInGrid(Map<String, Double> totals, GridPane gridPane) {
        gridPane.getChildren().clear();
        int row = 0;

        for (Map.Entry<String, Double> entry : totals.entrySet()) {
            Label clusterLabel = new Label(entry.getKey());
            Label totalLabel = new Label(String.format("%,.2f", entry.getValue()));

            gridPane.add(clusterLabel, 0, row);
            gridPane.add(totalLabel, 1, row);

            row++;
        }
    }
}
