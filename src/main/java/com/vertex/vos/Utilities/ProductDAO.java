package com.vertex.vos.Utilities;

import com.vertex.vos.Constructors.Product;
import com.vertex.vos.Constructors.ProductSEO;
import com.zaxxer.hikari.HikariDataSource;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();
    BrandDAO brandDAO = new BrandDAO();
    ProductClassDAO productClassDAO = new ProductClassDAO();
    CategoriesDAO categoriesDAO = new CategoriesDAO();
    SegmentDAO segmentDAO = new SegmentDAO();
    NatureDAO natureDAO = new NatureDAO();
    SectionsDAO sectionsDAO = new SectionsDAO();


    public Product getProductDetails(int productId) {
        String sqlQuery = "SELECT * FROM products WHERE product_id = ?";
        return getProduct(productId, sqlQuery);
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

    private Product extractProductFromResultSet(ResultSet rs) throws SQLException {
        Product product = new Product();
        product.setProductId(rs.getInt("product_id"));
        product.setIsActive(rs.getInt("isActive"));
        product.setParentId(rs.getInt("parent_id"));
        product.setBarcode(rs.getString("barcode"));
        product.setProductCode(rs.getString("product_code"));
        product.setProductImage(rs.getString("product_image"));
        product.setDescription(rs.getString("description"));
        product.setShortDescription(rs.getString("short_description"));
        product.setDateAdded(rs.getDate("date_added"));
        product.setLastUpdated(rs.getTimestamp("last_updated"));
        product.setProductBrand(rs.getInt("product_brand"));
        product.setProductCategory(rs.getInt("product_category"));
        product.setProductClass(rs.getInt("product_class"));
        product.setProductSegment(rs.getInt("product_segment"));
        product.setProductNature(rs.getInt("product_nature"));
        product.setProductSection(rs.getInt("product_section"));
        product.setProductShelfLife(rs.getInt("product_shelf_life"));
        product.setProductWeight(rs.getDouble("product_weight"));
        product.setMaintainingQuantity(rs.getInt("maintaining_quantity"));
        product.setQuantity(rs.getDouble("quantity"));
        product.setUnitOfMeasurement(rs.getInt("unit_of_measurement"));
        product.setUnitOfMeasurementCount(rs.getInt("unit_of_measurement_count"));
        product.setEstimatedUnitCost(rs.getDouble("estimated_unit_cost"));
        product.setEstimatedExtendedCost(rs.getDouble("estimated_extended_cost"));
        product.setPricePerUnit(rs.getDouble("price_per_unit"));
        product.setCostPerUnit(rs.getDouble("cost_per_unit"));
        product.setPriceA(rs.getDouble("priceA"));
        product.setPriceB(rs.getDouble("priceB"));
        product.setPriceC(rs.getDouble("priceC"));
        product.setPriceD(rs.getDouble("priceD"));
        product.setPriceE(rs.getDouble("priceE"));
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
        NatureDAO natureDAO = new NatureDAO();
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
                    config.setProductNature(resultSet.getInt("product_nature"));
                    config.setProductSection(resultSet.getInt("product_section"));
                    config.setProductShelfLife(resultSet.getInt("product_shelf_life"));
                    config.setProductWeight(resultSet.getDouble("product_weight"));
                    config.setMaintainingQuantity(resultSet.getInt("maintaining_quantity"));
                    config.setQuantity(resultSet.getDouble("quantity"));
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
                    config.setProductNatureString(natureDAO.getNatureNameById(resultSet.getInt("product_nature")));
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
        String sql = "UPDATE products SET product_name = ?, " +
                "barcode = ?, product_code = ?, product_image = ?, description = ?, " +
                "short_description = ?, last_updated = ?, product_brand = ?, " +
                "product_category = ?, product_class = ?, product_segment = ?, " +
                "product_nature = ?, product_section = ?, product_shelf_life = ?, " +
                "product_weight = ?, maintaining_quantity = ?, unit_of_measurement = ? " +
                "WHERE product_id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, product.getProductName());
            preparedStatement.setString(2, product.getBarcode());
            preparedStatement.setString(3, product.getProductCode());
            preparedStatement.setString(4, product.getProductImage());
            preparedStatement.setString(5, product.getDescription());
            preparedStatement.setString(6, product.getShortDescription());
            preparedStatement.setTimestamp(7, product.getLastUpdated());
            preparedStatement.setInt(8, product.getProductBrand());
            preparedStatement.setInt(9, product.getProductCategory());
            preparedStatement.setInt(10, product.getProductClass());
            preparedStatement.setInt(11, product.getProductSegment());
            preparedStatement.setInt(12, product.getProductNature());
            preparedStatement.setInt(13, product.getProductSection());
            preparedStatement.setInt(14, product.getProductShelfLife());
            preparedStatement.setDouble(15, product.getProductWeight());
            preparedStatement.setInt(16, product.getMaintainingQuantity());
            preparedStatement.setInt(17, product.getUnitOfMeasurement());
            preparedStatement.setInt(18, product.getProductId());

            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected; // Returns the number of rows affected by the update

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
        String sql = "INSERT INTO products (isActive, parent_id, product_name, barcode, product_code, product_image, description, short_description, date_added, last_updated, product_brand, product_category, product_class, product_segment, product_nature, product_section, product_shelf_life, product_weight, maintaining_quantity, quantity, unit_of_measurement, unit_of_measurement_count, estimated_unit_cost, estimated_extended_cost, price_per_unit, cost_per_unit, priceA, priceB, priceC, priceD, priceE) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setInt(1, product.getIsActive());
            preparedStatement.setInt(2, product.getParentId());
            preparedStatement.setString(3, product.getProductName());
            preparedStatement.setString(4, product.getBarcode());
            preparedStatement.setString(5, product.getProductCode());
            preparedStatement.setString(6, product.getProductImage());
            preparedStatement.setString(7, product.getDescription());
            preparedStatement.setString(8, product.getShortDescription());
            preparedStatement.setDate(9, new java.sql.Date(product.getDateAdded().getTime()));
            preparedStatement.setTimestamp(10, product.getLastUpdated());
            preparedStatement.setInt(11, product.getProductBrand());
            preparedStatement.setInt(12, product.getProductCategory());
            preparedStatement.setInt(13, product.getProductClass());
            preparedStatement.setInt(14, product.getProductSegment());
            preparedStatement.setInt(15, product.getProductNature());
            preparedStatement.setInt(16, product.getProductSection());
            preparedStatement.setInt(17, product.getProductShelfLife());
            preparedStatement.setDouble(18, product.getProductWeight());
            preparedStatement.setInt(19, product.getMaintainingQuantity());
            preparedStatement.setDouble(20, product.getQuantity());
            preparedStatement.setInt(21, product.getUnitOfMeasurement());
            preparedStatement.setInt(22, product.getUnitOfMeasurementCount());
            preparedStatement.setDouble(23, product.getEstimatedUnitCost());
            preparedStatement.setDouble(24, product.getEstimatedExtendedCost());
            preparedStatement.setDouble(25, product.getPricePerUnit());
            preparedStatement.setDouble(26, product.getCostPerUnit());
            preparedStatement.setDouble(27, product.getPriceA());
            preparedStatement.setDouble(28, product.getPriceB());
            preparedStatement.setDouble(29, product.getPriceC());
            preparedStatement.setDouble(30, product.getPriceD());
            preparedStatement.setDouble(31, product.getPriceE());

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
        Product product = new Product();
        String sqlQuery = "SELECT * FROM products WHERE product_id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {

            preparedStatement.setInt(1, productId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    product = new Product();
                    product.setProductName(resultSet.getString("product_name"));

                    return product;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return product;
    }

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
    public ProductSEO getProductSEOByDescription(String description) {
        ProductSEO productSEO = new ProductSEO();
        String sqlQuery = "SELECT product_brand, product_category, product_class, product_segment, product_nature, product_section FROM products WHERE description = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {

            preparedStatement.setString(1, description);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    productSEO.setDescription(description);
                    productSEO.setProductBrand(brandDAO.getBrandNameById(resultSet.getInt("product_brand")));
                    productSEO.setProductCategory(categoriesDAO.getCategoryNameById(resultSet.getInt("product_category")));
                    productSEO.setProductClass(productClassDAO.getProductClassNameById(resultSet.getInt("product_class")));
                    productSEO.setProductSegment(segmentDAO.getSegmentNameById(resultSet.getInt("product_segment")));
                    productSEO.setProductNature(natureDAO.getNatureNameById(resultSet.getInt("product_nature")));
                    productSEO.setProductSection(sectionsDAO.getSectionNameById(resultSet.getInt("product_section")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return productSEO;
    }

    public int getNextBarcodeNumber() {
        int nextPO = 0;
        String updateQuery = "UPDATE product_barcode SET bar_no = LAST_INSERT_ID(bar_no + 1)";
        String selectQuery = "SELECT LAST_INSERT_ID()";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {

            updateStatement.executeUpdate();

            try (PreparedStatement selectStatement = connection.prepareStatement(selectQuery);
                 ResultSet resultSet = selectStatement.executeQuery()) {

                if (resultSet.next()) {
                    nextPO = resultSet.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception according to your application's needs
        }

        return nextPO;
    }

}
