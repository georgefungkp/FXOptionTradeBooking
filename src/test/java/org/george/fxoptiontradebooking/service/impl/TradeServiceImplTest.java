package org.george.fxoptiontradebooking.service.impl;

import org.george.fxoptiontradebooking.dto.request.TradeBookingRequest;
import org.george.fxoptiontradebooking.dto.response.TradeResponse;
import org.george.fxoptiontradebooking.entity.*;
import org.george.fxoptiontradebooking.exception.BusinessValidationException;
import org.george.fxoptiontradebooking.exception.TradeNotFoundException;
import org.george.fxoptiontradebooking.repository.CounterpartyRepository;
import org.george.fxoptiontradebooking.repository.TradeRepository;
import org.george.fxoptiontradebooking.service.ValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TradeServiceImpl Tests")
class TradeServiceImplTest {

    @Mock
    private TradeRepository tradeRepository;

    @Mock
    private CounterpartyRepository counterpartyRepository;

    @Mock
    private ValidationService validationService;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private TradeServiceImpl tradeService;

    private TradeBookingRequest validRequest;
    private Trade validTrade;
    private TradeResponse validResponse;
    private Counterparty validCounterparty;

    @BeforeEach
    void setUp() {
        validRequest = createValidTradeBookingRequest();
        validTrade = createValidTrade();
        validResponse = createValidTradeResponse();
        validCounterparty = createValidCounterparty();
    }

    @Nested
    @DisplayName("Book Trade Tests")
    class BookTradeTests {

        @Test
        @DisplayName("Should book trade successfully")
        void shouldBookTradeSuccessfully() {
            // Given
            when(counterpartyRepository.findById(anyLong())).thenReturn(Optional.of(validCounterparty));
            when(tradeRepository.findByTradeReference(anyString())).thenReturn(Optional.empty());
            when(tradeRepository.save(any(Trade.class))).thenReturn(validTrade);
            when(modelMapper.map(validTrade, TradeResponse.class)).thenReturn(validResponse);
            doNothing().when(validationService).validateTradeRequest(any());

            // When
            TradeResponse result = tradeService.bookTrade(validRequest);

            // Then
            assertNotNull(result);
            assertEquals(validResponse.getTradeReference(), result.getTradeReference());
            verify(validationService).validateTradeRequest(validRequest);
            verify(counterpartyRepository).findById(validRequest.getCounterpartyId());
            verify(tradeRepository).save(any(Trade.class));
        }

