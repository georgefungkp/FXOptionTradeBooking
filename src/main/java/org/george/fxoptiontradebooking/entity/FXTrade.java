package org.george.fxoptiontradebooking.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Entity representing FX Forward and Spot trades.
 */
@Entity
@Table(name = "fx_trades")
@DiscriminatorValue("FX_FORWARD")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FXTrade extends Trade {
    
    @Column(name = "forward_rate", nullable = false, precision = 19, scale = 6)
    private BigDecimal forwardRate;
    
    @Column(name = "spot_rate", precision = 19, scale = 6)
    private BigDecimal spotRate;
    
    // For FX Spot vs Forward distinction
    @Column(name = "is_spot_trade", nullable = false)
    private Boolean isSpotTrade = false;
    
    @Override
    public ProductType getProductType() {
        return isSpotTrade ? ProductType.FX_SPOT : ProductType.FX_FORWARD;
    }
    
    /**
     * Returns whether this is a spot trade.
     */
    public boolean isSpot() {
        return Boolean.TRUE.equals(isSpotTrade);
    }
    
    /**
     * Returns whether this is a forward trade.
     */
    public boolean isForward() {
        return !Boolean.TRUE.equals(isSpotTrade);
    }
}
