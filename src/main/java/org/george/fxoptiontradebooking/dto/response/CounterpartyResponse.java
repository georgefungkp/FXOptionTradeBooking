package org.george.fxoptiontradebooking.dto.response;

import lombok.Data;

@Data
public class CounterpartyResponse {
    private Long counterpartyId;
    private String counterpartyCode;
    private String name;
    private String leiCode;
    private String swiftCode;
    private String creditRating;
    private Boolean isActive;
}
