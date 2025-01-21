package com.vertex.vos.DAO;

import com.vertex.vos.Objects.*;
import com.vertex.vos.Utilities.DatabaseConnectionPool;
import com.vertex.vos.Utilities.ProductDAO;
import com.vertex.vos.Utilities.ProductsPerSupplierDAO;
import com.zaxxer.hikari.HikariDataSource;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
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
    public List<PhysicalInventoryDetails> getInventory(Supplier supplier, Branch branch, Category category) {
        List<Integer> parentProducts = getProductsForSupplierCategory(supplier.getId(), category);

        // Fetch all product IDs (parents + children) in one query for better performance
        String productIds = parentProducts.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        String childQuery = """
                    SELECT product_id 
                    FROM products 
                    WHERE parent_id IN (%s)
                """.formatted(productIds);

        if (category.getCategoryId() != 160) {
            childQuery += " AND product_category = ?";
        }

        List<Integer> allProductIds = new ArrayList<>(parentProducts);
        try (Connection connection = dataSource.getConnection();
             PreparedStatement childStatement = connection.prepareStatement(childQuery)) {

            if (category.getCategoryId() != 160) {
                childStatement.setInt(1, category.getCategoryId());
            }

            try (ResultSet resultSet = childStatement.executeQuery()) {
                while (resultSet.next()) {
                    allProductIds.add(resultSet.getInt("product_id"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Fetch inventory details for all products in a single query
        String inventoryQuery = """
                    SELECT i.*, p.product_name 
                    FROM inventory i
                    JOIN products p ON i.product_id = p.product_id
                    WHERE i.product_id IN (%s) AND i.branch_id = ? AND i.quantity != 0
                """.formatted(allProductIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(",")));

        List<PhysicalInventoryDetails> inventoryDetailsList = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement inventoryStatement = connection.prepareStatement(inventoryQuery)) {

            inventoryStatement.setInt(1, branch.getId());
            try (ResultSet resultSet = inventoryStatement.executeQuery()) {
                while (resultSet.next()) {
                    PhysicalInventoryDetails details = new PhysicalInventoryDetails();
                    details.setProduct(productDAO.getProductById(resultSet.getInt("product_id"))); // Assuming cache can be used
                    details.setSystemCount(resultSet.getInt("quantity"));
                    inventoryDetailsList.add(details);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
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
