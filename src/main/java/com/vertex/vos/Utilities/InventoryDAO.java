package com.vertex.vos.Utilities;

import com.vertex.vos.Objects.*;
import com.zaxxer.hikari.HikariDataSource;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.sql.Date;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

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
                    List<CompletableFuture<Inventory>> futures = FXCollections.observableArrayList();

                    while (resultSet.next()) {
                        int productId = resultSet.getInt("product_id");
                        int quantity = resultSet.getInt("quantity");
                        Timestamp lastRestockDate = resultSet.getTimestamp("last_restock_date");

                        // Fetch branch name and product description in parallel
                        CompletableFuture<String> branchNameFuture = CompletableFuture.supplyAsync(() -> branchDAO.getBranchNameById(branchId));
                        CompletableFuture<String> productDescriptionFuture = CompletableFuture.supplyAsync(() -> productDAO.getProductDescriptionById(productId));

                        CompletableFuture<Inventory> future = branchNameFuture.thenCombine(productDescriptionFuture, (branchName, productDescription) -> {
                            Inventory item = new Inventory();
                            item.setBranchId(branchId);
                            item.setBranchName(branchName);
                            item.setProductId(productId);
                            item.setQuantity(quantity);
                            item.setProductDescription(productDescription);
                            item.setLastRestockDate(lastRestockDate.toLocalDateTime());
                            return item;
                        });

                        futures.add(future);
                    }

                    // Wait for all futures to complete and collect the results
                    CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
                    allOf.get();

                    inventoryItems.addAll(futures.stream().map(CompletableFuture::join).collect(Collectors.toList()));
                }
            }
        } catch (SQLException | InterruptedException | ExecutionException e) {
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
                DialogUtils.showErrorMessage("Failed to update reserved quantities.", e.getMessage());
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

    public CompletionStage<List<ProductBreakdown>> fetchPackageBreakdowns(int productId) {
        return CompletableFuture.supplyAsync(() -> {
            List<ProductBreakdown> breakdowns = new ArrayList<>();

            try (Connection connection = dataSource.getConnection()) {
                // Query to check if the selected product is a child and get its parent ID
                String parentQuery = "SELECT parent_id FROM products WHERE product_id = ?";
                int parentId = -1;
                boolean isChild = false;

                try (PreparedStatement parentStatement = connection.prepareStatement(parentQuery)) {
                    parentStatement.setInt(1, productId);

                    try (ResultSet parentResultSet = parentStatement.executeQuery()) {
                        if (parentResultSet.next()) {
                            parentId = parentResultSet.getInt("parent_id");
                            isChild = parentId != 0;
                        }
                    }
                }

                if (isChild) {
                    // Fetch the parent and its siblings, excluding the selected product itself
                    String breakdownQuery = "SELECT p.product_id, p.description, u.unit_id, u.unit_name, u.unit_shortcut, u.order " +
                            "FROM products p " +
                            "JOIN units u ON p.unit_of_measurement = u.unit_id " +
                            "WHERE (p.product_id = ? " + // Include the parent
                            "OR p.parent_id = ?) " + // Include siblings
                            "AND p.product_id != ? " + // Exclude selected product
                            "ORDER BY u.order";

                    try (PreparedStatement breakdownStatement = connection.prepareStatement(breakdownQuery)) {
                        breakdownStatement.setInt(1, parentId); // Parent
                        breakdownStatement.setInt(2, parentId); // Siblings
                        breakdownStatement.setInt(3, productId); // Exclude selected

                        try (ResultSet breakdownResultSet = breakdownStatement.executeQuery()) {
                            while (breakdownResultSet.next()) {
                                int unitId = breakdownResultSet.getInt("unit_id");
                                String unitName = breakdownResultSet.getString("unit_name");
                                String unitShortcut = breakdownResultSet.getString("unit_shortcut");
                                int order = breakdownResultSet.getInt("order");
                                String description = breakdownResultSet.getString("description");

                                ProductBreakdown breakdown = new ProductBreakdown(productId, unitId, unitName, unitShortcut, order, description);
                                breakdowns.add(breakdown);
                            }
                        }
                    }
                } else {
                    // Fetch breakdowns for all children of the selected product, excluding the parent
                    String childQuery = "SELECT product_id FROM products WHERE parent_id = ?";
                    List<Integer> childProductIds = new ArrayList<>();

                    try (PreparedStatement childStatement = connection.prepareStatement(childQuery)) {
                        childStatement.setInt(1, productId);

                        try (ResultSet childResultSet = childStatement.executeQuery()) {
                            while (childResultSet.next()) {
                                childProductIds.add(childResultSet.getInt("product_id"));
                            }
                        }
                    }

                    if (!childProductIds.isEmpty()) {
                        // Convert list of product IDs to a comma-separated string
                        String productIds = childProductIds.stream()
                                .map(String::valueOf)
                                .collect(Collectors.joining(","));

                        // Query to get breakdowns for the list of product IDs, excluding the parent
                        String breakdownQuery = "SELECT p.product_id, p.description, u.unit_id, u.unit_name, u.unit_shortcut, u.order " +
                                "FROM products p " +
                                "JOIN units u ON p.unit_of_measurement = u.unit_id " +
                                "WHERE p.product_id IN (" + productIds + ") " + // Children only
                                "AND p.product_id != ? " + // Exclude parent if needed
                                "ORDER BY u.order";

                        try (PreparedStatement breakdownStatement = connection.prepareStatement(breakdownQuery)) {
                            breakdownStatement.setInt(1, productId); // Exclude parent

                            try (ResultSet breakdownResultSet = breakdownStatement.executeQuery()) {
                                while (breakdownResultSet.next()) {
                                    int unitId = breakdownResultSet.getInt("unit_id");
                                    String unitName = breakdownResultSet.getString("unit_name");
                                    String unitShortcut = breakdownResultSet.getString("unit_shortcut");
                                    int order = breakdownResultSet.getInt("order");
                                    String description = breakdownResultSet.getString("description");

                                    ProductBreakdown breakdown = new ProductBreakdown(productId, unitId, unitName, unitShortcut, order, description);
                                    breakdowns.add(breakdown);
                                }
                            }
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return breakdowns;
        });
    }


}
