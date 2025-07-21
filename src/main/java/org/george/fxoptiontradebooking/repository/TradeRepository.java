package org.george.fxoptiontradebooking.repository;

import org.george.fxoptiontradebooking.entity.Trade;
import org.george.fxoptiontradebooking.entity.TradeStatus;
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
 * Repository interface for accessing and managing FX option trade data.
 * 
 * This repository provides data access methods for the Trade entity, including standard
 * CRUD operations inherited from JpaRepository and custom query methods specific to
 * FX option trading requirements. It supports various business operations such as:
 * 
 * - Trade retrieval by business identifiers and properties
 * - Filtering trades by status, date, counterparty, and currency
 * - Pagination and sorting for large result sets
 * - Statistical queries for reporting and analysis
 * 
 * The repository leverages Spring Data JPA's method naming conventions for simple queries
 * and explicit JPQL queries with @Query annotations for more complex operations.
 */
@Repository
public interface TradeRepository extends JpaRepository<Trade, Long> {
    
    /**
     * Finds a trade by its unique business reference.
     * 
     * The trade reference is a business identifier used across systems and in
     * communications with counterparties. It must be unique within the system.
     * 
     * @param tradeReference The unique business reference of the trade
     * @return An Optional containing the trade if found, or empty if not found
     */
    Optional<Trade> findByTradeReference(String tradeReference);

    /**
     * Finds all trades associated with a specific counterparty.
     * 
     * This method is used for counterparty exposure analysis, relationship
     * management, and reconciliation purposes.
     * 
     * @param counterpartyId The unique identifier of the counterparty
     * @return A list of all trades with the specified counterparty
     */
    List<Trade> findByCounterpartyCounterpartyId(Long counterpartyId);

    /**
     * Finds all trades with a specific status.
     * 
     * This method supports operational workflows such as finding pending trades
     * that need confirmation, or settled trades for reconciliation.
     * 
     * @param status The trade status to filter by (PENDING, CONFIRMED, SETTLED, etc.)
     * @return A list of all trades with the specified status
     */
    List<Trade> findByStatus(TradeStatus status);

    /**
     * Finds all trades executed within a specific date range.
     * 
     * This method supports reporting and analysis functions, allowing filtering
     * of trades based on their execution date.
     * 
     * @param startDate The beginning of the date range (inclusive)
     * @param endDate The end of the date range (inclusive)
     * @return A list of trades executed within the specified date range
     */
    @Query("SELECT t FROM Trade t WHERE t.tradeDate BETWEEN :startDate AND :endDate")
    List<Trade> findByTradeDateBetween(@Param("startDate") LocalDate startDate, 
                                       @Param("endDate") LocalDate endDate);

    /**
     * Finds all trades involving a specific currency.
     * 
     * This method searches for trades where the specified currency appears as either
     * the base currency or quote currency, allowing for currency-based risk management
     * and position reporting.
     * 
     * @param currency The ISO currency code to search for (e.g., "USD", "EUR")
     * @return A list of all trades involving the specified currency
     */
    @Query("SELECT t FROM Trade t WHERE t.baseCurrency = :currency OR t.quoteCurrency = :currency")
    List<Trade> findByCurrency(@Param("currency") String currency);

    /**
     * Finds trades for a specific counterparty with a specific status, with pagination.
     * 
     * This method combines counterparty and status filtering with pagination support,
     * making it suitable for displaying trades in user interfaces or generating
     * paginated reports.
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
     * This method supports operational reporting and monitoring, such as tracking
     * daily volumes of new trades or settlements.
     * 
     * @param status The trade status to count
     * @param date The trade date to filter by
     * @return The count of trades matching the criteria
     */
    @Query("SELECT COUNT(t) FROM Trade t WHERE t.status = :status AND t.tradeDate = :date")
    long countByStatusAndTradeDate(@Param("status") TradeStatus status, @Param("date") LocalDate date);
}
