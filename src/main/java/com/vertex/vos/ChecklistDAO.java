package com.vertex.vos;

import com.vertex.vos.DAO.ConsolidationDAO;
import com.vertex.vos.DAO.DispatchPlanDAO;
import com.vertex.vos.Objects.*;
import com.vertex.vos.Utilities.DatabaseConnectionPool;
import com.vertex.vos.Utilities.ProductDAO;
import com.zaxxer.hikari.HikariDataSource;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

public class ChecklistDAO {

    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();
    private final ProductDAO productDAO = new ProductDAO();
    private final ConsolidationDAO consolidationDAO = new ConsolidationDAO();
    private final DispatchPlanDAO dispatchPlanDAO = new DispatchPlanDAO();

    public ObservableList<ConsolidationDetails> getChecklistForConsolidation(Consolidation consolidation) {
        Map<Product, ConsolidationDetails> productMap = new HashMap<>();
        consolidation.setDispatchPlans(FXCollections.observableArrayList(consolidationDAO.getDispatchPlansForConsolidation(consolidation)));
        consolidation.setStockTransfers(FXCollections.observableArrayList(consolidationDAO.getStockTransfersForConsolidation(consolidation)));
        ObservableList<ConsolidationDetails> checklist = FXCollections.observableArrayList();
        if (!consolidation.getStockTransfers().isEmpty()) {
            ObservableList<StockTransfer> stockTransfers = getStockTransfersForConsolidation(consolidation);
            for (StockTransfer stockTransfer : stockTransfers) {
                Product product = stockTransfer.getProduct();
                ConsolidationDetails consolidationDetails = productMap.getOrDefault(product, new ConsolidationDetails());
                consolidationDetails.setProduct(product);
                consolidationDetails.setOrderedQuantity(consolidationDetails.getOrderedQuantity() + stockTransfer.getOrderedQuantity());
                consolidationDetails.setPickedQuantity(consolidationDetails.getPickedQuantity() + stockTransfer.getReceivedQuantity());
                productMap.put(product, consolidationDetails);
            }
        }
        if (!consolidation.getDispatchPlans().isEmpty()) {
            ObservableList<SalesOrder> salesOrders = getSalesOrdersForConsolidation(consolidation);
            for (SalesOrder salesOrder : salesOrders) {
                for (SalesOrderDetails salesOrderDetail : salesOrder.getSalesOrderDetails()) {
                    Product product = salesOrderDetail.getProduct();
                    ConsolidationDetails consolidationDetails = productMap.getOrDefault(product, new ConsolidationDetails());
                    consolidationDetails.setProduct(product);
                    consolidationDetails.setOrderedQuantity(consolidationDetails.getOrderedQuantity() + salesOrderDetail.getOrderedQuantity());
                    consolidationDetails.setPickedQuantity(consolidationDetails.getPickedQuantity() + salesOrderDetail.getServedQuantity());
                    productMap.put(product, consolidationDetails);
                }
            }
        }
        checklist.addAll(productMap.values());
        return checklist;
    }

    private ObservableList<StockTransfer> getStockTransfersForConsolidation(Consolidation consolidation) {
        ObservableList<StockTransfer> stockTransfers = FXCollections.observableArrayList();
        String stockTransferQuery = "SELECT order_no, product_id, ordered_quantity, received_quantity FROM stock_transfer WHERE order_no = ?";

        try (Connection connection = dataSource.getConnection()) {
            for (StockTransfer stockTransfer : consolidation.getStockTransfers()) {
                try (PreparedStatement preparedStatement = connection.prepareStatement(stockTransferQuery)) {
                    preparedStatement.setString(1, stockTransfer.getStockNo());
                    try (ResultSet resultSet = preparedStatement.executeQuery()) {
                        while (resultSet.next()) {
                            StockTransfer newStockTransfer = new StockTransfer();
                            newStockTransfer.setStockNo(resultSet.getString("order_no"));
                            newStockTransfer.setProduct(getProductByIdForWarehouse(resultSet.getInt("product_id")));
                            newStockTransfer.setOrderedQuantity(resultSet.getInt("ordered_quantity"));
                            newStockTransfer.setReceivedQuantity(resultSet.getInt("received_quantity"));
                            stockTransfers.add(newStockTransfer);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stockTransfers;
    }

    private Product getProductByIdForWarehouse(int productId) {
        return productDAO.getProductById(productId);
    }

    private ObservableList<SalesOrder> getSalesOrdersForConsolidation(Consolidation consolidation) {
        ObservableList<SalesOrder> salesOrders = FXCollections.observableArrayList();
        for (DispatchPlan dispatchPlan : consolidation.getDispatchPlans()) {
            ObservableList<SalesOrder> dispatchPlanSalesOrders = dispatchPlanDAO.getSalesOrdersForDispatchPlan(dispatchPlan.getDispatchId());
            for (SalesOrder salesOrder : dispatchPlanSalesOrders) {
                salesOrder.setSalesOrderDetails(getSalesOrderDetailsForConsolidation(salesOrder));
            }
            salesOrders.addAll(dispatchPlanSalesOrders);
        }
        return salesOrders;
    }

    private ObservableList<SalesOrderDetails> getSalesOrderDetailsForConsolidation(SalesOrder salesOrder) {
        ObservableList<SalesOrderDetails> salesOrderDetails = FXCollections.observableArrayList();
        String salesOrderDetailsQuery = "SELECT order_id, product_id, ordered_quantity, served_quantity FROM sales_order_details WHERE order_id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(salesOrderDetailsQuery)) {
            preparedStatement.setInt(1, salesOrder.getOrderId());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    SalesOrderDetails salesOrderDetail = new SalesOrderDetails();
                    salesOrderDetail.setProduct(getProductByIdForWarehouse(resultSet.getInt("product_id")));
                    salesOrderDetail.setOrderedQuantity(resultSet.getInt("ordered_quantity"));
                    salesOrderDetail.setServedQuantity(resultSet.getInt("served_quantity"));
                    salesOrderDetails.add(salesOrderDetail);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return salesOrderDetails;
    }
}
