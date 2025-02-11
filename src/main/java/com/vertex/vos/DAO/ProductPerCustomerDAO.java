package com.vertex.vos.DAO;

import com.vertex.vos.Objects.Customer;
import com.vertex.vos.Objects.DiscountType;
import com.vertex.vos.Objects.Product;
import com.vertex.vos.Utilities.DatabaseConnectionPool;
import com.vertex.vos.Utilities.DiscountDAO;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductPerCustomerDAO {
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    // Create
    public List<Integer> addProductsForCustomer(Customer customer, List<Product> products, DiscountType discountType) {
        String query = "INSERT INTO product_per_customer (customer_code, product_id, discount_type, unit_price) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE product_id = product_id";
        List<Integer> generatedIds = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            for (Product product : products) {
                statement.setString(1, customer.getCustomerCode());
                statement.setInt(2, product.getProductId());
                statement.setInt(3, discountType.getId());
                statement.setDouble(4, product.getPricePerUnit());
                statement.addBatch(); // Add the current statement to the batch
            }

            int[] rowsAffected = statement.executeBatch(); // Execute batch

            ResultSet generatedKeys = statement.getGeneratedKeys();
            while (generatedKeys.next()) {
                generatedIds.add(generatedKeys.getInt(1)); // Collect the generated IDs
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return generatedIds; // Return list of generated IDs
    }

    DiscountDAO discountDAO = new DiscountDAO();

    public Product getCustomerProductByCustomerAndProduct(Product product, Customer customer) {
        String query = "SELECT * FROM product_per_customer WHERE customer_code = ? AND product_id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, customer.getCustomerCode());
            statement.setInt(2, product.getProductId());
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                Product customerProduct = new Product();
                customerProduct.setDiscountType(discountDAO.getDiscountTypeById(resultSet.getInt("discount_type")));
                customerProduct.setPricePerUnit(resultSet.getDouble("unit_price"));
                return customerProduct;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Read
    public List<Product> getProductsForCustomer(Customer customer) {
        String query = "SELECT p.product_id, p.product_name, pc.unit_price AS price_per_unit, p.description, \n" +
                "       d.id AS discount_type_id, d.discount_type AS discount_type_name, \n" +
                "       u.unit_name, b.brand_name, c.category_name\n" +
                "FROM product_per_customer pc \n" +
                "JOIN products p ON pc.product_id = p.product_id\n" +
                "LEFT JOIN discount_type d ON pc.discount_type = d.id \n" +
                "JOIN units u ON p.unit_of_measurement = u.unit_id \n" +
                "JOIN brand b ON p.product_brand = b.brand_id \n" +
                "JOIN categories c ON p.product_category = c.category_id \n" +
                "WHERE pc.customer_code = ?\n";

        List<Product> products = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, customer.getCustomerCode());
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Product product = new Product();
                product.setProductId(resultSet.getInt("product_id"));
                product.setProductName(resultSet.getString("product_name"));
                product.setPricePerUnit(resultSet.getDouble("price_per_unit")); // Price from `unit_price` column
                product.setDescription(resultSet.getString("description"));
                product.setProductCategoryString(resultSet.getString("category_name"));
                product.setProductBrandString(resultSet.getString("brand_name"));

                // Create and set the DiscountType object if applicable
                int discountTypeId = resultSet.getInt("discount_type_id");
                if (!resultSet.wasNull()) { // Check for NULL
                    DiscountType discountType = new DiscountType();
                    discountType.setId(discountTypeId);
                    discountType.setTypeName(resultSet.getString("discount_type_name"));
                    product.setDiscountType(discountType);
                }

                // Set the unit name in the Product object
                product.setUnitOfMeasurementString(resultSet.getString("unit_name"));

                products.add(product);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }


    // Update
    public boolean updateProductsForCustomer(Customer customer, List<Product> products) {
        String query = "UPDATE product_per_customer SET discount_type = ?, unit_price = ? WHERE customer_code = ? AND product_id = ?";

        try (Connection connection = dataSource.getConnection()) {
            // Start a transaction
            connection.setAutoCommit(false);

            try (PreparedStatement statement = connection.prepareStatement(query)) {
                // Iterate over the list of products and set the parameters for each update
                for (Product product : products) {
                    statement.setInt(1, product.getDiscountType().getId());
                    statement.setDouble(2, product.getPricePerUnit());
                    statement.setString(3, customer.getCustomerCode());
                    statement.setInt(4, product.getProductId());

                    // Add the update to the batch
                    statement.addBatch();
                }

                // Execute all updates in a batch
                int[] rowsAffected = statement.executeBatch();

                // Commit the transaction if all updates were successful
                connection.commit();

                // Return true if at least one row was affected
                return rowsAffected.length > 0;
            } catch (SQLException e) {
                // Rollback the transaction in case of an error
                connection.rollback();
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }


    // Delete
    public boolean deleteProductForCustomer(Customer customer, Product product) {
        String query = "DELETE FROM product_per_customer WHERE customer_code = ? AND product_id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, customer.getCustomerCode());
            statement.setInt(2, product.getProductId());

            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
