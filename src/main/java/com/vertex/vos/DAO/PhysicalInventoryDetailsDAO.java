package com.vertex.vos.DAO;

import com.vertex.vos.Objects.*;
import com.vertex.vos.Utilities.DatabaseConnectionPool;
import com.vertex.vos.Utilities.ProductDAO;
import com.vertex.vos.Utilities.ProductsPerSupplierDAO;
import com.zaxxer.hikari.HikariDataSource;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PhysicalInventoryDetailsDAO {
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();
    private final ProductDAO productDAO = new ProductDAO();
    private final ProductsPerSupplierDAO productsPerSupplierDAO = new ProductsPerSupplierDAO();

    /**
     * Retrieves products for a supplier with optional category filtering.
     */
    public List<Integer> getProductsForSupplierCategory(int supplierId, Category category) {
        String query = (category.getCategoryId() == 160) ?
                "SELECT product_id FROM product_per_supplier WHERE supplier_id = ?" :
                """
                        SELECT p.product_id 
                        FROM product_per_supplier ps
                        JOIN products p ON ps.product_id = p.product_id
                        WHERE ps.supplier_id = ? AND p.product_category = ?
                        """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, supplierId);
            if (category.getCategoryId() != 160) {
                statement.setInt(2, category.getCategoryId());
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                List<Integer> products = new ArrayList<>();
                while (resultSet.next()) {
                    products.add(resultSet.getInt("product_id"));
                }
                return products;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return List.of(); // Return an empty list if there's an error
    }

    /**
     * Retrieves the full inventory details for a supplier, branch, and category.
     */
    public List<PhysicalInventoryDetails> getInventory(Supplier supplier, Branch branch, Category category, LocalDate cutOffDate) {
        // Step 1: Get last physical inventory date
        LocalDate lastInventoryDate = getLastPhysicalInventoryDate(branch, cutOffDate);
        if (lastInventoryDate == null) {
            System.out.println("No previous inventory record found. Defaulting to start of records.");
            lastInventoryDate = LocalDate.of(2000, 1, 1);
        }

        Timestamp startDate = Timestamp.valueOf(lastInventoryDate.atStartOfDay());
        Timestamp endDate = Timestamp.valueOf(cutOffDate.atStartOfDay());

        // Step 2: Get relevant product IDs
        List<Integer> parentProducts = getProductsForSupplierCategory(supplier.getId(), category);
        List<Integer> allProductIds = new ArrayList<>(parentProducts);

        if (!parentProducts.isEmpty()) {
            String placeholders = parentProducts.stream().map(id -> "?").collect(Collectors.joining(","));
            String childQuery = "SELECT product_id FROM products WHERE parent_id IN (" + placeholders + ")";
            if (category.getCategoryId() != 160) {
                childQuery += " AND product_category = ?";
            }

            try (Connection connection = dataSource.getConnection();
                 PreparedStatement childStatement = connection.prepareStatement(childQuery)) {

                // Set parent product IDs dynamically
                for (int i = 0; i < parentProducts.size(); i++) {
                    childStatement.setInt(i + 1, parentProducts.get(i));
                }
                if (category.getCategoryId() != 160) {
                    childStatement.setInt(parentProducts.size() + 1, category.getCategoryId());
                }

                try (ResultSet resultSet = childStatement.executeQuery()) {
                    while (resultSet.next()) {
                        allProductIds.add(resultSet.getInt("product_id"));
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        // Step 3: Get inventory movements within the period
        if (allProductIds.isEmpty()) {
            System.out.println("No products found for this supplier and category.");
            return List.of(); // Return empty list if no products exist
        }

        ObservableList<ProductLedger> inventoryMovements = productLedgerDAO.getProductLedger(startDate, endDate, getProductList(allProductIds), branch);

        // Step 4: Compute inventory based on movements
        return computeFinalInventory(inventoryMovements);
    }


    ProductLedgerDAO productLedgerDAO = new ProductLedgerDAO();

    // Helper method to find last physical inventory date
    private LocalDate getLastPhysicalInventoryDate(Branch branch, LocalDate cutOffDate) {
        String query = """
                    SELECT MAX(cutOff_date) AS last_date 
                    FROM physical_inventory 
                    WHERE branch_id = ? AND cutOff_date < ?
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, branch.getId());
            statement.setTimestamp(2, Timestamp.valueOf(cutOffDate.atStartOfDay()));  // Ensure proper conversion

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Timestamp lastDate = resultSet.getTimestamp("last_date"); // Use getTimestamp()
                    return lastDate != null ? lastDate.toLocalDateTime().toLocalDate() : null;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    // Helper method to convert product IDs to Product objects
    private ObservableList<Product> getProductList(List<Integer> productIds) {
        ObservableList<Product> products = FXCollections.observableArrayList();
        for (int productId : productIds) {
            products.add(productDAO.getProductById(productId));
        }
        return products;
    }

    // Compute final inventory based on movements
    private List<PhysicalInventoryDetails> computeFinalInventory(ObservableList<ProductLedger> ledgers) {
        Map<Integer, Integer> inventoryMap = new HashMap<>();

        for (ProductLedger ledger : ledgers) {
            int productId = ledger.getProduct().getProductId();
            int unitCount = ledger.getProduct().getUnitOfMeasurementCount();

            // Compute total in and out before division
            int totalIn = ledger.getIn();
            int totalOut = ledger.getOut();

            // Update the inventory map
            inventoryMap.compute(productId, (key, existingCount) ->
                    (existingCount == null ? 0 : existingCount) + (totalIn - totalOut) / unitCount
            );
        }

        List<PhysicalInventoryDetails> inventoryDetailsList = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : inventoryMap.entrySet()) {
            Product product = productDAO.getProductById(entry.getKey());
            if (product != null) {  // Ensure product exists
                PhysicalInventoryDetails details = new PhysicalInventoryDetails();
                details.setProduct(product);
                details.setSystemCount(entry.getValue());
                inventoryDetailsList.add(details);
            }
        }

        return inventoryDetailsList;
    }



    /**
     * Inserts a physical inventory detail record.
     */
    public void insert(PhysicalInventoryDetails details) {
        String insertQuery = """
                    INSERT INTO physical_inventory_details 
                    (ph_id, date_encoded, product_id, unit_price, system_count, 
                     physical_count, variance, difference_cost, amount)
                    VALUES (?, NOW(), ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(insertQuery)) {

            statement.setInt(1, details.getPhysicalInventory().getId());
            statement.setInt(2, details.getProduct().getProductId());
            statement.setDouble(3, details.getUnitPrice());
            statement.setInt(4, details.getSystemCount());
            statement.setInt(5, details.getPhysicalCount());
            statement.setInt(6, details.getVariance());
            statement.setDouble(7, details.getDifferenceCost());
            statement.setDouble(8, details.getAmount());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ObservableList<PhysicalInventoryDetails> getPhysicalInventoryDetails(PhysicalInventory selectedInventory) {
        ObservableList<PhysicalInventoryDetails> detailsList = FXCollections.observableArrayList();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM physical_inventory_details WHERE ph_id = ?")) {

            statement.setInt(1, selectedInventory.getId());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    PhysicalInventoryDetails details = new PhysicalInventoryDetails();
                    details.setPhysicalInventory(selectedInventory);
                    details.setProduct(productDAO.getProductById(resultSet.getInt("product_id")));
                    details.setUnitPrice(resultSet.getDouble("unit_price"));
                    details.setSystemCount(resultSet.getInt("system_count"));
                    details.setPhysicalCount(resultSet.getInt("physical_count"));
                    details.setVariance(resultSet.getInt("variance"));
                    details.setDifferenceCost(resultSet.getDouble("difference_cost"));
                    details.setAmount(resultSet.getDouble("amount"));
                    detailsList.add(details);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return detailsList;
    }
}
