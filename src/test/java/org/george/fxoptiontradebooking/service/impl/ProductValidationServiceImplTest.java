package org.george.fxoptiontradebooking.service.impl;

import org.george.fxoptiontradebooking.dto.request.TradeBookingRequest;
import org.george.fxoptiontradebooking.entity.*;
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
@DisplayName("Product Validation Service Tests")
class ProductValidationServiceImplTest {

    @InjectMocks
    private ProductValidationServiceImpl productValidationService;

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
            assertEquals("Forward rate is required for FX forwards", exception.getMessage());
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
    @DisplayName("Common Validation Tests")
    class CommonValidationTests {

        @Test
        @DisplayName("Should throw exception for unsupported currency")
        void shouldThrowExceptionForUnsupportedCurrency() {
            TradeBookingRequest request = createValidVanillaOptionRequest();
            request.setBaseCurrency("XYZ"); // Unsupported currency
            
            BusinessValidationException exception = assertThrows(
                BusinessValidationException.class, 
                () -> productValidationService.validateTradeRequest(request)
            );
            assertEquals("Unsupported base currency: XYZ", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when base and quote currencies are same")
        void shouldThrowExceptionWhenCurrenciesAreSame() {
            TradeBookingRequest request = createValidVanillaOptionRequest();
            request.setQuoteCurrency(request.getBaseCurrency());
            
            BusinessValidationException exception = assertThrows(
                BusinessValidationException.class, 
                () -> productValidationService.validateTradeRequest(request)
            );
            assertEquals("Base and quote currencies cannot be the same", exception.getMessage());
        }
    }

    // Helper methods
    private TradeBookingRequest createValidVanillaOptionRequest() {
        TradeBookingRequest request = new TradeBookingRequest();
        request.setProductType(ProductType.VANILLA_OPTION);
        request.setOptionType(OptionType.CALL);
        request.setBaseCurrency("EUR");
        request.setQuoteCurrency("USD");
        request.setNotionalAmount(new BigDecimal("1000000"));
        request.setStrikePrice(new BigDecimal("1.1000"));
        request.setTradeDate(LocalDate.now());
        request.setValueDate(LocalDate.now().plusDays(2));
        request.setMaturityDate(LocalDate.now().plusMonths(1));
        return request;
    }

    private TradeBookingRequest createValidBarrierOptionRequest() {
        TradeBookingRequest request = createValidVanillaOptionRequest();
        request.setProductType(ProductType.EXOTIC_OPTION);
        request.setExoticOptionType(ExoticOptionType.BARRIER_OPTION);
        request.setBarrierLevel(new BigDecimal("1.1500"));
        request.setKnockInOut("OUT");
        return request;
    }

    private TradeBookingRequest createValidFXForwardRequest() {
        TradeBookingRequest request = new TradeBookingRequest();
        request.setProductType(ProductType.FX_FORWARD);
        request.setBaseCurrency("GBP");
        request.setQuoteCurrency("USD");
        request.setNotionalAmount(new BigDecimal("2000000"));
        request.setForwardRate(new BigDecimal("1.2750"));
        request.setTradeDate(LocalDate.now());
        request.setValueDate(LocalDate.now().plusDays(2));
        request.setMaturityDate(LocalDate.now().plusMonths(3));
        return request;
    }

    private TradeBookingRequest createValidInterestRateSwapRequest() {
        TradeBookingRequest request = new TradeBookingRequest();
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
        return request;
    }
}