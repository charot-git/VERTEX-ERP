package com.vertex.vos.Utilities;

import com.vertex.vos.Objects.Tax;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class VATCalculator {

    private static final int DEFAULT_TAX_ID = 1; // Default tax ID to use for VAT rate retrieval

    /**
     * Retrieves the VAT rate from the database using TaxDAO.
     *
     * @return The VAT rate retrieved from the database.
     */
    private static double getVatRateFromDatabase() {
        TaxDAO taxDAO = new TaxDAO();
        Tax tax = taxDAO.getTaxRates(DEFAULT_TAX_ID);
        return tax != null ? tax.getVatRate() : 0.0; // Return 0.0 if tax is null
    }

    /**
     * Calculates the VAT amount based on the given value and VAT rate from the database.
     *
     * @param value The value on which VAT is applied.
     * @return The calculated VAT amount.
     */
    public static BigDecimal calculateVat(BigDecimal value) {
        double vatRate = getVatRateFromDatabase();
        BigDecimal vatRateDecimal = BigDecimal.valueOf(vatRate);
        BigDecimal vatAmount = value.multiply(vatRateDecimal);

        // Round VAT amount to two decimal places
        vatAmount = vatAmount.setScale(2, RoundingMode.HALF_UP);

        return vatAmount;
    }
}
