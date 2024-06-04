package com.vertex.vos.Utilities;

import com.vertex.vos.Constructors.SalesOrderHeader;
import com.zaxxer.hikari.HikariDataSource;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SalesDAO {

    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    public List<SalesOrderHeader> getAllOrders() throws SQLException {
        List<SalesOrderHeader> orders = new ArrayList<>();
        String sqlQuery = "SELECT * FROM tbl_orders";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sqlQuery);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                SalesOrderHeader order = extractOrderFromResultSet(resultSet);
                orders.add(order);
            }
        }

        return orders;
    }

    CustomerDAO customerDAO = new CustomerDAO();

    private SalesOrderHeader extractOrderFromResultSet(ResultSet resultSet) throws SQLException {
        SalesOrderHeader order = new SalesOrderHeader();
        order.setOrderID(resultSet.getInt("orderID"));
        order.setCustomerName(customerDAO.getStoreNameById(Integer.parseInt(resultSet.getString("customer_name"))));
        order.setAdminID(resultSet.getInt("admin_id"));
        order.setOrderDate(resultSet.getTimestamp("orderdate"));
        order.setPosNo(resultSet.getString("posno"));
        order.setTerminalNo(resultSet.getString("terminalno"));
        order.setHeaderID(resultSet.getInt("headerID"));
        order.setStatus(resultSet.getString("status"));
        order.setCash(resultSet.getBigDecimal("cash"));
        order.setAmountDue(resultSet.getBigDecimal("amountDue"));
        order.setChange(resultSet.getBigDecimal("change"));
        order.setPaidDate(resultSet.getTimestamp("paidDate"));
        order.setPaidBy(resultSet.getString("paidBy"));

        return order;
    }
}
