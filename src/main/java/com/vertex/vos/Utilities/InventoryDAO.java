package com.vertex.vos.Utilities;

import com.vertex.vos.Constructors.Inventory;
import com.zaxxer.hikari.HikariDataSource;
import com.vertex.vos.Constructors.ProductsInTransact;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class InventoryDAO {
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    public boolean addOrUpdateInventory(ProductsInTransact product) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false); // Start transaction
            String query = "UPDATE inventory SET quantity = quantity + ?, last_restock_date = ? " +
                    "WHERE branch_id = ? AND product_id = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, product.getReceivedQuantity());
                statement.setDate(2, new java.sql.Date(System.currentTimeMillis()));
                statement.setInt(3, product.getBranchId());
                statement.setInt(4, product.getProductId());
                int rowsUpdated = statement.executeUpdate();

                // If rows were updated, it means the entry exists and was successfully updated
                if (rowsUpdated > 0) {
                    connection.commit(); // Commit transaction
                    return true;
                } else {
                    // If no rows were updated, it means the entry doesn't exist
                    // Insert a new entry
                    String insertQuery = "INSERT INTO inventory (branch_id, product_id, quantity, last_restock_date) VALUES (?, ?, ?, ?)";
                    try (PreparedStatement insertStatement = connection.prepareStatement(insertQuery)) {
                        insertStatement.setInt(1, product.getBranchId());
                        insertStatement.setInt(2, product.getProductId());
                        insertStatement.setInt(3, product.getReceivedQuantity());
                        insertStatement.setTimestamp(4, new java.sql.Timestamp(System.currentTimeMillis()));
                        insertStatement.executeUpdate();
                        connection.commit(); // Commit transaction
                        return true;
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                connection.rollback(); // Rollback transaction
            } finally {
                connection.setAutoCommit(true); // Reset auto-commit mode
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // Return false if an exception occurs or the update/insert fails
    }


    BranchDAO branchDAO = new BranchDAO();
    ProductDAO productDAO = new ProductDAO();


    public ObservableList<Inventory> getInventoryItemsByBranch(int branchId) {
        ObservableList<Inventory> inventoryItems = FXCollections.observableArrayList();
        try (Connection connection = dataSource.getConnection()) {
            String query = "SELECT branch_id, product_id, quantity, last_restock_date FROM inventory WHERE branch_id = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, branchId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        int productId = resultSet.getInt("product_id");
                        int quantity = resultSet.getInt("quantity");
                        java.sql.Date lastRestockDate = resultSet.getDate("last_restock_date");

                        // Create Inventory object and add it to the observable list
                        Inventory item = new Inventory();
                        item.setBranchId(branchId);
                        item.setBranchName(branchDAO.getBranchNameById(branchId));
                        item.setProductId(productId);
                        item.setQuantity(quantity);
                        item.setProductDescription(productDAO.getProductDescriptionById(productId));
                        // Assuming you convert java.sql.Date to LocalDateTime
                        item.setLastRestockDate(lastRestockDate.toLocalDate().atStartOfDay());

                        inventoryItems.add(item);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return inventoryItems;
    }

    public ObservableList<String> getBranchNamesWithInventory() {
        ObservableList<String> branchNames = FXCollections.observableArrayList();
        String query = "SELECT DISTINCT b.branch_name FROM branches b " +
                "INNER JOIN inventory i ON b.id = i.branch_id";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                branchNames.add(resultSet.getString("branch_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return branchNames;
    }


    public ObservableList<Inventory> getAllInventoryItems() {
        ObservableList<Inventory> inventoryItems = FXCollections.observableArrayList();
        Map<String, Inventory> productDataMap = new HashMap<>(); // Map to store product data by description

        try (Connection connection = dataSource.getConnection()) {
            String query = "SELECT branch_id, product_id, quantity, last_restock_date FROM inventory";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        int productId = resultSet.getInt("product_id");
                        int quantity = resultSet.getInt("quantity");
                        LocalDateTime lastRestockDate = resultSet.getTimestamp("last_restock_date").toLocalDateTime();
                        String productDescription = productDAO.getProductDescriptionById(productId);

                        // Update product data in the map
                        if (productDataMap.containsKey(productDescription)) {
                            Inventory data = productDataMap.get(productDescription);
                            data.addQuantity(quantity);
                            if (lastRestockDate != null) {
                                String formattedDateTime = lastRestockDate.format(formatter);
                                data.setLastRestockDate(LocalDateTime.parse(formattedDateTime, formatter));
                            }
                        } else {
                            Inventory data = new Inventory(quantity, lastRestockDate != null ? lastRestockDate.toLocalDate().atStartOfDay() : null);
                            productDataMap.put(productDescription, data);
                        }
                    }
                }
            }

            // Create Inventory objects from the map
            for (Map.Entry<String, Inventory> entry : productDataMap.entrySet()) {
                String productDescription = entry.getKey();
                Inventory data = entry.getValue();

                // Create Inventory object and add it to the observable list
                Inventory item = new Inventory();
                item.setProductDescription(productDescription);
                item.setQuantity(data.getQuantity());

                item.setLastRestockDate(data.getLastRestockDate());
                inventoryItems.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return inventoryItems;
    }

    public int getQuantityByBranchAndProductID(int branchId, int productId) {
        int quantity = 0;
        String query = "SELECT quantity FROM inventory WHERE branch_id = ? AND product_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, branchId);
            statement.setInt(2, productId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    quantity = resultSet.getInt("quantity");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return quantity;
    }

}
