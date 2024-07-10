package com.vertex.vos.Utilities;

import com.zaxxer.hikari.HikariDataSource;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PurchaseOrderPaymentDAO {
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    public boolean insertPayment(int purchaseOrderId, double paidAmount, int chartOfAccount) {
        String sql = "INSERT INTO purchase_order_payment (purchase_order_id, paid_amount, chart_of_account) VALUES (?, ?, ?)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, purchaseOrderId);
            statement.setBigDecimal(2, BigDecimal.valueOf(paidAmount));
            statement.setInt(3, chartOfAccount);

            int rowsInserted = statement.executeUpdate();
            return rowsInserted > 0;

        } catch (SQLException e) {
            e.printStackTrace(); // Handle or log the exception as needed
            return false; // Return false indicating failure
        }
    }


    public void updatePayment(int paymentId, double paidAmount, int chartOfAccount) throws SQLException {
        String sql = "UPDATE purchase_order_payment SET paid_amount = ?, chart_of_account = ? WHERE purchase_order_totals_id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBigDecimal(1, BigDecimal.valueOf(paidAmount));
            statement.setInt(2, chartOfAccount);
            statement.setInt(3, paymentId);
            statement.executeUpdate();
        }
    }

    public void deletePayment(int paymentId) throws SQLException {
        String sql = "DELETE FROM purchase_order_payment WHERE purchase_order_totals_id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, paymentId);
            statement.executeUpdate();
        }
    }

    public BigDecimal getTotalPaidAmountForPurchaseOrder(int purchaseOrderId) throws SQLException {
        String sql = "SELECT SUM(paid_amount) AS total_paid_amount FROM purchase_order_payment WHERE purchase_order_id = ?";
        BigDecimal totalPaidAmount = BigDecimal.ZERO;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, purchaseOrderId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    totalPaidAmount = resultSet.getBigDecimal("total_paid_amount");
                }
            }
        }
        return totalPaidAmount;
    }
}
