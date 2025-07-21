package org.george.fxoptiontradebooking.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.george.fxoptiontradebooking.dto.request.TradeBookingRequest;
import org.george.fxoptiontradebooking.dto.response.TradeResponse;
import org.george.fxoptiontradebooking.entity.Counterparty;
import org.george.fxoptiontradebooking.entity.Trade;
import org.george.fxoptiontradebooking.entity.TradeStatus;
import org.george.fxoptiontradebooking.exception.BusinessValidationException;
import org.george.fxoptiontradebooking.exception.TradeNotFoundException;
import org.george.fxoptiontradebooking.repository.CounterpartyRepository;
import org.george.fxoptiontradebooking.repository.TradeRepository;
import org.george.fxoptiontradebooking.service.TradeService;
import org.george.fxoptiontradebooking.service.ValidationService;
import org.george.fxoptiontradebooking.util.TradeCalculationUtils;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of the TradeService interface for managing FX option trades.
 * 
 * Provides comprehensive business logic for the entire trade lifecycle including:
 * - Trade booking and validation
 * - Trade retrieval and querying
 * - Status management and transitions
 * - Trade cancellation
 * 
 * This service follows investment banking practices for FX option trading,
 * including proper validation, audit trails, and lifecycle event handling.
 * All operations are transactional to ensure data consistency.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TradeServiceImpl implements TradeService {

    /** Repository for trade data access operations */
    private final TradeRepository tradeRepository;

    /** Repository for counterparty data access operations */
    private final CounterpartyRepository counterpartyRepository;

    /** Service for validating trade data according to business rules */
    private final ValidationService validationService;

    /** Mapper for entity-to-DTO and DTO-to-entity conversions */
    private final ModelMapper modelMapper;

    /**
     * Books a new FX option trade in the system.
     * Implements the complete trade booking workflow including:
     * - Multi-level validation (basic, counterparty, business rules)
     * - Entity creation and data normalization
     * - Premium calculation if not provided
     * - Persistence with audit trail
     * - Post-trade processing
     * 
     * @param request The trade booking request containing all trade details
     * @return The newly created trade data with generated IDs and calculated fields
     * @throws BusinessValidationException if validation fails at any stage
     */
    @Override
    public TradeResponse bookTrade(TradeBookingRequest request) {
        // Add null check FIRST, before any other processing to prevent NullPointerExceptions
        if (request == null) {
            throw new BusinessValidationException("Trade booking request cannot be null");
        }

        log.info("Booking new trade with reference: {}", request.getTradeReference());

        try {
            // Step 1: Pre-trade validation (basic field validation)
            performPreTradeValidation(request);

            // Step 2: Counterparty and credit limit validation (ensures counterparty exists and is eligible)
            Counterparty counterparty = validateAndGetCounterparty(request.getCounterpartyId());

            // Step 3: Business rules validation (comprehensive trade data validation)
            validationService.validateTradeRequest(request);

            // Step 4: Check for duplicate trade reference (ensures uniqueness)
            validateUniqueTradeReference(request.getTradeReference());

            // Step 5: Create and populate trade entity (maps request to entity with normalization)
            Trade trade = createTradeEntity(request, counterparty);

            // Step 6: Apply business logic (calculate premium if not provided, apply risk adjustments)
            applyBusinessLogic(trade, request);

            // Step 7: Persist trade (save with audit information)
            Trade savedTrade = saveTradeWithAudit(trade);

            // Step 8: Post-trade processing (risk notifications, regulatory reporting flags)
            performPostTradeProcessing(savedTrade);

            log.info("Successfully booked trade with ID: {} and reference: {}",
                    savedTrade.getTradeId(), savedTrade.getTradeReference());

            return modelMapper.map(savedTrade, TradeResponse.class);

        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation while booking trade: {}", e.getMessage());
            throw new BusinessValidationException("Failed to book trade due to data constraint violation");
        } catch (Exception e) {
            log.error("Unexpected error while booking trade: {}", e.getMessage(), e);
            throw new BusinessValidationException("Failed to book trade: " + e.getMessage());
        }
    }

    /**
     * Retrieves a trade by its unique database ID.
     * This method is typically used for internal system operations and API endpoints
     * where the internal ID is known.
     * 
     * The operation is read-only and will not modify any trade data.
     *
     * @param tradeId The unique database identifier of the trade
     * @return The trade information in response format
     * @throws TradeNotFoundException if no trade exists with the specified ID
     */
    @Override
    @Transactional(readOnly = true)
    public TradeResponse getTradeById(Long tradeId) {
        log.debug("Retrieving trade by ID: {}", tradeId);

        // Find the trade by ID or throw an exception if not found
        Trade trade = tradeRepository.findById(tradeId)
            .orElseThrow(() -> new TradeNotFoundException("Trade not found with ID: " + tradeId));

        // Convert entity to response DTO
        return modelMapper.map(trade, TradeResponse.class);
    }

    /**
     * Retrieves a trade by its business reference number.
     * Trade references are unique business identifiers used across systems and
     * in communications with counterparties. This lookup method is typically used
     * for business operations and external inquiries.
     * 
     * The operation is read-only and will not modify any trade data.
     *
     * @param tradeReference The unique business reference of the trade
     * @return The trade information in response format
     * @throws BusinessValidationException if the reference is null or empty
     * @throws TradeNotFoundException if no trade exists with the specified reference
     */
    @Override
    @Transactional(readOnly = true)
    public TradeResponse getTradeByReference(String tradeReference) {
        log.debug("Retrieving trade by reference: {}", tradeReference);

        // Validate the trade reference
        if (tradeReference == null || tradeReference.trim().isEmpty()) {
            throw new BusinessValidationException("Trade reference cannot be null or empty");
        }

        // Find the trade by reference or throw an exception if not found
        // The reference is trimmed to handle potential whitespace issues
        Trade trade = tradeRepository.findByTradeReference(tradeReference.trim())
            .orElseThrow(() -> new TradeNotFoundException("Trade not found with reference: " + tradeReference));

        // Convert entity to response DTO
        return modelMapper.map(trade, TradeResponse.class);
    }

    /**
     * Retrieves all trades for a specific counterparty.
     * This method is used for counterparty exposure reporting, trade reconciliation,
     * and relationship management functions.
     * 
     * The operation returns all trades regardless of status, providing a complete
     * view of the trading relationship with the specified counterparty.
     * 
     * @param counterpartyId The unique identifier of the counterparty
     * @return A list of all trades with the specified counterparty
     * @throws BusinessValidationException if the counterparty ID is invalid
     * @throws TradeNotFoundException if the counterparty doesn't exist
     */
    @Override
    @Transactional(readOnly = true)
    public List<TradeResponse> getTradesByCounterparty(Long counterpartyId) {
        log.debug("Retrieving trades for counterparty ID: {}", counterpartyId);

        // Validate counterparty ID
        if (counterpartyId == null || counterpartyId <= 0) {
            throw new BusinessValidationException("Valid counterparty ID is required");
        }

        // Verify counterparty exists before searching for trades
        // This provides a more specific error message if the counterparty doesn't exist
        if (!counterpartyRepository.existsById(counterpartyId)) {
            throw new TradeNotFoundException("Counterparty not found with ID: " + counterpartyId);
        }

        // Retrieve all trades for the counterparty
        List<Trade> trades = tradeRepository.findByCounterpartyCounterpartyId(counterpartyId);

        // Convert entities to response DTOs
        return trades.stream()
            .map(trade -> modelMapper.map(trade, TradeResponse.class))
            .collect(Collectors.toList());
    }

    /**
     * Retrieves all trades with a specific status.
     * This method supports operational workflows such as:
     * - Finding pending trades that need confirmation
     * - Retrieving settled trades for reconciliation
     * - Identifying cancelled trades for reporting
     * 
     * The operation is read-only and returns trades across all counterparties.
     *
     * @param status The trade status to filter by (PENDING, CONFIRMED, SETTLED, etc.)
     * @return A list of all trades with the specified status
     * @throws BusinessValidationException if the status is null
     */
    @Override
    @Transactional(readOnly = true)
    public List<TradeResponse> getTradesByStatus(TradeStatus status) {
        log.debug("Retrieving trades with status: {}", status);

        // Validate status parameter
        if (status == null) {
            throw new BusinessValidationException("Trade status cannot be null");
        }

        // Retrieve all trades with the specified status
        List<Trade> trades = tradeRepository.findByStatus(status);

        // Convert entities to response DTOs
        return trades.stream()
            .map(trade -> modelMapper.map(trade, TradeResponse.class))
            .collect(Collectors.toList());
    }

    /**
     * Retrieves all trades executed within a specified date range.
     * This method supports reporting and analysis functions such as:
     * - Daily trading activity reports
     * - Period-based performance analysis
     * - Audit and compliance reviews
     * 
     * The date range is limited to one year to prevent excessive queries
     * and resource consumption, which is a common practice in financial systems.
     *
     * @param startDate The beginning of the date range (inclusive)
     * @param endDate The end of the date range (inclusive)
     * @return A list of trades executed within the specified date range
     * @throws BusinessValidationException if dates are invalid or range exceeds 1 year
     */
    @Override
    @Transactional(readOnly = true)
    public List<TradeResponse> getTradesByDateRange(LocalDate startDate, LocalDate endDate) {
        log.debug("Retrieving trades between {} and {}", startDate, endDate);

        // Validate that both dates are provided
        if (startDate == null || endDate == null) {
            throw new BusinessValidationException("Start date and end date are required");
        }

        // Ensure date range is valid (start before end)
        if (startDate.isAfter(endDate)) {
            throw new BusinessValidationException("Start date cannot be after end date");
        }

        // Limit date range to prevent excessive queries (investment bank best practice)
        // Large queries can impact system performance and are rarely needed for business operations
        if (startDate.plusYears(1).isBefore(endDate)) {
            throw new BusinessValidationException("Date range cannot exceed 1 year");
        }

        // Retrieve trades within the date range based on trade date
        List<Trade> trades = tradeRepository.findByTradeDateBetween(startDate, endDate);

        // Convert entities to response DTOs
        return trades.stream()
            .map(trade -> modelMapper.map(trade, TradeResponse.class))
            .collect(Collectors.toList());
    }

    /**
     * Retrieves all trades with pagination support.
     * This method provides a scalable way to access the full trade history
     * without loading all trades into memory at once, which is essential for
     * systems with large trade volumes.
     * 
     * Pagination parameters control:
     * - Page size (number of trades per page)
     * - Page number (which page to retrieve)
     * - Sorting (field and direction)
     *
     * @param pageable Pagination and sorting parameters
     * @return A page of trades according to the pagination parameters
     */
    @Override
    @Transactional(readOnly = true)
    public Page<TradeResponse> getAllTrades(Pageable pageable) {
        log.debug("Retrieving all trades with pagination: {}", pageable);

        // Retrieve a page of trades using the repository's pageable support
        Page<Trade> trades = tradeRepository.findAll(pageable);

        // Convert page of entities to page of response DTOs
        // This preserves pagination metadata like total pages, total elements, etc.
        return trades.map(trade -> modelMapper.map(trade, TradeResponse.class));
    }

    /**
     * Updates the status of an existing trade.
     * This method supports the trade lifecycle workflow by allowing status transitions
     * according to defined business rules.
     * 
     * Valid status transitions are enforced to maintain data integrity and
     * follow regulatory requirements for audit trails.
     * 
     * After a status change, appropriate business events are triggered through
     * the handleStatusChangeEvents method.
     *
     * @param tradeId The ID of the trade to update
     * @param newStatus The new status to set for the trade
     * @return The updated trade information
     * @throws BusinessValidationException if the trade ID or status is invalid
     * @throws TradeNotFoundException if the trade doesn't exist
     * @throws BusinessValidationException if the status transition is not allowed
     */
    @Override
    public TradeResponse updateTradeStatus(Long tradeId, TradeStatus newStatus) {
        log.info("Updating trade {} status to {}", tradeId, newStatus);

        // Validate input parameters
        if (tradeId == null || tradeId <= 0) {
            throw new BusinessValidationException("Valid trade ID is required");
        }

        if (newStatus == null) {
            throw new BusinessValidationException("New trade status cannot be null");
        }

        // Find the trade or throw exception if not found
        Trade trade = tradeRepository.findById(tradeId)
            .orElseThrow(() -> new TradeNotFoundException("Trade not found with ID: " + tradeId));

        // Validate that the status transition is allowed according to business rules
        validateStatusTransition(trade.getStatus(), newStatus);

        // Store old status for logging and event handling
        TradeStatus oldStatus = trade.getStatus();

        // Update and save the trade
        trade.setStatus(newStatus);
        Trade updatedTrade = tradeRepository.save(trade);

        log.info("Successfully updated trade {} status from {} to {}", tradeId, oldStatus, newStatus);

        // Trigger post-status-change processing (confirmations, settlements, etc.)
        handleStatusChangeEvents(updatedTrade, oldStatus, newStatus);

        return modelMapper.map(updatedTrade, TradeResponse.class);
    }

    /**
     * Cancels an existing trade.
     * In financial trading systems, cancellation is a controlled process with strict rules:
     * - Only PENDING trades can be cancelled
     * - Cancellation is typically only allowed on the same business day
     * 
     * These rules align with market practices and ensure proper audit trails
     * and regulatory compliance.
     *
     * @param tradeId The ID of the trade to cancel
     * @throws TradeNotFoundException if the trade doesn't exist
     * @throws BusinessValidationException if the trade cannot be cancelled due to its status or date
     */
    @Override
    public void cancelTrade(Long tradeId) {
        log.info("Cancelling trade with ID: {}", tradeId);

        // Find the trade or throw exception if not found
        Trade trade = tradeRepository.findById(tradeId)
            .orElseThrow(() -> new TradeNotFoundException("Trade not found with ID: " + tradeId));

        // Investment banks typically only allow cancellation of pending trades
        // This prevents cancelling trades that have already been processed or settled
        if (trade.getStatus() != TradeStatus.PENDING) {
            throw new BusinessValidationException("Cannot cancel trade in status: " + trade.getStatus());
        }

        // Check if trade is still within cancellation window (same business day)
        // This is a standard market practice to limit cancellations to the trade date
        if (!trade.getTradeDate().equals(LocalDate.now())) {
            throw new BusinessValidationException("Cannot cancel trade after trade date");
        }

        // Update trade status to CANCELLED and save
        trade.setStatus(TradeStatus.CANCELLED);
        tradeRepository.save(trade);

        log.info("Successfully cancelled trade with ID: {}", tradeId);
    }

    /**
     * Retrieves all trades involving a specific currency.
     * This method supports currency-based risk management, position reporting,
     * and regulatory reporting requirements.
     * 
     * The search includes trades where the specified currency appears as either
     * the base currency or quote currency, providing a complete view of exposure
     * to the given currency.
     *
     * @param currency The ISO currency code to search for (e.g., "USD", "EUR")
     * @return A list of all trades involving the specified currency
     * @throws BusinessValidationException if the currency is invalid
     */
    @Transactional(readOnly = true)
    public List<TradeResponse> getTradesByCurrency(String currency) {
        log.debug("Retrieving trades for currency: {}", currency);

        // Validate currency parameter
        if (currency == null || currency.trim().isEmpty()) {
            throw new BusinessValidationException("Currency cannot be null or empty");
        }

        // Normalize currency code for consistent searching
        String normalizedCurrency = currency.toUpperCase().trim();

        // Validate currency code format and existence
        validationService.validateCurrencyCode(normalizedCurrency);

        // Find trades where this currency appears (as base or quote currency)
        List<Trade> trades = tradeRepository.findByCurrency(normalizedCurrency);

        // Convert entities to response DTOs
        return trades.stream()
            .map(trade -> modelMapper.map(trade, TradeResponse.class))
            .collect(Collectors.toList());
    }

    // Private helper methods

    private void performPreTradeValidation(TradeBookingRequest request) {
        if (request == null) {
            throw new BusinessValidationException("Trade booking request cannot be null");
        }

        // Validate required fields are present
        if (request.getTradeReference() == null || request.getTradeReference().trim().isEmpty()) {
            throw new BusinessValidationException("Trade reference is required");
        }

        if (request.getCounterpartyId() == null) {
            throw new BusinessValidationException("Counterparty ID is required");
        }

        // Additional pre-trade checks can be added here
        log.debug("Pre-trade validation completed for trade reference: {}", request.getTradeReference());
    }

    private Counterparty validateAndGetCounterparty(Long counterpartyId) {
        Counterparty counterparty = counterpartyRepository.findById(counterpartyId)
            .orElseThrow(() -> new BusinessValidationException("Counterparty not found with ID: " + counterpartyId));

        // Investment bank validation: counterparty must be active
        if (!counterparty.getIsActive()) {
            throw new BusinessValidationException("Cannot trade with inactive counterparty: " + counterparty.getName());
        }

        // Additional counterparty validations (credit limits, regulatory checks, etc.)
        validateCounterpartyEligibility(counterparty);

        return counterparty;
    }

    private void validateCounterpartyEligibility(Counterparty counterparty) {
        // 1. Credit rating check for sub-investment grade counterparties
        if (counterparty.getCreditRating() != null) {
            String rating = counterparty.getCreditRating().trim();
            String[] subInvestmentGrade = {"BB+", "BB", "BB-", "B+", "B", "B-", "CCC+", "CCC", "CCC-", "CC", "C", "D"};

            for (String subRating : subInvestmentGrade) {
                if (subRating.equals(rating)) {
                    log.warn("Trading with sub-investment grade counterparty: {} (Rating: {})",
                            counterparty.getName(), rating);
                    break;
                }
            }
        }

        // 2. LEI code validation for regulatory compliance
        if (counterparty.getLeiCode() == null || counterparty.getLeiCode().trim().isEmpty()) {
            log.warn("Counterparty {} does not have LEI code - may impact regulatory reporting",
                    counterparty.getName());
        }

        log.debug("Counterparty eligibility validation completed for: {}", counterparty.getName());
    }

    private void validateUniqueTradeReference(String tradeReference) {
        if (tradeRepository.findByTradeReference(tradeReference.trim()).isPresent()) {
            throw new BusinessValidationException("Trade reference already exists: " + tradeReference);
        }
    }

    private Trade createTradeEntity(TradeBookingRequest request, Counterparty counterparty) {
        Trade trade = new Trade();

        // Basic trade information
        trade.setTradeReference(request.getTradeReference().trim());
        trade.setCounterparty(counterparty);
        trade.setBaseCurrency(request.getBaseCurrency().toUpperCase().trim());
        trade.setQuoteCurrency(request.getQuoteCurrency().toUpperCase().trim());
        trade.setNotionalAmount(request.getNotionalAmount());
        trade.setStrikePrice(request.getStrikePrice());
        trade.setSpotRate(request.getSpotRate());

        // Dates
        trade.setTradeDate(request.getTradeDate());
        trade.setValueDate(request.getValueDate());
        trade.setMaturityDate(request.getMaturityDate());

        // Option details
        trade.setOptionType(request.getOptionType());

        // Premium information
        trade.setPremiumAmount(request.getPremiumAmount());
        trade.setPremiumCurrency(request.getPremiumCurrency() != null ?
            request.getPremiumCurrency().toUpperCase().trim() : null);

        // Audit fields
        trade.setCreatedBy(request.getCreatedBy() != null ? request.getCreatedBy() : "SYSTEM");

        // Status is set to PENDING by default in @PrePersist

        return trade;
    }

    private void applyBusinessLogic(Trade trade, TradeBookingRequest request) {
        // 1. Calculate premium if not provided (simplified calculation)
        if (trade.getPremiumAmount() == null && trade.getSpotRate() != null) {
            BigDecimal calculatedPremium = calculateDefaultPremium(trade);
            trade.setPremiumAmount(calculatedPremium);

            // Set premium currency to quote currency if not specified
            if (trade.getPremiumCurrency() == null) {
                trade.setPremiumCurrency(trade.getQuoteCurrency());
            }

            log.info("Calculated default premium: {} {} for trade: {}",
                    calculatedPremium, trade.getPremiumCurrency(), trade.getTradeReference());
        }

        // 2. Additional business logic can be added here (risk calculations, hedging, etc.)
    }

    /**
     * Calculates a default premium for an option trade when not provided by the user.
     * 
     * This is a simplified calculation for demonstration purposes. In a real trading system,
     * this would use sophisticated option pricing models such as Black-Scholes or
     * Monte Carlo simulations that would factor in:
     * - Volatility of the currency pair
     * - Interest rates in both currencies
     * - Forward rates
     * - Time to maturity
     * - Strike price vs. spot price
     * - Option type (call/put)
     * 
     * The current implementation uses a simple percentage of notional adjusted for tenor.
     *
     * @param trade The trade for which to calculate the premium
     * @return The calculated premium amount
     */
    private BigDecimal calculateDefaultPremium(Trade trade) {
        // Simplified premium calculation (in real investment bank systems, this would use
        // sophisticated option pricing models like Black-Scholes)

        // Base premium rate - 2% of notional as default
        BigDecimal premiumRate = new BigDecimal("0.02"); 

        // Adjust premium based on time to maturity
        // Longer-term options typically have higher premiums due to increased time value
        long daysToMaturity = java.time.temporal.ChronoUnit.DAYS.between(
            trade.getValueDate(), trade.getMaturityDate()
        );

        // Tenor-based premium adjustments
        if (daysToMaturity > 365) {
            // Longer-term options (> 1 year) have higher premiums
            // Increased time value and uncertainty justifies 50% premium increase
            premiumRate = premiumRate.multiply(new BigDecimal("1.5")); 
        } else if (daysToMaturity < 30) {
            // Short-term options (< 30 days) have lower premiums
            // Reduced time value justifies 50% premium reduction
            premiumRate = premiumRate.multiply(new BigDecimal("0.5")); 
        }

        // Calculate final premium amount based on notional and adjusted rate
        return TradeCalculationUtils.calculatePremium(trade.getNotionalAmount(), premiumRate);
    }

    private Trade saveTradeWithAudit(Trade trade) {
        try {
            Trade savedTrade = tradeRepository.save(trade);

            // Log audit information
            log.info("Trade audit - ID: {}, Reference: {}, Counterparty: {}, Notional: {} {}, Status: {}",
                    savedTrade.getTradeId(),
                    savedTrade.getTradeReference(),
                    savedTrade.getCounterparty().getName(),
                    savedTrade.getNotionalAmount(),
                    savedTrade.getBaseCurrency(),
                    savedTrade.getStatus());

            return savedTrade;

        } catch (Exception e) {
            log.error("Failed to save trade with reference: {}", trade.getTradeReference(), e);
            throw e;
        }
    }

    private void performPostTradeProcessing(Trade trade) {
        // Investment bank post-trade processing

        // 1. Risk management notifications for large trades
        if (trade.getNotionalAmount().compareTo(new BigDecimal("50000000")) > 0) {
            log.warn("Large trade notification - Trade ID: {}, Notional: {} {}",
                    trade.getTradeId(), trade.getNotionalAmount(), trade.getBaseCurrency());
        }

        // 2. Regulatory reporting preparation
        if (trade.getCounterparty().getLeiCode() != null) {
            log.debug("Trade {} ready for regulatory reporting with counterparty LEI: {}",
                    trade.getTradeReference(), trade.getCounterparty().getLeiCode());
        }

        // 3. Additional post-trade processes can be added here
        // (confirmations, settlement instructions, hedge calculations, etc.)
    }

    /**
     * Validates if a status transition is allowed according to business rules.
     * Enforces the workflow state machine for trade lifecycle management:
     * - PENDING trades can transition to any status
     * - CONFIRMED trades cannot revert to PENDING
     * - SETTLED, CANCELLED, and EXPIRED are terminal states that cannot change
     * 
     * These rules ensure proper audit trail and prevent invalid state changes
     * that would violate regulatory and operational requirements.
     *
     * @param currentStatus The current status of the trade
     * @param newStatus The proposed new status for the trade
     * @throws BusinessValidationException if the status transition is not allowed
     */
    private void validateStatusTransition(TradeStatus currentStatus, TradeStatus newStatus) {
        // Investment bank status transition rules
        switch (currentStatus) {
            case PENDING:
                // PENDING can transition to any status
                // This is the initial state from which all transitions are valid
                break;
            case CONFIRMED:
                // Once confirmed, a trade cannot go back to pending
                // This prevents improper modifications after confirmation
                if (newStatus == TradeStatus.PENDING) {
                    throw new BusinessValidationException("Cannot revert confirmed trade to pending status");
                }
                break;
            case SETTLED:
                // Settled is a terminal state for trades
                // Once a trade is settled, its status cannot be changed
                if (newStatus != TradeStatus.SETTLED) {
                    throw new BusinessValidationException("Cannot change status of settled trade");
                }
                break;
            case CANCELLED:
                // Cancelled is a terminal state for trades
                // Once cancelled, a trade cannot be reinstated
                if (newStatus != TradeStatus.CANCELLED) {
                    throw new BusinessValidationException("Cannot change status of cancelled trade");
                }
                break;
            case EXPIRED:
                // Expired is a terminal state for trades
                // Once an option expires, its status cannot be changed
                if (newStatus != TradeStatus.EXPIRED) {
                    throw new BusinessValidationException("Cannot change status of expired trade");
                }
                break;
            default:
                throw new BusinessValidationException("Unknown trade status: " + currentStatus);
        }
    }

    /**
     * Handles business events triggered by trade status changes.
     * Executes appropriate business processes based on the new status:
     * - CONFIRMED: Initiates settlement process
     * - SETTLED: Updates positions and sends confirmations
     * - CANCELLED: Releases credit limits and sends cancellation notices
     * - EXPIRED: Processes option expiry
     * 
     * This method serves as a central point for status-based event processing
     * and workflow management in the trade lifecycle.
     *
     * @param trade The trade that had a status change
     * @param oldStatus The previous status of the trade
     * @param newStatus The new status of the trade
     */
    private void handleStatusChangeEvents(Trade trade, TradeStatus oldStatus, TradeStatus newStatus) {
        // Handle specific status change events based on the new status

        switch (newStatus) {
            case CONFIRMED:
                log.info("Trade {} confirmed - initiating settlement process", trade.getTradeReference());
                // Trigger settlement workflow
                // In a complete system, this would:
                // - Generate settlement instructions
                // - Calculate settlement amounts
                // - Initiate payment processing
                break;

            case SETTLED:
                log.info("Trade {} settled successfully", trade.getTradeReference());
                // Update positions, send confirmations, etc.
                // In a complete system, this would:
                // - Update risk positions
                // - Send final confirmations to counterparty
                // - Record settlement details for accounting
                break;

            case CANCELLED:
                log.info("Trade {} cancelled from status {}", trade.getTradeReference(), oldStatus);
                // Release credit limits, send cancellation notices, etc.
                // In a complete system, this would:
                // - Release reserved credit limits
                // - Send cancellation notices to counterparty
                // - Update risk positions
                break;

            case EXPIRED:
                log.info("Trade {} expired", trade.getTradeReference());
                // Handle expiry processing
                // In a complete system, this would:
                // - Calculate expiry value (if any)
                // - Initiate settlement for in-the-money options
                // - Update risk positions
                break;

            default:
                // No special processing required for other statuses
                break;
        }
    }
}