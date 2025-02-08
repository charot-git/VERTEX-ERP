package com.vertex.vos.Utilities;

import com.vertex.vos.Objects.Product;
import com.zaxxer.hikari.HikariDataSource;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

import java.sql.*;
import java.sql.Date;
import java.util.*;
import java.util.stream.Collectors;

public class ProductDAO {
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();
    BrandDAO brandDAO = new BrandDAO();
    ProductClassDAO productClassDAO = new ProductClassDAO();
    CategoriesDAO categoriesDAO = new CategoriesDAO();
    SegmentDAO segmentDAO = new SegmentDAO();
    SectionsDAO sectionsDAO = new SectionsDAO();


    public Product getProductDetails(int productId) {
        String sqlQuery = "SELECT * FROM products WHERE product_id = ?";
        return getProduct(productId, sqlQuery);
    }

    public Task<ObservableList<Product>> getAllProductsTask() {
        return new Task<ObservableList<Product>>() {
            @Override
            protected ObservableList<Product> call() {
                ObservableList<Product> products = FXCollections.observableArrayList();
                String sqlQuery = "SELECT * FROM products ORDER BY description";

                try (Connection conn = dataSource.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(sqlQuery);
                     ResultSet rs = stmt.executeQuery()) {

                    while (rs.next()) {
                        Product product = extractProductFromResultSet(rs);
                        products.add(product);
                    }
                } catch (SQLException e) {
                    e.printStackTrace(); // Handle the exception properly in your application
                }
                return products;
            }
        };
    }

    private final int batchSize = 50; // number of products to fetch in each batch
    private int offset = 0; // current offset for pagination

