package com.vertex.vos;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class DiscountCalculator {

    /**
     * Calculate the discounted price based on the list price and a list of line discounts.
     *
     * @param listPrice      The original price before discounts.
     * @param lineDiscounts  The list of line discounts to apply.
     * @return The discounted price.
     */
    public static BigDecimal calculateDiscountedPrice(BigDecimal listPrice, List<BigDecimal> lineDiscounts) {
        BigDecimal discountedPrice = listPrice;

        for (BigDecimal discount : lineDiscounts) {
            validateDiscount(discount);

            BigDecimal discountAmount = calculateDiscountAmount(listPrice, discount);
            discountedPrice = discountedPrice.subtract(discountAmount);
            listPrice = discountedPrice; // Update the list price for cascading discounts
        }

        return discountedPrice.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculate the total discount amount based on the list price and a list of line discounts.
     *
     * @param listPrice      The original price before discounts.
     * @param lineDiscounts  The list of line discounts to apply.
     * @return The total discount amount.
     */
    public static BigDecimal calculateTotalDiscountAmount(BigDecimal listPrice, List<BigDecimal> lineDiscounts) {
        BigDecimal totalDiscountAmount = BigDecimal.ZERO;

        for (BigDecimal discount : lineDiscounts) {
            validateDiscount(discount);

            BigDecimal discountAmount = calculateDiscountAmount(listPrice, discount);
            totalDiscountAmount = totalDiscountAmount.add(discountAmount);
            listPrice = listPrice.subtract(discountAmount); // Update the list price for cascading discounts
        }

        return totalDiscountAmount.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculate the discount amount for a single discount percentage.
     *
     * @param price      The price to apply the discount to.
     * @param discount   The discount percentage.
     * @return The discount amount.
     */
    private static BigDecimal calculateDiscountAmount(BigDecimal price, BigDecimal discount) {
        return price.multiply(discount.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));
    }

    /**
     * Validate the discount percentage.
     *
     * @param discount The discount percentage to validate.
     */
    private static void validateDiscount(BigDecimal discount) {
        if (discount.compareTo(BigDecimal.ZERO) < 0 || discount.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("Invalid discount percentage: " + discount);
        }
    }
}
