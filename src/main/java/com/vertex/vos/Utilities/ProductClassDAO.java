package com.vertex.vos.Utilities;

import com.vertex.vos.Constructors.ProductClass;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ProductClassDAO {

    public ObservableList<ProductClass> getProductClassDetails() {
        ObservableList<ProductClass> classList = FXCollections.observableArrayList();
        String sqlQuery = "SELECT class_id, class_name FROM classes";

        try (Connection connection = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                int classId = resultSet.getInt("class_id");
                String className = resultSet.getString("class_name");

                ProductClass productClass = new ProductClass(classId, className); // Use your ProductClass model
                classList.add(productClass);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            // Handle any SQL exceptions here
        }

        return classList;
    }

    public boolean createProductClass(String className) {
        String insertQuery = "INSERT INTO classes (class_name) VALUES (?)";

        try (Connection connection = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {

            preparedStatement.setString(1, className);
            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Product class created successfully: " + className);
                return true;
            } else {
                System.out.println("Failed to create product class: " + className);
                return false;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            DialogUtils.showErrorMessage("Database Error", "An error occurred while creating the product class: " + className);
            return false;
        }
    }


    public int getProductClassIdByName(String className) {
        String sqlQuery = "SELECT class_id FROM classes WHERE class_name = ?";
        int classId = -1; // Default value indicating not found

        try (Connection connection = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {

            preparedStatement.setString(1, className);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    classId = resultSet.getInt("class_id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle SQL exceptions here
        }

        return classId;
    }

    public String getProductClassNameById(int classId) {
        String sqlQuery = "SELECT class_name FROM classes WHERE class_id = ?";
        String className = null; // Default value indicating not found

        try (Connection connection = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {

            preparedStatement.setInt(1, classId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    className = resultSet.getString("class_name");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle SQL exceptions here
        }

        return className;
    }

}
