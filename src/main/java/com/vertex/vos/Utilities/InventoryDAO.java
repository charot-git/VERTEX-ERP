package com.vertex.vos.Utilities;

import com.vertex.vos.Objects.*;
import com.zaxxer.hikari.HikariDataSource;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
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
    BrandDAO brandDAO = new BrandDAO();
    CategoriesDAO categoriesDAO = new CategoriesDAO();
    ProductClassDAO classDAO = new ProductClassDAO();
    SegmentDAO segmentDAO = new SegmentDAO();
    SectionsDAO sectionsDAO = new SectionsDAO();

    //get items with inventoryByBranch as Product object


    public ObservableList<Inventory> getInventoryItemsByBranch(int branchId) {
        ObservableList<Inventory> inventoryItems = FXCollections.observableArrayList();

        // Updated query to include joins for all related tables
        String query = "SELECT i.branch_id, i.product_id, i.quantity, i.last_restock_date, " +
                "p.product_brand AS brand_id, b.brand_name AS brand_name, " +
                "p.product_category AS category_id, c.category_name AS category_name, " +
                "p.product_class AS class_id, cl.class_name AS class_name, " +
                "p.product_segment AS segment_id, s.segment_name AS segment_name, " +
                "p.product_section AS section_id, sec.section_name AS section_name, " +
                "p.unit_of_measurement AS unit_id, u.unit_name AS unit_name, " +
                "p.description, p.cost_per_unit " +
                "FROM inventory i " +
                "JOIN products p ON i.product_id = p.product_id " +
                "LEFT JOIN brand b ON p.product_brand = b.brand_id " +
                "LEFT JOIN categories c ON p.product_category = c.category_id " +
                "LEFT JOIN classes cl ON p.product_class = cl.class_id " +
                "LEFT JOIN units u ON p.unit_of_measurement = u.unit_id " +
                "LEFT JOIN segment s ON p.product_segment = s.segment_id " +
                "LEFT JOIN sections sec ON p.product_section = sec.section_id " +
                "WHERE i.branch_id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, branchId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    int productId = resultSet.getInt("product_id");
                    int quantity = resultSet.getInt("quantity");
                    Timestamp lastRestockDate = resultSet.getTimestamp("last_restock_date");
                    String brandName = resultSet.getString("brand_name");
                    String categoryName = resultSet.getString("category_name");
                    String productClassName = resultSet.getString("class_name");
                    String productSegmentName = resultSet.getString("segment_name");
                    String productSectionName = resultSet.getString("section_name");
                    String productDescription = resultSet.getString("description");
                    String unitName = resultSet.getString("unit_name");
                    double unitPrice = resultSet.getDouble("cost_per_unit");

                    Inventory item = new Inventory();
                    item.setBranchId(branchId);
                    item.setProductId(productId);
                    item.setProductDescription(productDescription);
                    item.setQuantity(quantity);
                    item.setLastRestockDate(lastRestockDate.toLocalDateTime());
                    item.setBrand(brandName);
                    item.setCategory(categoryName);
                    item.setProductClass(productClassName);
                    item.setProductSegment(productSegmentName);
                    item.setProductSection(productSectionName);
                    item.setUnitPrice(unitPrice);
                    item.setUnit(unitName);

                    if (item.getQuantity() > 0) {
                        inventoryItems.add(item);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle SQL exceptions
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

        // Updated query to include joins for all related tables
        String query = """
                             SELECT 
                                 i.branch_id, 
                                 i.product_id, 
                                 i.quantity, 
                                 i.last_restock_date, 
                                 p.product_brand AS brand_id, 
                                 b.brand_name AS brand_name, 
                                 p.product_category AS category_id, 
                                 c.category_name AS category_name, 
                                 p.product_class AS class_id, 
                                 cl.class_name AS class_name, 
                                 p.product_segment AS segment_id, 
                                 s.segment_name AS segment_name, 
                                 p.product_section AS section_id,
                                p.unit_of_measurement AS unit_id,
                u.unit_name AS unit_name,
                                 sec.section_name AS section_name, 
                                 p.description, 
                                 p.cost_per_unit
                                
                             FROM 
                                 inventory i 
                             JOIN 
                                 products p ON i.product_id = p.product_id 
                             LEFT JOIN 
                                 brand b ON p.product_brand = b.brand_id 
                             LEFT JOIN 
                                 categories c ON p.product_category = c.category_id 
                             LEFT JOIN 
                                 classes cl ON p.product_class = cl.class_id 
                             LEFT JOIN 
                                 segment s ON p.product_segment = s.segment_id 
                             LEFT JOIN 
                                 sections sec ON p.product_section = sec.section_id 
                             LEFT JOIN 
                                 units u ON p.unit_of_measurement = u.unit_id
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                int branchId = resultSet.getInt("branch_id");
                int productId = resultSet.getInt("product_id");
                int quantity = resultSet.getInt("quantity");
                Timestamp lastRestockDate = resultSet.getTimestamp("last_restock_date");
                String brandName = resultSet.getString("brand_name");
                String categoryName = resultSet.getString("category_name");
                String productClassName = resultSet.getString("class_name");
                String productSegmentName = resultSet.getString("segment_name");
                String productSectionName = resultSet.getString("section_name");
                String productDescription = resultSet.getString("description");
                String productUnit = resultSet.getString("unit_name");
                double unitPrice = resultSet.getDouble("cost_per_unit");

                Inventory item = new Inventory();
                item.setBranchId(branchId);
                item.setProductId(productId);
                item.setProductDescription(productDescription);
                item.setQuantity(quantity);
                item.setLastRestockDate(lastRestockDate != null ? lastRestockDate.toLocalDateTime() : null);
                item.setBrand(brandName);
                item.setCategory(categoryName);
                item.setProductClass(productClassName);
                item.setProductSegment(productSegmentName);
                item.setProductSection(productSectionName);
                item.setUnitPrice(unitPrice);
                item.setUnit(productUnit);

                if (item.getQuantity() > 0) {
                    inventoryItems.add(item);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle SQL exceptions
        }

        return inventoryItems;
    }


    public int getQuantityByBranchAndProductID(int branchId, int productId) {
        int quantity = 0;
        String query = "SELECT quantity - reserved_quantity AS available_quantity FROM inventory WHERE branch_id = ? AND product_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, branchId);
            statement.setInt(2, productId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    quantity = resultSet.getInt("available_quantity");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return quantity;
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
                                int breakdownProductId = breakdownResultSet.getInt("product_id");

                                ProductBreakdown breakdown = new ProductBreakdown(breakdownProductId, unitId, unitName, unitShortcut, order, description);
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
                                    int breakdownProductId = breakdownResultSet.getInt("product_id");

                                    ProductBreakdown breakdown = new ProductBreakdown(breakdownProductId, unitId, unitName, unitShortcut, order, description);
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

    public boolean updateInventoryBulk(List<Inventory> inventoryUpdates, Connection connection) throws SQLException {
        String query = "INSERT INTO inventory (product_id, branch_id, quantity, last_restock_date) " +
                "VALUES (?, ?, ?, NOW()) " +
                "ON DUPLICATE KEY UPDATE quantity = quantity + VALUES(quantity), last_updated = NOW()";

        final int batchSize = 1000; // Configurable batch size
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            int batchCount = 0;

            for (Inventory update : inventoryUpdates) {
                statement.setInt(1, update.getProductId());
                statement.setInt(2, update.getBranchId());
                statement.setInt(3, update.getQuantity()); // Can be positive or negative
                System.out.println(update.getProductId() + " " + update.getBranchId() + " " + update.getQuantity());

                statement.addBatch();

                if (++batchCount % batchSize == 0) {
                    statement.executeBatch();
                }
            }

            // Execute any remaining batches
            statement.executeBatch();
            return true;

        } catch (SQLException e) {
            // Log and rethrow the exception for the caller to handle
            System.err.println("Error updating inventory in bulk: " + e.getMessage());
            throw e;
        }
    }


    public boolean updateInventory(int productIdToConvert, int branchId, int quantity) {
        String query = "INSERT INTO inventory (product_id, branch_id, quantity, last_restock_date, last_updated) " +
                "VALUES (?, ?, ?, NOW(), NOW()) " +
                "ON DUPLICATE KEY UPDATE quantity = quantity + VALUES(quantity), last_updated = NOW()";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, productIdToConvert);
            statement.setInt(2, branchId);
            statement.setInt(3, quantity);  // This can be positive or negative depending on the operation

            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0; // Return true if the insert or update was successful

        } catch (SQLException e) {
            // Handle SQL exceptions
            e.printStackTrace();
            return false;
        }
    }
}
