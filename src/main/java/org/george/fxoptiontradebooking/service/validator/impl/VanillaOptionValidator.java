package org.george.fxoptiontradebooking.service.validator.impl;

import lombok.extern.slf4j.Slf4j;
import org.george.fxoptiontradebooking.dto.request.TradeBookingRequest;
import org.george.fxoptiontradebooking.entity.ProductType;
import org.george.fxoptiontradebooking.exception.BusinessValidationException;
import org.george.fxoptiontradebooking.service.validator.ProductValidator;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
@Slf4j
public class VanillaOptionValidator implements ProductValidator {

    @Override
    public void validate(TradeBookingRequest request) {
        log.debug("Validating vanilla option trade: {}", request.getTradeReference());
        
        validateOptionType(request);
        validateStrikePrice(request);
        validateSpotRate(request);
        validateMaturityDate(request);
        validatePremium(request);
        
        log.debug("Vanilla option validation completed for trade: {}", request.getTradeReference());
    }

    @Override
    public ProductType getSupportedProductType() {
        return ProductType.VANILLA_OPTION;
    }

    private void validateOptionType(TradeBookingRequest request) {
        if (request.getOptionType() == null) {
            throw new BusinessValidationException("Option type is required for vanilla options");
        }
    }

    private void validateStrikePrice(TradeBookingRequest request) {
        if (request.getStrikePrice() == null) {
            throw new BusinessValidationException("Strike price is required for vanilla options");
        }
        
        if (request.getStrikePrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessValidationException("Strike price must be positive");
        }
        
        if (request.getStrikePrice().scale() > 6) {
            throw new BusinessValidationException("Strike price cannot have more than 6 decimal places");
        }
    }

    private void validateSpotRate(TradeBookingRequest request) {
        if (request.getSpotRate() != null && request.getSpotRate().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessValidationException("Spot rate must be positive when provided");
        }
    }

    private void validateMaturityDate(TradeBookingRequest request) {
        if (request.getMaturityDate() == null) {
            throw new BusinessValidationException("Maturity date is required for vanilla options");
        }
        
        if (request.getMaturityDate().isBefore(request.getValueDate())) {
            throw new BusinessValidationException("Maturity date must be after value date");
        }
        
        if (request.getMaturityDate().isAfter(LocalDate.now().plusYears(10))) {
            throw new BusinessValidationException("Maturity date cannot be more than 10 years in the future");
        }
    }

    private void validatePremium(TradeBookingRequest request) {
        if (request.getPremiumAmount() != null) {
            if (request.getPremiumAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessValidationException("Premium amount must be positive when provided");
            }
            
            if (request.getPremiumCurrency() == null || request.getPremiumCurrency().trim().isEmpty()) {
                throw new BusinessValidationException("Premium currency is required when premium amount is specified");
            }
        }
    }
}
