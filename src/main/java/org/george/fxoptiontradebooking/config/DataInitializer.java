package org.george.fxoptiontradebooking.config;

import lombok.RequiredArgsConstructor;
import org.george.fxoptiontradebooking.entity.Counterparty;
import org.george.fxoptiontradebooking.entity.Role;
import org.george.fxoptiontradebooking.entity.User;
import org.george.fxoptiontradebooking.repository.CounterpartyRepository;
import org.george.fxoptiontradebooking.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Initializes sample data for the application on startup.
 * Creates default counterparties for testing and development purposes.
 * This component is executed when the application starts up via Spring's CommandLineRunner.
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final CounterpartyRepository counterpartyRepository;


    /**
     * Executes the data initialization process on application startup.
     * Creates sample counterparties if none exist in the database.
     *
     * @param args Command line arguments passed to the application
     * @throws Exception if initialization fails
     */
    @Override
    public void run(String... args) throws Exception {
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
        // Create sample counterparties
        if (counterpartyRepository.count() == 0) {
            Counterparty counterparty1 = new Counterparty();
            counterparty1.setCounterpartyCode("CP001");
            counterparty1.setName("Goldman Sachs");
            counterparty1.setLeiCode("7LTWFZYICNSX8D621K86");
            counterparty1.setSwiftCode("GSCCUS33");
            counterparty1.setCreditRating("A+");
            counterparty1.setIsActive(true);

            Counterparty counterparty2 = new Counterparty();
            counterparty2.setCounterpartyCode("CP002");
            counterparty2.setName("JPMorgan Chase");
            counterparty2.setLeiCode("8I5DZWZKVSZI1NUEF608");
            counterparty2.setSwiftCode("CHASUS33");
            counterparty2.setCreditRating("AA-");
            counterparty2.setIsActive(true);

            counterpartyRepository.save(counterparty1);
            counterpartyRepository.save(counterparty2);

            System.out.println("Sample counterparties created successfully!");
        }
    }
}
