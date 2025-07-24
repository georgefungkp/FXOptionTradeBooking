package org.george.fxoptiontradebooking.service.factory.impl;

import lombok.extern.slf4j.Slf4j;
import org.george.fxoptiontradebooking.dto.request.TradeBookingRequest;
import org.george.fxoptiontradebooking.entity.*;
import org.george.fxoptiontradebooking.service.factory.TradeFactory;
import org.springframework.stereotype.Component;

/**
 * Factory for creating vanilla option trades.
 */
@Component
@Slf4j
public class VanillaOptionTradeFactory implements TradeFactory {
    
    @Override
    public Trade createTrade(TradeBookingRequest request, Counterparty counterparty) {
        log.debug("Creating vanilla option trade for request: {}", request.getTradeReference());
        
        VanillaOptionTrade trade = new VanillaOptionTrade();
        
        // Set common fields
        setCommonFields(trade, request, counterparty);
        
        // Set option-specific fields
        trade.setOptionType(request.getOptionType());
        trade.setStrikePrice(request.getStrikePrice());
        trade.setSpotRate(request.getSpotRate());
        trade.setPremiumAmount(request.getPremiumAmount());
        trade.setPremiumCurrency(request.getPremiumCurrency());
        
        log.debug("Created vanilla option trade: {}", trade.getTradeReference());
        return trade;
    }
    
    @Override
    public boolean supports(ProductType productType) {
        return productType == ProductType.VANILLA_OPTION;
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
