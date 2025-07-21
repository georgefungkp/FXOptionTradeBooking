package org.george.fxoptiontradebooking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.george.fxoptiontradebooking.dto.request.CounterpartyRequest;
import org.george.fxoptiontradebooking.dto.response.CounterpartyResponse;
import org.george.fxoptiontradebooking.exception.BusinessValidationException;
import org.george.fxoptiontradebooking.exception.GlobalExceptionHandler;
import org.george.fxoptiontradebooking.exception.TradeNotFoundException;
import org.george.fxoptiontradebooking.service.CounterpartyService;
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

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CounterpartyController.class)
@ContextConfiguration(classes = {CounterpartyController.class, GlobalExceptionHandler.class, CounterpartyControllerTest.TestConfig.class})
@DisplayName("CounterpartyController Tests")
class CounterpartyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CounterpartyService counterpartyService;

    @Autowired
    private ObjectMapper objectMapper;

    private CounterpartyRequest validRequest;
    private CounterpartyResponse validResponse;

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public CounterpartyService counterpartyService() {
            return mock(CounterpartyService.class);
        }
    }

    @BeforeEach
    void setUp() {
        validRequest = createValidCounterpartyRequest();
        validResponse = createValidCounterpartyResponse();
        // Reset the mock before each test
        reset(counterpartyService);
    }

    @Nested
    @DisplayName("Create Counterparty Tests")
    class CreateCounterpartyTests {

        @Test
        @DisplayName("Should create counterparty successfully")
        void shouldCreateCounterpartySuccessfully() throws Exception {
            // Given
            when(counterpartyService.createCounterparty(any(CounterpartyRequest.class))).thenReturn(validResponse);

            // When & Then
            mockMvc.perform(post("/api/v1/counterparties")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Counterparty created successfully"))
                    .andExpect(jsonPath("$.data.counterpartyCode").value(validResponse.getCounterpartyCode()));

            verify(counterpartyService).createCounterparty(any(CounterpartyRequest.class));
        }

        @Test
        @DisplayName("Should return bad request for business validation exception")
        void shouldReturnBadRequestForBusinessValidationException() throws Exception {
            // Given
            when(counterpartyService.createCounterparty(any(CounterpartyRequest.class)))
                    .thenThrow(new BusinessValidationException("Counterparty code already exists"));

            // When & Then
            mockMvc.perform(post("/api/v1/counterparties")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Validation failed"))
                    .andExpect(jsonPath("$.error").value("Counterparty code already exists"));
        }
    }

    @Nested
    @DisplayName("Get Counterparty Tests")
    class GetCounterpartyTests {

        @Test
        @DisplayName("Should get counterparty by ID successfully")
        void shouldGetCounterpartyByIdSuccessfully() throws Exception {
            // Given
            Long counterpartyId = 1L;
            when(counterpartyService.getCounterpartyById(counterpartyId)).thenReturn(validResponse);

            // When & Then
            mockMvc.perform(get("/api/v1/counterparties/{id}", counterpartyId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.counterpartyId").value(counterpartyId))
                    .andExpect(jsonPath("$.data.counterpartyCode").value(validResponse.getCounterpartyCode()));

            verify(counterpartyService).getCounterpartyById(counterpartyId);
        }

        @Test
        @DisplayName("Should return not found for non-existent counterparty")
        void shouldReturnNotFoundForNonExistentCounterparty() throws Exception {
            // Given
            Long counterpartyId = 999L;
            when(counterpartyService.getCounterpartyById(counterpartyId))
                    .thenThrow(new TradeNotFoundException("Counterparty not found with ID: " + counterpartyId));

            // When & Then
            mockMvc.perform(get("/api/v1/counterparties/{id}", counterpartyId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Trade not found"))
                    .andExpect(jsonPath("$.error").value("Counterparty not found with ID: " + counterpartyId));
        }

        @Test
        @DisplayName("Should get all active counterparties successfully")
        void shouldGetAllActiveCounterpartiesSuccessfully() throws Exception {
            // Given
            List<CounterpartyResponse> responses = Arrays.asList(validResponse);
            when(counterpartyService.getAllActiveCounterparties()).thenReturn(responses);

            // When & Then
            mockMvc.perform(get("/api/v1/counterparties/active"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[0].counterpartyCode").value(validResponse.getCounterpartyCode()));

            verify(counterpartyService).getAllActiveCounterparties();
        }
    }

    private CounterpartyRequest createValidCounterpartyRequest() {
        CounterpartyRequest request = new CounterpartyRequest();
        request.setCounterpartyCode("CP001");
        request.setName("Goldman Sachs");
        request.setLeiCode("7LTWFZYICNSX8D621K86");
        request.setSwiftCode("GSCCUS33");
        request.setCreditRating("A+");
        request.setIsActive(true);
        return request;
    }

    private CounterpartyResponse createValidCounterpartyResponse() {
        CounterpartyResponse response = new CounterpartyResponse();
        response.setCounterpartyId(1L);
        response.setCounterpartyCode("CP001");
        response.setName("Goldman Sachs");
        response.setLeiCode("7LTWFZYICNSX8D621K86");
        response.setSwiftCode("GSCCUS33");
        response.setCreditRating("A+");
        response.setIsActive(true);
        return response;
    }
}