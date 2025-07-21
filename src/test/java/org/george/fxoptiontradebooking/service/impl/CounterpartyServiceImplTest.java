package org.george.fxoptiontradebooking.service.impl;

import org.george.fxoptiontradebooking.dto.request.CounterpartyRequest;
import org.george.fxoptiontradebooking.dto.response.CounterpartyResponse;
import org.george.fxoptiontradebooking.entity.Counterparty;
import org.george.fxoptiontradebooking.exception.BusinessValidationException;
import org.george.fxoptiontradebooking.exception.TradeNotFoundException;
import org.george.fxoptiontradebooking.repository.CounterpartyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CounterpartyServiceImpl Tests")
class CounterpartyServiceImplTest {

    @Mock
    private CounterpartyRepository counterpartyRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private CounterpartyServiceImpl counterpartyService;

    private CounterpartyRequest validRequest;
    private Counterparty validCounterparty;
    private CounterpartyResponse validResponse;

    @BeforeEach
    void setUp() {
        validRequest = createValidCounterpartyRequest();
        validCounterparty = createValidCounterparty();
        validResponse = createValidCounterpartyResponse();
    }

    @Nested
    @DisplayName("Create Counterparty Tests")
    class CreateCounterpartyTests {

        @Test
        @DisplayName("Should create counterparty successfully")
        void shouldCreateCounterpartySuccessfully() {
            // Given
            when(counterpartyRepository.findByCounterpartyCode(anyString())).thenReturn(Optional.empty());
            when(counterpartyRepository.findByLeiCode(anyString())).thenReturn(Optional.empty());
            when(counterpartyRepository.save(any(Counterparty.class))).thenReturn(validCounterparty);
            when(modelMapper.map(validCounterparty, CounterpartyResponse.class)).thenReturn(validResponse);

            // When
            CounterpartyResponse result = counterpartyService.createCounterparty(validRequest);

            // Then
            assertNotNull(result);
            assertEquals(validResponse.getCounterpartyCode(), result.getCounterpartyCode());
            verify(counterpartyRepository).save(any(Counterparty.class));
            verify(modelMapper).map(validCounterparty, CounterpartyResponse.class);
        }

