package com.vertex.vos.Utilities;

import com.vertex.vos.Utilities.DatabaseConnectionPool;
import com.zaxxer.hikari.HikariDataSource;

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
            // Handle the exception according to your needs
        }
        return products;
    }
}
