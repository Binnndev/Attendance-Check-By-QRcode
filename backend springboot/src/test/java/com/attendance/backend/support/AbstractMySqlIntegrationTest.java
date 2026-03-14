package com.attendance.backend.support;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;

public abstract class AbstractMySqlIntegrationTest {

    private static final boolean USE_TESTCONTAINERS = Boolean.parseBoolean(
            System.getProperty(
                    "useTestcontainers",
                    System.getenv().getOrDefault("USE_TESTCONTAINERS", "false")
            )
    );

    private static MySQLContainer<?> mysql;

    @DynamicPropertySource
    static void registerMySqlProperties(DynamicPropertyRegistry registry) {
        if (!USE_TESTCONTAINERS) {
            return;
        }

        if (mysql == null) {
            mysql = new MySQLContainer<>("mysql:8.0.36")
                    .withDatabaseName("attendance_test")
                    .withUsername("test")
                    .withPassword("test");
            mysql.start();
        }

        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.datasource.driver-class-name", mysql::getDriverClassName);

        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.flyway.url", mysql::getJdbcUrl);
        registry.add("spring.flyway.user", mysql::getUsername);
        registry.add("spring.flyway.password", mysql::getPassword);
        registry.add("spring.flyway.locations", () -> "classpath:db/migration");

        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
    }
}