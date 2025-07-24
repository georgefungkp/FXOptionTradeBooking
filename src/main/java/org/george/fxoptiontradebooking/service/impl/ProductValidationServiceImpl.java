package org.george.fxoptiontradebooking.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.george.fxoptiontradebooking.dto.request.TradeBookingRequest;
import org.george.fxoptiontradebooking.entity.ProductType;
import org.george.fxoptiontradebooking.exception.BusinessValidationException;
import org.george.fxoptiontradebooking.service.ProductValidationService;
import org.george.fxoptiontradebooking.service.validator.ProductValidator;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ProductValidationServiceImpl implements ProductValidationService {

    private static final List<String> SUPPORTED_CURRENCIES = Arrays.asList(
            "USD", "EUR", "GBP", "JPY", "CHF", "CAD", "AUD", "NZD", "SEK", "NOK", "DKK"
    );

    private final Map<ProductType, ProductValidator> validators;

    public ProductValidationServiceImpl(List<ProductValidator> validatorList) {
        this.validators = validatorList.stream()
                .collect(Collectors.toMap(
                        ProductValidator::getSupportedProductType,
                        Function.identity()
                ));
        
        log.info("Initialized ProductValidationService with {} validators: {}", 
                validators.size(), 
                validators.keySet());
    }

    @Override
    public void validateTradeRequest(TradeBookingRequest request) {
        log.debug("Starting validation for trade request: {}", request.getTradeReference());

        if (request.getProductType() == null) {
            throw new BusinessValidationException("Product type is required");
        }
        
        // Validate common fields first
        validateCommonFields(request);
        
        // Delegate to product-specific validator
        ProductValidator validator = validators.get(request.getProductType());
        if (validator == null) {
            throw new BusinessValidationException("No validator found for product type: " + request.getProductType());
        }
        
        validator.validate(request);
        
        log.debug("Validation completed successfully for trade: {}", request.getTradeReference());
    }

    @Override
    public void validateVanillaOption(TradeBookingRequest request) {
        ProductValidator validator = validators.get(ProductType.VANILLA_OPTION);
        if (validator == null) {
            throw new BusinessValidationException("Vanilla option validator not available");
        }
        validator.validate(request);
    }

    @Override
    public void validateExoticOption(TradeBookingRequest request) {
        ProductValidator validator = validators.get(ProductType.EXOTIC_OPTION);
        if (validator == null) {
            throw new BusinessValidationException("Exotic option validator not available");
        }
        validator.validate(request);
    }

    @Override
    public void validateFXContract(TradeBookingRequest request) {
        ProductValidator validator = validators.get(ProductType.FX_FORWARD);
        if (validator == null) {
            throw new BusinessValidationException("FX contract validator not available");
        }
        validator.validate(request);
    }

    @Override
    public void validateSwap(TradeBookingRequest request) {
        ProductValidator validator = validators.get(ProductType.FX_SWAP);
        if (validator == null) {
            throw new BusinessValidationException("Swap validator not available");
        }
        validator.validate(request);
    }

    private void validateCommonFields(TradeBookingRequest request) {
        // Validate trade reference
        if (request.getTradeReference() == null || request.getTradeReference().trim().isEmpty()) {
            throw new BusinessValidationException("Trade reference is required");
        }
        
        if (request.getTradeReference().length() > 50) {
            throw new BusinessValidationException("Trade reference cannot exceed 50 characters");
        }
        
        // Validate counterparty
        if (request.getCounterpartyId() == null || request.getCounterpartyId() <= 0) {
            throw new BusinessValidationException("Valid counterparty ID is required");
        }
        
        // Validate currencies
        validateCurrency(request.getBaseCurrency(), "Base currency");
        validateCurrency(request.getQuoteCurrency(), "Quote currency");
        
        if (request.getBaseCurrency().equals(request.getQuoteCurrency())) {
            throw new BusinessValidationException("Base and quote currencies must be different");
        }
        
        // Validate amounts
        if (request.getNotionalAmount() == null) {
            throw new BusinessValidationException("Notional amount is required");
        }
        
        if (request.getNotionalAmount().compareTo(java.math.BigDecimal.valueOf(10000)) < 0) {
            throw new BusinessValidationException("Minimum notional amount is 10,000");
        }
        
        if (request.getNotionalAmount().compareTo(java.math.BigDecimal.valueOf(1000000000)) > 0) {
            throw new BusinessValidationException("Maximum notional amount is 1 billion");
        }
        
        // Validate dates
        validateDates(request);
        
        // Validate created by
        if (request.getCreatedBy() == null || request.getCreatedBy().trim().isEmpty()) {
            throw new BusinessValidationException("Created by field is required");
        }
    }

    private void validateCurrency(String currency, String fieldName) {
        if (currency == null || currency.trim().isEmpty()) {
            throw new BusinessValidationException(fieldName + " is required");
        }
        
        if (currency.length() != 3) {
            throw new BusinessValidationException(fieldName + " must be exactly 3 characters");
        }
        
        if (!SUPPORTED_CURRENCIES.contains(currency.toUpperCase())) {
            log.warn("Currency {} is not in the list of commonly supported currencies", currency);
        }
    }

    private void validateDates(TradeBookingRequest request) {
        if (request.getTradeDate() == null) {
            throw new BusinessValidationException("Trade date is required");
        }
        
        if (request.getValueDate() == null) {
            throw new BusinessValidationException("Value date is required");
        }
        
        if (request.getTradeDate().isAfter(LocalDate.now().plusDays(3))) {
            throw new BusinessValidationException("Trade date cannot be more than 3 days in the future");
        }
        
        if (request.getValueDate().isBefore(request.getTradeDate())) {
            throw new BusinessValidationException("Value date must be on or after trade date");
        }
    }
}