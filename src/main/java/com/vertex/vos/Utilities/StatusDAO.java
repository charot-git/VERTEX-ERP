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
             PreparedStatement statement = connection.prepareStatement("SELECT transaction_name FROM transaction_status");
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                transactionStatuses.add(resultSet.getString("transaction_name"));
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
             PreparedStatement statement = connection.prepareStatement("SELECT transaction_name FROM transaction_status WHERE id = ?");
        ) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    transactionStatus = resultSet.getString("transaction_name");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return transactionStatus;
    }
}
