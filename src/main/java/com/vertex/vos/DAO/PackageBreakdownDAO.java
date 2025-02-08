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

    // Get linked products by product I

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
            conversionResult = convertPieceToBox(quantityRequested, productToConvert, productForConversion, availableQuantity);
        } else if (orderFrom == 3 && orderTo == 1) {
            // Convert Box to Piece
            conversionResult = convertBoxToPiece(quantityRequested, productToConvert, productForConversion, availableQuantity);
        } else if (orderFrom == 2 && orderTo == 3) {
            // Convert Pack to Box
            conversionResult = convertPackToBox(quantityRequested, productToConvert, productForConversion, availableQuantity);
        } else if (orderFrom == 3 && orderTo == 2) {
            // Convert Box to Pack
            conversionResult = convertBoxToPack(quantityRequested, productToConvert, productForConversion, availableQuantity);
        } else if (orderFrom == 1 && orderTo == 2) {
            // Convert Piece to Pack
            conversionResult = convertPieceToPack(quantityRequested, productToConvert, productForConversion, availableQuantity);
        } else if (orderFrom == 2 && orderTo == 1) {
            // Convert Pack to Piece
            conversionResult = convertPackToPiece(quantityRequested, productToConvert, productForConversion, availableQuantity);
        }


        if (conversionResult == null) {
            return false;
        }

        int newQuantityForTarget = conversionResult.quantityToConvert;

        // Update the inventories:
        boolean updateSource = inventoryDAO.updateInventory(productIdToConvert, branchId, -conversionResult.remainingQuantityForSource);
        boolean updateTarget = inventoryDAO.updateInventory(productIdForConversion, branchId, newQuantityForTarget);

        return updateSource && updateTarget;
    }

    private ConversionResult convertPackToPiece(int quantityRequested, Product productToConvert, Product productForConversion, int availableQuantity) {
        int unitsPerPack = productToConvert.getUnitOfMeasurementCount();
        int totalPieces = quantityRequested * unitsPerPack;

        if (availableQuantity < quantityRequested) {
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
            return null;
        }
        return new ConversionResult(quantityRequested, totalPiecesRequired);
    }


    private ConversionResult convertBoxToPack(int quantityRequested, Product box, Product pack, int availableQuantity) {
        int unitsPerBox = box.getUnitOfMeasurementCount(); // 200 pieces per box
        int unitsPerPack = pack.getUnitOfMeasurementCount(); // 20 pieces per pack
        int packsPerBox = unitsPerBox / unitsPerPack; // 200 / 20 = 10 packs per box
        int totalPacks = quantityRequested * packsPerBox;
        if (availableQuantity < quantityRequested) {
            return null;
        }
        return new ConversionResult(totalPacks, quantityRequested);
    }


    private ConversionResult convertPackToBox(int quantityRequested, Product pack, Product box, int availableQuantity) {
        int piecesPerBox = box.getUnitOfMeasurementCount(); // 200 pieces per box
        int piecesPerPack = pack.getUnitOfMeasurementCount(); // 20 pieces per pack
        int boxesPerPack = piecesPerBox / piecesPerPack; // 200 / 20 = 10 packs per box
        int totalPacksToDeduct = quantityRequested * boxesPerPack;
        int actualAvailableQuantityForBox = availableQuantity / boxesPerPack;
        if (actualAvailableQuantityForBox < quantityRequested) {
            return null;
        }
        return new ConversionResult(quantityRequested, totalPacksToDeduct);
    }


    private ConversionResult convertBoxToPiece(int quantityRequested, Product productToConvert, Product productForConversion, int availableQuantity) {
        int unitsPerBox = productToConvert.getUnitOfMeasurementCount(); // Assuming the units per box
        int totalPieces = quantityRequested * unitsPerBox;  // Convert the requested boxes to pieces
        if (availableQuantity < quantityRequested) {
            return null;
        }
        return new ConversionResult(totalPieces, quantityRequested); // Return the result
    }
    private ConversionResult convertPieceToBox(int quantityRequested, Product productToConvert, Product productForConversion, int availableQuantity) {
        int piecesPerBox = productForConversion.getUnitOfMeasurementCount(); // Assuming the pieces per box

        // Total pieces required for the conversion
        int totalPiecesRequired = quantityRequested * piecesPerBox;

        // Check if enough pieces are available for conversion
        if (availableQuantity < totalPiecesRequired) {
            return null;
        }

        return new ConversionResult(quantityRequested, totalPiecesRequired); // Return the result
    }

    private static class ConversionResult {
        int quantityToConvert;         // The quantity to be added to the target product inventory
        int remainingQuantityForSource; // The quantity to subtract from the source product inventory

        ConversionResult(int quantityToConvert, int remainingQuantityForSource) {
            this.quantityToConvert = quantityToConvert;
            this.remainingQuantityForSource = remainingQuantityForSource;
        }
    }
}
