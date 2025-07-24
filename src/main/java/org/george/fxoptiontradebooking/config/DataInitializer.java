package org.george.fxoptiontradebooking.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.george.fxoptiontradebooking.entity.*;
import org.george.fxoptiontradebooking.repository.CounterpartyRepository;
import org.george.fxoptiontradebooking.repository.TradeRepository;
import org.george.fxoptiontradebooking.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Initializes comprehensive sample data for multi-product financial trading system.
 * Creates default users, counterparties, and diverse sample trades across all product types
 * including vanilla options, exotic options, FX contracts, and swap instruments.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TradeRepository tradeRepository;
    private final CounterpartyRepository counterpartyRepository;

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting data initialization...");
        
        if (userRepository.count() == 0) {
            createSampleUsers();
            log.info("Sample users created");
        }
        
        if (counterpartyRepository.count() == 0) {
            createSampleCounterparties();
            log.info("Sample counterparties created");
        }
        
        if (tradeRepository.count() == 0) {
            createSampleTrades();
            log.info("Sample trades created");
        }
        
        log.info("Data initialization completed");
    }

    private void createSampleUsers() {
        List<User> users = List.of(
            createUser("admin", "admin@fxtrading.com", "password", Set.of(Role.ADMIN)),
            createUser("trader1", "trader1@fxtrading.com", "password", Set.of(Role.TRADER)),
            createUser("trader2", "trader2@fxtrading.com", "password", Set.of(Role.TRADER)),
            createUser("user1", "user1@fxtrading.com", "password", Set.of(Role.USER))
        );
        userRepository.saveAll(users);
    }

    private User createUser(String username, String email, String password, Set<Role> roles) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRoles(roles);
        user.setEnabled(true);
        return user;
    }

    private void createSampleCounterparties() {
        List<Counterparty> counterparties = List.of(
            createCounterparty("CP001", "Goldman Sachs", "GOLDMAN", "AAA"),
            createCounterparty("CP002", "JPMorgan Chase", "JPM", "AA+"),
            createCounterparty("CP003", "Deutsche Bank", "DB", "A+"),
            createCounterparty("CP004", "UBS Group", "UBS", "AA-"),
            createCounterparty("CP005", "Barclays", "BARC", "A"),
            createCounterparty("CP006", "Credit Suisse", "CS", "BBB+")
        );
        counterpartyRepository.saveAll(counterparties);
    }

    private Counterparty createCounterparty(String code, String name, String shortName, String rating) {
        Counterparty counterparty = new Counterparty();
        counterparty.setCounterpartyCode(code);
        counterparty.setName(name);
        counterparty.setCreditRating(rating);
        counterparty.setIsActive(true);
        counterparty.setCreatedAt(LocalDateTime.now());
        return counterparty;
    }

    private void createSampleTrades() {
        List<Counterparty> counterparties = counterpartyRepository.findAll();
        
        // Create vanilla options
        createVanillaOptions(counterparties);
        
        // Create exotic options
        createExoticOptions(counterparties);
        
        // Create FX contracts
        createFXContracts(counterparties);
        
        // Create swaps
        createSwaps(counterparties);
    }

    private void createVanillaOptions(List<Counterparty> counterparties) {
        List<Trade> vanillaOptions = List.of(
            createVanillaOption("VO-EUR-USD-001", counterparties.get(0), OptionType.CALL, 
                "EUR", "USD", new BigDecimal("1000000"), new BigDecimal("1.1000"), 
                new BigDecimal("1.0950"), LocalDate.now().plusDays(30)),
                
            createVanillaOption("VO-GBP-USD-002", counterparties.get(1), OptionType.PUT,
                "GBP", "USD", new BigDecimal("500000"), new BigDecimal("1.2500"),
                new BigDecimal("1.2600"), LocalDate.now().plusDays(45)),
                
            createVanillaOption("VO-USD-JPY-003", counterparties.get(2), OptionType.CALL,
                "USD", "JPY", new BigDecimal("2000000"), new BigDecimal("110.00"),
                new BigDecimal("109.50"), LocalDate.now().plusDays(60))
        );
        tradeRepository.saveAll(vanillaOptions);
    }

    private Trade createVanillaOption(String reference, Counterparty counterparty, OptionType optionType,
                                     String baseCurrency, String quoteCurrency, BigDecimal notional,
                                     BigDecimal strike, BigDecimal spot, LocalDate maturity) {
        Trade trade = new Trade();
        trade.setTradeReference(reference);
        trade.setCounterparty(counterparty);
        trade.setProductType(ProductType.VANILLA_OPTION);
        trade.setOptionType(optionType);
        trade.setBaseCurrency(baseCurrency);
        trade.setQuoteCurrency(quoteCurrency);
        trade.setNotionalAmount(notional);
        trade.setStrikePrice(strike);
        trade.setSpotRate(spot);
        trade.setTradeDate(LocalDate.now());
        trade.setValueDate(LocalDate.now().plusDays(2));
        trade.setMaturityDate(maturity);
        trade.setPremiumAmount(notional.multiply(new BigDecimal("0.025"))); // 2.5% premium
        trade.setPremiumCurrency(baseCurrency);
        trade.setStatus(TradeStatus.CONFIRMED);
        trade.setCreatedBy("trader1");
        return trade;
    }

    private void createExoticOptions(List<Counterparty> counterparties) {
        List<Trade> exoticOptions = List.of(
            createBarrierOption("EO-BARRIER-001", counterparties.get(0)),
            createAsianOption("EO-ASIAN-002", counterparties.get(1)),
            createDigitalOption("EO-DIGITAL-003", counterparties.get(2))
        );
        tradeRepository.saveAll(exoticOptions);
    }

    private Trade createBarrierOption(String reference, Counterparty counterparty) {
        Trade trade = createVanillaOption(reference, counterparty, OptionType.CALL,
            "EUR", "USD", new BigDecimal("1500000"), new BigDecimal("1.1200"),
            new BigDecimal("1.1000"), LocalDate.now().plusDays(90));
        
        trade.setProductType(ProductType.EXOTIC_OPTION);
        trade.setExoticOptionType(ExoticOptionType.BARRIER_OPTION);
        trade.setBarrierLevel(new BigDecimal("1.1500")); // Knock-out barrier
        trade.setKnockInOut("OUT");
        trade.setObservationFrequency("CONTINUOUS");
        trade.setPremiumAmount(trade.getNotionalAmount().multiply(new BigDecimal("0.015"))); // Lower premium due to barrier
        return trade;
    }

    private Trade createAsianOption(String reference, Counterparty counterparty) {
        Trade trade = createVanillaOption(reference, counterparty, OptionType.PUT,
            "GBP", "JPY", new BigDecimal("800000"), new BigDecimal("140.00"),
            new BigDecimal("141.50"), LocalDate.now().plusDays(120));
        
        trade.setProductType(ProductType.EXOTIC_OPTION);
        trade.setExoticOptionType(ExoticOptionType.ASIAN_OPTION);
        trade.setObservationFrequency("DAILY");
        trade.setPremiumAmount(trade.getNotionalAmount().multiply(new BigDecimal("0.020")));
        return trade;
    }

    private Trade createDigitalOption(String reference, Counterparty counterparty) {
        Trade trade = createVanillaOption(reference, counterparty, OptionType.CALL,
            "USD", "CHF", new BigDecimal("1200000"), new BigDecimal("0.9200"),
            new BigDecimal("0.9150"), LocalDate.now().plusDays(30));
        
        trade.setProductType(ProductType.EXOTIC_OPTION);
        trade.setExoticOptionType(ExoticOptionType.DIGITAL_OPTION);
        trade.setPremiumAmount(new BigDecimal("50000")); // Fixed payout for digital
        return trade;
    }

    private void createFXContracts(List<Counterparty> counterparties) {
        List<Trade> fxContracts = List.of(
            createFXSpot("FX-SPOT-001", counterparties.get(3)),
            createFXForward("FX-FWD-002", counterparties.get(4)),
            createFXForward("FX-FWD-003", counterparties.get(5))
        );
        tradeRepository.saveAll(fxContracts);
    }

    private Trade createFXSpot(String reference, Counterparty counterparty) {
        Trade trade = new Trade();
        trade.setTradeReference(reference);
        trade.setCounterparty(counterparty);
        trade.setProductType(ProductType.FX_SPOT);
        trade.setBaseCurrency("EUR");
        trade.setQuoteCurrency("USD");
        trade.setNotionalAmount(new BigDecimal("5000000"));
        trade.setSpotRate(new BigDecimal("1.0980"));
        trade.setTradeDate(LocalDate.now());
        trade.setValueDate(LocalDate.now().plusDays(2));
        trade.setStatus(TradeStatus.CONFIRMED);
        trade.setCreatedBy("trader2");
        return trade;
    }

    private Trade createFXForward(String reference, Counterparty counterparty) {
        Trade trade = new Trade();
        trade.setTradeReference(reference);
        trade.setCounterparty(counterparty);
        trade.setProductType(ProductType.FX_FORWARD);
        trade.setBaseCurrency("GBP");
        trade.setQuoteCurrency("USD");
        trade.setNotionalAmount(new BigDecimal("3000000"));
        trade.setForwardRate(new BigDecimal("1.2750"));
        trade.setSpotRate(new BigDecimal("1.2700"));
        trade.setTradeDate(LocalDate.now());
        trade.setValueDate(LocalDate.now().plusDays(2));
        trade.setMaturityDate(LocalDate.now().plusDays(180));
        trade.setStatus(TradeStatus.CONFIRMED);
        trade.setCreatedBy("trader1");
        return trade;
    }

    private void createSwaps(List<Counterparty> counterparties) {
        List<Trade> swaps = List.of(
            createFXSwap("FX-SWAP-001", counterparties.get(0)),
            createCurrencySwap("CCY-SWAP-002", counterparties.get(1)),
            createInterestRateSwap("IRS-001", counterparties.get(2))
        );
        tradeRepository.saveAll(swaps);
    }

    private Trade createFXSwap(String reference, Counterparty counterparty) {
        Trade trade = new Trade();
        trade.setTradeReference(reference);
        trade.setCounterparty(counterparty);
        trade.setProductType(ProductType.FX_SWAP);
        trade.setSwapType(SwapType.FX_SWAP);
        trade.setBaseCurrency("USD");
        trade.setQuoteCurrency("EUR");
        trade.setNotionalAmount(new BigDecimal("10000000"));
        
        // Near leg (spot)
        trade.setNearLegDate(LocalDate.now().plusDays(2));
        trade.setNearLegRate(new BigDecimal("1.1000"));
        trade.setNearLegAmount(new BigDecimal("10000000"));
        
        // Far leg (forward)
        trade.setFarLegDate(LocalDate.now().plusDays(32));
        trade.setFarLegRate(new BigDecimal("1.1050"));
        trade.setFarLegAmount(new BigDecimal("10000000"));
        
        trade.setTradeDate(LocalDate.now());
        trade.setValueDate(LocalDate.now().plusDays(2));
        trade.setMaturityDate(LocalDate.now().plusDays(32));
        trade.setStatus(TradeStatus.CONFIRMED);
        trade.setCreatedBy("trader2");
        return trade;
    }

    private Trade createCurrencySwap(String reference, Counterparty counterparty) {
        Trade trade = new Trade();
        trade.setTradeReference(reference);
        trade.setCounterparty(counterparty);
        trade.setProductType(ProductType.CURRENCY_SWAP);
        trade.setSwapType(SwapType.CURRENCY_SWAP);
        trade.setBaseCurrency("USD");
        trade.setQuoteCurrency("EUR");
        trade.setNotionalAmount(new BigDecimal("50000000"));
        trade.setFixedRate(new BigDecimal("3.50")); // USD fixed rate
        trade.setFloatingRateIndex("EURIBOR");
        trade.setPaymentFrequency("QUARTERLY");
        trade.setTradeDate(LocalDate.now());
        trade.setValueDate(LocalDate.now().plusDays(2));
        trade.setMaturityDate(LocalDate.now().plusYears(5));
        trade.setStatus(TradeStatus.CONFIRMED);
        trade.setCreatedBy("trader1");
        return trade;
    }

    private Trade createInterestRateSwap(String reference, Counterparty counterparty) {
        Trade trade = new Trade();
        trade.setTradeReference(reference);
        trade.setCounterparty(counterparty);
        trade.setProductType(ProductType.INTEREST_RATE_SWAP);
        trade.setSwapType(SwapType.INTEREST_RATE_SWAP);
        trade.setBaseCurrency("USD");
        trade.setQuoteCurrency("USD");
        trade.setNotionalAmount(new BigDecimal("25000000"));
        trade.setFixedRate(new BigDecimal("4.25"));
        trade.setFloatingRateIndex("SOFR");
        trade.setPaymentFrequency("SEMI_ANNUAL");
        trade.setTradeDate(LocalDate.now());
        trade.setValueDate(LocalDate.now().plusDays(2));
        trade.setMaturityDate(LocalDate.now().plusYears(10));
        trade.setStatus(TradeStatus.CONFIRMED);
        trade.setCreatedBy("trader2");
        return trade;
    }
}