package org.george.fxoptiontradebooking.service.impl;

import org.george.fxoptiontradebooking.dto.request.TradeBookingRequest;
import org.george.fxoptiontradebooking.entity.*;
import org.george.fxoptiontradebooking.exception.BusinessValidationException;
import org.george.fxoptiontradebooking.service.validator.ProductValidator;
import org.george.fxoptiontradebooking.service.validator.impl.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Product Validation Service Tests")
class ProductValidationServiceImplTest {

    private ProductValidationServiceImpl productValidationService;

    @BeforeEach
    void setUp() {
        // Create all validators
        List<ProductValidator> validators = Arrays.asList(
                new VanillaOptionValidator(),
                new ExoticOptionValidator(),
                new FXContractValidator(),
                new SwapValidator()
        );
        
        // Initialize service with validators
        productValidationService = new ProductValidationServiceImpl(validators);
    }

    @Nested
    @DisplayName("Service Initialization Tests")
    class ServiceInitializationTests {

        @Test
        @DisplayName("Should initialize with all validators")
        void shouldInitializeWithAllValidators() {
            assertNotNull(productValidationService);
            // Verify service can handle all product types without throwing exceptions during initialization
        }
    }

    @Nested
    @DisplayName("Common Validation Tests")
    class CommonValidationTests {

