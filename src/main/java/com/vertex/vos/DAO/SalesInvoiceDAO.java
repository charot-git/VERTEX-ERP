package com.vertex.vos.DAO;

import com.vertex.vos.Objects.*;
import com.vertex.vos.Utilities.*;
import com.zaxxer.hikari.HikariDataSource;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDate;
import java.util.*;
import java.util.logging.Logger;
import java.sql.*;
import java.util.stream.Collectors;

public class SalesInvoiceDAO {
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();
    private static final Logger LOGGER = Logger.getLogger(SalesInvoiceDAO.class.getName());

    public SalesInvoiceHeader createSalesInvoiceWithDetails(
            SalesInvoiceHeader invoice,
            List<SalesInvoiceDetail> salesInvoiceDetails,
            ObservableList<SalesInvoiceDetail> deletedSalesInvoiceDetails,
            SalesReturn salesReturn, Connection connection) {

        if (invoice == null || salesInvoiceDetails == null || connection == null) {
            throw new IllegalArgumentException("Invoice, salesInvoiceDetails, and connection cannot be null.");
        }

        System.out.println("Starting createSalesInvoiceWithDetails method...");

        String sqlQueryHeader = "INSERT INTO sales_invoice " +
                "(order_id, customer_code, salesman_id, invoice_date, dispatch_date, due_date, " +
                "payment_terms, transaction_status, payment_status, total_amount, sales_type, " +
                "invoice_type, price_type, invoice_no, vat_amount, discount_amount, net_amount, " +
                "created_by, created_date, modified_by, modified_date, posted_by, posted_date, " +
                "remarks, isReceipt, isPosted, isDispatched, gross_amount) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "order_id = VALUES(order_id), customer_code = VALUES(customer_code), salesman_id = VALUES(salesman_id), " +
                "invoice_date = VALUES(invoice_date), dispatch_date = VALUES(dispatch_date), " +
                "due_date = VALUES(due_date), payment_terms = VALUES(payment_terms), " +
                "transaction_status = VALUES(transaction_status), payment_status = VALUES(payment_status), " +
                "total_amount = VALUES(total_amount), sales_type = VALUES(sales_type), " +
                "invoice_type = VALUES(invoice_type), price_type = VALUES(price_type), " +
                "invoice_no = VALUES(invoice_no), vat_amount = VALUES(vat_amount), " +
                "discount_amount = VALUES(discount_amount), net_amount = VALUES(net_amount), " +
                "modified_by = VALUES(modified_by), modified_date = NOW(), " +
                "posted_by = VALUES(posted_by), posted_date = VALUES(posted_date), " +
                "remarks = VALUES(remarks), isReceipt = VALUES(isReceipt), isPosted = VALUES(isPosted), " +
                "isDispatched = VALUES(isDispatched), gross_amount = VALUES(gross_amount)";

        boolean autoCommitState = false;
        try {
            autoCommitState = connection.getAutoCommit();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        try (PreparedStatement statementHeader = connection.prepareStatement(sqlQueryHeader, Statement.RETURN_GENERATED_KEYS)) {
            connection.setAutoCommit(false); // Begin transaction

            // Remove deleted sales invoice details if necessary
            if (deletedSalesInvoiceDetails != null && !deletedSalesInvoiceDetails.isEmpty()) {
                System.out.println("Deleting removed sales invoice details...");
                if (!removeSalesInvoiceDetails(deletedSalesInvoiceDetails, connection)) {
                    connection.rollback();
                    throw new SQLException("Failed to delete invoice details. Transaction rolled back.");
                }
            }

            // Prepare and execute sales invoice header insert/update
            statementHeader.setString(1, invoice.getOrderId());
            statementHeader.setString(2, invoice.getCustomer().getCustomerCode());
            statementHeader.setInt(3, invoice.getSalesman().getId());
            statementHeader.setTimestamp(4, invoice.getInvoiceDate());
            statementHeader.setTimestamp(5, invoice.getDispatchDate());
            statementHeader.setTimestamp(6, invoice.getDueDate());
            statementHeader.setInt(7, invoice.getPaymentTerms());
            statementHeader.setString(8, invoice.getTransactionStatus());
            statementHeader.setString(9, invoice.getPaymentStatus());
            statementHeader.setDouble(10, invoice.getTotalAmount());
            statementHeader.setInt(11, invoice.getSalesType());
            statementHeader.setInt(12, invoice.getInvoiceType().getId());
            statementHeader.setString(13, String.valueOf(invoice.getPriceType()));
            statementHeader.setString(14, invoice.getInvoiceNo());
            statementHeader.setDouble(15, invoice.getVatAmount());
            statementHeader.setDouble(16, invoice.getDiscountAmount());
            statementHeader.setDouble(17, invoice.getNetAmount());

// Allow NULLs for user-based columns
            statementHeader.setObject(18, invoice.getCreatedBy() == 0 ? null : invoice.getCreatedBy(), Types.INTEGER);
            statementHeader.setTimestamp(19, invoice.getCreatedDate());
            statementHeader.setObject(20, invoice.getModifiedBy() == 0 ? null : invoice.getModifiedBy(), Types.INTEGER);
            statementHeader.setTimestamp(21, invoice.getModifiedDate());
            statementHeader.setObject(22, invoice.getPostedBy() == 0 ? null : invoice.getPostedBy(), Types.INTEGER);
            statementHeader.setTimestamp(23, invoice.getPostedDate());

            statementHeader.setString(24, invoice.getRemarks());
            statementHeader.setBoolean(25, invoice.isReceipt());
            statementHeader.setBoolean(26, invoice.isPosted());
            statementHeader.setBoolean(27, invoice.isDispatched());
            statementHeader.setDouble(28, invoice.getGrossAmount());

            int rowsInserted = statementHeader.executeUpdate();
            System.out.println("Rows affected: " + rowsInserted);

            int invoiceId;
            try (ResultSet generatedKeys = statementHeader.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    invoiceId = generatedKeys.getInt(1);
                    System.out.println("Generated Invoice ID: " + invoiceId);
                } else {
                    // Fetch invoice ID manually if it was an update
                    String fetchInvoiceIdQuery = "SELECT invoice_id FROM sales_invoice WHERE invoice_no = ?";
                    try (PreparedStatement fetchStmt = connection.prepareStatement(fetchInvoiceIdQuery)) {
                        fetchStmt.setString(1, invoice.getInvoiceNo());
                        try (ResultSet rs = fetchStmt.executeQuery()) {
                            if (rs.next()) {
                                invoiceId = rs.getInt("invoice_id");
                                System.out.println("Fetched existing Invoice ID: " + invoiceId);
                            } else {
                                connection.rollback();
                                throw new SQLException("Failed to retrieve invoice ID.");
                            }
                        }
                    }
                }
            }
            invoice.setInvoiceId(invoiceId);

            // Insert sales invoice details if present
            if (!salesInvoiceDetails.isEmpty()) {
                if (!createSalesInvoiceDetailsBulk(invoiceId, salesInvoiceDetails, connection)) {
                    connection.rollback();
                    throw new SQLException("Failed to insert sales invoice details. Transaction rolled back.");
                }
            }

            if (salesReturn != null) {
                if (!linkSalesInvoiceSalesReturn(invoice, salesReturn, connection)) {
                    connection.rollback();
                    throw new SQLException("Failed to link sales invoice and sales return. Transaction rolled back.");
                }
            }

            connection.commit(); // Commit the transaction
        } catch (SQLException ex) {
            try {
                connection.rollback();
                System.err.println("Transaction rolled back due to error: " + ex.getMessage());
            } catch (SQLException rollbackEx) {
                System.err.println("Failed to rollback transaction: " + rollbackEx.getMessage());
            }
            throw new RuntimeException("Database error: " + ex.getMessage(), ex);
        } finally {
            try {
                connection.setAutoCommit(autoCommitState);
            } catch (SQLException e) {
                System.err.println("Failed to restore auto-commit: " + e.getMessage());
            }
            System.out.println("Transaction completed, auto-commit restored.");
        }

        return invoice;
    }


