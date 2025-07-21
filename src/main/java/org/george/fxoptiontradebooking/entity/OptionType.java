package org.george.fxoptiontradebooking.entity;

public enum OptionType {
    CALL("Call Option"),
    PUT("Put Option");
    
    private final String displayName;
    
    OptionType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
