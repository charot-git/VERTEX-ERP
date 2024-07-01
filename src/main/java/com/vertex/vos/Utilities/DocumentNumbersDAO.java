package com.vertex.vos.Utilities;

import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DocumentNumbersDAO {
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    public int getNextPurchaseOrderNumber() {
        return getNextNumber("purchase_order_numbers", "po_no");
    }

    public int getNextTripNumber() {
        return getNextNumber("trip_no", "trip_no");
    }

    public int getNextSupplierCreditNumber() {
        return getNextNumber("supplier_credit_debit_numbers", "supplier_credit_no");
    }

    public int getNextSupplierDebitNumber() {
        return getNextNumber("supplier_credit_debit_numbers", "supplier_debit_no");
    }

    private int getNextNumber(String tableName, String columnName) {
        int nextNumber = 0;
        String updateQuery = "UPDATE " + tableName + " SET " + columnName + " = LAST_INSERT_ID(" + columnName + " + 1)";
        String selectQuery = "SELECT LAST_INSERT_ID()";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {

            // Update the specified column by incrementing it by 1
            updateStatement.executeUpdate();

            try (PreparedStatement selectStatement = connection.prepareStatement(selectQuery);
                 ResultSet resultSet = selectStatement.executeQuery()) {

                if (resultSet.next()) {
                    nextNumber = resultSet.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception according to your application's needs
        }

        return nextNumber;
    }
}
