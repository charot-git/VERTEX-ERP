package com.vertex.vos.Utilities;

import com.vertex.vos.Objects.*;
import com.zaxxer.hikari.HikariDataSource;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.math.BigDecimal;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

public class SalesInvoiceDAO {
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    public boolean createSalesInvoice(SalesInvoiceHeader invoice) throws SQLException {
        String sqlQuery = "INSERT INTO sales_invoice " +
                "(order_id, customer_code, salesman_id, invoice_date, due_date, " +
                "payment_terms, payment_status, transaction_status, total_amount, sales_type, vat_amount, discount_amount, " +
                "net_amount, created_by, created_date, modified_by, modified_date, " +
                "posted_by, posted_date, isReceipt, type, remarks) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sqlQuery)) {

            statement.setString(1, invoice.getOrderId());
            statement.setString(2, invoice.getCustomerCode());
            statement.setInt(3, invoice.getSalesmanId());
            statement.setDate(4, new java.sql.Date(invoice.getInvoiceDate().getTime()));
            statement.setDate(5, new java.sql.Date(invoice.getDueDate().getTime()));
            statement.setInt(6, invoice.getPaymentTerms());
            statement.setString(7, invoice.getPaymentStatus());
            statement.setString(8, invoice.getTransactionStatus());
            statement.setDouble(9, invoice.getTotalAmount());
            statement.setInt(10, invoice.getSalesType());
            statement.setDouble(11, invoice.getVatAmount());
            statement.setDouble(12, invoice.getDiscountAmount());
            statement.setDouble(13, invoice.getNetAmount());
            statement.setInt(14, invoice.getCreatedBy());
            statement.setTimestamp(15, invoice.getCreatedDate());
            statement.setInt(16, invoice.getModifiedBy());
            statement.setTimestamp(17, invoice.getModifiedDate());
            statement.setInt(18, invoice.getPostedBy());
            statement.setTimestamp(19, Timestamp.valueOf(invoice.getPostedDate().toLocalDate().atStartOfDay()));
            statement.setInt(20, invoice.getIsReceipt());
            statement.setInt(21, invoice.getType());
            statement.setString(22, invoice.getRemarks());

            int rowsInserted = statement.executeUpdate();
            return rowsInserted > 0;
        }
    }


    private String prepareStatementForCreateSalesInvoice(String baseQuery) {
        // You can customize this method to dynamically generate the prepared statement
        // based on the actual columns you want to insert values for.
        return baseQuery;
    }

    ProductDAO productDAO = new ProductDAO();

    public boolean createSalesInvoiceDetailsBulk(SalesOrderHeader order, List<ProductsInTransact> products) throws SQLException {
        String sqlQuery = "INSERT INTO sales_invoice_details (order_id, invoice_no, serial_no, product_id, unit, unit_price, quantity, total, created_date, modified_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sqlQuery)) {

            for (ProductsInTransact product : products) {
                statement.setString(1, order.getOrderId());
                statement.setString(2, product.getInvoiceNo()); // Assuming ProductsInTransact has a method getInvoiceNo()
                statement.setString(3, product.getSerialNo()); // Assuming ProductsInTransact has a method getSerialNo()
                int productId = product.getProductId();
                statement.setInt(4, productId);
                Product invoiceProduct = productDAO.getProductDetails(productId);
                statement.setInt(5, invoiceProduct.getUnitOfMeasurement());
                statement.setBigDecimal(6, BigDecimal.valueOf(product.getUnitPrice()));
                statement.setInt(7, product.getOrderedQuantity());
                statement.setBigDecimal(8, BigDecimal.valueOf(product.getUnitPrice() * product.getOrderedQuantity()));
                statement.setTimestamp(9, Timestamp.valueOf(LocalDateTime.now()));
                statement.setTimestamp(10, Timestamp.valueOf(LocalDateTime.now()));
                statement.addBatch();
            }

            int[] rowsInserted = statement.executeBatch();
            return Arrays.stream(rowsInserted).allMatch(row -> row > 0);
        }
    }


    CustomerDAO customerDAO = new CustomerDAO();
    SalesmanDAO salesmanDAO = new SalesmanDAO();
    EmployeeDAO employeeDAO = new EmployeeDAO();

    public ObservableList<SalesInvoiceHeader> loadSalesInvoices() throws SQLException {
        ObservableList<SalesInvoiceHeader> invoices = FXCollections.observableArrayList();
        String sqlQuery = "SELECT * FROM sales_invoice";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sqlQuery);
             ResultSet resultSet = statement.executeQuery()) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            while (resultSet.next()) {
                SalesInvoiceHeader invoice = new SalesInvoiceHeader();
                invoice.setInvoiceId(resultSet.getInt("invoice_id"));
                invoice.setOrderId(resultSet.getString("order_id"));
                invoice.setCustomerCode(resultSet.getString("customer_code"));

                // Fetch and set customer details
                Customer customer = customerDAO.getCustomerByCode(invoice.getCustomerCode());
                if (customer != null) {
                    invoice.setCustomerName(customer.getCustomerName());
                    invoice.setStoreName(customer.getStoreName());
                }

                // Set Salesman details
                int salesmanId = resultSet.getInt("salesman_id");
                invoice.setSalesmanId(salesmanId);
                invoice.setSalesman(salesmanDAO.getSalesmanDetails(salesmanId));

                // Handle date fields
                String invoiceDateString = resultSet.getString("invoice_date");
                LocalDate invoiceDate = LocalDate.parse(invoiceDateString, formatter);
                invoice.setInvoiceDate(Date.valueOf(invoiceDate));

                String invoiceDueDateString = resultSet.getString("due_date");
                LocalDate invoiceDueDate = LocalDate.parse(invoiceDueDateString, formatter);
                invoice.setDueDate(Date.valueOf(invoiceDueDate));

                String postedDateString = resultSet.getString("posted_date");
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a");
                java.util.Date utilDate = simpleDateFormat.parse(postedDateString);
                Date sqlDate = new Date(utilDate.getTime());
                invoice.setPostedDate(sqlDate);

                invoice.setPaymentTerms(resultSet.getInt("payment_terms"));
                invoice.setTransactionStatus(resultSet.getString("transaction_status"));
                invoice.setPaymentStatus(resultSet.getString("payment_status"));
                invoice.setTotalAmount(resultSet.getDouble("total_amount"));
                invoice.setVatAmount(resultSet.getDouble("vat_amount"));
                invoice.setDiscountAmount(resultSet.getDouble("discount_amount"));
                invoice.setNetAmount(resultSet.getDouble("net_amount"));
                invoice.setCreatedBy(resultSet.getInt("created_by"));
                invoice.setCreatedDate(resultSet.getTimestamp("created_date"));
                invoice.setModifiedBy(resultSet.getInt("modified_by"));
                invoice.setModifiedDate(resultSet.getTimestamp("modified_date"));
                invoice.setRemarks(resultSet.getString("remarks"));
                invoice.setType(resultSet.getInt("isReceipt"));
                invoice.setSalesType(resultSet.getInt("sales_type"));

                invoices.add(invoice);
            }
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        return invoices;
    }


    UnitDAO unitDAO = new UnitDAO();

    public ObservableList<ProductsInTransact> loadSalesInvoiceProducts(String orderId) {
        ObservableList<ProductsInTransact> products = FXCollections.observableArrayList();
        String sqlQuery = "SELECT sid.product_id, p.description, sid.unit, sid.unit_price, sid.quantity, sid.total, sid.invoice_no " +
                "FROM sales_invoice_details sid " +
                "INNER JOIN products p ON sid.product_id = p.product_id " +
                "WHERE sid.order_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sqlQuery)) {
            statement.setString(1, orderId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    ProductsInTransact product = new ProductsInTransact();
                    product.setProductId(resultSet.getInt("product_id"));
                    product.setDescription(resultSet.getString("description"));
                    product.setUnit(unitDAO.getUnitNameById(resultSet.getInt("unit")));
                    product.setUnitPrice(resultSet.getBigDecimal("unit_price").doubleValue());
                    product.setOrderedQuantity(resultSet.getInt("quantity"));
                    product.setTotalAmount(resultSet.getBigDecimal("total").doubleValue());
                    product.setInvoiceNo(resultSet.getString("invoice_no"));
                    products.add(product);
                }
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }


}
