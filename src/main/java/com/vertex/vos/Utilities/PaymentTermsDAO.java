package com.vertex.vos.Utilities;

import com.vertex.vos.Objects.PaymentTerms;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PaymentTermsDAO {
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    public void insertPaymentTerm(PaymentTerms paymentTerms) throws SQLException {
        String query = "INSERT INTO payment_terms (id, payment_name, payment_days) VALUES (?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, paymentTerms.getId());
            preparedStatement.setString(2, paymentTerms.getPaymentName());
            preparedStatement.setInt(3, paymentTerms.getPaymentDays());

            preparedStatement.executeUpdate();
        }
    }

    public int getPaymentTermIdByName(String name) throws SQLException {
        String query = "SELECT id FROM payment_terms WHERE payment_name = ?";
        int paymentTermId = 0;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, name);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    paymentTermId = resultSet.getInt("id");
                }
            }
        }
        return paymentTermId;
    }

    public String getPaymentTermNameById(int id) {
        String query = "SELECT payment_name FROM payment_terms WHERE id = ?";
        String paymentTermName = null;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, id);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    paymentTermName = resultSet.getString("payment_name");
                } else {
                    throw new SQLException("No payment term found for ID: " + id);
                }
            }
        } catch (SQLException e) {
            // Handle the exception here
            e.printStackTrace();
            // You can also re-throw the exception or return a default value
            // depending on your requirements
            throw new RuntimeException("Error getting payment term name", e);
        }
        return paymentTermName;
    }
}
