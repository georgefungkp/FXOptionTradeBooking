package org.george.fxoptiontradebooking.service.validator.impl;

import lombok.extern.slf4j.Slf4j;
import org.george.fxoptiontradebooking.dto.request.TradeBookingRequest;
import org.george.fxoptiontradebooking.entity.ProductType;
import org.george.fxoptiontradebooking.exception.BusinessValidationException;
import org.george.fxoptiontradebooking.service.validator.ProductValidator;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class SwapValidator implements ProductValidator {

    private static final List<String> SUPPORTED_INDICES = Arrays.asList(
            "SOFR", "LIBOR", "EURIBOR", "SONIA", "TONAR"
    );

    @Override
    public void validate(TradeBookingRequest request) {
        log.debug("Validating swap trade: {}", request.getTradeReference());
        
        validateSwapType(request);
        validateMaturityDate(request);
        
        if (request.getSwapType() != null) {
            switch (request.getSwapType()) {
                case FX_SWAP -> validateFXSwap(request);
                case CURRENCY_SWAP -> validateCurrencySwap(request);
                case INTEREST_RATE_SWAP -> validateInterestRateSwap(request);
                default -> throw new BusinessValidationException("Unsupported swap type: " + request.getSwapType());
            }
        }
        
        log.debug("Swap validation completed for trade: {}", request.getTradeReference());
    }

    @Override
    public ProductType getSupportedProductType() {
        return ProductType.FX_SWAP;
    }

    private void validateSwapType(TradeBookingRequest request) {
        if (request.getSwapType() == null) {
            throw new BusinessValidationException("Swap type is required for swap products");
        }
    }

    private void validateMaturityDate(TradeBookingRequest request) {
        if (request.getMaturityDate() == null) {
            throw new BusinessValidationException("Maturity date is required for swap products");
        }
        
        if (request.getMaturityDate().isBefore(request.getValueDate())) {
            throw new BusinessValidationException("Maturity date must be after value date for swaps");
        }
    }

    private void validateFXSwap(TradeBookingRequest request) {
        if (request.getNearLegAmount() == null || request.getFarLegAmount() == null) {
            throw new BusinessValidationException("Both near leg and far leg amounts are required for FX swaps");
        }
        
        if (request.getNearLegRate() == null || request.getFarLegRate() == null) {
            throw new BusinessValidationException("Both near leg and far leg rates are required for FX swaps");
        }
        
        if (request.getNearLegDate() == null || request.getFarLegDate() == null) {
            throw new BusinessValidationException("Both near leg and far leg dates are required for FX swaps");
        }
        
        if (request.getFarLegDate().isBefore(request.getNearLegDate())) {
            throw new BusinessValidationException("Far leg date must be after near leg date");
        }
    }

    private void validateCurrencySwap(TradeBookingRequest request) {
        if (request.getBaseCurrency().equals(request.getQuoteCurrency())) {
            throw new BusinessValidationException("Base and quote currencies must be different for currency swaps");
        }
        
        if (request.getFixedRate() == null) {
            throw new BusinessValidationException("Fixed rate is required for currency swaps");
        }
        
        if (request.getPaymentFrequency() == null || request.getPaymentFrequency().trim().isEmpty()) {
            throw new BusinessValidationException("Payment frequency is required for currency swaps");
        }
    }

    private void validateInterestRateSwap(TradeBookingRequest request) {
        if (request.getFixedRate() == null) {
            throw new BusinessValidationException("Fixed rate is required for interest rate swaps");
        }
        
        if (request.getFixedRate().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessValidationException("Fixed rate cannot be negative");
        }
        
        if (request.getFloatingRateIndex() == null || request.getFloatingRateIndex().trim().isEmpty()) {
            throw new BusinessValidationException("Floating rate index is required for interest rate swaps");
        }
        
        if (!SUPPORTED_INDICES.contains(request.getFloatingRateIndex())) {
            throw new BusinessValidationException("Unsupported floating rate index: " + request.getFloatingRateIndex() + 
                    ". Supported indices: " + String.join(", ", SUPPORTED_INDICES));
        }
        
        if (request.getPaymentFrequency() == null || request.getPaymentFrequency().trim().isEmpty()) {
            throw new BusinessValidationException("Payment frequency is required for interest rate swaps");
        }
    }
}
