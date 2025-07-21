package org.george.fxoptiontradebooking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.george.fxoptiontradebooking.config.TestSecurityConfig;
import org.george.fxoptiontradebooking.dto.request.TradeBookingRequest;
import org.george.fxoptiontradebooking.dto.response.TradeResponse;
import org.george.fxoptiontradebooking.entity.OptionType;
import org.george.fxoptiontradebooking.exception.GlobalExceptionHandler;
import org.george.fxoptiontradebooking.service.TradeService;
import org.george.fxoptiontradebooking.config.SecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TradeController.class)
@ContextConfiguration(classes = {
        TradeController.class,
        GlobalExceptionHandler.class,
        TestSecurityConfig.class, // Test-specific security configuration
        TradeControllerTest.TestConfig.class
})
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
    }

    @Nested
    @DisplayName("Book Trade Tests")
    class BookTradeTests {

        @Test
        @DisplayName("Should book trade successfully")
        void shouldBookTradeSuccessfully() throws Exception {
            when(tradeService.bookTrade(any(TradeBookingRequest.class))).thenReturn(validResponse);

            mockMvc.perform(post("/api/trades")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.tradeReference").value(validResponse.getTradeReference()));

            verify(tradeService).bookTrade(any(TradeBookingRequest.class));
        }
    }

    // Other nested test cases...

    private TradeBookingRequest createValidTradeBookingRequest() {
        TradeBookingRequest request = new TradeBookingRequest();
        request.setTradeReference("TRD-001"); // Required
        request.setCounterpartyId(1L);        // Required
        request.setBaseCurrency("EUR");
        request.setQuoteCurrency("USD");
        request.setNotionalAmount(new BigDecimal("100000.00"));
        request.setStrikePrice(new BigDecimal("1.2500")); // Required
        request.setSpotRate(new BigDecimal("1.2000"));
        request.setTradeDate(LocalDate.now());
        request.setValueDate(LocalDate.now().plusDays(2)); // Required
        request.setMaturityDate(LocalDate.now().plusDays(30)); // Required
        request.setOptionType(OptionType.CALL); // Required
        request.setCreatedBy("Test User"); // Optional
        return request;
    }


    private TradeResponse createValidTradeResponse() {
        TradeResponse response = new TradeResponse();
        response.setTradeId(1L);
        response.setTradeReference("TRD-001");
        return response;
    }
}