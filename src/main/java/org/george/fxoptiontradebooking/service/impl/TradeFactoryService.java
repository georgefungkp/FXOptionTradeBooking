package org.george.fxoptiontradebooking.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.george.fxoptiontradebooking.dto.request.TradeBookingRequest;
import org.george.fxoptiontradebooking.entity.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class TradeFactoryService {

    public Trade createTradeEntity(TradeBookingRequest request, Counterparty counterparty) {
        Trade trade = new Trade();
        
        // Set common fields
        setCommonFields(trade, request, counterparty);
        
        // Set product-specific fields
        setProductSpecificFields(trade, request);
        
        return trade;
    }

    private void setCommonFields(Trade trade, TradeBookingRequest request, Counterparty counterparty) {
        trade.setTradeReference(request.getTradeReference());
        trade.setCounterparty(counterparty);
        trade.setProductType(request.getProductType());
        trade.setBaseCurrency(request.getBaseCurrency());
        trade.setQuoteCurrency(request.getQuoteCurrency());
        trade.setNotionalAmount(request.getNotionalAmount());
        trade.setTradeDate(request.getTradeDate());
        trade.setValueDate(request.getValueDate());
        trade.setMaturityDate(request.getMaturityDate());
        trade.setCreatedBy(request.getCreatedBy());
        trade.setStatus(TradeStatus.PENDING);
        trade.setCreatedAt(LocalDateTime.now());
        trade.setUpdatedAt(LocalDateTime.now());
    }

    private void setProductSpecificFields(Trade trade, TradeBookingRequest request) {
        if (isOptionProduct(request.getProductType())) {
            setOptionFields(trade, request);
        }
        
        if (isFXProduct(request.getProductType())) {
            setFXFields(trade, request);
        }
        
        if (isSwapProduct(request.getProductType())) {
            setSwapFields(trade, request);
        }
    }

    private void setOptionFields(Trade trade, TradeBookingRequest request) {
        trade.setOptionType(request.getOptionType());
        trade.setStrikePrice(request.getStrikePrice());
        trade.setPremiumAmount(request.getPremiumAmount());
        trade.setPremiumCurrency(request.getPremiumCurrency());
        
        if (request.getProductType() == ProductType.EXOTIC_OPTION) {
            trade.setExoticOptionType(request.getExoticOptionType());
            trade.setBarrierLevel(request.getBarrierLevel());
            trade.setKnockInOut(request.getKnockInOut());
            trade.setObservationFrequency(request.getObservationFrequency());
        }
    }

    private void setFXFields(Trade trade, TradeBookingRequest request) {
        trade.setForwardRate(request.getForwardRate());
        trade.setSpotRate(request.getSpotRate());
    }

    private void setSwapFields(Trade trade, TradeBookingRequest request) {
        trade.setSwapType(request.getSwapType());
        trade.setNearLegAmount(request.getNearLegAmount());
        trade.setFarLegAmount(request.getFarLegAmount());
        trade.setNearLegRate(request.getNearLegRate());
        trade.setFarLegRate(request.getFarLegRate());
        trade.setNearLegDate(request.getNearLegDate());
        trade.setFarLegDate(request.getFarLegDate());
        
        if (request.getSwapType() == SwapType.INTEREST_RATE_SWAP) {
            trade.setFixedRate(request.getFixedRate());
            trade.setFloatingRateIndex(request.getFloatingRateIndex());
            trade.setPaymentFrequency(request.getPaymentFrequency());
        }
    }

    private boolean isOptionProduct(ProductType productType) {
        return productType == ProductType.VANILLA_OPTION || productType == ProductType.EXOTIC_OPTION;
    }

    private boolean isFXProduct(ProductType productType) {
        return productType == ProductType.FX_FORWARD || productType == ProductType.FX_SPOT;
    }

    private boolean isSwapProduct(ProductType productType) {
        return productType == ProductType.FX_SWAP || 
               productType == ProductType.CURRENCY_SWAP || 
               productType == ProductType.INTEREST_RATE_SWAP;
    }
}
