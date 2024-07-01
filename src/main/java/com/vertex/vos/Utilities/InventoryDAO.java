package com.vertex.vos.Utilities;

import com.vertex.vos.Objects.Inventory;
import com.vertex.vos.Objects.SalesOrder;
import com.zaxxer.hikari.HikariDataSource;
import com.vertex.vos.Objects.ProductsInTransact;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.*;

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
                        java.sql.Timestamp lastRestockDate = resultSet.getTimestamp("last_restock_date");

                        Inventory item = new Inventory();
                        item.setBranchId(branchId);
                        item.setBranchName(branchDAO.getBranchNameById(branchId));
                        item.setProductId(productId);
                        item.setQuantity(quantity);
                        item.setProductDescription(productDAO.getProductDescriptionById(productId));
                        item.setLastRestockDate(lastRestockDate.toLocalDateTime());

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
                        java.sql.Timestamp lastRestockDate = resultSet.getTimestamp("last_restock_date");
                        String productDescription = productDAO.getProductDescriptionById(productId);

                        // Update product data in the map
                        if (productDataMap.containsKey(productDescription)) {
                            Inventory data = productDataMap.get(productDescription);
                            data.addQuantity(quantity);
                            if (lastRestockDate != null) {
                                data.setLastRestockDate(lastRestockDate.toLocalDateTime());
                            }
                        } else {
                            Inventory data = new Inventory(quantity, lastRestockDate != null ? lastRestockDate.toLocalDateTime() : null);
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


    public void addOrUpdateReservedQuantityBulk(List<SalesOrder> orders) {
        try (Connection connection = dataSource.getConnection()) {
            boolean autoCommitMode = connection.getAutoCommit();
            if (autoCommitMode) {
                connection.setAutoCommit(false); // Start transaction if not already in a transaction
            }

            String insertQuery = "INSERT INTO inventory (branch_id, product_id, reserved_quantity) VALUES (?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE reserved_quantity = reserved_quantity + VALUES(reserved_quantity)";

            try (PreparedStatement insertStatement = connection.prepareStatement(insertQuery)) {
                for (SalesOrder order : orders) {
                    int branchId = order.getSourceBranchId();
                    int productId = order.getProductID();
                    int reservedQuantity = order.getQty();

                    // Add parameters for insert statement
                    insertStatement.setInt(1, branchId);
                    insertStatement.setInt(2, productId);
                    insertStatement.setInt(3, reservedQuantity);
                    insertStatement.addBatch();
                }

                // Execute batch updates
                int[] insertCounts = insertStatement.executeBatch();

                // Check if all inserts were successful
                boolean allInserted = Arrays.stream(insertCounts).allMatch(count -> count > 0);

                if (allInserted) {
                    connection.commit(); // Commit transaction
                    System.out.println("Reserved quantities updated successfully.");
                } else {
                    connection.rollback(); // Rollback transaction
                    System.out.println("Failed to update reserved quantities.");
                }
            } catch (SQLException e) {
                connection.rollback(); // Rollback transaction
                System.err.println("Error occurred while updating reserved quantities: " + e.getMessage());
            } finally {
                // Restore original auto-commit mode and close resources
                if (autoCommitMode) {
                    connection.setAutoCommit(true); // Restore original auto-commit mode
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to establish database connection: " + e.getMessage());
        }
    }


    private boolean checkUpdateResults(int[] counts) {
        for (int count : counts) {
            if (count < 0) {
                return false;
            }
        }
        return true;
    }
}
