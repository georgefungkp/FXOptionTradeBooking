package org.george.fxoptiontradebooking.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.george.fxoptiontradebooking.dto.request.CounterpartyRequest;
import org.george.fxoptiontradebooking.dto.response.ApiResponse;
import org.george.fxoptiontradebooking.dto.response.CounterpartyResponse;
import org.george.fxoptiontradebooking.service.CounterpartyService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing counterparty-related operations.
 * Provides endpoints for creating, retrieving, and querying counterparty data.
 * All endpoints return responses wrapped in a standardized ApiResponse format.
 */
@RestController
@RequestMapping("/api/v1/counterparties")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Allows requests from any origin for development purposes
public class CounterpartyController {

    /**
     * Service for handling counterparty business logic operations
     */
    private final CounterpartyService counterpartyService;

    /**
     * Creates a new counterparty in the system.
     * 
     * @param request The validated counterparty information
     * @return HTTP 201 Created with the newly created counterparty data
     * @throws org.george.fxoptiontradebooking.exception.BusinessValidationException if validation fails
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CounterpartyResponse>> createCounterparty(@Valid @RequestBody CounterpartyRequest request) {
        CounterpartyResponse response = counterpartyService.createCounterparty(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Counterparty created successfully", response));
    }

    /**
     * Retrieves a counterparty by its unique ID.
     * 
     * @param counterpartyId The ID of the counterparty to retrieve
     * @return HTTP 200 OK with the counterparty data if found
     * @throws org.george.fxoptiontradebooking.exception.TradeNotFoundException if counterparty doesn't exist
     */
    @GetMapping("/{counterpartyId}")
    public ResponseEntity<ApiResponse<CounterpartyResponse>> getCounterpartyById(@PathVariable Long counterpartyId) {
        CounterpartyResponse response = counterpartyService.getCounterpartyById(counterpartyId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Retrieves a counterparty by its unique code.
     * 
     * @param counterpartyCode The code of the counterparty to retrieve
     * @return HTTP 200 OK with the counterparty data if found
     * @throws org.george.fxoptiontradebooking.exception.TradeNotFoundException if counterparty doesn't exist
     * @throws org.george.fxoptiontradebooking.exception.BusinessValidationException if code is invalid
     */
    @GetMapping("/code/{counterpartyCode}")
    public ResponseEntity<ApiResponse<CounterpartyResponse>> getCounterpartyByCode(@PathVariable String counterpartyCode) {
        CounterpartyResponse response = counterpartyService.getCounterpartyByCode(counterpartyCode);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Retrieves all counterparties in the system.
     * 
     * @return HTTP 200 OK with a list of all counterparties (active and inactive)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<CounterpartyResponse>>> getAllCounterparties() {
        List<CounterpartyResponse> responses = counterpartyService.getAllCounterparties();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Retrieves only active counterparties in the system.
     * 
     * @return HTTP 200 OK with a list of active counterparties
     */
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<CounterpartyResponse>>> getAllActiveCounterparties() {
        List<CounterpartyResponse> responses = counterpartyService.getAllActiveCounterparties();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
}
