package com.cabs.ride.IT;

import com.cabs.ride.repository.RideRepository;
import com.cabs.ride.service.VehicleAssignmentService;
import com.cabs.ride.service.VehicleService;
import com.cabs.ride.model.DriverDetails;
import com.cabs.ride.model.Vehicle;
import com.cabs.ride.repository.DriverDetailsRepository;
import com.cabs.ride.repository.VehicleRepository;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.client.MockMvcWebTestClient;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;
import org.wiremock.integrations.testcontainers.WireMockContainer;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public abstract class TestContainers extends BaseServiceIT {

    @Autowired
    protected RabbitTemplate rabbitTemplate;

    @Autowired
    protected WebApplicationContext context;

    protected WebTestClient webTestClient;

    @Autowired
    protected VehicleAssignmentService vehicleAssignmentService;
    
    @Autowired
    protected VehicleRepository vehicleRepository;
    
    @Autowired
    protected DriverDetailsRepository driverDetailsRepository;

    @Autowired
    protected RideRepository rideRepository;

    @LocalServerPort
    private int randomServerPort;

    protected int getServerPort() {
        return randomServerPort;
    }


    private static final int DEFAULT_CONTAINER_TIMEOUT = 30;

    @Bean
    public TestRestTemplate testRestTemplate() {
        return new TestRestTemplate(new RestTemplateBuilder().rootUri("http://localhost:" + randomServerPort));
    }

    @Autowired
    protected TestRestTemplate restTemplate;

    private static final PostgreSQLContainer<?> POSTGRES_CONTAINER = new PostgreSQLContainer<>(DockerImageName.parse("postgres:12.8-alpine"))
            .waitingFor(Wait.forSuccessfulCommand("pg_isready -d instruments_db"))
            .withStartupTimeout(Duration.of(DEFAULT_CONTAINER_TIMEOUT, ChronoUnit.SECONDS))
            .withDatabaseName("ride_db")
            .withUsername("ride")
            .withPassword("ride");

    private static final RabbitMQContainer RABBIT_MQ_CONTAINER = new RabbitMQContainer(DockerImageName.parse("rabbitmq:3.9-management"))
            .withExposedPorts(5672, 15672)
            .waitingFor(Wait.forListeningPort())
            .withStartupTimeout(Duration.of(DEFAULT_CONTAINER_TIMEOUT, ChronoUnit.SECONDS));

    private static final WireMockContainer WIRE_MOCK_CONTAINER = new WireMockContainer(WireMockContainer.OFFICIAL_IMAGE_NAME)
            .withCliArg("--global-response-templating")
            .withClasspathResourceMapping("wiremock", "/home/wiremock/", BindMode.READ_ONLY)
            .waitingFor(Wait.forListeningPort())
            .withStartupTimeout(Duration.of(DEFAULT_CONTAINER_TIMEOUT, ChronoUnit.SECONDS))
            .withoutBanner();

    static {
        POSTGRES_CONTAINER.start();
        RABBIT_MQ_CONTAINER.start();
        WIRE_MOCK_CONTAINER.start();
    }

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRES_CONTAINER::getPassword);
        registry.add("spring.rabbitmq.host", RABBIT_MQ_CONTAINER::getHost);
        registry.add("spring.rabbitmq.port", RABBIT_MQ_CONTAINER::getAmqpPort);
        registry.add("MESSAGING_HOST", RABBIT_MQ_CONTAINER::getHost);
        registry.add("MESSAGING_PORT", RABBIT_MQ_CONTAINER::getHttpPort);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
        registry.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.PostgreSQLDialect");
//        registry.add("spring.liquibase.change-log", () -> "classpath:db/changelog/db.changelog-master.yaml");
        registry.add("spring.liquibase.contexts", () -> "dev");
        WireMock.configureFor(WIRE_MOCK_CONTAINER.getHost(), WIRE_MOCK_CONTAINER.getPort());
    }

    @BeforeEach
    public void setUp() {
        resetWiremock();
        setupWebTestClient();
        // Clean up any existing test data first
        cleanupTestData();
    }
    
    @AfterEach
    public void tearDown() {
        // Clean up test data after each test
        cleanupTestData();
    }
    
    protected void cleanupTestData() {
        rideRepository.deleteAll();
        // Delete all driver details first (due to foreign key constraints)
        driverDetailsRepository.deleteAll();
        // Delete all vehicles
        vehicleRepository.deleteAll();
    }

    private void setupWebTestClient() {
        webTestClient = MockMvcWebTestClient
                .bindToApplicationContext(context)
                .configureClient()
                .baseUrl("/v1/")
                .build();
    }

    public void resetWiremock() {
        WireMock.resetAllRequests();
    }

    protected String getBaseUrl() {
        return "http://localhost:" + getServerPort();
    }

    @Bean
    Jackson2ObjectMapperBuilderCustomizer objectMapperBuilderCustomizer() {
        return jacksonObjectMapperBuilder -> jacksonObjectMapperBuilder.serializationInclusion(JsonInclude.Include.NON_NULL);
    }

    protected <T> T executeUntil(Supplier<T> supplier, Predicate<T> predicate) {
        long timeoutMillis = 5000; // configurable
        long intervalMillis = 200; // polling interval
        long start = System.currentTimeMillis();

        while (System.currentTimeMillis() - start < timeoutMillis) {
            T result = supplier.get();
            if (predicate.test(result)) {
                return result;
            }
            try {
                Thread.sleep(intervalMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Thread interrupted while waiting for condition", e);
            }
        }

        throw new IllegalStateException("Condition not met within timeout");
    }

//    protected <T> T executeUntil(Supplier<T> supplier, Predicate<T> predicate) {
//        long timeoutMillis = 5000; // configurable
//        long intervalMillis = 200; // polling interval
//        long start = System.currentTimeMillis();
//
//        while (System.currentTimeMillis() - start < timeoutMillis) {
//            T result = supplier.get();
//            if (predicate.test(result)) {
//                return result;
//            }
//            try {
//                Thread.sleep(intervalMillis);
//            } catch (InterruptedException e) {
//                Thread.currentThread().interrupt();
//                throw new IllegalStateException("Thread interrupted while waiting for condition", e);
//            }
//        }
//
//        throw new IllegalStateException("Condition not met within timeout");
//    }
}
