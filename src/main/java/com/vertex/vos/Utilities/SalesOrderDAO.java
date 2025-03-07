package com.vertex.vos.Utilities;

import com.vertex.vos.DAO.SalesInvoiceTypeDAO;
import com.vertex.vos.Objects.*;
import com.zaxxer.hikari.HikariDataSource;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SalesOrderDAO {

    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final BranchDAO branchDAO = new BranchDAO();
    private final SalesmanDAO salesmanDAO = new SalesmanDAO();
    private final SalesInvoiceTypeDAO salesInvoiceTypeDAO = new SalesInvoiceTypeDAO();
    SupplierDAO supplierDAO = new SupplierDAO();

    // Get the next Sales Order number
    public int getNextSoNo() {
        int nextSoNo = 0;
        String updateQuery = "UPDATE sales_order_numbers SET so_no = so_no + 1";
        String selectQuery = "SELECT so_no FROM sales_order_numbers ORDER BY so_no DESC LIMIT 1";

        try (Connection connection = dataSource.getConnection(); PreparedStatement updateStatement = connection.prepareStatement(updateQuery); PreparedStatement selectStatement = connection.prepareStatement(selectQuery); ResultSet resultSet = selectStatement.executeQuery()) {

            if (resultSet.next()) {
                nextSoNo = resultSet.getInt("so_no");
            }

            updateStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return nextSoNo;
    }

    // Add a Sales Order
    public boolean addSalesOrder(SalesOrder salesOrder) {
        String query = "INSERT INTO sales_order (order_no, customer_code, salesman_id, order_date, delivery_date, due_date, payment_terms, " + "order_status, total_amount, sales_type, receipt_type, discount_amount, net_amount, created_by, created_date, modified_by, modified_date, " + "posted_by, posted_date, remarks, isDelivered, isCancelled, supplier_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = dataSource.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, salesOrder.getOrderNo());
            preparedStatement.setString(2, salesOrder.getCustomer().getCustomerCode());
            preparedStatement.setInt(3, salesOrder.getSalesman() != null ? salesOrder.getSalesman().getId() : 0);
            preparedStatement.setTimestamp(4, salesOrder.getOrderDate());
            preparedStatement.setTimestamp(5, salesOrder.getDeliveryDate());
            preparedStatement.setTimestamp(6, salesOrder.getDueDate());
            preparedStatement.setInt(7, salesOrder.getPaymentTerms());
            preparedStatement.setString(8, salesOrder.getOrderStatus().name());
            preparedStatement.setDouble(9, salesOrder.getTotalAmount());
            preparedStatement.setInt(10, salesOrder.getSalesType() != null ? salesOrder.getSalesType().getId() : 0);
            preparedStatement.setInt(11, salesOrder.getInvoiceType() != null ? salesOrder.getInvoiceType().getId() : 0);
            preparedStatement.setDouble(12, salesOrder.getDiscountAmount());
            preparedStatement.setDouble(13, salesOrder.getNetAmount());
            preparedStatement.setInt(14, salesOrder.getCreatedBy() != null ? salesOrder.getCreatedBy().getUser_id() : 0);
            preparedStatement.setTimestamp(15, salesOrder.getCreatedDate());
            preparedStatement.setInt(16, salesOrder.getModifiedBy() != null ? salesOrder.getModifiedBy().getUser_id() : 0);
            preparedStatement.setTimestamp(17, salesOrder.getModifiedDate());
            preparedStatement.setInt(18, salesOrder.getPostedBy() != null ? salesOrder.getPostedBy().getUser_id() : 0);
            preparedStatement.setTimestamp(19, salesOrder.getPostedDate());
            preparedStatement.setString(20, salesOrder.getRemarks());
            preparedStatement.setBoolean(21, salesOrder.getIsDelivered());
            preparedStatement.setBoolean(22, salesOrder.getIsCancelled());
            preparedStatement.setInt(23, salesOrder.getSupplier() != null ? salesOrder.getSupplier().getId() : 0);

            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    OperationDAO operationDAO = new OperationDAO();
    EmployeeDAO employeeDAO = new EmployeeDAO();

    // Get all Sales Orders
    public ObservableList<SalesOrder> getAllSalesOrders(int pageNumber, int rowsPerPage, String branchFilter, String orderNoFilter, String customerFilter, String salesmanFilter, String supplierFilter, SalesOrder.SalesOrderStatus statusFilter, Timestamp orderDateFromFilter, Timestamp orderDateToFilter) {

        ObservableList<SalesOrder> salesOrders = FXCollections.observableArrayList();
        StringBuilder sqlQuery = new StringBuilder("SELECT * FROM sales_order WHERE 1 = 1");
        List<Object> params = new ArrayList<>();

        // Add conditions based on input parameters
        if (orderNoFilter != null && !orderNoFilter.trim().isEmpty()) {
            sqlQuery.append(" AND order_no LIKE ?");
            params.add("%" + orderNoFilter.trim() + "%");
        }

        if (branchFilter != null && !branchFilter.trim().isEmpty()) {
            sqlQuery.append(" AND branch = ?");
            params.add(branchFilter);
        }
        if (customerFilter != null && !customerFilter.trim().isEmpty()) {
            sqlQuery.append(" AND customer_code LIKE ?");
            params.add("%" + customerFilter.trim() + "%");
        }
        if (salesmanFilter != null && !salesmanFilter.trim().isEmpty()) {
            sqlQuery.append(" AND salesman_id = ?");
            params.add(Integer.parseInt(salesmanFilter.trim()));
        }

        if (supplierFilter != null && !supplierFilter.trim().isEmpty()) {
            sqlQuery.append(" AND supplier_id = ?");
            params.add(Integer.parseInt(supplierFilter.trim()));
        }
        if (statusFilter != null) {
            sqlQuery.append(" AND order_status = ?");
            params.add(statusFilter.name());
        }
        if (orderDateFromFilter != null || orderDateToFilter != null) {
            sqlQuery.append(" AND (order_date BETWEEN ? AND ? OR ? IS NULL OR ? IS NULL)");
            params.add(orderDateFromFilter);
            params.add(orderDateToFilter);
            params.add(orderDateFromFilter);
            params.add(orderDateToFilter);
        }

        // Add pagination
        sqlQuery.append(" ORDER BY order_date DESC LIMIT ? OFFSET ?");
        params.add(rowsPerPage);
        params.add(pageNumber * rowsPerPage);

        // Log the SQL query and parameters
        System.out.println("Executing SQL Query: " + sqlQuery);
        System.out.println("SQL Parameters: " + params);

        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(sqlQuery.toString())) {

            // Set parameters dynamically
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            // Execute the query and process the results
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    SalesOrder salesOrder = new SalesOrder();
                    salesOrder.setOrderId(rs.getInt("order_id"));
                    salesOrder.setOrderNo(rs.getString("order_no"));
                    salesOrder.setCustomer(customerDAO.getCustomerByCode(rs.getString("customer_code")));
                    salesOrder.setSalesman(salesmanDAO.getSalesmanDetails(rs.getInt("salesman_id")));
                    salesOrder.setOrderDate(rs.getTimestamp("order_date"));
                    salesOrder.setDeliveryDate(rs.getTimestamp("delivery_date"));
                    salesOrder.setSupplier(supplierDAO.getSupplierById(rs.getInt("supplier_id")));
                    salesOrder.setDueDate(rs.getTimestamp("due_date"));
                    salesOrder.setPaymentTerms(rs.getInt("payment_terms"));
                    salesOrder.setOrderStatus(SalesOrder.SalesOrderStatus.valueOf(rs.getString("order_status")));
                    salesOrder.setTotalAmount(rs.getDouble("total_amount"));
                    salesOrder.setSalesType(operationDAO.getOperationById(rs.getInt("sales_type")));
                    salesOrder.setInvoiceType(salesInvoiceTypeDAO.getSalesInvoiceTypeById(rs.getInt("receipt_type")));
                    salesOrder.setDiscountAmount(rs.getDouble("discount_amount"));
                    salesOrder.setNetAmount(rs.getDouble("net_amount"));
                    salesOrder.setCreatedBy(employeeDAO.getUserById(rs.getInt("created_by")));
                    salesOrder.setCreatedDate(rs.getTimestamp("created_date"));
                    salesOrder.setModifiedBy(employeeDAO.getUserById(rs.getInt("modified_by")));
                    salesOrder.setModifiedDate(rs.getTimestamp("modified_date"));
                    salesOrder.setPostedBy(employeeDAO.getUserById(rs.getInt("posted_by")));
                    salesOrder.setPostedDate(rs.getTimestamp("posted_date"));
                    salesOrder.setRemarks(rs.getString("remarks"));
                    salesOrder.setIsDelivered(rs.getBoolean("isDelivered"));
                    salesOrder.setIsCancelled(rs.getBoolean("isCancelled"));

                    salesOrders.add(salesOrder);
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL Exception: " + e.getMessage());
            e.printStackTrace(); // Log the exception
        }

        return salesOrders;
    }


    // Get a Sales Order by its ID
    public SalesOrder getSalesOrderById(int orderId) {
        SalesOrder salesOrder = null;
        String query = "SELECT * FROM sales_order WHERE order_id = ?";

        try (Connection connection = dataSource.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, orderId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    salesOrder = new SalesOrder();
                    salesOrder.setOrderId(resultSet.getInt("order_id"));
                    salesOrder.setOrderNo(resultSet.getString("order_no"));
                    salesOrder.setCustomer(customerDAO.getCustomerByCode(resultSet.getString("customer_code")));
                    salesOrder.setSupplier(supplierDAO.getSupplierById(resultSet.getInt("supplier_id")));
                    salesOrder.setSalesman(salesmanDAO.getSalesmanDetails(resultSet.getInt("salesman_id")));
                    salesOrder.setOrderDate(resultSet.getTimestamp("order_date"));
                    salesOrder.setDeliveryDate(resultSet.getTimestamp("delivery_date"));
                    salesOrder.setDueDate(resultSet.getTimestamp("due_date"));
                    salesOrder.setPaymentTerms(resultSet.getInt("payment_terms"));
                    salesOrder.setOrderStatus(SalesOrder.SalesOrderStatus.valueOf(resultSet.getString("order_status")));
                    salesOrder.setTotalAmount(resultSet.getDouble("total_amount"));
                    salesOrder.setSalesType(operationDAO.getOperationById(resultSet.getInt("sales_type")));
                    salesOrder.setInvoiceType(salesInvoiceTypeDAO.getSalesInvoiceTypeById(resultSet.getInt("receipt_type")));
                    salesOrder.setDiscountAmount(resultSet.getDouble("discount_amount"));
                    salesOrder.setNetAmount(resultSet.getDouble("net_amount"));
                    salesOrder.setCreatedBy(employeeDAO.getUserById(resultSet.getInt("created_by")));
                    salesOrder.setCreatedDate(resultSet.getTimestamp("created_date"));
                    salesOrder.setModifiedBy(employeeDAO.getUserById(resultSet.getInt("modified_by")));
                    salesOrder.setModifiedDate(resultSet.getTimestamp("modified_date"));
                    salesOrder.setPostedBy(employeeDAO.getUserById(resultSet.getInt("posted_by")));
                    salesOrder.setPostedDate(resultSet.getTimestamp("posted_date"));
                    salesOrder.setRemarks(resultSet.getString("remarks"));
                    salesOrder.setIsDelivered(resultSet.getBoolean("isDelivered"));
                    salesOrder.setIsCancelled(resultSet.getBoolean("isCancelled"));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return salesOrder;
    }

    // Delete a Sales Order (optional)
    public boolean deleteSalesOrder(int orderId) {
        String query = "DELETE FROM sales_order WHERE order_id = ?";

        try (Connection connection = dataSource.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, orderId);
            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }
}
