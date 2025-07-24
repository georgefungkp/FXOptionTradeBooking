package org.george.fxoptiontradebooking.repository;

import org.george.fxoptiontradebooking.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Trade entities with support for inheritance hierarchy.
 */
@Repository
public interface TradeRepository extends JpaRepository<Trade, Long> {
    
    // Basic queries
    Optional<Trade> findByTradeReference(String tradeReference);
    
    List<Trade> findByCounterparty_CounterpartyId(Long counterpartyId);
    Page<Trade> findByCounterparty_CounterpartyId(Long counterpartyId, Pageable pageable);
    
    List<Trade> findByStatus(TradeStatus status);
    
    List<Trade> findByTradeDateBetween(LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT t FROM Trade t WHERE t.baseCurrency = :currency OR t.quoteCurrency = :currency")
    List<Trade> findByCurrency(@Param("currency") String currency);
    
    // Product type queries using discriminator
    @Query("SELECT t FROM Trade t WHERE TYPE(t) = VanillaOptionTrade")
    List<Trade> findAllVanillaOptions();
    
    @Query("SELECT t FROM Trade t WHERE TYPE(t) = ExoticOptionTrade")
    List<Trade> findAllExoticOptions();
    
    @Query("SELECT t FROM Trade t WHERE TYPE(t) = FXTrade")
    List<Trade> findAllFXTrades();
    
    @Query("SELECT t FROM Trade t WHERE TYPE(t) = SwapTrade")
    List<Trade> findAllSwaps();
    
    // Option-specific queries
    @Query("SELECT vo FROM VanillaOptionTrade vo WHERE vo.maturityDate BETWEEN :startDate AND :endDate")
    List<VanillaOptionTrade> findVanillaOptionsExpiringBetween(
            @Param("startDate") LocalDate startDate, 
            @Param("endDate") LocalDate endDate);
    
    @Query("SELECT eo FROM ExoticOptionTrade eo WHERE eo.exoticOptionType = :exoticType")
    List<ExoticOptionTrade> findExoticOptionsByType(@Param("exoticType") ExoticOptionType exoticType);
    
    // FX-specific queries
    @Query("SELECT fx FROM FXTrade fx WHERE fx.valueDate BETWEEN :startDate AND :endDate AND fx.isSpotTrade = false")
    List<FXTrade> findFXForwardsMaturing(
            @Param("startDate") LocalDate startDate, 
            @Param("endDate") LocalDate endDate);
    
    // Swap-specific queries
    @Query("SELECT s FROM SwapTrade s WHERE s.swapType = :swapType")
    List<SwapTrade> findSwapsByType(@Param("swapType") SwapType swapType);
    
    @Query("SELECT s FROM SwapTrade s WHERE s.floatingRateIndex = :index")
    List<SwapTrade> findInterestRateSwapsByIndex(@Param("index") String floatingRateIndex);
}