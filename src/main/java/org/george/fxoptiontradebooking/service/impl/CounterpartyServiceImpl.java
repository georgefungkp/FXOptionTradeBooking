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
            createCounterparty(request, counterparty);
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
    private void createCounterparty(CounterpartyRequest request, Counterparty counterparty) {
        // Normalize all inputs by trimming whitespace and converting to uppercase
        // This ensures consistent data storage regardless of input format

        // Counterparty code is stored in uppercase for consistent matching and lookup
        counterparty.setCounterpartyCode(request.getCounterpartyCode().toUpperCase().trim());

        // Name is trimmed but case is preserved to maintain proper entity naming
        counterparty.setName(request.getName().trim());

        // LEI code is standardized to uppercase as per ISO 17442 standard
        counterparty.setLeiCode(request.getLeiCode() != null ? request.getLeiCode().toUpperCase().trim() : null);

        // SWIFT/BIC codes are always uppercase per ISO 9362 standard
        counterparty.setSwiftCode(request.getSwiftCode() != null ? request.getSwiftCode().toUpperCase().trim() : null);

        // Credit ratings are standardized to uppercase for consistent risk assessment
        counterparty.setCreditRating(request.getCreditRating() != null ? request.getCreditRating().toUpperCase().trim() : null);
    }

    @Override
    @Transactional(readOnly = true)
    public CounterpartyResponse getCounterpartyById(Long counterpartyId) {
        log.debug("Retrieving counterparty by ID: {}", counterpartyId);

        Counterparty counterparty = counterpartyRepository.findById(counterpartyId)
                .orElseThrow(() -> new TradeNotFoundException("Counterparty not found with ID: " + counterpartyId));

        return modelMapper.map(counterparty, CounterpartyResponse.class);
    }

    @Override
    @Transactional(readOnly = true)
    public CounterpartyResponse getCounterpartyByCode(String counterpartyCode) {
        log.debug("Retrieving counterparty by code: {}", counterpartyCode);

        if (counterpartyCode == null || counterpartyCode.trim().isEmpty()) {
            throw new BusinessValidationException("Counterparty code cannot be null or empty");
        }

        Counterparty counterparty = counterpartyRepository.findByCounterpartyCode(counterpartyCode.toUpperCase().trim())
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

        List<Counterparty> counterparties = counterpartyRepository.findAll();

        return counterparties.stream()
                .map(counterparty -> modelMapper.map(counterparty, CounterpartyResponse.class))
                .collect(Collectors.toList());
    }

    @Override
    public CounterpartyResponse updateCounterparty(Long counterpartyId, CounterpartyRequest request) {
        log.info("Updating counterparty with ID: {}", counterpartyId);

        validateCounterpartyRequest(request);

        Counterparty existingCounterparty = counterpartyRepository.findById(counterpartyId)
                .orElseThrow(() -> new TradeNotFoundException("Counterparty not found with ID: " + counterpartyId));

        // Check for duplicate counterparty code (excluding current counterparty)
        Optional<Counterparty> duplicateCode = counterpartyRepository.findByCounterpartyCode(request.getCounterpartyCode());
        if (duplicateCode.isPresent() && !duplicateCode.get().getCounterpartyId().equals(counterpartyId)) {
            throw new BusinessValidationException("Counterparty code already exists: " + request.getCounterpartyCode());
        }

        // Check for duplicate LEI code if provided (excluding current counterparty)
        if (request.getLeiCode() != null && !request.getLeiCode().trim().isEmpty()) {
            Optional<Counterparty> duplicateLei = counterpartyRepository.findByLeiCode(request.getLeiCode());
            if (duplicateLei.isPresent() && !duplicateLei.get().getCounterpartyId().equals(counterpartyId)) {
                throw new BusinessValidationException("LEI code already exists: " + request.getLeiCode());
            }
        }

        try {
            // Update counterparty fields
            createCounterparty(request, existingCounterparty);
            existingCounterparty.setIsActive(request.getIsActive() != null ? request.getIsActive() : existingCounterparty.getIsActive());

            Counterparty updatedCounterparty = counterpartyRepository.save(existingCounterparty);

            log.info("Successfully updated counterparty with ID: {}", counterpartyId);

            return modelMapper.map(updatedCounterparty, CounterpartyResponse.class);

        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation while updating counterparty: {}", e.getMessage());
            throw new BusinessValidationException("Failed to update counterparty due to data constraint violation");
        }
    }

    @Override
    public void deactivateCounterparty(Long counterpartyId) {
        log.info("Deactivating counterparty with ID: {}", counterpartyId);

        Counterparty counterparty = counterpartyRepository.findById(counterpartyId)
                .orElseThrow(() -> new TradeNotFoundException("Counterparty not found with ID: " + counterpartyId));

        // Investment banks typically don't delete counterparties but deactivate them for audit purposes
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
        // Validate counterparty code - required field for all financial counterparties
        if (request.getCounterpartyCode() == null || request.getCounterpartyCode().trim().isEmpty()) {
            throw new BusinessValidationException("Counterparty code is required");
        }

        // Normalize and validate code format against industry standard pattern
        String normalizedCode = request.getCounterpartyCode().toUpperCase().trim();
        if (!COUNTERPARTY_CODE_PATTERN.matcher(normalizedCode).matches()) {
            throw new BusinessValidationException("Counterparty code must be 3-10 alphanumeric characters: " + request.getCounterpartyCode());
        }

        // Validate name
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new BusinessValidationException("Counterparty name is required");
        }

        if (request.getName().trim().length() > 255) {
            throw new BusinessValidationException("Counterparty name cannot exceed 255 characters");
        }

        // Validate LEI code if provided (Legal Entity Identifier standard)
        if (request.getLeiCode() != null && !request.getLeiCode().trim().isEmpty()) {
            String normalizedLei = request.getLeiCode().toUpperCase().trim();
            if (!LEI_CODE_PATTERN.matcher(normalizedLei).matches()) {
                throw new BusinessValidationException("Invalid LEI code format. Must be 20 alphanumeric characters: " + request.getLeiCode());
            }
        }

        // Validate SWIFT code if provided (Bank Identifier Code standard)
        if (request.getSwiftCode() != null && !request.getSwiftCode().trim().isEmpty()) {
            String normalizedSwift = request.getSwiftCode().toUpperCase().trim();
            if (!SWIFT_CODE_PATTERN.matcher(normalizedSwift).matches()) {
                throw new BusinessValidationException("Invalid SWIFT code format. Must follow BIC standards: " + request.getSwiftCode());
            }
        }

        // Validate credit rating if provided (investment grade and speculative ratings)
        if (request.getCreditRating() != null && !request.getCreditRating().trim().isEmpty()) {
            validateCreditRating(request.getCreditRating());
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
        // Enforce maximum length constraint
        if (creditRating.length() > 5) {
            throw new BusinessValidationException("Credit rating cannot exceed 5 characters");
        }

        String normalizedRating = creditRating.toUpperCase().trim();

        // Standard rating scales used by major rating agencies (S&P, Moody's, and Fitch)
        // Investment grade: AAA to BBB-
        // Speculative grade: BB+ to D
        // NR: Not Rated
        String[] validRatings = {
            "AAA", "AA+", "AA", "AA-", "A+", "A", "A-",
            "BBB+", "BBB", "BBB-", "BB+", "BB", "BB-",
            "B+", "B", "B-", "CCC+", "CCC", "CCC-",
            "CC", "C", "D", "NR" // NR = Not Rated
        };

        boolean isValidRating = false;
        for (String validRating : validRatings) {
            if (validRating.equals(normalizedRating)) {
                isValidRating = true;
                break;
            }
        }

        if (!isValidRating) {
            log.warn("Non-standard credit rating provided: {}. Proceeding with validation.", creditRating);
        }

        // Warn for sub-investment grade ratings (BB+ and below)
        String[] subInvestmentGrade = {"BB+", "BB", "BB-", "B+", "B", "B-", "CCC+", "CCC", "CCC-", "CC", "C", "D"};
        for (String rating : subInvestmentGrade) {
            if (rating.equals(normalizedRating)) {
                log.warn("Sub-investment grade rating detected: {}. Additional risk monitoring may be required.", creditRating);
                break;
            }
        }
    }
}