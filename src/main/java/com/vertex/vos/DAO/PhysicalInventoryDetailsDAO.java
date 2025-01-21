package com.vertex.vos.DAO;

import com.vertex.vos.Objects.*;
import com.vertex.vos.Utilities.DatabaseConnectionPool;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PhysicalInventoryDetailsDAO {
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();


    public PhysicalInventoryDetails getInventory(int productId, Branch branch, Category category) {
        String sql = getString(category);  // Use category directly here

        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            // Set productId and branchId
            stmt.setInt(1, productId);
            stmt.setInt(2, branch.getId());  // Assuming branch has an getId() method

            // If category is not "All" and not the default "160", set the category ID
            if (category != null && category.getCategoryId() != 160 && !"All".equals(category.getCategoryName())) {
                stmt.setInt(3, category.getCategoryId());  // Assuming category has getCategoryId() method
            }

            try (var resultSet = stmt.executeQuery()) {
                if (resultSet.next()) {
                    // Create a new PhysicalInventoryDetails object
                    PhysicalInventoryDetails inventoryDetails = new PhysicalInventoryDetails();

                    // Set the systemCount to be the quantity from inventory
                    inventoryDetails.setSystemCount(resultSet.getInt("quantity"));

                    // Set other fields as needed
                    inventoryDetails.setId(resultSet.getInt("id"));

                    // Fetch or create PhysicalInventory object based on your design
                    PhysicalInventory physicalInventory = new PhysicalInventory();
                    inventoryDetails.setPhysicalInventory(physicalInventory);

                    // Fetch or create Product object based on the result set
                    Product product = new Product();  // Assuming you'll fetch the actual product
                    inventoryDetails.setProduct(product);

                    return inventoryDetails;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();  // Log the exception
            throw new RuntimeException("Error occurred while fetching inventory details.", e);  // Re-throw as a RuntimeException or log appropriately
        }

        return null;  // Return null if no matching inventory found or in case of an exception
    }

    private static String getString(Category category) {
        String sql;

        // If category is "All" (i.e., categoryId is 160), fetch all inventories for the given product and branch
        if (category != null && (category.getCategoryId() == 160 || "All".equals(category.getCategoryName()))) {
            sql = "SELECT * FROM inventory i " +
                    "JOIN products p ON i.product_id = p.product_id " +
                    "WHERE i.product_id = ? AND i.branch_id = ? AND i.quantity != 0";
        } else {
            // If category is specific, filter by product_category in the products table
            sql = "SELECT * FROM inventory i " +
                    "JOIN products p ON i.product_id = p.product_id " +
                    "WHERE i.product_id = ? AND i.branch_id = ? AND p.product_category = ? AND i.quantity != 0";
        }
        return sql;
    }
}
