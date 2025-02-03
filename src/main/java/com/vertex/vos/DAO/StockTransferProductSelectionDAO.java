package com.vertex.vos.DAO;

import com.vertex.vos.Objects.*;
import com.vertex.vos.Utilities.DatabaseConnectionPool;
import com.vertex.vos.Utilities.ProductDAO;
import com.vertex.vos.Utilities.UnitDAO;
import com.zaxxer.hikari.HikariDataSource;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StockTransferProductSelectionDAO {
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    UnitDAO unitDAO = new UnitDAO();

    public ObservableList<Unit> getUnits() {
        return unitDAO.getAllUnits();
    }

    public List<Category> getCategoriesWithInventory(int branchId) {
        List<Category> categories = new ArrayList<>();
        String query = """
                    SELECT DISTINCT c.category_id, c.category_name
                    FROM inventory i
                    JOIN products p ON i.product_id = p.product_id
                    JOIN categories c ON p.product_category = c.category_id
                    WHERE i.branch_id = ? AND i.quantity > 0
                """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, branchId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    categories.add(new Category(rs.getInt("category_id"), rs.getString("category_name")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Consider using a logging framework
        }
        return categories;
    }

    public List<Brand> getBrandsWithInventory(int branchId) {
        List<Brand> brands = new ArrayList<>();
        String query = """
                    SELECT DISTINCT b.brand_id, b.brand_name
                    FROM inventory i
                    JOIN products p ON i.product_id = p.product_id
                    JOIN brand b ON p.product_brand = b.brand_id
                    WHERE i.branch_id = ? AND i.quantity > 0
                """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, branchId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    brands.add(new Brand(rs.getInt("brand_id"), rs.getString("brand_name")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Consider using a logging framework
        }
        return brands;
    }

    public List<String> getProductNamesWithInventory(int branchId) {
        List<String> products = new ArrayList<>();
        String query = """
                    SELECT DISTINCT p.product_name
                    FROM inventory i
                    JOIN products p ON i.product_id = p.product_id
                    WHERE i.branch_id = ? AND i.quantity > 0
                """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, branchId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    products.add(rs.getString("product_name"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Consider using a logging framework
        }
        return products;
    }

    ProductDAO productDAO = new ProductDAO();

    public List<ProductsInTransact> getFilteredProducts(int branchId, String category, String brand, String productName, String unit, int limit, int offset) {
        List<ProductsInTransact> products = new ArrayList<>();
        String query = """
                SELECT i.quantity, p.product_name, p.product_id , p.product_code, c.category_name, b.brand_name, u.unit_name
                FROM inventory i
                JOIN products p ON i.product_id = p.product_id
                JOIN categories c ON p.product_category = c.category_id
                JOIN brand b ON p.product_brand = b.brand_id
                JOIN units u ON p.unit_of_measurement = u.unit_id
                WHERE i.branch_id = ? AND i.quantity > 0
            """;

        // Dynamically build the WHERE clause based on provided filters
        List<String> conditions = new ArrayList<>();
        if (category != null && !category.isEmpty()) conditions.add("c.category_name LIKE ?");
        if (brand != null && !brand.isEmpty()) conditions.add("b.brand_name LIKE ?");
        if (productName != null && !productName.isEmpty()) conditions.add("p.product_name LIKE ?");
        if (unit != null && !unit.isEmpty()) conditions.add("u.unit_name LIKE ?");

        if (!conditions.isEmpty()) {
            query += " AND " + String.join(" AND ", conditions);
        }

        // Add pagination to the query
        query += " LIMIT ? OFFSET ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, branchId);

            int index = 2;
            if (category != null && !category.isEmpty()) stmt.setString(index++, "%" + category + "%");
            if (brand != null && !brand.isEmpty()) stmt.setString(index++, "%" + brand + "%");
            if (productName != null && !productName.isEmpty()) stmt.setString(index++, "%" + productName + "%");
            if (unit != null && !unit.isEmpty()) stmt.setString(index++, "%" + unit + "%");

            // Set limit and offset for pagination
            stmt.setInt(index++, limit);
            stmt.setInt(index++, offset);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ProductsInTransact product = new ProductsInTransact();
                    product.setReceivedQuantity(rs.getInt("quantity"));
                    product.setProduct(getProductById(rs.getInt("product_id")));
                    products.add(product);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Consider logging instead
        }
        return products;
    }

    private Product getProductById(int productId) {
        String sqlQuery = """
        SELECT p.product_id, p.product_code ,p.product_name, p.product_code, p.product_category,
               c.category_name, b.brand_name, u.unit_name
        FROM products p
        JOIN categories c ON p.product_category = c.category_id
        JOIN brand b ON p.product_brand = b.brand_id
        JOIN units u ON p.unit_of_measurement = u.unit_id
        WHERE p.product_id = ?
    """;

        Product product = null;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlQuery)) {

            stmt.setInt(1, productId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    product = new Product();
                    product.setProductId(rs.getInt("product_id"));
                    product.setProductName(rs.getString("product_name"));
                    product.setProductCode(rs.getString("product_code"));
                    product.setProductCategory(rs.getInt("product_category"));
                    product.setProductCategoryString(rs.getString("category_name"));
                    product.setProductBrandString(rs.getString("brand_name"));
                    product.setUnitOfMeasurementString(rs.getString("unit_name"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Consider using a logger for better error handling
        }

        return product;
    }



}