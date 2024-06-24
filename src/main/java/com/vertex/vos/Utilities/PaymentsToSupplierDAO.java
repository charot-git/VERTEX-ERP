package com.vertex.vos.Utilities;

import com.vertex.vos.Constructors.PaymentsToSupplier;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PaymentsToSupplierDAO {

    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    // Insert method for adding a payment to supplier
    public int addPayment(PaymentsToSupplier payment) throws SQLException {
        String query = "INSERT INTO payments_to_supplier (supplier_id, payment_date, amount, " +
                "payment_method_id, reference_number, chart_of_account_id, transaction_type_id, " +
                "notes, created_at, created_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, payment.getSupplierId());
            stmt.setDate(2, java.sql.Date.valueOf(payment.getPaymentDate()));
            stmt.setBigDecimal(3, payment.getAmount());
            stmt.setInt(4, payment.getPaymentMethodId());
            stmt.setString(5, payment.getReferenceNumber());
            stmt.setInt(6, payment.getChartOfAccountId());
            stmt.setInt(7, payment.getTransactionTypeId());
            stmt.setString(8, payment.getNotes());
            stmt.setTimestamp(9, java.sql.Timestamp.valueOf(payment.getCreatedAt()));
            stmt.setInt(10, payment.getCreatedBy());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating payment failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating payment failed, no ID obtained.");
                }
            }
        }
    }

    // Retrieve method to get a payment by its ID
    public PaymentsToSupplier getPaymentById(int paymentId) throws SQLException {
        String query = "SELECT * FROM payments_to_supplier WHERE payment_id = ?";
        PaymentsToSupplier payment = null;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, paymentId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    payment = mapResultSetToPayment(rs);
                }
            }
        }

        return payment;
    }

    // Update method to update an existing payment
    public boolean updatePayment(PaymentsToSupplier payment) throws SQLException {
        String query = "UPDATE payments_to_supplier SET supplier_id = ?, payment_date = ?, " +
                "amount = ?, payment_method_id = ?, reference_number = ?, chart_of_account_id = ?, " +
                "transaction_type_id = ?, notes = ?, created_at = ?, created_by = ? WHERE payment_id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, payment.getSupplierId());
            stmt.setDate(2, java.sql.Date.valueOf(payment.getPaymentDate()));
            stmt.setBigDecimal(3, payment.getAmount());
            stmt.setInt(4, payment.getPaymentMethodId());
            stmt.setString(5, payment.getReferenceNumber());
            stmt.setInt(6, payment.getChartOfAccountId());
            stmt.setInt(7, payment.getTransactionTypeId());
            stmt.setString(8, payment.getNotes());
            stmt.setTimestamp(9, java.sql.Timestamp.valueOf(payment.getCreatedAt()));
            stmt.setInt(10, payment.getCreatedBy());
            stmt.setInt(11, payment.getPaymentId());

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    // Delete method to delete a payment by its ID
    public boolean deletePayment(int paymentId) throws SQLException {
        String query = "DELETE FROM payments_to_supplier WHERE payment_id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, paymentId);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    // Method to retrieve all payments to suppliers
    public List<PaymentsToSupplier> getAllPayments() throws SQLException {
        String query = "SELECT * FROM payments_to_supplier";
        List<PaymentsToSupplier> paymentsList = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                PaymentsToSupplier payment = mapResultSetToPayment(rs);
                paymentsList.add(payment);
            }
        }

        return paymentsList;
    }

    // Helper method to map ResultSet to PaymentsToSupplier object
    private PaymentsToSupplier mapResultSetToPayment(ResultSet rs) throws SQLException {
        PaymentsToSupplier payment = new PaymentsToSupplier();
        payment.setPaymentId(rs.getInt("payment_id"));
        payment.setSupplierId(rs.getInt("supplier_id"));
        payment.setPaymentDate(rs.getDate("payment_date").toLocalDate());
        payment.setAmount(rs.getBigDecimal("amount"));
        payment.setPaymentMethodId(rs.getInt("payment_method_id"));
        payment.setReferenceNumber(rs.getString("reference_number"));
        payment.setChartOfAccountId(rs.getInt("chart_of_account_id"));
        payment.setTransactionTypeId(rs.getInt("transaction_type_id"));
        payment.setNotes(rs.getString("notes"));
        payment.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        payment.setCreatedBy(rs.getInt("created_by"));
        return payment;
    }
}
