package com.vertex.vos.Utilities;

import com.vertex.vos.Constructors.Product;
import com.vertex.vos.Constructors.ProductsInTransact;
import com.vertex.vos.Constructors.PurchaseOrder;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PurchaseOrderProductDAO {
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();
    double withholdingValue;
    double vatValue;

    public List<ProductsInTransact> getProductsForReceiving(int purchaseOrderId, int branchId) throws SQLException {
        List<ProductsInTransact> productsForReceiving = new ArrayList<>();

        String query = "SELECT pop.*, p.description, p.product_code, p.product_image, u.unit_name \n" +
                "FROM purchase_order_products pop \n" +
                "INNER JOIN products p ON pop.product_id = p.product_id \n" +
                "INNER JOIN units u ON p.unit_of_measurement = u.unit_id \n" +
                "WHERE pop.purchase_order_id = ? AND pop.branch_id = ? AND (pop.received IS NULL OR pop.received = 0)\n";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, purchaseOrderId);
            preparedStatement.setInt(2, branchId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    ProductsInTransact product = new ProductsInTransact();
                    product.setPurchaseOrderProductId(resultSet.getInt("purchase_order_product_id"));
                    product.setPurchaseOrderId(resultSet.getInt("purchase_order_id"));
                    product.setProductId(resultSet.getInt("product_id"));
                    product.setOrderedQuantity(resultSet.getInt("ordered_quantity"));
                    product.setUnitPrice(resultSet.getDouble("unit_price"));
                    product.setApprovedPrice(resultSet.getDouble("approved_price"));
                    product.setDiscountedPrice(resultSet.getDouble("discounted_price"));
                    product.setVatAmount(resultSet.getDouble("vat_amount"));
                    product.setWithholdingAmount(resultSet.getDouble("withholding_amount"));
                    product.setTotalAmount(resultSet.getDouble("total_amount"));
                    product.setBranchId(resultSet.getInt("branch_id"));
                    product.setReceivedQuantity(0); // Set received quantity to 0 for receiving
                    product.setDescription(resultSet.getString("description"));
                    product.setUnit(resultSet.getString("unit_name")); // Set the unit information

                    productsForReceiving.add(product);
                }
            }
        }

        return productsForReceiving;
    }

    private void initializeTaxes() {
        String query = "SELECT WithholdingRate, VATRate FROM tax_rates WHERE TaxID = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            int taxId = 1;

            preparedStatement.setInt(1, taxId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                withholdingValue = resultSet.getDouble("WithholdingRate");
                vatValue = resultSet.getDouble("VATRate");
            } else {
                // Handle the case where the specified TaxID was not found
                System.out.println("Tax rates not found for TaxID: " + taxId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle any SQL exceptions here
        }
    }

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

    public boolean updateApprovedPrice(int purchaseOrderProductId, double discountedPrice, double approvedPrice) throws SQLException {
        String query = "UPDATE purchase_order_products SET approved_price = ? , discounted_price = ? WHERE purchase_order_product_id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setDouble(1, approvedPrice);
            preparedStatement.setDouble(2, discountedPrice);
            preparedStatement.setInt(3, purchaseOrderProductId);

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
        String query = "SELECT pop.*, p.*, COALESCE(por.received_quantity, 0) AS received_quantity " +
                "FROM purchase_order_products pop " +
                "JOIN products p ON pop.product_id = p.product_id " +
                "LEFT JOIN purchase_order_receiving por ON pop.purchase_order_id = por.purchase_order_id AND pop.product_id = por.product_id AND pop.branch_id = por.branch_id " +
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
                product.setDiscountedPrice(resultSet.getDouble("discounted_price"));
                product.setBranchId(resultSet.getInt("branch_id"));
                product.setDescription(resultSet.getString("description"));
                int unitId = resultSet.getInt("unit_of_measurement");
                product.setUnit(unitDAO.getUnitNameById(unitId));
                int parentId = resultSet.getInt("parent_id");
                int discountTypeId = parentId == 0 ?
                        discountDAO.getProductDiscountForProductTypeId(product.getProductId(), purchaseOrder.getSupplierName()) :
                        discountDAO.getProductDiscountForProductTypeId(parentId, purchaseOrder.getSupplierName());
                product.setDiscountTypeId(discountTypeId);
                product.setReceivedQuantity(resultSet.getInt("received_quantity"));

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

    BranchDAO branchDAO = new BranchDAO();

    public boolean receivePurchaseOrderProduct(ProductsInTransact product, PurchaseOrder purchaseOrder, LocalDate receiptDate, String receiptNo) throws SQLException {
        String query = "INSERT INTO purchase_order_receiving " +
                "(purchase_order_id, product_id, received_quantity, unit_price, discounted_amount, vat_amount, withholding_amount, total_amount, branch_id, receipt_no, receipt_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "received_quantity = received_quantity + VALUES(received_quantity), " +
                "unit_price = VALUES(unit_price), " +
                "discounted_amount = VALUES(discounted_amount), " +
                "vat_amount = VALUES(vat_amount), " +
                "withholding_amount = VALUES(withholding_amount), " +
                "total_amount = VALUES(total_amount), " +
                "receipt_no = VALUES(receipt_no), " +
                "receipt_date = VALUES(receipt_date)";

        boolean success = false; // Initialize success flag

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            double unitPrice = product.getUnitPrice();
            double discountAmount = unitPrice - product.getDiscountedPrice(); // Subtract the discount from the unit price
            double totalAmount = discountAmount * product.getReceivedQuantity();

            preparedStatement.setInt(1, product.getPurchaseOrderId());
            preparedStatement.setInt(2, product.getProductId());
            preparedStatement.setInt(3, product.getReceivedQuantity());
            preparedStatement.setDouble(4, product.getUnitPrice());
            preparedStatement.setDouble(5, discountAmount);

            boolean receiptRequired = purchaseOrder.getReceiptRequired();
            if (receiptRequired) {
                double vatAmount = discountAmount * vatValue;
                double withholdingAmount = discountAmount * withholdingValue;

                preparedStatement.setDouble(6, vatAmount);
                preparedStatement.setDouble(7, withholdingAmount);

                // Update the total amount to include VAT and withholding tax
                totalAmount += vatAmount + withholdingAmount;
            } else {
                preparedStatement.setDouble(6, 0); // Default value for VAT amount
                preparedStatement.setDouble(7, 0); // Default value for withholding amount
            }

            preparedStatement.setDouble(8, totalAmount);
            preparedStatement.setInt(9, product.getBranchId());
            preparedStatement.setString(10, receiptNo);
            preparedStatement.setDate(11, java.sql.Date.valueOf(receiptDate)); // Convert LocalDate to java.sql.Date

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                boolean received = updateReceiveForProducts(product);
                if (received) {
                    success = true; // Mark success if everything went well
                }
            }
        } catch (SQLException e) {
            // Handle SQL Exception
            e.printStackTrace(); // You might want to handle this more gracefully
        }

        return success; // Return true if at least one row was affected
    }


    private boolean updateReceiveForProducts(ProductsInTransact product) throws SQLException {
        String query = "UPDATE purchase_order_products SET received = ? WHERE purchase_order_product_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setBoolean(1, true);
            preparedStatement.setInt(2, product.getPurchaseOrderProductId());
            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0;
        }
    }


}
