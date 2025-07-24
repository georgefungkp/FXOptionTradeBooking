package org.george.fxoptiontradebooking.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Entity representing vanilla FX option trades.
 */
@Entity
@Table(name = "vanilla_option_trades")
@DiscriminatorValue("VANILLA_OPTION")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class VanillaOptionTrade extends OptionTrade {
    
    // Vanilla options may have additional specific fields in the future
    // For now, they inherit all fields from OptionTrade
    
    @Override
    public ProductType getProductType() {
        return ProductType.VANILLA_OPTION;
    }
}
