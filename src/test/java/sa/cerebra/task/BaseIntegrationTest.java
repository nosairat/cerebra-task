package sa.cerebra.task;

import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@Testcontainers // Enables Testcontainers lifecycle management
@AutoConfigureMockMvc// Ensures test-specific configuration is used
public abstract class BaseIntegrationTest {

    // --- Container Configuration ---

    // Static nested class to define the container beans once per test context
    @TestConfiguration(proxyBeanMethods = false)
    static class ContainerConfiguration {


        @Container
        protected static final GenericContainer<?> REDIS_CONTAINER =
                new GenericContainer<>(DockerImageName.parse("redis:7.2.4-alpine"))
                        .withExposedPorts(6379);

        @DynamicPropertySource
        static void databaseProperties(DynamicPropertyRegistry registry) {
            registry.add("spring.data.redis.host", () -> REDIS_CONTAINER.getHost());
            registry.add("spring.data.redis.port", () -> REDIS_CONTAINER.getMappedPort(6379).toString());
        }


    }

}