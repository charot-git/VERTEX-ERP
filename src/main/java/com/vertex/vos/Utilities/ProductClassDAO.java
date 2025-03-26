package com.vertex.vos.Utilities;

import com.vertex.vos.Objects.ProductClass;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductClassDAO {
    private static List<ProductClass> productClassCache = null;
    private static List<String> productClassNamesCache = null;

    private void loadCache() {
        String sqlQuery = "SELECT class_id, class_name FROM classes";
        List<ProductClass> classList = new ArrayList<>();
        List<String> classNames = new ArrayList<>();

        try (Connection connection = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                int classId = resultSet.getInt("class_id");
                String className = resultSet.getString("class_name");

                classList.add(new ProductClass(classId, className));
                classNames.add(className);
            }

            // Store in cache for session duration
            productClassCache = classList;
            productClassNamesCache = classNames;

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ObservableList<ProductClass> getProductClassDetails() {
        if (productClassCache == null) {
            loadCache(); // Load once per session
        }
        return FXCollections.observableArrayList(productClassCache);
    }

    public ObservableList<String> getProductClassNames() {
        if (productClassNamesCache == null) {
            loadCache(); // Load once per session
        }
        return FXCollections.observableArrayList(productClassNamesCache);
    }

    public boolean createProductClass(String className) {
        String insertQuery = "INSERT INTO classes (class_name) VALUES (?)";

        try (Connection connection = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {

            preparedStatement.setString(1, className);
            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Product class created successfully: " + className);
                loadCache(); // Refresh cache after insert
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
        if (productClassCache == null) {
            loadCache();
        }

        for (ProductClass productClass : productClassCache) {
            if (productClass.getClassName().equalsIgnoreCase(className)) {
                return productClass.getClassId();
            }
        }

        return -1; // Not found
    }

    public String getProductClassNameById(int classId) {
        if (productClassCache == null) {
            loadCache();
        }

        for (ProductClass productClass : productClassCache) {
            if (productClass.getClassId() == classId) {
                return productClass.getClassName();
            }
        }

        return null; // Not found
    }

    public List<ProductClass> getAllProductClasses() {
        if (productClassCache == null) {
            loadCache();
        }
        return productClassCache;
    }
}
