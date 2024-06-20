package com.vertex.vos.Utilities;

import com.vertex.vos.Constructors.Vehicle;
import com.zaxxer.hikari.HikariDataSource;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class VehicleDAO {
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    public ObservableList<String> getAllVehicleTruckPlates() {
        ObservableList<String> vehicleTruckPlates = FXCollections.observableArrayList();
        String query = "SELECT vehicle_plate FROM vehicles";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                vehicleTruckPlates.add(resultSet.getString("vehicle_plate"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return vehicleTruckPlates;
    }

    public ObservableList<Vehicle> getAllVehicles() {
        ObservableList<Vehicle> vehicleList = FXCollections.observableArrayList();
        String sqlQuery = "SELECT v.*, vt.type_name " +
                "FROM vehicles v " +
                "INNER JOIN vehicle_type vt ON v.vehicle_type = vt.id";

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
        String sqlQuery = "INSERT INTO vehicles (vehicle_type, vehicle_plate, max_load, status, branch_id) VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sqlQuery)) {

            statement.setInt(1, vehicle.getVehicleType());
            statement.setString(2, vehicle.getVehiclePlate());
            statement.setDouble(3, vehicle.getMaxLoad());
            statement.setString(4, vehicle.getStatus());
            statement.setInt(5, vehicle.getBranchId()); // Assuming getBranchId() method exists in Vehicle class

            int rowsInserted = statement.executeUpdate();
            return rowsInserted > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    // Update an existing vehicle
    public boolean updateVehicle(Vehicle vehicle) {
        String sqlQuery = "UPDATE vehicles SET branch_id = ?, status = ?, max_load = ?, vehicle_type = ?, vehicle_plate = ? WHERE vehicle_id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sqlQuery)) {

            statement.setInt(1, vehicle.getBranchId());
            statement.setString(2, vehicle.getStatus());
            statement.setDouble(3, vehicle.getMaxLoad());
            statement.setInt(4, vehicle.getVehicleType());
            statement.setString(5, vehicle.getVehiclePlate());
            statement.setInt(6, vehicle.getVehicleId());

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
        int vehicleType = resultSet.getInt("vehicle_type");
        String vehiclePlate = resultSet.getString("vehicle_plate");
        double maxLoad = resultSet.getDouble("max_load");
        String status = resultSet.getString("status");
        int branchId = resultSet.getInt("branch_id");
        String vehicleTypeString = resultSet.getString("type_name"); // Assuming "type_name" is the column name

        return new Vehicle(vehicleId, vehicleType, vehicleTypeString, vehiclePlate, maxLoad, status, branchId);
    }


    public ObservableList<String> getAllVehicleTypeNames() {
        ObservableList<String> vehicleTypeNames = FXCollections.observableArrayList();
        String query = "SELECT type_name FROM vehicle_type";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                vehicleTypeNames.add(resultSet.getString("type_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return vehicleTypeNames;
    }

    public Integer getVehicleTypeIdByName(String typeName) {
        String query = "SELECT id FROM vehicle_type WHERE type_name = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, typeName);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getVehicleTypeNameById(int id) {
        String query = "SELECT type_name FROM vehicle_type WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, id);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("type_name");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

}
