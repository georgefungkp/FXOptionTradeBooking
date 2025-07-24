package org.george.fxoptiontradebooking.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

/**
 * Abstract base class for all option trades.
 * Contains common option-specific fields.
 */
@Entity
@Table(name = "option_trades")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public abstract class OptionTrade extends Trade {
    
    @Enumerated(EnumType.STRING)
    @Column(name = "option_type", nullable = false)
    private OptionType optionType;
    
    @Column(name = "strike_price", nullable = false, precision = 19, scale = 6)
    private BigDecimal strikePrice;
    
    @Column(name = "spot_rate", precision = 19, scale = 6)
    private BigDecimal spotRate;
    
    @Column(name = "premium_amount", precision = 19, scale = 2)
    private BigDecimal premiumAmount;
    
    @Column(name = "premium_currency", length = 3)
    private String premiumCurrency;
    
    /**
     * Returns whether this is a vanilla option.
     */
    public boolean isVanillaOption() {
        return getProductType() == ProductType.VANILLA_OPTION;
    }
    
    /**
     * Returns whether this is an exotic option.
     */
    public boolean isExoticOption() {
        return getProductType() == ProductType.EXOTIC_OPTION;
    }
}
