package com.vertex.vos.Utilities;

import com.vertex.vos.Objects.Product;
import com.vertex.vos.Objects.Unit;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class UnitDAO {
    private final Map<Integer, Unit> unitCache = new HashMap<>();
    private final Map<String, Integer> unitNameToIdCache = new HashMap<>();

    public UnitDAO() {
        loadUnitCache(); // Load cache on initialization
    }

    private void loadUnitCache() {
        String sqlQuery = "SELECT * FROM units ORDER BY `order`";

        try (Connection connection = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            unitCache.clear();
            unitNameToIdCache.clear();

            while (resultSet.next()) {
                int unitId = resultSet.getInt("unit_id");
                String unitName = resultSet.getString("unit_name");
                String unitShortcut = resultSet.getString("unit_shortcut");
                int order = resultSet.getInt("order");

                Unit unit = new Unit(unitId, unitName, unitShortcut, order);
                unitCache.put(unitId, unit);
                unitNameToIdCache.put(unitName, unitId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ObservableList<String> getUnitNames() {
        return FXCollections.observableArrayList(unitNameToIdCache.keySet());
    }

    public ObservableList<Unit> getUnitDetails() {
        return FXCollections.observableArrayList(unitCache.values());
    }

    public boolean createUnit(String unitName) {
        String insertQuery = "INSERT INTO units (unit_name) VALUES (?)";

        try (Connection connection = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(insertQuery, PreparedStatement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setString(1, unitName);
            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int newUnitId = generatedKeys.getInt(1);
                        Unit newUnit = new Unit(newUnitId, unitName, "", 0);
                        unitCache.put(newUnitId, newUnit);
                        unitNameToIdCache.put(unitName, newUnitId);
                    }
                }
                System.out.println("Unit created successfully: " + unitName);
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            DialogUtils.showErrorMessage("Database Error", "An error occurred while creating the unit: " + unitName);
        }
        return false;
    }

    public int getUnitIdByName(String unitName) {
        return unitNameToIdCache.getOrDefault(unitName, -1);
    }

    public String getUnitNameById(int unitId) {
        Unit unit = unitCache.get(unitId);
        return (unit != null) ? unit.getUnit_name() : null;
    }

    public ObservableList<Unit> getAllUnits() {
        return FXCollections.observableArrayList(unitCache.values());
    }

    public Unit getUnitDetail(Product productToConvert) {
        return unitCache.get(productToConvert.getUnitOfMeasurement());
    }
}