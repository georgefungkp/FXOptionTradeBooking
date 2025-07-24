package org.george.fxoptiontradebooking.service.factory.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.george.fxoptiontradebooking.dto.request.TradeBookingRequest;
import org.george.fxoptiontradebooking.entity.*;
import org.george.fxoptiontradebooking.service.factory.TradeFactory;
import org.springframework.stereotype.Component;

/**
 * Factory for creating exotic option trades.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ExoticOptionTradeFactory implements TradeFactory {
    
    private final ObjectMapper objectMapper;
    
    @Override
    public Trade createTrade(TradeBookingRequest request, Counterparty counterparty) {
        log.debug("Creating exotic option trade for request: {}", request.getTradeReference());
        
        ExoticOptionTrade trade = new ExoticOptionTrade();
        
        // Set common fields
        setCommonFields(trade, request, counterparty);
        
        // Set option-specific fields
        trade.setOptionType(request.getOptionType());
        trade.setStrikePrice(request.getStrikePrice());
        trade.setSpotRate(request.getSpotRate());
        trade.setPremiumAmount(request.getPremiumAmount());
        trade.setPremiumCurrency(request.getPremiumCurrency());
        
        // Set exotic-specific fields
        trade.setExoticOptionType(request.getExoticOptionType());
        trade.setBarrierLevel(request.getBarrierLevel());
        trade.setKnockInOut(request.getKnockInOut());
        trade.setObservationFrequency(request.getObservationFrequency());
        
        // Store additional parameters as JSON
        if (request.getAdditionalParameters() != null && !request.getAdditionalParameters().isEmpty()) {
            try {
                trade.setExoticParameters(objectMapper.writeValueAsString(request.getAdditionalParameters()));
            } catch (JsonProcessingException e) {
                log.warn("Failed to serialize exotic parameters for trade {}: {}", 
                        request.getTradeReference(), e.getMessage());
            }
        }
        
        log.debug("Created exotic option trade: {}", trade.getTradeReference());
        return trade;
    }
    
    @Override
    public boolean supports(ProductType productType) {
        return productType == ProductType.EXOTIC_OPTION;
    }
    
    private void setCommonFields(Trade trade, TradeBookingRequest request, Counterparty counterparty) {
        trade.setTradeReference(request.getTradeReference());
        trade.setCounterparty(counterparty);
        trade.setBaseCurrency(request.getBaseCurrency());
        trade.setQuoteCurrency(request.getQuoteCurrency());
        trade.setNotionalAmount(request.getNotionalAmount());
        trade.setTradeDate(request.getTradeDate());
        trade.setValueDate(request.getValueDate());
        trade.setMaturityDate(request.getMaturityDate());
        trade.setCreatedBy(request.getCreatedBy());
    }
}
