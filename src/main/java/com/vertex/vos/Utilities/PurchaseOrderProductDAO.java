package com.vertex.vos.Utilities;

import com.vertex.vos.Constructors.ProductsInTransact;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PurchaseOrderProductDAO {
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    public boolean entryProductPerPO(ProductsInTransact productsInTransact) throws SQLException {
        String query = "INSERT INTO purchase_order_products (purchase_order_id, product_id, ordered_quantity, " +
                "unit_price, branch_id) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, productsInTransact.getPurchaseOrderId());
            preparedStatement.setInt(2, productsInTransact.getProductId());
            preparedStatement.setInt(3, productsInTransact.getOrderedQuantity());
            preparedStatement.setDouble(4, productsInTransact.getUnitPrice());
            preparedStatement.setInt(5, productsInTransact.getBranchId());

            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0; // Return true if rows were affected (insert successful)
        }
    }
}
