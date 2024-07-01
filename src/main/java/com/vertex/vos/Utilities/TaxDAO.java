package com.vertex.vos.Utilities;

import com.vertex.vos.Objects.Tax;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TaxDAO {
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    public Tax getTaxRates(int taxId) {
        String query = "SELECT WithholdingRate, VATRate FROM tax_rates WHERE TaxID = ?";
        Tax tax = null;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, taxId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                double withholdingRate = resultSet.getDouble("WithholdingRate");
                double vatRate = resultSet.getDouble("VATRate");
                tax = new Tax(withholdingRate, vatRate);
            } else {
                // Handle the case where the specified TaxID was not found
                System.out.println("Tax rates not found for TaxID: " + taxId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle any SQL exceptions here
        }

        return tax;
    }
}
