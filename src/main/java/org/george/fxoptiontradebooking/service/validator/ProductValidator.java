
package org.george.fxoptiontradebooking.service.validator;

import org.george.fxoptiontradebooking.dto.request.TradeBookingRequest;

/**
 * Strategy interface for product-specific validation logic.
 * Each product type implements its own validation rules through this interface.
 */
public interface ProductValidator {
    
    /**
     * Validates a trade booking request for a specific product type.
     * 
     * @param request The trade booking request to validate
     * @throws org.george.fxoptiontradebooking.exception.BusinessValidationException if validation fails
     */
    void validate(TradeBookingRequest request);
    
    /**
     * Returns the product type this validator handles.
     * 
     * @return The product type supported by this validator
     */
    org.george.fxoptiontradebooking.entity.ProductType getSupportedProductType();
}
