package com.mellinnial.plance.config;

import com.mellinnial.plance.entity.RoleEntity;
import com.mellinnial.plance.entity.ProjectEntity;
import com.mellinnial.plance.entity.TaskEntity;
import com.mellinnial.plance.entity.TaskPriority;
import com.mellinnial.plance.entity.TaskStatus;
import com.mellinnial.plance.entity.enums.RoleType;
import com.mellinnial.plance.repository.RoleRepository;
import com.mellinnial.plance.repository.ProjectRepository;
import com.mellinnial.plance.repository.TaskRepository;
import com.mellinnial.plance.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import org.springframework.transaction.annotation.Transactional;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Starting database seeding process...");
        seedRoles();
        fixDatabaseSchema();
        seedTasksForProjects();
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

    private void seedTasksForProjects() {
        var projects = projectRepository.findAll();
        var adminUser = userRepository.findAll().stream()
                .filter(u -> u.getRole().getName() == RoleType.ROLE_ADMIN)
                .findFirst()
                .orElse(null);

        if (adminUser == null) {
            log.warn("Cannot seed tasks because no Admin user was found.");
            return;
        }

        for (var project : projects) {
            long taskCount = taskRepository.countByProject(project);
            if (taskCount == 0) {
                log.info("Seeding 5 tasks for project: {}", project.getName());
                var employees = List.copyOf(project.getAssignedEmployees());
                
                for (int i = 1; i <= 5; i++) {
                    TaskPriority priority = TaskPriority.values()[i % TaskPriority.values().length];
                    TaskStatus status = TaskStatus.values()[i % TaskStatus.values().length];
                    
                    var builder = TaskEntity.builder()
                            .name("Seeded Task " + i + " for " + project.getName())
                            .description("Description for seeded task " + i + " in " + project.getName())
                            .priority(priority)
                            .status(status)
                            .deadline(java.time.LocalDate.now().plusDays(i * 3))
                            .estimatedHours((double) (i * 4))
                            .project(project)
                            .createdBy(adminUser);
                            
                    if (!employees.isEmpty()) {
                        builder.employee(employees.get(i % employees.size()));
                    }
                    
                    taskRepository.save(builder.build());
                }
            }
        }
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
