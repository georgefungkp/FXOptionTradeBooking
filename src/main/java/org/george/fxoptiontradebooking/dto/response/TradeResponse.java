package org.george.fxoptiontradebooking.dto.response;

import lombok.Data;
import org.george.fxoptiontradebooking.entity.OptionType;
import org.george.fxoptiontradebooking.entity.TradeStatus;

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
    private LocalDate tradeDate;
    private LocalDate valueDate;
    private LocalDate maturityDate;
    private OptionType optionType;
    private TradeStatus status;
    private BigDecimal premiumAmount;
    private String premiumCurrency;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