    public Task<ObservableList<Product>> getMoreParentProductsTask() {
        return new Task<ObservableList<Product>>() {
            @Override
            protected ObservableList<Product> call() {
                ObservableList<Product> parentProducts = FXCollections.observableArrayList();
                String sqlQuery = "SELECT * FROM products WHERE parent_id = 0 LIMIT ? OFFSET ?";

                try (Connection conn = dataSource.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(sqlQuery)) {

                    stmt.setInt(1, batchSize);
                    stmt.setInt(2, offset);

                    try (ResultSet rs = stmt.executeQuery()) {

                        while (rs.next()) {
                            Product product = extractProductFromResultSet(rs);
                            parentProducts.add(product);
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace(); // Handle the exception properly in your application
                }

                offset += batchSize; // increment offset for next batch
                return parentProducts;
            }
        };
    }

    public List<Product> getAllProductConfigs(int productId) {
        List<Product> productConfigurations = new ArrayList<>();
        String sqlQuery = "SELECT * FROM products WHERE parent_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlQuery)) {

            stmt.setInt(1, productId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Product product = extractProductFromResultSet(rs);
                productConfigurations.add(product);
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception properly in your application
        }

        return productConfigurations;
    }

    private Product extractProductFromResultSet(ResultSet resultSet) throws SQLException {
        Product product = new Product();
        product.setProductId(resultSet.getInt("product_id"));
        product.setProductName(resultSet.getString("product_name"));
        product.setIsActive(resultSet.getInt("isActive"));
        product.setParentId(resultSet.getInt("parent_id"));
        product.setBarcode(resultSet.getString("barcode"));
        product.setProductCode(resultSet.getString("product_code"));
        product.setProductImage(resultSet.getString("product_image"));
        product.setDescription(resultSet.getString("description"));
        product.setShortDescription(resultSet.getString("short_description"));
        product.setDateAdded(resultSet.getDate("date_added"));
        product.setLastUpdated(resultSet.getTimestamp("last_updated"));
        product.setProductBrandString(brandDAO.getBrandNameById(resultSet.getInt("product_brand")));
        product.setProductCategoryString(categoriesDAO.getCategoryNameById(resultSet.getInt("product_category")));
        product.setProductClassString(productClassDAO.getProductClassNameById(resultSet.getInt("product_class")));
        product.setProductSegmentString(segmentDAO.getSegmentNameById(resultSet.getInt("product_segment")));
        product.setProductSectionString(sectionsDAO.getSectionNameById(resultSet.getInt("product_section")));
        product.setProductShelfLife(resultSet.getInt("product_shelf_life"));
        product.setProductWeight(resultSet.getDouble("product_weight"));
        product.setMaintainingQuantity(resultSet.getInt("maintaining_quantity"));
        product.setUnitOfMeasurement(resultSet.getInt("unit_of_measurement"));
        product.setUnitOfMeasurementCount(resultSet.getInt("unit_of_measurement_count"));
        product.setEstimatedUnitCost(resultSet.getDouble("estimated_unit_cost"));
        product.setEstimatedExtendedCost(resultSet.getDouble("estimated_extended_cost"));
        product.setPricePerUnit(resultSet.getDouble("price_per_unit"));
        product.setCostPerUnit(resultSet.getDouble("cost_per_unit"));
        product.setPriceA(resultSet.getDouble("priceA"));
        product.setPriceB(resultSet.getDouble("priceB"));
        product.setPriceC(resultSet.getDouble("priceC"));
        product.setPriceD(resultSet.getDouble("priceD"));
        product.setPriceE(resultSet.getDouble("priceE"));
        UnitDAO unitDAO = new UnitDAO();
        String unitOfMeasurementString = unitDAO.getUnitNameById(product.getUnitOfMeasurement());
        product.setUnitOfMeasurementString(unitOfMeasurementString);

        return product;
    }

    private Product getProduct(int productId, String sqlQuery) {
        UnitDAO unitDAO = new UnitDAO();
        BrandDAO brandDAO = new BrandDAO();
        CategoriesDAO categoriesDAO = new CategoriesDAO();
        ProductClassDAO classDAO = new ProductClassDAO();
        SegmentDAO segmentDAO = new SegmentDAO();
        SectionsDAO sectionsDAO = new SectionsDAO();
        String unitOfMeasurementString;
        Product config = null;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {

            preparedStatement.setInt(1, productId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    unitOfMeasurementString = unitDAO.getUnitNameById(resultSet.getInt("unit_of_measurement"));
                    config = new Product();
                    config.setProductName(resultSet.getString("product_name"));
                    config.setProductId(resultSet.getInt("product_id"));
                    config.setIsActive(resultSet.getInt("isActive"));
                    config.setParentId(resultSet.getInt("parent_id"));
                    config.setBarcode(resultSet.getString("barcode"));
                    config.setProductCode(resultSet.getString("product_code"));
                    config.setProductImage(resultSet.getString("product_image"));
                    config.setDescription(resultSet.getString("description"));
                    config.setShortDescription(resultSet.getString("short_description"));
                    config.setDateAdded(resultSet.getDate("date_added"));
                    config.setLastUpdated(resultSet.getTimestamp("last_updated"));
                    config.setProductBrand(resultSet.getInt("product_brand"));
                    config.setProductCategory(resultSet.getInt("product_category"));
                    config.setProductClass(resultSet.getInt("product_class"));
                    config.setProductSegment(resultSet.getInt("product_segment"));
                    config.setProductSection(resultSet.getInt("product_section"));
                    config.setProductShelfLife(resultSet.getInt("product_shelf_life"));
                    config.setProductWeight(resultSet.getDouble("product_weight"));
                    config.setMaintainingQuantity(resultSet.getInt("maintaining_quantity"));
                    config.setUnitOfMeasurement(resultSet.getInt("unit_of_measurement"));
                    config.setUnitOfMeasurementCount(resultSet.getInt("unit_of_measurement_count"));
                    config.setEstimatedUnitCost(resultSet.getDouble("estimated_unit_cost"));
                    config.setEstimatedExtendedCost(resultSet.getDouble("estimated_extended_cost"));
                    config.setPricePerUnit(resultSet.getDouble("price_per_unit"));
                    config.setCostPerUnit(resultSet.getDouble("cost_per_unit"));
                    config.setPriceA(resultSet.getDouble("priceA"));
                    config.setPriceB(resultSet.getDouble("priceB"));
                    config.setPriceC(resultSet.getDouble("priceC"));
                    config.setPriceD(resultSet.getDouble("priceD"));
                    config.setPriceE(resultSet.getDouble("priceE"));
                    config.setUnitOfMeasurementString(unitOfMeasurementString);
                    config.setProductBrandString(brandDAO.getBrandNameById(resultSet.getInt("product_brand")));
                    config.setProductCategoryString(categoriesDAO.getCategoryNameById(resultSet.getInt("product_category")));
                    config.setProductClassString(classDAO.getProductClassNameById(resultSet.getInt("product_class")));
                    config.setProductSegmentString(segmentDAO.getSegmentNameById(resultSet.getInt("product_segment")));
                    config.setProductSectionString(sectionsDAO.getSectionNameById(resultSet.getInt("product_section")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return config;
    }

    public String getProductDescriptionByBarcode(String barcode) {
        String sqlQuery = "SELECT description FROM products WHERE barcode = ?";
        String description = null;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {

            preparedStatement.setString(1, barcode);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    description = resultSet.getString("description");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle any SQL exceptions here
        }
        return description;
    }

    public boolean doesBarcodeExist(String barcode) {
        String sqlQuery = "SELECT COUNT(*) AS count FROM products WHERE barcode = ?";
        int count = 0;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {

            preparedStatement.setString(1, barcode);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    count = resultSet.getInt("count");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle any SQL exceptions here
        }
        return count > 0;
    }


    public int updateProduct(Product product) {
        // SQL statements
        String updateProductSql = "UPDATE products SET product_name = ?, " +
                "barcode = ?, product_code = ?, description = ?, " +
                "short_description = ?, last_updated = ?, product_brand = ?, " +
                "product_category = ?, product_class = ?, product_segment = ?, " +
                "product_section = ?, product_shelf_life = ?, " +
                "product_weight = ?, maintaining_quantity = ?, unit_of_measurement = ?, unit_of_measurement_count = ?, isActive = ? " +
                "WHERE product_id = ?";

        String updateChildrenSql = "UPDATE products SET product_name = ?, " +
                "product_brand = ?, product_category = ?, product_class = ?, product_segment = ?, product_section = ? " +
                "WHERE parent_id = ?";

        String updateParentAndChildrenSql = "UPDATE products SET product_name = ?, " +
                "product_brand = ?, product_category = ?, product_class = ?, product_segment = ?, product_section = ? " +
                "WHERE product_id = ? OR parent_id = ?";

        // Initialize the rows affected count
        int rowsAffected = 0;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement updateProductStmt = connection.prepareStatement(updateProductSql)) {

            // Update the product itself
            updateProductStmt.setString(1, product.getProductName());
            updateProductStmt.setString(2, product.getBarcode());
            updateProductStmt.setString(3, product.getProductCode());
            updateProductStmt.setString(4, product.getDescription());
            updateProductStmt.setString(5, product.getShortDescription());
            updateProductStmt.setTimestamp(6, product.getLastUpdated());
            updateProductStmt.setInt(7, product.getProductBrand());
            updateProductStmt.setInt(8, product.getProductCategory());
            updateProductStmt.setInt(9, product.getProductClass());
            updateProductStmt.setInt(10, product.getProductSegment());
            updateProductStmt.setInt(11, product.getProductSection());
            updateProductStmt.setInt(12, product.getProductShelfLife());
            updateProductStmt.setDouble(13, product.getProductWeight());
            updateProductStmt.setInt(14, product.getMaintainingQuantity());
            updateProductStmt.setInt(15, product.getUnitOfMeasurement());
            updateProductStmt.setInt(16, product.getUnitOfMeasurementCount());
            updateProductStmt.setInt(17, product.getIsActive());
            updateProductStmt.setInt(18, product.getProductId());

            rowsAffected += updateProductStmt.executeUpdate(); // Update the product itself

            // Determine if the product is a parent or child
            if (product.getParentId() == 0) {
                // This is a parent product, update all children
                try (PreparedStatement updateChildrenStmt = connection.prepareStatement(updateChildrenSql)) {
                    updateChildrenStmt.setString(1, product.getProductName()); // Update name for children
                    updateChildrenStmt.setInt(2, product.getProductBrand());
                    updateChildrenStmt.setInt(3, product.getProductCategory());
                    updateChildrenStmt.setInt(4, product.getProductClass());
                    updateChildrenStmt.setInt(5, product.getProductSegment());
                    updateChildrenStmt.setInt(6, product.getProductSection());
                    updateChildrenStmt.setInt(7, product.getProductId());

                    rowsAffected += updateChildrenStmt.executeUpdate(); // Update children
                }
            } else {
                // This is a child product, find the parent
                String parentProductSql = "SELECT parent_id FROM products WHERE product_id = ?";
                try (PreparedStatement parentStmt = connection.prepareStatement(parentProductSql)) {
                    parentStmt.setInt(1, product.getProductId());
                    ResultSet rs = parentStmt.executeQuery();
                    if (rs.next()) {
                        int parentProductId = rs.getInt("parent_id");

                        // Update parent product and all its children
                        try (PreparedStatement updateParentAndChildrenStmt = connection.prepareStatement(updateParentAndChildrenSql)) {
                            updateParentAndChildrenStmt.setString(1, product.getProductName()); // Update name for parent and children
                            updateParentAndChildrenStmt.setInt(2, product.getProductBrand());
                            updateParentAndChildrenStmt.setInt(3, product.getProductCategory());
                            updateParentAndChildrenStmt.setInt(4, product.getProductClass());
                            updateParentAndChildrenStmt.setInt(5, product.getProductSegment());
                            updateParentAndChildrenStmt.setInt(6, product.getProductSection());
                            updateParentAndChildrenStmt.setInt(7, parentProductId);
                            updateParentAndChildrenStmt.setInt(8, parentProductId);

                            rowsAffected += updateParentAndChildrenStmt.executeUpdate(); // Update parent and children
                        }
                    }
                }
            }

            return rowsAffected;

        } catch (SQLException e) {
            // Handle the exception gracefully
            System.err.println("Error updating product: " + e.getMessage());
            return -1; // Indicates failure due to exception
        }
    }


    public int addInitialProduct(String barcode, String description, int unitOfMeasurement, int brandId, int parentId, int unitOfMeasurementCount) {
        String sql = "INSERT INTO products (barcode, description, unit_of_measurement, unit_of_measurement_count, product_brand, parent_id, date_added, isActive) VALUES (?, ?, ?, ?, ?, ?, CURRENT_DATE, 1)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setString(1, barcode);
            preparedStatement.setString(2, description);
            preparedStatement.setInt(3, unitOfMeasurement);
            preparedStatement.setInt(4, unitOfMeasurementCount);
            preparedStatement.setInt(5, brandId);
            preparedStatement.setInt(6, parentId);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1); // This retrieves the generated ID
                }
            }
            return -1; // Indicates failure to retrieve the ID
        } catch (SQLException e) {
            // Handle any SQL errors
            e.printStackTrace();
            return -1; // Indicates failure due to exception
        }
    }

    private String getProductDescription(int productId) throws SQLException {
        String sql = "SELECT description FROM products WHERE product_id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setInt(1, productId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("description");
                }
            }
        }

        return null;
    }

    public int addProduct(Product product) {
        String sql = "INSERT INTO products (isActive, parent_id, product_name, barcode, product_code, product_image, description, short_description, date_added, last_updated, product_brand, product_category, product_class, product_segment, product_section, product_shelf_life, product_weight, maintaining_quantity, unit_of_measurement, unit_of_measurement_count, estimated_unit_cost, estimated_extended_cost, price_per_unit, cost_per_unit, priceA, priceB, priceC, priceD, priceE) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            int parameterIndex = 1;
            preparedStatement.setInt(parameterIndex++, product.getIsActive());
            preparedStatement.setInt(parameterIndex++, product.getParentId());
            preparedStatement.setString(parameterIndex++, product.getProductName());
            preparedStatement.setString(parameterIndex++, product.getBarcode());
            preparedStatement.setString(parameterIndex++, product.getProductCode());
            preparedStatement.setString(parameterIndex++, product.getProductImage());
            preparedStatement.setString(parameterIndex++, product.getDescription());
            preparedStatement.setString(parameterIndex++, product.getShortDescription());
            preparedStatement.setDate(parameterIndex++, new java.sql.Date(product.getDateAdded().getTime()));
            preparedStatement.setTimestamp(parameterIndex++, product.getLastUpdated());
            preparedStatement.setInt(parameterIndex++, product.getProductBrand());
            preparedStatement.setInt(parameterIndex++, product.getProductCategory());
            preparedStatement.setInt(parameterIndex++, product.getProductClass());
            preparedStatement.setInt(parameterIndex++, product.getProductSegment());
            preparedStatement.setInt(parameterIndex++, product.getProductSection());
            preparedStatement.setInt(parameterIndex++, product.getProductShelfLife());
            preparedStatement.setDouble(parameterIndex++, product.getProductWeight());
            preparedStatement.setInt(parameterIndex++, product.getMaintainingQuantity());
            preparedStatement.setInt(parameterIndex++, product.getUnitOfMeasurement());
            preparedStatement.setInt(parameterIndex++, product.getUnitOfMeasurementCount());
            preparedStatement.setDouble(parameterIndex++, product.getEstimatedUnitCost());
            preparedStatement.setDouble(parameterIndex++, product.getEstimatedExtendedCost());
            preparedStatement.setDouble(parameterIndex++, product.getPricePerUnit());
            preparedStatement.setDouble(parameterIndex++, product.getCostPerUnit());
            preparedStatement.setDouble(parameterIndex++, product.getPriceA());
            preparedStatement.setDouble(parameterIndex++, product.getPriceB());
            preparedStatement.setDouble(parameterIndex++, product.getPriceC());
            preparedStatement.setDouble(parameterIndex++, product.getPriceD());
            preparedStatement.setDouble(parameterIndex++, product.getPriceE());

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1); // This retrieves the generated ID
                }
            }
            return -1; // Indicates failure to retrieve the ID
        } catch (SQLException e) {
            DialogUtils.showErrorMessage("Error", e.getMessage());
            return -1; // Indicates failure due to exception
        }
    }

    public int updateProductPrices(int productId, double estimatedUnitCost, double estimatedExtendedCost,
                                   double pricePerUnit, double costPerUnit, double priceA,
                                   double priceB, double priceC, double priceD, double priceE) {
        String sql = "UPDATE products SET estimated_unit_cost = ?, estimated_extended_cost = ?, " +
                "price_per_unit = ?, cost_per_unit = ?, priceA = ?, priceB = ?, priceC = ?, " +
                "priceD = ?, priceE = ? WHERE product_id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setDouble(1, estimatedUnitCost);
            preparedStatement.setDouble(2, estimatedExtendedCost);
            preparedStatement.setDouble(3, pricePerUnit);
            preparedStatement.setDouble(4, costPerUnit);
            preparedStatement.setDouble(5, priceA);
            preparedStatement.setDouble(6, priceB);
            preparedStatement.setDouble(7, priceC);
            preparedStatement.setDouble(8, priceD);
            preparedStatement.setDouble(9, priceE);
            preparedStatement.setInt(10, productId);

            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected; // Returns the number of rows affected by the update
        } catch (SQLException e) {
            e.printStackTrace();
            return -1; // Indicates failure due to exception
        }
    }


    public Product getProductById(int productId) {
        String sqlQuery = "SELECT * FROM products WHERE product_id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {

            preparedStatement.setInt(1, productId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next() ? extractProductFromResultSet(resultSet) : null;
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving product with ID " + productId + ": " + e.getMessage());
            return null;
        }
    }

    public Product getProductByName(String productName) {
        if (productName == null || productName.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be null or empty");
        }

        String sql = "SELECT * FROM products WHERE product_name = ? AND parent_id = 0";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, productName);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractProductFromResultSet(rs); // Return the product if found
                } else {
                    return null; // No product found
                }
            }
        } catch (SQLException e) {
            // Log the exception (consider using a logging framework)
            System.err.println("SQL error while fetching product by name: " + e.getMessage());
            return null; // Return null in case of an error
        }
    }

