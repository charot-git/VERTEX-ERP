package com.vertex.vos.Utilities;

import com.vertex.vos.Constructors.Product;
import com.vertex.vos.Constructors.ProductInventory;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.*;

public class ProductDAO {
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    public Product getProductDetails(String productName) {
        Product product = null;
        String sqlQuery = "SELECT * FROM products WHERE product_name = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {

            preparedStatement.setString(1, productName);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    // Retrieve product details from the result set and create a Product object
                    product = new Product();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle any SQL exceptions here
        }

        return product;
    }

    public boolean addProduct(Product product) {
        String sqlQuery = "INSERT INTO products " +
                "(product_name, product_code, description, supplier_name, date_added, product_brand, product_category, product_segment, product_section, base_unit, isActive, product_class, product_nature, product_shelf_life, maintaining_base_quantity, product_base_weight) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {

            preparedStatement.setString(1, product.getProduct_name());
            preparedStatement.setString(2, product.getProduct_code());
            preparedStatement.setString(3, product.getDescription());
            preparedStatement.setInt(4, product.getSupplier_name());
            preparedStatement.setDate(5, product.getDate_added());
            preparedStatement.setInt(6, product.getProduct_brand()); // Assuming product_brand is an ID reference
            preparedStatement.setInt(7, product.getProduct_category()); // Assuming product_category is an ID reference
            preparedStatement.setInt(8, product.getProduct_segment()); // Assuming product_segment is an ID reference
            preparedStatement.setInt(9, product.getProduct_section()); // Assuming product_section is an ID reference
            preparedStatement.setInt(10, product.getBase_unit());
            preparedStatement.setInt(11, 1);
            preparedStatement.setInt(12, product.getProduct_class()); // Assuming product_class is an ID reference
            preparedStatement.setInt(13, product.getProduct_nature()); // Assuming product_nature is an ID reference
            preparedStatement.setInt(14, product.getProduct_shelf_life());
            preparedStatement.setInt(15, product.getMaintaining_base_quantity());
            preparedStatement.setDouble(16, product.getProduct_base_weight());

            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle any SQL exceptions here
            return false;
        }

    }

    public Product getProductByCode(String productCode) {
        Product product = null;
        String sqlQuery = "SELECT * FROM products WHERE product_code = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {

            preparedStatement.setString(1, productCode);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    product = new Product();
                    product.setProduct_id(resultSet.getInt("product_id"));
                    product.setProduct_name(resultSet.getString("product_name"));
                    product.setProduct_code(resultSet.getString("product_code"));
                    product.setDescription(resultSet.getString("description"));
                    product.setSupplier_name(resultSet.getInt("supplier_name"));
                    product.setDate_added(resultSet.getDate("date_added"));
                    product.setProduct_brand(resultSet.getInt("product_brand"));
                    product.setProduct_category(resultSet.getInt("product_category"));
                    product.setProduct_segment(resultSet.getInt("product_segment"));
                    product.setProduct_section(resultSet.getInt("product_section"));
                    product.setBase_unit(resultSet.getInt("base_unit"));
                    product.setProduct_class(resultSet.getInt("product_class"));
                    product.setProduct_nature(resultSet.getInt("product_nature"));
                    product.setProduct_shelf_life(resultSet.getInt("product_shelf_life"));
                    product.setMaintaining_base_quantity(resultSet.getInt("maintaining_base_quantity"));
                    product.setProduct_base_weight(resultSet.getDouble("product_base_weight"));
                    // Set other product attributes accordingly
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle any SQL exceptions here
        }

        return product;
    }


    public int getProductIdByName(String productName) {
        String sqlQuery = "SELECT product_id FROM products WHERE product_name = ?";
        int supplierId = -1; // Set a default value indicating not found

        try (Connection connection = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {

            preparedStatement.setString(1, productName);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    supplierId = resultSet.getInt("product_id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle any SQL exceptions here
        }

        return supplierId;
    }

    public boolean addProductConfiguration(ProductInventory productInventory) {
        String sqlQuery = "INSERT INTO product_inventory " +
                "(product_id, price, unit_of_measurement, unit_of_measurement_count, barcode, description, secondary_category, secondary_segment, estimated_unit_cost, estimated_extended_cost) " +
                "VALUES (? , ? , ? , ? , ? , ? , ? ,?,?,?  )";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {

            preparedStatement.setInt(1, productInventory.getProduct_id());
            preparedStatement.setDouble(2, productInventory.getPrice());
            preparedStatement.setInt(3, productInventory.getUnit_of_measurement());
            preparedStatement.setInt(4, productInventory.getUnit_of_measurement_count());
            preparedStatement.setString(5, productInventory.getBarcode());
            preparedStatement.setString(6, productInventory.getDescription());
            preparedStatement.setInt(7, productInventory.getSecondary_category());
            preparedStatement.setInt(8, productInventory.getSecondary_segment());
            preparedStatement.setDouble(9, productInventory.getEstimated_unit_cost());
            preparedStatement.setDouble(10, productInventory.getEstimated_extended_cost());

            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle any SQL exceptions here
            return false;
        }
    }
}
