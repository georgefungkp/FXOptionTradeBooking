package org.george.fxoptiontradebooking.dto.response;

import lombok.Data;
import org.george.fxoptiontradebooking.entity.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class TradeResponse {
    
    private Long tradeId;
    private String tradeReference;
    private CounterpartyResponse counterparty;
    private String baseCurrency;
    private String quoteCurrency;
    private BigDecimal notionalAmount;
    private BigDecimal strikePrice;
    private BigDecimal spotRate;
    private BigDecimal forwardRate;
    private BigDecimal barrierLevel;
    private LocalDate tradeDate;
    private LocalDate valueDate;
    private LocalDate maturityDate;
    private OptionType optionType;
    private ProductType productType;
    private ExoticOptionType exoticOptionType;
    private SwapType swapType;
    private TradeStatus status;
    private BigDecimal premiumAmount;
    private String premiumCurrency;
    private BigDecimal fixedRate;
    private String floatingRateIndex;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
