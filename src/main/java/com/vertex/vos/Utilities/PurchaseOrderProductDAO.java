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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public boolean quantityOverride(int purchaseOrderProductId, int newQuantity) throws SQLException {
        String query = "UPDATE purchase_order_products SET ordered_quantity = ? WHERE purchase_order_product_id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, newQuantity);
            preparedStatement.setInt(2, purchaseOrderProductId);

            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0; // Return true if rows were affected (update successful)
        }
    }

    public boolean approvePurchaseOrderProduct(int purchaseOrderProductId, double vatAmount, double withholdingAmount, double totalAmount) throws SQLException {
        String query = "UPDATE purchase_order_products SET vat_amount = ?, withholding_amount = ?, total_amount = ? WHERE purchase_order_product_id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setDouble(1, vatAmount);
            preparedStatement.setDouble(2, withholdingAmount);
            preparedStatement.setDouble(3, totalAmount);
            preparedStatement.setInt(4, purchaseOrderProductId);

            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0; // Return true if rows were affected (update successful)
        }
    }

    DiscountDAO discountDAO = new DiscountDAO();
    UnitDAO unitDAO = new UnitDAO();
    public List<ProductsInTransact> getProductsInTransactForBranch(PurchaseOrder purchaseOrder, int branchId) throws SQLException {
        List<ProductsInTransact> products = new ArrayList<>();
        String query = "SELECT pop.*, p.* FROM purchase_order_products pop " +
                "JOIN products p ON pop.product_id = p.product_id " +
                "WHERE pop.purchase_order_id = ? AND pop.branch_id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, purchaseOrder.getPurchaseOrderNo());
            preparedStatement.setInt(2, branchId);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                ProductsInTransact product = new ProductsInTransact();
                product.setPurchaseOrderProductId(resultSet.getInt("purchase_order_product_id"));
                product.setPurchaseOrderId(resultSet.getInt("purchase_order_id"));
                product.setProductId(resultSet.getInt("product_id"));
                product.setOrderedQuantity(resultSet.getInt("ordered_quantity"));
                product.setUnitPrice(resultSet.getDouble("unit_price"));
                product.setApprovedPrice(resultSet.getDouble("approved_price"));
                product.setBranchId(resultSet.getInt("branch_id"));

                // Set product details directly from resultSet instead of making separate DB calls
                product.setDescription(resultSet.getString("description"));
                int unitId = resultSet.getInt("unit_of_measurement");
                product.setUnit(unitDAO.getUnitNameById(unitId));

                int parentId = resultSet.getInt("parent_id");
                int discountTypeId = parentId == 0 ?
                        discountDAO.getProductDiscountForProductTypeId(product.getProductId(), purchaseOrder.getSupplierName()) :
                        discountDAO.getProductDiscountForProductTypeId(parentId, purchaseOrder.getSupplierName());

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
