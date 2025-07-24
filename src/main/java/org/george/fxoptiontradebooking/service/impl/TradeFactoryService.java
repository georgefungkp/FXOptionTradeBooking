package org.george.fxoptiontradebooking.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.george.fxoptiontradebooking.dto.request.TradeBookingRequest;
import org.george.fxoptiontradebooking.entity.Counterparty;
import org.george.fxoptiontradebooking.entity.ProductType;
import org.george.fxoptiontradebooking.entity.Trade;
import org.george.fxoptiontradebooking.exception.BusinessValidationException;
import org.george.fxoptiontradebooking.service.factory.TradeFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for creating trade entities using the Factory Pattern.
 * Delegates creation to product-specific factories.
 */
@Service
@Slf4j
public class TradeFactoryService {

    private final Map<ProductType, TradeFactory> factories;

    public TradeFactoryService(List<TradeFactory> factoryList) {
        this.factories = factoryList.stream()
                .flatMap(factory -> {
                    // Handle factories that might support multiple product types
                    return java.util.Arrays.stream(ProductType.values())
                            .filter(factory::supports)
                            .map(productType -> Map.entry(productType, factory));
                })
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (existing, _) -> {
                            log.warn("Multiple factories found for product type: {}. Using first one: {}", 
                                    existing, existing.getClass().getSimpleName());
                            return existing;
                        }
                ));
        
        log.info("Initialized TradeFactoryService with {} factories: {}", 
                factories.size(), 
                factories.entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                entry -> entry.getValue().getClass().getSimpleName()
                        )));
    }

    /**
     * Creates a trade entity based on the product type in the request.
     * 
     * @param request The trade booking request
     * @param counterparty The counterparty entity
     * @return The appropriate trade entity subclass
     */
    public Trade createTradeEntity(TradeBookingRequest request, Counterparty counterparty) {
        log.debug("Creating trade entity for product type: {}", request.getProductType());
        
        if (request.getProductType() == null) {
            throw new BusinessValidationException("Product type is required for trade creation");
        }
        
        TradeFactory factory = factories.get(request.getProductType());
        if (factory == null) {
            throw new BusinessValidationException("No factory found for product type: " + request.getProductType());
        }
        
        Trade trade = factory.createTrade(request, counterparty);
        
        log.debug("Successfully created trade entity: {} for product type: {}", 
                trade.getTradeReference(), request.getProductType());
        
        return trade;
    }

}