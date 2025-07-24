
package org.george.fxoptiontradebooking.entity;

/**
 * Enumeration of supported financial product types.
 * Represents the main categories of financial instruments that can be traded.
 */
public enum ProductType {
    VANILLA_OPTION("Vanilla Option"),
    EXOTIC_OPTION("Exotic Option"), 
    FX_FORWARD("FX Forward"),
    FX_SPOT("FX Spot"),
    FX_SWAP("FX Swap"),
    CURRENCY_SWAP("Currency Swap"),
    INTEREST_RATE_SWAP("Interest Rate Swap");
    
    private final String displayName;
    
    ProductType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
