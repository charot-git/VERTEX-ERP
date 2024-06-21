package com.vertex.vos.Utilities;

import com.vertex.vos.Constructors.SalesOrderHeader;
import com.vertex.vos.Constructors.TripSummaryDetails;
import com.zaxxer.hikari.HikariDataSource;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TripSummaryDetailsDAO {

    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    SalesOrderDAO salesOrderDAO = new SalesOrderDAO();

    public boolean saveTripSummaryDetails(ObservableList<SalesOrderHeader> salesOrders, int tripId) throws SQLException {
        String insertSql = "INSERT INTO trip_summary_details (trip_id, order_id) VALUES (?, ?)";
        String updateSql = "UPDATE tbl_orders SET status = ? WHERE orderID = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement insertStatement = connection.prepareStatement(insertSql, PreparedStatement.RETURN_GENERATED_KEYS);
             PreparedStatement updateStatement = connection.prepareStatement(updateSql)) {

            // Insert trip summary details
            for (SalesOrderHeader salesOrder : salesOrders) {
                insertStatement.setInt(1, tripId);
                insertStatement.setInt(2, salesOrder.getOrderId());
                insertStatement.addBatch();
            }

            int[] insertBatchResult = insertStatement.executeBatch();

            for (int i = 0; i < insertBatchResult.length; i++) {
                int rowsAffected = insertBatchResult[i];
                if (rowsAffected <= 0) {
                    throw new SQLException("Failed to insert trip summary detail at index " + i);
                }
            }

            // Update sales order statuses
            for (SalesOrderHeader salesOrder : salesOrders) {
                salesOrder.setStatus("For Layout");
                updateStatement.setString(1, salesOrder.getStatus());
                updateStatement.setInt(2, salesOrder.getOrderId());
                updateStatement.addBatch();
            }

            int[] updateBatchResult = updateStatement.executeBatch();

            for (int i = 0; i < updateBatchResult.length; i++) {
                int rowsUpdated = updateBatchResult[i];
                if (rowsUpdated <= 0) {
                    throw new SQLException("Failed to update sales order status at index " + i);
                }
            }

            return true;
        } catch (SQLException e) {
            throw new RuntimeException("Error saving trip summary details", e);
        }
    }


    public List<TripSummaryDetails> getDetailsByTripId(int tripId) throws SQLException {
        List<TripSummaryDetails> detailsList = new ArrayList<>();
        String sql = "SELECT * FROM trip_summary_details WHERE trip_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, tripId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    TripSummaryDetails details = mapResultSetToTripSummaryDetails(resultSet);
                    detailsList.add(details);
                }
            }
        }
        return detailsList;
    }

    private TripSummaryDetails mapResultSetToTripSummaryDetails(ResultSet resultSet) throws SQLException {
        int detailId = resultSet.getInt("detail_id");
        int tripId = resultSet.getInt("trip_id");
        int orderId = resultSet.getInt("order_id");

        return new TripSummaryDetails(detailId, tripId, orderId);
    }
}
