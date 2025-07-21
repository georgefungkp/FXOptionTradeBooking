package org.george.fxoptiontradebooking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for the FX Option Trade Booking System.
 * <p>
 * This system provides functionality for booking, managing, and processing
 * foreign exchange (FX) option trades in an investment banking environment.
 * It includes features for counterparty management, trade validation, and
 * business rule enforcement.
 * </p>
 */
/**
 * Main application class for the FX Option Trade Booking system.
 * Initializes the Spring Boot application context and all required components.
 * 
 * This application manages FX option trades, counterparties, validation, and reporting
 * for a financial institution's trading operations.
 */
@SpringBootApplication
public class FxOptionTradeBookingApplication {

    /**
     * Main entry point for the application.
     * 
     * @param args Command line arguments passed to the application
     */
    /**
     * Main entry point for the application.
     * 
     * @param args Command line arguments passed to the application
     */
    public static void main(String[] args) {
        SpringApplication.run(FxOptionTradeBookingApplication.class, args);
    }

}
