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
                Supplier supplier = new Supplier(resultSet.getInt("id"), resultSet.getString("supplier_name"), resultSet.getString("contact_person"), resultSet.getString("email_address"), resultSet.getString("phone_number"), resultSet.getString("address"), resultSet.getString("city"), resultSet.getString("brgy"), resultSet.getString("state_province"), resultSet.getString("postal_code"), resultSet.getString("country"), resultSet.getInt("discount_type"), resultSet.getString("supplier_type"), resultSet.getString("tin_number"), resultSet.getString("bank_details"), resultSet.getString("products_or_services"), resultSet.getString("payment_terms"), resultSet.getString("delivery_terms"), resultSet.getString("agreement_or_contract"), resultSet.getString("preferred_communication_method"), resultSet.getString("notes_or_comments"), resultSet.getDate("date_added"), resultSet.getString("supplier_image"));
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
