package com.vertex.vos.Utilities;

import com.vertex.vos.Constructors.TripSummary;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class TripSummaryDAO {

    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    public boolean saveTripSummary(TripSummary tripSummary) {
        String sql = "INSERT INTO trip_summary (trip_no, trip_date, vehicle_id, total_sales_orders, status) VALUES (?, ?, ?, ?, ?)";
        boolean success = false;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setString(1, tripSummary.getTripNo());
            preparedStatement.setDate(2, tripSummary.getTripDate());
            preparedStatement.setInt(3, tripSummary.getVehicleId());
            preparedStatement.setInt(4, tripSummary.getTotalSalesOrders());
            preparedStatement.setString(5, tripSummary.getStatus());

            int rowsAffected = preparedStatement.executeUpdate();

            // Check if insert was successful and retrieve the auto-generated trip_id
            if (rowsAffected == 1) {
                ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int tripId = generatedKeys.getInt(1);
                    tripSummary.setTripId(tripId);
                    success = true; // Set success to true if trip summary was successfully saved
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle or log the exception as needed
        }

        return success;
    }


    public TripSummary getTripSummaryByTripNo(String tripNo) throws SQLException {
        TripSummary tripSummary = null;
        String sql = "SELECT * FROM trip_summary WHERE trip_no = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, tripNo);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    tripSummary = mapResultSetToTripSummary(resultSet);
                }
            }
        }
        return tripSummary;
    }

    private TripSummary mapResultSetToTripSummary(ResultSet resultSet) throws SQLException {
        int tripId = resultSet.getInt("trip_id");
        String tripNo = resultSet.getString("trip_no");
        java.sql.Date tripDate = resultSet.getDate("trip_date");
        int vehicleId = resultSet.getInt("vehicle_id");
        int totalSalesOrders = resultSet.getInt("total_sales_orders");
        Timestamp createdAt = resultSet.getTimestamp("created_at");
        String status = resultSet.getString("status");

        return new TripSummary(tripId, tripNo, tripDate, vehicleId, totalSalesOrders, status, createdAt);
    }
}
