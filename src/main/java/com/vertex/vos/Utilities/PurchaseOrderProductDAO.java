package com.vertex.vos.Utilities;

import com.vertex.vos.Constructors.ProductsInTransact;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class PurchaseOrderProductDAO {

    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    // Method to insert purchase order products into the database
    public void insertPurchaseOrderProducts(int purchaseOrderId, List<ProductsInTransact> productsList) throws SQLException {
        String sql = "INSERT INTO purchase_order_products (purchase_order_id, product_id, ordered_quantity, unit_price, " +
                "vat_amount, withholding_amount, total_amount) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            for (ProductsInTransact product : productsList) {

                preparedStatement.executeUpdate();
            }
        }
    }
}
