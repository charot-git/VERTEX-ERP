package com.vertex.vos.DAO;

import com.vertex.vos.Objects.Customer;
import com.vertex.vos.Objects.DiscountType;
import com.vertex.vos.Objects.Product;
import com.vertex.vos.Objects.SalesInvoiceDetail;
import com.vertex.vos.Utilities.DatabaseConnectionPool;
import com.vertex.vos.Utilities.ProductDAO;
import com.zaxxer.hikari.HikariDataSource;
import javafx.collections.ObservableList;

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
     * @param branchId         The ID of the branch to fetch data for.
     * @param offset           The offset for pagination.
     * @param limit            The number of records to fetch.
     * @param brand            The brand filter (optional).
     * @param description      The description filter (optional).
     * @param unit             The unit filter (optional).
     * @param selectedCustomer
     * @param priceType
     * @return List of SalesInvoiceDetail objects.
     * @throws SQLException if a database error occurs.
     */
    public List<SalesInvoiceDetail> getSalesInvoiceDetailsForBranch(
            int branchId, int offset, int limit, String brand, String description, String unit, Customer selectedCustomer, String priceType) throws SQLException {
        List<SalesInvoiceDetail> salesInvoiceDetails = new ArrayList<>();
        StringBuilder query = getStringBuilder(brand, description, unit);

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query.toString())) {

            int paramIndex = 1;

            // Set customer ID for filtering (if no customer, use 0 to avoid null issues)
            preparedStatement.setInt(paramIndex++, selectedCustomer != null ? selectedCustomer.getCustomerId() : 0);

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


                        // Check for customer-specific pricing and discounts
                        Double customerPrice = resultSet.getObject("customer_price", Double.class);
                        String discountTypeName = resultSet.getString("discount_type_name");
                        int discountTypeId = resultSet.getInt("discount_type_id");

                        DiscountType discountType = new DiscountType();
                        discountType.setId(discountTypeId);
                        discountType.setTypeName(discountTypeName);

                        if (customerPrice != null && customerPrice != 0.0) {
                            detail.setUnitPrice(customerPrice);
                        } else {
                            switch (priceType) {
                                case "A":
                                    detail.setUnitPrice(product.getPriceA());
                                    break;
                                case "B":
                                    detail.setUnitPrice(resultSet.getDouble("priceB"));
                                    break;
                                case "C":
                                    detail.setUnitPrice(resultSet.getDouble("priceC"));
                                    break;
                                case "D":
                                    detail.setUnitPrice(resultSet.getDouble("priceD"));
                                    break;
                                case "E":
                                    detail.setUnitPrice(resultSet.getDouble("priceE"));
                                    break;
                                default:
                                    detail.setUnitPrice(0.0);
                            }
                        }

                        product.setDiscountType(discountType);
                        detail.setDiscountType(discountType);
                        detail.setModifiedAt(resultSet.getTimestamp("last_updated"));
                        detail.setAvailableQuantity(quantity);
                        detail.setProduct(product);

                        salesInvoiceDetails.add(detail);
                    }
                }
            }
        }

        return salesInvoiceDetails;
    }

    private static StringBuilder getStringBuilder(String brand, String description, String unit) {
        System.out.println(brand + " " + description +  " " + unit);

        StringBuilder query = new StringBuilder("SELECT \n" +
                "    i.product_id,\n" +
                "    i.quantity,\n" +
                "    i.last_updated,\n" +
                "    COALESCE(ppc.unit_price, 0) AS customer_price,\n" +
                "    p.priceA,\n" +
                "    p.priceB,\n" +
                "    p.priceC,\n" +
                "    p.priceD,\n" +
                "    p.priceE,\n" +
                "    dt.id AS discount_type_id,\n" +
                "    dt.discount_type AS discount_type_name\n" +
                "FROM inventory i\n" +
                "JOIN products p ON i.product_id = p.product_id\n" +
                "LEFT JOIN product_per_customer ppc ON i.product_id = ppc.product_id\n" +
                "    AND ppc.customer_id = ?\n" +
                "LEFT JOIN discount_type dt ON ppc.discount_type = dt.id\n" +
                "WHERE i.branch_id = ? AND i.quantity > 0\n");

        // Apply filters if provided
        if (brand != null && !brand.isEmpty()) {
            query.append(" AND p.product_brand LIKE ? ");
        }
        if (description != null && !description.isEmpty()) {
            query.append(" AND p.description LIKE ? ");
        }
        if (unit != null && !unit.isEmpty()) {
            query.append(" AND p.unit_of_measurement LIKE ? ");
        }

        query.append(" LIMIT ? OFFSET ?");
        return query;
    }


    public List<Product> getFilteredParentProducts(String brand, String category, String name, int limit, int offset, Customer selectedCustomer, ObservableList<Product> existingProducts) {
        List<Product> products = new ArrayList<>();

        // Create a comma-separated list of existing product IDs
        StringBuilder idPlaceholders = new StringBuilder();
        for (int i = 0; i < existingProducts.size(); i++) {
            idPlaceholders.append("?,");
        }
        if (idPlaceholders.length() > 0) {
            idPlaceholders.setLength(idPlaceholders.length() - 1); // Remove the trailing comma
        }

        String query = """
                SELECT p.*, b.brand_name, c.category_name
                FROM products p
                LEFT JOIN brand b ON p.product_brand = b.brand_id
                LEFT JOIN categories c ON p.product_category = c.category_id
                WHERE p.isActive = 1
                AND (p.parent_id IS NULL OR p.parent_id = 0)
                AND (b.brand_name LIKE ? OR ? IS NULL)
                AND (c.category_name LIKE ? OR ? IS NULL)
                AND (p.product_name LIKE ? OR ? IS NULL)
                """ + (idPlaceholders.length() > 0 ? " AND p.product_id NOT IN (" + idPlaceholders + ") " : "") + """
                LIMIT ? OFFSET ?;
                """;

        try (PreparedStatement ps = dataSource.getConnection().prepareStatement(query)) {
            int parameterIndex = 1;

            // Set filter parameters
            ps.setString(parameterIndex++, brand == null || brand.isEmpty() ? null : "%" + brand + "%");
            ps.setString(parameterIndex++, brand == null || brand.isEmpty() ? null : brand);
            ps.setString(parameterIndex++, category == null || category.isEmpty() ? null : "%" + category + "%");
            ps.setString(parameterIndex++, category == null || category.isEmpty() ? null : category);
            ps.setString(parameterIndex++, name == null || name.isEmpty() ? null : "%" + name + "%");
            ps.setString(parameterIndex++, name == null || name.isEmpty() ? null : name);

            // Set existing product IDs for the NOT IN clause
            for (Product existingProduct : existingProducts) {
                ps.setInt(parameterIndex++, existingProduct.getProductId());
            }

            // Set limit and offset
            ps.setInt(parameterIndex++, limit);
            ps.setInt(parameterIndex, offset);

            // Execute the query
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Product product = mapRowToProduct(rs);

                // Set the price based on the customer's price type
                String priceType = selectedCustomer.getPriceType(); // Assuming getPriceType() returns "A", "B", etc.
                switch (priceType) {
                    case "A":
                        product.setPricePerUnit(rs.getDouble("priceA"));
                        break;
                    case "B":
                        product.setPricePerUnit(rs.getDouble("priceB"));
                        break;
                    case "C":
                        product.setPricePerUnit(rs.getDouble("priceC"));
                        break;
                    case "D":
                        product.setPricePerUnit(rs.getDouble("priceD"));
                        break;
                    case "E":
                        product.setPricePerUnit(rs.getDouble("priceE"));
                        break;
                    default:
                        product.setPricePerUnit(rs.getDouble("price_per_unit"));
                        break;
                }
                products.add(product);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }


    private Product mapRowToProduct(ResultSet rs) throws SQLException {
        Product product = new Product();
        product.setProductId(rs.getInt("product_id"));
        product.setProductName(rs.getString("product_name"));
        product.setProductCode(rs.getString("product_code"));
        product.setProductBrandString(rs.getString("brand_name"));
        product.setProductCategoryString(rs.getString("category_name"));
        product.setPricePerUnit(rs.getDouble("price_per_unit"));
        return product;
    }

    private Product mapRowToProductChildren(ResultSet rs, Product selectedProduct) throws SQLException {
        Product product = new Product();
        product.setProductId(rs.getInt("product_id"));
        product.setProductName(rs.getString("product_name"));
        product.setProductCode(rs.getString("product_code"));
        return product;
    }


    public List<Product> getProductChildren(Product product, Customer selectedCustomer) {
        List<Product> children = new ArrayList<>();
        String baseQuery = """
                    SELECT product_id, product_name, product_code, 
                           priceA, priceB, priceC, priceD, priceE, price_per_unit
                    FROM products
                    WHERE parent_id = ?
                    AND isActive = 1
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(baseQuery)) {

            ps.setInt(1, product.getProductId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Product childProduct = mapRowToProductChildren(rs, product);
                    double price = getPriceBasedOnCustomerType(rs, selectedCustomer.getPriceType());
                    childProduct.setPricePerUnit(price);
                    children.add(childProduct);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return children;
    }

    private double getPriceBasedOnCustomerType(ResultSet rs, String priceType) throws SQLException {
        return switch (priceType) {
            case "A" -> rs.getDouble("priceA");
            case "B" -> rs.getDouble("priceB");
            case "C" -> rs.getDouble("priceC");
            case "D" -> rs.getDouble("priceD");
            case "E" -> rs.getDouble("priceE");
            default -> rs.getDouble("price_per_unit");
        };
    }


}
