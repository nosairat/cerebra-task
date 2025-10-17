package sa.cerebra.task.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;

@TestConfiguration
        (proxyBeanMethods = false)
class ContainerConfiguration {

    // 1. MySQL Container Setup
    // Uses the official MySQL container image
    @Bean
    @ServiceConnection // Automatically sets spring.datasource.url, username, password
    public MySQLContainer<?> mySQLContainer() {
        return new MySQLContainer<>(DockerImageName.parse("mysql:8.0.36"))
                .withDatabaseName("testdb")
                .withUsername("testuser")
                .withPassword("testpass");
    }

    // 2. Redis Container Setup
    // Uses a GenericContainer for services not covered by specific modules
    // The default Redis port is 6379, which is correctly handled by @ServiceConnection
    @Bean
    @ServiceConnection // Automatically sets spring.data.redis.host and port
    public GenericContainer<?> redisContainer() {
        return new GenericContainer<>(DockerImageName.parse("redis:7.2.4-alpine"))
                .withExposedPorts(6379);
    }
}