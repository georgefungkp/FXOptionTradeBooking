package org.george.fxoptiontradebooking.entity;

/**
 * Enumeration of exotic option types supported by the system.
 * Exotic options have more complex payoff structures than vanilla options.
 */
public enum ExoticOptionType {
    BARRIER_OPTION("Barrier Option"),
    ASIAN_OPTION("Asian Option"),
    LOOKBACK_OPTION("Lookback Option"),
    DIGITAL_OPTION("Digital Option"),
    COMPOUND_OPTION("Compound Option"),
    RAINBOW_OPTION("Rainbow Option"),
    BERMUDA_OPTION("Bermuda Option");
    
    private final String displayName;
    
    ExoticOptionType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
