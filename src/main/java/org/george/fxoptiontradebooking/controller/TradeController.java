package org.george.fxoptiontradebooking.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.george.fxoptiontradebooking.dto.request.TradeBookingRequest;
import org.george.fxoptiontradebooking.dto.response.ApiResponse;
import org.george.fxoptiontradebooking.dto.response.TradeResponse;
import org.george.fxoptiontradebooking.entity.TradeStatus;
import org.george.fxoptiontradebooking.service.TradeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing FX option trades.
 * Provides endpoints for booking, retrieving, updating, and cancelling trades.
 * Handles all trade lifecycle operations through a standardized API interface.
 * All responses are wrapped in a consistent ApiResponse format for unified error handling.
 */
@RestController
@RequestMapping("/api/trades")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Allows cross-origin requests for development and testing
public class TradeController {
    
    /** Service for handling trade business logic operations */
    private final TradeService tradeService;

    /**
     * Books a new FX option trade in the system.
     * Validates the request and creates a new trade record if validation passes.
     * 
     * @param request The validated trade booking request containing all trade details
     * @return HTTP 201 Created with the newly booked trade data
     * @throws org.george.fxoptiontradebooking.exception.BusinessValidationException if validation fails
     */
    @PostMapping
    @PreAuthorize("hasRole('TRADER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TradeResponse>> bookTrade(@Valid @RequestBody TradeBookingRequest request) {
        TradeResponse response = tradeService.bookTrade(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Trade booked successfully", response));
    }

    /**
     * Retrieves a trade by its unique ID.
     * 
     * @param tradeId The ID of the trade to retrieve
     * @return HTTP 200 OK with the trade data if found
     * @throws org.george.fxoptiontradebooking.exception.TradeNotFoundException if the trade doesn't exist
     */
    @GetMapping("/{tradeId}")
    @PreAuthorize("hasRole('USER') or hasRole('TRADER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TradeResponse>> getTradeById(@PathVariable Long tradeId) {
        TradeResponse response = tradeService.getTradeById(tradeId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    /**
     * Retrieves a trade by its unique reference number.
     * 
     * @param tradeReference The reference number of the trade to retrieve
     * @return HTTP 200 OK with the trade data if found
     * @throws org.george.fxoptiontradebooking.exception.TradeNotFoundException if the trade doesn't exist
     * @throws org.george.fxoptiontradebooking.exception.BusinessValidationException if reference is invalid
     */
    @GetMapping("/reference/{tradeReference}")
    @PreAuthorize("hasRole('USER') or hasRole('TRADER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TradeResponse>> getTradeByReference(@PathVariable String tradeReference) {
        TradeResponse response = tradeService.getTradeByReference(tradeReference);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Retrieves all trades for a specific counterparty.
     * 
     * Access: TRADER and ADMIN roles only (sensitive counterparty data)
     * 
     * @param counterpartyId The ID of the counterparty to filter trades by
     * @return HTTP 200 OK with a list of trades for the specified counterparty
     * @throws org.george.fxoptiontradebooking.exception.TradeNotFoundException if the counterparty doesn't exist
     */
    @GetMapping("/counterparty/{counterpartyId}")
    @PreAuthorize("hasRole('TRADER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<TradeResponse>>> getTradesByCounterparty(@PathVariable Long counterpartyId) {
        List<TradeResponse> responses = tradeService.getTradesByCounterparty(counterpartyId);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Retrieves all trades with a specific status.
     * 
     * Access: USER, TRADER and ADMIN roles
     * 
     * @param status The trade status to filter by
     * @return HTTP 200 OK with a list of trades with the specified status
     * @throws org.george.fxoptiontradebooking.exception.BusinessValidationException if status is invalid
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('USER') or hasRole('TRADER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<TradeResponse>>> getTradesByStatus(@PathVariable TradeStatus status) {
        List<TradeResponse> responses = tradeService.getTradesByStatus(status);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Retrieves all trades with pagination support.
     * 
     * Access: USER, TRADER and ADMIN roles
     * 
     * @param pageable Pagination parameters (page, size, sort)
     * @return HTTP 200 OK with a page of trades according to pagination parameters
     */
    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('TRADER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<TradeResponse>>> getAllTrades(Pageable pageable) {
        Page<TradeResponse> responses = tradeService.getAllTrades(pageable);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Updates the status of an existing trade.
     * Status transitions are validated according to business rules.
     * 
     * Access: TRADER and ADMIN roles only (critical trade operation)
     * 
     * @param tradeId The ID of the trade to update
     * @param status The new status to set
     * @return HTTP 200 OK with the updated trade data
     * @throws org.george.fxoptiontradebooking.exception.TradeNotFoundException if the trade doesn't exist
     * @throws org.george.fxoptiontradebooking.exception.BusinessValidationException if the status transition is invalid
     */
    @PutMapping("/{tradeId}/status")
    @PreAuthorize("hasRole('TRADER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TradeResponse>> updateTradeStatus(
            @PathVariable Long tradeId, 
            @RequestParam TradeStatus status) {
        TradeResponse response = tradeService.updateTradeStatus(tradeId, status);
        return ResponseEntity.ok(ApiResponse.success("Trade status updated", response));
    }

    /**
     * Cancels an existing trade.
     * Only trades in PENDING status and on the same business day can be cancelled.
     * 
     * Access: TRADER and ADMIN roles only (critical trade operation)
     * 
     * @param tradeId The ID of the trade to cancel
     * @return HTTP 200 OK with cancellation confirmation
     * @throws org.george.fxoptiontradebooking.exception.TradeNotFoundException if the trade doesn't exist
     * @throws org.george.fxoptiontradebooking.exception.BusinessValidationException if the trade cannot be cancelled
     */
    @DeleteMapping("/{tradeId}")
    @PreAuthorize("hasRole('TRADER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Object>> cancelTrade(@PathVariable Long tradeId) {
        tradeService.cancelTrade(tradeId);
        return ResponseEntity.ok(ApiResponse.success("Trade cancelled successfully", null));
    }
}