package org.george.fxoptiontradebooking.service.validator.impl;

import lombok.extern.slf4j.Slf4j;
import org.george.fxoptiontradebooking.dto.request.TradeBookingRequest;
import org.george.fxoptiontradebooking.entity.ProductType;
import org.george.fxoptiontradebooking.exception.BusinessValidationException;
import org.george.fxoptiontradebooking.service.validator.ProductValidator;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Slf4j
public class ExoticOptionValidator implements ProductValidator {

    @Override
    public void validate(TradeBookingRequest request) {
        log.debug("Validating exotic option trade: {}", request.getTradeReference());
        
        validateExoticOptionType(request);
        validateOptionType(request);
        validateStrikePrice(request);
        validateMaturityDate(request);
        
        // Validate specific exotic features based on type
        if (request.getExoticOptionType() != null) {
            switch (request.getExoticOptionType()) {
                case BARRIER_OPTION -> validateBarrierOption(request);
                case ASIAN_OPTION -> validateAsianOption(request);
                case DIGITAL_OPTION -> validateDigitalOption(request);
                default -> throw new BusinessValidationException("Unsupported exotic option type: " + request.getExoticOptionType());
            }
        }
        
        log.debug("Exotic option validation completed for trade: {}", request.getTradeReference());
    }

    @Override
    public ProductType getSupportedProductType() {
        return ProductType.EXOTIC_OPTION;
    }

    private void validateExoticOptionType(TradeBookingRequest request) {
        if (request.getExoticOptionType() == null) {
            throw new BusinessValidationException("Exotic option type is required for exotic options");
        }
    }

    private void validateOptionType(TradeBookingRequest request) {
        if (request.getOptionType() == null) {
            throw new BusinessValidationException("Option type (CALL/PUT) is required for exotic options");
        }
    }

    private void validateStrikePrice(TradeBookingRequest request) {
        if (request.getStrikePrice() == null) {
            throw new BusinessValidationException("Strike price is required for exotic options");
        }
        
        if (request.getStrikePrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessValidationException("Strike price must be positive");
        }
    }

    private void validateMaturityDate(TradeBookingRequest request) {
        if (request.getMaturityDate() == null) {
            throw new BusinessValidationException("Maturity date is required for exotic options");
        }
        
        if (request.getMaturityDate().isBefore(request.getValueDate())) {
            throw new BusinessValidationException("Maturity date must be after value date");
        }
    }

    private void validateBarrierOption(TradeBookingRequest request) {
        if (request.getBarrierLevel() == null) {
            throw new BusinessValidationException("Barrier level is required for barrier options");
        }
        
        if (request.getBarrierLevel().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessValidationException("Barrier level must be positive");
        }
        
        if (request.getKnockInOut() == null || request.getKnockInOut().trim().isEmpty()) {
            throw new BusinessValidationException("Knock-in/out specification is required for barrier options");
        }
        
        if (!request.getKnockInOut().matches("^(KNOCK_IN|KNOCK_OUT)$")) {
            throw new BusinessValidationException("Knock-in/out must be either 'KNOCK_IN' or 'KNOCK_OUT'");
        }
    }

    private void validateAsianOption(TradeBookingRequest request) {
        if (request.getObservationFrequency() == null || request.getObservationFrequency().trim().isEmpty()) {
            throw new BusinessValidationException("Observation frequency is required for Asian options");
        }
        
        if (!request.getObservationFrequency().matches("^(DAILY|WEEKLY|MONTHLY)$")) {
            throw new BusinessValidationException("Observation frequency must be DAILY, WEEKLY, or MONTHLY");
        }
    }

    private void validateDigitalOption(TradeBookingRequest request) {
        if (request.getStrikePrice() == null) {
            throw new BusinessValidationException("Strike price is required for digital options");
        }
        
        // Digital options typically have a fixed payout
        if (request.getPremiumAmount() == null) {
            throw new BusinessValidationException("Payout amount (premium) is required for digital options");
        }
    }
}