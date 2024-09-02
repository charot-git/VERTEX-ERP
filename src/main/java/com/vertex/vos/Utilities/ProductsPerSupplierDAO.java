package com.vertex.vos.Utilities;

import com.vertex.vos.Objects.Product;
import com.vertex.vos.Objects.Taskbar;
import com.zaxxer.hikari.HikariDataSource;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductsPerSupplierDAO {
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    public int addProductForSupplier(int supplierId, int productId) {
        String query = "INSERT INTO product_per_supplier (supplier_id, product_id) VALUES (?, ?)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, supplierId);
            statement.setInt(2, productId);
            int rowsAffected = statement.executeUpdate();

            if (rowsAffected == 1) {
                ResultSet generatedKeys = statement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1); // Return the generated ID
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private final int batchSize = 100; // Number of products to fetch in each batch
    private int offset = 0;


    public Task<ObservableList<Product>> searchProductTask(int supplierId, String searchQuery, String brand, String unit) {
        return new Task<>() {
            @Override
            protected ObservableList<Product> call() throws Exception {
                ObservableList<Product> products = FXCollections.observableArrayList();
                StringBuilder sql = new StringBuilder(
                        "SELECT " +
                                "    pps.supplier_id, " +
                                "    p.product_id, " +
                                "    p.parent_id, " +
                                "    p.product_name, " +
                                "    p.barcode, " +
                                "    p.description, " +
                                "    p.cost_per_unit, " +
                                "    p.price_per_unit, " +
                                "    p.priceA, " +
                                "    p.priceB, " +
                                "    p.priceC, " +
                                "    p.priceD, " +
                                "    p.priceE, " +
                                "    b.brand_name, " +
                                "    c.category_name, " +
                                "    u.unit_name " +
                                "FROM " +
                                "    product_per_supplier pps " +
                                "JOIN " +
                                "    products p ON pps.product_id = p.product_id " +
                                "LEFT JOIN " +
                                "    brand b ON p.product_brand = b.brand_id " +
                                "LEFT JOIN " +
                                "    categories c ON p.product_category = c.category_id " +
                                "LEFT JOIN " +
                                "    units u ON p.unit_of_measurement = u.unit_id " +
                                "WHERE " +
                                "    pps.supplier_id = ? " +
                                "    AND p.isActive = 1 " +
                                "    AND p.description LIKE ? ");

                // Append conditions based on the additional filters
                if (brand != null && !brand.isEmpty()) {
                    sql.append(" AND b.brand_name LIKE ?");
                }
                if (unit != null && !unit.isEmpty()) {
                    sql.append(" AND u.unit_name LIKE ?");
                }

                sql.append(
                        "UNION ALL " +
                                "SELECT " +
                                "    pps.supplier_id, " +
                                "    child.product_id, " +
                                "    child.parent_id, " +
                                "    child.product_name, " +
                                "    child.barcode, " +
                                "    child.description, " +
                                "    child.cost_per_unit, " +
                                "    child.price_per_unit, " +
                                "    child.priceA, " +
                                "    child.priceB, " +
                                "    child.priceC, " +
                                "    child.priceD, " +
                                "    child.priceE, " +
                                "    b.brand_name, " +
                                "    c.category_name, " +
                                "    u.unit_name " +
                                "FROM " +
                                "    product_per_supplier pps " +
                                "JOIN " +
                                "    products p ON pps.product_id = p.product_id " +
                                "JOIN " +
                                "    products child ON p.product_id = child.parent_id " +
                                "LEFT JOIN " +
                                "    brand b ON child.product_brand = b.brand_id " +
                                "LEFT JOIN " +
                                "    categories c ON child.product_category = c.category_id " +
                                "LEFT JOIN " +
                                "    units u ON child.unit_of_measurement = u.unit_id " +
                                "WHERE " +
                                "    pps.supplier_id = ? " +
                                "    AND child.description LIKE ? ");

                if (brand != null && !brand.isEmpty()) {
                    sql.append(" AND b.brand_name LIKE ?");
                }
                if (unit != null && !unit.isEmpty()) {
                    sql.append(" AND u.unit_name LIKE ?");
                }

                sql.append(" ORDER BY description");

                try (Connection conn = dataSource.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

                    int index = 1;
                    pstmt.setInt(index++, supplierId);
                    pstmt.setString(index++, "%" + searchQuery + "%");

                    if (brand != null && !brand.isEmpty()) {
                        pstmt.setString(index++, "%" + brand + "%");
                    }
                    if (unit != null && !unit.isEmpty()) {
                        pstmt.setString(index++, "%" + unit + "%");
                    }

                    pstmt.setInt(index++, supplierId);
                    pstmt.setString(index++, "%" + searchQuery + "%");

                    if (brand != null && !brand.isEmpty()) {
                        pstmt.setString(index++, "%" + brand + "%");
                    }
                    if (unit != null && !unit.isEmpty()) {
                        pstmt.setString(index++, "%" + unit + "%");
                    }

                    try (ResultSet rs = pstmt.executeQuery()) {
                        while (rs.next()) {
                            Product product = new Product();
                            product.setProductId(rs.getInt("product_id"));
                            product.setParentId(rs.getInt("parent_id"));
                            product.setProductName(rs.getString("product_name"));
                            product.setBarcode(rs.getString("barcode"));
                            product.setDescription(rs.getString("description"));
                            product.setCostPerUnit(rs.getDouble("cost_per_unit"));
                            product.setPricePerUnit(rs.getDouble("price_per_unit"));
                            product.setPriceA(rs.getDouble("priceA"));
                            product.setPriceB(rs.getDouble("priceB"));
                            product.setPriceC(rs.getDouble("priceC"));
                            product.setPriceD(rs.getDouble("priceD"));
                            product.setPriceE(rs.getDouble("priceE"));
                            product.setProductBrandString(rs.getString("brand_name"));
                            product.setProductCategoryString(rs.getString("category_name"));
                            product.setUnitOfMeasurementString(rs.getString("unit_name"));

                            products.add(product);
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace(); // Handle exception
                }

                return products;
            }
        };
    }




    public Task<ObservableList<Product>> getPaginatedProductsForSupplier(int supplierId) {
        return new Task<>() {
            @Override
            protected ObservableList<Product> call() throws Exception {
                ObservableList<Product> products = FXCollections.observableArrayList();
                String sql =
                        "SELECT " +
                                "    pps.supplier_id, " +
                                "    p.product_id, " +
                                "    p.parent_id, " +
                                "    p.product_name, " +
                                "    p.barcode, " +
                                "    p.description, " +
                                "    p.cost_per_unit, " +
                                "    p.price_per_unit, " +
                                "    p.priceA, " +
                                "    p.priceB, " +
                                "    p.priceC, " +
                                "    p.priceD, " +
                                "    p.priceE, " +
                                "    b.brand_name, " +
                                "    c.category_name, " +
                                "    u.unit_name " +
                                "FROM " +
                                "    product_per_supplier pps " +
                                "JOIN " +
                                "    products p ON pps.product_id = p.product_id " +
                                "LEFT JOIN " +
                                "    brand b ON p.product_brand = b.brand_id " +
                                "LEFT JOIN " +
                                "    categories c ON p.product_category = c.category_id " +
                                "LEFT JOIN " +
                                "    units u ON p.unit_of_measurement = u.unit_id " +
                                "WHERE " +
                                "    pps.supplier_id = ? AND p.isActive = 1 " +
                                "UNION ALL " +
                                "SELECT " +
                                "    pps.supplier_id, " +
                                "    child.product_id, " +
                                "    child.parent_id, " +
                                "    child.product_name, " +
                                "    child.barcode, " +
                                "    child.description, " +
                                "    child.cost_per_unit, " +
                                "    child.price_per_unit, " +
                                "    child.priceA, " +
                                "    child.priceB, " +
                                "    child.priceC, " +
                                "    child.priceD, " +
                                "    child.priceE, " +
                                "    b.brand_name, " +
                                "    c.category_name, " +
                                "    u.unit_name " +
                                "FROM " +
                                "    product_per_supplier pps " +
                                "JOIN " +
                                "    products p ON pps.product_id = p.product_id " +
                                "JOIN " +
                                "    products child ON p.product_id = child.parent_id " +
                                "LEFT JOIN " +
                                "    brand b ON child.product_brand = b.brand_id " +
                                "LEFT JOIN " +
                                "    categories c ON child.product_category = c.category_id " +
                                "LEFT JOIN " +
                                "    units u ON child.unit_of_measurement = u.unit_id " +
                                "WHERE " +
                                "    pps.supplier_id = ? " +
                                "ORDER BY " +
                                "    description " +
                                "LIMIT ? OFFSET ?";

                try (Connection conn = dataSource.getConnection(); // Assumes you have a method to get a DB connection
                     PreparedStatement pstmt = conn.prepareStatement(sql)) {

                    pstmt.setInt(1, supplierId); // Set supplierId for the first query part
                    pstmt.setInt(2, supplierId); // Set supplierId for the second query part
                    pstmt.setInt(3, batchSize);  // Set batchSize for LIMIT
                    pstmt.setInt(4, offset);     // Set offset for OFFSET

                    try (ResultSet rs = pstmt.executeQuery()) {
                        while (rs.next()) {
                            Product product = new Product();
                            product.setProductId(rs.getInt("product_id"));
                            product.setParentId(rs.getInt("parent_id"));
                            product.setProductName(rs.getString("product_name"));
                            product.setBarcode(rs.getString("barcode"));
                            product.setDescription(rs.getString("description"));
                            product.setCostPerUnit(rs.getDouble("cost_per_unit"));
                            product.setPricePerUnit(rs.getDouble("price_per_unit"));
                            product.setPriceA(rs.getDouble("priceA"));
                            product.setPriceB(rs.getDouble("priceB"));
                            product.setPriceC(rs.getDouble("priceC"));
                            product.setPriceD(rs.getDouble("priceD"));
                            product.setPriceE(rs.getDouble("priceE"));
                            product.setProductBrandString(rs.getString("brand_name"));
                            product.setProductCategoryString(rs.getString("category_name"));
                            product.setUnitOfMeasurementString(rs.getString("unit_name"));

                            products.add(product);
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace(); // Handle exception
                }

                offset += batchSize;

                return products;
            }
        };
    }




    public List<Integer> getProductsForSupplier(int supplierId) {
        List<Integer> products = new ArrayList<>();
        String query = "SELECT product_id FROM product_per_supplier WHERE supplier_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, supplierId);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                products.add(resultSet.getInt("product_id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    public boolean deleteProductForSupplier(int supplierId, int productId) {
        String query = "DELETE FROM product_per_supplier WHERE supplier_id = ? AND product_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, supplierId);
            statement.setInt(2, productId);
            int rowsAffected = statement.executeUpdate();
            return rowsAffected == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
