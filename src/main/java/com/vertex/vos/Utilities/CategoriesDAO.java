package com.vertex.vos.Utilities;

import com.vertex.vos.Constructors.Category;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CategoriesDAO {

    public ObservableList<Category> getCategoryDetails() {
        ObservableList<Category> categoryList = FXCollections.observableArrayList();
        String sqlQuery = "SELECT category_id, category_name FROM categories";

        try (Connection connection = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                int category_id = resultSet.getInt("category_id");
                String category_name = resultSet.getString("category_name");

                Category category = new Category(category_id, category_name);
                categoryList.add(category);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            // Handle any SQL exceptions here
        }

        return categoryList;
    }

    public ObservableList<String> getCategoryNames() {
        ObservableList<String> categoryNames = FXCollections.observableArrayList();
        String sqlQuery = "SELECT category_name FROM categories";

        try (Connection connection = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                String categoryName = resultSet.getString("category_name");
                categoryNames.add(categoryName);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            // Handle any SQL exceptions here
        }

        return categoryNames;
    }

    public boolean createCategory(String categoryName) {
        String insertQuery = "INSERT INTO categories (category_name) VALUES (?)";

        try (Connection connection = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {

            preparedStatement.setString(1, categoryName);
            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Category created successfully: " + categoryName);
                return true;
            } else {
                System.out.println("Failed to create category: " + categoryName);
                return false;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            // Handle any SQL exceptions here
            // Optionally, show an error message dialog to the user
            DialogUtils.showErrorMessage("Database Error", "An error occurred while creating the category: " + categoryName);
            return false;
        }
    }


    public int getCategoryIdByName(String categoryName) {
        String sqlQuery = "SELECT category_id FROM categories WHERE category_name = ?";
        int categoryId = -1; // Set a default value indicating not found

        try (Connection connection = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {

            preparedStatement.setString(1, categoryName);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    categoryId = resultSet.getInt("category_id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle any SQL exceptions here
        }

        return categoryId;
    }

    public String getCategoryNameById(int categoryId) {
        String sqlQuery = "SELECT category_name FROM categories WHERE category_id = ?";
        String categoryName = null; // Default value if not found

        try (Connection connection = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {

            preparedStatement.setInt(1, categoryId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    categoryName = resultSet.getString("category_name");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle any SQL exceptions here
        }
        return categoryName;
    }


}
