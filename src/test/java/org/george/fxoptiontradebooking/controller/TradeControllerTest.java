package org.george.fxoptiontradebooking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.george.fxoptiontradebooking.dto.request.TradeBookingRequest;
import org.george.fxoptiontradebooking.dto.response.TradeResponse;
import org.george.fxoptiontradebooking.entity.OptionType;
import org.george.fxoptiontradebooking.entity.TradeStatus;
import org.george.fxoptiontradebooking.exception.BusinessValidationException;
import org.george.fxoptiontradebooking.exception.GlobalExceptionHandler;
import org.george.fxoptiontradebooking.exception.TradeNotFoundException;
import org.george.fxoptiontradebooking.service.TradeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TradeController.class)
@ContextConfiguration(classes = {TradeController.class, GlobalExceptionHandler.class, TradeControllerTest.TestConfig.class})
@DisplayName("TradeController Tests")
class TradeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TradeService tradeService;

    @Autowired
    private ObjectMapper objectMapper;

    private TradeBookingRequest validRequest;
    private TradeResponse validResponse;

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public TradeService tradeService() {
            return mock(TradeService.class);
        }
    }

    @BeforeEach
    void setUp() {
        validRequest = createValidTradeBookingRequest();
        validResponse = createValidTradeResponse();
        // Reset the mock before each test
        reset(tradeService);
    }

    @Nested
    @DisplayName("Book Trade Tests")
    class BookTradeTests {

        @Test
        @DisplayName("Should book trade successfully")
        void shouldBookTradeSuccessfully() throws Exception {
            // Given
            when(tradeService.bookTrade(any(TradeBookingRequest.class))).thenReturn(validResponse);

            // When & Then
            mockMvc.perform(post("/api/trades")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.tradeReference").value(validResponse.getTradeReference()))
                    .andExpect(jsonPath("$.data.status").value(validResponse.getStatus().toString()));

            verify(tradeService).bookTrade(any(TradeBookingRequest.class));
        }

        @Test
        @DisplayName("Should return bad request for business validation exception")
        void shouldReturnBadRequestForBusinessValidationException() throws Exception {
            // Given
            when(tradeService.bookTrade(any(TradeBookingRequest.class)))
                    .thenThrow(new BusinessValidationException("Trade reference already exists"));

            // When & Then
            mockMvc.perform(post("/api/trades")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Validation failed"))
                    .andExpect(jsonPath("$.error").value("Trade reference already exists"));
        }
    }

    @Nested
    @DisplayName("Get Trade Tests")
    class GetTradeTests {

        @Test
        @DisplayName("Should get trade by ID successfully")
        void shouldGetTradeByIdSuccessfully() throws Exception {
            // Given
            Long tradeId = 1L;
            when(tradeService.getTradeById(tradeId)).thenReturn(validResponse);

            // When & Then
            mockMvc.perform(get("/api/trades/{id}", tradeId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.tradeId").value(tradeId))
                    .andExpect(jsonPath("$.data.tradeReference").value(validResponse.getTradeReference()));

            verify(tradeService).getTradeById(tradeId);
        }

        @Test
        @DisplayName("Should return not found for non-existent trade")
        void shouldReturnNotFoundForNonExistentTrade() throws Exception {
            // Given
            Long tradeId = 999L;
            when(tradeService.getTradeById(tradeId))
                    .thenThrow(new TradeNotFoundException("Trade not found with ID: " + tradeId));

            // When & Then
            mockMvc.perform(get("/api/trades/{id}", tradeId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Trade not found"))
                    .andExpect(jsonPath("$.error").value("Trade not found with ID: " + tradeId));
        }

        @Test
        @DisplayName("Should get all trades with pagination successfully")
        void shouldGetAllTradesWithPaginationSuccessfully() throws Exception {
            // Given
            Page<TradeResponse> tradePage = new PageImpl<>(Arrays.asList(validResponse));
            when(tradeService.getAllTrades(any())).thenReturn(tradePage);

            // When & Then
            mockMvc.perform(get("/api/trades")
                    .param("page", "0")
                    .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray());

            verify(tradeService).getAllTrades(any());
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
}