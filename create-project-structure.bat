
@echo off
echo Creating test directory structure and files for FX Option Trade Booking...

REM Create test directory structure
mkdir "src\test\java\org\george\fxoptiontradebooking\service\impl" 2>nul
mkdir "src\test\java\org\george\fxoptiontradebooking\controller" 2>nul

echo Creating ValidationServiceImplTest.java...
(
echo package org.george.fxoptiontradebooking.service.impl;
echo.
echo import org.george.fxoptiontradebooking.dto.request.TradeBookingRequest;
echo import org.george.fxoptiontradebooking.entity.OptionType;
echo import org.george.fxoptiontradebooking.exception.BusinessValidationException;
echo import org.junit.jupiter.api.BeforeEach;
echo import org.junit.jupiter.api.DisplayName;
echo import org.junit.jupiter.api.Nested;
echo import org.junit.jupiter.api.Test;
echo import org.junit.jupiter.api.extension.ExtendWith;
echo import org.mockito.InjectMocks;
echo import org.mockito.junit.jupiter.MockitoExtension;
echo.
echo import java.math.BigDecimal;
echo import java.time.LocalDate;
echo.
echo import static org.junit.jupiter.api.Assertions.*;
echo.
echo @ExtendWith^(MockitoExtension.class^)
echo @DisplayName^("ValidationServiceImpl Tests"^)
echo class ValidationServiceImplTest {
echo.
echo     @InjectMocks
echo     private ValidationServiceImpl validationService;
echo.
echo     private TradeBookingRequest validRequest;
echo.
echo     @BeforeEach
echo     void setUp^(^) {
echo         validRequest = createValidTradeRequest^(^);
echo     }
echo.
echo     @Nested
echo     @DisplayName^("Currency Validation Tests"^)
echo     class CurrencyValidationTests {
echo.
echo         @Test
echo         @DisplayName^("Should validate valid major currency code"^)
echo         void shouldValidateValidMajorCurrency^(^) {
echo             assertDoesNotThrow^(^(^) -^> validationService.validateCurrencyCode^("USD"^)^);
echo             assertDoesNotThrow^(^(^) -^> validationService.validateCurrencyCode^("eur"^)^);
echo             assertDoesNotThrow^(^(^) -^> validationService.validateCurrencyCode^(" GBP "^)^);
echo         }
echo.
echo         @Test
echo         @DisplayName^("Should throw exception for null currency code"^)
echo         void shouldThrowExceptionForNullCurrency^(^) {
echo             BusinessValidationException exception = assertThrows^(
echo                 BusinessValidationException.class,
echo                 ^(^) -^> validationService.validateCurrencyCode^(null^)
echo             ^);
echo             assertEquals^("Currency code cannot be null or empty", exception.getMessage^(^)^);
echo         }
echo.
echo         @Test
echo         @DisplayName^("Should throw exception for empty currency code"^)
echo         void shouldThrowExceptionForEmptyCurrency^(^) {
echo             BusinessValidationException exception = assertThrows^(
echo                 BusinessValidationException.class,
echo                 ^(^) -^> validationService.validateCurrencyCode^(""^)
echo             ^);
echo             assertEquals^("Currency code cannot be null or empty", exception.getMessage^(^)^);
echo         }
echo.
echo         @Test
echo         @DisplayName^("Should throw exception for invalid currency length"^)
echo         void shouldThrowExceptionForInvalidCurrencyLength^(^) {
echo             BusinessValidationException exception = assertThrows^(
echo                 BusinessValidationException.class,
echo                 ^(^) -^> validationService.validateCurrencyCode^("US"^)
echo             ^);
echo             assertEquals^("Currency code must be exactly 3 characters: US", exception.getMessage^(^)^);
echo.
echo             exception = assertThrows^(
echo                 BusinessValidationException.class,
echo                 ^(^) -^> validationService.validateCurrencyCode^("USDX"^)
echo             ^);
echo             assertEquals^("Currency code must be exactly 3 characters: USDX", exception.getMessage^(^)^);
echo         }
echo.
echo         @Test
echo         @DisplayName^("Should validate same currencies as different"^)
echo         void shouldValidateDifferentCurrencies^(^) {
echo             validRequest.setBaseCurrency^("USD"^);
echo             validRequest.setQuoteCurrency^("USD"^);
echo.
echo             BusinessValidationException exception = assertThrows^(
echo                 BusinessValidationException.class,
echo                 ^(^) -^> validationService.validateTradeRequest^(validRequest^)
echo             ^);
echo             assertEquals^("Base currency and quote currency must be different", exception.getMessage^(^)^);
echo         }
echo     }
echo.
echo     @Nested
echo     @DisplayName^("Trade Request Validation Tests"^)
echo     class TradeRequestValidationTests {
echo.
echo         @Test
echo         @DisplayName^("Should validate valid trade request"^)
echo         void shouldValidateValidTradeRequest^(^) {
echo             assertDoesNotThrow^(^(^) -^> validationService.validateTradeRequest^(validRequest^)^);
echo         }
echo.
echo         @Test
echo         @DisplayName^("Should throw exception for null trade reference"^)
echo         void shouldThrowExceptionForNullTradeReference^(^) {
echo             validRequest.setTradeReference^(null^);
echo.
echo             BusinessValidationException exception = assertThrows^(
echo                 BusinessValidationException.class,
echo                 ^(^) -^> validationService.validateTradeRequest^(validRequest^)
echo             ^);
echo             assertEquals^("Trade reference is required", exception.getMessage^(^)^);
echo         }
echo.
echo         @Test
echo         @DisplayName^("Should throw exception for empty trade reference"^)
echo         void shouldThrowExceptionForEmptyTradeReference^(^) {
echo             validRequest.setTradeReference^("  "^);
echo.
echo             BusinessValidationException exception = assertThrows^(
echo                 BusinessValidationException.class,
echo                 ^(^) -^> validationService.validateTradeRequest^(validRequest^)
echo             ^);
echo             assertEquals^("Trade reference is required", exception.getMessage^(^)^);
echo         }
echo.
echo         @Test
echo         @DisplayName^("Should throw exception for too long trade reference"^)
echo         void shouldThrowExceptionForTooLongTradeReference^(^) {
echo             validRequest.setTradeReference^("A".repeat^(51^)^);
echo.
echo             BusinessValidationException exception = assertThrows^(
echo                 BusinessValidationException.class,
echo                 ^(^) -^> validationService.validateTradeRequest^(validRequest^)
echo             ^);
echo             assertEquals^("Trade reference cannot exceed 50 characters", exception.getMessage^(^)^);
echo         }
echo.
echo         @Test
echo         @DisplayName^("Should throw exception for null counterparty ID"^)
echo         void shouldThrowExceptionForNullCounterpartyId^(^) {
echo             validRequest.setCounterpartyId^(null^);
echo.
echo             BusinessValidationException exception = assertThrows^(
echo                 BusinessValidationException.class,
echo                 ^(^) -^> validationService.validateTradeRequest^(validRequest^)
echo             ^);
echo             assertEquals^("Valid counterparty ID is required", exception.getMessage^(^)^);
echo         }
echo.
echo         @Test
echo         @DisplayName^("Should throw exception for invalid counterparty ID"^)
echo         void shouldThrowExceptionForInvalidCounterpartyId^(^) {
echo             validRequest.setCounterpartyId^(0L^);
echo.
echo             BusinessValidationException exception = assertThrows^(
echo                 BusinessValidationException.class,
echo                 ^(^) -^> validationService.validateTradeRequest^(validRequest^)
echo             ^);
echo             assertEquals^("Valid counterparty ID is required", exception.getMessage^(^)^);
echo         }
echo.
echo         @Test
echo         @DisplayName^("Should throw exception for null option type"^)
echo         void shouldThrowExceptionForNullOptionType^(^) {
echo             validRequest.setOptionType^(null^);
echo.
echo             BusinessValidationException exception = assertThrows^(
echo                 BusinessValidationException.class,
echo                 ^(^) -^> validationService.validateTradeRequest^(validRequest^)
echo             ^);
echo             assertEquals^("Option type is required", exception.getMessage^(^)^);
echo         }
echo     }
echo.
echo     @Nested
echo     @DisplayName^("Amount Validation Tests"^)
echo     class AmountValidationTests {
echo.
echo         @Test
echo         @DisplayName^("Should throw exception for null notional amount"^)
echo         void shouldThrowExceptionForNullNotionalAmount^(^) {
echo             validRequest.setNotionalAmount^(null^);
echo.
echo             BusinessValidationException exception = assertThrows^(
echo                 BusinessValidationException.class,
echo                 ^(^) -^> validationService.validateTradeRequest^(validRequest^)
echo             ^);
echo             assertEquals^("Notional amount is required", exception.getMessage^(^)^);
echo         }
echo.
echo         @Test
echo         @DisplayName^("Should throw exception for too small notional amount"^)
echo         void shouldThrowExceptionForTooSmallNotionalAmount^(^) {
echo             validRequest.setNotionalAmount^(new BigDecimal^("9999.99"^)^);
echo.
echo             BusinessValidationException exception = assertThrows^(
echo                 BusinessValidationException.class,
echo                 ^(^) -^> validationService.validateTradeRequest^(validRequest^)
echo             ^);
echo             assertEquals^("Notional amount must be at least 10000.00", exception.getMessage^(^)^);
echo         }
echo.
echo         @Test
echo         @DisplayName^("Should throw exception for too large notional amount"^)
echo         void shouldThrowExceptionForTooLargeNotionalAmount^(^) {
echo             validRequest.setNotionalAmount^(new BigDecimal^("1000000000.01"^)^);
echo.
echo             BusinessValidationException exception = assertThrows^(
echo                 BusinessValidationException.class,
echo                 ^(^) -^> validationService.validateTradeRequest^(validRequest^)
echo             ^);
echo             assertEquals^("Notional amount exceeds maximum limit of 1000000000.00", exception.getMessage^(^)^);
echo         }
echo.
echo         @Test
echo         @DisplayName^("Should throw exception for too many decimal places in notional"^)
echo         void shouldThrowExceptionForTooManyDecimalPlacesInNotional^(^) {
echo             validRequest.setNotionalAmount^(new BigDecimal^("100000.123"^)^);
echo.
echo             BusinessValidationException exception = assertThrows^(
echo                 BusinessValidationException.class,
echo                 ^(^) -^> validationService.validateTradeRequest^(validRequest^)
echo             ^);
echo             assertEquals^("Notional amount cannot have more than 2 decimal places", exception.getMessage^(^)^);
echo         }
echo.
echo         @Test
echo         @DisplayName^("Should throw exception for null strike price"^)
echo         void shouldThrowExceptionForNullStrikePrice^(^) {
echo             validRequest.setStrikePrice^(null^);
echo.
echo             BusinessValidationException exception = assertThrows^(
echo                 BusinessValidationException.class,
echo                 ^(^) -^> validationService.validateTradeRequest^(validRequest^)
echo             ^);
echo             assertEquals^("Strike price is required", exception.getMessage^(^)^);
echo         }
echo.
echo         @Test
echo         @DisplayName^("Should throw exception for invalid strike price"^)
echo         void shouldThrowExceptionForInvalidStrikePrice^(^) {
echo             validRequest.setStrikePrice^(new BigDecimal^("0.000001"^)^);
echo.
echo             BusinessValidationException exception = assertThrows^(
echo                 BusinessValidationException.class,
echo                 ^(^) -^> validationService.validateTradeRequest^(validRequest^)
echo             ^);
echo             assertEquals^("Strike price must be greater than 0.000001", exception.getMessage^(^)^);
echo         }
echo.
echo         @Test
echo         @DisplayName^("Should throw exception for too many decimal places in strike price"^)
echo         void shouldThrowExceptionForTooManyDecimalPlacesInStrikePrice^(^) {
echo             validRequest.setStrikePrice^(new BigDecimal^("1.1234567"^)^);
echo.
echo             BusinessValidationException exception = assertThrows^(
echo                 BusinessValidationException.class,
echo                 ^(^) -^> validationService.validateTradeRequest^(validRequest^)
echo             ^);
echo             assertEquals^("Strike price cannot have more than 6 decimal places", exception.getMessage^(^)^);
echo         }
echo     }
echo.
echo     @Nested
echo     @DisplayName^("Date Validation Tests"^)
echo     class DateValidationTests {
echo.
echo         @Test
echo         @DisplayName^("Should validate correct date sequence"^)
echo         void shouldValidateCorrectDateSequence^(^) {
echo             LocalDate today = LocalDate.now^(^);
echo             validRequest.setTradeDate^(today^);
echo             validRequest.setValueDate^(today.plusDays^(2^)^);
echo             validRequest.setMaturityDate^(today.plusDays^(30^)^);
echo.
echo             assertDoesNotThrow^(^(^) -^> validationService.validateDateSequence^(validRequest^)^);
echo         }
echo.
echo         @Test
echo         @DisplayName^("Should throw exception for trade date too far in future"^)
echo         void shouldThrowExceptionForTradeDateTooFarInFuture^(^) {
echo             validRequest.setTradeDate^(LocalDate.now^(^).plusDays^(2^)^);
echo.
echo             BusinessValidationException exception = assertThrows^(
echo                 BusinessValidationException.class,
echo                 ^(^) -^> validationService.validateDateSequence^(validRequest^)
echo             ^);
echo             assertEquals^("Trade date cannot be more than 1 day in the future", exception.getMessage^(^)^);
echo         }
echo.
echo         @Test
echo         @DisplayName^("Should throw exception for invalid value date"^)
echo         void shouldThrowExceptionForInvalidValueDate^(^) {
echo             LocalDate today = LocalDate.now^(^);
echo             validRequest.setTradeDate^(today^);
echo             validRequest.setValueDate^(today^);
echo.
echo             BusinessValidationException exception = assertThrows^(
echo                 BusinessValidationException.class,
echo                 ^(^) -^> validationService.validateDateSequence^(validRequest^)
echo             ^);
echo             assertEquals^("Value date must be at least 1 day after trade date", exception.getMessage^(^)^);
echo         }
echo.
echo         @Test
echo         @DisplayName^("Should throw exception for invalid maturity date"^)
echo         void shouldThrowExceptionForInvalidMaturityDate^(^) {
echo             LocalDate today = LocalDate.now^(^);
echo             validRequest.setTradeDate^(today^);
echo             validRequest.setValueDate^(today.plusDays^(2^)^);
echo             validRequest.setMaturityDate^(today.plusDays^(2^)^);
echo.
echo             BusinessValidationException exception = assertThrows^(
echo                 BusinessValidationException.class,
echo                 ^(^) -^> validationService.validateDateSequence^(validRequest^)
echo             ^);
echo             assertEquals^("Maturity date must be after value date", exception.getMessage^(^)^);
echo         }
echo.
echo         @Test
echo         @DisplayName^("Should throw exception for weekend value date"^)
echo         void shouldThrowExceptionForWeekendValueDate^(^) {
echo             LocalDate today = LocalDate.now^(^);
echo             LocalDate saturday = today.with^(java.time.DayOfWeek.SATURDAY^);
echo             
echo             validRequest.setTradeDate^(today^);
echo             validRequest.setValueDate^(saturday^);
echo             validRequest.setMaturityDate^(saturday.plusDays^(30^)^);
echo.
echo             BusinessValidationException exception = assertThrows^(
echo                 BusinessValidationException.class,
echo                 ^(^) -^> validationService.validateDateSequence^(validRequest^)
echo             ^);
echo             assertTrue^(exception.getMessage^(^).contains^("Value date cannot be a weekend"^)^);
echo         }
echo.
echo         @Test
echo         @DisplayName^("Should throw exception for same day maturity"^)
echo         void shouldThrowExceptionForSameDayMaturity^(^) {
echo             validRequest.setMaturityDate^(validRequest.getTradeDate^(^)^);
echo.
echo             BusinessValidationException exception = assertThrows^(
echo                 BusinessValidationException.class,
echo                 ^(^) -^> validationService.validateTradeRequest^(validRequest^)
echo             ^);
echo             assertEquals^("Same-day options are not supported", exception.getMessage^(^)^);
echo         }
echo     }
echo.
echo     @Nested
echo     @DisplayName^("Premium Validation Tests"^)
echo     class PremiumValidationTests {
echo.
echo         @Test
echo         @DisplayName^("Should throw exception when premium amount provided without currency"^)
echo         void shouldThrowExceptionWhenPremiumAmountWithoutCurrency^(^) {
echo             validRequest.setPremiumAmount^(new BigDecimal^("1000"^)^);
echo             validRequest.setPremiumCurrency^(null^);
echo.
echo             BusinessValidationException exception = assertThrows^(
echo                 BusinessValidationException.class,
echo                 ^(^) -^> validationService.validateTradeRequest^(validRequest^)
echo             ^);
echo             assertEquals^("Premium currency is required when premium amount is specified", exception.getMessage^(^)^);
echo         }
echo.
echo         @Test
echo         @DisplayName^("Should throw exception when premium currency provided without amount"^)
echo         void shouldThrowExceptionWhenPremiumCurrencyWithoutAmount^(^) {
echo             validRequest.setPremiumAmount^(null^);
echo             validRequest.setPremiumCurrency^("USD"^);
echo.
echo             BusinessValidationException exception = assertThrows^(
echo                 BusinessValidationException.class,
echo                 ^(^) -^> validationService.validateTradeRequest^(validRequest^)
echo             ^);
echo             assertEquals^("Premium amount is required when premium currency is specified", exception.getMessage^(^)^);
echo         }
echo.
echo         @Test
echo         @DisplayName^("Should throw exception for negative premium amount"^)
echo         void shouldThrowExceptionForNegativePremiumAmount^(^) {
echo             validRequest.setPremiumAmount^(new BigDecimal^("-100"^)^);
echo             validRequest.setPremiumCurrency^("USD"^);
echo.
echo             BusinessValidationException exception = assertThrows^(
echo                 BusinessValidationException.class,
echo                 ^(^) -^> validationService.validateTradeRequest^(validRequest^)
echo             ^);
echo             assertEquals^("Premium amount must be positive", exception.getMessage^(^)^);
echo         }
echo.
echo         @Test
echo         @DisplayName^("Should throw exception for too many decimal places in premium"^)
echo         void shouldThrowExceptionForTooManyDecimalPlacesInPremium^(^) {
echo             validRequest.setPremiumAmount^(new BigDecimal^("100.123"^)^);
echo             validRequest.setPremiumCurrency^("USD"^);
echo.
echo             BusinessValidationException exception = assertThrows^(
echo                 BusinessValidationException.class,
echo                 ^(^) -^> validationService.validateTradeRequest^(validRequest^)
echo             ^);
echo             assertEquals^("Premium amount cannot have more than 2 decimal places", exception.getMessage^(^)^);
echo         }
echo     }
echo.
echo     private TradeBookingRequest createValidTradeRequest^(^) {
echo         TradeBookingRequest request = new TradeBookingRequest^(^);
echo         request.setTradeReference^("TRD-001"^);
echo         request.setCounterpartyId^(1L^);
echo         request.setBaseCurrency^("EUR"^);
echo         request.setQuoteCurrency^("USD"^);
echo         request.setNotionalAmount^(new BigDecimal^("100000.00"^)^);
echo         request.setStrikePrice^(new BigDecimal^("1.2500"^)^);
echo         request.setSpotRate^(new BigDecimal^("1.2000"^)^);
echo         request.setTradeDate^(LocalDate.now^(^)^);
echo         request.setValueDate^(LocalDate.now^(^).plusDays^(2^)^);
echo         request.setMaturityDate^(LocalDate.now^(^).plusDays^(30^)^);
echo         request.setOptionType^(OptionType.CALL^);
echo         request.setCreatedBy^("TEST_USER"^);
echo         return request;
echo     }
echo }
) > "src\test\java\org\george\fxoptiontradebooking\service\impl\ValidationServiceImplTest.java"

