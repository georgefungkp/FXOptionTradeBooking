package org.george.fxoptiontradebooking.repository;

import org.george.fxoptiontradebooking.entity.Counterparty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CounterpartyRepository extends JpaRepository<Counterparty, Long> {
    
    Optional<Counterparty> findByCounterpartyCode(String counterpartyCode);
    
    List<Counterparty> findByIsActiveTrue();
    
    Optional<Counterparty> findByLeiCode(String leiCode);
    
    List<Counterparty> findByNameContainingIgnoreCase(String name);
}
