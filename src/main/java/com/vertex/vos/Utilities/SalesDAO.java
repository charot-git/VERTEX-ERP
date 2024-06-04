package com.vertex.vos.Utilities;

import com.vertex.vos.Constructors.SalesOrder;
import com.zaxxer.hikari.HikariDataSource;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SalesDAO {

    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();
    private final CustomerDAO customerDAO = new CustomerDAO();

    public SalesOrder getOrderById(int poOrdersID) throws SQLException {
        String sqlQuery = "SELECT * FROM tbl_po_orders WHERE POORDERSID = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sqlQuery)) {
            statement.setInt(1, poOrdersID);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return extractOrderFromResultSet(resultSet);
                }
            }
        }
        return null;
    }

    public List<SalesOrder> getAllOrders() throws SQLException {
        List<SalesOrder> orders = new ArrayList<>();
        String sqlQuery = "SELECT DISTINCT * FROM tbl_po_orders";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sqlQuery);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                SalesOrder order = extractOrderFromResultSet(resultSet);
                orders.add(order);
            }
        }
        return orders;
    }


    public void createOrder(SalesOrder order) throws SQLException {
        String sqlQuery = "INSERT INTO tbl_po_orders (ORDERID, PRODUCT_ID, DESCRIPTION, BARCODE, QTY, PRICE, TAB_NAME, " +
                "CUSTOMERID, CUSTOMER_NAME, STORE_NAME, SALES_MAN, CREATED_DATE, TOTAL, PO_STATUS) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sqlQuery, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, order.getOrderID());
            statement.setInt(2, order.getProductID());
            statement.setString(3, order.getDescription());
            statement.setString(4, order.getBarcode());
            statement.setBigDecimal(5, order.getQty());
            statement.setBigDecimal(6, order.getPrice());
            statement.setString(7, order.getTabName());
            statement.setString(8, order.getCustomerID());
            statement.setString(9, order.getCustomerName());
            statement.setString(10, order.getStoreName());
            statement.setString(11, order.getSalesMan());
            statement.setTimestamp(12, order.getCreatedDate());
            statement.setBigDecimal(13, order.getTotal());
            statement.setString(14, order.getPoStatus());
            statement.executeUpdate();
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    order.setPoOrdersID(generatedKeys.getInt(1));
                }
            }
        }
    }

    public void updateOrder(SalesOrder order) throws SQLException {
        String sqlQuery = "UPDATE tbl_po_orders SET ORDERID = ?, PRODUCT_ID = ?, DESCRIPTION = ?, BARCODE = ?, QTY = ?, PRICE = ?, TAB_NAME = ?, " +
                "CUSTOMERID = ?, CUSTOMER_NAME = ?, STORE_NAME = ?, SALES_MAN = ?, CREATED_DATE = ?, TOTAL = ?, PO_STATUS = ? WHERE POORDERSID = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sqlQuery)) {
            statement.setString(1, order.getOrderID());
            statement.setInt(2, order.getProductID());
            statement.setString(3, order.getDescription());
            statement.setString(4, order.getBarcode());
            statement.setBigDecimal(5, order.getQty());
            statement.setBigDecimal(6, order.getPrice());
            statement.setString(7, order.getTabName());
            statement.setString(8, order.getCustomerID());
            statement.setString(9, order.getCustomerName());
            statement.setString(10, order.getStoreName());
            statement.setString(11, order.getSalesMan());
            statement.setTimestamp(12, order.getCreatedDate());
            statement.setBigDecimal(13, order.getTotal());
            statement.setString(14, order.getPoStatus());
            statement.setInt(15, order.getPoOrdersID());
            statement.executeUpdate();
        }
    }

    public void deleteOrder(int poOrdersID) throws SQLException {
        String sqlQuery = "DELETE FROM tbl_po_orders WHERE POORDERSID = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sqlQuery)) {
            statement.setInt(1, poOrdersID);
            statement.executeUpdate();
        }
    }

    private SalesOrder extractOrderFromResultSet(ResultSet resultSet) throws SQLException {
        SalesOrder order = new SalesOrder();
        order.setPoOrdersID(resultSet.getInt("POORDERSID"));
        order.setOrderID(resultSet.getString("ORDERID"));
        order.setProductID(resultSet.getInt("PRODUCT_ID"));
        order.setDescription(resultSet.getString("DESCRIPTION"));
        order.setBarcode(resultSet.getString("BARCODE"));
        order.setQty(resultSet.getBigDecimal("QTY"));
        order.setPrice(resultSet.getBigDecimal("PRICE"));
        order.setTabName(resultSet.getString("TAB_NAME"));
        order.setCustomerID(resultSet.getString("CUSTOMERID"));
        order.setCustomerName(resultSet.getString("CUSTOMER_NAME"));
        order.setStoreName(resultSet.getString("STORE_NAME"));
        order.setSalesMan(resultSet.getString("SALES_MAN"));
        order.setCreatedDate(resultSet.getTimestamp("CREATED_DATE"));
        order.setTotal(resultSet.getBigDecimal("TOTAL"));
        order.setPoStatus(resultSet.getString("PO_STATUS"));

        return order;
    }
}
