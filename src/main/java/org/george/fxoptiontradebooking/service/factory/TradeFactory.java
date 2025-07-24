package org.george.fxoptiontradebooking.service.factory;

import org.george.fxoptiontradebooking.dto.request.TradeBookingRequest;
import org.george.fxoptiontradebooking.entity.Counterparty;
import org.george.fxoptiontradebooking.entity.Trade;

/**
 * Factory interface for creating trade entities based on product type.
 */
public interface TradeFactory {
    
    /**
     * Creates a trade entity based on the product type in the request.
     * 
     * @param request The trade booking request
     * @param counterparty The counterparty entity
     * @return The appropriate trade entity subclass
     */
    Trade createTrade(TradeBookingRequest request, Counterparty counterparty);
    
    /**
     * Returns whether this factory supports the given product type.
     * 
     * @param productType The product type to check
     * @return true if supported, false otherwise
     */
    boolean supports(org.george.fxoptiontradebooking.entity.ProductType productType);
}
