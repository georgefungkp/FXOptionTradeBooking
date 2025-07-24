package org.george.fxoptiontradebooking.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.george.fxoptiontradebooking.entity.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TradeBookingRequest {
    
    @NotBlank(message = "Trade reference is required")
    @Size(max = 50, message = "Trade reference must not exceed 50 characters")
    private String tradeReference;
    
    @NotNull(message = "Counterparty ID is required")
    @Positive(message = "Counterparty ID must be positive")
    private Long counterpartyId;
    
    @NotNull(message = "Product type is required")
    private ProductType productType;
    
    // Common fields
    @NotBlank(message = "Base currency is required")
    @Size(min = 3, max = 3, message = "Base currency must be exactly 3 characters")
    private String baseCurrency;
    
    @NotBlank(message = "Quote currency is required")  
    @Size(min = 3, max = 3, message = "Quote currency must be exactly 3 characters")
    private String quoteCurrency;
    
    @NotNull(message = "Notional amount is required")
    @DecimalMin(value = "10000.00", message = "Minimum notional amount is 10,000")
    @DecimalMax(value = "1000000000.00", message = "Maximum notional amount is 1 billion")
    private BigDecimal notionalAmount;
    
    @NotNull(message = "Trade date is required")
    private LocalDate tradeDate;
    
    @NotNull(message = "Value date is required")
    private LocalDate valueDate;
    
    private LocalDate maturityDate;
    
    // Option-specific fields
    private OptionType optionType;
    private ExoticOptionType exoticOptionType;
    
    @DecimalMin(value = "0.000001", message = "Strike price must be positive")
    private BigDecimal strikePrice;
    
    @DecimalMin(value = "0.01", message = "Premium amount must be positive")
    private BigDecimal premiumAmount;
    
    @Size(min = 3, max = 3, message = "Premium currency must be exactly 3 characters")
    private String premiumCurrency;
    
    // FX Forward/Spot fields
    @DecimalMin(value = "0.000001", message = "Forward rate must be positive")
    private BigDecimal forwardRate;
    
    @DecimalMin(value = "0.000001", message = "Spot rate must be positive")
    private BigDecimal spotRate;
    
    // Swap-specific fields
    private SwapType swapType;
    private BigDecimal nearLegAmount;
    private BigDecimal farLegAmount;
    private BigDecimal nearLegRate;
    private BigDecimal farLegRate;
    private LocalDate nearLegDate;
    private LocalDate farLegDate;
    
    // Exotic option fields
    private BigDecimal barrierLevel;
    private String knockInOut;
    private String observationFrequency;
    
    // Interest Rate Swap fields
    private BigDecimal fixedRate;
    private String floatingRateIndex;
    private String paymentFrequency;
    
    // Additional parameters for complex products
    private Map<String, Object> additionalParameters;
    
    private String createdBy;
}