package com.vertex.vos.DAO;

import com.vertex.vos.Objects.Product;
import com.vertex.vos.Utilities.*;
import com.zaxxer.hikari.HikariDataSource;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PackageBreakdownDAO {
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();
    BrandDAO brandDAO = new BrandDAO();
    CategoriesDAO categoriesDAO = new CategoriesDAO();
    ProductClassDAO productClassDAO = new ProductClassDAO();
    SegmentDAO segmentDAO = new SegmentDAO();
    SectionsDAO sectionsDAO = new SectionsDAO();

    // Get linked products by product ID
    public ObservableList<Product> getLinkedProductsInProductTable(int productId) {
        ObservableList<Product> linkedProducts = FXCollections.observableArrayList();
        String query = "SELECT p.*, u.order " +
                "FROM products p " +
                "JOIN units u ON p.unit_of_measurement = u.unit_id " +
                "WHERE p.product_id = ? OR p.parent_id = ? " +
                "ORDER BY u.order"; // Assuming `unit_order` is the column for ordering

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, productId);
            stmt.setInt(2, productId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Product product = extractProductFromResultSet(rs);
                linkedProducts.add(product);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return linkedProducts;
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
        product.setProductBrandString(brandDAO.getBrandNameById(rs.getInt("product_brand")));
        product.setProductCategoryString(categoriesDAO.getCategoryNameById(rs.getInt("product_category")));
        product.setProductClassString(productClassDAO.getProductClassNameById(rs.getInt("product_class")));
        product.setProductSegmentString(segmentDAO.getSegmentNameById(rs.getInt("product_segment")));
        product.setProductSectionString(sectionsDAO.getSectionNameById(rs.getInt("product_section")));
        product.setProductShelfLife(rs.getInt("product_shelf_life"));
        product.setProductWeight(rs.getDouble("product_weight"));
        product.setMaintainingQuantity(rs.getInt("maintaining_quantity"));
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

    ProductDAO productDAO = new ProductDAO();
    InventoryDAO inventoryDAO = new InventoryDAO();


    public boolean convertQuantity(int productIdToConvert, int availableQuantity, int productIdForConversion, int quantityRequested, int branchId) {

        Product productToConvert = productDAO.getProductDetails(productIdToConvert);
        Product productForConversion = productDAO.getProductDetails(productIdForConversion);

        if (productToConvert == null || productForConversion == null) {
            DialogUtils.showErrorMessage("Product Error", "One or both products not found.");
            return false;
        }

        String productUnitToConvert = productToConvert.getUnitOfMeasurementString();
        String productUnitForConversion = productForConversion.getUnitOfMeasurementString();

        int countPerUnitToConvert = productToConvert.getUnitOfMeasurementCount();
        int countPerUnitForConversion = productForConversion.getUnitOfMeasurementCount();

        int quantityToConvert = 0;
        int remainingQuantityForSource = 0;
        if (productUnitToConvert.equals(productUnitForConversion)) {
            quantityToConvert = quantityRequested;
        } else {
            if (productUnitForConversion.equals("Pieces")) {
                quantityToConvert = (quantityRequested * countPerUnitToConvert);
                remainingQuantityForSource = quantityRequested;
            } else {
                int neededPieces = quantityRequested * countPerUnitForConversion; // Pieces needed for requested boxes
                if (availableQuantity < neededPieces) {
                    DialogUtils.showErrorMessage("Insufficient Quantity", "Not enough quantity available for conversion.");
                } else {
                    quantityToConvert = quantityRequested;
                    remainingQuantityForSource = quantityRequested * countPerUnitForConversion;
                }
            }
        }

        if (availableQuantity < quantityRequested) {
            DialogUtils.showErrorMessage("Insufficient Quantity", "Not enough quantity available for conversion.");
        }


        int newQuantityForTarget = quantityToConvert; // Add quantity to target

        // Perform inventory updates
        boolean updateSource = inventoryDAO.updateInventory(productIdToConvert, branchId, -remainingQuantityForSource); // Subtract from source
        boolean updateTarget = inventoryDAO.updateInventory(productIdForConversion, branchId, newQuantityForTarget); // Add to target

        // Check if both updates succeeded
        if (updateSource && updateTarget) {
            DialogUtils.showCompletionDialog("Conversion Successful", "Successfully converted " + quantityRequested + " units.");
            return true;
        } else {
            DialogUtils.showErrorMessage("Conversion Failed", "Conversion failed. Please try again.");
            return false;
        }
    }
}
