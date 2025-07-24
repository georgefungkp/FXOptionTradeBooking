package org.george.fxoptiontradebooking.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "trades")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "tradeId")
public class Trade {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trade_id")
    private Long tradeId;
    
    @Column(name = "trade_reference", unique = true, nullable = false)
    private String tradeReference;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "counterparty_id", nullable = false)
    private Counterparty counterparty;
    
    // Product Information
    @Enumerated(EnumType.STRING)
    @Column(name = "product_type", nullable = false)
    private ProductType productType;
    
    // Currency Information
    @Column(name = "base_currency", nullable = false, length = 3)
    private String baseCurrency;
    
    @Column(name = "quote_currency", nullable = false, length = 3)
    private String quoteCurrency;
    
    // Common Trade Information
    @Column(name = "notional_amount", nullable = false, precision = 19, scale = 2)
    @NotNull
    @Positive
    private BigDecimal notionalAmount;
    
    @Column(name = "trade_date", nullable = false)
    private LocalDate tradeDate;

    /**
     * This is the **date on which the trade is settled**, meaning the actual delivery or payment between parties occurs.
     * It is often a few days after the `tradeDate`, depending on the nature of the trade, market conventions, or settlement cycles.
     **/
    @Column(name = "value_date", nullable = false)
    private LocalDate valueDate;
    
    @Column(name = "maturity_date")
    private LocalDate maturityDate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "trade_status", nullable = false)
    private TradeStatus status;
    
    // Option-specific fields (nullable for non-option products)
    @Enumerated(EnumType.STRING)
    @Column(name = "option_type")
    private OptionType optionType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "exotic_option_type")
    private ExoticOptionType exoticOptionType;
    
    @Column(name = "strike_price", precision = 19, scale = 6)
    private BigDecimal strikePrice;
    
    @Column(name = "premium_amount", precision = 19, scale = 2)
    private BigDecimal premiumAmount;
    
    @Column(name = "premium_currency", length = 3)
    private String premiumCurrency;
    
    // FX Forward/Spot specific fields
    @Column(name = "forward_rate", precision = 19, scale = 6)
    private BigDecimal forwardRate;
    
    @Column(name = "spot_rate", precision = 19, scale = 6)
    private BigDecimal spotRate;
    
    // Swap-specific fields
    @Enumerated(EnumType.STRING)
    @Column(name = "swap_type")
    private SwapType swapType;
    
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
    
    // Exotic option specific fields
    @Column(name = "barrier_level", precision = 19, scale = 6)
    private BigDecimal barrierLevel;
    
    @Column(name = "knock_in_out")
    private String knockInOut; // "IN" or "OUT"
    
    @Column(name = "observation_frequency")
    private String observationFrequency; // "CONTINUOUS", "DAILY", etc.
    
    // Interest Rate Swap specific fields
    @Column(name = "fixed_rate", precision = 19, scale = 6)
    private BigDecimal fixedRate;
    
    @Column(name = "floating_rate_index", length = 10)
    private String floatingRateIndex; // "LIBOR", "SOFR", etc.
    
    @Column(name = "payment_frequency")
    private String paymentFrequency; // "QUARTERLY", "SEMI_ANNUAL", etc.
    
    // Additional product-specific parameters stored as JSON
    @Column(name = "additional_parameters", columnDefinition = "TEXT")
    private String additionalParameters;
    
    // Audit Information
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "created_by", nullable = false)
    private String createdBy;
    
    @Column(name = "updated_by")
    private String updatedBy;
    
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.status = TradeStatus.PENDING;
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // Helper methods to check product types
    public boolean isOption() {
        return productType == ProductType.VANILLA_OPTION || productType == ProductType.EXOTIC_OPTION;
    }
    
    public boolean isVanillaOption() {
        return productType == ProductType.VANILLA_OPTION;
    }
    
    public boolean isExoticOption() {
        return productType == ProductType.EXOTIC_OPTION;
    }
    
    public boolean isSwap() {
        return productType == ProductType.FX_SWAP || 
               productType == ProductType.CURRENCY_SWAP || 
               productType == ProductType.INTEREST_RATE_SWAP;
    }
    
    public boolean isFXProduct() {
        return productType == ProductType.FX_FORWARD || 
               productType == ProductType.FX_SPOT || 
               productType == ProductType.FX_SWAP;
    }
}