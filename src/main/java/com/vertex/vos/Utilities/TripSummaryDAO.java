package com.vertex.vos.Utilities;

import com.vertex.vos.Objects.TripSummary;
import com.zaxxer.hikari.HikariDataSource;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class TripSummaryDAO {

    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    public boolean saveTripSummary(TripSummary tripSummary) {
        String sql = "INSERT INTO trip_summary (trip_no, trip_date, vehicle_id, total_sales_orders, status, created_by, dispatch_by) VALUES (?, ?, ?, ?, ?, ?, ?)";
        boolean success = false;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setString(1, tripSummary.getTripNo());
            preparedStatement.setDate(2, tripSummary.getTripDate());
            preparedStatement.setInt(3, tripSummary.getVehicleId());
            preparedStatement.setInt(4, tripSummary.getTotalSalesOrders());
            preparedStatement.setString(5, tripSummary.getStatus());
            preparedStatement.setInt(6, tripSummary.getCreatedBy());
            preparedStatement.setInt(7, tripSummary.getDispatchBy());

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected == 1) {
                ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int tripId = generatedKeys.getInt(1);
                    tripSummary.setTripId(tripId);
                    success = true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
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

    public ObservableList<TripSummary> getAllTripSummaries() throws SQLException {
        ObservableList<TripSummary> tripSummaries = FXCollections.observableArrayList();
        String sql = "SELECT * FROM trip_summary";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                TripSummary tripSummary = mapResultSetToTripSummary(resultSet);
                tripSummaries.add(tripSummary);
            }
        }
        return tripSummaries;
    }

    public boolean deleteTripSummaryByTripNo(String tripNo) throws SQLException {
        String sql = "DELETE FROM trip_summary WHERE trip_no = ?";
        boolean success = false;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, tripNo);
            int rowsAffected = preparedStatement.executeUpdate();
            success = rowsAffected > 0;
        }
        return success;
    }

    public boolean deleteTripSummaryById(int tripId) throws SQLException {
        String sql = "DELETE FROM trip_summary WHERE trip_id = ?";
        boolean success = false;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, tripId);
            int rowsAffected = preparedStatement.executeUpdate();
            success = rowsAffected > 0;
        }
        return success;
    }

    public boolean updateTripSummary(TripSummary tripSummary) {
        String sql = "UPDATE trip_summary SET trip_no = ?, trip_date = ?, vehicle_id = ?, total_sales_orders = ?, status = ?, created_by = ?, dispatch_by = ? WHERE trip_id = ?";
        boolean success = false;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, tripSummary.getTripNo());
            preparedStatement.setDate(2, tripSummary.getTripDate());
            preparedStatement.setInt(3, tripSummary.getVehicleId());
            preparedStatement.setInt(4, tripSummary.getTotalSalesOrders());
            preparedStatement.setString(5, tripSummary.getStatus());
            preparedStatement.setInt(6, tripSummary.getCreatedBy());
            preparedStatement.setInt(7, tripSummary.getDispatchBy());
            preparedStatement.setInt(8, tripSummary.getTripId());

            int rowsAffected = preparedStatement.executeUpdate();
            success = rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return success;
    }



    private TripSummary mapResultSetToTripSummary(ResultSet resultSet) throws SQLException {
        int tripId = resultSet.getInt("trip_id");
        String tripNo = resultSet.getString("trip_no");
        java.sql.Date tripDate = resultSet.getDate("trip_date");
        int vehicleId = resultSet.getInt("vehicle_id");
        int totalSalesOrders = resultSet.getInt("total_sales_orders");
        Timestamp createdAt = resultSet.getTimestamp("created_at");
        String status = resultSet.getString("status");
        int createdBy = resultSet.getInt("created_by");
        int dispatchBy = resultSet.getInt("dispatch_by");
        return new TripSummary(tripId, tripNo, tripDate, vehicleId, totalSalesOrders, status, createdAt, createdBy, dispatchBy);
    }
}
