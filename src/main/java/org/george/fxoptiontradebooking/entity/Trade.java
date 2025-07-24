package org.george.fxoptiontradebooking.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Base Trade entity using inheritance hierarchy with Joined Table strategy.
 * Contains common fields shared across all trade types.
 */
@Entity
@Table(name = "trades")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "product_type", discriminatorType = DiscriminatorType.STRING)
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "tradeId")
public abstract class Trade {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trade_id")
    private Long tradeId;
    
    @Column(name = "trade_reference", unique = true, nullable = false)
    private String tradeReference;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "counterparty_id", nullable = false)
    private Counterparty counterparty;
    
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

    @Column(name = "value_date", nullable = false)
    private LocalDate valueDate;
    
    @Column(name = "maturity_date")
    private LocalDate maturityDate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "trade_status", nullable = false)
    private TradeStatus status;
    
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
    
    /**
     * Returns the product type for this trade.
     * Must be implemented by each concrete trade type.
     */
    public abstract ProductType getProductType();
    
    /**
     * Returns whether this trade is an option product.
     */
    public boolean isOption() {
        ProductType type = getProductType();
        return type == ProductType.VANILLA_OPTION || type == ProductType.EXOTIC_OPTION;
    }
    
    /**
     * Returns whether this trade is a swap product.
     */
    public boolean isSwap() {
        ProductType type = getProductType();
        return type == ProductType.FX_SWAP;
    }
    
    /**
     * Returns whether this trade is an FX product.
     */
    public boolean isFXProduct() {
        ProductType type = getProductType();
        return type == ProductType.FX_FORWARD || type == ProductType.FX_SPOT;
    }
}