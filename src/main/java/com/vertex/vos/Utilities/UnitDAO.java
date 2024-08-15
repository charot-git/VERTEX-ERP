package com.vertex.vos.Utilities;

import com.vertex.vos.Objects.Unit;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UnitDAO {

    public ObservableList<String> getUnitNames() {
        ObservableList<String> unitNames = FXCollections.observableArrayList();
        String sqlQuery = "SELECT unit_name FROM units";

        try (Connection connection = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                String unitName = resultSet.getString("unit_name");
                unitNames.add(unitName);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            // Handle any SQL exceptions here
        }
        return unitNames;
    }

    public ObservableList<Unit> getUnitDetails() {
        ObservableList<Unit> unitList = FXCollections.observableArrayList();
        String sqlQuery = "SELECT * FROM units";

        try (Connection connection = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                int unitId = resultSet.getInt("unit_id");
                String unitName = resultSet.getString("unit_name");
                String unitShortcut = resultSet.getString("unit_shortcut");
                int order = resultSet.getInt("order");

                Unit unit = new Unit(unitId, unitName, unitShortcut, order);
                unitList.add(unit);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            // Handle any SQL exceptions here
        }
        return unitList;
    }

    public boolean createUnit(String unitName) {
        String insertQuery = "INSERT INTO units (unit_name) VALUES (?)";

        try (Connection connection = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {

            preparedStatement.setString(1, unitName);
            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Unit created successfully: " + unitName);
                return true;
            } else {
                System.out.println("Failed to create unit: " + unitName);
                return false;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            // Handle any SQL exceptions here
            // Optionally, show an error message dialog to the user
            DialogUtils.showErrorMessage("Database Error", "An error occurred while creating the unit: " + unitName);
            return false;
        }
    }


    public int getUnitIdByName(String unitName) {
        String sqlQuery = "SELECT unit_id FROM units WHERE unit_name = ?";
        int unitId = -1; // Set a default value indicating not found

        try (Connection connection = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {

            preparedStatement.setString(1, unitName);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    unitId = resultSet.getInt("unit_id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle any SQL exceptions here
        }

        return unitId;
    }

    public String getUnitNameById(int unitId) {
        String sqlQuery = "SELECT unit_name FROM units WHERE unit_id = ?";
        String unitName = null; // Set default value indicating not found

        try (Connection connection = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {

            preparedStatement.setInt(1, unitId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    unitName = resultSet.getString("unit_name");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle any SQL exceptions here
        }

        return unitName;
    }

    public ObservableList<Unit> getAllUnits() {
        ObservableList<Unit> unitList = FXCollections.observableArrayList();
        String sqlQuery = "SELECT * FROM units ORDER BY `order`";

        try (Connection connection = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                int unitId = resultSet.getInt("unit_id");
                String unitName = resultSet.getString("unit_name");
                String unitShortcut = resultSet.getString("unit_shortcut");
                int order = resultSet.getInt("order");

                Unit unit = new Unit(unitId, unitName, unitShortcut, order);
                unitList.add(unit);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            // Handle any SQL exceptions here
        }
        return unitList;
    }
}
