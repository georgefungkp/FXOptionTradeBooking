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
import java.time.LocalDateTime;
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
    private org.george.fxoptiontradebooking.service.impl.TradeValidationService tradeValidationService;

    @Mock
    private TradeFactoryService tradeFactoryService;

    @Mock
    private TradeBusinessLogicService tradeBusinessLogicService;

    @Mock
    private TradeQueryService tradeQueryService;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private TradeServiceImpl tradeService;

    private TradeBookingRequest validRequest;
    private VanillaOptionTrade validTrade;
    private TradeResponse validResponse;
    private Counterparty validCounterparty;

    @BeforeEach
    void setUp() {
        validRequest = createValidTradeBookingRequest();
        validTrade = createValidVanillaOptionTrade();
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
            when(tradeValidationService.validateAndGetCounterparty(anyLong())).thenReturn(validCounterparty);
            when(tradeFactoryService.createTradeEntity(any(TradeBookingRequest.class), any(Counterparty.class))).thenReturn(validTrade);
            when(tradeBusinessLogicService.saveTradeWithAudit(any(Trade.class))).thenReturn(validTrade);
            when(modelMapper.map(any(Trade.class), eq(TradeResponse.class))).thenReturn(validResponse);
            
            doNothing().when(tradeValidationService).performPreTradeValidation(any());
            doNothing().when(tradeValidationService).validateCounterpartyEligibility(any());
            doNothing().when(tradeValidationService).validateUniqueTradeReference(any());
            doNothing().when(tradeBusinessLogicService).applyBusinessLogic(any(), any());
            doNothing().when(tradeBusinessLogicService).performPostTradeProcessing(any());

            // When
            TradeResponse result = tradeService.bookTrade(validRequest);

            // Then
            assertNotNull(result);
            assertEquals(validResponse.getTradeReference(), result.getTradeReference());
            
            verify(tradeValidationService).performPreTradeValidation(validRequest);
            verify(tradeValidationService).validateAndGetCounterparty(validRequest.getCounterpartyId());
            verify(tradeValidationService).validateCounterpartyEligibility(validCounterparty);
            verify(tradeValidationService).validateUniqueTradeReference(validRequest.getTradeReference());
            verify(tradeFactoryService).createTradeEntity(validRequest, validCounterparty);
            verify(tradeBusinessLogicService).applyBusinessLogic(validTrade, validRequest);
            verify(tradeBusinessLogicService).saveTradeWithAudit(validTrade);
            verify(tradeBusinessLogicService).performPostTradeProcessing(validTrade);
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
        @DisplayName("Should throw exception for validation failure")
        void shouldThrowExceptionForValidationFailure() {
            // Given
            doThrow(new BusinessValidationException("Validation failed"))
                .when(tradeValidationService).performPreTradeValidation(any());

            // When & Then
            BusinessValidationException exception = assertThrows(
                BusinessValidationException.class,
                () -> tradeService.bookTrade(validRequest)
            );
            assertEquals("Validation failed", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for non-existent counterparty")
        void shouldThrowExceptionForNonExistentCounterparty() {
            // Given
            doNothing().when(tradeValidationService).performPreTradeValidation(any());
            when(tradeValidationService.validateAndGetCounterparty(anyLong()))
                .thenThrow(new TradeNotFoundException("Counterparty not found with ID: 1"));

            // When & Then
            TradeNotFoundException exception = assertThrows(
                TradeNotFoundException.class,
                () -> tradeService.bookTrade(validRequest)
            );
            assertTrue(exception.getMessage().contains("Counterparty not found"));
        }

        @Test
        @DisplayName("Should throw exception for inactive counterparty")
        void shouldThrowExceptionForInactiveCounterparty() {
            // Given
            doNothing().when(tradeValidationService).performPreTradeValidation(any());
            when(tradeValidationService.validateAndGetCounterparty(anyLong())).thenReturn(validCounterparty);
            doThrow(new BusinessValidationException("Cannot trade with inactive counterparty: Test Counterparty"))
                .when(tradeValidationService).validateCounterpartyEligibility(any());

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
            doNothing().when(tradeValidationService).performPreTradeValidation(any());
            when(tradeValidationService.validateAndGetCounterparty(anyLong())).thenReturn(validCounterparty);
            doNothing().when(tradeValidationService).validateCounterpartyEligibility(any());
            doThrow(new BusinessValidationException("Trade reference already exists: TRD-001"))
                .when(tradeValidationService).validateUniqueTradeReference(any());

            // When & Then
            BusinessValidationException exception = assertThrows(
                BusinessValidationException.class,
                () -> tradeService.bookTrade(validRequest)
            );
            assertTrue(exception.getMessage().contains("Trade reference already exists"));
        }

        @Test
        @DisplayName("Should handle unexpected exception")
        void shouldHandleUnexpectedException() {
            // Given
            doNothing().when(tradeValidationService).performPreTradeValidation(any());
            when(tradeValidationService.validateAndGetCounterparty(anyLong())).thenReturn(validCounterparty);
            doNothing().when(tradeValidationService).validateCounterpartyEligibility(any());
            doNothing().when(tradeValidationService).validateUniqueTradeReference(any());
            when(tradeFactoryService.createTradeEntity(any(), any()))
                .thenThrow(new RuntimeException("Unexpected error"));

            // When & Then
            BusinessValidationException exception = assertThrows(
                BusinessValidationException.class,
                () -> tradeService.bookTrade(validRequest)
            );
            assertEquals("Trade booking failed due to unexpected error", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Update Trade Status Tests")
    class UpdateTradeStatusTests {

        @Test
        @DisplayName("Should update trade status successfully")
        void shouldUpdateTradeStatusSuccessfully() {
            // Given
            Long tradeId = 1L;
            TradeStatus newStatus = TradeStatus.CONFIRMED;
            validTrade.setStatus(TradeStatus.PENDING);
            
            when(tradeRepository.findById(tradeId)).thenReturn(Optional.of(validTrade));
            when(tradeRepository.save(any(Trade.class))).thenReturn(validTrade);
            when(modelMapper.map(any(Trade.class), eq(TradeResponse.class))).thenReturn(validResponse);
            doNothing().when(tradeValidationService).validateStatusTransition(any(), any());
            doNothing().when(tradeBusinessLogicService).handleStatusChangeEvents(any(), any(), any());

            // When
            TradeResponse result = tradeService.updateTradeStatus(tradeId, newStatus);

            // Then
            assertNotNull(result);
            verify(tradeValidationService).validateStatusTransition(TradeStatus.PENDING, newStatus);
            verify(tradeRepository).save(validTrade);
            verify(tradeBusinessLogicService).handleStatusChangeEvents(validTrade, TradeStatus.PENDING, newStatus);
        }

        @Test
        @DisplayName("Should throw exception for invalid trade ID")
        void shouldThrowExceptionForInvalidTradeId() {
            // When & Then
            BusinessValidationException exception = assertThrows(
                BusinessValidationException.class,
                () -> tradeService.updateTradeStatus(null, TradeStatus.CONFIRMED)
            );
            assertEquals("Trade ID must be a positive number", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for null status")
        void shouldThrowExceptionForNullStatus() {
            // When & Then
            BusinessValidationException exception = assertThrows(
                BusinessValidationException.class,
                () -> tradeService.updateTradeStatus(1L, null)
            );
            assertEquals("New status cannot be null", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when trade not found")
        void shouldThrowExceptionWhenTradeNotFound() {
            // Given
            Long tradeId = 999L;
            when(tradeRepository.findById(tradeId)).thenReturn(Optional.empty());

            // When & Then
            TradeNotFoundException exception = assertThrows(
                TradeNotFoundException.class,
                () -> tradeService.updateTradeStatus(tradeId, TradeStatus.CONFIRMED)
            );
            assertEquals("Trade not found with ID: " + tradeId, exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Cancel Trade Tests")
    class CancelTradeTests {

        @Test
        @DisplayName("Should cancel trade successfully")
        void shouldCancelTradeSuccessfully() {
            // Given
            Long tradeId = 1L;
            validTrade.setStatus(TradeStatus.PENDING);
            validTrade.setTradeDate(LocalDate.now());
            
            when(tradeRepository.findById(tradeId)).thenReturn(Optional.of(validTrade));
            when(tradeRepository.save(any(Trade.class))).thenReturn(validTrade);
            doNothing().when(tradeValidationService).validateTradeForCancellation(any());

            // When
            tradeService.cancelTrade(tradeId);

            // Then
            verify(tradeValidationService).validateTradeForCancellation(validTrade);
            verify(tradeRepository).save(validTrade);
            assertEquals(TradeStatus.CANCELLED, validTrade.getStatus());
        }

        @Test
        @DisplayName("Should throw exception for invalid trade ID")
        void shouldThrowExceptionForInvalidTradeId() {
            // When & Then
            BusinessValidationException exception = assertThrows(
                BusinessValidationException.class,
                () -> tradeService.cancelTrade(null)
            );
            assertEquals("Trade ID must be a positive number", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when trade not found")
        void shouldThrowExceptionWhenTradeNotFound() {
            // Given
            Long tradeId = 999L;
            when(tradeRepository.findById(tradeId)).thenReturn(Optional.empty());

            // When & Then
            TradeNotFoundException exception = assertThrows(
                TradeNotFoundException.class,
                () -> tradeService.cancelTrade(tradeId)
            );
            assertEquals("Trade not found with ID: " + tradeId, exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for validation failure")
        void shouldThrowExceptionForValidationFailure() {
            // Given
            Long tradeId = 1L;
            when(tradeRepository.findById(tradeId)).thenReturn(Optional.of(validTrade));
            doThrow(new BusinessValidationException("Only PENDING trades can be cancelled"))
                .when(tradeValidationService).validateTradeForCancellation(any());

            // When & Then
            BusinessValidationException exception = assertThrows(
                BusinessValidationException.class,
                () -> tradeService.cancelTrade(tradeId)
            );
            assertEquals("Only PENDING trades can be cancelled", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Query Delegation Tests")
    class QueryDelegationTests {

        @Test
        @DisplayName("Should delegate getTradeById to TradeQueryService")
        void shouldDelegateGetTradeById() {
            // Given
            Long tradeId = 1L;
            when(tradeQueryService.getTradeById(tradeId)).thenReturn(validResponse);

            // When
            TradeResponse result = tradeService.getTradeById(tradeId);

            // Then
            assertNotNull(result);
            assertEquals(validResponse.getTradeReference(), result.getTradeReference());
            verify(tradeQueryService).getTradeById(tradeId);
        }

        @Test
        @DisplayName("Should delegate getTradeByReference to TradeQueryService")
        void shouldDelegateGetTradeByReference() {
            // Given
            String tradeReference = "TRD-001";
            when(tradeQueryService.getTradeByReference(tradeReference)).thenReturn(validResponse);

            // When
            TradeResponse result = tradeService.getTradeByReference(tradeReference);

            // Then
            assertNotNull(result);
            assertEquals(validResponse.getTradeReference(), result.getTradeReference());
            verify(tradeQueryService).getTradeByReference(tradeReference);
        }

        @Test
        @DisplayName("Should delegate getTradesByCounterparty to TradeQueryService")
        void shouldDelegateGetTradesByCounterparty() {
            // Given
            Long counterpartyId = 1L;
            List<TradeResponse> expectedTrades = Arrays.asList(validResponse);
            when(tradeQueryService.getTradesByCounterparty(counterpartyId)).thenReturn(expectedTrades);

            // When
            List<TradeResponse> result = tradeService.getTradesByCounterparty(counterpartyId);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            verify(tradeQueryService).getTradesByCounterparty(counterpartyId);
        }

        @Test
        @DisplayName("Should delegate getTradesByStatus to TradeQueryService")
        void shouldDelegateGetTradesByStatus() {
            // Given
            TradeStatus status = TradeStatus.PENDING;
            List<TradeResponse> expectedTrades = Arrays.asList(validResponse);
            when(tradeQueryService.getTradesByStatus(status)).thenReturn(expectedTrades);

            // When
            List<TradeResponse> result = tradeService.getTradesByStatus(status);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            verify(tradeQueryService).getTradesByStatus(status);
        }

        @Test
        @DisplayName("Should delegate getAllTrades to TradeQueryService")
        void shouldDelegateGetAllTrades() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<TradeResponse> expectedPage = new PageImpl<>(Arrays.asList(validResponse));
            when(tradeQueryService.getAllTrades(pageable)).thenReturn(expectedPage);

            // When
            Page<TradeResponse> result = tradeService.getAllTrades(pageable);

            // Then
            assertNotNull(result);
            assertEquals(1, result.getContent().size());
            verify(tradeQueryService).getAllTrades(pageable);
        }
    }

    @Nested
    @DisplayName("Product-Specific Trade Tests")
    class ProductSpecificTradeTests {

        @Test
        @DisplayName("Should book exotic option successfully")
        void shouldBookExoticOptionSuccessfully() {
            // Given
            TradeBookingRequest exoticRequest = createExoticOptionRequest();
            ExoticOptionTrade exoticTrade = createValidExoticOptionTrade();
            
            when(tradeValidationService.validateAndGetCounterparty(anyLong())).thenReturn(validCounterparty);
            when(tradeFactoryService.createTradeEntity(any(TradeBookingRequest.class), any(Counterparty.class))).thenReturn(exoticTrade);
            when(tradeBusinessLogicService.saveTradeWithAudit(any(Trade.class))).thenReturn(exoticTrade);
            when(modelMapper.map(any(Trade.class), eq(TradeResponse.class))).thenReturn(validResponse);
            
            doNothing().when(tradeValidationService).performPreTradeValidation(any());
            doNothing().when(tradeValidationService).validateCounterpartyEligibility(any());
            doNothing().when(tradeValidationService).validateUniqueTradeReference(any());
            doNothing().when(tradeBusinessLogicService).applyBusinessLogic(any(), any());
            doNothing().when(tradeBusinessLogicService).performPostTradeProcessing(any());

            // When
            TradeResponse result = tradeService.bookTrade(exoticRequest);

            // Then
            assertNotNull(result);
            verify(tradeFactoryService).createTradeEntity(exoticRequest, validCounterparty);
        }

        @Test
        @DisplayName("Should book FX trade successfully")
        void shouldBookFXTradeSuccessfully() {
            // Given
            TradeBookingRequest fxRequest = createFXTradeRequest();
            FXTrade fxTrade = createValidFXTrade();
            
            when(tradeValidationService.validateAndGetCounterparty(anyLong())).thenReturn(validCounterparty);
            when(tradeFactoryService.createTradeEntity(any(TradeBookingRequest.class), any(Counterparty.class))).thenReturn(fxTrade);
            when(tradeBusinessLogicService.saveTradeWithAudit(any(Trade.class))).thenReturn(fxTrade);
            when(modelMapper.map(any(Trade.class), eq(TradeResponse.class))).thenReturn(validResponse);
            
            doNothing().when(tradeValidationService).performPreTradeValidation(any());
            doNothing().when(tradeValidationService).validateCounterpartyEligibility(any());
            doNothing().when(tradeValidationService).validateUniqueTradeReference(any());
            doNothing().when(tradeBusinessLogicService).applyBusinessLogic(any(), any());
            doNothing().when(tradeBusinessLogicService).performPostTradeProcessing(any());

            // When
            TradeResponse result = tradeService.bookTrade(fxRequest);

            // Then
            assertNotNull(result);
            verify(tradeFactoryService).createTradeEntity(fxRequest, validCounterparty);
        }

        @Test
        @DisplayName("Should book swap trade successfully")
        void shouldBookSwapTradeSuccessfully() {
            // Given
            TradeBookingRequest swapRequest = createSwapTradeRequest();
            SwapTrade swapTrade = createValidSwapTrade();
            
            when(tradeValidationService.validateAndGetCounterparty(anyLong())).thenReturn(validCounterparty);
            when(tradeFactoryService.createTradeEntity(any(TradeBookingRequest.class), any(Counterparty.class))).thenReturn(swapTrade);
            when(tradeBusinessLogicService.saveTradeWithAudit(any(Trade.class))).thenReturn(swapTrade);
            when(modelMapper.map(any(Trade.class), eq(TradeResponse.class))).thenReturn(validResponse);
            
            doNothing().when(tradeValidationService).performPreTradeValidation(any());
            doNothing().when(tradeValidationService).validateCounterpartyEligibility(any());
            doNothing().when(tradeValidationService).validateUniqueTradeReference(any());
            doNothing().when(tradeBusinessLogicService).applyBusinessLogic(any(), any());
            doNothing().when(tradeBusinessLogicService).performPostTradeProcessing(any());

            // When
            TradeResponse result = tradeService.bookTrade(swapRequest);

            // Then
            assertNotNull(result);
            verify(tradeFactoryService).createTradeEntity(swapRequest, validCounterparty);
        }
    }

    // Helper methods to create test data
    private TradeBookingRequest createValidTradeBookingRequest() {
        TradeBookingRequest request = new TradeBookingRequest();
        request.setTradeReference("TRD-001");
        request.setCounterpartyId(1L);
        request.setProductType(ProductType.VANILLA_OPTION);
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

    private TradeBookingRequest createExoticOptionRequest() {
        TradeBookingRequest request = new TradeBookingRequest();
        request.setTradeReference("EO-001");
        request.setCounterpartyId(1L);
        request.setProductType(ProductType.EXOTIC_OPTION);
        request.setBaseCurrency("EUR");
        request.setQuoteCurrency("USD");
        request.setNotionalAmount(new BigDecimal("100000.00"));
        request.setStrikePrice(new BigDecimal("1.2500"));
        request.setSpotRate(new BigDecimal("1.2000"));
        request.setTradeDate(LocalDate.now());
        request.setValueDate(LocalDate.now().plusDays(2));
        request.setMaturityDate(LocalDate.now().plusDays(30));
        request.setOptionType(OptionType.CALL);
        request.setExoticOptionType(ExoticOptionType.BARRIER_OPTION);
        request.setBarrierLevel(new BigDecimal("1.3000"));
        request.setCreatedBy("TEST_USER");
        return request;
    }

    private TradeBookingRequest createFXTradeRequest() {
        TradeBookingRequest request = new TradeBookingRequest();
        request.setTradeReference("FX-001");
        request.setCounterpartyId(1L);
        request.setProductType(ProductType.FX_FORWARD);
        request.setBaseCurrency("EUR");
        request.setQuoteCurrency("USD");
        request.setNotionalAmount(new BigDecimal("100000.00"));
        request.setForwardRate(new BigDecimal("1.2500"));
        request.setSpotRate(new BigDecimal("1.2000"));
        request.setTradeDate(LocalDate.now());
        request.setValueDate(LocalDate.now().plusDays(30));
        request.setCreatedBy("TEST_USER");
        return request;
    }

    private TradeBookingRequest createSwapTradeRequest() {
        TradeBookingRequest request = new TradeBookingRequest();
        request.setTradeReference("SWAP-001");
        request.setCounterpartyId(1L);
        request.setProductType(ProductType.FX_SWAP);
        request.setBaseCurrency("USD");
        request.setQuoteCurrency("USD");
        request.setNotionalAmount(new BigDecimal("1000000.00"));
        request.setTradeDate(LocalDate.now());
        request.setValueDate(LocalDate.now().plusDays(2));
        request.setMaturityDate(LocalDate.now().plusYears(5));
        request.setSwapType(SwapType.INTEREST_RATE_SWAP);
        request.setFixedRate(new BigDecimal("2.5"));
        request.setFloatingRateIndex("SOFR");
        request.setCreatedBy("TEST_USER");
        return request;
    }

    private VanillaOptionTrade createValidVanillaOptionTrade() {
        VanillaOptionTrade trade = new VanillaOptionTrade();
        trade.setTradeId(1L);
        trade.setTradeReference("TRD-001");
        trade.setCounterparty(createValidCounterparty());
        trade.setBaseCurrency("EUR");
        trade.setQuoteCurrency("USD");
        trade.setNotionalAmount(new BigDecimal("100000.00"));
        trade.setTradeDate(LocalDate.now());
        trade.setValueDate(LocalDate.now().plusDays(2));
        trade.setMaturityDate(LocalDate.now().plusDays(30));
        trade.setStatus(TradeStatus.PENDING);
        trade.setCreatedBy("TEST_USER");
        trade.setCreatedAt(LocalDateTime.now());
        trade.setUpdatedAt(LocalDateTime.now());
        
        // Option-specific fields
        trade.setOptionType(OptionType.CALL);
        trade.setStrikePrice(new BigDecimal("1.2500"));
        trade.setSpotRate(new BigDecimal("1.2000"));
        trade.setPremiumAmount(new BigDecimal("2500.00"));
        trade.setPremiumCurrency("EUR");
        
        return trade;
    }

    private ExoticOptionTrade createValidExoticOptionTrade() {
        ExoticOptionTrade trade = new ExoticOptionTrade();
        trade.setTradeId(2L);
        trade.setTradeReference("EO-001");
        trade.setCounterparty(createValidCounterparty());
        trade.setBaseCurrency("EUR");
        trade.setQuoteCurrency("USD");
        trade.setNotionalAmount(new BigDecimal("100000.00"));
        trade.setTradeDate(LocalDate.now());
        trade.setValueDate(LocalDate.now().plusDays(2));
        trade.setMaturityDate(LocalDate.now().plusDays(30));
        trade.setStatus(TradeStatus.PENDING);
        trade.setCreatedBy("TEST_USER");
        trade.setCreatedAt(LocalDateTime.now());
        trade.setUpdatedAt(LocalDateTime.now());
        
        // Option-specific fields
        trade.setOptionType(OptionType.CALL);
        trade.setStrikePrice(new BigDecimal("1.2500"));
        trade.setSpotRate(new BigDecimal("1.2000"));
        trade.setPremiumAmount(new BigDecimal("3000.00"));
        trade.setPremiumCurrency("EUR");
        
        // Exotic-specific fields
        trade.setExoticOptionType(ExoticOptionType.BARRIER_OPTION);
        trade.setBarrierLevel(new BigDecimal("1.3000"));
        trade.setKnockInOut("KNOCK_OUT");
        
        return trade;
    }

    private FXTrade createValidFXTrade() {
        FXTrade trade = new FXTrade();
        trade.setTradeId(3L);
        trade.setTradeReference("FX-001");
        trade.setCounterparty(createValidCounterparty());
        trade.setBaseCurrency("EUR");
        trade.setQuoteCurrency("USD");
        trade.setNotionalAmount(new BigDecimal("100000.00"));
        trade.setTradeDate(LocalDate.now());
        trade.setValueDate(LocalDate.now().plusDays(30));
        trade.setStatus(TradeStatus.PENDING);
        trade.setCreatedBy("TEST_USER");
        trade.setCreatedAt(LocalDateTime.now());
        trade.setUpdatedAt(LocalDateTime.now());
        
        // FX-specific fields
        trade.setForwardRate(new BigDecimal("1.2500"));
        trade.setSpotRate(new BigDecimal("1.2000"));
        trade.setIsSpotTrade(false);
        
        return trade;
    }

    private SwapTrade createValidSwapTrade() {
        SwapTrade trade = new SwapTrade();
        trade.setTradeId(4L);
        trade.setTradeReference("SWAP-001");
        trade.setCounterparty(createValidCounterparty());
        trade.setBaseCurrency("USD");
        trade.setQuoteCurrency("USD");
        trade.setNotionalAmount(new BigDecimal("1000000.00"));
        trade.setTradeDate(LocalDate.now());
        trade.setValueDate(LocalDate.now().plusDays(2));
        trade.setMaturityDate(LocalDate.now().plusYears(5));
        trade.setStatus(TradeStatus.PENDING);
        trade.setCreatedBy("TEST_USER");
        trade.setCreatedAt(LocalDateTime.now());
        trade.setUpdatedAt(LocalDateTime.now());
        
        // Swap-specific fields
        trade.setSwapType(SwapType.INTEREST_RATE_SWAP);
        trade.setFixedRate(new BigDecimal("2.5"));
        trade.setFloatingRateIndex("SOFR");
        trade.setPaymentFrequency("QUARTERLY");
        
        return trade;
    }

    private TradeResponse createValidTradeResponse() {
        TradeResponse response = new TradeResponse();
        response.setTradeId(1L);
        response.setTradeReference("TRD-001");
        response.setProductType(ProductType.VANILLA_OPTION);
        response.setBaseCurrency("EUR");
        response.setQuoteCurrency("USD");
        response.setNotionalAmount(new BigDecimal("100000.00"));
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