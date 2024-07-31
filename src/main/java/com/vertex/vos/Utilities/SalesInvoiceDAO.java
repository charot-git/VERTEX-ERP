package com.vertex.vos.Utilities;

import com.vertex.vos.Objects.*;
import com.zaxxer.hikari.HikariDataSource;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public class SalesInvoiceDAO {
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    public boolean createSalesInvoice(SalesInvoice invoice) throws SQLException {
        String sqlQuery = "INSERT INTO sales_invoice " +
                "(order_id, invoice_no, customer_code, salesman_id, invoice_date, due_date, " +
                "payment_terms, status, total_amount, sales_type, vat_amount, discount_amount, " +
                "net_amount, created_by, created_date, modified_by, modified_date, " +
                "posted_by, isReceipt, type, remarks) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sqlQuery)) {
            statement.setString(1, invoice.getOrderId());
            statement.setString(2, invoice.getInvoiceNo());
            statement.setString(3, invoice.getCustomerCode());
            statement.setInt(4, invoice.getSalesmanId());
            statement.setDate(5, invoice.getInvoiceDate());
            statement.setDate(6, invoice.getDueDate());
            statement.setString(7, invoice.getPaymentTerms());
            statement.setString(8, invoice.getStatus());
            statement.setBigDecimal(9, invoice.getTotalAmount());
            statement.setInt(10, invoice.getSalesType());
            statement.setBigDecimal(11, invoice.getVatAmount());
            statement.setBigDecimal(12, invoice.getDiscountAmount());
            statement.setBigDecimal(13, invoice.getNetAmount());
            statement.setInt(14, UserSession.getInstance().getUserId());
            statement.setTimestamp(15, invoice.getCreatedDate());
            statement.setInt(16, UserSession.getInstance().getUserId());
            statement.setTimestamp(17, invoice.getModifiedDate());
            statement.setInt(18, invoice.getPostedBy());
            statement.setBoolean(19, invoice.isReceipt());
            statement.setInt(20, invoice.getInvoiceType());
            statement.setString(21, invoice.getRemarks());

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

    public ObservableList<SalesInvoice> loadSalesInvoices() throws SQLException {
        ObservableList<SalesInvoice> invoices = FXCollections.observableArrayList();
        String sqlQuery = "SELECT * FROM sales_invoice";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sqlQuery);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                SalesInvoice invoice = new SalesInvoice();
                invoice.setInvoiceId(resultSet.getInt("invoice_id"));
                invoice.setOrderId(resultSet.getString("order_id"));
                invoice.setInvoiceNo(resultSet.getString("invoice_no"));
                invoice.setCustomerCode(resultSet.getString("customer_code"));
                Customer customer = customerDAO.getCustomerByCode(invoice.getCustomerCode());
                invoice.setCustomerName(customer.getCustomerName());
                invoice.setStoreName(customer.getStoreName());
                int salesmanId = resultSet.getInt("salesman_id");
                invoice.setSalesmanId(salesmanId);
                invoice.setSalesmanName(salesmanDAO.getSalesmanNameById(salesmanId));
                invoice.setInvoiceDate(resultSet.getDate("invoice_date"));
                invoice.setDueDate(resultSet.getDate("due_date"));
                invoice.setPaymentTerms(resultSet.getString("payment_terms"));
                invoice.setStatus(resultSet.getString("status"));
                invoice.setTotalAmount(resultSet.getBigDecimal("total_amount"));
                invoice.setVatAmount(resultSet.getBigDecimal("vat_amount"));
                invoice.setDiscountAmount(resultSet.getBigDecimal("discount_amount"));
                invoice.setNetAmount(resultSet.getBigDecimal("net_amount"));
                invoice.setCreatedBy(resultSet.getString("created_by"));
                invoice.setCreatedDate(resultSet.getTimestamp("created_date"));
                invoice.setModifiedBy(resultSet.getString("modified_by"));
                invoice.setModifiedDate(resultSet.getTimestamp("modified_date"));
                invoice.setRemarks(resultSet.getString("remarks"));
                invoice.setInvoiceType(resultSet.getInt("type"));
                invoices.add(invoice);
            }
        }
        return invoices;
    }
    UnitDAO unitDAO = new UnitDAO();
    public ObservableList<ProductsInTransact> loadSalesInvoiceProducts(String orderId) throws SQLException {
        ObservableList<ProductsInTransact> products = FXCollections.observableArrayList();
        String sqlQuery = "SELECT sid.product_id, p.description, sid.unit, sid.unit_price, sid.quantity, sid.total " +
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
                    products.add(product);
                }
            }
        }
        return products;
    }




}
