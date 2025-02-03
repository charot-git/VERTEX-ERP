package com.vertex.vos.DAO;

import com.vertex.vos.Objects.*;
import com.vertex.vos.Utilities.*;
import com.zaxxer.hikari.HikariDataSource;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SalesInvoiceDAO {
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    public boolean createSalesInvoiceWithDetails(SalesInvoiceHeader invoice, List<SalesInvoiceDetail> salesInvoiceDetails, Connection connection) throws SQLException {
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
                // If header is successfully inserted or updated, retrieve the generated invoice ID
                try (ResultSet generatedKeys = statementHeader.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int invoiceId = generatedKeys.getInt(1);
                        // Now proceed to insert/update the invoice details with the generated invoice ID
                        return createSalesInvoiceDetailsBulk(invoiceId, salesInvoiceDetails, connection);
                    }
                }
            }
            return false;
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

    public ObservableList<SalesInvoiceHeader> loadSalesInvoices() {
        ObservableList<SalesInvoiceHeader> invoices = FXCollections.observableArrayList();
        String sqlQuery = "SELECT * FROM sales_invoice";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sqlQuery);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                SalesInvoiceHeader invoice = mapResultSetToInvoice(resultSet);
                invoices.add(invoice);
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
}