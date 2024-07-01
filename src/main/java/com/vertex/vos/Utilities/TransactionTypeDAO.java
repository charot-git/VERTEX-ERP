package com.vertex.vos.Utilities;

import com.vertex.vos.Objects.TransactionType;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TransactionTypeDAO {
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    public String getTransactionTypeById(int id) {
        String transactionType = null;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT transaction_type FROM transaction_type WHERE id = ?");
        ) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    transactionType = resultSet.getString("transaction_type");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle exceptions appropriately
        }
        return transactionType;
    }

    public List<String> getAllTransactionTypeNames() {
        List<String> transactionTypeNames = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT transaction_type FROM transaction_type");
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                String transactionTypeName = resultSet.getString("transaction_type");
                transactionTypeNames.add(transactionTypeName);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle exceptions appropriately
        }
        return transactionTypeNames;
    }

    public List<TransactionType> getAllTransactionTypes() {
        List<TransactionType> transactionTypes = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM transaction_type");
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String transactionTypeName = resultSet.getString("transaction_type");
                TransactionType transactionType = new TransactionType(id, transactionTypeName);
                transactionTypes.add(transactionType);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle exceptions appropriately
        }
        return transactionTypes;
    }

}
