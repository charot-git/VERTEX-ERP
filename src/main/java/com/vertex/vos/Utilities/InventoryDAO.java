package com.vertex.vos.Utilities;

import com.zaxxer.hikari.HikariDataSource;
import com.vertex.vos.Constructors.ProductsInTransact;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class InventoryDAO {
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    public void addOrUpdateInventory(ProductsInTransact product) {
        try (Connection connection = dataSource.getConnection()) {
            String query = "UPDATE inventory SET quantity = quantity + ?, last_restock_date = ? " +
                    "WHERE branch_id = ? AND product_id = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, product.getReceivedQuantity());
                statement.setDate(2, new java.sql.Date(System.currentTimeMillis()));
                statement.setInt(3, product.getBranchId());
                statement.setInt(4, product.getProductId());
                int rowsUpdated = statement.executeUpdate();

                // If no rows were updated, it means the entry doesn't exist
                if (rowsUpdated == 0) {
                    // Insert a new entry
                    String insertQuery = "INSERT INTO inventory (branch_id, product_id, quantity, last_restock_date) VALUES (?, ?, ?, ?)";
                    try (PreparedStatement insertStatement = connection.prepareStatement(insertQuery)) {
                        insertStatement.setInt(1, product.getBranchId());
                        insertStatement.setInt(2, product.getProductId());
                        insertStatement.setInt(3, product.getReceivedQuantity());
                        insertStatement.setTimestamp(4, new java.sql.Timestamp(System.currentTimeMillis()));
                        insertStatement.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    public List<ProductsInTransact> getAllInventoryItems() {
        List<ProductsInTransact> inventoryItems = new ArrayList<>();
        try (Connection connection = dataSource.getConnection()) {
            String query = "SELECT branch_id, product_id, quantity, last_restock_date FROM inventory";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        int branchId = resultSet.getInt("branch_id");
                        int productId = resultSet.getInt("product_id");
                        int quantity = resultSet.getInt("quantity");
                        java.sql.Date lastRestockDate = resultSet.getDate("last_restock_date");

                        // Create ProductsInTransact object and add it to list
                        ProductsInTransact item = new ProductsInTransact();
                        item.setBranchId(branchId);
                        item.setProductId(productId);
                        // Set other properties accordingly

                        inventoryItems.add(item);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return inventoryItems;
    }
}
