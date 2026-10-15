package com.waterbill.config;

import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FlywayConfig {

    private static final Logger log = LoggerFactory.getLogger(FlywayConfig.class);

    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        return flyway -> {
            try {
                log.info("Executing Flyway repair to clean any failed schema history entries...");
                flyway.repair();
            } catch (Exception ex) {
                log.warn("Flyway repair completed with notice: {}", ex.getMessage());
            }

            log.info("Executing Flyway migration...");
            flyway.migrate();
            log.info("Flyway migration completed successfully.");
        };
    }
}
