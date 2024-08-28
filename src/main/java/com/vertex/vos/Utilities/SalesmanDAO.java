package com.vertex.vos.Utilities;

import com.vertex.vos.Objects.Salesman;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SalesmanDAO {

    // Method to insert a new salesman into the database
    public boolean createSalesman(Salesman salesman) {
        String sqlQuery = "INSERT INTO salesman (employee_id, salesman_code, salesman_name, truck_plate, division_id, branch_code, operation, company_code, supplier_code, price_type, isActive, isInventory, canCollect, inventory_day, modified_date, encoder_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), ?)";

        try (Connection connection = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {

            preparedStatement.setInt(1, salesman.getEmployeeId());
            preparedStatement.setString(2, salesman.getSalesmanCode());
            preparedStatement.setString(3, salesman.getSalesmanName());
            preparedStatement.setString(4, salesman.getTruckPlate());
            preparedStatement.setInt(5, salesman.getDivisionId());
            preparedStatement.setInt(6, salesman.getBranchCode());
            preparedStatement.setInt(7, salesman.getOperation());
            preparedStatement.setInt(8, salesman.getCompanyCode());
            preparedStatement.setInt(9, salesman.getSupplierCode());
            preparedStatement.setString(10, salesman.getPriceType());
            preparedStatement.setBoolean(11, salesman.isActive());
            preparedStatement.setBoolean(12, salesman.isInventory());
            preparedStatement.setBoolean(13, salesman.isCanCollect());
            preparedStatement.setInt(14, salesman.getInventoryDay());
            preparedStatement.setInt(15, salesman.getEncoderId());

            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            // Handle any SQL exceptions here
            return false;
        }
    }

    public ObservableList<String> getAllSalesmanNames() {
        ObservableList<String> salesmanNames = FXCollections.observableArrayList();
        String sqlQuery = "SELECT salesman_name FROM salesman WHERE isActive = 1";

        try (Connection connection = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                salesmanNames.add(resultSet.getString("salesman_name"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            // Handle any SQL exceptions here
        }

        return salesmanNames;
    }

    public Salesman getSalesmanDetails(int salesmanId) {
        Salesman salesman = null;
        String query = "SELECT * FROM salesman WHERE id = ?";

        try (Connection connection = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, salesmanId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                salesman = new Salesman();
                salesman.setEmployeeId(resultSet.getInt("employee_id"));
                salesman.setSalesmanCode(resultSet.getString("salesman_code"));
                salesman.setSalesmanName(resultSet.getString("salesman_name"));
                salesman.setTruckPlate(resultSet.getString("truck_plate"));
                salesman.setDivisionId(resultSet.getInt("division_id"));
                salesman.setBranchCode(resultSet.getInt("branch_code"));
                salesman.setOperation(resultSet.getInt("operation"));
                salesman.setCompanyCode(resultSet.getInt("company_code"));
                salesman.setSupplierCode(resultSet.getInt("supplier_code"));
                salesman.setPriceType(resultSet.getString("price_type"));
                salesman.setActive(resultSet.getBoolean("isActive"));
                salesman.setInventory(resultSet.getBoolean("isInventory"));
                salesman.setCanCollect(resultSet.getBoolean("canCollect"));
                salesman.setInventoryDay(resultSet.getInt("inventory_day"));
                salesman.setEncoderId(resultSet.getInt("encoder_id"));
            }

        } catch (SQLException e) {
            e.printStackTrace(); // Handle or log the exception
        }

        return salesman;
    }

    public String getSalesmanNameById(int salesmanId) {
        String salesmanName = null;
        String query = "SELECT salesman_name FROM salesman WHERE id = ?";

        try (Connection connection = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, salesmanId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                salesmanName = resultSet.getString("salesman_name");
            }

        } catch (SQLException e) {
            e.printStackTrace(); // Handle or log the exception
        }

        return salesmanName;
    }

    public int getSalesmanIdByStoreName(String storeName) {
        int salesmanId = 0;
        String query = "SELECT id FROM salesman WHERE salesman_name = ?";

        try (Connection connection = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, storeName);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                salesmanId = resultSet.getInt("id");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return salesmanId;
    }

    public List<Salesman> getAllSalesmen() {
        List<Salesman> salesmen = new ArrayList<>();
        String sqlQuery = "SELECT * FROM salesman";

        try (Connection connection = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                Salesman salesman = new Salesman();
                salesman.setEmployeeId(resultSet.getInt("employee_id"));
                salesman.setSalesmanCode(resultSet.getString("salesman_code"));
                salesman.setSalesmanName(resultSet.getString("salesman_name"));
                salesman.setTruckPlate(resultSet.getString("truck_plate"));
                salesman.setDivisionId(resultSet.getInt("division_id"));
                salesman.setBranchCode(resultSet.getInt("branch_code"));
                salesman.setOperation(resultSet.getInt("operation"));
                salesman.setCompanyCode(resultSet.getInt("company_code"));
                salesman.setSupplierCode(resultSet.getInt("supplier_code"));
                salesman.setPriceType(resultSet.getString("price_type"));
                salesman.setActive(resultSet.getBoolean("isActive"));
                salesman.setInventory(resultSet.getBoolean("isInventory"));
                salesman.setCanCollect(resultSet.getBoolean("canCollect"));
                salesman.setInventoryDay(resultSet.getInt("inventory_day"));
                salesman.setEncoderId(resultSet.getInt("encoder_id"));
                // Add the salesman to the list
                salesmen.add(salesman);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return salesmen;
    }
}
