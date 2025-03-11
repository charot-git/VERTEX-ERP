package com.vertex.vos.DAO;

import com.vertex.vos.Enums.TripSummaryStatus;
import com.vertex.vos.Objects.*;
import com.vertex.vos.Utilities.DatabaseConnectionPool;
import com.vertex.vos.Utilities.EmployeeDAO;
import com.vertex.vos.Utilities.VehicleDAO;
import com.zaxxer.hikari.HikariDataSource;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class TripSummaryDAO {

    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();
    private final VehicleDAO vehicleDAO = new VehicleDAO();
    private final ClusterDAO clusterDAO = new ClusterDAO();
    private final EmployeeDAO employeeDAO = new EmployeeDAO();

    public ObservableList<TripSummary> getFilteredTripSummaries(String tripNo, String cluster, String status, String vehicle, Timestamp dateFrom, Timestamp dateTo) {
        ObservableList<TripSummary> tripSummaries = FXCollections.observableArrayList();

        String query = "SELECT * FROM trip_summary WHERE 1=1";

        if (tripNo != null && !tripNo.isEmpty()) query += " AND trip_no LIKE ?";
        if (cluster != null && !cluster.isEmpty())
            query += " AND cluster_id IN (SELECT id FROM cluster WHERE cluster_name LIKE ?)";
        if (status != null && !status.isEmpty()) query += " AND status = ?";
        if (vehicle != null && !vehicle.isEmpty())
            query += " AND vehicle_id IN (SELECT vehicle_id FROM vehicles WHERE vehicle_plate LIKE ?)";
        if (dateFrom != null) query += " AND trip_date >= ?";
        if (dateTo != null) query += " AND trip_date <= ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            int index = 1;
            if (tripNo != null && !tripNo.isEmpty()) stmt.setString(index++, "%" + tripNo + "%");
            if (cluster != null && !cluster.isEmpty()) stmt.setString(index++, "%" + cluster + "%");
            if (status != null && !status.isEmpty()) stmt.setString(index++, status);
            if (vehicle != null && !vehicle.isEmpty()) stmt.setString(index++, "%" + vehicle + "%");
            if (dateFrom != null) stmt.setTimestamp(index++, dateFrom);
            if (dateTo != null) stmt.setTimestamp(index++, dateTo);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                TripSummary trip = mapResultSetToTripSummary(rs);
                if (trip != null) {
                    tripSummaries.add(trip);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return tripSummaries;
    }

    private TripSummary mapResultSetToTripSummary(ResultSet rs) {
        try {
            TripSummary trip = new TripSummary();
            trip.setTripId(rs.getInt("trip_id"));
            trip.setTripNo(rs.getString("trip_no"));
            trip.setTripDate(rs.getTimestamp("trip_date"));

            // Set vehicle object
            Vehicle vehicle = vehicleDAO.getVehicleById(rs.getInt("vehicle_id"));
            trip.setVehicle(vehicle);

            // Set dispatcher (if applicable)
            User dispatchBy = employeeDAO.getUserById(rs.getInt("dispatch_by"));
            trip.setDispatchBy(dispatchBy);

            // Set cluster (if applicable)
            Cluster cluster = clusterDAO.getClusterById(rs.getInt("cluster_id"));
            trip.setCluster(cluster);

            // Set created by
            User createdBy = employeeDAO.getUserById(rs.getInt("created_by"));
            trip.setCreatedBy(createdBy);

            trip.setCreatedAt(rs.getTimestamp("created_at"));
            trip.setTripAmount(rs.getDouble("trip_amount"));

            // Set status (convert from String to Enum safely)
            try {
                trip.setStatus(TripSummaryStatus.TripStatus.valueOf(rs.getString("status")));
            } catch (IllegalArgumentException e) {
                System.err.println("Invalid status value: " + rs.getString("status"));
                trip.setStatus(TripSummaryStatus.TripStatus.Pending); // Default to Picking
            }

            return trip;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String generateNextTripNo() {
        String selectQuery = "SELECT trip_no FROM trip_no FOR UPDATE";
        String updateQuery = "UPDATE trip_no SET trip_no = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement selectStmt = connection.prepareStatement(selectQuery);
             PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {

            connection.setAutoCommit(false);

            try (ResultSet resultSet = selectStmt.executeQuery()) {
                if (resultSet.next()) {
                    int no = resultSet.getInt("trip_no");
                    int nextNo = no + 1;

                    updateStmt.setInt(1, nextNo);
                    updateStmt.executeUpdate();
                    connection.commit();

                    return String.format("TRIP-%05d", nextNo);
                }
            }
            connection.rollback();
        } catch (SQLException e) {
            if ("40001".equals(e.getSQLState())) { // Deadlock detected, retry
                return generateNextTripNo();
            }
            throw new RuntimeException("Failed to generate new trip number", e);
        }
        return null;
    }

    public boolean saveTrip(TripSummary tripSummary) {
        String insertTripSQL = """
                INSERT INTO trip_summary (trip_no, trip_date, vehicle_id, created_by, status, trip_amount, dispatch_by, cluster_id)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

        String insertTripDetailSQL = """
                INSERT INTO trip_summary_details (trip_id, invoice_id)
                VALUES (?, ?)
            """;

        String updateInvoicesSQL = """
                UPDATE sales_invoice
                SET transaction_status = 'Picking'
                WHERE invoice_id = ?
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement tripStmt = conn.prepareStatement(insertTripSQL, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement tripDetailStmt = conn.prepareStatement(insertTripDetailSQL);
             PreparedStatement updateInvoicesStmt = conn.prepareStatement(updateInvoicesSQL)) {

            conn.setAutoCommit(false);

            tripStmt.setString(1, tripSummary.getTripNo());
            tripStmt.setTimestamp(2, tripSummary.getTripDate() == null ? null : Timestamp.valueOf(tripSummary.getTripDate().toLocalDateTime()));
            tripStmt.setObject(3, tripSummary.getVehicle() == null ? null : tripSummary.getVehicle().getVehicleId(), Types.INTEGER);
            tripStmt.setObject(4, tripSummary.getCreatedBy() == null ? null : tripSummary.getCreatedBy().getUser_id(), Types.INTEGER);
            tripStmt.setString(5, tripSummary.getStatus() == null ? null : tripSummary.getStatus().name());
            tripStmt.setObject(6, tripSummary.getTripAmount() == 0.0 ? null : tripSummary.getTripAmount(), Types.DOUBLE);
            tripStmt.setObject(7, tripSummary.getDispatchBy() == null ? null : tripSummary.getDispatchBy().getUser_id(), Types.INTEGER);
            tripStmt.setObject(8, tripSummary.getCluster() == null ? null : tripSummary.getCluster().getId(), Types.INTEGER);

            if (tripStmt.executeUpdate() == 0) {
                conn.rollback();
                return false;
            }

            try (ResultSet generatedKeys = tripStmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int tripId = generatedKeys.getInt(1);

                    for (SalesInvoiceHeader invoice : tripSummary.getSalesInvoices()) {
                        tripDetailStmt.setInt(1, tripId);
                        tripDetailStmt.setInt(2, invoice.getInvoiceId());
                        tripDetailStmt.addBatch();

                        // Update invoice status
                        updateInvoicesStmt.setInt(1, invoice.getInvoiceId());
                        updateInvoicesStmt.addBatch();
                    }

                    tripDetailStmt.executeBatch();
                    updateInvoicesStmt.executeBatch(); // Execute invoice updates
                } else {
                    conn.rollback();
                    return false;
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


}