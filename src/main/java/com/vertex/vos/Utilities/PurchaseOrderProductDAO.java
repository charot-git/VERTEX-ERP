package com.vertex.vos.Utilities;

import com.vertex.vos.Constructors.Product;
import com.vertex.vos.Constructors.ProductsInTransact;
import com.vertex.vos.Constructors.PurchaseOrder;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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

    public boolean updateApprovedPrice(int purchaseOrderProductId, double approvedPrice) throws SQLException {
        String query = "UPDATE purchase_order_products SET approved_price = ? WHERE purchase_order_product_id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setDouble(1, approvedPrice);
            preparedStatement.setInt(2, purchaseOrderProductId);

            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0; // Return true if rows were affected (update successful)
        }
    }



    public List<ProductsInTransact> getProductsInTransactForBranch(PurchaseOrder purchaseOrder, int branchId) throws SQLException {
        ProductDAO productDAO = new ProductDAO();
        DiscountDAO discountDAO = new DiscountDAO();
        SupplierDAO supplierDAO = new SupplierDAO();
        List<ProductsInTransact> products = new ArrayList<>();
        String query = "SELECT * FROM purchase_order_products WHERE purchase_order_id = ? AND branch_id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, purchaseOrder.getPurchaseOrderNo());
            preparedStatement.setInt(2, branchId);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                ProductsInTransact product = new ProductsInTransact();
                int productId = resultSet.getInt("product_id");
                Product productDetails = productDAO.getProductDetails(productId);
                int parentId = productDetails.getParentId();
                String productDescription = productDetails.getDescription();
                String stringUnit = productDetails.getUnitOfMeasurementString();
                product.setPurchaseOrderProductId(resultSet.getInt("purchase_order_product_id"));
                product.setPurchaseOrderId(resultSet.getInt("purchase_order_id"));
                product.setProductId(resultSet.getInt("product_id"));
                product.setDescription(productDescription);
                product.setOrderedQuantity(resultSet.getInt("ordered_quantity"));
                product.setUnitPrice(resultSet.getDouble("unit_price"));
                product.setBranchId(resultSet.getInt("branch_id"));
                product.setUnit(stringUnit);
                int discountTypeId = 0;

                if (parentId == 0) {
                    discountTypeId = discountDAO.getProductDiscountForProductTypeId(productId, purchaseOrder.getSupplierName());
                } else {
                    discountTypeId = discountDAO.getProductDiscountForProductTypeId(parentId, purchaseOrder.getSupplierName());

                }
                product.setDiscountTypeId(discountTypeId);


                products.add(product);
            }
        }
        return products;
    }

    public List<ProductsInTransact> getProductsInTransactForPO(int purchaseOrderId) throws SQLException {
        ProductDAO productDAO = new ProductDAO();
        DiscountDAO discountDAO = new DiscountDAO();
        List<ProductsInTransact> products = new ArrayList<>();
        String query = "SELECT * FROM purchase_order_products WHERE purchase_order_id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, purchaseOrderId);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                ProductsInTransact product = new ProductsInTransact();
                int productId = resultSet.getInt("product_id");
                Product productDetails = productDAO.getProductDetails(productId);
                int parentId = productDetails.getParentId();
                String productDescription = productDetails.getDescription();
                String stringUnit = productDetails.getUnitOfMeasurementString();
                product.setPurchaseOrderProductId(resultSet.getInt("purchase_order_product_id"));
                product.setPurchaseOrderId(resultSet.getInt("purchase_order_id"));
                product.setProductId(resultSet.getInt("product_id"));
                product.setDescription(productDescription);
                product.setOrderedQuantity(resultSet.getInt("ordered_quantity"));
                product.setUnitPrice(resultSet.getDouble("unit_price"));
                product.setBranchId(resultSet.getInt("branch_id"));
                product.setUnit(stringUnit);

                products.add(product);
            }
        }
        return products;
    }

}
