package com.vertex.vos.Utilities;

import com.vertex.vos.Objects.Category;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class CategoriesDAO {
    private static List<Category> categoryCache = null;
    private static List<String> categoryNamesCache = null;

    private void loadCache() {
        String sqlQuery = "SELECT category_id, category_name FROM categories";
        List<Category> categories = new ArrayList<>();
        List<String> categoryNames = new ArrayList<>();

        try (Connection connection = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                int categoryId = resultSet.getInt("category_id");
                String categoryName = resultSet.getString("category_name");

                categories.add(new Category(categoryId, categoryName));
                categoryNames.add(categoryName);
            }

            // Store in cache for session duration
            categoryCache = categories;
            categoryNamesCache = categoryNames;

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ObservableList<Category> getCategoryDetails() {
        if (categoryCache == null) {
            loadCache(); // Load once per session
        }
        return FXCollections.observableArrayList(categoryCache);
    }

    public ObservableList<String> getCategoryNames() {
        if (categoryNamesCache == null) {
            loadCache(); // Load once per session
        }
        return FXCollections.observableArrayList(categoryNamesCache);
    }

    public boolean createCategory(String categoryName) {
        String insertQuery = "INSERT INTO categories (category_name) VALUES (?)";

        try (Connection connection = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {

            preparedStatement.setString(1, categoryName);
            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Category created successfully: " + categoryName);
                loadCache(); // Refresh cache after insert
                return true;
            } else {
                System.out.println("Failed to create category: " + categoryName);
                return false;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            DialogUtils.showErrorMessage("Database Error", "An error occurred while creating the category: " + categoryName);
            return false;
        }
    }

    public int getCategoryIdByName(String categoryName) {
        if (categoryCache == null) {
            loadCache();
        }

        for (Category category : categoryCache) {
            if (category.getCategoryName().equalsIgnoreCase(categoryName)) {
                return category.getCategoryId();
            }
        }

        return -1; // Not found
    }

    public String getCategoryNameById(int categoryId) {
        if (categoryCache == null) {
            loadCache();
        }

        for (Category category : categoryCache) {
            if (category.getCategoryId() == categoryId) {
                return category.getCategoryName();
            }
        }

        return null; // Not found
    }

    public List<Category> getAllCategories() {
        if (categoryCache == null) {
            loadCache();
        }
        return categoryCache;
    }
}