        @Test
        @DisplayName("Should throw exception for null request")
        void shouldThrowExceptionForNullRequest() {
            // When & Then
            BusinessValidationException exception = assertThrows(
                BusinessValidationException.class,
                () -> tradeService.bookTrade(null)
            );
            assertEquals("Trade booking request cannot be null", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for non-existent counterparty")
        void shouldThrowExceptionForNonExistentCounterparty() {
            // Given
            when(counterpartyRepository.findById(anyLong())).thenReturn(Optional.empty());

            // When & Then
            BusinessValidationException exception = assertThrows(
                BusinessValidationException.class,
                () -> tradeService.bookTrade(validRequest)
            );
            assertTrue(exception.getMessage().contains("Counterparty not found"));
        }

        @Test
        @DisplayName("Should throw exception for inactive counterparty")
        void shouldThrowExceptionForInactiveCounterparty() {
            // Given
            validCounterparty.setIsActive(false);
            when(counterpartyRepository.findById(anyLong())).thenReturn(Optional.of(validCounterparty));

            // When & Then
            BusinessValidationException exception = assertThrows(
                BusinessValidationException.class,
                () -> tradeService.bookTrade(validRequest)
            );
            assertTrue(exception.getMessage().contains("Cannot trade with inactive counterparty"));
        }

        @Test
        @DisplayName("Should throw exception for duplicate trade reference")
        void shouldThrowExceptionForDuplicateTradeReference() {
            // Given
            when(counterpartyRepository.findById(anyLong())).thenReturn(Optional.of(validCounterparty));
            when(tradeRepository.findByTradeReference(anyString())).thenReturn(Optional.of(validTrade));
            doNothing().when(validationService).validateTradeRequest(any());

            // When & Then
            BusinessValidationException exception = assertThrows(
                BusinessValidationException.class,
                () -> tradeService.bookTrade(validRequest)
            );
            assertTrue(exception.getMessage().contains("Trade reference already exists"));
        }
    }

    @Nested
    @DisplayName("Get Trade Tests")
    class GetTradeTests {

        @Test
        @DisplayName("Should get trade by ID successfully")
        void shouldGetTradeByIdSuccessfully() {
            // Given
            Long tradeId = 1L;
            when(tradeRepository.findById(tradeId)).thenReturn(Optional.of(validTrade));
            when(modelMapper.map(validTrade, TradeResponse.class)).thenReturn(validResponse);

            // When
            TradeResponse result = tradeService.getTradeById(tradeId);

            // Then
            assertNotNull(result);
            assertEquals(validResponse.getTradeReference(), result.getTradeReference());
            verify(tradeRepository).findById(tradeId);
        }

        @Test
        @DisplayName("Should throw exception when trade not found by ID")
        void shouldThrowExceptionWhenTradeNotFoundById() {
            // Given
            Long tradeId = 999L;
            when(tradeRepository.findById(tradeId)).thenReturn(Optional.empty());

            // When & Then
            TradeNotFoundException exception = assertThrows(
                TradeNotFoundException.class,
                () -> tradeService.getTradeById(tradeId)
            );
            assertEquals("Trade not found with ID: " + tradeId, exception.getMessage());
        }
    }

    private TradeBookingRequest createValidTradeBookingRequest() {
        TradeBookingRequest request = new TradeBookingRequest();
        request.setTradeReference("TRD-001");
        request.setCounterpartyId(1L);
        request.setBaseCurrency("EUR");
        request.setQuoteCurrency("USD");
        request.setNotionalAmount(new BigDecimal("100000.00"));
        request.setStrikePrice(new BigDecimal("1.2500"));
        request.setSpotRate(new BigDecimal("1.2000"));
        request.setTradeDate(LocalDate.now());
        request.setValueDate(LocalDate.now().plusDays(2));
        request.setMaturityDate(LocalDate.now().plusDays(30));
        request.setOptionType(OptionType.CALL);
        request.setCreatedBy("TEST_USER");
        return request;
    }

    private Trade createValidTrade() {
        Trade trade = new Trade();
        trade.setTradeId(1L);
        trade.setTradeReference("TRD-001");
        trade.setCounterparty(createValidCounterparty());
        trade.setBaseCurrency("EUR");
        trade.setQuoteCurrency("USD");
        trade.setNotionalAmount(new BigDecimal("100000.00"));
        trade.setStrikePrice(new BigDecimal("1.2500"));
        trade.setSpotRate(new BigDecimal("1.2000"));
        trade.setTradeDate(LocalDate.now());
        trade.setValueDate(LocalDate.now().plusDays(2));
        trade.setMaturityDate(LocalDate.now().plusDays(30));
        trade.setOptionType(OptionType.CALL);
        trade.setStatus(TradeStatus.PENDING);
        trade.setCreatedBy("TEST_USER");
        return trade;
    }

    private TradeResponse createValidTradeResponse() {
        TradeResponse response = new TradeResponse();
        response.setTradeId(1L);
        response.setTradeReference("TRD-001");
        response.setBaseCurrency("EUR");
        response.setQuoteCurrency("USD");
        response.setNotionalAmount(new BigDecimal("100000.00"));
        response.setStrikePrice(new BigDecimal("1.2500"));
        response.setStatus(TradeStatus.PENDING);
        return response;
    }

    private Counterparty createValidCounterparty() {
        Counterparty counterparty = new Counterparty();
        counterparty.setCounterpartyId(1L);
        counterparty.setCounterpartyCode("CP001");
        counterparty.setName("Test Counterparty");
        counterparty.setIsActive(true);
        return counterparty;
    }
}
