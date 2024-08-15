package com.vertex.vos.Utilities;

import com.vertex.vos.Objects.Product;
import javafx.collections.ObservableList;

public class CalculateProductPackageBreakdown {

    /**
     * Calculates the package breakdown for each product in the list.
     *
     * @param productToConvertForCalculation the list of products to calculate the breakdown for
     */
    public static void calculate(ObservableList<Product> productToConvertForCalculation) {
        // Calculate products in package
        for (Product product : productToConvertForCalculation) {
            int productId = product.getProductId();
            String productUnit = product.getUnitOfMeasurementString();
            int productCountPerUnit = product.getUnitOfMeasurementCount();


            // Output the results
            System.out.println("Product ID: " + productId);
            System.out.println("Unit of Measurement: " + productUnit);
            System.out.println("Count Per Unit: " + productCountPerUnit);
            System.out.println();
        }
    }
}