        @Test
        @DisplayName("Should throw exception for duplicate counterparty code")
        void shouldThrowExceptionForDuplicateCounterpartyCode() {
            // Given
            when(counterpartyRepository.findByCounterpartyCode(anyString())).thenReturn(Optional.of(validCounterparty));

            // When & Then
            BusinessValidationException exception = assertThrows(
                BusinessValidationException.class,
                () -> counterpartyService.createCounterparty(validRequest)
            );
            assertEquals("Counterparty code already exists: " + validRequest.getCounterpartyCode(), exception.getMessage());
            verify(counterpartyRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception for duplicate LEI code")
        void shouldThrowExceptionForDuplicateLeiCode() {
            // Given
            when(counterpartyRepository.findByCounterpartyCode(anyString())).thenReturn(Optional.empty());
            when(counterpartyRepository.findByLeiCode(anyString())).thenReturn(Optional.of(validCounterparty));

            // When & Then
            BusinessValidationException exception = assertThrows(
                BusinessValidationException.class,
                () -> counterpartyService.createCounterparty(validRequest)
            );
            assertEquals("LEI code already exists: " + validRequest.getLeiCode(), exception.getMessage());
            verify(counterpartyRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception for null request")
        void shouldThrowExceptionForNullRequest() {
            // When & Then
            BusinessValidationException exception = assertThrows(
                BusinessValidationException.class,
                () -> counterpartyService.createCounterparty(null)
            );
            assertEquals("Counterparty request cannot be null", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for invalid counterparty code format")
        void shouldThrowExceptionForInvalidCounterpartyCodeFormat() {
            // Given
            validRequest.setCounterpartyCode("XX");

            // When & Then
            BusinessValidationException exception = assertThrows(
                BusinessValidationException.class,
                () -> counterpartyService.createCounterparty(validRequest)
            );
            assertTrue(exception.getMessage().contains("Counterparty code must be 3-10 alphanumeric characters"));
        }

        @Test
        @DisplayName("Should throw exception for invalid LEI code format")
        void shouldThrowExceptionForInvalidLeiCodeFormat() {
            // Given
            validRequest.setLeiCode("INVALID_LEI");

            // When & Then
            BusinessValidationException exception = assertThrows(
                BusinessValidationException.class,
                () -> counterpartyService.createCounterparty(validRequest)
            );
            assertTrue(exception.getMessage().contains("Invalid LEI code format"));
        }

        @Test
        @DisplayName("Should throw exception for invalid SWIFT code format")
        void shouldThrowExceptionForInvalidSwiftCodeFormat() {
            // Given
            validRequest.setSwiftCode("INVALID");

            // When & Then
            BusinessValidationException exception = assertThrows(
                BusinessValidationException.class,
                () -> counterpartyService.createCounterparty(validRequest)
            );
            assertTrue(exception.getMessage().contains("Invalid SWIFT code format"));
        }

        @Test
        @DisplayName("Should handle data integrity violation")
        void shouldHandleDataIntegrityViolation() {
            // Given
            when(counterpartyRepository.findByCounterpartyCode(anyString())).thenReturn(Optional.empty());
            when(counterpartyRepository.findByLeiCode(anyString())).thenReturn(Optional.empty());
            when(counterpartyRepository.save(any(Counterparty.class))).thenThrow(new DataIntegrityViolationException("Constraint violation"));

            // When & Then
            BusinessValidationException exception = assertThrows(
                BusinessValidationException.class,
                () -> counterpartyService.createCounterparty(validRequest)
            );
            assertEquals("Failed to create counterparty due to data constraint violation", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Get Counterparty Tests")
    class GetCounterpartyTests {

        @Test
        @DisplayName("Should get counterparty by ID successfully")
        void shouldGetCounterpartyByIdSuccessfully() {
            // Given
            Long counterpartyId = 1L;
            when(counterpartyRepository.findById(counterpartyId)).thenReturn(Optional.of(validCounterparty));
            when(modelMapper.map(validCounterparty, CounterpartyResponse.class)).thenReturn(validResponse);

            // When
            CounterpartyResponse result = counterpartyService.getCounterpartyById(counterpartyId);

            // Then
            assertNotNull(result);
            assertEquals(validResponse.getCounterpartyCode(), result.getCounterpartyCode());
            verify(counterpartyRepository).findById(counterpartyId);
        }

        @Test
        @DisplayName("Should throw exception when counterparty not found by ID")
        void shouldThrowExceptionWhenCounterpartyNotFoundById() {
            // Given
            Long counterpartyId = 999L;
            when(counterpartyRepository.findById(counterpartyId)).thenReturn(Optional.empty());

            // When & Then
            TradeNotFoundException exception = assertThrows(
                TradeNotFoundException.class,
                () -> counterpartyService.getCounterpartyById(counterpartyId)
            );
            assertEquals("Counterparty not found with ID: " + counterpartyId, exception.getMessage());
        }

        @Test
        @DisplayName("Should get counterparty by code successfully")
        void shouldGetCounterpartyByCodeSuccessfully() {
            // Given
            String counterpartyCode = "CP001";
            when(counterpartyRepository.findByCounterpartyCode(counterpartyCode.toUpperCase())).thenReturn(Optional.of(validCounterparty));
            when(modelMapper.map(validCounterparty, CounterpartyResponse.class)).thenReturn(validResponse);

            // When
            CounterpartyResponse result = counterpartyService.getCounterpartyByCode(counterpartyCode);

            // Then
            assertNotNull(result);
            assertEquals(validResponse.getCounterpartyCode(), result.getCounterpartyCode());
            verify(counterpartyRepository).findByCounterpartyCode(counterpartyCode.toUpperCase());
        }

        @Test
        @DisplayName("Should throw exception for null counterparty code")
        void shouldThrowExceptionForNullCounterpartyCode() {
            // When & Then
            BusinessValidationException exception = assertThrows(
                BusinessValidationException.class,
                () -> counterpartyService.getCounterpartyByCode(null)
            );
            assertEquals("Counterparty code cannot be null or empty", exception.getMessage());
        }

        @Test
        @DisplayName("Should get all active counterparties")
        void shouldGetAllActiveCounterparties() {
            // Given
            List<Counterparty> activeCounterparties = Arrays.asList(validCounterparty);
            when(counterpartyRepository.findByIsActiveTrue()).thenReturn(activeCounterparties);
            when(modelMapper.map(validCounterparty, CounterpartyResponse.class)).thenReturn(validResponse);

            // When
            List<CounterpartyResponse> result = counterpartyService.getAllActiveCounterparties();

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            verify(counterpartyRepository).findByIsActiveTrue();
        }

        @Test
        @DisplayName("Should get all counterparties")
        void shouldGetAllCounterparties() {
            // Given
            List<Counterparty> allCounterparties = Arrays.asList(validCounterparty);
            when(counterpartyRepository.findAll()).thenReturn(allCounterparties);
            when(modelMapper.map(validCounterparty, CounterpartyResponse.class)).thenReturn(validResponse);

            // When
            List<CounterpartyResponse> result = counterpartyService.getAllCounterparties();

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            verify(counterpartyRepository).findAll();
        }
    }

    private CounterpartyRequest createValidCounterpartyRequest() {
        CounterpartyRequest request = new CounterpartyRequest();
        request.setCounterpartyCode("CP001");
        request.setName("Test Counterparty");
        request.setLeiCode("12345678901234567890");
        request.setSwiftCode("TESTUS33");
        request.setCreditRating("AA");
        request.setIsActive(true);
        return request;
    }

    private Counterparty createValidCounterparty() {
        Counterparty counterparty = new Counterparty();
        counterparty.setCounterpartyId(1L);
        counterparty.setCounterpartyCode("CP001");
        counterparty.setName("Test Counterparty");
        counterparty.setLeiCode("12345678901234567890");
        counterparty.setSwiftCode("TESTUS33");
        counterparty.setCreditRating("AA");
        counterparty.setIsActive(true);
        return counterparty;
    }

    private CounterpartyResponse createValidCounterpartyResponse() {
        CounterpartyResponse response = new CounterpartyResponse();
        response.setCounterpartyId(1L);
        response.setCounterpartyCode("CP001");
        response.setName("Test Counterparty");
        response.setLeiCode("12345678901234567890");
        response.setSwiftCode("TESTUS33");
        response.setCreditRating("AA");
        response.setIsActive(true);
        return response;
    }
}
