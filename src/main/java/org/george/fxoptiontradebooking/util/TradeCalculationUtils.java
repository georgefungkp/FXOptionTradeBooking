package org.george.fxoptiontradebooking.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Utility class for financial calculations related to FX option trades.
 * 
 * This class provides methods for calculating various financial aspects of options trading,
 * including premium calculations, profit and loss (PnL) calculations, and potentially
 * other financial metrics relevant to FX option trading.
 * 
 * All calculations follow financial industry conventions for precision and rounding,
 * using BigDecimal to ensure accuracy in monetary calculations.
 */
    
    /**
     * Calculates the premium amount for an FX option trade.
     * 
     * The premium is the price paid by the option buyer to the option seller,
     * typically calculated as a percentage (premium rate) of the notional amount.
     * 
     * This method implements a simple premium calculation:
     *   Premium = Notional Amount × Premium Rate
     * 
     * The result is rounded to 2 decimal places using HALF_UP rounding,
     * following standard financial rounding conventions for monetary amounts.
     * 
     * @param notionalAmount The principal amount of the trade in the base currency
     * @param premiumRate The premium rate as a decimal (e.g., 0.02 for 2%)
     * @return The calculated premium amount, or ZERO if any input is null
     */
    public static BigDecimal calculatePremium(BigDecimal notionalAmount, BigDecimal premiumRate) {
        if (notionalAmount == null || premiumRate == null) {
            return BigDecimal.ZERO;
        }
        return notionalAmount.multiply(premiumRate).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculates the profit and loss (PnL) for an FX option position.
     * 
     * This method provides a simplified PnL calculation for an option position
     * based on the difference between the current market rate and the strike price,
     * multiplied by the notional amount.
     * 
     * For a complete implementation, this would need to factor in:
     * - Option type (call/put) which determines profit direction
     * - Premium paid/received
     * - Whether the option is exercised or not
     * 
     * The result is rounded to 2 decimal places using HALF_UP rounding,
     * following standard financial rounding conventions.
     * 
     * @param notionalAmount The principal amount of the trade in the base currency
     * @param strikePrice The strike price of the option
     * @param currentRate The current market rate
     * @return The calculated PnL, or ZERO if any input is null
     */
    public static BigDecimal calculatePnL(BigDecimal notionalAmount, BigDecimal strikePrice, BigDecimal currentRate) {
        if (notionalAmount == null || strikePrice == null || currentRate == null) {
            return BigDecimal.ZERO;
        }
        return notionalAmount.multiply(currentRate.subtract(strikePrice)).setScale(2, RoundingMode.HALF_UP);
    }
}
