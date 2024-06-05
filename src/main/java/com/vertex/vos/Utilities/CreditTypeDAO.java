package com.vertex.vos.Utilities;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.zaxxer.hikari.HikariDataSource;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class CreditTypeDAO {

    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    public int getCreditTypeIdByName(String creditTypeName) throws SQLException {
        int creditTypeId = -1; // Default value if credit type is not found
        String query = "SELECT id FROM credit_type WHERE credit_name = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, creditTypeName);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    creditTypeId = resultSet.getInt("id");
                }
            }
        }

        return creditTypeId;
    }

    public ObservableList<String> getAllCreditTypeNames() throws SQLException {
        ObservableList<String> creditTypeNames = FXCollections.observableArrayList();
        String query = "SELECT credit_name FROM credit_type";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                creditTypeNames.add(resultSet.getString("credit_name"));
            }
        }

        return creditTypeNames;
    }

    public String getCreditTypeNameById(int creditTypeId) throws SQLException {
        String creditTypeName = null; // Default value if credit type is not found
        String query = "SELECT credit_name FROM credit_type WHERE id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, creditTypeId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    creditTypeName = resultSet.getString("credit_name");
                }
            }
        }

        return creditTypeName;
    }


}