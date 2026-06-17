package com.mellinnial.plance.config;

import com.mellinnial.plance.entity.RoleEntity;
import com.mellinnial.plance.entity.enums.RoleType;
import com.mellinnial.plance.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        log.info("Starting database seeding process...");
        seedRoles();
        fixDatabaseSchema();
        log.info("Database seeding process completed successfully.");
    }

    private void seedRoles() {
        Arrays.stream(RoleType.values()).forEach(roleType -> {
            if (!roleRepository.existsByName(roleType)) {
                RoleEntity role = RoleEntity.builder()
                        .name(roleType)
                        .build();
                roleRepository.save(role);
                log.info("Seeded role: {}", roleType);
            }
        });
    }

    private void fixDatabaseSchema() {
        try {
            jdbcTemplate.execute("UPDATE users SET active = '1' WHERE active = 'true'");
            jdbcTemplate.execute("UPDATE users SET active = '0' WHERE active = 'false'");
            jdbcTemplate.execute("UPDATE users SET verified = '1' WHERE verified = 'true'");
            jdbcTemplate.execute("UPDATE users SET verified = '0' WHERE verified = 'false'");

            jdbcTemplate.execute("ALTER TABLE users MODIFY COLUMN active TINYINT(1) DEFAULT 1");
            jdbcTemplate.execute("ALTER TABLE users MODIFY COLUMN verified TINYINT(1) DEFAULT 0");
            log.info("Successfully fixed active and verified columns to boolean TINYINT types.");
        } catch (Exception e) {
            log.warn("Note: database schema update skipped or already applied: {}", e.getMessage());
        }
    }
}
