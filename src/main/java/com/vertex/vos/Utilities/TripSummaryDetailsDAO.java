package com.vertex.vos.Utilities;

import com.vertex.vos.Objects.SalesOrderHeader;
import com.vertex.vos.Objects.TripSummary;
import com.vertex.vos.Objects.TripSummaryDetails;
import com.vertex.vos.Objects.TripSummaryStaff;
import com.zaxxer.hikari.HikariDataSource;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

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


    public ObservableList<Integer> getDetailsByTripId(int tripId) throws SQLException {
        ObservableList<Integer> orderIds = FXCollections.observableArrayList();
        String sql = "SELECT order_id FROM trip_summary_details WHERE trip_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, tripId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    int orderId = resultSet.getInt("order_id");
                    orderIds.add(orderId);
                }
            }
        }
        return orderIds;
    }

    private TripSummaryDetails mapResultSetToTripSummaryDetails(ResultSet resultSet) throws SQLException {
        int detailId = resultSet.getInt("detail_id");
        int tripId = resultSet.getInt("trip_id");
        int orderId = resultSet.getInt("order_id");

        return new TripSummaryDetails(detailId, tripId, orderId);
    }

    public boolean saveLogisticsDetails(TripSummary trip) {
        String sql = "UPDATE trip_summary SET trip_date = ?, vehicle_id = ?, total_sales_orders = ?, status = ?, created_by = ?, dispatch_by = ? WHERE trip_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setDate(1, trip.getTripDate());
            preparedStatement.setInt(2, trip.getVehicleId());
            preparedStatement.setInt(3, trip.getTotalSalesOrders());
            preparedStatement.setString(4, trip.getStatus());
            preparedStatement.setInt(5, trip.getCreatedBy());
            preparedStatement.setInt(6, trip.getDispatchBy());
            preparedStatement.setInt(7, trip.getTripId());

            int rowsAffected = preparedStatement.executeUpdate();

            return rowsAffected == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean saveLogisticsStaff(TripSummary trip, List<TripSummaryStaff> staffList) {
        String sql = "INSERT IGNORE INTO trip_summary_staff (trip_id, staff_name, role) VALUES (?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            for (TripSummaryStaff staff : staffList) {
                preparedStatement.setInt(1, trip.getTripId());
                preparedStatement.setString(2, staff.getStaffName());
                preparedStatement.setString(3, staff.getRole());
                preparedStatement.addBatch();
            }

            int[] rowsAffected = preparedStatement.executeBatch();

            return Arrays.stream(rowsAffected).sum() == staffList.size();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
