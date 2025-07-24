package org.george.fxoptiontradebooking.entity;

/**
 * Enumeration of swap types supported by the system.
 */
public enum SwapType {
    FX_SWAP("FX Swap"),
    CURRENCY_SWAP("Currency Swap"),
    INTEREST_RATE_SWAP("Interest Rate Swap"),
    CROSS_CURRENCY_SWAP("Cross Currency Swap"),
    BASIS_SWAP("Basis Swap");
    
    private final String displayName;
    
    SwapType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
