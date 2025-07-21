package org.george.fxoptiontradebooking.config;

import lombok.RequiredArgsConstructor;
import org.george.fxoptiontradebooking.entity.*;
import org.george.fxoptiontradebooking.repository.CounterpartyRepository;
import org.george.fxoptiontradebooking.repository.TradeRepository;
import org.george.fxoptiontradebooking.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * Initializes sample data for the application on startup.
 * Creates default users, counterparties, and sample trades for testing and demo purposes.
 * This component is executed when the application starts up via Spring's CommandLineRunner.
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TradeRepository tradeRepository;

    private final CounterpartyRepository counterpartyRepository;

    /**
     * Executes the data initialization process on application startup.
     * Creates sample users, counterparties, and trades if none exist in the database.
     *
     * @param args Command line arguments passed to the application
     * @throws Exception if initialization fails
     */
    @Override
    public void run(String... args) throws Exception {
        createSampleUsers();
        createSampleCounterparties();
        createSampleTrades();
    }

    private void createSampleUsers() {
        if (userRepository.count() == 0) {
            // Create admin user
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setEmail("admin@fxtrading.com");
            admin.setEnabled(true);
            admin.setRoles(Set.of(Role.ADMIN, Role.TRADER));
            userRepository.save(admin);

            // Create regular user
            User trader = new User();
            trader.setUsername("trader");
            trader.setPassword(passwordEncoder.encode("trader123"));
            trader.setEmail("trader@fxtrading.com");
            trader.setEnabled(true);
            trader.setRoles(Set.of(Role.TRADER, Role.USER));
            userRepository.save(trader);

            System.out.println("Default users created:");
            System.out.println("Admin: admin/admin123");
            System.out.println("Trader: trader/trader123");
        }
    }

    private void createSampleCounterparties() {
        if (counterpartyRepository.count() == 0) {
            Counterparty goldmanSachs = new Counterparty();
            goldmanSachs.setCounterpartyCode("CP001");
            goldmanSachs.setName("Goldman Sachs");
            goldmanSachs.setLeiCode("7LTWFZYICNSX8D621K86");
            goldmanSachs.setSwiftCode("GSCCUS33");
            goldmanSachs.setCreditRating("A+");
            goldmanSachs.setIsActive(true);

            Counterparty jpMorgan = new Counterparty();
            jpMorgan.setCounterpartyCode("CP002");
            jpMorgan.setName("JPMorgan Chase");
            jpMorgan.setLeiCode("8I5DZWZKVSZI1NUEF608");
            jpMorgan.setSwiftCode("CHASUS33");
            jpMorgan.setCreditRating("AA-");
            jpMorgan.setIsActive(true);

            Counterparty citigroup = new Counterparty();
            citigroup.setCounterpartyCode("CP003");
            citigroup.setName("Citigroup Inc");
            citigroup.setLeiCode("6SHGI4ZSSLCXXQSBB395");
            citigroup.setSwiftCode("CITIUS33");
            citigroup.setCreditRating("A");
            citigroup.setIsActive(true);

            Counterparty barclay = new Counterparty();
            barclay.setCounterpartyCode("CP004");
            barclay.setName("Barclays Bank");
            barclay.setLeiCode("G5GSEF7VJP5I7OUK5573");
            barclay.setSwiftCode("BARCGB22");
            barclay.setCreditRating("A-");
            barclay.setIsActive(true);

            counterpartyRepository.saveAll(List.of(goldmanSachs, jpMorgan, citigroup, barclay));
            System.out.println("Sample counterparties created successfully!");
        }
    }

    private void createSampleTrades() {
        if (tradeRepository.count() == 0) {
            List<Counterparty> counterparties = counterpartyRepository.findAll();

            if (!counterparties.isEmpty()) {
                LocalDate today = LocalDate.now();
                LocalDate yesterday = today.minusDays(1);
                LocalDate lastWeek = today.minusDays(7);

                // Trade 1: EUR/USD Call Option with Goldman Sachs
                Trade eurUsdCall = new Trade();
                eurUsdCall.setTradeReference("TRD-2024-001");
                eurUsdCall.setCounterparty(counterparties.get(0)); // Goldman Sachs
                eurUsdCall.setBaseCurrency("EUR");
                eurUsdCall.setQuoteCurrency("USD");
                eurUsdCall.setNotionalAmount(new BigDecimal("1000000.00")); // 1M EUR
                eurUsdCall.setStrikePrice(new BigDecimal("1.0850"));
                eurUsdCall.setSpotRate(new BigDecimal("1.0820"));
                eurUsdCall.setTradeDate(yesterday);
                eurUsdCall.setValueDate(yesterday.plusDays(2));
                eurUsdCall.setMaturityDate(yesterday.plusDays(30));
                eurUsdCall.setOptionType(OptionType.CALL);
                eurUsdCall.setStatus(TradeStatus.CONFIRMED);
                eurUsdCall.setPremiumAmount(new BigDecimal("15000.00"));
                eurUsdCall.setPremiumCurrency("USD");
                eurUsdCall.setCreatedAt(LocalDateTime.now().minusDays(1));
                eurUsdCall.setCreatedBy("trader");

                // Trade 2: GBP/USD Put Option with JPMorgan
                Trade gbpUsdPut = new Trade();
                gbpUsdPut.setTradeReference("TRD-2024-002");
                gbpUsdPut.setCounterparty(counterparties.get(1)); // JPMorgan
                gbpUsdPut.setBaseCurrency("GBP");
                gbpUsdPut.setQuoteCurrency("USD");
                gbpUsdPut.setNotionalAmount(new BigDecimal("750000.00")); // 750K GBP
                gbpUsdPut.setStrikePrice(new BigDecimal("1.2650"));
                gbpUsdPut.setSpotRate(new BigDecimal("1.2680"));
                gbpUsdPut.setTradeDate(lastWeek);
                gbpUsdPut.setValueDate(lastWeek.plusDays(2));
                gbpUsdPut.setMaturityDate(lastWeek.plusDays(60));
                gbpUsdPut.setOptionType(OptionType.PUT);
                gbpUsdPut.setStatus(TradeStatus.SETTLED);
                gbpUsdPut.setPremiumAmount(new BigDecimal("22500.00"));
                gbpUsdPut.setPremiumCurrency("USD");
                gbpUsdPut.setCreatedAt(LocalDateTime.now().minusDays(7));
                gbpUsdPut.setCreatedBy("admin");

                // Trade 3: USD/JPY Call Option with Citigroup (Pending)
                Trade usdJpyCall = new Trade();
                usdJpyCall.setTradeReference("TRD-2024-003");
                usdJpyCall.setCounterparty(counterparties.get(2)); // Citigroup
                usdJpyCall.setBaseCurrency("USD");
                usdJpyCall.setQuoteCurrency("JPY");
                usdJpyCall.setNotionalAmount(new BigDecimal("500000.00")); // 500K USD
                usdJpyCall.setStrikePrice(new BigDecimal("150.50"));
                usdJpyCall.setSpotRate(new BigDecimal("149.85"));
                usdJpyCall.setTradeDate(today);
                usdJpyCall.setValueDate(today.plusDays(2));
                usdJpyCall.setMaturityDate(today.plusDays(45));
                usdJpyCall.setOptionType(OptionType.CALL);
                usdJpyCall.setStatus(TradeStatus.PENDING);
                usdJpyCall.setPremiumAmount(new BigDecimal("12000.00"));
                usdJpyCall.setPremiumCurrency("USD");
                usdJpyCall.setCreatedAt(LocalDateTime.now());
                usdJpyCall.setCreatedBy("trader");

                // Trade 4: EUR/GBP Put Option with Barclays
                Trade eurGbpPut = new Trade();
                eurGbpPut.setTradeReference("TRD-2024-004");
                eurGbpPut.setCounterparty(counterparties.get(3)); // Barclays
                eurGbpPut.setBaseCurrency("EUR");
                eurGbpPut.setQuoteCurrency("GBP");
                eurGbpPut.setNotionalAmount(new BigDecimal("2000000.00")); // 2M EUR
                eurGbpPut.setStrikePrice(new BigDecimal("0.8550"));
                eurGbpPut.setSpotRate(new BigDecimal("0.8575"));
                eurGbpPut.setTradeDate(today.minusDays(3));
                eurGbpPut.setValueDate(today.minusDays(1));
                eurGbpPut.setMaturityDate(today.plusDays(90));
                eurGbpPut.setOptionType(OptionType.PUT);
                eurGbpPut.setStatus(TradeStatus.CONFIRMED);
                eurGbpPut.setPremiumAmount(new BigDecimal("35000.00"));
                eurGbpPut.setPremiumCurrency("EUR");
                eurGbpPut.setCreatedAt(LocalDateTime.now().minusDays(3));
                eurGbpPut.setCreatedBy("admin");

                // Trade 5: USD/CHF Call Option with Goldman Sachs (Large Trade)
                Trade usdChfCall = new Trade();
                usdChfCall.setTradeReference("TRD-2024-005");
                usdChfCall.setCounterparty(counterparties.get(0)); // Goldman Sachs
                usdChfCall.setBaseCurrency("USD");
                usdChfCall.setQuoteCurrency("CHF");
                usdChfCall.setNotionalAmount(new BigDecimal("5000000.00")); // 5M USD
                usdChfCall.setStrikePrice(new BigDecimal("0.9200"));
                usdChfCall.setSpotRate(new BigDecimal("0.9180"));
                usdChfCall.setTradeDate(today.minusDays(2));
                usdChfCall.setValueDate(today);
                usdChfCall.setMaturityDate(today.plusDays(180));
                usdChfCall.setOptionType(OptionType.CALL);
                usdChfCall.setStatus(TradeStatus.CONFIRMED);
                usdChfCall.setPremiumAmount(new BigDecimal("75000.00"));
                usdChfCall.setPremiumCurrency("USD");
                usdChfCall.setCreatedAt(LocalDateTime.now().minusDays(2));
                usdChfCall.setCreatedBy("trader");

                // Trade 6: AUD/USD Put Option with JPMorgan (Recent)
                Trade audUsdPut = new Trade();
                audUsdPut.setTradeReference("TRD-2024-006");
                audUsdPut.setCounterparty(counterparties.get(1)); // JPMorgan
                audUsdPut.setBaseCurrency("AUD");
                audUsdPut.setQuoteCurrency("USD");
                audUsdPut.setNotionalAmount(new BigDecimal("800000.00")); // 800K AUD
                audUsdPut.setStrikePrice(new BigDecimal("0.6450"));
                audUsdPut.setSpotRate(new BigDecimal("0.6475"));
                audUsdPut.setTradeDate(today);
                audUsdPut.setValueDate(today.plusDays(2));
                audUsdPut.setMaturityDate(today.plusDays(120));
                audUsdPut.setOptionType(OptionType.PUT);
                audUsdPut.setStatus(TradeStatus.PENDING);
                audUsdPut.setPremiumAmount(new BigDecimal("18000.00"));
                audUsdPut.setPremiumCurrency("USD");
                audUsdPut.setCreatedAt(LocalDateTime.now().minusHours(2));
                audUsdPut.setCreatedBy("admin");

                // Trade 7: EUR/CHF Call Option with Citigroup (Cancelled)
                Trade eurChfCall = new Trade();
                eurChfCall.setTradeReference("TRD-2024-007");
                eurChfCall.setCounterparty(counterparties.get(2)); // Citigroup
                eurChfCall.setBaseCurrency("EUR");
                eurChfCall.setQuoteCurrency("CHF");
                eurChfCall.setNotionalAmount(new BigDecimal("300000.00")); // 300K EUR
                eurChfCall.setStrikePrice(new BigDecimal("0.9850"));
                eurChfCall.setSpotRate(new BigDecimal("0.9825"));
                eurChfCall.setTradeDate(today.minusDays(5));
                eurChfCall.setValueDate(today.minusDays(3));
                eurChfCall.setMaturityDate(today.plusDays(30));
                eurChfCall.setOptionType(OptionType.CALL);
                eurChfCall.setStatus(TradeStatus.CANCELLED);
                eurChfCall.setPremiumAmount(new BigDecimal("8500.00"));
                eurChfCall.setPremiumCurrency("EUR");
                eurChfCall.setCreatedAt(LocalDateTime.now().minusDays(5));
                eurChfCall.setCreatedBy("trader");

                tradeRepository.saveAll(List.of(
                    eurUsdCall, gbpUsdPut, usdJpyCall, eurGbpPut,
                    usdChfCall, audUsdPut, eurChfCall
                ));

                System.out.println("Sample trades created successfully!");
                System.out.println("- 7 sample FX option trades created");
                System.out.println("- Various currency pairs: EUR/USD, GBP/USD, USD/JPY, EUR/GBP, USD/CHF, AUD/USD, EUR/CHF");
                System.out.println("- Different statuses: PENDING, CONFIRMED, SETTLED, CANCELLED");
                System.out.println("- Both CALL and PUT options");
                System.out.println("- Different notional amounts and maturities");
            }
        }
    }
}