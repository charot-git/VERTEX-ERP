package com.vertex.vos.Utilities;

import com.vertex.vos.Constructors.Salesman;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
}
