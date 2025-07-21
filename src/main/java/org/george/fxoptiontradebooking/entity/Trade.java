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
    
    @Column(name = "base_currency", nullable = false, length = 3)
    private String baseCurrency;
    
    @Column(name = "quote_currency", nullable = false, length = 3)
    private String quoteCurrency;
    
    @Column(name = "notional_amount", nullable = false, precision = 19, scale = 2)
    @NotNull
    @Positive
    private BigDecimal notionalAmount;
    
    @Column(name = "strike_price", nullable = false, precision = 19, scale = 6)
    @NotNull
    @Positive
    private BigDecimal strikePrice;
    
    @Column(name = "spot_rate", precision = 19, scale = 6)
    private BigDecimal spotRate;
    
    @Column(name = "trade_date", nullable = false)
    private LocalDate tradeDate;
    
    @Column(name = "value_date", nullable = false)
    private LocalDate valueDate;
    
    @Column(name = "maturity_date", nullable = false)
    private LocalDate maturityDate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "option_type", nullable = false)
    private OptionType optionType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "trade_status", nullable = false)
    private TradeStatus status;
    
    @Column(name = "premium_amount", precision = 19, scale = 2)
    private BigDecimal premiumAmount;
    
    @Column(name = "premium_currency", length = 3)
    private String premiumCurrency;
    
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
}