echo Creating CounterpartyServiceImplTest.java...
(
echo package org.george.fxoptiontradebooking.service.impl;
echo.
echo import org.george.fxoptiontradebooking.dto.request.CounterpartyRequest;
echo import org.george.fxoptiontradebooking.dto.response.CounterpartyResponse;
echo import org.george.fxoptiontradebooking.entity.Counterparty;
echo import org.george.fxoptiontradebooking.exception.BusinessValidationException;
echo import org.george.fxoptiontradebooking.exception.TradeNotFoundException;
echo import org.george.fxoptiontradebooking.repository.CounterpartyRepository;
echo import org.junit.jupiter.api.BeforeEach;
echo import org.junit.jupiter.api.DisplayName;
echo import org.junit.jupiter.api.Nested;
echo import org.junit.jupiter.api.Test;
echo import org.junit.jupiter.api.extension.ExtendWith;
echo import org.mockito.InjectMocks;
echo import org.mockito.Mock;
echo import org.mockito.junit.jupiter.MockitoExtension;
echo import org.modelmapper.ModelMapper;
echo import org.springframework.dao.DataIntegrityViolationException;
echo.
echo import java.util.Arrays;
echo import java.util.List;
echo import java.util.Optional;
echo.
echo import static org.junit.jupiter.api.Assertions.*;
echo import static org.mockito.ArgumentMatchers.*;
echo import static org.mockito.Mockito.*;
echo.
echo @ExtendWith^(MockitoExtension.class^)
echo @DisplayName^("CounterpartyServiceImpl Tests"^)
echo class CounterpartyServiceImplTest {
echo.
echo     @Mock
echo     private CounterpartyRepository counterpartyRepository;
echo.
echo     @Mock
echo     private ModelMapper modelMapper;
echo.
echo     @InjectMocks
echo     private CounterpartyServiceImpl counterpartyService;
echo.
echo     private CounterpartyRequest validRequest;
echo     private Counterparty validCounterparty;
echo     private CounterpartyResponse validResponse;
echo.
echo     @BeforeEach
echo     void setUp^(^) {
echo         validRequest = createValidCounterpartyRequest^(^);
echo         validCounterparty = createValidCounterparty^(^);
echo         validResponse = createValidCounterpartyResponse^(^);
echo     }
echo.
echo     @Nested
echo     @DisplayName^("Create Counterparty Tests"^)
echo     class CreateCounterpartyTests {
echo.
echo         @Test
echo         @DisplayName^("Should create counterparty successfully"^)
echo         void shouldCreateCounterpartySuccessfully^(^) {
echo             // Given
echo             when^(counterpartyRepository.findByCounterpartyCode^(anyString^(^)^)^).thenReturn^(Optional.empty^(^)^);
echo             when^(counterpartyRepository.findByLeiCode^(anyString^(^)^)^).thenReturn^(Optional.empty^(^)^);
echo             when^(counterpartyRepository.save^(any^(Counterparty.class^)^)^).thenReturn^(validCounterparty^);
echo             when^(modelMapper.map^(validCounterparty, CounterpartyResponse.class^)^).thenReturn^(validResponse^);
echo.
echo             // When
echo             CounterpartyResponse result = counterpartyService.createCounterparty^(validRequest^);
echo.
echo             // Then
echo             assertNotNull^(result^);
echo             assertEquals^(validResponse.getCounterpartyCode^(^), result.getCounterpartyCode^(^)^);
echo             verify^(counterpartyRepository^).save^(any^(Counterparty.class^)^);
echo             verify^(modelMapper^).map^(validCounterparty, CounterpartyResponse.class^);
echo         }
echo.
echo         @Test
echo         @DisplayName^("Should throw exception for duplicate counterparty code"^)
echo         void shouldThrowExceptionForDuplicateCounterpartyCode^(^) {
echo             // Given
echo             when^(counterpartyRepository.findByCounterpartyCode^(anyString^(^)^)^).thenReturn^(Optional.of^(validCounterparty^)^);
echo.
echo             // When ^& Then
echo             BusinessValidationException exception = assertThrows^(
echo                 BusinessValidationException.class,
echo                 ^(^) -^> counterpartyService.createCounterparty^(validRequest^)
echo             ^);
echo             assertEquals^("Counterparty code already exists: " + validRequest.getCounterpartyCode^(^), exception.getMessage^(^)^);
echo             verify^(counterpartyRepository, never^(^)^).save^(any^(^)^);
echo         }
echo.
echo         @Test
echo         @DisplayName^("Should throw exception for duplicate LEI code"^)
echo         void shouldThrowExceptionForDuplicateLeiCode^(^) {
echo             // Given
echo             when^(counterpartyRepository.findByCounterpartyCode^(anyString^(^)^)^).thenReturn^(Optional.empty^(^)^);
echo             when^(counterpartyRepository.findByLeiCode^(anyString^(^)^)^).thenReturn^(Optional.of^(validCounterparty^)^);
echo.
echo             // When ^& Then
echo             BusinessValidationException exception = assertThrows^(
echo                 BusinessValidationException.class,
echo                 ^(^) -^> counterpartyService.createCounterparty^(validRequest^)
echo             ^);
echo             assertEquals^("LEI code already exists: " + validRequest.getLeiCode^(^), exception.getMessage^(^)^);
echo             verify^(counterpartyRepository, never^(^)^).save^(any^(^)^);
echo         }
echo.
echo         @Test
echo         @DisplayName^("Should throw exception for null request"^)
echo         void shouldThrowExceptionForNullRequest^(^) {
echo             // When ^& Then
echo             BusinessValidationException exception = assertThrows^(
echo                 BusinessValidationException.class,
echo                 ^(^) -^> counterpartyService.createCounterparty^(null^)
echo             ^);
echo             assertEquals^("Counterparty request cannot be null", exception.getMessage^(^)^);
echo         }
echo.
echo         @Test
echo         @DisplayName^("Should throw exception for invalid counterparty code format"^)
echo         void shouldThrowExceptionForInvalidCounterpartyCodeFormat^(^) {
echo             // Given
echo             validRequest.setCounterpartyCode^("XX"^);
echo.
echo             // When ^& Then
echo             BusinessValidationException exception = assertThrows^(
echo                 BusinessValidationException.class,
echo                 ^(^) -^> counterpartyService.createCounterparty^(validRequest^)
echo             ^);
echo             assertTrue^(exception.getMessage^(^).contains^("Counterparty code must be 3-10 alphanumeric characters"^)^);
echo         }
echo.
echo         @Test
echo         @DisplayName^("Should throw exception for invalid LEI code format"^)
echo         void shouldThrowExceptionForInvalidLeiCodeFormat^(^) {
echo             // Given
echo             validRequest.setLeiCode^("INVALID_LEI"^);
echo.
echo             // When ^& Then
echo             BusinessValidationException exception = assertThrows^(
echo                 BusinessValidationException.class,
echo                 ^(^) -^> counterpartyService.createCounterparty^(validRequest^)
echo             ^);
echo             assertTrue^(exception.getMessage^(^).contains^("Invalid LEI code format"^)^);
echo         }
echo.
echo         @Test
echo         @DisplayName^("Should throw exception for invalid SWIFT code format"^)
echo         void shouldThrowExceptionForInvalidSwiftCodeFormat^(^) {
echo             // Given
echo             validRequest.setSwiftCode^("INVALID"^);
echo.
echo             // When ^& Then
echo             BusinessValidationException exception = assertThrows^(
echo                 BusinessValidationException.class,
echo                 ^(^) -^> counterpartyService.createCounterparty^(validRequest^)
echo             ^);
echo             assertTrue^(exception.getMessage^(^).contains^("Invalid SWIFT code format"^)^);
echo         }
echo.
echo         @Test
echo         @DisplayName^("Should handle data integrity violation"^)
echo         void shouldHandleDataIntegrityViolation^(^) {
echo             // Given
echo             when^(counterpartyRepository.findByCounterpartyCode^(anyString^(^)^)^).thenReturn^(Optional.empty^(^)^);
echo             when^(counterpartyRepository.findByLeiCode^(anyString^(^)^)^).thenReturn^(Optional.empty^(^)^);
echo             when^(counterpartyRepository.save^(any^(Counterparty.class^)^)^).thenThrow^(new DataIntegrityViolationException^("Constraint violation"^)^);
echo.
echo             // When ^& Then
echo             BusinessValidationException exception = assertThrows^(
echo                 BusinessValidationException.class,
echo                 ^(^) -^> counterpartyService.createCounterparty^(validRequest^)
echo             ^);
echo             assertEquals^("Failed to create counterparty due to data constraint violation", exception.getMessage^(^)^);
echo         }
echo     }
echo.
echo     @Nested
echo     @DisplayName^("Get Counterparty Tests"^)
echo     class GetCounterpartyTests {
echo.
echo         @Test
echo         @DisplayName^("Should get counterparty by ID successfully"^)
echo         void shouldGetCounterpartyByIdSuccessfully^(^) {
echo             // Given
echo             Long counterpartyId = 1L;
echo             when^(counterpartyRepository.findById^(counterpartyId^)^).thenReturn^(Optional.of^(validCounterparty^)^);
echo             when^(modelMapper.map^(validCounterparty, CounterpartyResponse.class^)^).thenReturn^(validResponse^);
echo.
echo             // When
echo             CounterpartyResponse result = counterpartyService.getCounterpartyById^(counterpartyId^);
echo.
echo             // Then
echo             assertNotNull^(result^);
echo             assertEquals^(validResponse.getCounterpartyCode^(^), result.getCounterpartyCode^(^)^);
echo             verify^(counterpartyRepository^).findById^(counterpartyId^);
echo         }
echo.
echo         @Test
echo         @DisplayName^("Should throw exception when counterparty not found by ID"^)
echo         void shouldThrowExceptionWhenCounterpartyNotFoundById^(^) {
echo             // Given
echo             Long counterpartyId = 999L;
echo             when^(counterpartyRepository.findById^(counterpartyId^)^).thenReturn^(Optional.empty^(^)^);
echo.
echo             // When ^& Then
echo             TradeNotFoundException exception = assertThrows^(
echo                 TradeNotFoundException.class,
echo                 ^(^) -^> counterpartyService.getCounterpartyById^(counterpartyId^)
echo             ^);
echo             assertEquals^("Counterparty not found with ID: " + counterpartyId, exception.getMessage^(^)^);
echo         }
echo.
echo         @Test
echo         @DisplayName^("Should get counterparty by code successfully"^)
echo         void shouldGetCounterpartyByCodeSuccessfully^(^) {
echo             // Given
echo             String counterpartyCode = "CP001";
echo             when^(counterpartyRepository.findByCounterpartyCode^(counterpartyCode.toUpperCase^(^)^)^).thenReturn^(Optional.of^(validCounterparty^)^);
echo             when^(modelMapper.map^(validCounterparty, CounterpartyResponse.class^)^).thenReturn^(validResponse^);
echo.
echo             // When
echo             CounterpartyResponse result = counterpartyService.getCounterpartyByCode^(counterpartyCode^);
echo.
echo             // Then
echo             assertNotNull^(result^);
echo             assertEquals^(validResponse.getCounterpartyCode^(^), result.getCounterpartyCode^(^)^);
echo             verify^(counterpartyRepository^).findByCounterpartyCode^(counterpartyCode.toUpperCase^(^)^);
echo         }
echo.
echo         @Test
echo         @DisplayName^("Should throw exception for null counterparty code"^)
echo         void shouldThrowExceptionForNullCounterpartyCode^(^) {
echo             // When ^& Then
echo             BusinessValidationException exception = assertThrows^(
echo                 BusinessValidationException.class,
echo                 ^(^) -^> counterpartyService.getCounterpartyByCode^(null^)
echo             ^);
echo             assertEquals^("Counterparty code cannot be null or empty", exception.getMessage^(^)^);
echo         }
echo.
echo         @Test
echo         @DisplayName^("Should get all active counterparties"^)
echo         void shouldGetAllActiveCounterparties^(^) {
echo             // Given
echo             List^<Counterparty^> activeCounterparties = Arrays.asList^(validCounterparty^);
echo             when^(counterpartyRepository.findByIsActiveTrue^(^)^).thenReturn^(activeCounterparties^);
echo             when^(modelMapper.map^(validCounterparty, CounterpartyResponse.class^)^).thenReturn^(validResponse^);
echo.
echo             // When
echo             List^<CounterpartyResponse^> result = counterpartyService.getAllActiveCounterparties^(^);
echo.
echo             // Then
echo             assertNotNull^(result^);
echo             assertEquals^(1, result.size^(^)^);
echo             verify^(counterpartyRepository^).findByIsActiveTrue^(^);
echo         }
echo.
echo         @Test
echo         @DisplayName^("Should get all counterparties"^)
echo         void shouldGetAllCounterparties^(^) {
echo             // Given
echo             List^<Counterparty^> allCounterparties = Arrays.asList^(validCounterparty^);
echo             when^(counterpartyRepository.findAll^(^)^).thenReturn^(allCounterparties^);
echo             when^(modelMapper.map^(validCounterparty, CounterpartyResponse.class^)^).thenReturn^(validResponse^);
echo.
echo             // When
echo             List^<CounterpartyResponse^> result = counterpartyService.getAllCounterparties^(^);
echo.
echo             // Then
echo             assertNotNull^(result^);
echo             assertEquals^(1, result.size^(^)^);
echo             verify^(counterpartyRepository^).findAll^(^);
echo         }
echo     }
echo.
echo     private CounterpartyRequest createValidCounterpartyRequest^(^) {
echo         CounterpartyRequest request = new CounterpartyRequest^(^);
echo         request.setCounterpartyCode^("CP001"^);
echo         request.setName^("Test Counterparty"^);
echo         request.setLeiCode^("12345678901234567890"^);
echo         request.setSwiftCode^("TESTUS33"^);
echo         request.setCreditRating^("AA"^);
echo         request.setIsActive^(true^);
echo         return request;
echo     }
echo.
echo     private Counterparty createValidCounterparty^(^) {
echo         Counterparty counterparty = new Counterparty^(^);
echo         counterparty.setCounterpartyId^(1L^);
echo         counterparty.setCounterpartyCode^("CP001"^);
echo         counterparty.setName^("Test Counterparty"^);
echo         counterparty.setLeiCode^("12345678901234567890"^);
echo         counterparty.setSwiftCode^("TESTUS33"^);
echo         counterparty.setCreditRating^("AA"^);
echo         counterparty.setIsActive^(true^);
echo         return counterparty;
echo     }
echo.
echo     private CounterpartyResponse createValidCounterpartyResponse^(^) {
echo         CounterpartyResponse response = new CounterpartyResponse^(^);
echo         response.setCounterpartyId^(1L^);
echo         response.setCounterpartyCode^("CP001"^);
echo         response.setName^("Test Counterparty"^);
echo         response.setLeiCode^("12345678901234567890"^);
echo         response.setSwiftCode^("TESTUS33"^);
echo         response.setCreditRating^("AA"^);
echo         response.setIsActive^(true^);
echo         return response;
echo     }
echo }
) > "src\test\java\org\george\fxoptiontradebooking\service\impl\CounterpartyServiceImplTest.java"

