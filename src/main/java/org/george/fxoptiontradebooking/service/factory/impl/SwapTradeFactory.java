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
 * Factory for creating swap trades.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SwapTradeFactory implements TradeFactory {
    
    private final ObjectMapper objectMapper;
    
    @Override
    public Trade createTrade(TradeBookingRequest request, Counterparty counterparty) {
        log.debug("Creating swap trade for request: {}", request.getTradeReference());
        
        SwapTrade trade = new SwapTrade();
        
        // Set common fields
        setCommonFields(trade, request, counterparty);
        
        // Set swap-specific fields
        trade.setSwapType(request.getSwapType());
        
        // FX Swap fields
        trade.setNearLegAmount(request.getNearLegAmount());
        trade.setFarLegAmount(request.getFarLegAmount());
        trade.setNearLegRate(request.getNearLegRate());
        trade.setFarLegRate(request.getFarLegRate());
        trade.setNearLegDate(request.getNearLegDate());
        trade.setFarLegDate(request.getFarLegDate());
        
        // Interest Rate Swap fields
        trade.setFixedRate(request.getFixedRate());
        trade.setFloatingRateIndex(request.getFloatingRateIndex());
        trade.setPaymentFrequency(request.getPaymentFrequency());
        
        // Store additional parameters as JSON
        if (request.getAdditionalParameters() != null && !request.getAdditionalParameters().isEmpty()) {
            try {
                trade.setSwapParameters(objectMapper.writeValueAsString(request.getAdditionalParameters()));
            } catch (JsonProcessingException e) {
                log.warn("Failed to serialize swap parameters for trade {}: {}", 
                        request.getTradeReference(), e.getMessage());
            }
        }
        
        log.debug("Created {} swap trade: {}", request.getSwapType(), trade.getTradeReference());
        return trade;
    }
    
    @Override
    public boolean supports(ProductType productType) {
        return productType == ProductType.FX_SWAP;
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
