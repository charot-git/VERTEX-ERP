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
            if (discount.compareTo(BigDecimal.ZERO) < 0 || discount.compareTo(BigDecimal.valueOf(100)) > 0) {
                throw new IllegalArgumentException("Invalid discount percentage: " + discount);
            }

            BigDecimal discountAmount = listPrice.multiply(discount.divide(BigDecimal.valueOf(100)));
            discountedPrice = discountedPrice.subtract(discountAmount);
            listPrice = discountedPrice;
        }

        return discountedPrice.setScale(2, RoundingMode.HALF_UP);
    }


}
