package com.vertex.vos.DAO;

import com.vertex.vos.Objects.*;
import com.vertex.vos.Utilities.DatabaseConnectionPool;
import com.vertex.vos.Utilities.ProductDAO;
import com.vertex.vos.Utilities.ProductsPerSupplierDAO;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PhysicalInventoryDetailsDAO {
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    ProductDAO productDAO = new ProductDAO();
    ProductsPerSupplierDAO productsPerSupplierDAO = new ProductsPerSupplierDAO();

    public List<PhysicalInventoryDetails> getInventory(Supplier supplier, Branch branch, Category category) {
        List<Integer> productsPerSupplier = productsPerSupplierDAO.getProductsForSupplier(supplier.getId());
        List<PhysicalInventoryDetails> inventoryDetailsList = new ArrayList<>();

        String query = "SELECT * FROM inventory WHERE product_id = ? AND branch_id = ? AND quantity != 0";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            for (Integer productId : productsPerSupplier) {
                preparedStatement.setInt(1, productId);
                preparedStatement.setInt(2, branch.getId()); // Assuming Branch has a method getId()

                try (var resultSet = preparedStatement.executeQuery()) {
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
}
