package com.vertex.vos.Utilities;

import com.vertex.vos.Objects.Product;
import javafx.collections.ObservableList;

import java.util.HashMap;
import java.util.Map;

public class CalculateProductPackageBreakdown {

    // Map to store product_id and the total base quantity required
    private static Map<Integer, Integer> productBreakdownMap = new HashMap<>();

    public static void calculatePackageBreakdown(ObservableList<Product> products) {
        // Iterate through each product in the list
        for (Product product : products) {
            // Calculate the breakdown for each product
            int baseUnits = calculateBreakdown(product, products);
            // Store the result in the map
            productBreakdownMap.put(product.getProductId(), baseUnits);
        }

        // Display the breakdown results
        productBreakdownMap.forEach((productId, baseUnits) -> {
            System.out.println("Product ID: " + productId + " requires " + baseUnits + " base units.");
        });
    }

    // Recursive method to calculate the total base units for a given product
    private static int calculateBreakdown(Product product, ObservableList<Product> products) {
        // If the product is a base product (no parent)
        if (product.getParentId() == 0) {
            return product.getUnitOfMeasurementCount(); // Return its own count as base
        }

        // Find the parent product
        Product parentProduct = findProductById(product.getParentId(), products);

        // Recursively calculate the base units for the parent and multiply by current product's count
        if (parentProduct != null) {
            int parentBaseUnits = calculateBreakdown(parentProduct, products);
            return parentBaseUnits * product.getUnitOfMeasurementCount();
        }

        // If no parent is found, assume 1 base unit (this case should ideally never happen if data is correct)
        return product.getUnitOfMeasurementCount();
    }

    // Helper method to find a product by its ID
    private static Product findProductById(int productId, ObservableList<Product> products) {
        for (Product product : products) {
            if (product.getProductId() == productId) {
                return product;
            }
        }
        return null;
    }
}
