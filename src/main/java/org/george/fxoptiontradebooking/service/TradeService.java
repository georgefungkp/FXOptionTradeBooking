package org.george.fxoptiontradebooking.service;

import org.george.fxoptiontradebooking.dto.request.TradeBookingRequest;
import org.george.fxoptiontradebooking.dto.response.TradeResponse;
import org.george.fxoptiontradebooking.entity.Trade;
import org.george.fxoptiontradebooking.entity.TradeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface TradeService {
    
    TradeResponse bookTrade(TradeBookingRequest request);
    
    TradeResponse getTradeById(Long tradeId);
    
    TradeResponse getTradeByReference(String tradeReference);
    
    List<TradeResponse> getTradesByCounterparty(Long counterpartyId);
    
    List<TradeResponse> getTradesByStatus(TradeStatus status);
    
    List<TradeResponse> getTradesByDateRange(LocalDate startDate, LocalDate endDate);
    
    Page<TradeResponse> getAllTrades(Pageable pageable);
    
    TradeResponse updateTradeStatus(Long tradeId, TradeStatus newStatus);
    
    void cancelTrade(Long tradeId);
    
    /**
     * Service interface for managing FX option trade operations.
     * Provides methods for booking, retrieving, and managing trade lifecycle events.
     */
    public interface TradeService {

        /**
         * Books a new FX option trade in the system.
         * 
         * @param request The trade booking request containing all trade details
         * @return The newly created trade data
         * @throws BusinessValidationException if validation fails
         */
        TradeResponse bookTrade(TradeBookingRequest request);

        /**
         * Retrieves a trade by its unique ID.
         * 
         * @param tradeId The ID of the trade to retrieve
         * @return The trade information
         * @throws TradeNotFoundException if the trade doesn't exist
         */
        TradeResponse getTradeById(Long tradeId);

        /**
         * Retrieves a trade by its unique reference number.
         * 
         * @param reference The reference number of the trade
         * @return The trade information
         * @throws TradeNotFoundException if the trade doesn't exist
         */
        TradeResponse getTradeByReference(String reference);

        /**
         * Retrieves all trades with pagination support.
         * 
         * @param pageable Pagination information
         * @return A page of trades
         */
        Page<TradeResponse> getAllTrades(Pageable pageable);

        /**
         * Updates the status of an existing trade.
         * 
         * @param tradeId The ID of the trade to update
         * @param status The new status to set
         * @return The updated trade information
         * @throws TradeNotFoundException if the trade doesn't exist
         * @throws BusinessValidationException if the status transition is invalid
         */
        TradeResponse updateTradeStatus(Long tradeId, TradeStatus status);

        /**
         * Cancels an existing trade.
         * 
         * @param tradeId The ID of the trade to cancel
         * @throws TradeNotFoundException if the trade doesn't exist
         * @throws BusinessValidationException if the trade cannot be canceled
         */
        void cancelTrade(Long tradeId);

        /**
         * Retrieves all trades for a specific counterparty.
         * 
         * @param counterpartyId The ID of the counterparty
         * @return A list of trades for the specified counterparty
         */
        List<TradeResponse> getTradesByCounterparty(Long counterpartyId);

        /**
         * Retrieves all trades with a specific status.
         * 
         * @param status The trade status to filter by
         * @return A list of trades with the specified status
         */
        List<TradeResponse> getTradesByStatus(TradeStatus status);

        /**
         * Retrieves all trades within a date range.
         * 
         * @param startDate The start date (inclusive)
         * @param endDate The end date (inclusive)
         * @return A list of trades within the specified date range
         */
        List<TradeResponse> getTradesByDateRange(LocalDate startDate, LocalDate endDate);

        /**
         * Retrieves all trades for a specific currency.
         * 
         * @param currency The currency code to filter by
         * @return A list of trades with the specified currency
         */
        List<TradeResponse> getTradesByCurrency(String currency);
}
