package com.vertex.vos.Utilities;

import com.vertex.vos.Objects.BalanceType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BalanceTypeDAO {
    public String getBalanceTypeNameById(int id) {
        String sqlQuery = "SELECT balance_name FROM balance_type WHERE id = ?";
        String name = null; // Default value if not found

        try (Connection connection = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {

            preparedStatement.setInt(1, id);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    name = resultSet.getString("balance_name");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle any SQL exceptions here
        }
        return name;
    }

    public ObservableList<BalanceType> getAllBalanceTypes() {
        String sqlQuery = "SELECT * FROM balance_type";
        ObservableList<BalanceType> balanceTypes = FXCollections.observableArrayList();

        try (Connection connection = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("balance_name");
                BalanceType balanceType = new BalanceType();
                balanceType.setId(id);
                balanceType.setBalanceName(name);
                balanceTypes.add(balanceType);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle any SQL exceptions here
        }
        return balanceTypes;
    }

    public int getBalanceTypeByString(String value) {
        String sqlQuery = "SELECT id FROM balance_type WHERE balance_name = ?";
        int id = 0; // Default value if not found

        try (Connection connection = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {

            preparedStatement.setString(1, value);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    id = resultSet.getInt("id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle any SQL exceptions here
        }
        return id;
    }

    public BalanceType getBalanceTypeById(int balanceTypeId) {
        String sqlQuery = "SELECT * FROM balance_type WHERE id = ?";
        BalanceType balanceType = null; // Default value if not found

        try (Connection connection = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {

            preparedStatement.setInt(1, balanceTypeId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    String name = resultSet.getString("balance_name");
                    balanceType = new BalanceType();
                    balanceType.setId(id);
                    balanceType.setBalanceName(name);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle any SQL exceptions here
        }
        return balanceType;
    }
}


