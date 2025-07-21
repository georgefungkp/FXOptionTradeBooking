package org.george.fxoptiontradebooking.entity;

import lombok.Getter;

@Getter
public enum OptionType {
    CALL("Call Option"),
    PUT("Put Option");
    
    private final String displayName;
    
    OptionType(String displayName) {
        this.displayName = displayName;
    }

}
