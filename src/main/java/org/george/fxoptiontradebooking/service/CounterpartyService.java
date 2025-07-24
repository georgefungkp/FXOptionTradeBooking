package org.george.fxoptiontradebooking.service;

import org.george.fxoptiontradebooking.dto.request.CounterpartyRequest;
import org.george.fxoptiontradebooking.dto.response.CounterpartyResponse;

import java.util.List;

/**
 * Service interface for managing counterparty operations in the FX option trading system.
 * Provides business logic for creating, retrieving, updating, and deactivating counterparties
 * with appropriate validation and business rules specific to financial institutions.
 */
public interface CounterpartyService {

    /**
     * Creates a new counterparty in the system.
     * Performs validation, uniqueness checks, and data normalization before persisting.
     *
     * @param request The counterparty information to create
     * @return The newly created counterparty data
     * @throws org.george.fxoptiontradebooking.exception.BusinessValidationException if validation fails or if the counterparty already exists
     */
    CounterpartyResponse createCounterparty(CounterpartyRequest request);

    /**
     * Retrieves a counterparty by its unique ID.
     *
     * @param counterpartyId The ID of the counterparty to retrieve
     * @return The counterparty data if found
     * @throws org.george.fxoptiontradebooking.exception.TradeNotFoundException if counterparty doesn't exist
     * @throws org.george.fxoptiontradebooking.exception.BusinessValidationException if ID is invalid
     */
    CounterpartyResponse getCounterpartyById(Long counterpartyId);

    /**
     * Retrieves a counterparty by its unique code.
     *
     * @param counterpartyCode The code of the counterparty to retrieve
     * @return The counterparty data if found
     * @throws org.george.fxoptiontradebooking.exception.TradeNotFoundException if counterparty doesn't exist
     * @throws org.george.fxoptiontradebooking.exception.BusinessValidationException if code is invalid
     */
    CounterpartyResponse getCounterpartyByCode(String counterpartyCode);

    /**
     * Retrieves all active counterparties in the system.
     *
     * @return A list of active counterparties
     */
    List<CounterpartyResponse> getAllActiveCounterparties();

    /**
     * Retrieves all counterparties in the system (both active and inactive).
     *
     * @return A list of all counterparties
     */
    List<CounterpartyResponse> getAllCounterparties();

    /**
     * Updates an existing counterparty in the system.
     * Performs validation, uniqueness checks, and data normalization before persisting.
     *
     * @param counterpartyId The ID of the counterparty to update
     * @param request The updated counterparty information
     * @return The updated counterparty data
     * @throws org.george.fxoptiontradebooking.exception.TradeNotFoundException if counterparty doesn't exist
     * @throws org.george.fxoptiontradebooking.exception.BusinessValidationException if validation fails
     */
    CounterpartyResponse updateCounterparty(Long counterpartyId, CounterpartyRequest request);

    /**
     * Deactivates a counterparty in the system.
     * Sets the counterparty's active status to false without deleting the record.
     * This is a soft delete operation to maintain data integrity and audit trail.
     *
     * @param counterpartyId The ID of the counterparty to deactivate
     * @throws org.george.fxoptiontradebooking.exception.TradeNotFoundException if counterparty doesn't exist
     * @throws org.george.fxoptiontradebooking.exception.BusinessValidationException if ID is invalid or counterparty is already inactive
     */
    void deactivateCounterparty(Long counterpartyId);
}