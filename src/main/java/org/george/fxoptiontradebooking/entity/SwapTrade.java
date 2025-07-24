package org.george.fxoptiontradebooking.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entity representing swap trades (FX Swap, Currency Swap, Interest Rate Swap).
 */
@Entity
@Table(name = "swap_trades")
@DiscriminatorValue("SWAP")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SwapTrade extends Trade {
    
    @Enumerated(EnumType.STRING)
    @Column(name = "swap_type", nullable = false)
    private SwapType swapType;
    
    // FX Swap specific fields
    @Column(name = "near_leg_amount", precision = 19, scale = 2)
    private BigDecimal nearLegAmount;
    
    @Column(name = "far_leg_amount", precision = 19, scale = 2)
    private BigDecimal farLegAmount;
    
    @Column(name = "near_leg_rate", precision = 19, scale = 6)
    private BigDecimal nearLegRate;
    
    @Column(name = "far_leg_rate", precision = 19, scale = 6)
    private BigDecimal farLegRate;
    
    @Column(name = "near_leg_date")
    private LocalDate nearLegDate;
    
    @Column(name = "far_leg_date")
    private LocalDate farLegDate;
    
    // Interest Rate Swap specific fields
    @Column(name = "fixed_rate", precision = 19, scale = 6)
    private BigDecimal fixedRate;
    
    @Column(name = "floating_rate_index", length = 10)
    private String floatingRateIndex; // "LIBOR", "SOFR", etc.
    
    @Column(name = "payment_frequency")
    private String paymentFrequency; // "QUARTERLY", "SEMI_ANNUAL", etc.
    
    // Additional swap-specific parameters stored as JSON
    @Column(name = "swap_parameters", columnDefinition = "TEXT")
    private String swapParameters;
    
    @Override
    public ProductType getProductType() {
        return ProductType.FX_SWAP;
    }
    
    /**
     * Returns whether this is an FX swap.
     */
    public boolean isFXSwap() {
        return swapType == SwapType.FX_SWAP;
    }
    
    /**
     * Returns whether this is a currency swap.
     */
    public boolean isCurrencySwap() {
        return swapType == SwapType.CURRENCY_SWAP;
    }
    
    /**
     * Returns whether this is an interest rate swap.
     */
    public boolean isInterestRateSwap() {
        return swapType == SwapType.INTEREST_RATE_SWAP;
    }
}
