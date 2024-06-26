package com.vertex.vos.Utilities;

import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StatusDAO {
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    public List<String> getAllTransactionStatuses() {
        List<String> transactionStatuses = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT status FROM transaction_status");
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                transactionStatuses.add(resultSet.getString("status"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle exceptions appropriately
        }
        return transactionStatuses;
    }

    public String getTransactionStatusById(int id) {
        String transactionStatus = null;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT status FROM transaction_status WHERE id = ?");
        ) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    transactionStatus = resultSet.getString("status");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return transactionStatus;
    }

    // New methods for payment status

    public List<String> getAllPaymentStatuses() {
        List<String> paymentStatuses = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT status FROM payment_status");
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                paymentStatuses.add(resultSet.getString("status"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle exceptions appropriately
        }
        return paymentStatuses;
    }

    public String getPaymentStatusById(int id) {
        String paymentStatus = null;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT status FROM payment_status WHERE id = ?");
        ) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    paymentStatus = resultSet.getString("status");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return paymentStatus;
    }
}
