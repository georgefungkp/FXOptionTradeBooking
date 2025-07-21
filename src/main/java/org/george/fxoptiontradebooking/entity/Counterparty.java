package org.george.fxoptiontradebooking.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "counterparties")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Counterparty {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "counterparty_id")
    private Long counterpartyId;
    
    @Column(name = "counterparty_code", unique = true, nullable = false)
    private String counterpartyCode;
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "lei_code")
    private String leiCode;
    
    @Column(name = "swift_code")
    private String swiftCode;
    
    @Column(name = "credit_rating")
    private String creditRating;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @OneToMany(mappedBy = "counterparty", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Trade> trades;
    
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