echo Creating TradeServiceImplTest.java...
(
echo package org.george.fxoptiontradebooking.service.impl;
echo.
echo import org.george.fxoptiontradebooking.dto.request.TradeBookingRequest;
echo import org.george.fxoptiontradebooking.dto.response.TradeResponse;
echo import org.george.fxoptiontradebooking.entity.*;
echo import org.george.fxoptiontradebooking.exception.BusinessValidationException;
echo import org.george.fxoptiontradebooking.exception.TradeNotFoundException;
echo import org.george.fxoptiontradebooking.repository.CounterpartyRepository;
echo import org.george.fxoptiontradebooking.repository.TradeRepository;
echo import org.george.fxoptiontradebooking.service.ValidationService;
echo import org.junit.jupiter.api.BeforeEach;
echo import org.junit.jupiter.api.DisplayName;
echo import org.junit.jupiter.api.Nested;
echo import org.junit.jupiter.api.Test;
echo import org.junit.jupiter.api.extension.ExtendWith;
echo import org.mockito.InjectMocks;
echo import org.mockito.Mock;
echo import org.mockito.junit.jupiter.MockitoExtension;
echo import org.modelmapper.ModelMapper;
echo import org.springframework.data.domain.Page;
echo import org.springframework.data.domain.PageImpl;
echo import org.springframework.data.domain.PageRequest;
echo import org.springframework.data.domain.Pageable;
echo.
echo import java.math.BigDecimal;
echo import java.time.LocalDate;
echo import java.util.Arrays;
echo import java.util.List;
echo import java.util.Optional;
echo.
echo import static org.junit.jupiter.api.Assertions.*;
echo import static org.mockito.ArgumentMatchers.*;
echo import static org.mockito.Mockito.*;
echo.
echo @ExtendWith^(MockitoExtension.class^)
echo @DisplayName^("TradeServiceImpl Tests"^)
echo class TradeServiceImplTest {
echo.
echo     @Mock
echo     private TradeRepository tradeRepository;
echo.
echo     @Mock
echo     private CounterpartyRepository counterpartyRepository;
echo.
echo     @Mock
echo     private ValidationService validationService;
echo.
echo     @Mock
echo     private ModelMapper modelMapper;
echo.
echo     @InjectMocks
echo     private TradeServiceImpl tradeService;
echo.
echo     private TradeBookingRequest validRequest;
echo     private Trade validTrade;
echo     private TradeResponse validResponse;
echo     private Counterparty validCounterparty;
echo.
echo     @BeforeEach
echo     void setUp^(^) {
echo         validRequest = createValidTradeBookingRequest^(^);
echo         validTrade = createValidTrade^(^);
echo         validResponse = createValidTradeResponse^(^);
echo         validCounterparty = createValidCounterparty^(^);
echo     }
echo.
echo     @Nested
echo     @DisplayName^("Book Trade Tests"^)
echo     class BookTradeTests {
echo.
echo         @Test
echo         @DisplayName^("Should book trade successfully"^)
echo         void shouldBookTradeSuccessfully^(^) {
echo             // Given
echo             when^(counterpartyRepository.findById^(anyLong^(^)^)^).thenReturn^(Optional.of^(validCounterparty^)^);
echo             when^(tradeRepository.findByTradeReference^(anyString^(^)^)^).thenReturn^(Optional.empty^(^)^);
echo             when^(tradeRepository.save^(any^(Trade.class^)^)^).thenReturn^(validTrade^);
echo             when^(modelMapper.map^(validTrade, TradeResponse.class^)^).thenReturn^(validResponse^);
echo             doNothing^(^).when^(validationService^).validateTradeRequest^(any^(^)^);
echo.
echo             // When
echo             TradeResponse result = tradeService.bookTrade^(validRequest^);
echo.
echo             // Then
echo             assertNotNull^(result^);
echo             assertEquals^(validResponse.getTradeReference^(^), result.getTradeReference^(^)^);
echo             verify^(validationService^).validateTradeRequest^(validRequest^);
echo             verify^(counterpartyRepository^).findById^(validRequest.getCounterpartyId^(^)^);
echo             verify^(tradeRepository^).save^(any^(Trade.class^)^);
echo         }
echo.
echo         @Test
echo         @DisplayName^("Should throw exception for null request"^)
echo         void shouldThrowExceptionForNullRequest^(^) {
echo             // When ^& Then
echo             BusinessValidationException exception = assertThrows^(
echo                 BusinessValidationException.class,
echo                 ^(^) -^> tradeService.bookTrade^(null^)
echo             ^);
echo             assertEquals^("Trade booking request cannot be null", exception.getMessage^(^)^);
echo         }
echo.
echo         @Test
echo         @DisplayName^("Should throw exception for non-existent counterparty"^)
echo         void shouldThrowExceptionForNonExistentCounterparty^(^) {
echo             // Given
echo             when^(counterpartyRepository.findById^(anyLong^(^)^)^).thenReturn^(Optional.empty^(^)^);
echo.
echo             // When ^& Then
echo             BusinessValidationException exception = assertThrows^(
echo                 BusinessValidationException.class,
echo                 ^(^) -^> tradeService.bookTrade^(validRequest^)
echo             ^);
echo             assertTrue^(exception.getMessage^(^).contains^("Counterparty not found"^)^);
echo         }
echo.
echo         @Test
echo         @DisplayName^("Should throw exception for inactive counterparty"^)
echo         void shouldThrowExceptionForInactiveCounterparty^(^) {
echo             // Given
echo             validCounterparty.setIsActive^(false^);
echo             when^(counterpartyRepository.findById^(anyLong^(^)^)^).thenReturn^(Optional.of^(validCounterparty^)^);
echo.
echo             // When ^& Then
echo             BusinessValidationException exception = assertThrows^(
echo                 BusinessValidationException.class,
echo                 ^(^) -^> tradeService.bookTrade^(validRequest^)
echo             ^);
echo             assertTrue^(exception.getMessage^(^).contains^("Cannot trade with inactive counterparty"^)^);
echo         }
echo.
echo         @Test
echo         @DisplayName^("Should throw exception for duplicate trade reference"^)
echo         void shouldThrowExceptionForDuplicateTradeReference^(^) {
echo             // Given
echo             when^(counterpartyRepository.findById^(anyLong^(^)^)^).thenReturn^(Optional.of^(validCounterparty^)^);
echo             when^(tradeRepository.findByTradeReference^(anyString^(^)^)^).thenReturn^(Optional.of^(validTrade^)^);
echo             doNothing^(^).when^(validationService^).validateTradeRequest^(any^(^)^);
echo.
echo             // When ^& Then
echo             BusinessValidationException exception = assertThrows^(
echo                 BusinessValidationException.class,
echo                 ^(^) -^> tradeService.bookTrade^(validRequest^)
echo             ^);
echo             assertTrue^(exception.getMessage^(^).contains^("Trade reference already exists"^)^);
echo         }
echo     }
echo.
echo     @Nested
echo     @DisplayName^("Get Trade Tests"^)
echo     class GetTradeTests {
echo.
echo         @Test
echo         @DisplayName^("Should get trade by ID successfully"^)
echo         void shouldGetTradeByIdSuccessfully^(^) {
echo             // Given
echo             Long tradeId = 1L;
echo             when^(tradeRepository.findById^(tradeId^)^).thenReturn^(Optional.of^(validTrade^)^);
echo             when^(modelMapper.map^(validTrade, TradeResponse.class^)^).thenReturn^(validResponse^);
echo.
echo             // When
echo             TradeResponse result = tradeService.getTradeById^(tradeId^);
echo.
echo             // Then
echo             assertNotNull^(result^);
echo             assertEquals^(validResponse.getTradeReference^(^), result.getTradeReference^(^)^);
echo             verify^(tradeRepository^).findById^(tradeId^);
echo         }
echo.
echo         @Test
echo         @DisplayName^("Should throw exception when trade not found by ID"^)
echo         void shouldThrowExceptionWhenTradeNotFoundById^(^) {
echo             // Given
echo             Long tradeId = 999L;
echo             when^(tradeRepository.findById^(tradeId^)^).thenReturn^(Optional.empty^(^)^);
echo.
echo             // When ^& Then
echo             TradeNotFoundException exception = assertThrows^(
echo                 TradeNotFoundException.class,
echo                 ^(^) -^> tradeService.getTradeById^(tradeId^)
echo             ^);
echo             assertEquals^("Trade not found with ID: " + tradeId, exception.getMessage^(^)^);
echo         }
echo     }
echo.
echo     private TradeBookingRequest createValidTradeBookingRequest^(^) {
echo         TradeBookingRequest request = new TradeBookingRequest^(^);
echo         request.setTradeReference^("TRD-001"^);
echo         request.setCounterpartyId^(1L^);
echo         request.setBaseCurrency^("EUR"^);
echo         request.setQuoteCurrency^("USD"^);
echo         request.setNotionalAmount^(new BigDecimal^("100000.00"^)^);
echo         request.setStrikePrice^(new BigDecimal^("1.2500"^)^);
echo         request.setSpotRate^(new BigDecimal^("1.2000"^)^);
echo         request.setTradeDate^(LocalDate.now^(^)^);
echo         request.setValueDate^(LocalDate.now^(^).plusDays^(2^)^);
echo         request.setMaturityDate^(LocalDate.now^(^).plusDays^(30^)^);
echo         request.setOptionType^(OptionType.CALL^);
echo         request.setCreatedBy^("TEST_USER"^);
echo         return request;
echo     }
echo.
echo     private Trade createValidTrade^(^) {
echo         Trade trade = new Trade^(^);
echo         trade.setTradeId^(1L^);
echo         trade.setTradeReference^("TRD-001"^);
echo         trade.setCounterparty^(createValidCounterparty^(^)^);
echo         trade.setBaseCurrency^("EUR"^);
echo         trade.setQuoteCurrency^("USD"^);
echo         trade.setNotionalAmount^(new BigDecimal^("100000.00"^)^);
echo         trade.setStrikePrice^(new BigDecimal^("1.2500"^)^);
echo         trade.setSpotRate^(new BigDecimal^("1.2000"^)^);
echo         trade.setTradeDate^(LocalDate.now^(^)^);
echo         trade.setValueDate^(LocalDate.now^(^).plusDays^(2^)^);
echo         trade.setMaturityDate^(LocalDate.now^(^).plusDays^(30^)^);
echo         trade.setOptionType^(OptionType.CALL^);
echo         trade.setStatus^(TradeStatus.PENDING^);
echo         trade.setCreatedBy^("TEST_USER"^);
echo         return trade;
echo     }
echo.
echo     private TradeResponse createValidTradeResponse^(^) {
echo         TradeResponse response = new TradeResponse^(^);
echo         response.setTradeId^(1L^);
echo         response.setTradeReference^("TRD-001"^);
echo         response.setCounterpartyId^(1L^);
echo         response.setBaseCurrency^("EUR"^);
echo         response.setQuoteCurrency^("USD"^);
echo         response.setNotionalAmount^(new BigDecimal^("100000.00"^)^);
echo         response.setStrikePrice^(new BigDecimal^("1.2500"^)^);
echo         response.setStatus^(TradeStatus.PENDING^);
echo         return response;
echo     }
echo.
echo     private Counterparty createValidCounterparty^(^) {
echo         Counterparty counterparty = new Counterparty^(^);
echo         counterparty.setCounterpartyId^(1L^);
echo         counterparty.setCounterpartyCode^("CP001"^);
echo         counterparty.setName^("Test Counterparty"^);
echo         counterparty.setIsActive^(true^);
echo         return counterparty;
echo     }
echo }
) > "src\test\java\org\george\fxoptiontradebooking\service\impl\TradeServiceImplTest.java"

