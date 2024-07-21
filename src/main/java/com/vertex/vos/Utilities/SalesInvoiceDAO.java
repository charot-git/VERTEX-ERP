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
                "(order_id, customer_id, salesman_id, invoice_date, due_date, " +
                "payment_terms, status, total_amount, vat_amount, discount_amount, " +
                "net_amount, created_by, created_date, modified_by, modified_date, " +
                "remarks, type) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sqlQuery)) {
            statement.setString(1, invoice.getOrderId());
            statement.setInt(2, invoice.getCustomerId());
            statement.setInt(3, invoice.getSalesmanId());
            statement.setTimestamp(4, invoice.getInvoiceDate());
            statement.setDate(5, invoice.getDueDate());
            statement.setString(6, invoice.getPaymentTerms());
            statement.setString(7, invoice.getStatus());
            statement.setBigDecimal(8, invoice.getTotalAmount());
            statement.setBigDecimal(9, invoice.getVatAmount());
            statement.setBigDecimal(10, invoice.getDiscountAmount());
            statement.setBigDecimal(11, invoice.getNetAmount());
            statement.setInt(12, UserSession.getInstance().getUserId());
            statement.setTimestamp(13, invoice.getCreatedDate());
            statement.setInt(14, UserSession.getInstance().getUserId());
            statement.setTimestamp(15, invoice.getModifiedDate());
            statement.setString(16, invoice.getRemarks());
            statement.setString(17, invoice.getType());

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

    public boolean createSalesInvoiceDetailsBulk(String orderId, List<ProductsInTransact> products) throws SQLException {
        String sqlQuery = "INSERT INTO sales_invoice_details (order_id, product_id, unit, unit_price, quantity, total, created_date, modified_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sqlQuery)) {
            for (ProductsInTransact product : products) {
                statement.setString(1, orderId);
                int productId = product.getProductId();
                statement.setInt(2, productId);
                Product invoiceProduct = productDAO.getProductDetails(productId);
                statement.setInt(3, invoiceProduct.getUnitOfMeasurement());
                statement.setBigDecimal(4, BigDecimal.valueOf(product.getUnitPrice())); // Assuming unitPrice field exists in ProductsInTransact
                statement.setInt(5, product.getOrderedQuantity()); // Assuming orderedQuantity field exists in ProductsInTransact
                statement.setBigDecimal(6, BigDecimal.valueOf(product.getUnitPrice() * product.getOrderedQuantity())); // Total calculation
                statement.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now())); // Assuming created_date field exists in SalesInvoiceDetail
                statement.setTimestamp(8, Timestamp.valueOf(LocalDateTime.now())); // Assuming modified_date field exists in SalesInvoiceDetail
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
                invoice.setCustomerId(customerDAO.getCustomerIdByCustomerCode(resultSet.getString("customer_code")));
                Customer customer = customerDAO.getCustomer(invoice.getCustomerId());
                invoice.setCustomerName(customer.getCustomerName());
                invoice.setStoreName(customer.getStoreName());
                int salesmanId = resultSet.getInt("salesman_id");
                invoice.setSalesmanId(salesmanId);
                invoice.setSalesmanName(salesmanDAO.getSalesmanNameById(salesmanId));
                invoice.setInvoiceDate(resultSet.getTimestamp("invoice_date"));
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
                invoice.setType(resultSet.getString("type"));
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
