package org.george.fxoptiontradebooking.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.george.fxoptiontradebooking.dto.request.CounterpartyRequest;
import org.george.fxoptiontradebooking.dto.response.CounterpartyResponse;
import org.george.fxoptiontradebooking.entity.Counterparty;
import org.george.fxoptiontradebooking.exception.BusinessValidationException;
import org.george.fxoptiontradebooking.exception.TradeNotFoundException;
import org.george.fxoptiontradebooking.repository.CounterpartyRepository;
import org.george.fxoptiontradebooking.service.CounterpartyService;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Implementation of the CounterpartyService interface.
 * Provides business logic for managing counterparty operations in the FX option trading system.
 * Handles creation, retrieval, update, and deactivation of counterparties with appropriate
 * validation and business rules specific to financial institutions.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CounterpartyServiceImpl implements CounterpartyService {

    /** Repository for counterparty data access operations */
    private final CounterpartyRepository counterpartyRepository;

    /** ModelMapper for entity-to-DTO and DTO-to-entity conversion */
    private final ModelMapper modelMapper;

    // Investment bank counterparty validation patterns

    /** 
     * Pattern for validating Legal Entity Identifier (LEI) codes.
     * LEI is a 20-character alphanumeric code that uniquely identifies legal entities
     * participating in financial transactions globally.
     */
    private static final Pattern LEI_CODE_PATTERN = Pattern.compile("^[A-Z0-9]{20}$");

    /**
     * Pattern for validating SWIFT/BIC (Bank Identifier Code) format.
     * Format: 8 or 11 characters - first 4 bank code, next 2 country code,
     * next 2 location code, last 3 branch code (optional).
     */
    private static final Pattern SWIFT_CODE_PATTERN = Pattern.compile("^[A-Z]{6}[A-Z0-9]{2}([A-Z0-9]{3})?$");

    /**
     * Pattern for validating internal counterparty codes.
     * Format: 3-10 alphanumeric characters, following financial institution standards.
     */
    private static final Pattern COUNTERPARTY_CODE_PATTERN = Pattern.compile("^[A-Z0-9]{3,10}$");

    /**
     * Creates a new counterparty in the system.
     * Performs validation, uniqueness checks, and data normalization before persisting.
     *
     * @param request The counterparty information to create
     * @return The newly created counterparty data
     * @throws BusinessValidationException if validation fails or if the counterparty already exists
     */
    @Override
    public CounterpartyResponse createCounterparty(CounterpartyRequest request) {

        // Perform null check first before any operations to prevent NullPointerException
        if (request == null) {
            throw new BusinessValidationException("Counterparty request cannot be null");
        }

        log.info("Creating new counterparty with code: {}", request.getCounterpartyCode());

        // Validate all counterparty data fields
        validateCounterpartyRequest(request);

        // Check for duplicate counterparty code to maintain uniqueness
        if (counterpartyRepository.findByCounterpartyCode(request.getCounterpartyCode()).isPresent()) {
            throw new BusinessValidationException("Counterparty code already exists: " + request.getCounterpartyCode());
        }

        // Check for duplicate LEI code if provided (financial institutions require unique LEI codes)
        if (request.getLeiCode() != null && !request.getLeiCode().trim().isEmpty()) {
            if (counterpartyRepository.findByLeiCode(request.getLeiCode()).isPresent()) {
                throw new BusinessValidationException("LEI code already exists: " + request.getLeiCode());
            }
        }

        try {
            Counterparty counterparty = new Counterparty();
            mapRequestToEntity(request, counterparty);
            counterparty.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);

            Counterparty savedCounterparty = counterpartyRepository.save(counterparty);

            log.info("Successfully created counterparty with ID: {} and code: {}",
                    savedCounterparty.getCounterpartyId(), savedCounterparty.getCounterpartyCode());

            return modelMapper.map(savedCounterparty, CounterpartyResponse.class);

        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation while creating counterparty: {}", e.getMessage());
            throw new BusinessValidationException("Failed to create counterparty due to data constraint violation");
        }
    }

    /**
     * Maps data from a CounterpartyRequest to a Counterparty entity.
     * Normalizes input data by trimming whitespace and converting to uppercase where appropriate.
     * Used for both creation and updates to maintain consistent data handling.
     *
     * @param request The source request containing counterparty data
     * @param counterparty The target counterparty entity to populate
     */
    private void mapRequestToEntity(CounterpartyRequest request, Counterparty counterparty) {
        // Normalize all inputs by trimming whitespace and converting to uppercase
        // This ensures consistent data storage regardless of input format

        // Counterparty code is stored in uppercase for consistent matching and lookup
        counterparty.setCounterpartyCode(request.getCounterpartyCode().trim().toUpperCase());
        
        // Name is stored as provided but trimmed of whitespace
        counterparty.setName(request.getName().trim());
        
        // LEI code is uppercase alphanumeric only for global consistency
        if (request.getLeiCode() != null && !request.getLeiCode().trim().isEmpty()) {
            counterparty.setLeiCode(request.getLeiCode().trim().toUpperCase());
        }
        
        // SWIFT code is uppercase for BIC standard compliance
        if (request.getSwiftCode() != null && !request.getSwiftCode().trim().isEmpty()) {
            counterparty.setSwiftCode(request.getSwiftCode().trim().toUpperCase());
        }
        
        // Credit rating is uppercase for standard rating agency format
        if (request.getCreditRating() != null && !request.getCreditRating().trim().isEmpty()) {
            counterparty.setCreditRating(request.getCreditRating().trim().toUpperCase());
        }
        
        // Set audit timestamps
        LocalDateTime now = LocalDateTime.now();
        if (counterparty.getCounterpartyId() == null) {
            counterparty.setCreatedAt(now);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public CounterpartyResponse getCounterpartyById(Long counterpartyId) {
        if (counterpartyId == null || counterpartyId <= 0) {
            throw new BusinessValidationException("Counterparty ID must be a positive number");
        }

        log.debug("Retrieving counterparty with ID: {}", counterpartyId);

        Counterparty counterparty = counterpartyRepository.findById(counterpartyId)
                .orElseThrow(() -> new TradeNotFoundException("Counterparty not found with ID: " + counterpartyId));

        return modelMapper.map(counterparty, CounterpartyResponse.class);
    }

    @Override
    @Transactional(readOnly = true)
    public CounterpartyResponse getCounterpartyByCode(String counterpartyCode) {
        if (counterpartyCode == null || counterpartyCode.trim().isEmpty()) {
            throw new BusinessValidationException("Counterparty code cannot be null or empty");
        }

        log.debug("Retrieving counterparty with code: {}", counterpartyCode);

        Counterparty counterparty = counterpartyRepository.findByCounterpartyCode(counterpartyCode.trim().toUpperCase())
                .orElseThrow(() -> new TradeNotFoundException("Counterparty not found with code: " + counterpartyCode));

        return modelMapper.map(counterparty, CounterpartyResponse.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CounterpartyResponse> getAllActiveCounterparties() {
        log.debug("Retrieving all active counterparties");

        List<Counterparty> activeCounterparties = counterpartyRepository.findByIsActiveTrue();
        
        return activeCounterparties.stream()
                .map(counterparty -> modelMapper.map(counterparty, CounterpartyResponse.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CounterpartyResponse> getAllCounterparties() {
        log.debug("Retrieving all counterparties");

        List<Counterparty> allCounterparties = counterpartyRepository.findAll();
        
        return allCounterparties.stream()
                .map(counterparty -> modelMapper.map(counterparty, CounterpartyResponse.class))
                .collect(Collectors.toList());
    }

    @Override
    public CounterpartyResponse updateCounterparty(Long counterpartyId, CounterpartyRequest request) {
        if (counterpartyId == null || counterpartyId <= 0) {
            throw new BusinessValidationException("Counterparty ID must be a positive number");
        }

        if (request == null) {
            throw new BusinessValidationException("Counterparty request cannot be null");
        }

        log.info("Updating counterparty with ID: {}", counterpartyId);

        Counterparty existingCounterparty = counterpartyRepository.findById(counterpartyId)
                .orElseThrow(() -> new TradeNotFoundException("Counterparty not found with ID: " + counterpartyId));

        // Validate the updated request
        validateCounterpartyRequest(request);

        // Check for duplicate counterparty code (but allow keeping the same code)
        Optional<Counterparty> existingByCode = counterpartyRepository.findByCounterpartyCode(request.getCounterpartyCode());
        if (existingByCode.isPresent() && !existingByCode.get().getCounterpartyId().equals(counterpartyId)) {
            throw new BusinessValidationException("Counterparty code already exists: " + request.getCounterpartyCode());
        }

        // Check for duplicate LEI code (but allow keeping the same LEI)
        if (request.getLeiCode() != null && !request.getLeiCode().trim().isEmpty()) {
            Optional<Counterparty> existingByLei = counterpartyRepository.findByLeiCode(request.getLeiCode());
            if (existingByLei.isPresent() && !existingByLei.get().getCounterpartyId().equals(counterpartyId)) {
                throw new BusinessValidationException("LEI code already exists: " + request.getLeiCode());
            }
        }

        try {
            mapRequestToEntity(request, existingCounterparty);
            
            if (request.getIsActive() != null) {
                existingCounterparty.setIsActive(request.getIsActive());
            }

            Counterparty updatedCounterparty = counterpartyRepository.save(existingCounterparty);

            log.info("Successfully updated counterparty with ID: {}", counterpartyId);

            return modelMapper.map(updatedCounterparty, CounterpartyResponse.class);

        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation while updating counterparty: {}", e.getMessage());
            throw new BusinessValidationException("Failed to update counterparty due to data constraint violation");
        }
    }

    public void deactivateCounterparty(Long counterpartyId) {
        if (counterpartyId == null || counterpartyId <= 0) {
            throw new BusinessValidationException("Counterparty ID must be a positive number");
        }

        log.info("Deactivating counterparty with ID: {}", counterpartyId);

        Counterparty counterparty = counterpartyRepository.findById(counterpartyId)
                .orElseThrow(() -> new TradeNotFoundException("Counterparty not found with ID: " + counterpartyId));

        if (!counterparty.getIsActive()) {
            throw new BusinessValidationException("Counterparty is already inactive");
        }

        counterparty.setIsActive(false);

        counterpartyRepository.save(counterparty);

        log.info("Successfully deactivated counterparty with ID: {}", counterpartyId);
    }

    /**
     * Validates all aspects of a counterparty request according to financial industry standards.
     * Performs validation on required fields, formats, and lengths to ensure data integrity.
     *
     * @param request The counterparty request to validate
     * @throws BusinessValidationException if any validation check fails
     */
    private void validateCounterpartyRequest(CounterpartyRequest request) {
        // Validate required fields
        if (request.getCounterpartyCode() == null || request.getCounterpartyCode().trim().isEmpty()) {
            throw new BusinessValidationException("Counterparty code is required");
        }

        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new BusinessValidationException("Counterparty name is required");
        }

        // Validate counterparty code format
        if (!COUNTERPARTY_CODE_PATTERN.matcher(request.getCounterpartyCode().trim().toUpperCase()).matches()) {
            throw new BusinessValidationException("Counterparty code must be 3-10 alphanumeric characters");
        }

        // Validate name length (reasonable business limit)
        if (request.getName().trim().length() > 100) {
            throw new BusinessValidationException("Counterparty name cannot exceed 100 characters");
        }

        // Validate LEI code format if provided
        if (request.getLeiCode() != null && !request.getLeiCode().trim().isEmpty()) {
            if (!LEI_CODE_PATTERN.matcher(request.getLeiCode().trim().toUpperCase()).matches()) {
                throw new BusinessValidationException("Invalid LEI code format. LEI must be 20 alphanumeric characters");
            }
        }

        // Validate SWIFT code format if provided
        if (request.getSwiftCode() != null && !request.getSwiftCode().trim().isEmpty()) {
            if (!SWIFT_CODE_PATTERN.matcher(request.getSwiftCode().trim().toUpperCase()).matches()) {
                throw new BusinessValidationException("Invalid SWIFT code format. SWIFT code must be 8 or 11 characters");
            }
        }

        // Validate credit rating if provided
        if (request.getCreditRating() != null && !request.getCreditRating().trim().isEmpty()) {
            validateCreditRating(request.getCreditRating().trim());
        }
    }

    /**
     * Validates credit rating against standard industry rating scales.
     * Checks for length constraints and recognized rating values from major agencies.
     * Also logs warnings for non-standard ratings and sub-investment grade ratings.
     *
     * @param creditRating The credit rating to validate
     * @throws BusinessValidationException if the rating format is invalid
     */
    private void validateCreditRating(String creditRating) {
        if (creditRating.length() > 10) {
            throw new BusinessValidationException("Credit rating cannot exceed 10 characters");
        }

        // Standard rating patterns from major agencies (S&P, Moody's, Fitch)
        String upperRating = creditRating.toUpperCase();
        
        // Check for recognized rating patterns
        boolean isValidRating = upperRating.matches("^(AAA|AA[+-]?|A[+-]?|BBB[+-]?|BB[+-]?|B[+-]?|CCC[+-]?|CC|C|D)$") ||
                               upperRating.matches("^(AAA|AA[123]?|A[123]?|BAA[123]?|BA[123]?|B[123]?|CAA[123]?|CA|C)$");

        if (!isValidRating) {
            log.warn("Non-standard credit rating provided: {}", creditRating);
            // We don't throw an exception for non-standard ratings to allow flexibility
        }

        // Log warning for sub-investment grade ratings
        if (upperRating.startsWith("BB") || upperRating.startsWith("B") || upperRating.startsWith("C") || 
            upperRating.startsWith("BA") || upperRating.equals("D")) {
            log.warn("Sub-investment grade rating assigned: {}", creditRating);
        }
    }
}