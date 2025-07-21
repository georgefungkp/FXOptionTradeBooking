package org.george.fxoptiontradebooking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.george.fxoptiontradebooking.config.TestSecurityConfig;
import org.george.fxoptiontradebooking.dto.request.CounterpartyRequest;
import org.george.fxoptiontradebooking.dto.response.CounterpartyResponse;
import org.george.fxoptiontradebooking.exception.GlobalExceptionHandler;
import org.george.fxoptiontradebooking.service.CounterpartyService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CounterpartyController.class)
@ContextConfiguration(classes = {
        CounterpartyController.class,
        GlobalExceptionHandler.class,
        TestSecurityConfig.class, // Test-specific security configuration
        CounterpartyControllerTest.TestConfig.class
})
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
        reset(counterpartyService);
    }

    @Nested
    @DisplayName("Create Counterparty Tests")
    class CreateCounterpartyTests {

        @Test
        @DisplayName("Should create counterparty successfully")
        void shouldCreateCounterpartySuccessfully() throws Exception {
            when(counterpartyService.createCounterparty(any(CounterpartyRequest.class))).thenReturn(validResponse);

            mockMvc.perform(post("/api/v1/counterparties")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Counterparty created successfully"))
                    .andExpect(jsonPath("$.data.counterpartyCode").value(validResponse.getCounterpartyCode()));

            verify(counterpartyService).createCounterparty(any(CounterpartyRequest.class));
        }
    }

    // Other nested test cases...

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