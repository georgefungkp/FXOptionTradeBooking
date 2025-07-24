package org.george.fxoptiontradebooking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.george.fxoptiontradebooking.config.TestSecurityConfig;
import org.george.fxoptiontradebooking.dto.request.TradeBookingRequest;
import org.george.fxoptiontradebooking.dto.response.TradeResponse;
import org.george.fxoptiontradebooking.entity.*;
import org.george.fxoptiontradebooking.exception.TradeNotFoundException;
import org.george.fxoptiontradebooking.service.TradeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TradeController.class)
@ContextConfiguration(classes = {TestSecurityConfig.class, TradeController.class})
@DisplayName("Multi-Product Trade Controller Tests")
class TradeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TradeService tradeService;

    @Autowired
    private ObjectMapper objectMapper;

    private TradeResponse vanillaOptionResponse;
    private TradeResponse exoticOptionResponse;
    private TradeResponse fxForwardResponse;
    private TradeResponse swapResponse;

    @BeforeEach
    void setUp() {
        vanillaOptionResponse = createVanillaOptionResponse();
        exoticOptionResponse = createExoticOptionResponse();
        fxForwardResponse = createFXForwardResponse();
        swapResponse = createSwapResponse();
    }

    @Nested
    @DisplayName("Vanilla Option Tests")
    class VanillaOptionTests {

        @Test
        @DisplayName("Should book vanilla option successfully")
        void shouldBookVanillaOptionSuccessfully() throws Exception {
            TradeBookingRequest request = createVanillaOptionRequest();
            when(tradeService.bookTrade(any(TradeBookingRequest.class))).thenReturn(vanillaOptionResponse);

            mockMvc.perform(post("/api/v1/trades")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.productType").value("VANILLA_OPTION"))
                    .andExpect(jsonPath("$.data.optionType").value("CALL"));

            verify(tradeService).bookTrade(any(TradeBookingRequest.class));
        }

        @Test
        @DisplayName("Should get vanilla options expiring between dates")
        void shouldGetVanillaOptionsExpiringBetween() throws Exception {
            LocalDate startDate = LocalDate.now();
            LocalDate endDate = LocalDate.now().plusDays(30);

            when(tradeService.getVanillaOptionsExpiringBetween(startDate, endDate))
                .thenReturn(List.of(vanillaOptionResponse));

            mockMvc.perform(get("/api/v1/trades/options/vanilla/expiring")
                            .param("startDate", startDate.toString())
                            .param("endDate", endDate.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[0].productType").value("VANILLA_OPTION"));
        }
    }

    @Nested
    @DisplayName("Exotic Option Tests")
    class ExoticOptionTests {

        @Test
        @DisplayName("Should book barrier option successfully")
        void shouldBookBarrierOptionSuccessfully() throws Exception {
            TradeBookingRequest request = createBarrierOptionRequest();
            when(tradeService.bookTrade(any(TradeBookingRequest.class))).thenReturn(exoticOptionResponse);

            mockMvc.perform(post("/api/v1/trades")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.productType").value("EXOTIC_OPTION"))
                    .andExpect(jsonPath("$.data.exoticOptionType").value("BARRIER_OPTION"));
        }

        @Test
        @DisplayName("Should get exotic options by type")
        void shouldGetExoticOptionsByType() throws Exception {
            when(tradeService.getExoticOptionsByType(ExoticOptionType.BARRIER_OPTION))
                .thenReturn(List.of(exoticOptionResponse));

            mockMvc.perform(get("/api/v1/trades/options/exotic/BARRIER_OPTION"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data[0].exoticOptionType").value("BARRIER_OPTION"));
        }
    }

    @Nested
    @DisplayName("FX Contract Tests")
    class FXContractTests {

        @Test
        @DisplayName("Should book FX forward successfully")
        void shouldBookFXForwardSuccessfully() throws Exception {
            TradeBookingRequest request = createFXForwardRequest();
            when(tradeService.bookTrade(any(TradeBookingRequest.class))).thenReturn(fxForwardResponse);

            mockMvc.perform(post("/api/v1/trades")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.productType").value("FX_FORWARD"));
        }

        @Test
        @DisplayName("Should get all FX contracts")
        void shouldGetAllFXContracts() throws Exception {
            when(tradeService.getAllFXContracts()).thenReturn(List.of(fxForwardResponse));

            mockMvc.perform(get("/api/v1/trades/fx-contracts"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    @Nested
    @DisplayName("Swap Tests")
    class SwapTests {

        @Test
        @DisplayName("Should book interest rate swap successfully")
        void shouldBookInterestRateSwapSuccessfully() throws Exception {
            TradeBookingRequest request = createInterestRateSwapRequest();
            when(tradeService.bookTrade(any(TradeBookingRequest.class))).thenReturn(swapResponse);

            mockMvc.perform(post("/api/v1/trades")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.productType").value("INTEREST_RATE_SWAP"));
        }

        @Test
        @DisplayName("Should get swaps by type")
        void shouldGetSwapsByType() throws Exception {
            when(tradeService.getSwapsByType(SwapType.INTEREST_RATE_SWAP))
                .thenReturn(List.of(swapResponse));

            mockMvc.perform(get("/api/v1/trades/swaps/INTEREST_RATE_SWAP"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data[0].swapType").value("INTEREST_RATE_SWAP"));
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should return 400 for invalid trade booking request")
        void shouldReturn400ForInvalidRequest() throws Exception {
            TradeBookingRequest invalidRequest = new TradeBookingRequest(); // Missing required fields

            mockMvc.perform(post("/api/v1/trades")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
        }
    }

    // Helper methods to create test data
    private TradeBookingRequest createVanillaOptionRequest() {
        TradeBookingRequest request = new TradeBookingRequest();
        request.setTradeReference("VO-TEST-001");
        request.setCounterpartyId(1L);
        request.setProductType(ProductType.VANILLA_OPTION);
        request.setOptionType(OptionType.CALL);
        request.setBaseCurrency("EUR");
        request.setQuoteCurrency("USD");
        request.setNotionalAmount(new BigDecimal("1000000"));
        request.setStrikePrice(new BigDecimal("1.1000"));
        request.setSpotRate(new BigDecimal("1.0950"));
        request.setTradeDate(LocalDate.now());
        request.setValueDate(LocalDate.now().plusDays(2));
        request.setMaturityDate(LocalDate.now().plusDays(30));
        request.setCreatedBy("test-user");
        return request;
    }

    private TradeBookingRequest createBarrierOptionRequest() {
        TradeBookingRequest request = createVanillaOptionRequest();
        request.setTradeReference("EO-BARRIER-TEST-001");
        request.setProductType(ProductType.EXOTIC_OPTION);
        request.setExoticOptionType(ExoticOptionType.BARRIER_OPTION);
        request.setBarrierLevel(new BigDecimal("1.1500"));
        request.setKnockInOut("OUT");
        request.setObservationFrequency("CONTINUOUS");
        return request;
    }

    private TradeBookingRequest createFXForwardRequest() {
        TradeBookingRequest request = new TradeBookingRequest();
        request.setTradeReference("FX-FWD-TEST-001");
        request.setCounterpartyId(1L);
        request.setProductType(ProductType.FX_FORWARD);
        request.setBaseCurrency("GBP");
        request.setQuoteCurrency("USD");
        request.setNotionalAmount(new BigDecimal("2000000"));
        request.setForwardRate(new BigDecimal("1.2750"));
        request.setSpotRate(new BigDecimal("1.2700"));
        request.setTradeDate(LocalDate.now());
        request.setValueDate(LocalDate.now().plusDays(2));
        request.setMaturityDate(LocalDate.now().plusDays(90));
        request.setCreatedBy("test-user");
        return request;
    }

    private TradeBookingRequest createInterestRateSwapRequest() {
        TradeBookingRequest request = new TradeBookingRequest();
        request.setTradeReference("IRS-TEST-001");
        request.setCounterpartyId(1L);
        request.setProductType(ProductType.INTEREST_RATE_SWAP);
        request.setSwapType(SwapType.INTEREST_RATE_SWAP);
        request.setBaseCurrency("USD");
        request.setQuoteCurrency("USD");
        request.setNotionalAmount(new BigDecimal("10000000"));
        request.setFixedRate(new BigDecimal("4.25"));
        request.setFloatingRateIndex("SOFR");
        request.setPaymentFrequency("SEMI_ANNUAL");
        request.setTradeDate(LocalDate.now());
        request.setValueDate(LocalDate.now().plusDays(2));
        request.setMaturityDate(LocalDate.now().plusYears(5));
        request.setCreatedBy("test-user");
        return request;
    }

    private TradeResponse createVanillaOptionResponse() {
        TradeResponse response = new TradeResponse();
        response.setTradeId(1L);
        response.setTradeReference("VO-TEST-001");
        response.setProductType(ProductType.VANILLA_OPTION);
        response.setOptionType(OptionType.CALL);
        response.setBaseCurrency("EUR");
        response.setQuoteCurrency("USD");
        response.setNotionalAmount(new BigDecimal("1000000"));
        response.setStrikePrice(new BigDecimal("1.1000"));
        response.setStatus(TradeStatus.PENDING);
        response.setCreatedAt(LocalDateTime.now());
        return response;
    }

    private TradeResponse createExoticOptionResponse() {
        TradeResponse response = createVanillaOptionResponse();
        response.setTradeId(2L);
        response.setTradeReference("EO-BARRIER-TEST-001");
        response.setProductType(ProductType.EXOTIC_OPTION);
        response.setExoticOptionType(ExoticOptionType.BARRIER_OPTION);
        response.setBarrierLevel(new BigDecimal("1.1500"));
        return response;
    }

    private TradeResponse createFXForwardResponse() {
        TradeResponse response = new TradeResponse();
        response.setTradeId(3L);
        response.setTradeReference("FX-FWD-TEST-001");
        response.setProductType(ProductType.FX_FORWARD);
        response.setBaseCurrency("GBP");
        response.setQuoteCurrency("USD");
        response.setNotionalAmount(new BigDecimal("2000000"));
        response.setForwardRate(new BigDecimal("1.2750"));
        response.setStatus(TradeStatus.PENDING);
        response.setCreatedAt(LocalDateTime.now());
        return response;
    }

    private TradeResponse createSwapResponse() {
        TradeResponse response = new TradeResponse();
        response.setTradeId(4L);
        response.setTradeReference("IRS-TEST-001");
        response.setProductType(ProductType.INTEREST_RATE_SWAP);
        response.setSwapType(SwapType.INTEREST_RATE_SWAP);
        response.setBaseCurrency("USD");
        response.setQuoteCurrency("USD");
        response.setNotionalAmount(new BigDecimal("10000000"));
        response.setFixedRate(new BigDecimal("4.25"));
        response.setFloatingRateIndex("SOFR");
        response.setStatus(TradeStatus.PENDING);
        response.setCreatedAt(LocalDateTime.now());
        return response;
    }
}