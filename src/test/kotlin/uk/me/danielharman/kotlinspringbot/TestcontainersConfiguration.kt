package uk.me.danielharman.kotlinspringbot

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.testcontainers.mongodb.MongoDBContainer
import org.testcontainers.utility.DockerImageName

/**
 * Starts MongoDB in Docker for tests that load the Spring context. Import it with
 * `@Import(TestcontainersConfiguration::class)`; Spring Boot wires the connection automatically.
 */
@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {
    // Keep in step with the mongo image in docker-compose.yaml
    @Bean
    @ServiceConnection
    fun mongoDbContainer(): MongoDBContainer = MongoDBContainer(DockerImageName.parse("mongo:8.2.5"))
}
