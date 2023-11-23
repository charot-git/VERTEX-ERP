package com.vertex.vos.Utilities;

import com.vertex.vos.Constructors.Supplier;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SupplierDAO {

    public ObservableList<Supplier> getAllSuppliers() {
        ObservableList<Supplier> suppliersList = FXCollections.observableArrayList();
        String sqlQuery = "SELECT * FROM suppliers";

        try (Connection connection = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                Supplier supplier = new Supplier();
                supplier.setId(resultSet.getInt("id"));
                supplier.setSupplierName(resultSet.getString("supplier_name"));
                supplier.setContactPerson(resultSet.getString("contact_person"));

                suppliersList.add(supplier);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            // Handle any SQL exceptions here
        }

        return suppliersList;
    }

    public int getSupplierIdByName(String supplierName) {
        String sqlQuery = "SELECT id FROM suppliers WHERE supplier_name = ?";
        int supplierId = -1; // Set a default value indicating not found

        try (Connection connection = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {

            preparedStatement.setString(1, supplierName);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    supplierId = resultSet.getInt("id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle any SQL exceptions here
        }

        return supplierId;
    }
    public String getSupplierNameById(int supplierId) {
        String sqlQuery = "SELECT supplier_name FROM suppliers WHERE id = ?";
        String supplierName = null; // Default value if not found

        try (Connection connection = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {

            preparedStatement.setInt(1, supplierId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    supplierName = resultSet.getString("supplier_name");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle any SQL exceptions here
        }

        return supplierName;
    }

}
