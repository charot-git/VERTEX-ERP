package com.vertex.vos.DAO;

import com.vertex.vos.Objects.Product;
import com.vertex.vos.Objects.SalesInvoiceDetail;
import com.vertex.vos.Utilities.DatabaseConnectionPool;
import com.vertex.vos.Utilities.ProductDAO;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductSelectionTempDAO {

    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();
    private final ProductDAO productDAO = new ProductDAO(); // Assume ProductDAO is already implemented

    /**
     * Retrieves a list of SalesInvoiceDetail objects for products in the specified branch with optional filters.
     *
     * @param branchId   The ID of the branch to fetch data for.
     * @param offset     The offset for pagination.
     * @param limit      The number of records to fetch.
     * @param brand      The brand filter (optional).
     * @param description The description filter (optional).
     * @param unit       The unit filter (optional).
     * @return List of SalesInvoiceDetail objects.
     * @throws SQLException if a database error occurs.
     */
    public List<SalesInvoiceDetail> getSalesInvoiceDetailsForBranch(
            int branchId, int offset, int limit, String brand, String description, String unit) throws SQLException {
        List<SalesInvoiceDetail> salesInvoiceDetails = new ArrayList<>();
        StringBuilder query = new StringBuilder("""
                SELECT 
                    inventory.product_id,
                    inventory.quantity,
                    inventory.last_updated
                FROM inventory
                JOIN products ON inventory.product_id = products.product_id
                WHERE inventory.branch_id = ?
                """);

        // Apply filters if provided
        if (brand != null && !brand.isEmpty()) {
            query.append(" AND products.product_brand LIKE ? ");
        }
        if (description != null && !description.isEmpty()) {
            query.append(" AND products.description LIKE ? ");
        }
        if (unit != null && !unit.isEmpty()) {
            query.append(" AND products.unit_of_measurement LIKE ? ");
        }

        query.append(" LIMIT ? OFFSET ?");

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query.toString())) {

            int paramIndex = 1;

            // Set the branch ID
            preparedStatement.setInt(paramIndex++, branchId);

            // Set the filter values if provided
            if (brand != null && !brand.isEmpty()) {
                preparedStatement.setString(paramIndex++, "%" + brand + "%");
            }
            if (description != null && !description.isEmpty()) {
                preparedStatement.setString(paramIndex++, "%" + description + "%");
            }
            if (unit != null && !unit.isEmpty()) {
                preparedStatement.setString(paramIndex++, "%" + unit + "%");
            }

            preparedStatement.setInt(paramIndex++, limit);
            preparedStatement.setInt(paramIndex, offset);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    int productId = resultSet.getInt("product_id");
                    int quantity = resultSet.getInt("quantity");

                    // Fetch Product details using ProductDAO
                    Product product = productDAO.getProductById(productId);

                    if (product != null) {
                        // Create and populate SalesInvoiceDetail object
                        SalesInvoiceDetail detail = new SalesInvoiceDetail();
                        detail.setProduct(product);
                        detail.setQuantity(quantity);
                        detail.setUnitPrice(product.getPricePerUnit());
                        detail.setTotalPrice(quantity * product.getPricePerUnit());
                        detail.setModifiedAt(resultSet.getTimestamp("last_updated"));

                        salesInvoiceDetails.add(detail);
                    }
                }
            }
        }

        return salesInvoiceDetails;
    }
}
