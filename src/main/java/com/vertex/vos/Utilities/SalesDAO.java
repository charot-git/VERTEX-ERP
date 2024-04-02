package com.vertex.vos.Utilities;

import com.vertex.vos.Constructors.SalesOrder;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SalesDAO {

    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    public List<SalesOrder> getAllSalesOrders() throws SQLException {
        List<SalesOrder> salesOrders = new ArrayList<>();
        String query = "SELECT DISTINCT ORDERID FROM tbl_po_orders ";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                String orderId = resultSet.getString("ORDERID");
                SalesOrder salesOrder = getSalesOrderByOrderId(orderId);
                if (salesOrder != null) {
                    salesOrders.add(salesOrder);
                }
            }
        }
        return salesOrders;
    }

    public List<SalesOrder> getAllRequestedSalesOrders() throws SQLException {
        List<SalesOrder> salesOrders = new ArrayList<>();
        String query = "SELECT DISTINCT ORDERID FROM tbl_po_orders WHERE PO_STATUS = 'REQUESTED'";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                String orderId = resultSet.getString("ORDERID");
                SalesOrder salesOrder = getSalesOrderByOrderId(orderId);
                if (salesOrder != null) {
                    salesOrders.add(salesOrder);
                }
            }
        }
        return salesOrders;
    }

    public SalesOrder getSalesOrderByOrderId(String orderId) throws SQLException {
        SalesOrder salesOrder = null;
        String query = "SELECT * FROM tbl_po_orders WHERE ORDERID = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, orderId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    salesOrder = extractSalesOrderFromResultSet(resultSet);
                }
            }
        }
        return salesOrder;
    }

    private SalesOrder extractSalesOrderFromResultSet(ResultSet resultSet) throws SQLException {
        SalesOrder salesOrder = new SalesOrder();
        salesOrder.setSalesOrderId(resultSet.getInt("POORDERSID"));
        salesOrder.setOrderId(resultSet.getString("ORDERID"));
        salesOrder.setProductId(resultSet.getInt("PRODUCT_ID"));
        salesOrder.setDescription(resultSet.getString("DESCRIPTION"));
        salesOrder.setBarcode(resultSet.getString("BARCODE"));
        salesOrder.setQty(resultSet.getBigDecimal("QTY"));
        salesOrder.setPrice(resultSet.getBigDecimal("PRICE"));
        salesOrder.setTabName(resultSet.getString("TAB_NAME"));
        salesOrder.setCustomerId(resultSet.getString("CUSTOMERID"));
        salesOrder.setCustomerName(resultSet.getString("CUSTOMER_NAME"));
        salesOrder.setStoreName(resultSet.getString("STORE_NAME"));
        salesOrder.setSalesMan(resultSet.getString("SALES_MAN"));
        salesOrder.setCreatedDate(resultSet.getTimestamp("CREATED_DATE"));
        salesOrder.setTotal(resultSet.getBigDecimal("TOTAL"));
        salesOrder.setSoStatus(resultSet.getString("PO_STATUS"));
        return salesOrder;
    }
}

