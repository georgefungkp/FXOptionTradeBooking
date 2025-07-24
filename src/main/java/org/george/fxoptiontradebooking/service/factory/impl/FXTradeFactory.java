package org.george.fxoptiontradebooking.service.factory.impl;

import lombok.extern.slf4j.Slf4j;
import org.george.fxoptiontradebooking.dto.request.TradeBookingRequest;
import org.george.fxoptiontradebooking.entity.*;
import org.george.fxoptiontradebooking.service.factory.TradeFactory;
import org.springframework.stereotype.Component;

/**
 * Factory for creating FX forward and spot trades.
 */
@Component
@Slf4j
public class FXTradeFactory implements TradeFactory {
    
    @Override
    public Trade createTrade(TradeBookingRequest request, Counterparty counterparty) {
        log.debug("Creating FX trade for request: {}", request.getTradeReference());
        
        FXTrade trade = new FXTrade();
        
        // Set common fields
        setCommonFields(trade, request, counterparty);
        
        // Set FX-specific fields
        trade.setForwardRate(request.getForwardRate());
        trade.setSpotRate(request.getSpotRate());
        
        // Determine if this is a spot trade (typically T+2 settlement)
        boolean isSpot = request.getValueDate() != null && 
                        request.getValueDate().isBefore(request.getTradeDate().plusDays(3));
        trade.setIsSpotTrade(isSpot);
        
        log.debug("Created {} trade: {}", isSpot ? "FX spot" : "FX forward", trade.getTradeReference());
        return trade;
    }
    
    @Override
    public boolean supports(ProductType productType) {
        return productType == ProductType.FX_FORWARD || productType == ProductType.FX_SPOT;
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