echo Creating CounterpartyControllerTest.java...
(
echo package org.george.fxoptiontradebooking.controller;
echo.
echo import com.fasterxml.jackson.databind.ObjectMapper;
echo import org.george.fxoptiontradebooking.dto.request.CounterpartyRequest;
echo import org.george.fxoptiontradebooking.dto.response.CounterpartyResponse;
echo import org.george.fxoptiontradebooking.exception.BusinessValidationException;
echo import org.george.fxoptiontradebooking.exception.TradeNotFoundException;
echo import org.george.fxoptiontradebooking.service.CounterpartyService;
echo import org.junit.jupiter.api.BeforeEach;
echo import org.junit.jupiter.api.DisplayName;
echo import org.junit.jupiter.api.Nested;
echo import org.junit.jupiter.api.Test;
echo import org.springframework.beans.factory.annotation.Autowired;
echo import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
echo import org.springframework.boot.test.mock.mockito.MockBean;
echo import org.springframework.http.MediaType;
echo import org.springframework.test.web.servlet.MockMvc;
echo.
echo import java.util.Arrays;
echo import java.util.List;
echo.
echo import static org.mockito.ArgumentMatchers.any;
echo import static org.mockito.ArgumentMatchers.anyLong;
echo import static org.mockito.ArgumentMatchers.anyString;
echo import static org.mockito.Mockito.*;
echo import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
echo import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
echo.
echo @WebMvcTest^(CounterpartyController.class^)
echo @DisplayName^("CounterpartyController Tests"^)
echo class CounterpartyControllerTest {
echo.
echo     @Autowired
echo     private MockMvc mockMvc;
echo.
echo     @MockBean
echo     private CounterpartyService counterpartyService;
echo.
echo     @Autowired
echo     private ObjectMapper objectMapper;
echo.
echo     private CounterpartyRequest validRequest;
echo     private CounterpartyResponse validResponse;
echo.
echo     @BeforeEach
echo     void setUp^(^) {
echo         validRequest = createValidCounterpartyRequest^(^);
echo         validResponse = createValidCounterpartyResponse^(^);
echo     }
echo.
echo     @Nested
echo     @DisplayName^("Create Counterparty Tests"^)
echo     class CreateCounterpartyTests {
echo.
echo         @Test
echo         @DisplayName^("Should create counterparty successfully"^)
echo         void shouldCreateCounterpartySuccessfully^(^) throws Exception {
echo             // Given
echo             when^(counterpartyService.createCounterparty^(any^(CounterpartyRequest.class^)^)^).thenReturn^(validResponse^);
echo.
echo             // When ^& Then
echo             mockMvc.perform^(post^("/api/counterparties"^)
echo                     .contentType^(MediaType.APPLICATION_JSON^)
echo                     .content^(objectMapper.writeValueAsString^(validRequest^)^)^)
echo                     .andExpect^(status^(^).isCreated^(^)^)
echo                     .andExpect^(jsonPath^("$.success"^).value^(true^)^)
echo                     .andExpect^(jsonPath^("$.data.counterpartyCode"^).value^(validResponse.getCounterpartyCode^(^)^)^)
echo                     .andExpect^(jsonPath^("$.data.name"^).value^(validResponse.getName^(^)^)^);
echo.
echo             verify^(counterpartyService^).createCounterparty^(any^(CounterpartyRequest.class^)^);
echo         }
echo.
echo         @Test
echo         @DisplayName^("Should return bad request for business validation exception"^)
echo         void shouldReturnBadRequestForBusinessValidationException^(^) throws Exception {
echo             // Given
echo             when^(counterpartyService.createCounterparty^(any^(CounterpartyRequest.class^)^)^)
echo                     .thenThrow^(new BusinessValidationException^("Counterparty code already exists"^)^);
echo.
echo             // When ^& Then
echo             mockMvc.perform^(post^("/api/counterparties"^)
echo                     .contentType^(MediaType.APPLICATION_JSON^)
echo                     .content^(objectMapper.writeValueAsString^(validRequest^)^)^)
echo                     .andExpect^(status^(^).isBadRequest^(^)^)
echo                     .andExpect^(jsonPath^("$.success"^).value^(false^)^)
echo                     .andExpect^(jsonPath^("$.message"^).value^("Counterparty code already exists"^)^);
echo         }
echo     }
echo.
echo     @Nested
echo     @DisplayName^("Get Counterparty Tests"^)
echo     class GetCounterpartyTests {
echo.
echo         @Test
echo         @DisplayName^("Should get counterparty by ID successfully"^)
echo         void shouldGetCounterpartyByIdSuccessfully^(^) throws Exception {
echo             // Given
echo             Long counterpartyId = 1L;
echo             when^(counterpartyService.getCounterpartyById^(counterpartyId^)^).thenReturn^(validResponse^);
echo.
echo             // When ^& Then
echo             mockMvc.perform^(get^("/api/counterparties/{id}", counterpartyId^)^)
echo                     .andExpect^(status^(^).isOk^(^)^)
echo                     .andExpect^(jsonPath^("$.success"^).value^(true^)^)
echo                     .andExpect^(jsonPath^("$.data.counterpartyId"^).value^(counterpartyId^)^)
echo                     .andExpect^(jsonPath^("$.data.counterpartyCode"^).value^(validResponse.getCounterpartyCode^(^)^)^);
echo.
echo             verify^(counterpartyService^).getCounterpartyById^(counterpartyId^);
echo         }
echo.
echo         @Test
echo         @DisplayName^("Should return not found for non-existent counterparty"^)
echo         void shouldReturnNotFoundForNonExistentCounterparty^(^) throws Exception {
echo             // Given
echo             Long counterpartyId = 999L;
echo             when^(counterpartyService.getCounterpartyById^(counterpartyId^)^)
echo                     .thenThrow^(new TradeNotFoundException^("Counterparty not found with ID: " + counterpartyId^)^);
echo.
echo             // When ^& Then
echo             mockMvc.perform^(get^("/api/counterparties/{id}", counterpartyId^)^)
echo                     .andExpect^(status^(^).isNotFound^(^)^)
echo                     .andExpect^(jsonPath^("$.success"^).value^(false^)^)
echo                     .andExpect^(jsonPath^("$.message"^).value^("Counterparty not found with ID: " + counterpartyId^)^);
echo         }
echo.
echo         @Test
echo         @DisplayName^("Should get all active counterparties successfully"^)
echo         void shouldGetAllActiveCounterpartiesSuccessfully^(^) throws Exception {
echo             // Given
echo             List^<CounterpartyResponse^> activeCounterparties = Arrays.asList^(validResponse^);
echo             when^(counterpartyService.getAllActiveCounterparties^(^)^).thenReturn^(activeCounterparties^);
echo.
echo             // When ^& Then
echo             mockMvc.perform^(get^("/api/counterparties/active"^)^)
echo                     .andExpect^(status^(^).isOk^(^)^)
echo                     .andExpect^(jsonPath^("$.success"^).value^(true^)^)
echo                     .andExpect^(jsonPath^("$.data"^).isArray^(^)^)
echo                     .andExpect^(jsonPath^("$.data[0].counterpartyCode"^).value^(validResponse.getCounterpartyCode^(^)^)^);
echo.
echo             verify^(counterpartyService^).getAllActiveCounterparties^(^);
echo         }
echo     }
echo.
echo     private CounterpartyRequest createValidCounterpartyRequest^(^) {
echo         CounterpartyRequest request = new CounterpartyRequest^(^);
echo         request.setCounterpartyCode^("CP001"^);
echo         request.setName^("Test Counterparty"^);
echo         request.setLeiCode^("12345678901234567890"^);
echo         request.setSwiftCode^("TESTUS33"^);
echo         request.setCreditRating^("AA"^);
echo         request.setIsActive^(true^);
echo         return request;
echo     }
echo.
echo     private CounterpartyResponse createValidCounterpartyResponse^(^) {
echo         CounterpartyResponse response = new CounterpartyResponse^(^);
echo         response.setCounterpartyId^(1L^);
echo         response.setCounterpartyCode^("CP001"^);
echo         response.setName^("Test Counterparty"^);
echo         response.setLeiCode^("12345678901234567890"^);
echo         response.setSwiftCode^("TESTUS33"^);
echo         response.setCreditRating^("AA"^);
echo         response.setIsActive^(true^);
echo         return response;
echo     }
echo }
) > "src\test\java\org\george\fxoptiontradebooking\controller\CounterpartyControllerTest.java"

