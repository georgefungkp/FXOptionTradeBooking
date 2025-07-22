package org.george.fxoptiontradebooking.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.Tracer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenTelemetry configuration for the FX Option Trade Booking System.
 * 
 * This configuration leverages Spring Boot's auto-configuration for OpenTelemetry
 * instead of manually configuring OpenTelemetry SDK components.
 */
@Configuration
public class ObservabilityConfig {

    @Value("${spring.application.name:fx-option-trade-booking}")
    private String serviceName;

    /**
     * Custom configuration for observability can be added here.
     * Spring Boot auto-configuration handles most of the OpenTelemetry setup.
     * Example of how to customize meter registry if needed
     */
    public void configureMeterRegistry(MeterRegistry registry) {
        registry.config()
                .commonTags("service", serviceName)
                .commonTags("version", "0.0.1-SNAPSHOT")
                .commonTags("environment", "development");
    }
}