    public List<String> salesInvoiceNumbers() throws SQLException {
        List<String> invoiceNumbers = new ArrayList<>();
        String sql = "SELECT invoice_no FROM sales_invoice";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String invoiceNo = rs.getString("invoice_no");
                invoiceNumbers.add(invoiceNo);
            }
        }

        return invoiceNumbers;
    }

    private boolean createSalesInvoiceDetailsBulk(int invoiceId, List<SalesInvoiceDetail> salesInvoiceDetails, Connection connection) throws SQLException {
        String sqlQueryDetails = "INSERT INTO sales_invoice_details " +
                "(order_id, invoice_no, discount_type, product_id, unit, unit_price, quantity, discount_amount, total_amount, created_date, modified_date, gross_amount) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW(), ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "quantity = VALUES(quantity), " +
                "discount_amount = VALUES(discount_amount), " +
                "total_amount = VALUES(total_amount), " +
                "unit_price = VALUES(unit_price), " +
                "gross_amount = VALUES(gross_amount), " +
                "discount_type = VALUES(discount_type), " +
                "modified_date = NOW()";

        try (PreparedStatement statement = connection.prepareStatement(sqlQueryDetails)) {
            for (SalesInvoiceDetail detail : salesInvoiceDetails) {
                statement.setString(1, detail.getOrderId());
                statement.setInt(2, invoiceId);
                statement.setObject(3, detail.getDiscountType() != null ? detail.getDiscountType().getId() : null, Types.INTEGER);
                statement.setInt(4, detail.getProduct().getProductId());
                statement.setInt(5, detail.getProduct().getUnitOfMeasurement());
                statement.setDouble(6, detail.getUnitPrice());
                statement.setInt(7, detail.getQuantity());
                statement.setDouble(8, detail.getDiscountAmount());
                statement.setDouble(9, detail.getTotalPrice());
                statement.setDouble(10, detail.getGrossAmount());

                statement.addBatch();
            }

            statement.executeBatch();
            return true;
        } catch (SQLException ex) {
            throw ex;
        }
    }


    private String prepareStatementForCreateSalesInvoice(String baseQuery) {
        // You can customize this method to dynamically generate the prepared statement
        // based on the actual columns you want to insert values for.
        return baseQuery;
    }

    ProductDAO productDAO = new ProductDAO();


    CustomerDAO customerDAO = new CustomerDAO();
    SalesmanDAO salesmanDAO = new SalesmanDAO();
    EmployeeDAO employeeDAO = new EmployeeDAO();

    //loadSalesInvoiceById
    public SalesInvoiceHeader loadSalesInvoiceById(int invoiceId) {
        String sqlQuery = "SELECT * FROM sales_invoice WHERE invoice_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sqlQuery)) {
            statement.setInt(1, invoiceId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToInvoice(resultSet);
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(java.util.logging.Level.SEVERE, "Error loading sales invoice by ID: " + ex.getMessage());
            throw new RuntimeException("Error loading sales invoice by ID", ex);
        }
        return null;
    }

    public ObservableList<SalesInvoiceHeader> loadSalesInvoices(
            String customerCode, String invoiceNo, Integer salesmanId, Integer salesType,
            Boolean isDispatched, Boolean isPaid, LocalDate fromDate, LocalDate toDate, int offset, int limit) {

        ObservableList<SalesInvoiceHeader> invoices = FXCollections.observableArrayList();

        StringBuilder sqlQuery = new StringBuilder("SELECT * FROM sales_invoice WHERE 1=1");
        List<Object> parameters = new ArrayList<>();

        if (customerCode != null && !customerCode.isEmpty()) {
            sqlQuery.append(" AND customer_code = ?");
            parameters.add(customerCode);
        }
        if (invoiceNo != null && !invoiceNo.isEmpty()) {
            sqlQuery.append(" AND invoice_no LIKE ?");
            parameters.add(invoiceNo + "%");
        }
        if (salesmanId != null) {
            sqlQuery.append(" AND salesman_id = ?");
            parameters.add(salesmanId);
        }
        if (salesType != null) {
            sqlQuery.append(" AND sales_type = ?");
            parameters.add(salesType);
        }
        if (isDispatched != null) {
            sqlQuery.append(" AND isDispatched = ?");
            parameters.add(isDispatched);
        }
        if (isPaid != null) {
            sqlQuery.append(" AND payment_status IN (?, ?)");
            parameters.add(isPaid ? "Paid" : "Unpaid");
            parameters.add(isPaid ? "Paid" : "Partially Paid");
        }

        // 🔹 Convert JavaFX DatePicker values to Timestamp
        if (fromDate != null) {
            sqlQuery.append(" AND invoice_date >= ?");
            parameters.add(Timestamp.valueOf(fromDate.atStartOfDay()));
        }
        if (toDate != null) {
            sqlQuery.append(" AND invoice_date <= ?");
            parameters.add(Timestamp.valueOf(toDate.atTime(23, 59, 59)));
        }

        sqlQuery.append(" ORDER BY invoice_date DESC LIMIT ?, ?");
        parameters.add(offset);
        parameters.add(limit);

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sqlQuery.toString())) {

            for (int i = 0; i < parameters.size(); i++) {
                Object param = parameters.get(i);
                if (param instanceof String) {
                    statement.setString(i + 1, (String) param);
                } else if (param instanceof Integer) {
                    statement.setInt(i + 1, (Integer) param);
                } else if (param instanceof Boolean) {
                    statement.setBoolean(i + 1, (Boolean) param);
                } else if (param instanceof Timestamp) {
                    statement.setTimestamp(i + 1, (Timestamp) param);
                }
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    SalesInvoiceHeader invoice = mapResultSetToInvoice(resultSet);
                    invoices.add(invoice);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return invoices;
    }

    SalesInvoiceTypeDAO salesInvoiceTypeDAO = new SalesInvoiceTypeDAO();

    public SalesInvoiceHeader mapResultSetToInvoice(ResultSet resultSet) throws SQLException {
        SalesInvoiceHeader invoice = new SalesInvoiceHeader();

        invoice.setInvoiceId(resultSet.getInt("invoice_id"));
        invoice.setOrderId(resultSet.getString("order_id"));
        invoice.setCustomerCode(resultSet.getString("customer_code"));
        Customer customer = customerDAO.getCustomerByCode(invoice.getCustomerCode());
        if (customer != null) {
            invoice.setCustomer(customer);
        }
        int salesmanId = resultSet.getInt("salesman_id");
        invoice.setSalesmanId(salesmanId);
        invoice.setSalesman(salesmanDAO.getSalesmanDetails(salesmanId));
        invoice.setInvoiceDate(resultSet.getTimestamp("invoice_date"));
        invoice.setDueDate(resultSet.getTimestamp("due_date"));
        invoice.setPostedDate(resultSet.getTimestamp("posted_date"));
        invoice.setModifiedDate(resultSet.getTimestamp("modified_date"));
        invoice.setDispatchDate(resultSet.getTimestamp("dispatch_date"));
        invoice.setDispatched(resultSet.getBoolean("isDispatched"));
        invoice.setPaymentTerms(resultSet.getInt("payment_terms"));
        invoice.setTransactionStatus(resultSet.getString("transaction_status"));
        invoice.setPriceType(resultSet.getString("price_type"));
        invoice.setPaymentStatus(resultSet.getString("payment_status"));
        invoice.setTotalAmount(resultSet.getDouble("total_amount"));
        invoice.setGrossAmount(resultSet.getDouble("gross_amount"));
        invoice.setVatAmount(resultSet.getDouble("vat_amount"));
        invoice.setDiscountAmount(resultSet.getDouble("discount_amount"));
        invoice.setNetAmount(resultSet.getDouble("net_amount"));
        invoice.setCreatedBy(resultSet.getInt("created_by"));
        invoice.setCreatedDate(resultSet.getTimestamp("created_date"));
        invoice.setModifiedBy(resultSet.getInt("modified_by"));
        invoice.setRemarks(resultSet.getString("remarks"));
        invoice.setInvoiceType(salesInvoiceTypeDAO.getSalesInvoiceTypeById(resultSet.getInt("invoice_type")));
        invoice.setReceipt(resultSet.getBoolean("isReceipt"));
        invoice.setSalesType(resultSet.getInt("sales_type"));
        invoice.setPostedBy(resultSet.getInt("posted_by"));
        invoice.setPosted(resultSet.getBoolean("isPosted"));
        invoice.setInvoiceNo(resultSet.getString("invoice_no"));
        invoice.setCustomerMemos(FXCollections.observableArrayList(customerMemoDAO.getCustomerMemoByInvoiceId(invoice)));
        return invoice;
    }

    CustomerMemoDAO customerMemoDAO = new CustomerMemoDAO();

    UnitDAO unitDAO = new UnitDAO();

    public ObservableList<ProductsInTransact> loadSalesInvoiceProducts(String orderId) {
        ObservableList<ProductsInTransact> products = FXCollections.observableArrayList();
        String sqlQuery = "SELECT sid.product_id, sid.invoice_no , p.description, sid.unit, sid.unit_price, sid.quantity, sid.total " +
                "FROM sales_invoice_details sid " +
                "INNER JOIN products p ON sid.product_id = p.product_id " +
                "WHERE sid.order_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sqlQuery)) {
            statement.setString(1, orderId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    ProductsInTransact product = new ProductsInTransact();
                    product.setInvoiceNo(resultSet.getString("invoice_no"));
                    product.setProductId(resultSet.getInt("product_id"));
                    product.setDescription(resultSet.getString("description"));
                    product.setUnit(unitDAO.getUnitNameById(resultSet.getInt("unit")));
                    product.setUnitPrice(resultSet.getBigDecimal("unit_price").doubleValue());
                    product.setOrderedQuantity(resultSet.getInt("quantity"));
                    product.setTotalAmount(resultSet.getBigDecimal("total").doubleValue());
                    products.add(product);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    public boolean paidOrder(Connection conn, SalesInvoiceHeader selectedInvoice) {
        String sqlQuery = "UPDATE sales_invoice SET payment_status = ? WHERE invoice_id = ?";

        try (PreparedStatement statement = conn.prepareStatement(sqlQuery)) {
            // Set the values for the prepared statement
            statement.setString(1, selectedInvoice.getPaymentStatus());
            statement.setInt(2, selectedInvoice.getInvoiceId());

            // Execute the update and return true if at least one row was affected
            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();  // Log the exception
            return false;
        }
    }


    DiscountDAO discountDAO = new DiscountDAO();

    public SalesInvoiceHeader getSalesInvoiceById(int invoiceId) {
        String sqlQuery = "SELECT * FROM sales_invoice WHERE invoice_id = ?";
        SalesInvoiceHeader salesInvoiceHeader = null;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sqlQuery)) {
            statement.setInt(1, invoiceId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    salesInvoiceHeader = mapResultSetToInvoice(resultSet);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return salesInvoiceHeader;
    }

    public ObservableList<SalesInvoiceDetail> getSalesInvoiceDetails(SalesInvoiceHeader salesInvoiceHeader) {
        ObservableList<SalesInvoiceDetail> salesInvoiceDetails = FXCollections.observableArrayList();
        String sqlQuery = "SELECT * FROM sales_invoice_details WHERE invoice_no = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sqlQuery)) {

            // Set the invoice_id parameter
            statement.setInt(1, salesInvoiceHeader.getInvoiceId());

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    SalesInvoiceDetail salesInvoiceDetail = new SalesInvoiceDetail();
                    salesInvoiceDetail.setOrderId(resultSet.getString("order_id"));
                    salesInvoiceDetail.setSalesInvoiceNo(salesInvoiceHeader); // Assuming invoice_id is needed
                    salesInvoiceDetail.setProduct(productDAO.getProductById(resultSet.getInt("product_id")));
                    salesInvoiceDetail.setUnitPrice(resultSet.getBigDecimal("unit_price").doubleValue());
                    salesInvoiceDetail.setQuantity(resultSet.getInt("quantity"));
                    salesInvoiceDetail.setDiscountAmount(resultSet.getBigDecimal("discount_amount").doubleValue());
                    salesInvoiceDetail.setTotalPrice(resultSet.getBigDecimal("total_amount").doubleValue());
                    salesInvoiceDetail.setDiscountType(discountDAO.getDiscountTypeById(resultSet.getInt("discount_type")));

                    // Add to the ObservableList
                    salesInvoiceDetails.add(salesInvoiceDetail);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return salesInvoiceDetails;
    }

    public List<String> getAllInvoiceNumbersUnlinkedToSalesReturns() {
        List<String> invoiceNumbers = new ArrayList<>();
        String sqlQuery = "SELECT invoice_no \n" +
                "FROM sales_invoice \n" +
                "WHERE invoice_id NOT IN (SELECT invoice_no FROM sales_return);\n";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sqlQuery);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                invoiceNumbers.add(resultSet.getString("invoice_no"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return invoiceNumbers;
    }

    public ObservableList<SalesInvoiceHeader> loadSalesInvoicesByInvoiceNo(String newValue) {
        String sqlQuery = "SELECT * FROM sales_invoice WHERE invoice_no = ?";
        ObservableList<SalesInvoiceHeader> salesInvoices = FXCollections.observableArrayList();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sqlQuery)) {
            statement.setString(1, newValue);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) { // Loop through all matching invoices
                    SalesInvoiceHeader invoice = mapResultSetToInvoice(resultSet);
                    salesInvoices.add(invoice);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return salesInvoices;
    }

    public List<String> getAllInvoiceNumbers() {
        List<String> invoiceNumbers = new ArrayList<>();
        String query = "SELECT invoice_no, invoice_id FROM sales_invoice";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                invoiceNumbers.add(rs.getString("invoice_no"));
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Consider logging this instead
        }
        return invoiceNumbers;
    }

    public List<String> getAllSalesOrderBySalesmanAndCustomer(int id, String customerCode) {
        List<String> invoiceNumbers = new ArrayList<>();
        String query = "SELECT order_id FROM sales_invoice WHERE salesman_id = ? AND customer_code = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            stmt.setString(2, customerCode);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    invoiceNumbers.add(rs.getString("order_id"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Consider logging this instead
        }
        return invoiceNumbers;
    }

    public List<String> getAllInvoiceNumbersByOrderNo(String newValue) {
        List<String> invoiceNumbers = new ArrayList<>();
        String sqlQuery = "SELECT invoice_no FROM sales_invoice WHERE order_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sqlQuery)) {
            statement.setString(1, newValue);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    invoiceNumbers.add(resultSet.getString("invoice_no"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return invoiceNumbers;
    }

    public boolean linkSalesInvoiceSalesReturn(SalesInvoiceHeader salesInvoiceHeader, SalesReturn salesReturn, Connection connection) throws SQLException {
        String insertSql = "INSERT INTO sales_invoice_sales_return (return_no, invoice_no, linked_by) VALUES (?, ?, ?)";
        String updateSalesReturn = "UPDATE sales_return SET isApplied = 1 WHERE return_id = ?";

        try (PreparedStatement insertStmt = connection.prepareStatement(insertSql);
             PreparedStatement updateStmt = connection.prepareStatement(updateSalesReturn)) {

            insertStmt.setInt(1, salesReturn.getReturnId());
            insertStmt.setInt(2, salesInvoiceHeader.getInvoiceId());
            insertStmt.setInt(3, UserSession.getInstance().getUserId());

            int rowsAffected = insertStmt.executeUpdate();

            if (rowsAffected > 0) {
                updateStmt.setInt(1, salesReturn.getReturnId());
                updateStmt.executeUpdate();
                System.out.println("Sales Return linked successfully: Return No " + salesReturn.getReturnId() +
                        ", Invoice No " + salesInvoiceHeader.getInvoiceId());
                return true;
            } else {
                System.err.println("Failed to link Sales Return: Return No " + salesReturn.getReturnId() +
                        ", Invoice No " + salesInvoiceHeader.getInvoiceId());
                return false;
            }
        }
    }


    public List<String> getAllCustomerNamesForUnpaidInvoicesOfSalesman(Salesman salesman) {
        List<String> customerNames = new ArrayList<>();
        String query = "SELECT DISTINCT c.customer_name FROM sales_invoice si JOIN customer c ON si.customer_code = c.customer_code WHERE si.salesman_id = ? AND si.payment_status = 'Unpaid'";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, salesman.getId());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    customerNames.add(rs.getString("customer_name"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Consider logging this instead
        }
        return customerNames;
    }

    public ObservableList<SalesInvoiceHeader> loadUnpaidAndUnlinkedSalesInvoicesBySalesman(Salesman salesman, Timestamp value, Timestamp dateToValue) {
        ObservableList<SalesInvoiceHeader> salesInvoices = FXCollections.observableArrayList();

        // Updated query with date range filtering
        String query = "SELECT * FROM sales_invoice si " +
                "WHERE si.salesman_id = ? " +
                "AND si.payment_status IN ('Unpaid', 'Partially Paid') " +
                "AND si.invoice_date >= ? " +
                "AND si.invoice_date <= ? " +
                "AND NOT EXISTS (SELECT 1 FROM collection_invoices ci WHERE ci.invoice_id = si.invoice_id)";


        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {

            // Set salesman ID
            stmt.setInt(1, salesman.getId());
            System.out.println("SalesInvoiceDAO.loadUnpaidSalesInvoicesBySalesman: Salesman ID: " + salesman.getId());

            // Convert LocalDate to java.sql.Timestamp for query compatibility
            stmt.setTimestamp(2, value);  // Start date
            stmt.setTimestamp(3, dateToValue);  // End date (exclusive)
            System.out.println("SalesInvoiceDAO.loadUnpaidSalesInvoicesBySalesman: Executing query: " + stmt.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    SalesInvoiceHeader salesInvoiceHeader = mapResultSetToInvoice(rs);
                    salesInvoices.add(salesInvoiceHeader);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error loading unpaid sales invoices: " + e.getMessage());
            e.printStackTrace();
        }
        return salesInvoices;
    }


    public ObservableList<SalesInvoiceHeader> loadSalesInvoicesBySalesmanName(int salesmanId) {
        ObservableList<SalesInvoiceHeader> salesInvoiceHeaders = FXCollections.observableArrayList();
        String sqlQuery = "SELECT * FROM sales_invoice WHERE salesman_id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sqlQuery)) {
            statement.setInt(1, salesmanId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    SalesInvoiceHeader salesInvoiceHeader = mapResultSetToInvoice(resultSet);
                    salesInvoiceHeaders.add(salesInvoiceHeader);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return salesInvoiceHeaders;
    }

    public boolean deleteSalesInvoice(SalesInvoiceHeader salesInvoiceHeader) {
        String sqlQuery = "DELETE FROM sales_invoice WHERE invoice_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sqlQuery)) {
            statement.setInt(1, salesInvoiceHeader.getInvoiceId());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean removeSalesInvoiceDetails(ObservableList<SalesInvoiceDetail> deletedSalesInvoiceDetails, Connection connection) {
        String sqlQuery = "DELETE FROM sales_invoice_details WHERE invoice_no = ? AND product_id = ?";

        if (deletedSalesInvoiceDetails == null || deletedSalesInvoiceDetails.isEmpty()) {
            System.out.println("No sales invoice details to delete.");
            return true;
        }

        boolean autoCommitStatus = true;

        try {
            autoCommitStatus = connection.getAutoCommit(); // Save original auto-commit state
            connection.setAutoCommit(false); // Start transaction

            try (PreparedStatement statement = connection.prepareStatement(sqlQuery)) {
                for (SalesInvoiceDetail salesInvoiceDetail : deletedSalesInvoiceDetails) {
                    if (salesInvoiceDetail.getSalesInvoiceNo() == null || salesInvoiceDetail.getSalesInvoiceNo().getInvoiceId() == 0) {
                        System.err.println("Skipping deletion due to missing invoice ID for product: " + salesInvoiceDetail.getProduct().getProductId());
                        continue;
                    }

                    statement.setInt(1, salesInvoiceDetail.getSalesInvoiceNo().getInvoiceId());
                    statement.setInt(2, salesInvoiceDetail.getProduct().getProductId());
                    statement.addBatch();
                }

                int[] affectedRows = statement.executeBatch();
                System.out.println("Total rows deleted: " + affectedRows.length);

                connection.commit();
                return true;

            } catch (SQLException e) {
                System.err.println("Error deleting sales invoice details: " + e.getMessage());

                if (!autoCommitStatus) { // Only rollback if auto-commit was false
                    connection.rollback();
                }

                return false;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;

        } finally {
            try {
                connection.setAutoCommit(autoCommitStatus); // Restore previous auto-commit mode
            } catch (SQLException ignored) {
                System.err.println("Failed to restore auto-commit mode.");
            }
        }
    }


    public List<Salesman> salesmanWithSalesInvoices() {
        List<Salesman> salesmen = new ArrayList<>();
        String sqlQuery = "SELECT DISTINCT s.id, s.salesman_name " +
                "FROM salesman s " +
                "JOIN sales_invoice si ON s.id = si.salesman_id " +
                "WHERE si.salesman_id IS NOT NULL";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sqlQuery);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Salesman salesman = new Salesman();
                salesman.setId(resultSet.getInt("id"));
                salesman.setSalesmanName(resultSet.getString("salesman_name"));
                salesmen.add(salesman);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return salesmen;
    }


    public List<Customer> customersWithSalesInvoices() {
        List<Customer> customers = new ArrayList<>();
        String sqlQuery = "SELECT DISTINCT c.customer_code, c.customer_name, c.store_name " +
                "FROM customer c " +
                "JOIN sales_invoice si ON c.customer_code = si.customer_code " +
                "WHERE si.customer_code IS NOT NULL";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sqlQuery);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Customer customer = new Customer();
                customer.setCustomerCode(resultSet.getString("customer_code"));
                customer.setCustomerName(resultSet.getString("customer_name"));
                customer.setStoreName(resultSet.getString("store_name"));
                customers.add(customer);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return customers;
    }

    public boolean invoiceExists(String invoiceNo, Connection connection) {
        String sqlQuery = "SELECT COUNT(*) FROM sales_invoice WHERE invoice_no = ?";
        try (PreparedStatement statement = connection.prepareStatement(sqlQuery)) {
            statement.setString(1, invoiceNo);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public ObservableList<SalesInvoiceHeader> getInvoicesByInvoiceIds(List<Integer> invoiceIds) {
        ObservableList<SalesInvoiceHeader> invoices = FXCollections.observableArrayList();

        if (invoiceIds.isEmpty()) return invoices; // Avoid empty queries

        // Dynamically build the IN clause (e.g., "IN (?, ?, ?)")
        String placeholders = String.join(",", Collections.nCopies(invoiceIds.size(), "?"));
        String sqlQuery = "SELECT * FROM sales_invoice WHERE invoice_id IN (" + placeholders + ")";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sqlQuery)) {
            for (int i = 0; i < invoiceIds.size(); i++) {
                statement.setInt(i + 1, invoiceIds.get(i)); // Correctly bind each ID
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    SalesInvoiceHeader salesInvoice = mapResultSetToInvoice(resultSet);
                    invoices.add(salesInvoice);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return invoices;
    }

    ClusterDAO clusterDAO = new ClusterDAO();

    public ObservableList<SalesInvoiceHeader> getSalesInvoicesForTripSummary(Cluster cluster) {
        ObservableList<SalesInvoiceHeader> salesInvoices = FXCollections.observableArrayList();

        // Get all areas in the cluster
        ObservableList<AreaPerCluster> areaPerClusters = FXCollections.observableArrayList(clusterDAO.getAreasByClusterId(cluster.getId()));

        if (areaPerClusters.isEmpty()) {
            return salesInvoices; // No areas in this cluster, return empty list
        }

        // Extract unique non-null values
        Set<String> areaBrgys = areaPerClusters.stream().map(AreaPerCluster::getBaranggay).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<String> areaCities = areaPerClusters.stream().map(AreaPerCluster::getCity).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<String> areaProvinces = areaPerClusters.stream().map(AreaPerCluster::getProvince).filter(Objects::nonNull).collect(Collectors.toSet());

        // **STRICT filtering: City and Province must exist**
        if (areaCities.isEmpty() || areaProvinces.isEmpty()) {
            return salesInvoices; // If city or province is missing, stop filtering
        }

        // Convert lists to SQL-friendly comma-separated strings
        String cityList = areaCities.stream().map(city -> "'" + city + "'").collect(Collectors.joining(","));
        String provinceList = areaProvinces.stream().map(province -> "'" + province + "'").collect(Collectors.joining(","));

        // Build the WHERE clause dynamically
        StringBuilder customerQuery = new StringBuilder("SELECT DISTINCT customer_code FROM customer WHERE city IN (" + cityList + ") AND province IN (" + provinceList + ")");

        // Add barangay filtering if it exists
        if (!areaBrgys.isEmpty()) {
            String brgyList = areaBrgys.stream().map(brgy -> "'" + brgy + "'").collect(Collectors.joining(","));
            customerQuery.append(" OR brgy IN (" + brgyList + ")");
        }

        List<String> customerCodes = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(customerQuery.toString());
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                customerCodes.add(rs.getString("customer_code"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return salesInvoices;
        }

        if (customerCodes.isEmpty()) {
            return salesInvoices; // No customers found
        }

        // Query sales invoices for those customers
        String customerCodesList = customerCodes.stream().map(code -> "'" + code + "'").collect(Collectors.joining(","));

        String invoiceQuery = """
                    SELECT si.* 
                    FROM sales_invoice si
                    LEFT JOIN trip_summary_details tsd ON si.invoice_id = tsd.invoice_id
                    WHERE si.customer_code IN (%s)
                    AND si.transaction_status = 'For Trip Summary'
                    AND tsd.invoice_id IS NULL
                """.formatted(customerCodesList);

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(invoiceQuery);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                SalesInvoiceHeader salesInvoice = mapResultSetToInvoice(rs);
                salesInvoices.add(salesInvoice);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return salesInvoices;
    }


}