echo Creating TradeControllerTest.java...
(
echo package org.george.fxoptiontradebooking.controller;
echo.
echo import com.fasterxml.jackson.databind.ObjectMapper;
echo import org.george.fxoptiontradebooking.dto.request.TradeBookingRequest;
echo import org.george.fxoptiontradebooking.dto.response.TradeResponse;
echo import org.george.fxoptiontradebooking.entity.OptionType;
echo import org.george.fxoptiontradebooking.entity.TradeStatus;
echo import org.george.fxoptiontradebooking.exception.BusinessValidationException;
echo import org.george.fxoptiontradebooking.exception.TradeNotFoundException;
echo import org.george.fxoptiontradebooking.service.TradeService;
echo import org.junit.jupiter.api.BeforeEach;
echo import org.junit.jupiter.api.DisplayName;
echo import org.junit.jupiter.api.Nested;
echo import org.junit.jupiter.api.Test;
echo import org.springframework.beans.factory.annotation.Autowired;
echo import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
echo import org.springframework.boot.test.mock.mockito.MockBean;
echo import org.springframework.data.domain.Page;
echo import org.springframework.data.domain.PageImpl;
echo import org.springframework.data.domain.PageRequest;
echo import org.springframework.http.MediaType;
echo import org.springframework.test.web.servlet.MockMvc;
echo.
echo import java.math.BigDecimal;
echo import java.time.LocalDate;
echo import java.util.Arrays;
echo import java.util.List;
echo.
echo import static org.mockito.ArgumentMatchers.*;
echo import static org.mockito.Mockito.*;
echo import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
echo import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
echo.
echo @WebMvcTest^(TradeController.class^)
echo @DisplayName^("TradeController Tests"^)
echo class TradeControllerTest {
echo.
echo     @Autowired
echo     private MockMvc mockMvc;
echo.
echo     @MockBean
echo     private TradeService tradeService;
echo.
echo     @Autowired
echo     private ObjectMapper objectMapper;
echo.
echo     private TradeBookingRequest validRequest;
echo     private TradeResponse validResponse;
echo.
echo     @BeforeEach
echo     void setUp^(^) {
echo         validRequest = createValidTradeBookingRequest^(^);
echo         validResponse = createValidTradeResponse^(^);
echo     }
echo.
echo     @Nested
echo     @DisplayName^("Book Trade Tests"^)
echo     class BookTradeTests {
echo.
echo         @Test
echo         @DisplayName^("Should book trade successfully"^)
echo         void shouldBookTradeSuccessfully^(^) throws Exception {
echo             // Given
echo             when^(tradeService.bookTrade^(any^(TradeBookingRequest.class^)^)^).thenReturn^(validResponse^);
echo.
echo             // When ^& Then
echo             mockMvc.perform^(post^("/api/trades"^)
echo                     .contentType^(MediaType.APPLICATION_JSON^)
echo                     .content^(objectMapper.writeValueAsString^(validRequest^)^)^)
echo                     .andExpect^(status^(^).isCreated^(^)^)
echo                     .andExpect^(jsonPath^("$.success"^).value^(true^)^)
echo                     .andExpect^(jsonPath^("$.data.tradeReference"^).value^(validResponse.getTradeReference^(^)^)^)
echo                     .andExpect^(jsonPath^("$.data.status"^).value^(validResponse.getStatus^(^).toString^(^)^)^);
echo.
echo             verify^(tradeService^).bookTrade^(any^(TradeBookingRequest.class^)^);
echo         }
echo.
echo         @Test
echo         @DisplayName^("Should return bad request for business validation exception"^)
echo         void shouldReturnBadRequestForBusinessValidationException^(^) throws Exception {
echo             // Given
echo             when^(tradeService.bookTrade^(any^(TradeBookingRequest.class^)^)^)
echo                     .thenThrow^(new BusinessValidationException^("Trade reference already exists"^)^);
echo.
echo             // When ^& Then
echo             mockMvc.perform^(post^("/api/trades"^)
echo                     .contentType^(MediaType.APPLICATION_JSON^)
echo                     .content^(objectMapper.writeValueAsString^(validRequest^)^)^)
echo                     .andExpect^(status^(^).isBadRequest^(^)^)
echo                     .andExpect^(jsonPath^("$.success"^).value^(false^)^)
echo                     .andExpect^(jsonPath^("$.message"^).value^("Trade reference already exists"^)^);
echo         }
echo     }
echo.
echo     @Nested
echo     @DisplayName^("Get Trade Tests"^)
echo     class GetTradeTests {
echo.
echo         @Test
echo         @DisplayName^("Should get trade by ID successfully"^)
echo         void shouldGetTradeByIdSuccessfully^(^) throws Exception {
echo             // Given
echo             Long tradeId = 1L;
echo             when^(tradeService.getTradeById^(tradeId^)^).thenReturn^(validResponse^);
echo.
echo             // When ^& Then
echo             mockMvc.perform^(get^("/api/trades/{id}", tradeId^)^)
echo                     .andExpect^(status^(^).isOk^(^)^)
echo                     .andExpect^(jsonPath^("$.success"^).value^(true^)^)
echo                     .andExpect^(jsonPath^("$.data.tradeId"^).value^(tradeId^)^)
echo                     .andExpect^(jsonPath^("$.data.tradeReference"^).value^(validResponse.getTradeReference^(^)^)^);
echo.
echo             verify^(tradeService^).getTradeById^(tradeId^);
echo         }
echo.
echo         @Test
echo         @DisplayName^("Should return not found for non-existent trade"^)
echo         void shouldReturnNotFoundForNonExistentTrade^(^) throws Exception {
echo             // Given
echo             Long tradeId = 999L;
echo             when^(tradeService.getTradeById^(tradeId^)^)
echo                     .thenThrow^(new TradeNotFoundException^("Trade not found with ID: " + tradeId^)^);
echo.
echo             // When ^& Then
echo             mockMvc.perform^(get^("/api/trades/{id}", tradeId^)^)
echo                     .andExpect^(status^(^).isNotFound^(^)^)
echo                     .andExpect^(jsonPath^("$.success"^).value^(false^)^)
echo                     .andExpect^(jsonPath^("$.message"^).value^("Trade not found with ID: " + tradeId^)^);
echo         }
echo.
echo         @Test
echo         @DisplayName^("Should get all trades with pagination successfully"^)
echo         void shouldGetAllTradesWithPaginationSuccessfully^(^) throws Exception {
echo             // Given
echo             Page^<TradeResponse^> tradePage = new PageImpl^<^>^(Arrays.asList^(validResponse^)^);
echo             when^(tradeService.getAllTrades^(any^(^)^)^).thenReturn^(tradePage^);
echo.
echo             // When ^& Then
echo             mockMvc.perform^(get^("/api/trades"^)
echo                     .param^("page", "0"^)
echo                     .param^("size", "10"^)^)
echo                     .andExpect^(status^(^).isOk^(^)^)
echo                     .andExpect^(jsonPath^("$.success"^).value^(true^)^)
echo                     .andExpect^(jsonPath^("$.data.content"^).isArray^(^)^);
echo.
echo             verify^(tradeService^).getAllTrades^(any^(^)^);
echo         }
echo     }
echo.
echo     private TradeBookingRequest createValidTradeBookingRequest^(^) {
echo         TradeBookingRequest request = new TradeBookingRequest^(^);
echo         request.setTradeReference^("TRD-001"^);
echo         request.setCounterpartyId^(1L^);
echo         request.setBaseCurrency^("EUR"^);
echo         request.setQuoteCurrency^("USD"^);
echo         request.setNotionalAmount^(new BigDecimal^("100000.00"^)^);
echo         request.setStrikePrice^(new BigDecimal^("1.2500"^)^);
echo         request.setSpotRate^(new BigDecimal^("1.2000"^)^);
echo         request.setTradeDate^(LocalDate.now^(^)^);
echo         request.setValueDate^(LocalDate.now^(^).plusDays^(2^)^);
echo         request.setMaturityDate^(LocalDate.now^(^).plusDays^(30^)^);
echo         request.setOptionType^(OptionType.CALL^);
echo         request.setCreatedBy^("TEST_USER"^);
echo         return request;
echo     }
echo.
echo     private TradeResponse createValidTradeResponse^(^) {
echo         TradeResponse response = new TradeResponse^(^);
echo         response.setTradeId^(1L^);
echo         response.setTradeReference^("TRD-001"^);
echo         response.setCounterpartyId^(1L^);
echo         response.setBaseCurrency^("EUR"^);
echo         response.setQuoteCurrency^("USD"^);
echo         response.setNotionalAmount^(new BigDecimal^("100000.00"^)^);
echo         response.setStrikePrice^(new BigDecimal^("1.2500"^)^);
echo         response.setStatus^(TradeStatus.PENDING^);
echo         return response;
echo     }
echo }
) > "src\test\java\org\george\fxoptiontradebooking\controller\TradeControllerTest.java"

echo.
echo ✅ Test files created successfully!
echo.
echo Created the following test files:
echo   - ValidationServiceImplTest.java
echo   - CounterpartyServiceImplTest.java  
echo   - TradeServiceImplTest.java
echo   - CounterpartyControllerTest.java
echo   - TradeControllerTest.java
echo.
echo You can run the tests with: mvn test
echo.
echo Note: These are simplified versions of the test files. 
echo You may need to add additional imports and implement missing methods
echo based on your actual entity and DTO implementations.
echo.
pause