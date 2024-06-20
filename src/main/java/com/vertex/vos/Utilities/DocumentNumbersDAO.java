package com.vertex.vos.Utilities;

import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DocumentNumbersDAO {
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    public int getNextPurchaseOrderNumber() {
        int nextPO = 0;
        String updateQuery = "UPDATE purchase_order_numbers SET po_no = LAST_INSERT_ID(po_no + 1)";
        String selectQuery = "SELECT LAST_INSERT_ID()";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {

            // Update the po_no by incrementing it by 1
            updateStatement.executeUpdate();

            try (PreparedStatement selectStatement = connection.prepareStatement(selectQuery);
                 ResultSet resultSet = selectStatement.executeQuery()) {

                if (resultSet.next()) {
                    nextPO = resultSet.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception according to your application's needs
        }

        return nextPO;
    }

    public int getNextTripNumber() {
        int nextTripNumber = 0;
        String updateQuery = "UPDATE trip_no SET trip_no = LAST_INSERT_ID(trip_no + 1)";
        String selectQuery = "SELECT LAST_INSERT_ID()";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {

            // Update the trip_no by incrementing it by 1
            updateStatement.executeUpdate();

            try (PreparedStatement selectStatement = connection.prepareStatement(selectQuery);
                 ResultSet resultSet = selectStatement.executeQuery()) {

                if (resultSet.next()) {
                    nextTripNumber = resultSet.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception according to your application's needs
        }

        return nextTripNumber;
    }
}