// Clean up:
// - Standardized variable names
// - Removed debugging statements
// - Improved readability
// - Removed redundant comments


    public int getProductIdByBarcode(String barcode) {
        int productId = -1; // Initialize to -1 if not found
        String sqlQuery = "SELECT product_id FROM products WHERE barcode = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {

            preparedStatement.setString(1, barcode);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    productId = resultSet.getInt("product_id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle any SQL exceptions here
        }
        return productId;
    }


    public String getProductNameByBarcode(String barcode) {
        String productName = null;
        String sqlQuery = "SELECT product_name FROM products WHERE barcode = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {

            preparedStatement.setString(1, barcode);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    productName = resultSet.getString("product_name");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle any SQL exceptions here
        }
        return productName;
    }

    public Product getProductByDescription(String description) {
        String sqlQuery = "SELECT * FROM products WHERE description = ?";
        Product product = null;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {

            preparedStatement.setString(1, description);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    product = extractProductFromResultSet(resultSet);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle any SQL exceptions here
        }

        return product;
    }


    public int getProductIdByName(String productName) {
        String sqlQuery = "SELECT product_id FROM products WHERE product_name = ?";
        int supplierId = -1;

        try (Connection connection = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {

            preparedStatement.setString(1, productName);
            preparedStatement.setString(1, productName);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    supplierId = resultSet.getInt("product_id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle any SQL exceptions here
        }
        return supplierId;
    }

    public String getProductDescriptionById(int productId) {
        String sqlQuery = "SELECT description FROM products WHERE product_id = ?";
        String description = null;

        try (Connection connection = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {

            preparedStatement.setInt(1, productId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    description = resultSet.getString("description");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle any SQL exceptions here
        }
        return description;
    }

    //get product brand by id
    public int getProductBrandById(int productId) {
        String sqlQuery = "SELECT product_brand FROM products WHERE product_id = ?";
        int brandId = -1; // Default value in case the brand is not found

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
            preparedStatement.setInt(1, productId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    brandId = resultSet.getInt("product_brand");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Consider using a logger for better error handling
        }

        return brandId;
    }


    public ObservableList<String> getProductDescriptionsByBrand(int brand) {
        String sqlQuery = "SELECT description FROM products WHERE product_brand = ?";
        ObservableList<String> descriptions = FXCollections.observableArrayList();

        try (Connection connection = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {

            preparedStatement.setInt(1, brand);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String description = resultSet.getString("description");
                    descriptions.add(description);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle any SQL exceptions here
        }
        return descriptions;
    }

    public int getProductIdByDescription(String description) {
        String sqlQuery = "SELECT product_id FROM products WHERE description = ?";
        int productId = -1;

        try (Connection connection = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {

            preparedStatement.setString(1, description);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    productId = resultSet.getInt("product_id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle any SQL exceptions here
        }
        return productId;
    }

    public Task<ObservableList<Product>> searchParentProductsTask(String searchQuery, int batchSize, int offset) {
        return new Task<ObservableList<Product>>() {
            @Override
            protected ObservableList<Product> call() {
                ObservableList<Product> products = FXCollections.observableArrayList();
                String sqlQuery = "SELECT * FROM products WHERE description LIKE ? AND parent_id =0 OR parent_id IS NULL LIMIT ? OFFSET ?";

                try (Connection conn = dataSource.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(sqlQuery)) {

                    stmt.setString(1, "%" + searchQuery + "%");
                    stmt.setInt(2, batchSize);
                    stmt.setInt(3, offset);

                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            Product product = extractProductFromResultSet(rs);
                            products.add(product);
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace(); // Handle the exception properly in your application
                }

                return products;
            }
        };
    }


    public String getNextBarcodeNumber() {
        String nextBarcode = null;
        boolean isUnique = false;
        String updateQuery = "UPDATE product_barcode SET bar_no = LAST_INSERT_ID(bar_no + 1)";
        String selectQuery = "SELECT LAST_INSERT_ID()";

        try (Connection connection = dataSource.getConnection()) {

            while (!isUnique) {
                int nextNumber = 0;

                try (PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {
                    updateStatement.executeUpdate();
                    try (PreparedStatement selectStatement = connection.prepareStatement(selectQuery);
                         ResultSet resultSet = selectStatement.executeQuery()) {
                        if (resultSet.next()) {
                            nextNumber = resultSet.getInt(1);
                        }
                    }
                }
                nextBarcode = String.format("%06d", nextNumber);
                if (isBarcodeUnique(connection, nextBarcode)) {
                    isUnique = true;
                }
            }
        } catch (SQLException e) {
            // Log the exception or handle it according to your application's needs
            System.err.println("Error while generating next barcode number: " + e.getMessage());
            e.printStackTrace();
        }

        return nextBarcode;
    }

    private boolean isBarcodeUnique(Connection connection, String barcode) {
        String checkQuery = "SELECT COUNT(*) FROM products WHERE barcode = ?";

        try (PreparedStatement checkStatement = connection.prepareStatement(checkQuery)) {
            checkStatement.setString(1, barcode);

            try (ResultSet resultSet = checkStatement.executeQuery()) {
                if (resultSet.next()) {
                    int count = resultSet.getInt(1);
                    return count == 0;
                }
            }
        } catch (SQLException e) {
            // Log the exception or handle it according to your application's needs
            System.err.println("Error while checking barcode uniqueness: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }


    public int getParentIdByProductId(int productId) {
        String query = "SELECT parent_id FROM products WHERE product_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, productId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("parent_id");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving parent ID by product ID", e);
        }
        return -1;
    }


    public Task<ObservableList<Product>> getProductsByParentIdTask(int parentId) {
        return new Task<ObservableList<Product>>() {
            @Override
            protected ObservableList<Product> call() {
                ObservableList<Product> products = FXCollections.observableArrayList();
                String query = "SELECT * FROM products WHERE parent_id = ? OR product_id = ?";
                try (Connection connection = dataSource.getConnection();
                     PreparedStatement statement = connection.prepareStatement(query)) {
                    statement.setInt(1, parentId);
                    statement.setInt(2, parentId);
                    try (ResultSet resultSet = statement.executeQuery()) {
                        while (resultSet.next()) {
                            Product product = extractProductFromResultSet(resultSet);
                            products.add(product);
                        }
                    }
                } catch (SQLException e) {
                    throw new RuntimeException("Error retrieving products by parent ID", e);
                }
                return products;
            }
        };
    }

    public ObservableList<Product> getProductsByIds(List<Integer> productIds) {
        ObservableList<Product> products = FXCollections.observableArrayList();

        if (productIds == null || productIds.isEmpty()) {
            return products; // Return empty list if no product IDs are provided
        }

        // Build the query with dynamic placeholders
        String placeholders = productIds.stream().map(id -> "?").collect(Collectors.joining(","));
        String query = "SELECT * FROM products WHERE product_id IN (" + placeholders + ")";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            // Set each product ID as a parameter in the prepared statement
            for (int i = 0; i < productIds.size(); i++) {
                statement.setInt(i + 1, productIds.get(i));
            }

            // Execute the query and extract products from the result set
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Product product = extractProductFromResultSet(resultSet);
                    products.add(product);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving products by IDs", e);
        }

        return products;
    }

    public List<String> getAllProductNames() {
        Set<String> productNames = new HashSet<>();
        String sqlQuery = "SELECT product_name FROM products";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String productName = resultSet.getString("product_name");
                    productNames.add(productName);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle any SQL exceptions here
        }
        return new ArrayList<>(productNames);
    }

    public List<String> getProductNamesWithInventory(int branchId) {
        List<String> products = new ArrayList<>();
        String query = """
                    SELECT DISTINCT p.product_name
                    FROM inventory i
                    JOIN products p ON i.product_id = p.product_id
                    WHERE i.branch_id = ? AND i.quantity > 0
                """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, branchId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs != null && rs.next()) {
                    String productName = rs.getString("product_name");
                    if (productName != null) {
                        products.add(productName);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Consider using a logging framework
        }
        return products;
    }

    public ObservableList<String> getProductUnitsWithInventory(int branchId, String productName) {
        ObservableList<String> units = FXCollections.observableArrayList();
        String query = """
                SELECT DISTINCT u.unit_name
                FROM inventory i
                JOIN products p ON i.product_id = p.product_id
                JOIN units u ON p.unit_of_measurement = u.unit_id
                WHERE i.branch_id = ? 
                  AND i.quantity > 0 
                  AND p.product_name = ?
                """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, branchId);
            stmt.setString(2, productName);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    units.add(rs.getString("unit_name"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Consider using a logging framework in production
        }

        return units;
    }

    public Product getProductByNameAndUnit(String productName, String unitName) {
        String query = """
                SELECT p.*
                FROM products p
                JOIN units u ON p.unit_of_measurement = u.unit_id
                WHERE p.product_name = ? AND u.unit_name = ?
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, productName);
            statement.setString(2, unitName);

            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? extractProductFromResultSet(result) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
