package org.george.fxoptiontradebooking.service.impl;

import org.george.fxoptiontradebooking.dto.request.TradeBookingRequest;
import org.george.fxoptiontradebooking.entity.OptionType;
import org.george.fxoptiontradebooking.exception.BusinessValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ValidationServiceImpl Tests")
class ValidationServiceImplTest {

    @InjectMocks
    private ValidationServiceImpl validationService;

    private TradeBookingRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = createValidTradeRequest();
    }

    @Nested
    @DisplayName("Currency Validation Tests")
    class CurrencyValidationTests {

        @Test
        @DisplayName("Should validate valid major currency code")
        void shouldValidateValidMajorCurrency() {
            assertDoesNotThrow(() -> validationService.validateCurrencyCode("USD"));
            assertDoesNotThrow(() -> validationService.validateCurrencyCode("eur"));
            assertDoesNotThrow(() -> validationService.validateCurrencyCode(" GBP "));
        }

        @Test
        @DisplayName("Should throw exception for null currency code")
        void shouldThrowExceptionForNullCurrency() {
            BusinessValidationException exception = assertThrows(
                BusinessValidationException.class,
                () -> validationService.validateCurrencyCode(null)
            );
            assertEquals("Currency code cannot be null or empty", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for empty currency code")
        void shouldThrowExceptionForEmptyCurrency() {
            BusinessValidationException exception = assertThrows(
                BusinessValidationException.class,
                () -> validationService.validateCurrencyCode("")
            );
            assertEquals("Currency code cannot be null or empty", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for invalid currency length")
        void shouldThrowExceptionForInvalidCurrencyLength() {
            BusinessValidationException exception = assertThrows(
                BusinessValidationException.class,
                () -> validationService.validateCurrencyCode("US")
            );
            assertEquals("Currency code must be exactly 3 characters: US", exception.getMessage());

            exception = assertThrows(
                BusinessValidationException.class,
                () -> validationService.validateCurrencyCode("USDX")
            );
            assertEquals("Currency code must be exactly 3 characters: USDX", exception.getMessage());
        }

        @Test
        @DisplayName("Should validate same currencies as different")
        void shouldValidateDifferentCurrencies() {
            validRequest.setBaseCurrency("USD");
            validRequest.setQuoteCurrency("USD");

            BusinessValidationException exception = assertThrows(
                BusinessValidationException.class,
                () -> validationService.validateTradeRequest(validRequest)
            );
            assertEquals("Base currency and quote currency must be different", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Trade Request Validation Tests")
    class TradeRequestValidationTests {

        @Test
        @DisplayName("Should validate valid trade request")
        void shouldValidateValidTradeRequest() {
            assertDoesNotThrow(() -> validationService.validateTradeRequest(validRequest));
        }

        @Test
        @DisplayName("Should throw exception for null trade reference")
        void shouldThrowExceptionForNullTradeReference() {
            validRequest.setTradeReference(null);

            BusinessValidationException exception = assertThrows(
                BusinessValidationException.class,
                () -> validationService.validateTradeRequest(validRequest)
            );
            assertEquals("Trade reference is required", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for empty trade reference")
        void shouldThrowExceptionForEmptyTradeReference() {
            validRequest.setTradeReference("  ");

            BusinessValidationException exception = assertThrows(
                BusinessValidationException.class,
                () -> validationService.validateTradeRequest(validRequest)
            );
            assertEquals("Trade reference is required", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for too long trade reference")
        void shouldThrowExceptionForTooLongTradeReference() {
            validRequest.setTradeReference("A".repeat(51));

            BusinessValidationException exception = assertThrows(
                BusinessValidationException.class,
                () -> validationService.validateTradeRequest(validRequest)
            );
            assertEquals("Trade reference cannot exceed 50 characters", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for null counterparty ID")
        void shouldThrowExceptionForNullCounterpartyId() {
            validRequest.setCounterpartyId(null);

            BusinessValidationException exception = assertThrows(
                BusinessValidationException.class,
                () -> validationService.validateTradeRequest(validRequest)
            );
            assertEquals("Valid counterparty ID is required", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for invalid counterparty ID")
        void shouldThrowExceptionForInvalidCounterpartyId() {
            validRequest.setCounterpartyId(0L);

            BusinessValidationException exception = assertThrows(
                BusinessValidationException.class,
                () -> validationService.validateTradeRequest(validRequest)
            );
            assertEquals("Valid counterparty ID is required", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for null option type")
        void shouldThrowExceptionForNullOptionType() {
            validRequest.setOptionType(null);

            BusinessValidationException exception = assertThrows(
                BusinessValidationException.class,
                () -> validationService.validateTradeRequest(validRequest)
            );
            assertEquals("Option type is required", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Amount Validation Tests")
    class AmountValidationTests {

        @Test
        @DisplayName("Should throw exception for null notional amount")
        void shouldThrowExceptionForNullNotionalAmount() {
            validRequest.setNotionalAmount(null);

            BusinessValidationException exception = assertThrows(
                BusinessValidationException.class,
                () -> validationService.validateTradeRequest(validRequest)
            );
            assertEquals("Notional amount is required", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for too small notional amount")
        void shouldThrowExceptionForTooSmallNotionalAmount() {
            validRequest.setNotionalAmount(new BigDecimal("9999.99"));

            BusinessValidationException exception = assertThrows(
                BusinessValidationException.class,
                () -> validationService.validateTradeRequest(validRequest)
            );
            assertEquals("Notional amount must be at least 10000.00", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for too large notional amount")
        void shouldThrowExceptionForTooLargeNotionalAmount() {
            validRequest.setNotionalAmount(new BigDecimal("1000000000.01"));

            BusinessValidationException exception = assertThrows(
                BusinessValidationException.class,
                () -> validationService.validateTradeRequest(validRequest)
            );
            assertEquals("Notional amount exceeds maximum limit of 1000000000.00", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for too many decimal places in notional")
        void shouldThrowExceptionForTooManyDecimalPlacesInNotional() {
            validRequest.setNotionalAmount(new BigDecimal("100000.123"));

            BusinessValidationException exception = assertThrows(
                BusinessValidationException.class,
                () -> validationService.validateTradeRequest(validRequest)
            );
            assertEquals("Notional amount cannot have more than 2 decimal places", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for null strike price")
        void shouldThrowExceptionForNullStrikePrice() {
            validRequest.setStrikePrice(null);

            BusinessValidationException exception = assertThrows(
                BusinessValidationException.class,
                () -> validationService.validateTradeRequest(validRequest)
            );
            assertEquals("Strike price is required", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for invalid strike price")
        void shouldThrowExceptionForInvalidStrikePrice() {
            validRequest.setStrikePrice(new BigDecimal("0.000001"));

            BusinessValidationException exception = assertThrows(
                BusinessValidationException.class,
                () -> validationService.validateTradeRequest(validRequest)
            );
            assertEquals("Strike price must be greater than 0.000001", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for too many decimal places in strike price")
        void shouldThrowExceptionForTooManyDecimalPlacesInStrikePrice() {
            validRequest.setStrikePrice(new BigDecimal("1.1234567"));

            BusinessValidationException exception = assertThrows(
                BusinessValidationException.class,
                () -> validationService.validateTradeRequest(validRequest)
            );
            assertEquals("Strike price cannot have more than 6 decimal places", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Date Validation Tests")
    class DateValidationTests {

        @Test
        @DisplayName("Should validate correct date sequence")
        void shouldValidateCorrectDateSequence() {
            LocalDate today = LocalDate.now();
            validRequest.setTradeDate(today);
            validRequest.setValueDate(today.plusDays(2));
            validRequest.setMaturityDate(today.plusDays(30));

            assertDoesNotThrow(() -> validationService.validateDateSequence(validRequest));
        }

        @Test
        @DisplayName("Should throw exception for trade date too far in future")
        void shouldThrowExceptionForTradeDateTooFarInFuture() {
            validRequest.setTradeDate(LocalDate.now().plusDays(2));

            BusinessValidationException exception = assertThrows(
                BusinessValidationException.class,
                () -> validationService.validateDateSequence(validRequest)
            );
            assertEquals("Trade date cannot be more than 1 day in the future", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for invalid value date")
        void shouldThrowExceptionForInvalidValueDate() {
            LocalDate today = LocalDate.now();
            validRequest.setTradeDate(today);
            validRequest.setValueDate(today);

            BusinessValidationException exception = assertThrows(
                BusinessValidationException.class,
                () -> validationService.validateDateSequence(validRequest)
            );
            assertEquals("Value date must be at least 1 day after trade date", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for invalid maturity date")
        void shouldThrowExceptionForInvalidMaturityDate() {
            LocalDate today = LocalDate.now();
            validRequest.setTradeDate(today);
            validRequest.setValueDate(today.plusDays(2));
            validRequest.setMaturityDate(today.plusDays(2));

            BusinessValidationException exception = assertThrows(
                BusinessValidationException.class,
                () -> validationService.validateDateSequence(validRequest)
            );
            assertEquals("Maturity date must be after value date", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for weekend value date")
        void shouldThrowExceptionForWeekendValueDate() {
            LocalDate today = LocalDate.now();
            LocalDate saturday = today.with(java.time.DayOfWeek.SATURDAY);
            validRequest.setTradeDate(today);
            validRequest.setValueDate(saturday);
            validRequest.setMaturityDate(saturday.plusDays(30));

            BusinessValidationException exception = assertThrows(
                BusinessValidationException.class,
                () -> validationService.validateDateSequence(validRequest)
            );
            assertTrue(exception.getMessage().contains("Value date cannot be a weekend"));
        }

        @Test
        @DisplayName("Should throw exception for same day maturity")
        void shouldThrowExceptionForSameDayMaturity() {
            validRequest.setMaturityDate(validRequest.getTradeDate());

            BusinessValidationException exception = assertThrows(
                BusinessValidationException.class,
                () -> validationService.validateTradeRequest(validRequest)
            );
            assertEquals("Same-day options are not supported", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Premium Validation Tests")
    class PremiumValidationTests {

        @Test
        @DisplayName("Should throw exception when premium amount provided without currency")
        void shouldThrowExceptionWhenPremiumAmountWithoutCurrency() {
            validRequest.setPremiumAmount(new BigDecimal("1000"));
            validRequest.setPremiumCurrency(null);

            BusinessValidationException exception = assertThrows(
                BusinessValidationException.class,
                () -> validationService.validateTradeRequest(validRequest)
            );
            assertEquals("Premium currency is required when premium amount is specified", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when premium currency provided without amount")
        void shouldThrowExceptionWhenPremiumCurrencyWithoutAmount() {
            validRequest.setPremiumAmount(null);
            validRequest.setPremiumCurrency("USD");

            BusinessValidationException exception = assertThrows(
                BusinessValidationException.class,
                () -> validationService.validateTradeRequest(validRequest)
            );
            assertEquals("Premium amount is required when premium currency is specified", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for negative premium amount")
        void shouldThrowExceptionForNegativePremiumAmount() {
            validRequest.setPremiumAmount(new BigDecimal("-100"));
            validRequest.setPremiumCurrency("USD");

            BusinessValidationException exception = assertThrows(
                BusinessValidationException.class,
                () -> validationService.validateTradeRequest(validRequest)
            );
            assertEquals("Premium amount must be positive", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for too many decimal places in premium")
        void shouldThrowExceptionForTooManyDecimalPlacesInPremium() {
            validRequest.setPremiumAmount(new BigDecimal("100.123"));
            validRequest.setPremiumCurrency("USD");

            BusinessValidationException exception = assertThrows(
                BusinessValidationException.class,
                () -> validationService.validateTradeRequest(validRequest)
            );
            assertEquals("Premium amount cannot have more than 2 decimal places", exception.getMessage());
        }
    }

    private TradeBookingRequest createValidTradeRequest() {
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
}
