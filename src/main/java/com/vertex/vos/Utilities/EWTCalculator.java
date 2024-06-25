package com.vertex.vos.Utilities;

import com.vertex.vos.Constructors.Tax;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class EWTCalculator {

    private static final int DEFAULT_TAX_ID = 1; // Default tax ID to use for EWT rate retrieval

    /**
     * Retrieves the EWT and VAT rates from the database using TaxDAO.
     *
     * @return Array where [0] is EWT rate and [1] is VAT rate.
     */
    private static double[] getTaxRatesFromDatabase() {
        TaxDAO taxDAO = new TaxDAO();
        Tax tax = taxDAO.getTaxRates(DEFAULT_TAX_ID);
        double ewtRate = tax != null ? tax.getWithholdingRate() : 0.0;
        double vatRate = tax != null ? tax.getVatRate() : 0.0;
        return new double[] { ewtRate, vatRate };
    }

    /**
     * Calculates the EWT amount based on the given value and EWT rate from the database.
     *
     * @param value The value on which EWT is applied.
     * @return The calculated EWT amount.
     */
    public static BigDecimal calculateWithholding(BigDecimal value) {
        double[] taxRates = getTaxRatesFromDatabase();
        double ewtRate = taxRates[0];
        double vatRate = taxRates[1];

        BigDecimal ewtRateDecimal = BigDecimal.valueOf(ewtRate);
        BigDecimal vatRateDecimal = BigDecimal.valueOf(1 + vatRate); // Adjust as per your calculation logic

        // Calculate EWT amount
        BigDecimal ewtAmount = value.divide(vatRateDecimal, 2, RoundingMode.HALF_UP).multiply(ewtRateDecimal);

        // Round EWT amount to two decimal places
        ewtAmount = ewtAmount.setScale(2, RoundingMode.HALF_UP);

        return ewtAmount;
    }
}
