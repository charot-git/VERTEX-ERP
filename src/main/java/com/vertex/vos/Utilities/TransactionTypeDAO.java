package com.vertex.vos.Utilities;

import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TransactionTypeDAO {
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    public List<String> getAllTransactionTypes() {
        List<String> transactionTypes = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT transaction_type FROM transaction_type");
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                transactionTypes.add(resultSet.getString("transaction_type"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle exceptions appropriately
        }
        return transactionTypes;
    }

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

    // Add other CRUD methods as needed
}
