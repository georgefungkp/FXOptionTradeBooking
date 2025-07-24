package org.george.fxoptiontradebooking.controller;

import io.micrometer.observation.annotation.Observed;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.george.fxoptiontradebooking.dto.request.TradeBookingRequest;
import org.george.fxoptiontradebooking.dto.response.ApiResponse;
import org.george.fxoptiontradebooking.dto.response.TradeResponse;
import org.george.fxoptiontradebooking.entity.*;
import org.george.fxoptiontradebooking.service.TradeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST controller for managing multi-product financial trades.
 * Provides comprehensive endpoints for booking, retrieving, updating, and cancelling trades
 * across various financial instruments including vanilla options, exotic options, 
 * FX contracts, and swap products.
 * 
 * All responses are wrapped in a consistent ApiResponse format for unified error handling
 * and include observability annotations for distributed tracing.
 */
@RestController
@RequestMapping("/api/v1/trades")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TradeController {
    
    private final TradeService tradeService;

    // ========================= CORE TRADE OPERATIONS =========================

    /**
     * Books a new financial trade in the system.
     * Supports multiple product types with comprehensive validation.
     * 
     * @param request The validated trade booking request containing all trade details
     * @return HTTP 201 Created with the newly booked trade data
     */
    @PostMapping
    @PreAuthorize("hasRole('TRADER') or hasRole('ADMIN')")
    @Observed(
        name = "trade.book",
        contextualName = "book-new-trade",
        lowCardinalityKeyValues = {
            "operation", "create",
            "resource", "trade",
            "endpoint", "/api/v1/trades",
            "method", "POST"
        }
    )
    public ResponseEntity<ApiResponse<TradeResponse>> bookTrade(@Valid @RequestBody TradeBookingRequest request) {
        TradeResponse response = tradeService.bookTrade(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Trade booked successfully", response));
    }

    /**
     * Retrieves a trade by its unique ID.
     */
    @GetMapping("/{tradeId}")
    @PreAuthorize("hasRole('USER') or hasRole('TRADER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TradeResponse>> getTradeById(@PathVariable Long tradeId) {
        TradeResponse response = tradeService.getTradeById(tradeId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    /**
     * Retrieves a trade by its unique reference number.
     */
    @GetMapping("/reference/{tradeReference}")
    @PreAuthorize("hasRole('USER') or hasRole('TRADER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TradeResponse>> getTradeByReference(@PathVariable String tradeReference) {
        TradeResponse response = tradeService.getTradeByReference(tradeReference);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Retrieves all trades with pagination support.
     */
    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('TRADER') or hasRole('ADMIN')")
    @Observed(name = "trade.list", contextualName = "get-all-trades-paginated")
    public ResponseEntity<ApiResponse<Page<TradeResponse>>> getAllTrades(Pageable pageable) {
        Page<TradeResponse> responses = tradeService.getAllTrades(pageable);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Updates the status of an existing trade.
     */
    @PutMapping("/{tradeId}/status")
    @PreAuthorize("hasRole('TRADER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TradeResponse>> updateTradeStatus(
            @PathVariable Long tradeId, 
            @RequestParam TradeStatus status) {
        TradeResponse response = tradeService.updateTradeStatus(tradeId, status);
        return ResponseEntity.ok(ApiResponse.success("Trade status updated successfully", response));
    }

    /**
     * Cancels an existing trade.
     */
    @DeleteMapping("/{tradeId}")
    @PreAuthorize("hasRole('TRADER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Object>> cancelTrade(@PathVariable Long tradeId) {
        tradeService.cancelTrade(tradeId);
        return ResponseEntity.ok(ApiResponse.success("Trade cancelled successfully", null));
    }

    // ========================= COUNTERPARTY QUERIES =========================

    /**
     * Retrieves all trades for a specific counterparty.
     */
    @GetMapping("/counterparty/{counterpartyId}")
    @PreAuthorize("hasRole('TRADER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<TradeResponse>>> getTradesByCounterparty(@PathVariable Long counterpartyId) {
        List<TradeResponse> responses = tradeService.getTradesByCounterparty(counterpartyId);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    // ========================= STATUS QUERIES =========================

    /**
     * Retrieves all trades with a specific status.
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('USER') or hasRole('TRADER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<TradeResponse>>> getTradesByStatus(@PathVariable TradeStatus status) {
        List<TradeResponse> responses = tradeService.getTradesByStatus(status);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    // ========================= FX CONTRACT QUERIES =========================

    /**
     * Retrieves all FX contracts (forwards and spots).
     */
    @GetMapping("/fx-contracts")
    @PreAuthorize("hasRole('USER') or hasRole('TRADER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<TradeResponse>>> getAllFXContracts() {
        List<TradeResponse> responses = tradeService.getAllFXContracts();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Retrieves FX forwards maturing between specified dates.
     */
    @GetMapping("/fx-forwards/maturing")
    @PreAuthorize("hasRole('TRADER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<TradeResponse>>> getFXForwardsMaturing(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        List<TradeResponse> responses = tradeService.getFXForwardsMaturing(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    // ========================= SWAP QUERIES =========================

    /**
     * Retrieves all swap products.
     */
    @GetMapping("/swaps")
    @PreAuthorize("hasRole('USER') or hasRole('TRADER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<TradeResponse>>> getAllSwaps() {
        List<TradeResponse> responses = tradeService.getAllSwaps();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Retrieves swaps by type.
     */
    @GetMapping("/swaps/{swapType}")
    @PreAuthorize("hasRole('TRADER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<TradeResponse>>> getSwapsByType(@PathVariable SwapType swapType) {
        List<TradeResponse> responses = tradeService.getSwapsByType(swapType);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Retrieves interest rate swaps by floating rate index.
     */
    @GetMapping("/swaps/interest-rate/index/{index}")
    @PreAuthorize("hasRole('TRADER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<TradeResponse>>> getInterestRateSwapsByIndex(@PathVariable String index) {
        List<TradeResponse> responses = tradeService.getInterestRateSwapsByIndex(index);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    // ========================= OPTION QUERIES =========================

    /**
     * Retrieves vanilla options expiring between specified dates.
     */
    @GetMapping("/options/vanilla/expiring")
    @PreAuthorize("hasRole('TRADER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<TradeResponse>>> getVanillaOptionsExpiringBetween(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        List<TradeResponse> responses = tradeService.getVanillaOptionsExpiringBetween(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Retrieves exotic options by type.
     */
    @GetMapping("/options/exotic/{exoticType}")
    @PreAuthorize("hasRole('TRADER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<TradeResponse>>> getExoticOptionsByType(@PathVariable ExoticOptionType exoticType) {
        List<TradeResponse> responses = tradeService.getExoticOptionsByType(exoticType);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    // ========================= PRODUCT TYPE QUERIES =========================

    /**
     * Retrieves trades by product type.
     */
    @GetMapping("/product-type/{productType}")
    @PreAuthorize("hasRole('USER') or hasRole('TRADER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<TradeResponse>>> getTradesByProductType(@PathVariable ProductType productType) {
        List<TradeResponse> responses = tradeService.getTradesByProductType(productType);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Retrieves trades by product type with pagination.
     */
    @GetMapping("/product-type/{productType}/paginated")
    @PreAuthorize("hasRole('USER') or hasRole('TRADER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<TradeResponse>>> getTradesByProductTypePaginated(
            @PathVariable ProductType productType, 
            Pageable pageable) {
        Page<TradeResponse> responses = tradeService.getTradesByProductTypePaginated(productType, pageable);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    // ========================= CURRENCY QUERIES =========================

    /**
     * Retrieves trades by currency.
     */
    @GetMapping("/currency/{currency}")
    @PreAuthorize("hasRole('TRADER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<TradeResponse>>> getTradesByCurrency(@PathVariable String currency) {
        List<TradeResponse> responses = tradeService.getTradesByCurrency(currency);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Retrieves trades by date range.
     */
    @GetMapping("/date-range")
    @PreAuthorize("hasRole('TRADER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<TradeResponse>>> getTradesByDateRange(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        List<TradeResponse> responses = tradeService.getTradesByDateRange(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Retrieves trades by counterparty with pagination.
     */
    @GetMapping("/counterparty/{counterpartyId}/paginated")
    @PreAuthorize("hasRole('TRADER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<TradeResponse>>> getTradesByCounterpartyPaginated(
            @PathVariable Long counterpartyId, 
            Pageable pageable) {
        Page<TradeResponse> responses = tradeService.getTradesByCounterpartyPaginated(counterpartyId, pageable);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
}