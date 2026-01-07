package com.footbooking.api.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class DatabaseConstraintFixer {

    @Bean
    public CommandLineRunner fixStatusConstraint(JdbcTemplate jdbcTemplate) {
        return args -> {
            try {
                log.info("Attempting to fix booking_requests_status_check constraint...");
                // On supprime l'ancienne contrainte restrictive
                jdbcTemplate.execute(
                        "ALTER TABLE booking_requests DROP CONSTRAINT IF EXISTS booking_requests_status_check");

                // Optionnel : On peut la recréer avec la nouvelle valeur, ou laisser Hibernate
                // s'en charger au prochain ddl-auto
                // Pour être sûr, on la recrée manuellement avec ANNULEE
                jdbcTemplate.execute("ALTER TABLE booking_requests ADD CONSTRAINT booking_requests_status_check " +
                        "CHECK (status IN ('EN_ATTENTE_PAIEMENT', 'EN_ATTENTE_VALIDATION_ADMIN', 'APPROUVEE', 'REJETEE', 'EXPIREE', 'ANNULEE'))");

                log.info("Constraint booking_requests_status_check updated successfully to include ANNULEE.");
            } catch (Exception e) {
                log.error("Failed to update constraint: " + e.getMessage());
                // On ne bloque pas le démarrage car la contrainte n'existe peut-être pas ou
                // autre
            }
        };
    }
}
