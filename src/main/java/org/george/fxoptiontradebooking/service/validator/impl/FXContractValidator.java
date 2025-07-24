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
public class FXContractValidator implements ProductValidator {

    @Override
    public void validate(TradeBookingRequest request) {
        log.debug("Validating FX contract trade: {}", request.getTradeReference());
        
        validateForwardRate(request);
        validateSettlementDate(request);
        validateCurrencyPair(request);
        
        log.debug("FX contract validation completed for trade: {}", request.getTradeReference());
    }

    @Override
    public ProductType getSupportedProductType() {
        return ProductType.FX_FORWARD;
    }

    private void validateForwardRate(TradeBookingRequest request) {
        if (request.getForwardRate() == null) {
            throw new BusinessValidationException("Forward rate is required for FX contracts");
        }
        
        if (request.getForwardRate().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessValidationException("Forward rate must be positive");
        }
        
        if (request.getForwardRate().scale() > 6) {
            throw new BusinessValidationException("Forward rate cannot have more than 6 decimal places");
        }
    }

    private void validateSettlementDate(TradeBookingRequest request) {
        if (request.getValueDate().isBefore(request.getTradeDate().plusDays(1))) {
            throw new BusinessValidationException("FX forward settlement date must be at least T+1");
        }
        
        if (request.getValueDate().isAfter(LocalDate.now().plusYears(5))) {
            throw new BusinessValidationException("FX forward settlement date cannot be more than 5 years in the future");
        }
    }

    private void validateCurrencyPair(TradeBookingRequest request) {
        if (request.getBaseCurrency().equals(request.getQuoteCurrency())) {
            throw new BusinessValidationException("Base and quote currencies must be different for FX contracts");
        }
    }
}
