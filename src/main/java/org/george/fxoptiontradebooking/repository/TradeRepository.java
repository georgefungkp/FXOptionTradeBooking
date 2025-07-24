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
 * Repository interface for accessing and managing multi-product financial trade data.
 * 
 * This repository provides comprehensive data access methods for the Trade entity, supporting
 * multiple financial product types including vanilla options, exotic options, FX contracts,
 * and various swap instruments. It includes:
 * 
 * - Standard CRUD operations inherited from JpaRepository
 * - Product-specific query methods for different financial instruments
 * - Risk management and portfolio analysis queries
 * - Pagination and filtering capabilities for large datasets
 * - Custom JPQL queries for complex business requirements
 * 
 * The repository leverages Spring Data JPA's method naming conventions for simple queries
 * and explicit JPQL queries with @Query annotations for more complex operations, following
 * investment banking practices for trade data management.
 */
@Repository
public interface TradeRepository extends JpaRepository<Trade, Long> {
    
    // ========================= BASIC TRADE LOOKUPS =========================
    
    /**
     * Finds a trade by its unique business reference.
     * 
     * The trade reference is a business identifier used across systems and in
     * communications with counterparties. It must be unique within the system.
     * This method is commonly used for trade inquiries and reconciliation.
     * 
     * @param tradeReference The unique business reference of the trade
     * @return An Optional containing the trade if found, or empty if not found
     */
    Optional<Trade> findByTradeReference(String tradeReference);

    /**
     * Finds all trades associated with a specific counterparty.
     * 
     * This method is essential for counterparty exposure analysis, relationship
     * management, trade reconciliation, and regulatory reporting. It provides
     * a complete view of the trading relationship with a specific counterparty.
     * 
     * @param counterpartyId The unique identifier of the counterparty
     * @return A list of all trades with the specified counterparty
     */
    List<Trade> findByCounterpartyCounterpartyId(Long counterpartyId);

    /**
     * Finds all trades with a specific status.
     * 
     * This method supports operational workflows such as:
     * - Finding pending trades that need confirmation
     * - Retrieving settled trades for reconciliation
     * - Identifying cancelled trades for reporting
     * - Processing expired options
     * 
     * @param status The trade status to filter by (PENDING, CONFIRMED, SETTLED, etc.)
     * @return A list of all trades with the specified status
     */
    List<Trade> findByStatus(TradeStatus status);

    /**
     * Finds all trades executed within a specific date range.
     * 
     * This method supports reporting and analysis functions such as:
     * - Daily trading activity reports
     * - Period-based performance analysis
     * - Audit and compliance reviews
     * - Historical trade analysis
     * 
     * @param startDate The beginning of the date range (inclusive)
     * @param endDate The end of the date range (inclusive)
     * @return A list of trades executed within the specified date range
     */
    List<Trade> findByTradeDateBetween(LocalDate startDate, LocalDate endDate);
    
    /**
     * Checks if a trade reference already exists in the system.
     * 
     * This method is used for validation during trade booking to ensure
     * trade reference uniqueness, which is critical for trade identification
     * and regulatory compliance.
     * 
     * @param tradeReference The trade reference to check
     * @return true if the reference exists, false otherwise
     */
    boolean existsByTradeReference(String tradeReference);
    
    /**
     * Finds all trades involving a specific currency as either base or quote currency.
     * 
     * This method supports currency-based risk management, position analysis,
     * and regulatory reporting by providing exposure across all currency positions.
     * Essential for FX risk management and portfolio analysis.
     * 
     * @param currency The ISO currency code to search for (e.g., "USD", "EUR")
     * @return A list of all trades involving the specified currency
     */
    @Query("SELECT t FROM Trade t WHERE t.baseCurrency = :currency OR t.quoteCurrency = :currency")
    List<Trade> findTradesByCurrency(@Param("currency") String currency);
    
    // ========================= PRODUCT-SPECIFIC QUERIES =========================
    
    /**
     * Finds all trades of a specific product type.
     * 
     * This method enables product-specific analysis and reporting, allowing
     * segregation of different financial instruments for specialized processing,
     * risk management, and regulatory reporting requirements.
     * 
     * @param productType The product type to filter by (VANILLA_OPTION, EXOTIC_OPTION, etc.)
     * @return A list of trades of the specified product type
     */
    List<Trade> findByProductType(ProductType productType);
    
    /**
     * Finds all trades matching any of the specified product types.
     * 
     * This method supports portfolio analysis across multiple related product types,
     * such as all option products or all swap instruments, enabling comprehensive
     * risk analysis and reporting across product categories.
     * 
     * @param productTypes A list of product types to include in the search
     * @return A list of trades matching any of the specified product types
     */
    @Query("SELECT t FROM Trade t WHERE t.productType IN :productTypes")
    List<Trade> findByProductTypeIn(@Param("productTypes") List<ProductType> productTypes);
    
