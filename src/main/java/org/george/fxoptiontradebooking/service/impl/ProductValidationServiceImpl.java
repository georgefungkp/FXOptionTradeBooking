package org.george.fxoptiontradebooking.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.george.fxoptiontradebooking.dto.request.TradeBookingRequest;
import org.george.fxoptiontradebooking.entity.ProductType;
import org.george.fxoptiontradebooking.exception.BusinessValidationException;
import org.george.fxoptiontradebooking.service.ProductValidationService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class ProductValidationServiceImpl implements ProductValidationService {

    private static final List<String> SUPPORTED_CURRENCIES = Arrays.asList(
            "USD", "EUR", "GBP", "JPY", "CHF", "CAD", "AUD", "NZD", "SEK", "NOK", "DKK"
    );

    private static final List<String> SUPPORTED_INDICES = Arrays.asList(
            "SOFR", "LIBOR", "EURIBOR", "SONIA", "TONAR"
    );

    @Override
    public void validateTradeRequest(TradeBookingRequest request) {
        log.debug("Validating trade request for product type: {}", request.getProductType());

        switch (request.getProductType()) {
            case VANILLA_OPTION -> validateVanillaOption(request);
            case EXOTIC_OPTION -> validateExoticOption(request);
            case FX_FORWARD, FX_SPOT -> validateFXContract(request);
            case FX_SWAP, CURRENCY_SWAP, INTEREST_RATE_SWAP -> validateSwap(request);
            default -> throw new BusinessValidationException(
                    "Unsupported product type: " + request.getProductType()
            );
        }

        validateCommonFields(request);
    }

    @Override
    public void validateVanillaOption(TradeBookingRequest request) {
        if (request.getOptionType() == null) {
            throw new BusinessValidationException("Option type is required for vanilla options");
        }

        if (request.getStrikePrice() == null) {
            throw new BusinessValidationException("Strike price is required for vanilla options");
        }

        if (request.getMaturityDate() == null) {
            throw new BusinessValidationException("Maturity date is required for options");
        }

        if (request.getMaturityDate().isBefore(request.getValueDate())) {
            throw new BusinessValidationException("Maturity date must be after value date");
        }

        // Validate option tenor (max 5 years)
        if (request.getMaturityDate().isAfter(request.getTradeDate().plusYears(5))) {
            throw new BusinessValidationException("Option tenor cannot exceed 5 years");
        }
    }

    @Override
    public void validateExoticOption(TradeBookingRequest request) {
        validateVanillaOption(request); // Base option validation

        if (request.getExoticOptionType() == null) {
            throw new BusinessValidationException("Exotic option type is required for exotic options");
        }

        switch (request.getExoticOptionType()) {
            case BARRIER_OPTION -> validateBarrierOption(request);
            case ASIAN_OPTION -> validateAsianOption(request);
            case DIGITAL_OPTION -> validateDigitalOption(request);
            // Add more exotic option validations as needed
        }
    }

    @Override
    public void validateFXContract(TradeBookingRequest request) {
        if (request.getProductType() == ProductType.FX_FORWARD && request.getForwardRate() == null) {
            throw new BusinessValidationException("Forward rate is required for FX forwards");
        }

        if (request.getProductType() == ProductType.FX_SPOT && request.getSpotRate() == null) {
            throw new BusinessValidationException("Spot rate is required for FX spots");
        }

        // FX forwards must have maturity date
        if (request.getProductType() == ProductType.FX_FORWARD && request.getMaturityDate() == null) {
            throw new BusinessValidationException("Maturity date is required for FX forwards");
        }

        // Validate tenor limits
        if (request.getMaturityDate() != null &&
                request.getMaturityDate().isAfter(request.getTradeDate().plusYears(2))) {
            throw new BusinessValidationException("FX forward tenor cannot exceed 2 years");
        }
    }

    @Override
    public void validateSwap(TradeBookingRequest request) {
        if (request.getSwapType() == null) {
            throw new BusinessValidationException("Swap type is required for swap products");
        }

        switch (request.getProductType()) {
            case FX_SWAP -> validateFXSwap(request);
            case CURRENCY_SWAP -> validateCurrencySwap(request);
            case INTEREST_RATE_SWAP -> validateInterestRateSwap(request);
        }
    }

    private void validateCommonFields(TradeBookingRequest request) {
        // Validate currencies
        if (!SUPPORTED_CURRENCIES.contains(request.getBaseCurrency())) {
            throw new BusinessValidationException("Unsupported base currency: " + request.getBaseCurrency());
        }

        if (!SUPPORTED_CURRENCIES.contains(request.getQuoteCurrency())) {
            throw new BusinessValidationException("Unsupported quote currency: " + request.getQuoteCurrency());
        }

        // For interest rate swaps, base and quote currencies should be the same
        // For FX products, they should be different
        if (request.getProductType() == ProductType.INTEREST_RATE_SWAP) {
            if (!request.getBaseCurrency().equals(request.getQuoteCurrency())) {
                throw new BusinessValidationException("For interest rate swaps, base and quote currencies must be the same");
            }
        } else {
            // For FX products (options, forwards, spots, FX swaps, currency swaps)
            if (request.getBaseCurrency().equals(request.getQuoteCurrency())) {
                throw new BusinessValidationException("Base and quote currencies cannot be the same");
            }
        }

        // Validate dates
        if (request.getTradeDate().isAfter(LocalDate.now())) {
            throw new BusinessValidationException("Trade date cannot be in the future");
        }

        if (request.getValueDate().isBefore(request.getTradeDate())) {
            throw new BusinessValidationException("Value date cannot be before trade date");
        }

    }

    private void validateBarrierOption(TradeBookingRequest request) {
        if (request.getBarrierLevel() == null) {
            throw new BusinessValidationException("Barrier level is required for barrier options");
        }

        if (request.getKnockInOut() == null ||
                (!request.getKnockInOut().equals("IN") && !request.getKnockInOut().equals("OUT"))) {
            throw new BusinessValidationException("Knock-in/out type must be 'IN' or 'OUT'");
        }
    }

    private void validateAsianOption(TradeBookingRequest request) {
        if (request.getObservationFrequency() == null) {
            throw new BusinessValidationException("Observation frequency is required for Asian options");
        }
    }

    private void validateDigitalOption(TradeBookingRequest request) {
        // Digital options have fixed payout, no additional validation needed currently
        log.debug("Validating digital option: {}", request.getTradeReference());
    }

    private void validateFXSwap(TradeBookingRequest request) {
        if (request.getNearLegDate() == null || request.getFarLegDate() == null) {
            throw new BusinessValidationException("Both near and far leg dates are required for FX swaps");
        }

        if (request.getNearLegRate() == null || request.getFarLegRate() == null) {
            throw new BusinessValidationException("Both near and far leg rates are required for FX swaps");
        }

        if (request.getFarLegDate().isBefore(request.getNearLegDate())) {
            throw new BusinessValidationException("Far leg date must be after near leg date");
        }
    }

    private void validateCurrencySwap(TradeBookingRequest request) {
        validateFXSwap(request); // Base swap validation

        if (request.getMaturityDate() == null) {
            throw new BusinessValidationException("Maturity date is required for currency swaps");
        }

        // Currency swaps typically have longer tenors
        if (request.getMaturityDate().isAfter(request.getTradeDate().plusYears(10))) {
            throw new BusinessValidationException("Currency swap tenor cannot exceed 10 years");
        }
    }

    private void validateInterestRateSwap(TradeBookingRequest request) {
        if (request.getFixedRate() == null) {
            throw new BusinessValidationException("Fixed rate is required for interest rate swaps");
        }

        if (request.getFloatingRateIndex() == null ||
                !SUPPORTED_INDICES.contains(request.getFloatingRateIndex())) {
            throw new BusinessValidationException("Valid floating rate index is required");
        }

        if (request.getPaymentFrequency() == null) {
            throw new BusinessValidationException("Payment frequency is required for interest rate swaps");
        }

        if (request.getMaturityDate() == null) {
            throw new BusinessValidationException("Maturity date is required for interest rate swaps");
        }
    }
}
