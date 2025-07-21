package org.george.fxoptiontradebooking.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CounterpartyRequest {
    
    @NotBlank(message = "Counterparty code is required")
    private String counterpartyCode;
    
    @NotBlank(message = "Name is required")
    private String name;
    
    private String leiCode;
    
    private String swiftCode;
    
    private String creditRating;
    
    private Boolean isActive = true;
}