    // ========================= OPTION-SPECIFIC QUERIES =========================
    
    /**
     * Finds all options of a specific type (CALL or PUT).
     * 
     * This method supports option-specific analysis, including:
     * - Delta hedging calculations
     * - Option portfolio analysis
     * - Risk management for directional exposure
     * 
     * @param optionType The option type (CALL or PUT)
     * @return A list of all options of the specified type
     */
    List<Trade> findByOptionType(OptionType optionType);
    
    /**
     * Finds all exotic options of a specific exotic type.
     * 
     * This method supports specialized exotic option management, allowing
     * segregation by exotic features for specialized pricing, risk management,
     * and hedging strategies specific to each exotic option type.
     * 
     * @param exoticOptionType The exotic option type (BARRIER_OPTION, ASIAN_OPTION, etc.)
     * @return A list of all exotic options of the specified type
     */
    List<Trade> findByExoticOptionType(ExoticOptionType exoticOptionType);
    
    /**
     * Finds vanilla options expiring within a specific date range.
     * 
     * This method supports option expiry management, including:
     * - Identifying options approaching expiration
     * - Exercise decision workflows
     * - Expiry-based risk management
     * - Settlement processing preparation
     * 
     * @param startDate The beginning of the expiry date range (inclusive)
     * @param endDate The end of the expiry date range (inclusive)
     * @return A list of vanilla options expiring within the specified range
     */
    @Query("SELECT t FROM Trade t WHERE t.productType = 'VANILLA_OPTION' AND t.maturityDate BETWEEN :startDate AND :endDate")
    List<Trade> findVanillaOptionsExpiringBetween(@Param("startDate") LocalDate startDate, 
                                                  @Param("endDate") LocalDate endDate);
    
    /**
     * Finds exotic options by their specific exotic type.
     * 
     * This query provides more precise filtering for exotic options, supporting
     * specialized risk management, pricing model application, and hedging
     * strategies specific to each exotic option variant.
     * 
     * @param exoticType The specific exotic option type to filter by
     * @return A list of exotic options of the specified type
     */
    @Query("SELECT t FROM Trade t WHERE t.productType = 'EXOTIC_OPTION' AND t.exoticOptionType = :exoticType")
    List<Trade> findExoticOptionsByType(@Param("exoticType") ExoticOptionType exoticType);
    
    // ========================= SWAP-SPECIFIC QUERIES =========================
    
    /**
     * Finds all swaps of a specific swap type.
     * 
     * This method supports swap portfolio management, enabling analysis
     * and risk management specific to different swap structures and
     * their unique characteristics and risk profiles.
     * 
     * @param swapType The swap type to filter by (FX_SWAP, CURRENCY_SWAP, etc.)
     * @return A list of swaps of the specified type
     */
    List<Trade> findBySwapType(SwapType swapType);
    
    /**
     * Finds all swap instruments across all swap types.
     * 
     * This method provides a comprehensive view of the swap portfolio,
     * supporting overall swap risk management, regulatory reporting,
     * and portfolio analysis across all swap product variations.
     * 
     * @return A list of all swap trades
     */
    @Query("SELECT t FROM Trade t WHERE t.productType IN ('FX_SWAP', 'CURRENCY_SWAP', 'INTEREST_RATE_SWAP')")
    List<Trade> findAllSwaps();
    
    /**
     * Finds interest rate swaps referencing a specific floating rate index.
     * 
     * This method supports interest rate risk management by grouping swaps
     * that share the same reference rate, enabling comprehensive analysis
     * of exposure to specific rate benchmarks (SOFR, LIBOR, etc.).
     * 
     * @param floatingRateIndex The floating rate index (e.g., "SOFR", "LIBOR")
     * @return A list of interest rate swaps using the specified index
     */
    @Query("SELECT t FROM Trade t WHERE t.productType = 'INTEREST_RATE_SWAP' AND t.floatingRateIndex = :index")
    List<Trade> findInterestRateSwapsByIndex(@Param("index") String floatingRateIndex);
    
    // ========================= FX CONTRACT QUERIES =========================
    
    /**
     * Finds all FX forward and spot contracts.
     * 
     * This method supports FX portfolio management and risk analysis,
     * providing a comprehensive view of outright FX positions across
     * both spot and forward instruments.
     * 
     * @return A list of all FX forward and spot trades
     */
    @Query("SELECT t FROM Trade t WHERE t.productType IN ('FX_FORWARD', 'FX_SPOT')")
    List<Trade> findAllFXContracts();
    
