package com.vertex.vos.DAO;

import com.vertex.vos.Objects.*;
import com.vertex.vos.Utilities.*;
import com.zaxxer.hikari.HikariDataSource;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDate;
import java.util.logging.Logger;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SalesInvoiceDAO {
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();
    private static final Logger LOGGER = Logger.getLogger(SalesInvoiceDAO.class.getName());

    public SalesInvoiceHeader createSalesInvoiceWithDetails(SalesInvoiceHeader invoice, List<SalesInvoiceDetail> salesInvoiceDetails, Connection connection) throws SQLException {
        String sqlQueryHeader = "INSERT INTO sales_invoice " +
                "(order_id, customer_code, salesman_id, invoice_date, dispatch_date, due_date, " +
                "payment_terms, transaction_status, payment_status, total_amount, sales_type, " +
                "invoice_type, price_type, invoice_no, vat_amount, discount_amount, net_amount, " +
                "created_by, created_date, modified_by, modified_date, posted_by, posted_date, " +
                "remarks, isReceipt, isPosted, isDispatched, gross_amount) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "customer_code = VALUES(customer_code), salesman_id = VALUES(salesman_id), " +
                "invoice_date = VALUES(invoice_date), dispatch_date = VALUES(dispatch_date), " +
                "due_date = VALUES(due_date), payment_terms = VALUES(payment_terms), " +
                "transaction_status = VALUES(transaction_status), payment_status = VALUES(payment_status), " +
                "total_amount = VALUES(total_amount), sales_type = VALUES(sales_type), " +
                "invoice_type = VALUES(invoice_type), price_type = VALUES(price_type), " +
                "vat_amount = VALUES(vat_amount), discount_amount = VALUES(discount_amount), " +
                "net_amount = VALUES(net_amount), modified_by = VALUES(modified_by), " +
                "modified_date = VALUES(modified_date), posted_by = VALUES(posted_by), " +
                "posted_date = VALUES(posted_date), remarks = VALUES(remarks), " +
                "isReceipt = VALUES(isReceipt), isPosted = VALUES(isPosted), isDispatched = VALUES(isDispatched), " +
                "gross_amount = VALUES(gross_amount)";

        try (PreparedStatement statementHeader = connection.prepareStatement(sqlQueryHeader, Statement.RETURN_GENERATED_KEYS)) {
            connection.setAutoCommit(false); // Begin transaction

            // Set parameters for the sales invoice header
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
            statementHeader.setInt(18, invoice.getCreatedBy());
            statementHeader.setTimestamp(19, invoice.getCreatedDate());
            statementHeader.setInt(20, invoice.getModifiedBy());
            statementHeader.setTimestamp(21, invoice.getModifiedDate());
            statementHeader.setInt(22, invoice.getPostedBy());
            statementHeader.setTimestamp(23, invoice.getPostedDate());
            statementHeader.setString(24, invoice.getRemarks());
            statementHeader.setBoolean(25, invoice.isReceipt());
            statementHeader.setBoolean(26, invoice.isPosted());
            statementHeader.setBoolean(27, invoice.isDispatched());
            statementHeader.setDouble(28, invoice.getGrossAmount());

            // Execute the header insertion
            int rowsInserted = statementHeader.executeUpdate();

            if (rowsInserted > 0) {
                // Retrieve the generated invoice ID
                try (ResultSet generatedKeys = statementHeader.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int invoiceId = generatedKeys.getInt(1);
                        invoice.setInvoiceId(invoiceId); // Set the generated ID to the invoice object

                        // Now insert the invoice details
                        if (!createSalesInvoiceDetailsBulk(invoiceId, salesInvoiceDetails, connection)) {
                            connection.rollback(); // Rollback in case of failure
                            throw new SQLException("Failed to insert invoice details.");
                        }

                        connection.commit(); // Commit transaction if everything is successful
                        return invoice; // Return the updated invoice
                    }
                }
            }

            connection.rollback(); // Rollback if insertion fails
            throw new SQLException("Failed to insert sales invoice header.");
        } catch (SQLException ex) {
            connection.rollback(); // Ensure rollback on error
            LOGGER.log(java.util.logging.Level.SEVERE, "Error inserting sales invoice header: " + ex.getMessage());
            ex.printStackTrace();
            throw ex;
        } finally {
            connection.setAutoCommit(true); // Reset auto-commit mode
        }
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
                "modified_date = NOW()";

        try (PreparedStatement statement = connection.prepareStatement(sqlQueryDetails)) {
            int batchCount = 0;
            final int batchSize = 1000; // Configurable batch size

            for (SalesInvoiceDetail detail : salesInvoiceDetails) {
                statement.setString(1, detail.getOrderId()); // Assuming SalesInvoiceDetail has getOrderId()
                statement.setInt(2, invoiceId); // Use the generated invoice ID from header insertion

                if (detail.getDiscountType() != null) {
                    statement.setInt(3, detail.getDiscountType().getId());
                } else {
                    statement.setNull(3, Types.INTEGER);
                }

                statement.setInt(4, detail.getProduct().getProductId());
                statement.setInt(5, detail.getProduct().getUnitOfMeasurement());
                statement.setDouble(6, detail.getUnitPrice());
                statement.setInt(7, detail.getQuantity());
                statement.setDouble(8, detail.getDiscountAmount());
                statement.setDouble(9, detail.getTotalPrice());
                statement.setDouble(10, detail.getGrossAmount());

                statement.addBatch();
                batchCount++;

                // Execute batch at regular intervals
                if (batchCount % batchSize == 0) {
                    statement.executeBatch();
                }
            }

            // Execute remaining batch
            statement.executeBatch();

            return true;
        } catch (SQLException ex) {
            // Log and rethrow for the caller to handle
            System.err.println("Error inserting invoice details: " + ex.getMessage());
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

    private SalesInvoiceHeader mapResultSetToInvoice(ResultSet resultSet) throws SQLException {
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
        invoice.setInvoiceType(salesInvoiceTypeDAO.getInvoiceIdByType(resultSet.getInt("invoice_type")));
        invoice.setReceipt(resultSet.getBoolean("isReceipt"));
        invoice.setSalesType(resultSet.getInt("sales_type"));
        invoice.setPostedBy(resultSet.getInt("posted_by"));
        invoice.setPosted(resultSet.getBoolean("isPosted"));
        invoice.setInvoiceNo(resultSet.getString("invoice_no"));
        return invoice;
    }


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

    public boolean paidOrder(SalesInvoiceHeader selectedInvoice) {
        String sqlQuery = "UPDATE sales_invoice SET payment_status = 'PAID' WHERE invoice_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sqlQuery)) {
            statement.setInt(1, selectedInvoice.getInvoiceId());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    DiscountDAO discountDAO = new DiscountDAO();


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

    public boolean linkSalesInvoiceSalesReturn(SalesInvoiceHeader salesInvoiceHeader, SalesReturn salesReturn, Connection connection) {
        String sql = "INSERT INTO sales_invoice_sales_return (return_no, invoice_no, linked_by) VALUES (?, ?, ?)";
        String updateSalesReturn = "UPDATE sales_return SET isApplied = 1 WHERE return_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             PreparedStatement updateStmt = connection.prepareStatement(updateSalesReturn)) {

            stmt.setInt(1, salesReturn.getReturnId());
            stmt.setInt(2, salesInvoiceHeader.getInvoiceId());
            stmt.setInt(3, UserSession.getInstance().getUserId());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                updateStmt.setInt(1, salesReturn.getReturnId());
                updateStmt.executeUpdate();
                return true;
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
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

    public ObservableList<SalesInvoiceHeader> loadUnpaidSalesInvoicesBySalesman(Salesman salesman, LocalDate value, LocalDate dateToValue) {
        ObservableList<SalesInvoiceHeader> salesInvoices = FXCollections.observableArrayList();

        // Updated query with date range filtering
        String query = "SELECT * FROM sales_invoice WHERE salesman_id = ? " +
                "AND payment_status IN ('Unpaid', 'Partially Paid') " +
                "AND invoice_date >= ? " +
                "AND invoice_date < ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {

            // Set salesman ID
            stmt.setInt(1, salesman.getId());
            System.out.println("SalesInvoiceDAO.loadUnpaidSalesInvoicesBySalesman: Salesman ID: " + salesman.getId());

            // Convert LocalDate to java.sql.Timestamp for query compatibility
            stmt.setTimestamp(2, Timestamp.valueOf(value.atStartOfDay()));  // Start date
            stmt.setTimestamp(3, Timestamp.valueOf(dateToValue.plusDays(1).atStartOfDay()));  // End date (exclusive)
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
        String sqlQuery = "DELETE FROM sales_invoice_details WHERE invoice_no = ?";
        try (PreparedStatement statement = connection.prepareStatement(sqlQuery)) {
            for (SalesInvoiceDetail salesInvoiceDetail : deletedSalesInvoiceDetails) {
                statement.setInt(1, salesInvoiceDetail.getSalesInvoiceNo().getInvoiceId());
                statement.executeUpdate();
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
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
}