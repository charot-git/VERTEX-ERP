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

    UnitDAO unitDAO = new UnitDAO();

    public boolean convertQuantity(int productIdToConvert, int availableQuantity,
                                   int productIdForConversion, int quantityRequested, int branchId) {

        // Get product details
        Product productToConvert = productDAO.getProductDetails(productIdToConvert);
        Product productForConversion = productDAO.getProductDetails(productIdForConversion);

        // Retrieve unit details (for productToConvert, for example)
        productToConvert.setUnit(unitDAO.getUnitDetail(productToConvert));
        productForConversion.setUnit(unitDAO.getUnitDetail(productForConversion));

        // We'll store the conversion outcome here.
        ConversionResult conversionResult = null;

        int orderFrom = productToConvert.getUnit().getUnit_order();
        int orderTo = productForConversion.getUnit().getUnit_order();

        // Choose the appropriate conversion method based on unit orders.
        if (orderFrom == 1 && orderTo == 3) {
            // Convert Piece to Box
            conversionResult = convertPieceToBox(quantityRequested, productToConvert, productForConversion ,availableQuantity);
        } else if (orderFrom == 3 && orderTo == 1) {
            // Convert Box to Piece
            conversionResult = convertBoxToPiece(quantityRequested, productToConvert, productForConversion ,availableQuantity);
        } else if (orderFrom == 2 && orderTo == 3) {
            // Convert Pack to Box
            conversionResult = convertPackToBox(quantityRequested, productToConvert, productForConversion ,availableQuantity);
        } else if (orderFrom == 3 && orderTo == 2) {
            // Convert Box to Pack
            conversionResult = convertBoxToPack(quantityRequested, productToConvert, productForConversion ,availableQuantity);
        } else if (orderFrom == 1 && orderTo == 2) {
            // Convert Piece to Pack
            conversionResult = convertPieceToPack(quantityRequested, productToConvert, productForConversion ,availableQuantity);
        } else if (orderFrom == 2 && orderTo == 1) {
            // Convert Pack to Piece
            conversionResult = convertPackToPiece(quantityRequested, productToConvert, productForConversion ,availableQuantity);
        }


        // Check if conversionResult is valid and if available quantity suffices.

        assert conversionResult != null;
        if (availableQuantity < conversionResult.remainingQuantityForSource) {
            DialogUtils.showErrorMessage("Insufficient Quantity", "Not enough quantity available for conversion.");
            return false;
        }

        int newQuantityForTarget = conversionResult.quantityToConvert;

        // Update the inventories:
        boolean updateSource = inventoryDAO.updateInventory(productIdToConvert, branchId, -conversionResult.remainingQuantityForSource);
        boolean updateTarget = inventoryDAO.updateInventory(productIdForConversion, branchId, newQuantityForTarget);

        if (updateSource && updateTarget) {
            DialogUtils.showCompletionDialog("Conversion Successful", "Successfully converted " + quantityRequested + " units.");
            return true;
        } else {
            DialogUtils.showErrorMessage("Conversion Failed", "Conversion failed. Please try again.");
            return false;
        }
    }

    private ConversionResult convertPackToPiece(int quantityRequested, Product productToConvert, Product productForConversion, int availableQuantity) {
        int unitsPerPack = productToConvert.getUnitOfMeasurementCount();
        int totalPieces = quantityRequested * unitsPerPack;

        if (availableQuantity < quantityRequested) {
            DialogUtils.showErrorMessage("Insufficient Quantity", "Not enough packs available for conversion.");
            return null;
        }
        return new ConversionResult(totalPieces, quantityRequested);
    }

    private ConversionResult convertPieceToPack(int quantityRequested, Product piece, Product pack, int availableQuantity) {
        int piecesPerPack = pack.getUnitOfMeasurementCount();

        // Total pieces required for the conversion
        int totalPiecesRequired = quantityRequested * piecesPerPack;

        // Check if enough pieces are available
        if (totalPiecesRequired > availableQuantity) {
            DialogUtils.showErrorMessage("Insufficient Quantity", "Not enough pieces available for conversion.");
            return null;
        }

        // Calculate the number of packs to be created
        int totalPacksToBeAdded = quantityRequested;


        return new ConversionResult(totalPacksToBeAdded, totalPiecesRequired);
    }



    private ConversionResult convertBoxToPack(int quantityRequested, Product productToConvert, Product productForConversion, int availableQuantity) {
        return null;
    }

    private ConversionResult convertPackToBox(int quantityRequested, Product productToConvert, Product productForConversion, int availableQuantity) {
        return null;
    }

    private ConversionResult convertBoxToPiece(int quantityRequested, Product productToConvert, Product productForConversion, int availableQuantity) {
        return null;
    }

    private ConversionResult convertPieceToBox(int quantityRequested, Product productToConvert, Product productForConversion, int availableQuantity) {
        return null;
    }

    /**
     * Helper class to hold conversion results.
     */
    private static class ConversionResult {
        int quantityToConvert;         // The quantity to be added to the target product inventory
        int remainingQuantityForSource; // The quantity to subtract from the source product inventory

        ConversionResult(int quantityToConvert, int remainingQuantityForSource) {
            this.quantityToConvert = quantityToConvert;
            this.remainingQuantityForSource = remainingQuantityForSource;
        }
    }

    /*
     * Below are sample conversion methods.
     * Adjust the logic in each method based on your business rules.
     */


}
