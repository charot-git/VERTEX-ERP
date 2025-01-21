package com.vertex.vos.DAO;

import com.vertex.vos.Objects.*;
import com.vertex.vos.Utilities.DatabaseConnectionPool;
import com.vertex.vos.Utilities.ProductDAO;
import com.vertex.vos.Utilities.ProductsPerSupplierDAO;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PhysicalInventoryDetailsDAO {
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    ProductDAO productDAO = new ProductDAO();
    ProductsPerSupplierDAO productsPerSupplierDAO = new ProductsPerSupplierDAO();

    // Retrieves products for a supplier, with optional category filtering
    public List<Integer> getProductsForSupplierCategory(int supplierId, Category category) {
        List<Integer> products = new ArrayList<>();
        String query;

        if (category.getCategoryId() == 160) { // If category is "All", don't filter by category
            query = "SELECT product_id FROM product_per_supplier WHERE supplier_id = ?";
        } else { // Filter by category
            query = """
                        SELECT p.product_id 
                        FROM product_per_supplier ps
                        JOIN products p ON ps.product_id = p.product_id
                        WHERE ps.supplier_id = ? AND p.product_category = ?
                    """;
        }

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, supplierId);
            if (category.getCategoryId() != 160) {
                statement.setInt(2, category.getCategoryId()); // Add category filter if applicable
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    products.add(resultSet.getInt("product_id"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return products;
    }

    public List<PhysicalInventoryDetails> getInventory(Supplier supplier, Branch branch, Category category) {
        // Get parent products for the supplier (with optional category filtering)
        List<Integer> parentProducts = getProductsForSupplierCategory(supplier.getId(), category);
        List<Integer> allProductIds = new ArrayList<>(parentProducts);

        // Fetch child products for each parent product
        String childQuery = """
                    SELECT product_id 
                    FROM products 
                    WHERE parent_id = ?
                """;

        // Add category filter only if the category is not "All"
        if (category.getCategoryId() != 160) {
            childQuery += " AND product_category = ?";
        }

        try (Connection connection = dataSource.getConnection();
             PreparedStatement childStatement = connection.prepareStatement(childQuery)) {

            for (Integer parentId : parentProducts) {
                childStatement.setInt(1, parentId);

                if (category.getCategoryId() != 160) {
                    childStatement.setInt(2, category.getCategoryId());
                }

                try (ResultSet resultSet = childStatement.executeQuery()) {
                    while (resultSet.next()) {
                        allProductIds.add(resultSet.getInt("product_id"));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Fetch inventory details for all products (filtered parent + child products)
        List<PhysicalInventoryDetails> inventoryDetailsList = new ArrayList<>();
        String inventoryQuery = """
                    SELECT * 
                    FROM inventory 
                    WHERE product_id = ? AND branch_id = ? AND quantity != 0
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement inventoryStatement = connection.prepareStatement(inventoryQuery)) {

            for (Integer productId : allProductIds) {
                inventoryStatement.setInt(1, productId);
                inventoryStatement.setInt(2, branch.getId()); // Assuming Branch has a method getId()

                try (ResultSet resultSet = inventoryStatement.executeQuery()) {
                    if (resultSet.next()) {
                        PhysicalInventoryDetails details = new PhysicalInventoryDetails();
                        details.setProduct(productDAO.getProductById(productId)); // Assuming ProductDAO has this method
                        details.setSystemCount(resultSet.getInt("quantity"));
                        inventoryDetailsList.add(details);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return inventoryDetailsList; // Return list, empty if no inventory found
    }

    public void insert(PhysicalInventoryDetails details) {
        String insertQuery = """
                    INSERT INTO physical_inventory_details 
                    (ph_id, date_encoded, product_id, unit_price, system_count, 
                     physical_count, variance, difference_cost, amount)
                    VALUES (?, NOW(), ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(insertQuery)) {

            // Set parameters for the prepared statement
            statement.setInt(1, details.getPhysicalInventory().getId()); // ph_id from PhysicalInventory object
            statement.setInt(2, details.getProduct().getProductId());  // product_id from Product object
            statement.setDouble(3, details.getUnitPrice());             // unit_price
            statement.setInt(4, details.getSystemCount());              // system_count
            statement.setInt(5, details.getPhysicalCount());            // physical_count
            statement.setInt(6, details.getVariance());                 // variance (physical_count - system_count)
            statement.setDouble(7, details.getDifferenceCost());        // difference_cost (variance * unit_price)
            statement.setDouble(8, details.getAmount());                // amount (depends on your formula for amount)

            // Execute the insert statement
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
