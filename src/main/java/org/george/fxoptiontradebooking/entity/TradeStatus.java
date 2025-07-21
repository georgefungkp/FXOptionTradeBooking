package org.george.fxoptiontradebooking.entity;

public enum TradeStatus {
    PENDING("Pending"),
    CONFIRMED("Confirmed"),
    SETTLED("Settled"),
    CANCELLED("Cancelled"),
    EXPIRED("Expired");
    
    private final String displayName;
    
    TradeStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
