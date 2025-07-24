package org.george.fxoptiontradebooking.service;

import org.george.fxoptiontradebooking.dto.request.TradeBookingRequest;

public interface ProductValidationService {
    
    void validateTradeRequest(TradeBookingRequest request);
    
    void validateVanillaOption(TradeBookingRequest request);
    
    void validateExoticOption(TradeBookingRequest request);
    
    void validateFXContract(TradeBookingRequest request);
    
    void validateSwap(TradeBookingRequest request);
}