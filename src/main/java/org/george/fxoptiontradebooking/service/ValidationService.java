package org.george.fxoptiontradebooking.service;

import org.george.fxoptiontradebooking.dto.request.TradeBookingRequest;
import org.george.fxoptiontradebooking.exception.BusinessValidationException;

/**
 * Service interface for validating trade data according to financial industry standards.
 * Provides methods to validate various aspects of trade requests including dates, currencies,
 * and business rules specific to FX option trading.
 */
public interface ValidationService {

    /**
     * Performs comprehensive validation of a trade booking request.
     * Validates all aspects of the trade including currencies, amounts, dates, and business rules.
     * 
     * @param request The trade booking request to validate
     * @throws BusinessValidationException if any validation fails
     */
    void validateTradeRequest(TradeBookingRequest request);

    /**
     * Validates a currency code according to ISO standards and supported currencies list.
     * 
     * @param currencyCode The currency code to validate
     * @throws BusinessValidationException if the currency code is invalid
     */
    void validateCurrencyCode(String currencyCode);

    /**
     * Validates the sequence of dates in a trade request (trade date, value date, maturity date).
     * Ensures dates follow proper sequencing according to financial trading standards.
     * 
     * @param request The trade booking request containing dates to validate
     * @throws BusinessValidationException if date sequencing is invalid
     */
    void validateDateSequence(TradeBookingRequest request);
}
