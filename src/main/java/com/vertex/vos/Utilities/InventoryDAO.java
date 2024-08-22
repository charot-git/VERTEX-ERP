package com.vertex.vos.Utilities;

import com.vertex.vos.Objects.Inventory;
import com.vertex.vos.Objects.ProductBreakdown;
import com.vertex.vos.Objects.ProductsInTransact;
import com.vertex.vos.Objects.SalesOrder;
import com.zaxxer.hikari.HikariDataSource;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
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


    public ObservableList<Inventory> getInventoryItemsByBranch(int branchId) {
        ObservableList<Inventory> inventoryItems = FXCollections.observableArrayList();

        // Updated query to include joins for all related tables
        String query = "SELECT i.branch_id, i.product_id, i.quantity, i.last_restock_date, " +
                "p.product_brand AS brand_id, b.brand_name AS brand_name, " +
                "p.product_category AS category_id, c.category_name AS category_name, " +
                "p.product_class AS class_id, cl.class_name AS class_name, " +
                "p.product_segment AS segment_id, s.segment_name AS segment_name, " +
                "p.product_section AS section_id, sec.section_name AS section_name, " +
                "p.description, p.cost_per_unit " +
                "FROM inventory i " +
                "JOIN products p ON i.product_id = p.product_id " +
                "LEFT JOIN brand b ON p.product_brand = b.brand_id " +
                "LEFT JOIN categories c ON p.product_category = c.category_id " +
                "LEFT JOIN classes cl ON p.product_class = cl.class_id " +
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

                    inventoryItems.add(item);
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
        String query = "SELECT i.branch_id, i.product_id, i.quantity, i.last_restock_date, " +
                "p.product_brand AS brand_id, b.brand_name AS brand_name, " +
                "p.product_category AS category_id, c.category_name AS category_name, " +
                "p.product_class AS class_id, cl.class_name AS class_name, " +
                "p.product_segment AS segment_id, s.segment_name AS segment_name, " +
                "p.product_section AS section_id, sec.section_name AS section_name, " +
                "p.description, p.cost_per_unit " +
                "FROM inventory i " +
                "JOIN products p ON i.product_id = p.product_id " +
                "LEFT JOIN brand b ON p.product_brand = b.brand_id " +
                "LEFT JOIN categories c ON p.product_category = c.category_id " +
                "LEFT JOIN classes cl ON p.product_class = cl.class_id " +
                "LEFT JOIN segment s ON p.product_segment = s.segment_id " +
                "LEFT JOIN sections sec ON p.product_section = sec.section_id";

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

                inventoryItems.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle SQL exceptions
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


    /**
     * Updates the inventory for a specific product in a specific branch by converting it to a new quantity.
     * If the inventory does not exist, it will be inserted.
     *
     * @param productIdToConvert the ID of the product to convert
     * @param branchId the ID of the branch where the inventory is located
     * @param newQuantityToConvert the new quantity to convert the product to
     * @return true if the inventory was successfully updated or inserted, false otherwise
     */
    public boolean updateInventory(int productIdToConvert, int branchId, int newQuantityToConvert) {
        try (Connection connection = dataSource.getConnection()) {
            String updateQuery = "UPDATE inventory SET quantity = ? WHERE product_id = ? AND branch_id = ?";
            String insertQuery = "INSERT INTO inventory (product_id, branch_id, quantity) VALUES (?, ?, ?)";

            try (PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {
                updateStatement.setInt(1, newQuantityToConvert);
                updateStatement.setInt(2, productIdToConvert);
                updateStatement.setInt(3, branchId);

                int rowsAffected = updateStatement.executeUpdate();
                if (rowsAffected == 0) {
                    try (PreparedStatement insertStatement = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {
                        insertStatement.setInt(1, productIdToConvert);
                        insertStatement.setInt(2, branchId);
                        insertStatement.setInt(3, newQuantityToConvert);
                        insertStatement.executeUpdate();
                        try (ResultSet generatedKeys = insertStatement.getGeneratedKeys()) {
                            if (generatedKeys.next()) {
                                int insertedId = generatedKeys.getInt(1);
                                // Use the inserted ID if needed
                            }
                        }
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            // Handle any SQL errors
            e.printStackTrace();
            return false;
        }
    }}
