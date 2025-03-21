package com.vertex.vos;

import com.vertex.vos.DAO.ConsolidationDAO;
import com.vertex.vos.DAO.DispatchPlanDAO;
import com.vertex.vos.Objects.*;
import com.vertex.vos.Utilities.DatabaseConnectionPool;
import com.vertex.vos.Utilities.ProductDAO;
import com.zaxxer.hikari.HikariDataSource;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

public class ChecklistDAO {

    HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();
    ProductDAO productDAO = new ProductDAO();
    ConsolidationDAO consolidationDAO = new ConsolidationDAO();
    DispatchPlanDAO dispatchPlanDAO = new DispatchPlanDAO();

    public ObservableList<ChecklistDTO> getChecklistForConsolidation(Consolidation consolidation) {
        consolidation.setDispatchPlans(FXCollections.observableArrayList(consolidationDAO.getDispatchPlansForConsolidation(consolidation)));
        consolidation.setStockTransfers(FXCollections.observableArrayList(consolidationDAO.getStockTransfersForConsolidation(consolidation)));
        ObservableList<ChecklistDTO> checklist = FXCollections.observableArrayList();
        ObservableList<StockTransfer> stockTransfers = getStockTransfersForConsolidation(consolidation);
        ObservableList<SalesOrder> salesOrders = getSalesOrdersForConsolidation(consolidation);
        Map<Product, ChecklistDTO> productMap = new HashMap<>();
        for (SalesOrder salesOrder : salesOrders) {
            for (SalesOrderDetails salesOrderDetail : salesOrder.getSalesOrderDetails()) {
                Product product = salesOrderDetail.getProduct();
                ChecklistDTO checklistDTO = productMap.getOrDefault(product, new ChecklistDTO());
                checklistDTO.setProduct(product);
                checklistDTO.setOrderedQuantity(checklistDTO.getOrderedQuantity() + salesOrderDetail.getOrderedQuantity());
                checklistDTO.setServedQuantity(checklistDTO.getServedQuantity() + salesOrderDetail.getServedQuantity());
                productMap.put(product, checklistDTO);
            }
        }
        for (StockTransfer stockTransfer : stockTransfers) {
            Product product = stockTransfer.getProduct();
            ChecklistDTO checklistDTO = productMap.getOrDefault(product, new ChecklistDTO());
            checklistDTO.setProduct(product);
            checklistDTO.setOrderedQuantity(checklistDTO.getOrderedQuantity() + stockTransfer.getOrderedQuantity());
            checklistDTO.setServedQuantity(checklistDTO.getServedQuantity() + stockTransfer.getReceivedQuantity());
            productMap.put(product, checklistDTO);
        }

        checklist.addAll(productMap.values());


        return checklist;
    }

    ObservableList<StockTransfer> getStockTransfersForConsolidation(Consolidation consolidation) {
        ObservableList<StockTransfer> stockTransfers = FXCollections.observableArrayList();
        String stockTransferQuery = "SELECT order_no, product_id, ordered_quantity, received_quantity FROM stock_transfer WHERE order_no = ?";

        try {
            for (StockTransfer stockTransfer : consolidation.getStockTransfers()) {
                try (var connection = dataSource.getConnection();
                     var preparedStatement = connection.prepareStatement(stockTransferQuery)) {

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
            return null;
        }
        return stockTransfers;
    }

    private synchronized Product getProductByIdForWarehouse(int productId) {
        return productDAO.getProductById(productId);
    }


    ObservableList<SalesOrder> getSalesOrdersForConsolidation(Consolidation consolidation) {
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

    ObservableList<SalesOrderDetails> getSalesOrderDetailsForConsolidation(SalesOrder salesOrder) {
        System.out.println("Fetching sales order details for order ID: " + salesOrder.getOrderId());
        ObservableList<SalesOrderDetails> salesOrderDetails = FXCollections.observableArrayList();
        String salesOrderDetailsQuery = "SELECT order_id, product_id, ordered_quantity, served_quantity FROM sales_order_details WHERE order_id = ?";

        try (PreparedStatement preparedStatement = dataSource.getConnection().prepareStatement(salesOrderDetailsQuery)) {
            preparedStatement.setInt(1, salesOrder.getOrderId());
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                SalesOrderDetails salesOrderDetail = new SalesOrderDetails();
                salesOrderDetail.setProduct(getProductByIdForWarehouse(resultSet.getInt("product_id")));
                salesOrderDetail.setOrderedQuantity(resultSet.getInt("ordered_quantity"));
                salesOrderDetail.setServedQuantity(resultSet.getInt("served_quantity"));
                salesOrderDetails.add(salesOrderDetail);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return salesOrderDetails;
    }
}