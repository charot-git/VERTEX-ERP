package com.vertex.vos.Utilities;

import com.vertex.vos.DAO.SalesInvoiceDAO;
import com.vertex.vos.DAO.SalesInvoiceTypeDAO;
import com.vertex.vos.Enums.SalesOrderStatus;
import com.vertex.vos.Objects.*;
import com.zaxxer.hikari.HikariDataSource;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        String orderQuery = "INSERT INTO sales_order (order_no, po_no, branch_id, customer_code, salesman_id, order_date, delivery_date, due_date, payment_terms, " +
                "order_status, total_amount, sales_type, receipt_type, discount_amount, net_amount, created_by, created_date, modified_by, modified_date, " +
                "posted_by, posted_date, remarks, isDelivered, supplier_id, for_approval_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        String detailQuery = "INSERT INTO sales_order_details (product_id, order_id, unit_price, ordered_quantity, served_quantity, discount_type, " +
                "discount_amount, gross_amount, net_amount, remarks) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false); // Start transaction

            try (PreparedStatement orderStmt = connection.prepareStatement(orderQuery, Statement.RETURN_GENERATED_KEYS)) {
                // Insert into sales_order
                orderStmt.setString(1, salesOrder.getOrderNo());
                orderStmt.setString(2, salesOrder.getPurchaseNo()); // Added po_no
                orderStmt.setInt(3, salesOrder.getBranch().getId());
                orderStmt.setString(4, salesOrder.getCustomer().getCustomerCode());
                orderStmt.setInt(5, salesOrder.getSalesman() != null ? salesOrder.getSalesman().getId() : 0);
                orderStmt.setDate(6, salesOrder.getOrderDate());
                orderStmt.setTimestamp(7, salesOrder.getDeliveryDate());
                orderStmt.setTimestamp(8, salesOrder.getDueDate());
                orderStmt.setInt(9, salesOrder.getPaymentTerms());
                orderStmt.setString(10, salesOrder.getOrderStatus().getDbValue());
                orderStmt.setDouble(11, salesOrder.getTotalAmount());
                orderStmt.setInt(12, salesOrder.getSalesType() != null ? salesOrder.getSalesType().getId() : 0);
                orderStmt.setInt(13, salesOrder.getInvoiceType() != null ? salesOrder.getInvoiceType().getId() : 0);
                orderStmt.setDouble(14, salesOrder.getDiscountAmount());
                orderStmt.setDouble(15, salesOrder.getNetAmount());
                orderStmt.setInt(16, salesOrder.getCreatedBy() != null ? salesOrder.getCreatedBy().getUser_id() : 0);
                orderStmt.setTimestamp(17, salesOrder.getCreatedDate());
                orderStmt.setObject(18, salesOrder.getModifiedBy() != null ? salesOrder.getModifiedBy().getUser_id() : null, java.sql.Types.INTEGER);
                orderStmt.setTimestamp(19, salesOrder.getModifiedDate());
                orderStmt.setObject(20, salesOrder.getPostedBy() != null ? salesOrder.getPostedBy().getUser_id() : null, java.sql.Types.INTEGER);
                orderStmt.setTimestamp(21, salesOrder.getPostedDate());
                orderStmt.setString(22, salesOrder.getRemarks());
                orderStmt.setBoolean(23, salesOrder.getIsDelivered() != null && salesOrder.getIsDelivered());
                orderStmt.setInt(24, salesOrder.getSupplier() != null ? salesOrder.getSupplier().getId() : 0);
                orderStmt.setTimestamp(25, salesOrder.getForApprovalAt());

                int rowsAffected = orderStmt.executeUpdate();
                if (rowsAffected == 0) {
                    connection.rollback();
                    return false;
                }

                // Retrieve generated order_id
                int orderId;
                try (ResultSet generatedKeys = orderStmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        orderId = generatedKeys.getInt(1);
                    } else {
                        connection.rollback();
                        return false;
                    }
                }

                // Insert into sales_order_details
                try (PreparedStatement detailStmt = connection.prepareStatement(detailQuery)) {
                    for (SalesOrderDetails detail : salesOrder.getSalesOrderDetails()) {
                        detailStmt.setInt(1, detail.getProduct().getProductId());
                        detailStmt.setInt(2, orderId);
                        detailStmt.setDouble(3, detail.getUnitPrice());
                        detailStmt.setInt(4, detail.getOrderedQuantity());
                        detailStmt.setInt(5, detail.getServedQuantity());
                        detailStmt.setObject(6, detail.getDiscountType() != null ? detail.getDiscountType().getId() : null);
                        detailStmt.setObject(7, detail.getDiscountAmount());
                        detailStmt.setDouble(8, detail.getGrossAmount());
                        detailStmt.setDouble(9, detail.getNetAmount());
                        detailStmt.setString(10, detail.getRemarks());

                        detailStmt.addBatch(); // Add to batch
                    }
                    detailStmt.executeBatch(); // Execute batch insert
                }

                connection.commit(); // Commit transaction
                return true;
            } catch (SQLException e) {
                connection.rollback(); // Rollback on error
                e.printStackTrace();
            } finally {
                connection.setAutoCommit(true); // Restore default behavior
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    OperationDAO operationDAO = new OperationDAO();
    EmployeeDAO employeeDAO = new EmployeeDAO();

    // Get all Sales Orders
    public ObservableList<SalesOrder> getAllSalesOrders(int pageNumber, int rowsPerPage, Branch branch, String orderNoFilter, String purchaseNo, Customer customer, Salesman salesman, Supplier supplier, SalesOrderStatus statusFilter, Timestamp orderDateFromFilter, Timestamp orderDateToFilter) {

        ObservableList<SalesOrder> salesOrders = FXCollections.observableArrayList();
        StringBuilder sqlQuery = new StringBuilder("SELECT * FROM sales_order WHERE 1 = 1");
        List<Object> params = new ArrayList<>();

        // Add conditions based on input parameters
        if (orderNoFilter != null && !orderNoFilter.trim().isEmpty()) {
            sqlQuery.append(" AND order_no LIKE ?");
            params.add("%" + orderNoFilter.trim() + "%");
        }

        if (purchaseNo != null && !purchaseNo.trim().isEmpty()) {
            sqlQuery.append(" AND po_no LIKE ?");
            params.add("%" + purchaseNo.trim() + "%");
        }

        if (branch != null) {
            sqlQuery.append(" AND branch_id = ?");
            params.add(branch.getId());
        }
        if (customer != null) {
            sqlQuery.append(" AND customer_code LIKE ?");
            params.add("%" + customer.getCustomerCode() + "%");
        }
        if (salesman != null) {
            sqlQuery.append(" AND salesman_id = ?");
            params.add(salesman.getId());
        }

        if (supplier != null) {
            sqlQuery.append(" AND supplier_id = ?");
            params.add(supplier.getId());
        }
        if (statusFilter != null) {
            sqlQuery.append(" AND order_status = ?");
            params.add(statusFilter.getDbValue());
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
                    salesOrder.setPurchaseNo(rs.getString("po_no"));
                    salesOrder.setCustomer(customerDAO.getCustomerByCode(rs.getString("customer_code")));
                    salesOrder.setSalesman(salesmanDAO.getSalesmanDetails(rs.getInt("salesman_id")));
                    salesOrder.setOrderDate(rs.getDate("order_date"));
                    salesOrder.setDeliveryDate(rs.getTimestamp("delivery_date"));
                    salesOrder.setSupplier(supplierDAO.getSupplierById(rs.getInt("supplier_id")));
                    salesOrder.setBranch(branchDAO.getBranchById(rs.getInt("branch_id")));
                    salesOrder.setDueDate(rs.getTimestamp("due_date"));
                    salesOrder.setPaymentTerms(rs.getInt("payment_terms"));
                    salesOrder.setOrderStatus(SalesOrderStatus.fromDbValue(rs.getString("order_status")));
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
                    salesOrder.setPurchaseNo(resultSet.getString("po_no"));
                    salesOrder.setSupplier(supplierDAO.getSupplierById(resultSet.getInt("supplier_id")));
                    salesOrder.setSalesman(salesmanDAO.getSalesmanDetails(resultSet.getInt("salesman_id")));
                    salesOrder.setOrderDate(resultSet.getDate("order_date"));
                    salesOrder.setDeliveryDate(resultSet.getTimestamp("delivery_date"));
                    salesOrder.setDueDate(resultSet.getTimestamp("due_date"));
                    salesOrder.setPaymentTerms(resultSet.getInt("payment_terms"));
                    salesOrder.setOrderStatus(SalesOrderStatus.fromDbValue(resultSet.getString("order_status")));
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

    ProductDAO productDAO = new ProductDAO();
    DiscountDAO discountDAO = new DiscountDAO();

    public ObservableList<SalesOrderDetails> getSalesOrderDetails(SalesOrder selectedItem) {
        ObservableList<SalesOrderDetails> salesOrderDetailsList = FXCollections.observableArrayList();
        String query = "SELECT * FROM sales_order_details WHERE order_id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, selectedItem.getOrderId());

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    SalesOrderDetails details = new SalesOrderDetails();
                    details.setProduct(productDAO.getProductById(resultSet.getInt("product_id")));
                    details.setUnitPrice(resultSet.getDouble("unit_price"));
                    details.setSalesOrder(selectedItem);
                    details.setOrderedQuantity(resultSet.getInt("ordered_quantity"));
                    details.setServedQuantity(resultSet.getInt("served_quantity"));
                    details.setDiscountType(discountDAO.getDiscountTypeById(resultSet.getInt("discount_type")));
                    details.setDiscountAmount(resultSet.getDouble("discount_amount"));
                    details.setGrossAmount(resultSet.getDouble("gross_amount"));
                    details.setNetAmount(resultSet.getDouble("net_amount"));
                    details.setRemarks(resultSet.getString("remarks"));
                    salesOrderDetailsList.add(details);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return salesOrderDetailsList;
    }

    public boolean updateSalesOrder(SalesOrder salesOrder) {
        String query = "UPDATE sales_order SET order_no = ?, po_no = ?, branch_id = ?, customer_code = ?, salesman_id = ?, order_date = ?, " +
                "delivery_date = ?, due_date = ?, payment_terms = ?, order_status = ?, total_amount = ?, sales_type = ?, receipt_type = ?, " +
                "discount_amount = ?, net_amount = ?, created_by = ?, created_date = ?, modified_by = ?, modified_date = ?, posted_by = ?, " +
                "posted_date = ?, remarks = ?, isDelivered = ?, isCancelled = ? WHERE order_id = ?";

        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false); // Start transaction

            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, salesOrder.getOrderNo());
                preparedStatement.setString(2, salesOrder.getPurchaseNo());
                preparedStatement.setObject(3, salesOrder.getBranch() != null ? salesOrder.getBranch().getId() : null, java.sql.Types.INTEGER);
                preparedStatement.setString(4, salesOrder.getCustomer() != null ? salesOrder.getCustomer().getCustomerCode() : null);
                preparedStatement.setObject(5, salesOrder.getSalesman() != null ? salesOrder.getSalesman().getId() : null, java.sql.Types.INTEGER);
                preparedStatement.setDate(6, salesOrder.getOrderDate());
                preparedStatement.setTimestamp(7, salesOrder.getDeliveryDate());
                preparedStatement.setTimestamp(8, salesOrder.getDueDate());
                preparedStatement.setObject(9, salesOrder.getPaymentTerms(), java.sql.Types.INTEGER);
                preparedStatement.setString(10, salesOrder.getOrderStatus() != null ? salesOrder.getOrderStatus().getDbValue() : null);
                preparedStatement.setObject(11, salesOrder.getTotalAmount(), java.sql.Types.DOUBLE);
                preparedStatement.setObject(12, salesOrder.getSalesType() != null ? salesOrder.getSalesType().getId() : null, java.sql.Types.INTEGER);
                preparedStatement.setObject(13, salesOrder.getInvoiceType() != null ? salesOrder.getInvoiceType().getId() : null, java.sql.Types.INTEGER);
                preparedStatement.setObject(14, salesOrder.getDiscountAmount(), java.sql.Types.DOUBLE);
                preparedStatement.setObject(15, salesOrder.getNetAmount(), java.sql.Types.DOUBLE);
                preparedStatement.setObject(16, salesOrder.getCreatedBy() != null ? salesOrder.getCreatedBy().getUser_id() : null, java.sql.Types.INTEGER);
                preparedStatement.setTimestamp(17, salesOrder.getCreatedDate());
                preparedStatement.setObject(18, salesOrder.getModifiedBy() != null ? salesOrder.getModifiedBy().getUser_id() : null, java.sql.Types.INTEGER);
                preparedStatement.setTimestamp(19, salesOrder.getModifiedDate());
                preparedStatement.setObject(20, salesOrder.getPostedBy() != null ? salesOrder.getPostedBy().getUser_id() : null, java.sql.Types.INTEGER);
                preparedStatement.setTimestamp(21, salesOrder.getPostedDate());
                preparedStatement.setString(22, salesOrder.getRemarks());
                preparedStatement.setObject(23, salesOrder.getIsDelivered(), java.sql.Types.BOOLEAN);
                preparedStatement.setObject(24, salesOrder.getIsCancelled(), java.sql.Types.BOOLEAN);
                preparedStatement.setInt(25, salesOrder.getOrderId());

                int rowsAffected = preparedStatement.executeUpdate();
                if (rowsAffected == 0) {
                    System.out.println("No sales order updated: " + salesOrder.getOrderNo());
                    connection.rollback();
                    return false;
                }
            }

// Update sales order details
            List<SalesOrderDetails> oldDetails = getSalesOrderDetails(salesOrder);
            List<SalesOrderDetails> newDetails = salesOrder.getSalesOrderDetails();

// Convert old details into a map for quick lookup
            Map<Integer, SalesOrderDetails> oldDetailsMap = oldDetails.stream()
                    .collect(Collectors.toMap(d -> d.getProduct().getProductId(), d -> d));

// Add or update details
            for (SalesOrderDetails newDetail : newDetails) {
                int productId = newDetail.getProduct().getProductId();

                if (!oldDetailsMap.containsKey(productId)) {
                    addSalesOrderDetail(connection, salesOrder, newDetail); // Add new
                } else {
                    SalesOrderDetails oldDetail = oldDetailsMap.get(productId);
                    if (!newDetail.equals(oldDetail)) {
                        updateSalesOrderDetail(connection, salesOrder, newDetail); // Update if changed
                    }
                    oldDetailsMap.remove(productId); // Remove from map (not to be deleted)
                }
            }

            for (SalesOrderDetails removedDetail : oldDetailsMap.values()) {
                deleteSalesOrderDetail(connection, salesOrder.getOrderId(), removedDetail.getProduct().getProductId());
            }

            connection.commit(); // Commit all changes
            System.out.println("Updated sales order: " + salesOrder.getOrderNo());
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
            return false;
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true); // Restore default
                    connection.close();
                } catch (SQLException closeEx) {
                    closeEx.printStackTrace();
                }
            }
        }
    }

    private void updateSalesOrderDetail(Connection connection, SalesOrder salesOrder, SalesOrderDetails newDetail) throws SQLException {
        String query = "UPDATE sales_order_details SET unit_price = ?, ordered_quantity = ?, served_quantity = ?, discount_type = ?, " +
                "discount_amount = ?, gross_amount = ?, net_amount = ?, remarks = ? " +
                "WHERE order_id = ? AND product_id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setDouble(1, newDetail.getUnitPrice());
            preparedStatement.setInt(2, newDetail.getOrderedQuantity());
            preparedStatement.setInt(3, newDetail.getServedQuantity());
            preparedStatement.setObject(4, newDetail.getDiscountType() != null ? newDetail.getDiscountType().getId() : null, java.sql.Types.INTEGER);
            preparedStatement.setDouble(5, newDetail.getDiscountAmount());
            preparedStatement.setDouble(6, newDetail.getGrossAmount());
            preparedStatement.setDouble(7, newDetail.getNetAmount());
            preparedStatement.setString(8, newDetail.getRemarks());
            preparedStatement.setInt(9, salesOrder.getOrderId());
            preparedStatement.setInt(10, newDetail.getProduct().getProductId());

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Updated sales order detail: " + newDetail.getProduct().getProductName());
            } else {
                System.out.println("No sales order detail updated: " + newDetail.getProduct().getProductName());
            }
        }
    }

    private void addSalesOrderDetail(Connection connection, SalesOrder salesOrder, SalesOrderDetails salesOrderDetail) throws SQLException {
        String query = "INSERT INTO sales_order_details (order_id, product_id, unit_price, ordered_quantity, served_quantity, discount_type, " +
                "discount_amount, gross_amount, net_amount, remarks) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, salesOrder.getOrderId());
            preparedStatement.setInt(2, salesOrderDetail.getProduct().getProductId());
            preparedStatement.setDouble(3, salesOrderDetail.getUnitPrice());
            preparedStatement.setInt(4, salesOrderDetail.getOrderedQuantity());
            preparedStatement.setInt(5, salesOrderDetail.getServedQuantity());
            preparedStatement.setObject(6, salesOrderDetail.getDiscountType() != null ? salesOrderDetail.getDiscountType().getId() : null, java.sql.Types.INTEGER);
            preparedStatement.setDouble(7, salesOrderDetail.getDiscountAmount());
            preparedStatement.setDouble(8, salesOrderDetail.getGrossAmount());
            preparedStatement.setDouble(9, salesOrderDetail.getNetAmount());
            preparedStatement.setString(10, salesOrderDetail.getRemarks());

            int rowsAffected = preparedStatement.executeUpdate();
            System.out.println(rowsAffected > 0 ? "Added sales order detail: " + salesOrderDetail.getProduct().getProductName()
                    : "No sales order detail added: " + salesOrderDetail.getProduct().getProductName());
        }
    }

    private void deleteSalesOrderDetail(Connection connection, int orderId, int productId) throws SQLException {
        String query = "DELETE FROM sales_order_details WHERE order_id = ? AND product_id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, orderId);
            preparedStatement.setInt(2, productId);

            int rowsAffected = preparedStatement.executeUpdate();
            System.out.println(rowsAffected > 0 ? "Deleted sales order detail: " + productId
                    : "No sales order detail deleted: " + productId);
        }
    }


    public boolean approveSalesOrder(SalesOrder selectedItem) {
        if (selectedItem == null) {
            DialogUtils.showErrorMessage("Error", "Invalid Sales Order");
            return false;
        }

        if (!(selectedItem.getOrderStatus() == SalesOrderStatus.FOR_APPROVAL ||
                selectedItem.getOrderStatus() == SalesOrderStatus.ON_HOLD)) {
            DialogUtils.showErrorMessage("Error", "Current status is already " + selectedItem.getOrderStatus().getDbValue());
            return false;
        }

        String query = "UPDATE sales_order SET order_status = ?, modified_by = ?, modified_date = ?, due_date = ? WHERE order_id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            connection.setAutoCommit(false); // Disable auto-commit

            preparedStatement.setString(1, SalesOrderStatus.FOR_CONSOLIDATION.getDbValue());
            preparedStatement.setInt(2, UserSession.getInstance().getUser().getUser_id());
            preparedStatement.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            preparedStatement.setTimestamp(4, selectedItem.getDueDate());
            preparedStatement.setInt(5, selectedItem.getOrderId());

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                connection.commit();
                return true;
            } else {
                connection.rollback();
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean holdSalesOrder(SalesOrder selectedItem) {
        if (selectedItem == null) {
            DialogUtils.showErrorMessage("Error", "Invalid Sales Order");
            return false;
        }

        if (selectedItem.getOrderStatus() != SalesOrderStatus.FOR_APPROVAL) {
            DialogUtils.showErrorMessage("Error", "Current status is already " + selectedItem.getOrderStatus().getDbValue());
            return false;
        }

        String query = "UPDATE sales_order SET order_status = ?, modified_by = ?, modified_date = ? WHERE order_id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            connection.setAutoCommit(false);

            preparedStatement.setString(1, SalesOrderStatus.ON_HOLD.getDbValue());
            preparedStatement.setInt(2, UserSession.getInstance().getUser().getUser_id());
            preparedStatement.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            preparedStatement.setInt(4, selectedItem.getOrderId());

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                connection.commit();
                System.out.println("Sales order placed on hold: " + selectedItem.getOrderNo());
                return true;
            } else {
                connection.rollback();
                System.out.println("Failed to place sales order on hold: " + selectedItem.getOrderNo());
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    SalesInvoiceDAO salesInvoiceDAO = new SalesInvoiceDAO();

    public boolean updateSalesOrderWithConnection(SalesOrder salesOrder, Connection connection) throws SQLException {
        String query = "UPDATE sales_order SET order_no = ?, branch_id = ?, customer_code = ?, salesman_id = ?, order_date = ?, " +
                "delivery_date = ?, due_date = ?, payment_terms = ?, order_status = ?, total_amount = ?, sales_type = ?, receipt_type = ?, " +
                "discount_amount = ?, net_amount = ?, created_by = ?, created_date = ?, modified_by = ?, modified_date = ?, posted_by = ?, " +
                "posted_date = ?, remarks = ?, isDelivered = ?, isCancelled = ? WHERE order_id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, salesOrder.getOrderNo());
            preparedStatement.setObject(2, salesOrder.getBranch() != null ? salesOrder.getBranch().getId() : null, java.sql.Types.INTEGER);
            preparedStatement.setString(3, salesOrder.getCustomer() != null ? salesOrder.getCustomer().getCustomerCode() : null);
            preparedStatement.setObject(4, salesOrder.getSalesman() != null ? salesOrder.getSalesman().getId() : null, java.sql.Types.INTEGER);
            preparedStatement.setDate(5, salesOrder.getOrderDate());
            preparedStatement.setTimestamp(6, salesOrder.getDeliveryDate());
            preparedStatement.setTimestamp(7, salesOrder.getDueDate());
            preparedStatement.setObject(8, salesOrder.getPaymentTerms(), java.sql.Types.INTEGER);
            preparedStatement.setString(9, salesOrder.getOrderStatus() != null ? salesOrder.getOrderStatus().getDbValue() : null);
            preparedStatement.setObject(10, salesOrder.getTotalAmount(), java.sql.Types.DOUBLE);
            preparedStatement.setObject(11, salesOrder.getSalesType() != null ? salesOrder.getSalesType().getId() : null, java.sql.Types.INTEGER);
            preparedStatement.setObject(12, salesOrder.getInvoiceType() != null ? salesOrder.getInvoiceType().getId() : null, java.sql.Types.INTEGER);
            preparedStatement.setObject(13, salesOrder.getDiscountAmount(), java.sql.Types.DOUBLE);
            preparedStatement.setObject(14, salesOrder.getNetAmount(), java.sql.Types.DOUBLE);
            preparedStatement.setObject(15, salesOrder.getCreatedBy() != null ? salesOrder.getCreatedBy().getUser_id() : null, java.sql.Types.INTEGER);
            preparedStatement.setTimestamp(16, salesOrder.getCreatedDate());
            preparedStatement.setObject(17, salesOrder.getModifiedBy() != null ? salesOrder.getModifiedBy().getUser_id() : null, java.sql.Types.INTEGER);
            preparedStatement.setTimestamp(18, salesOrder.getModifiedDate());
            preparedStatement.setObject(19, salesOrder.getPostedBy() != null ? salesOrder.getPostedBy().getUser_id() : null, java.sql.Types.INTEGER);
            preparedStatement.setTimestamp(20, salesOrder.getPostedDate());
            preparedStatement.setString(21, salesOrder.getRemarks());
            preparedStatement.setObject(22, salesOrder.getIsDelivered(), java.sql.Types.BOOLEAN);
            preparedStatement.setObject(23, salesOrder.getIsCancelled(), java.sql.Types.BOOLEAN);
            preparedStatement.setInt(24, salesOrder.getOrderId());

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected == 0) {
                System.out.println("No sales order updated: " + salesOrder.getOrderNo());
                return false;
            }
        }

        return true;
    }


    public boolean convertSalesOrder(SalesOrder salesOrder, ObservableList<SalesInvoiceHeader> salesInvoiceHeaders) {
        salesOrder.setOrderStatus(SalesOrderStatus.FOR_LOADING);
        salesOrder.setModifiedBy(UserSession.getInstance().getUser());
        salesOrder.setModifiedDate(Timestamp.valueOf(LocalDateTime.now()));

        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false); // Start transaction

            // Update Sales Order (with transaction handling)
            if (!updateSalesOrderWithConnection(salesOrder, connection)) {
                connection.rollback();
                return false;
            }

            // Convert Sales Order into Invoices
            for (SalesInvoiceHeader salesInvoiceHeader : salesInvoiceHeaders) {
                SalesInvoiceHeader invoiceCreated = salesInvoiceDAO.createSalesInvoiceWithDetails(
                        salesInvoiceHeader, salesInvoiceHeader.getSalesInvoiceDetails(), null, null, connection);

                if (invoiceCreated.getInvoiceId() == -1) {
                    connection.rollback(); // Rollback everything on failure
                    System.out.println("Failed to create Sales Invoice for Order: " + salesOrder.getOrderNo());
                    return false;
                }
            }

            // **Update Sales Order Details Quantities after successful invoice creation**
            if (!updateSalesOrderDetailsQuantities(salesOrder, connection)) {
                connection.rollback(); // Rollback if update fails
                System.out.println("Failed to update served quantities for Order: " + salesOrder.getOrderNo());
                return false;
            }

            connection.commit(); // Commit only if everything succeeds
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            if (connection != null) {
                try {
                    connection.rollback(); // Rollback on error
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
            return false;

        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException closeEx) {
                    closeEx.printStackTrace();
                }
            }
        }
    }


    private boolean updateSalesOrderDetailsQuantities(SalesOrder salesOrder, Connection connection) {
        String updateSQL = "UPDATE sales_order_details " +
                "SET served_quantity = ? " +
                "WHERE order_id = ? AND product_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(updateSQL)) {
            for (SalesOrderDetails detail : salesOrder.getSalesOrderDetails()) {
                stmt.setInt(1, detail.getServedQuantity()); // Update with exact served quantity
                stmt.setInt(2, salesOrder.getOrderId()); // Order ID
                stmt.setInt(3, detail.getProduct().getProductId()); // Product ID
                stmt.addBatch();
            }

            int[] affectedRows = stmt.executeBatch();
            for (int rows : affectedRows) {
                if (rows == 0) {
                    return false; // If any update fails, return false
                }
            }

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    public boolean pickSalesOrder(SalesOrder salesOrder) {
        if (salesOrder == null) {
            DialogUtils.showErrorMessage("Error", "Invalid Sales Order");
            return false;
        }

        if (salesOrder.getOrderStatus() != SalesOrderStatus.FOR_CONSOLIDATION) {
            DialogUtils.showErrorMessage("Error", "Current status is already " + salesOrder.getOrderStatus().getDbValue());
            return false;
        }

        String query = "UPDATE sales_order SET order_status = ?, modified_by = ?, modified_date = ? WHERE order_id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            connection.setAutoCommit(false);

            preparedStatement.setString(1, SalesOrderStatus.FOR_INVOICING.getDbValue());
            preparedStatement.setInt(2, UserSession.getInstance().getUser().getUser_id());
            preparedStatement.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            preparedStatement.setInt(4, salesOrder.getOrderId());

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                connection.commit();
                System.out.println("Sales order picked: " + salesOrder.getOrderNo());
                return true;
            } else {
                connection.rollback();
                System.out.println("Failed to pick sales order: " + salesOrder.getOrderNo());
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
