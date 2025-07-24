package org.george.fxoptiontradebooking.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Entity representing exotic FX option trades.
 */
@Entity
@Table(name = "exotic_option_trades")
@DiscriminatorValue("EXOTIC_OPTION")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ExoticOptionTrade extends OptionTrade {
    
    @Enumerated(EnumType.STRING)
    @Column(name = "exotic_option_type", nullable = false)
    private ExoticOptionType exoticOptionType;
    
    // Barrier option fields
    @Column(name = "barrier_level", precision = 19, scale = 6)
    private BigDecimal barrierLevel;
    
    @Column(name = "knock_in_out")
    private String knockInOut; // "KNOCK_IN" or "KNOCK_OUT"
    
    // Asian option fields
    @Column(name = "observation_frequency")
    private String observationFrequency; // "DAILY", "WEEKLY", "MONTHLY"
    
    // Additional parameters for complex exotic features stored as JSON
    @Column(name = "exotic_parameters", columnDefinition = "TEXT")
    private String exoticParameters;
    
    @Override
    public ProductType getProductType() {
        return ProductType.EXOTIC_OPTION;
    }
    
    /**
     * Returns whether this is a barrier option.
     */
    public boolean isBarrierOption() {
        return exoticOptionType == ExoticOptionType.BARRIER_OPTION;
    }
    
    /**
     * Returns whether this is an Asian option.
     */
    public boolean isAsianOption() {
        return exoticOptionType == ExoticOptionType.ASIAN_OPTION;
    }
    
    /**
     * Returns whether this is a digital option.
     */
    public boolean isDigitalOption() {
        return exoticOptionType == ExoticOptionType.DIGITAL_OPTION;
    }
}