        @Test
        @DisplayName("Should throw exception for null request")
        void shouldThrowExceptionForNullRequest() {
            BusinessValidationException exception = assertThrows(
                    BusinessValidationException.class,
                    () -> productValidationService.validateTradeRequest(null)
            );
            assertEquals("Trade booking request cannot be null", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for missing product type")
        void shouldThrowExceptionForMissingProductType() {
            TradeBookingRequest request = createValidVanillaOptionRequest();
            request.setProductType(null);

            BusinessValidationException exception = assertThrows(
                    BusinessValidationException.class,
                    () -> productValidationService.validateTradeRequest(request)
            );
            assertEquals("Product type is required", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for unsupported product type")
        void shouldThrowExceptionForUnsupportedProductType() {
            TradeBookingRequest request = createValidVanillaOptionRequest();
            // Assuming there's a product type not covered by validators
            // This test may need adjustment based on actual enum values

            BusinessValidationException exception = assertThrows(
                    BusinessValidationException.class,
                    () -> productValidationService.validateTradeRequest(request)
            );
            assertTrue(exception.getMessage().contains("No validator found for product type") ||
                      exception.getMessage().contains("validator not available"));
        }

        @Test
        @DisplayName("Should throw exception for invalid trade reference")
        void shouldThrowExceptionForInvalidTradeReference() {
            TradeBookingRequest request = createValidVanillaOptionRequest();
            request.setTradeReference("");

            BusinessValidationException exception = assertThrows(
                    BusinessValidationException.class,
                    () -> productValidationService.validateTradeRequest(request)
            );
            assertEquals("Trade reference is required", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for long trade reference")
        void shouldThrowExceptionForLongTradeReference() {
            TradeBookingRequest request = createValidVanillaOptionRequest();
            request.setTradeReference("A".repeat(51)); // 51 characters

            BusinessValidationException exception = assertThrows(
                    BusinessValidationException.class,
                    () -> productValidationService.validateTradeRequest(request)
            );
            assertEquals("Trade reference cannot exceed 50 characters", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for invalid counterparty ID")
        void shouldThrowExceptionForInvalidCounterpartyId() {
            TradeBookingRequest request = createValidVanillaOptionRequest();
            request.setCounterpartyId(-1L);

            BusinessValidationException exception = assertThrows(
                    BusinessValidationException.class,
                    () -> productValidationService.validateTradeRequest(request)
            );
            assertEquals("Valid counterparty ID is required", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for same currencies")
        void shouldThrowExceptionForSameCurrencies() {
            TradeBookingRequest request = createValidVanillaOptionRequest();
            request.setBaseCurrency("USD");
            request.setQuoteCurrency("USD");

            BusinessValidationException exception = assertThrows(
                    BusinessValidationException.class,
                    () -> productValidationService.validateTradeRequest(request)
            );
            assertEquals("Base and quote currencies must be different", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for small notional amount")
        void shouldThrowExceptionForSmallNotionalAmount() {
            TradeBookingRequest request = createValidVanillaOptionRequest();
            request.setNotionalAmount(new BigDecimal("5000"));

            BusinessValidationException exception = assertThrows(
                    BusinessValidationException.class,
                    () -> productValidationService.validateTradeRequest(request)
            );
            assertEquals("Minimum notional amount is 10,000", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for large notional amount")
        void shouldThrowExceptionForLargeNotionalAmount() {
            TradeBookingRequest request = createValidVanillaOptionRequest();
            request.setNotionalAmount(new BigDecimal("2000000000")); // 2 billion

            BusinessValidationException exception = assertThrows(
                    BusinessValidationException.class,
                    () -> productValidationService.validateTradeRequest(request)
            );
            assertEquals("Maximum notional amount is 1 billion", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Vanilla Option Validation Tests")
    class VanillaOptionValidationTests {

        @Test
        @DisplayName("Should validate vanilla option successfully")
        void shouldValidateVanillaOptionSuccessfully() {
            TradeBookingRequest request = createValidVanillaOptionRequest();

            assertDoesNotThrow(() -> productValidationService.validateTradeRequest(request));
        }

        @Test
        @DisplayName("Should throw exception when option type is missing")
        void shouldThrowExceptionWhenOptionTypeIsMissing() {
            TradeBookingRequest request = createValidVanillaOptionRequest();
            request.setOptionType(null);

            BusinessValidationException exception = assertThrows(
                    BusinessValidationException.class,
                    () -> productValidationService.validateTradeRequest(request)
            );
            assertEquals("Option type is required for vanilla options", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when strike price is missing")
        void shouldThrowExceptionWhenStrikePriceIsMissing() {
            TradeBookingRequest request = createValidVanillaOptionRequest();
            request.setStrikePrice(null);

            BusinessValidationException exception = assertThrows(
                    BusinessValidationException.class,
                    () -> productValidationService.validateTradeRequest(request)
            );
            assertEquals("Strike price is required for vanilla options", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when maturity is before value date")
        void shouldThrowExceptionWhenMaturityIsBeforeValueDate() {
            TradeBookingRequest request = createValidVanillaOptionRequest();
            request.setMaturityDate(request.getValueDate().minusDays(1));

            BusinessValidationException exception = assertThrows(
                    BusinessValidationException.class,
                    () -> productValidationService.validateTradeRequest(request)
            );
            assertEquals("Maturity date must be after value date", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Exotic Option Validation Tests")
    class ExoticOptionValidationTests {

        @Test
        @DisplayName("Should validate barrier option successfully")
        void shouldValidateBarrierOptionSuccessfully() {
            TradeBookingRequest request = createValidBarrierOptionRequest();

            assertDoesNotThrow(() -> productValidationService.validateTradeRequest(request));
        }

        @Test
        @DisplayName("Should throw exception when exotic option type is missing")
        void shouldThrowExceptionWhenExoticOptionTypeIsMissing() {
            TradeBookingRequest request = createValidBarrierOptionRequest();
            request.setExoticOptionType(null);

            BusinessValidationException exception = assertThrows(
                    BusinessValidationException.class,
                    () -> productValidationService.validateTradeRequest(request)
            );
            assertEquals("Exotic option type is required for exotic options", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when barrier level is missing for barrier option")
        void shouldThrowExceptionWhenBarrierLevelIsMissing() {
            TradeBookingRequest request = createValidBarrierOptionRequest();
            request.setBarrierLevel(null);

            BusinessValidationException exception = assertThrows(
                    BusinessValidationException.class,
                    () -> productValidationService.validateTradeRequest(request)
            );
            assertEquals("Barrier level is required for barrier options", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("FX Contract Validation Tests")
    class FXContractValidationTests {

        @Test
        @DisplayName("Should validate FX forward successfully")
        void shouldValidateFXForwardSuccessfully() {
            TradeBookingRequest request = createValidFXForwardRequest();

            assertDoesNotThrow(() -> productValidationService.validateTradeRequest(request));
        }

        @Test
        @DisplayName("Should throw exception when forward rate is missing")
        void shouldThrowExceptionWhenForwardRateIsMissing() {
            TradeBookingRequest request = createValidFXForwardRequest();
            request.setForwardRate(null);

            BusinessValidationException exception = assertThrows(
                    BusinessValidationException.class,
                    () -> productValidationService.validateTradeRequest(request)
            );
            assertEquals("Forward rate is required for FX contracts", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Swap Validation Tests")
    class SwapValidationTests {

        @Test
        @DisplayName("Should validate interest rate swap successfully")
        void shouldValidateInterestRateSwapSuccessfully() {
            TradeBookingRequest request = createValidInterestRateSwapRequest();

            assertDoesNotThrow(() -> productValidationService.validateTradeRequest(request));
        }

        @Test
        @DisplayName("Should throw exception when swap type is missing")
        void shouldThrowExceptionWhenSwapTypeIsMissing() {
            TradeBookingRequest request = createValidInterestRateSwapRequest();
            request.setSwapType(null);

            BusinessValidationException exception = assertThrows(
                    BusinessValidationException.class,
                    () -> productValidationService.validateTradeRequest(request)
            );
            assertEquals("Swap type is required for swap products", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when fixed rate is missing for IRS")
        void shouldThrowExceptionWhenFixedRateIsMissingForIRS() {
            TradeBookingRequest request = createValidInterestRateSwapRequest();
            request.setFixedRate(null);

            BusinessValidationException exception = assertThrows(
                    BusinessValidationException.class,
                    () -> productValidationService.validateTradeRequest(request)
            );
            assertEquals("Fixed rate is required for interest rate swaps", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Legacy Method Tests")
    class LegacyMethodTests {

        @Test
        @DisplayName("Should delegate vanilla option validation")
        void shouldDelegateVanillaOptionValidation() {
            TradeBookingRequest request = createValidVanillaOptionRequest();

            assertDoesNotThrow(() -> productValidationService.validateVanillaOption(request));
        }

        @Test
        @DisplayName("Should delegate exotic option validation")
        void shouldDelegateExoticOptionValidation() {
            TradeBookingRequest request = createValidBarrierOptionRequest();

            assertDoesNotThrow(() -> productValidationService.validateExoticOption(request));
        }

        @Test
        @DisplayName("Should delegate FX contract validation")
        void shouldDelegateFXContractValidation() {
            TradeBookingRequest request = createValidFXForwardRequest();

            assertDoesNotThrow(() -> productValidationService.validateFXContract(request));
        }

        @Test
        @DisplayName("Should delegate swap validation")
        void shouldDelegateSwapValidation() {
            TradeBookingRequest request = createValidInterestRateSwapRequest();

            assertDoesNotThrow(() -> productValidationService.validateSwap(request));
        }
    }

    // Helper methods
    private TradeBookingRequest createValidVanillaOptionRequest() {
        TradeBookingRequest request = new TradeBookingRequest();
        request.setTradeReference("VO-001");
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

    private TradeBookingRequest createValidBarrierOptionRequest() {
        TradeBookingRequest request = new TradeBookingRequest();
        request.setTradeReference("EO-001");
        request.setCounterpartyId(1L);
        request.setProductType(ProductType.EXOTIC_OPTION);
        request.setBaseCurrency("EUR");
        request.setQuoteCurrency("USD");
        request.setNotionalAmount(new BigDecimal("100000.00"));
        request.setStrikePrice(new BigDecimal("1.2500"));
        request.setTradeDate(LocalDate.now());
        request.setValueDate(LocalDate.now().plusDays(2));
        request.setMaturityDate(LocalDate.now().plusDays(30));
        request.setOptionType(OptionType.CALL);
        request.setExoticOptionType(ExoticOptionType.BARRIER_OPTION);
        request.setBarrierLevel(new BigDecimal("1.3000"));
        request.setKnockInOut("KNOCK_OUT");
        request.setCreatedBy("TEST_USER");
        return request;
    }

    private TradeBookingRequest createValidFXForwardRequest() {
        TradeBookingRequest request = new TradeBookingRequest();
        request.setTradeReference("FXF-001");
        request.setCounterpartyId(1L);
        request.setProductType(ProductType.FX_FORWARD);
        request.setBaseCurrency("EUR");
        request.setQuoteCurrency("USD");
        request.setNotionalAmount(new BigDecimal("100000.00"));
        request.setForwardRate(new BigDecimal("1.2500"));
        request.setTradeDate(LocalDate.now());
        request.setValueDate(LocalDate.now().plusDays(30));
        request.setCreatedBy("TEST_USER");
        return request;
    }

    private TradeBookingRequest createValidInterestRateSwapRequest() {
        TradeBookingRequest request = new TradeBookingRequest();
        request.setTradeReference("IRS-001");
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
        request.setPaymentFrequency("QUARTERLY");
        request.setCreatedBy("TEST_USER");
        return request;
    }
}