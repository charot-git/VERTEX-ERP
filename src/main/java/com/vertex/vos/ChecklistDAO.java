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

public class ChecklistDAO {

    HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();
    ProductDAO productDAO = new ProductDAO();
    ConsolidationDAO consolidationDAO = new ConsolidationDAO();
    DispatchPlanDAO dispatchPlanDAO = new DispatchPlanDAO();

    public ObservableList<ChecklistDTO> getChecklistForConsolidation(Consolidation consolidation) {
        System.out.println("Fetching checklist for consolidation: " + consolidation.getConsolidationNo());

        consolidation.setDispatchPlans(FXCollections.observableArrayList(consolidationDAO.getDispatchPlansForConsolidation(consolidation)));
        consolidation.setStockTransfers(FXCollections.observableArrayList(consolidationDAO.getStockTransfersForConsolidation(consolidation)));

        ObservableList<ChecklistDTO> checklist = FXCollections.observableArrayList();
        ObservableList<StockTransfer> stockTransfers = getStockTransfersForConsolidation(consolidation);
        ObservableList<SalesOrder> salesOrders = getSalesOrdersForConsolidation(consolidation);

        for (SalesOrder salesOrder : salesOrders) {
            ObservableList<SalesOrderDetails> salesOrderDetails = getSalesOrderDetailsForConsolidation(salesOrder);
            for (SalesOrderDetails salesOrderDetail : salesOrderDetails) {
                System.out.println("Processing sales order detail: " + salesOrderDetail);
                ChecklistDTO checklistDTO = new ChecklistDTO();
                checklistDTO.setProduct(salesOrderDetail.getProduct());
                checklistDTO.setOrderedQuantity(salesOrderDetail.getOrderedQuantity());
                checklistDTO.setServedQuantity(salesOrderDetail.getServedQuantity());
                checklist.add(checklistDTO);
            }
        }

        for (StockTransfer stockTransfer : stockTransfers) {
            System.out.println("Processing stock transfer: " + stockTransfer);
            ChecklistDTO checklistDTO = new ChecklistDTO();
            checklistDTO.setProduct(stockTransfer.getProduct());
            checklistDTO.setOrderedQuantity(stockTransfer.getOrderedQuantity());
            checklistDTO.setServedQuantity(stockTransfer.getReceivedQuantity());
            checklist.add(checklistDTO);
        }
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
                            newStockTransfer.setProduct(productDAO.getProductById(resultSet.getInt("product_id")));
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


    ObservableList<SalesOrder> getSalesOrdersForConsolidation(Consolidation consolidation) {
        System.out.println("Fetching sales orders for consolidation: " + consolidation.getConsolidationNo());
        ObservableList<SalesOrder> salesOrders = FXCollections.observableArrayList();
        for (DispatchPlan dispatchPlan : consolidation.getDispatchPlans()) {
            dispatchPlan.setSalesOrders(dispatchPlanDAO.getSalesOrdersForDispatchPlan(dispatchPlan.getDispatchId()));
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
                salesOrderDetail.setProduct(productDAO.getProductById(resultSet.getInt("product_id")));
                salesOrderDetail.setOrderedQuantity(resultSet.getInt("ordered_quantity"));
                salesOrderDetail.setServedQuantity(resultSet.getInt("served_quantity"));
                System.out.println("Retrieved sales order detail: " + salesOrderDetail);
                salesOrderDetails.add(salesOrderDetail);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return salesOrderDetails;
    }
}