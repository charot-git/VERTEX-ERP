package com.vertex.vos.Utilities;

import com.vertex.vos.Objects.Vehicle;
import com.zaxxer.hikari.HikariDataSource;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.math.BigDecimal;
import java.sql.*;

public class VehicleDAO {
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    public ObservableList<String> getAllVehicleTruckPlatesByStatus(String status) {
        ObservableList<String> vehicleTruckPlates = FXCollections.observableArrayList();
        String query = "SELECT vehicle_plate FROM vehicles WHERE status = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, status);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    vehicleTruckPlates.add(resultSet.getString("vehicle_plate"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return vehicleTruckPlates;
    }

    public BigDecimal getVehicleMinimumLoadByTruckPlate(String vehiclePlate) {
        String query = "SELECT minimum_load FROM vehicles WHERE vehicle_plate = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, vehiclePlate);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getBigDecimal("minimum_load");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return BigDecimal.ZERO;
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
        String sqlQuery = "INSERT INTO vehicles (vehicle_type, vehicle_plate, minimum_load, status, branch_id) VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sqlQuery)) {

            statement.setInt(1, vehicle.getVehicleType());
            statement.setString(2, vehicle.getVehiclePlate());
            statement.setDouble(3, vehicle.getMinimumLoad());
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
        String sqlQuery = "UPDATE vehicles SET branch_id = ?, status = ?, minimum_load = ?, vehicle_type = ?, vehicle_plate = ? WHERE vehicle_id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sqlQuery)) {

            statement.setInt(1, vehicle.getBranchId());
            statement.setString(2, vehicle.getStatus());
            statement.setDouble(3, vehicle.getMinimumLoad());
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
        double maxLoad = resultSet.getDouble("minimum_load");
        String status = resultSet.getString("status");
        int branchId = resultSet.getInt("branch_id");
        String vehicleTypeString = getVehicleTypeNameById(vehicleType);

        return new Vehicle(vehicleId, vehicleType, vehicleTypeString, vehiclePlate, maxLoad, status, branchId);
    }

    //getVehicleTypeNameById



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

    //set status by id
    public boolean setStatusById(int id, String status) {
        String query = "UPDATE vehicles SET status = ? WHERE vehicle_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, status);
            preparedStatement.setInt(2, id);
            int rowsUpdated = preparedStatement.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int getVehicleIdByName(String selectedItem) {
        String query = "SELECT vehicle_id FROM vehicles WHERE vehicle_plate = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, selectedItem);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("vehicle_id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public String getTruckPlateById(int vehicleId) {
        String query = "SELECT vehicle_plate FROM vehicles WHERE vehicle_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, vehicleId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("vehicle_plate");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Vehicle getVehicleById(int vehicleId) {
        String query = "SELECT * FROM vehicles WHERE vehicle_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, vehicleId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return extractVehicleFromResultSet(resultSet);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