    /**
     * Finds FX forwards maturing within a specific date range.
     * 
     * This method supports FX forward maturity management, including:
     * - Settlement preparation and cash flow planning
     * - Roll-over decision workflows
     * - Maturity-based risk management
     * - Forward curve analysis
     * 
     * @param startDate The beginning of the maturity date range (inclusive)
     * @param endDate The end of the maturity date range (inclusive)
     * @return A list of FX forwards maturing within the specified range
     */
    @Query("SELECT t FROM Trade t WHERE t.productType = 'FX_FORWARD' AND t.maturityDate BETWEEN :startDate AND :endDate")
    List<Trade> findFXForwardsMaturing(@Param("startDate") LocalDate startDate, 
                                       @Param("endDate") LocalDate endDate);
    
    // ========================= RISK & PORTFOLIO QUERIES =========================
    
    /**
     * Finds trades by base currency and specific product types.
     * 
     * This method supports sophisticated portfolio analysis by combining
     * currency and product type filters, enabling targeted risk analysis
     * for specific currency-product combinations essential for comprehensive
     * risk management strategies.
     * 
     * @param currency The base currency to filter by
     * @param productTypes The list of product types to include
     * @return A list of trades matching the currency and product type criteria
     */
    @Query("SELECT t FROM Trade t WHERE t.baseCurrency = :currency AND t.productType IN :productTypes")
    List<Trade> findByBaseCurrencyAndProductTypes(@Param("currency") String currency, 
                                                  @Param("productTypes") List<ProductType> productTypes);
    
    /**
     * Calculates the total confirmed exposure to a specific counterparty.
     * 
     * This method is critical for credit risk management, providing the
     * total notional amount of all confirmed trades with a counterparty.
     * Essential for credit limit monitoring and regulatory capital calculations.
     * Only includes CONFIRMED trades to exclude pending or cancelled positions.
     * 
     * @param counterpartyId The unique identifier of the counterparty
     * @return The total notional amount of confirmed trades with the counterparty
     */
    @Query("SELECT SUM(t.notionalAmount) FROM Trade t WHERE t.counterparty.counterpartyId = :counterpartyId AND t.status = 'CONFIRMED'")
    Long getTotalExposureByCounterparty(@Param("counterpartyId") Long counterpartyId);
    
    // ========================= PAGINATION SUPPORT =========================
    
    /**
     * Finds trades by product type with pagination support.
     * 
     * This method provides scalable access to product-specific trades,
     * essential for user interfaces and reports dealing with large volumes
     * of trades. Supports sorting and pagination for optimal performance.
     * 
     * @param productType The product type to filter by
     * @param pageable Pagination and sorting parameters
     * @return A page of trades of the specified product type
     */
    Page<Trade> findByProductType(ProductType productType, Pageable pageable);
    
    /**
     * Finds trades by counterparty with pagination support.
     * 
     * This method provides scalable access to counterparty-specific trades,
     * supporting user interfaces that display counterparty trade history
     * and relationship management tools with large datasets.
     * 
     * @param counterpartyId The unique identifier of the counterparty
     * @param pageable Pagination and sorting parameters
     * @return A page of trades for the specified counterparty
     */
    Page<Trade> findByCounterpartyCounterpartyId(Long counterpartyId, Pageable pageable);
    
    /**
     * Finds trades for a specific counterparty with a specific status, with pagination.
     * 
     * This method combines counterparty and status filtering with pagination support,
     * making it suitable for displaying filtered trades in user interfaces,
     * generating targeted reports, and supporting operational workflows
     * that require both counterparty and status-based filtering.
     * 
     * @param counterpartyId The unique identifier of the counterparty
     * @param status The trade status to filter by
     * @param pageable Pagination and sorting parameters
     * @return A page of trades matching the criteria
     */
    Page<Trade> findByCounterpartyCounterpartyIdAndStatus(Long counterpartyId, TradeStatus status, Pageable pageable);

    /**
     * Counts the number of trades with a specific status on a specific date.
     * 
     * This method supports operational reporting and monitoring, such as:
     * - Daily volume reporting by status
     * - Tracking settlement activities
     * - Monitoring trade booking patterns
     * - Supporting operational dashboards and KPI calculations
     * 
     * Returns a count rather than the full trade list for performance optimization
     * when only statistical information is needed.
     * 
     * @param status The trade status to count
     * @param date The trade date to filter by
     * @return The count of trades matching the criteria
     */
    @Query("SELECT COUNT(t) FROM Trade t WHERE t.status = :status AND t.tradeDate = :date")
    long countByStatusAndTradeDate(@Param("status") TradeStatus status, @Param("date") LocalDate date);
}