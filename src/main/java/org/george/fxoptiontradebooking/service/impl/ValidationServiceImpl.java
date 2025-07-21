package org.george.fxoptiontradebooking.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.george.fxoptiontradebooking.dto.request.TradeBookingRequest;
import org.george.fxoptiontradebooking.exception.BusinessValidationException;
import org.george.fxoptiontradebooking.service.ValidationService;
import org.george.fxoptiontradebooking.util.DateUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Implementation of the ValidationService interface for validating FX option trade data.
 * 
 * Provides comprehensive validation for trade booking requests according to financial industry
 * standards and investment banking business rules. Validates currencies, amounts, dates,
 * and option-specific parameters to ensure data integrity and compliance with business requirements.
 * 
 * This service is crucial for preventing invalid trades from entering the system and
 * maintaining data quality standards required in financial institutions.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ValidationServiceImpl implements ValidationService {

    /** 
     * List of major currencies commonly supported in FX option trading.
     * Includes major and common emerging market currencies used in global financial markets.
     */
    private static final Set<String> MAJOR_CURRENCIES = new HashSet<>(Arrays.asList(
        "USD", /* US Dollar */
        "EUR", /* Euro */
        "JPY", /* Japanese Yen */
        "GBP", /* British Pound */
        "CHF", /* Swiss Franc */
        "CAD", /* Canadian Dollar */
        "AUD", /* Australian Dollar */
        "NZD", /* New Zealand Dollar */
        "SEK", /* Swedish Krona */
        "NOK", /* Norwegian Krone */
        "DKK", /* Danish Krone */
        "SGD", /* Singapore Dollar */
        "HKD", /* Hong Kong Dollar */
        "CNY", /* Chinese Yuan */
        "INR", /* Indian Rupee */
        "KRW", /* Korean Won */
        "MXN", /* Mexican Peso */
        "BRL", /* Brazilian Real */
        "ZAR", /* South African Rand */
        "RUB", /* Russian Ruble */
        "TRY"  /* Turkish Lira */
    ));

    /** 
     * Minimum trade notional amount allowed (10,000 in currency units).
     * Standard minimum size for institutional FX option trades to ensure economic viability.
     */
    private static final BigDecimal MIN_NOTIONAL_AMOUNT = new BigDecimal("10000.00");

    /** 
     * Maximum trade notional amount allowed (1 billion in currency units).
     * Upper limit to manage risk exposure and prevent erroneous large trades.
     */
    private static final BigDecimal MAX_NOTIONAL_AMOUNT = new BigDecimal("1000000000.00"); // 1 billion

    /**
     * Minimum allowed strike price for FX options.
     * Prevents zero or negative strike prices which would be economically invalid.
     */
    private static final BigDecimal MIN_STRIKE_PRICE = new BigDecimal("0.000001");

    /**
     * Maximum allowed strike price for FX options.
     * Prevents unrealistically high strike prices which could indicate input errors.
     */
    private static final BigDecimal MAX_STRIKE_PRICE = new BigDecimal("1000000.00");

    /**
     * Maximum maturity tenor for FX options in days (10 years).
     * Standard market practice limits option tenors to 10 years for liquidity
     * and risk management purposes.
     */
    private static final int MAX_MATURITY_DAYS = 3650; // 10 years * 365 days

    /**
     * Performs comprehensive validation of a trade booking request.
     * Validates all aspects of the trade including basic data, currencies, amounts, dates,
     * option-specific data, premium data, and business rules.
     * 
     * The validation is performed in a specific sequence to ensure all critical validations
     * are performed first, followed by more specific validations.
     *
     * @param request The trade booking request to validate
     * @throws BusinessValidationException if any validation fails
     */
    @Override
    public void validateTradeRequest(TradeBookingRequest request) {
        log.debug("Starting validation for trade reference: {}", request.getTradeReference());

        // Step 1: Validate basic trade data (reference, counterparty, etc.)
        validateBasicTradeData(request);

        // Step 2: Validate currency pair (existence, format, convention)
        validateCurrencyPair(request);

        // Step 3: Validate trade amounts (notional, strike price)
        validateAmounts(request);

        // Critical validation: Check same-day maturity FIRST before any date sequence validation
        // This is a common error that should be caught early in the validation process
        if (request.getMaturityDate().equals(request.getTradeDate())) {
            throw new BusinessValidationException("Same-day options are not supported");
        }

        // Step 4: Validate date sequences and business days
        validateDates(request);

        // Step 5: Validate option-specific data (option type, etc.)
        validateOptionSpecificData(request);

        // Step 6: Validate premium data (amount, currency)
        validatePremiumData(request);

        // Step 7: Validate additional business rules
        validateBusinessRules(request);

        log.debug("Validation completed successfully for trade reference: {}", request.getTradeReference());
    }

    /**
     * Validates a currency code according to ISO standards and supported currencies list.
     * Checks that the code is non-null, 3 characters in length, and ideally in the major currencies list.
     * 
     * @param currencyCode The currency code to validate
     * @throws BusinessValidationException if the currency code is invalid
     */
    @Override
    public void validateCurrencyCode(String currencyCode) {
        // Check for null or empty currency code
        if (currencyCode == null || currencyCode.trim().isEmpty()) {
            throw new BusinessValidationException("Currency code cannot be null or empty");
        }

        // Normalize currency code for consistent validation
        String normalizedCurrency = currencyCode.trim().toUpperCase();

        // ISO 4217 standard requires 3-character currency codes
        if (normalizedCurrency.length() != 3) {
            throw new BusinessValidationException("Currency code must be exactly 3 characters: " + currencyCode);
        }

        // Warn about non-major currencies but don't block them
        // This allows for more exotic currency pairs while still alerting risk management
        if (!MAJOR_CURRENCIES.contains(normalizedCurrency)) {
            log.warn("Currency {} is not in major currencies list, proceeding with validation", normalizedCurrency);
        }
    }

    /**
     * Validates the sequence of dates in a trade request (trade date, value date, maturity date).
     * Ensures dates follow proper sequencing according to financial trading standards:
     * - Trade date should be today or in the past (with limited exceptions)
     * - Value date must be after trade date
     * - Maturity date must be after value date
     * - Maximum tenor restrictions are enforced
     * - Business day validation for settlement and maturity dates
     * 
     * @param request The trade booking request containing dates to validate
     * @throws BusinessValidationException if date sequencing is invalid
     */
    @Override
    public void validateDateSequence(TradeBookingRequest request) {
        LocalDate tradeDate = request.getTradeDate();
        LocalDate valueDate = request.getValueDate();
        LocalDate maturityDate = request.getMaturityDate();
        LocalDate today = LocalDate.now();

        // Trade date validation - typically today or in the past
        // Allow one day forward for time zone differences and after-hours trading
        if (tradeDate.isAfter(today.plusDays(1))) {
            throw new BusinessValidationException("Trade date cannot be more than 1 day in the future");
        }

        // Value date must be after trade date (standard FX settlement rules)
        // Minimum T+1 settlement for most currencies
        if (valueDate.isBefore(tradeDate.plusDays(1))) {
            throw new BusinessValidationException("Value date must be at least 1 day after trade date");
        }

        // Maturity date must be after value date
        // Options cannot mature before they're settled
        if (maturityDate.isBefore(valueDate.plusDays(1))) {
            throw new BusinessValidationException("Maturity date must be after value date");
        }

        // Maximum tenor validation (investment bank standard)
        // Limits exposure to very long-dated options
        if (maturityDate.isAfter(tradeDate.plusDays(MAX_MATURITY_DAYS))) {
            throw new BusinessValidationException("Maturity date exceeds maximum allowed tenor of 10 years");
        }

        // Weekend validation for value and maturity dates
        // Options should not settle or mature on non-business days
        validateBusinessDay(valueDate, "Value date");
        validateBusinessDay(maturityDate, "Maturity date");
    }

    /**
     * Validates the basic trade data required for any FX option trade.
     * Checks for presence and format of essential fields like trade reference,
     * counterparty ID, and option type.
     *
     * @param request The trade request to validate
     * @throws BusinessValidationException if basic data validation fails
     */
    private void validateBasicTradeData(TradeBookingRequest request) {
        // Trade reference must be present and non-empty
        if (request.getTradeReference() == null || request.getTradeReference().trim().isEmpty()) {
            throw new BusinessValidationException("Trade reference is required");
        }

        // Trade reference must not exceed maximum length for database/system constraints
        if (request.getTradeReference().length() > 50) {
            throw new BusinessValidationException("Trade reference cannot exceed 50 characters");
        }

        // Counterparty ID must be present and positive
        if (request.getCounterpartyId() == null || request.getCounterpartyId() <= 0) {
            throw new BusinessValidationException("Valid counterparty ID is required");
        }

        // Option type (CALL/PUT) must be specified
        if (request.getOptionType() == null) {
            throw new BusinessValidationException("Option type is required");
        }
    }

    /**
     * Validates the currency pair for the FX option trade.
     * Ensures both currencies are valid ISO codes, are different from each other,
     * and follow standard market conventions for currency pair representation.
     *
     * @param request The trade request containing the currency pair
     * @throws BusinessValidationException if currency pair validation fails
     */
    private void validateCurrencyPair(TradeBookingRequest request) {
        // Validate individual currency codes
        validateCurrencyCode(request.getBaseCurrency());
        validateCurrencyCode(request.getQuoteCurrency());

        // Normalize currencies for consistent comparison
        String baseCurrency = request.getBaseCurrency().toUpperCase().trim();
        String quoteCurrency = request.getQuoteCurrency().toUpperCase().trim();

        // Currencies must be different (can't have USD/USD for example)
        if (baseCurrency.equals(quoteCurrency)) {
            throw new BusinessValidationException("Base currency and quote currency must be different");
        }

        // Check if the currency pair follows standard market conventions
        // (e.g., EUR/USD not USD/EUR, USD/JPY not JPY/USD)
        validateCurrencyPairConvention(baseCurrency, quoteCurrency);
    }

    /**
     * Validates if the currency pair follows standard market conventions.
     * Certain currencies have established conventions in forex markets:
     * - EUR, GBP, AUD, NZD are typically the base currency against USD
     * - USD is typically the base currency against other currencies (JPY, CAD, etc.)
     * 
     * This method provides warnings for non-standard conventions but doesn't block the trade,
     * as some traders may have specific reasons for using non-standard conventions.
     *
     * @param baseCurrency The base currency in the pair
     * @param quoteCurrency The quote currency in the pair
     */
    private void validateCurrencyPairConvention(String baseCurrency, String quoteCurrency) {
        // Major pairs where USD is typically the quote currency
        // EUR/USD, GBP/USD, AUD/USD, NZD/USD are standard conventions
        Set<String> usdQuotePairs = new HashSet<>(Arrays.asList("EUR", "GBP", "AUD", "NZD"));

        // If base is EUR, GBP, AUD, or NZD but quote is not USD, it's non-standard
        if (usdQuotePairs.contains(baseCurrency) && !"USD".equals(quoteCurrency)) {
            log.warn("Non-standard currency pair convention: {}/{} - consider {}/USD",
                    baseCurrency, quoteCurrency, baseCurrency);
        }

        // If USD is the base against EUR, GBP, AUD, or NZD, it's backward from convention
        if ("USD".equals(baseCurrency) && usdQuotePairs.contains(quoteCurrency)) {
            log.warn("Non-standard currency pair convention: USD/{} - consider {}/USD",
                    quoteCurrency, quoteCurrency);
        }
    }

    /**
     * Validates the numerical amounts in the trade request.
     * Checks notional amount, strike price, and spot rate (if provided) for:
     * - Presence (required fields)
     * - Minimum and maximum value constraints
     * - Decimal precision limitations
     * 
     * These validations enforce market standards and system constraints
     * while preventing erroneous inputs that could lead to financial losses.
     *
     * @param request The trade request containing amounts to validate
     * @throws BusinessValidationException if amount validation fails
     */
    private void validateAmounts(TradeBookingRequest request) {
        // Notional amount validation
        if (request.getNotionalAmount() == null) {
            throw new BusinessValidationException("Notional amount is required");
        }

        // Enforce minimum notional amount (market standard minimum size)
        if (request.getNotionalAmount().compareTo(MIN_NOTIONAL_AMOUNT) < 0) {
            throw new BusinessValidationException("Notional amount must be at least " + MIN_NOTIONAL_AMOUNT);
        }

        // Enforce maximum notional amount (risk management constraint)
        if (request.getNotionalAmount().compareTo(MAX_NOTIONAL_AMOUNT) > 0) {
            throw new BusinessValidationException("Notional amount exceeds maximum limit of " + MAX_NOTIONAL_AMOUNT);
        }

        // Currency notional amounts typically have at most 2 decimal places
        if (request.getNotionalAmount().scale() > 2) {
            throw new BusinessValidationException("Notional amount cannot have more than 2 decimal places");
        }

        // Strike price validation
        if (request.getStrikePrice() == null) {
            throw new BusinessValidationException("Strike price is required");
        }

        // Strike price must be positive and non-zero
        if (request.getStrikePrice().compareTo(MIN_STRIKE_PRICE) <= 0) {
            throw new BusinessValidationException("Strike price must be greater than " + MIN_STRIKE_PRICE);
        }

        // Unrealistically high strike prices are likely errors
        if (request.getStrikePrice().compareTo(MAX_STRIKE_PRICE) > 0) {
            throw new BusinessValidationException("Strike price exceeds maximum limit of " + MAX_STRIKE_PRICE);
        }

        // FX rates typically have up to 6 decimal places of precision
        if (request.getStrikePrice().scale() > 6) {
            throw new BusinessValidationException("Strike price cannot have more than 6 decimal places");
        }

        // Spot rate validation (if provided)
        if (request.getSpotRate() != null) {
            // Spot rate must be positive and non-zero
            if (request.getSpotRate().compareTo(MIN_STRIKE_PRICE) <= 0) {
                throw new BusinessValidationException("Spot rate must be greater than " + MIN_STRIKE_PRICE);
            }

            // FX rates typically have up to 6 decimal places of precision
            if (request.getSpotRate().scale() > 6) {
                throw new BusinessValidationException("Spot rate cannot have more than 6 decimal places");
            }
        }
    }

    /**
     * Validates all date fields in a trade request.
     * Ensures all required date fields are present and then validates
     * their sequence and business day constraints.
     * 
     * Critical dates for FX options include:
     * - Trade date: When the trade is executed
     * - Value date: When the option premium is settled
     * - Maturity date: When the option expires
     *
     * @param request The trade request containing dates to validate
     * @throws BusinessValidationException if date validation fails
     */
    private void validateDates(TradeBookingRequest request) {
        // Trade date is when the trade is executed and recorded
        if (request.getTradeDate() == null) {
            throw new BusinessValidationException("Trade date is required");
        }

        // Value date is when the premium is paid/received
        if (request.getValueDate() == null) {
            throw new BusinessValidationException("Value date is required");
        }

        // Maturity date is when the option expires and can be exercised
        if (request.getMaturityDate() == null) {
            throw new BusinessValidationException("Maturity date is required");
        }

        // Perform comprehensive date sequence validation
        validateDateSequence(request);
    }

    /**
     * Validates option-specific data based on the option type.
     * Different option types (CALL, PUT) may have specific validation requirements.
     * This method provides a hook for option-type-specific validations.
     * 
     * For vanilla options (simple calls and puts), minimal additional validation is needed.
     * For exotic options, additional validations would be implemented here.
     *
     * @param request The trade request containing option data to validate
     * @throws BusinessValidationException if option-specific validation fails
     */
    private void validateOptionSpecificData(TradeBookingRequest request) {
        // Additional validation for FX options based on industry standards

        // Validate based on option type
        switch (request.getOptionType()) {
            case CALL:
                // Call option specific validations
                // For vanilla calls, no additional validation is currently required
                // In a complete system, this might validate barriers, digital payouts, etc.
                break;
            case PUT:
                // Put option specific validations
                // For vanilla puts, no additional validation is currently required
                // In a complete system, this might validate barriers, digital payouts, etc.
                break;
            default:
                // Reject unsupported option types
                throw new BusinessValidationException("Unsupported option type: " + request.getOptionType());
        }

        // Note: In a full trading system, exotic option types would require
        // additional validation for parameters like barriers, knock-in/knock-out levels,
        // digital payouts, etc.
    }

    /**
     * Validates premium information for the option trade.
     * Premium is the price paid by the option buyer to the option seller.
     * 
     * This method validates:
     * - Consistency of premium amount and currency (both or neither)
     * - Premium amount is positive
     * - Premium amount decimal precision
     * - Premium currency is valid
     * - Premium currency follows market conventions
     *
     * @param request The trade request containing premium data to validate
     * @throws BusinessValidationException if premium validation fails
     */
    private void validatePremiumData(TradeBookingRequest request) {
        BigDecimal premiumAmount = request.getPremiumAmount();
        String premiumCurrency = request.getPremiumCurrency();

        // Premium amount and currency must be provided together
        // If one is provided, the other must also be provided
        if (premiumAmount != null && premiumCurrency == null) {
            throw new BusinessValidationException("Premium currency is required when premium amount is specified");
        }

        if (premiumCurrency != null && premiumAmount == null) {
            throw new BusinessValidationException("Premium amount is required when premium currency is specified");
        }

        // Only validate further if premium information is provided
        if (premiumAmount != null) {
            // Premium must be positive (option buyer pays seller)
            if (premiumAmount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessValidationException("Premium amount must be positive");
            }

            // Premium amounts typically have at most 2 decimal places as they are monetary amounts
            if (premiumAmount.scale() > 2) {
                throw new BusinessValidationException("Premium amount cannot have more than 2 decimal places");
            }

            // Validate premium currency format
            validateCurrencyCode(premiumCurrency);

            // Premium currency convention check
            // Premium is typically paid in one of the currencies in the pair, or in USD
            String baseCurrency = request.getBaseCurrency().toUpperCase();
            String quoteCurrency = request.getQuoteCurrency().toUpperCase();
            String normalizedPremiumCurrency = premiumCurrency.toUpperCase();

            // Log warning for non-standard premium currency
            // This is allowed but unusual and may indicate an error
            if (!normalizedPremiumCurrency.equals(baseCurrency) &&
                !normalizedPremiumCurrency.equals(quoteCurrency) &&
                !"USD".equals(normalizedPremiumCurrency)) {
                log.warn("Premium currency {} is not base currency, quote currency, or USD", premiumCurrency);
            }
        }
    }

    /**
     * Validates business rules specific to FX option trading.
     * These rules enforce financial industry standards and risk management practices.
     * 
     * Checks include:
     * 1. No same-day maturity options
     * 2. Flagging large trades for additional scrutiny
     * 3. Warning for very short-term options
     * 4. Strike price reasonableness check against spot rate
     *
     * @param request The trade request to validate against business rules
     * @throws BusinessValidationException if critical business rules are violated
     */
    private void validateBusinessRules(TradeBookingRequest request) {
        // Investment bank specific business rules

        // 1. Same-day maturity validation - OPTIONS CANNOT EXPIRE ON TRADE DATE
        // This is a critical check that should be performed early in validation
        if (request.getMaturityDate().equals(request.getTradeDate())) {
            throw new BusinessValidationException("Same-day options are not supported");
        }

        // 2. Large trade validation (requires additional approval for trades > 100M)
        // Large trades present increased market risk and may require special handling
        if (request.getNotionalAmount().compareTo(new BigDecimal("100000000")) > 0) {
            log.warn("Large trade detected (>{} 100M) for reference: {}",
                    request.getBaseCurrency(), request.getTradeReference());
        }

        // 3. Very short-term options (less than 1 week) validation
        // Short-term options may indicate special trading strategies or unusual market conditions
        if (request.getMaturityDate().isBefore(request.getTradeDate().plusDays(7))) {
            log.warn("Short-term option detected (< 1 week) for reference: {}", request.getTradeReference());
        }

        // 4. Strike price reasonableness check (if spot rate is provided)
        // Helps identify potentially erroneous inputs or unusual trading strategies
        if (request.getSpotRate() != null) {
            BigDecimal strikeToSpotRatio = request.getStrikePrice().divide(request.getSpotRate(), 4, RoundingMode.HALF_UP);

            // Flag if strike is more than 50% away from spot (deep in/out of money)
            // These are unusual trades that may indicate errors or special strategies
            if (strikeToSpotRatio.compareTo(new BigDecimal("1.5")) > 0 ||
                strikeToSpotRatio.compareTo(new BigDecimal("0.5")) < 0) {
                log.warn("Deep in/out of money option detected. Strike/Spot ratio: {} for reference: {}",
                        strikeToSpotRatio, request.getTradeReference());
            }
        }
    }

    /**
     * Validates that a date falls on a business day (not a weekend).
     * In financial markets, settlements and option exercises typically
     * cannot occur on weekends or holidays.
     * 
     * This method checks for weekends and could be extended to include
     * holiday calendar validation for specific currency pairs.
     *
     * @param date The date to validate
     * @param fieldName The name of the field being validated (for error messages)
     * @throws BusinessValidationException if the date falls on a weekend
     */
    private void validateBusinessDay(LocalDate date, String fieldName) {
        // Check if date falls on a weekend
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            throw new BusinessValidationException(fieldName + " cannot be a weekend: " + date);
        }

        // Additional holiday validation could be added here using a holiday calendar service
        // This would check for country-specific holidays based on the currencies involved
        // Example: USD holidays for USD trades, both USD and EUR holidays for EUR/USD trades
    }

    /**
     * Validates that an option's maturity date is not the same as the trade date.
     * Same-day options are not supported in standard FX option markets because:
     * 1. There is insufficient time for proper settlement
     * 2. They violate standard option pricing models
     * 3. They blur the line between spot and option transactions
     * 
     * This is a critical validation performed early in the validation process.
     *
     * @param request The trade request to check for same-day maturity
     * @throws BusinessValidationException if maturity date equals trade date
     */
    private void validateSameDayMaturity(TradeBookingRequest request) {
        // Check same-day maturity specifically
        // This rule is so important it's checked in multiple places as a safeguard
        if (request.getMaturityDate().equals(request.getTradeDate())) {
            throw new BusinessValidationException("Same-day options are not supported");
        }
    }
}