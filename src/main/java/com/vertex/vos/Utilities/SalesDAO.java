package com.vertex.vos.Utilities;

import com.vertex.vos.Constructors.SalesOrder;
import com.vertex.vos.Constructors.SalesOrderHeader;
import com.zaxxer.hikari.HikariDataSource;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SalesDAO {

    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();
    private final CustomerDAO customerDAO = new CustomerDAO();

    public int getNextSoNo() {
        int nextSoNo = 0;
        String updateQuery = "UPDATE sales_order_numbers SET so_no = so_no + 1";
        String selectQuery = "SELECT so_no FROM sales_order_numbers ORDER BY so_no DESC LIMIT 1";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement updateStatement = connection.prepareStatement(updateQuery);
             PreparedStatement selectStatement = connection.prepareStatement(selectQuery);
             ResultSet resultSet = selectStatement.executeQuery()) {

            if (resultSet.next()) {
                nextSoNo = resultSet.getInt("so_no");
            }

            updateStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return nextSoNo;
    }

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

    public List<SalesOrderHeader> getAllOrders() throws SQLException {
        List<SalesOrderHeader> orders = new ArrayList<>();
        String sqlQuery = "SELECT * FROM tbl_orders";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sqlQuery);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                SalesOrderHeader order = new SalesOrderHeader();
                order.setOrderId(resultSet.getInt("orderID"));
                order.setCustomerName(customerDAO.getStoreNameById(Integer.parseInt(resultSet.getString("customer_name"))));
                order.setAdminId(resultSet.getInt("admin_id"));
                order.setOrderDate(resultSet.getTimestamp("orderdate"));
                order.setPosNo(resultSet.getString("posno"));
                order.setTerminalNo(resultSet.getString("terminalno"));
                order.setHeaderId(resultSet.getInt("headerID"));
                order.setStatus(resultSet.getString("status"));
                order.setCash(resultSet.getBigDecimal("cash"));
                order.setAmountDue(resultSet.getBigDecimal("amountDue"));
                order.setChange(resultSet.getBigDecimal("change"));
                Timestamp paidDateTimestamp = resultSet.getTimestamp("paidDate");
                LocalDateTime paidDate = paidDateTimestamp != null ? paidDateTimestamp.toLocalDateTime() : null;
                order.setPaidBy(resultSet.getString("paidBy"));
                orders.add(order);
            }
        }
        return orders;
    }


    public boolean createSalesOrderHeader(SalesOrderHeader header) throws SQLException {
        String sqlQuery = "INSERT INTO tbl_orders (orderID, customer_name, admin_id, orderdate, posno, terminalno, status, cash, amountDue, `change`, paidDate, paidBy) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sqlQuery, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, header.getOrderId());
            statement.setString(2, header.getCustomerName());
            statement.setInt(3, header.getAdminId());
            statement.setTimestamp(4, header.getOrderDate());
            statement.setString(5, header.getPosNo());
            statement.setString(6, header.getTerminalNo());
            statement.setString(7, header.getStatus());
            statement.setBigDecimal(8, header.getCash());
            statement.setBigDecimal(9, header.getAmountDue());
            statement.setBigDecimal(10, header.getChange());
            statement.setTimestamp(11, header.getPaidDate());
            statement.setString(12, header.getPaidBy());

            int rowsInserted = statement.executeUpdate();
            return rowsInserted > 0;
        }
    }


    public boolean createOrderPerProduct(List<SalesOrder> orders) throws SQLException {
        String sqlQuery = "INSERT INTO tbl_po_orders (ORDERID, PRODUCT_ID, DESCRIPTION, BARCODE, QTY, PRICE, TAB_NAME, " +
                "CUSTOMERID, CUSTOMER_NAME, STORE_NAME, SALES_MAN, CREATED_DATE, TOTAL, PO_STATUS) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sqlQuery, Statement.RETURN_GENERATED_KEYS)) {
            for (SalesOrder order : orders) {
                statement.setString(1, order.getOrderID());
                statement.setInt(2, order.getProductID());
                statement.setString(3, order.getDescription());
                statement.setString(4, order.getBarcode());
                statement.setInt(5, order.getQty());
                statement.setBigDecimal(6, order.getPrice());
                statement.setString(7, order.getTabName());
                statement.setString(8, order.getCustomerID());
                statement.setString(9, order.getCustomerName());
                statement.setString(10, order.getStoreName());
                statement.setString(11, order.getSalesMan());
                statement.setTimestamp(12, order.getCreatedDate());
                statement.setBigDecimal(13, order.getTotal());
                statement.setString(14, order.getPoStatus());
                statement.addBatch();
            }
            int[] rowsInserted = statement.executeBatch();
            if (rowsInserted.length == orders.size()) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
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
            statement.setInt(5, order.getQty());
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
        order.setQty(resultSet.getInt("QTY"));
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
