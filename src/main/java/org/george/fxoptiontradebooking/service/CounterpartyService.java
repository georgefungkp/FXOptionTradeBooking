package org.george.fxoptiontradebooking.service;

import org.george.fxoptiontradebooking.dto.request.CounterpartyRequest;
import org.george.fxoptiontradebooking.dto.response.CounterpartyResponse;

import java.util.List;

/**
 * Service interface for managing counterparty entities in the FX option trade booking system.
 * Provides methods for creating, retrieving, updating, and deactivating counterparties.
 */
public interface CounterpartyService {

    /**
     * Creates a new counterparty in the system.
     * 
     * Performs validation on the counterparty data including checking for:
     * - Required fields (code, name)
     * - Format validation (LEI code, SWIFT code)
     * - Uniqueness constraints (code must be unique)
     * - Business rules (credit rating format)
     * 
     * New counterparties are created in active status by default unless specified otherwise.
     * 
     * @param request The counterparty details including code, name, and other identification information
     * @return A response containing the created counterparty details with generated ID
     * @throws org.george.fxoptiontradebooking.exception.BusinessValidationException if validation fails or if the counterparty already exists
     */
    CounterpartyResponse createCounterparty(CounterpartyRequest request);

    /**
     * Retrieves a counterparty by its unique identifier.
     * 
     * Provides complete counterparty information including:
     * - Basic identification (ID, code, name)
     * - Industry identifiers (LEI code, SWIFT code)
     * - Credit information (credit rating)
     * - Status information (active/inactive)
     * 
     * @param counterpartyId The unique identifier of the counterparty
     * @return The counterparty information in response format
     * @throws org.george.fxoptiontradebooking.exception.TradeNotFoundException if counterparty does not exist
     */
    CounterpartyResponse getCounterpartyById(Long counterpartyId);

    /**
     * Retrieves a counterparty by its unique code.
     * 
     * Counterparty codes are business identifiers used for efficient lookup
     * and reference in trading systems. This method provides a business-oriented
     * alternative to ID-based lookup.
     * 
     * @param counterpartyCode The unique code assigned to the counterparty (3-10 alphanumeric characters)
     * @return The counterparty information in response format
     * @throws org.george.fxoptiontradebooking.exception.TradeNotFoundException if counterparty does not exist
     * @throws org.george.fxoptiontradebooking.exception.BusinessValidationException if code is invalid (null, empty, or malformed)
     */
    CounterpartyResponse getCounterpartyByCode(String counterpartyCode);

    /**
     * Retrieves all active counterparties in the system.
     * 
     * Active counterparties are those available for new trade booking.
     * This method is typically used in trade entry interfaces to populate
     * counterparty selection lists.
     * 
     * @return A list of all active counterparties sorted by name
     */
    List<CounterpartyResponse> getAllActiveCounterparties();

    /**
     * Retrieves all counterparties in the system, both active and inactive.
     * 
     * This method provides a complete view of all counterparties for administrative
     * and reporting purposes. It includes inactive counterparties that are no longer
     * available for new trades but maintain historical trade relationships.
     * 
     * @return A list of all counterparties (active and inactive) sorted by name
     */
    List<CounterpartyResponse> getAllCounterparties();

    /**
     * Updates an existing counterparty with new information.
     * 
     * Allows modification of counterparty details while maintaining data integrity:
     * - Validates all updated fields according to business rules
     * - Checks for uniqueness constraints on updated values
     * - Preserves historical relationships with existing trades
     * 
     * The counterparty ID cannot be changed, and certain updates may be restricted
     * if the counterparty has existing trades.
     * 
     * @param counterpartyId The unique identifier of the counterparty to update
     * @param request The new counterparty details with updated information
     * @return The updated counterparty information in response format
     * @throws org.george.fxoptiontradebooking.exception.TradeNotFoundException if counterparty does not exist
     * @throws org.george.fxoptiontradebooking.exception.BusinessValidationException if validation fails or uniqueness constraints are violated
     */
    CounterpartyResponse updateCounterparty(Long counterpartyId, CounterpartyRequest request);

    /**
     * Deactivates a counterparty in the system (soft delete).
     * Follows investment banking practices of keeping historical records.
     * 
     * Rather than physically deleting the counterparty, this method:
     * - Sets the counterparty status to inactive
     * - Preserves all historical trade records
     * - Prevents new trades from being booked with this counterparty
     * 
     * This approach ensures regulatory compliance and data integrity
     * while allowing proper historical reporting.
     * 
     * @param counterpartyId The unique identifier of the counterparty to deactivate
     * @throws org.george.fxoptiontradebooking.exception.TradeNotFoundException if counterparty does not exist
     */
    void deactivateCounterparty(Long counterpartyId);
}
