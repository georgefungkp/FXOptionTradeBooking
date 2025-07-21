package org.george.fxoptiontradebooking.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.george.fxoptiontradebooking.entity.OptionType;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class TradeBookingRequest {
    
    @NotBlank(message = "Trade reference is required")
    private String tradeReference;
    
    @NotNull(message = "Counterparty ID is required")
    private Long counterpartyId;
    
    @NotBlank(message = "Base currency is required")
    @Size(min = 3, max = 3, message = "Currency code must be 3 characters")
    private String baseCurrency;
    
    @NotBlank(message = "Quote currency is required")
    @Size(min = 3, max = 3, message = "Currency code must be 3 characters")
    private String quoteCurrency;
    
    @NotNull(message = "Notional amount is required")
    @DecimalMin(value = "0.01", message = "Notional amount must be greater than 0")
    private BigDecimal notionalAmount;
    
    @NotNull(message = "Strike price is required")
    @DecimalMin(value = "0.000001", message = "Strike price must be greater than 0")
    private BigDecimal strikePrice;
    
    private BigDecimal spotRate;
    
    @NotNull(message = "Trade date is required")
    private LocalDate tradeDate;
    
    @NotNull(message = "Value date is required")
    @Future(message = "Value date must be in the future")
    private LocalDate valueDate;
    
    @NotNull(message = "Maturity date is required")
    @Future(message = "Maturity date must be in the future")
    private LocalDate maturityDate;
    
    @NotNull(message = "Option type is required")
    private OptionType optionType;
    
    private BigDecimal premiumAmount;
    
    private String premiumCurrency;
    
    private String createdBy;
}
