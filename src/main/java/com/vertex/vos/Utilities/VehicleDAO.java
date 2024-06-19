package com.vertex.vos.Utilities;

import com.vertex.vos.Constructors.Vehicle;
import com.zaxxer.hikari.HikariDataSource;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.*;

public class VehicleDAO {
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    // Retrieve all vehicles
    public ObservableList<Vehicle> getAllVehicles() {
        ObservableList<Vehicle> vehicleList = FXCollections.observableArrayList();
        String sqlQuery = "SELECT * FROM vehicles";

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sqlQuery)) {

            while (resultSet.next()) {
                Vehicle vehicle = extractVehicleFromResultSet(resultSet);
                vehicleList.add(vehicle);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return vehicleList;
    }

    // Insert a new vehicle
    public boolean insertVehicle(Vehicle vehicle) {
        String sqlQuery = "INSERT INTO vehicles (vehicle_type, vehicle_plate, max_load, status) VALUES (?, ?, ?, ?)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sqlQuery)) {

            statement.setString(1, vehicle.getVehicleType());
            statement.setString(2, vehicle.getVehiclePlate());
            statement.setDouble(3, vehicle.getMaxLoad());
            statement.setString(4, vehicle.getStatus());

            int rowsInserted = statement.executeUpdate();
            return rowsInserted > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Update an existing vehicle
    public boolean updateVehicle(Vehicle vehicle) {
        String sqlQuery = "UPDATE vehicles SET vehicle_type = ?, vehicle_plate = ?, max_load = ?, status = ? WHERE vehicle_id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sqlQuery)) {

            statement.setString(1, vehicle.getVehicleType());
            statement.setString(2, vehicle.getVehiclePlate());
            statement.setDouble(3, vehicle.getMaxLoad());
            statement.setString(4, vehicle.getStatus());
            statement.setInt(5, vehicle.getVehicleId());

            int rowsUpdated = statement.executeUpdate();
            return rowsUpdated > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Delete a vehicle
    public boolean deleteVehicle(int vehicleId) {
        String sqlQuery = "DELETE FROM vehicles WHERE vehicle_id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sqlQuery)) {

            statement.setInt(1, vehicleId);

            int rowsDeleted = statement.executeUpdate();
            return rowsDeleted > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Helper method to extract a Vehicle object from ResultSet
    private Vehicle extractVehicleFromResultSet(ResultSet resultSet) throws SQLException {
        int vehicleId = resultSet.getInt("vehicle_id");
        String vehicleType = resultSet.getString("vehicle_type");
        String vehiclePlate = resultSet.getString("vehicle_plate");
        double maxLoad = resultSet.getDouble("max_load");
        String status = resultSet.getString("status");

        return new Vehicle(vehicleId, vehicleType, vehiclePlate, maxLoad, status);
    }
}
