package com.vertex.vos.Utilities;

import com.vertex.vos.Constructors.PaymentTerms;
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
}
