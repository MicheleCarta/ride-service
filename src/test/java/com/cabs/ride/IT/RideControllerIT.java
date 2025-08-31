package com.cabs.ride.IT;

import com.cabs.ride.dto.request.PickupRequest;
import com.cabs.ride.dto.response.RideResponse;
import com.cabs.ride.model.Ride;
import com.cabs.ride.model.RideStatus;
import com.cabs.ride.service.RideService;
import com.cabs.ride.service.VehicleAssignmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class RideControllerIT extends TestContainers {

    private static final String BASE_PATH = "/api/v1/cabs/ride";

    @Autowired
    private VehicleAssignmentService vehicleAssignmentService;

    @Autowired
    private RideService rideService;

    @BeforeEach
    public void setUp() {
        super.setUp();
        // Set up test data - create drivers and vehicles
        vehicleAssignmentService.createEntitiesCabs();
    }


    @Test
    public void createRide_WhenValidRequest_ShouldReturnCreatedRide() {
        // Given
        PickupRequest request = new PickupRequest(40.7128, -74.0060);

        // When
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<PickupRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<RideResponse> response = restTemplate.postForEntity(
                getBaseUrl() + BASE_PATH + "/",
                entity,
                RideResponse.class
        );

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(201);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().rideId()).isNotNull();
        assertThat(response.getBody().latitude()).isEqualTo(40.7128);
        assertThat(response.getBody().longitude()).isEqualTo(-74.0060);
        assertThat(response.getBody().rideStatus()).isEqualTo(RideStatus.LEAD);
    }

    @Test
    public void createRide_WhenInvalidRequest_ShouldReturnBadRequest() {
        // Given - invalid coordinates
        PickupRequest request = new PickupRequest(null, null);

        // When
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<PickupRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                getBaseUrl() + BASE_PATH + "/",
                entity,
                String.class
        );

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).contains("{\"latitude\":\"must not be null\",\"longitude\":\"must not be null\"}");
    }


    @Test
    public void getCurrentRide_WhenValidRideId_ShouldReturnRideDetails() {
        // Given - first create a ride to get a valid UUID
        PickupRequest createRequest = new PickupRequest(40.7128, -74.0060);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<PickupRequest> createEntity = new HttpEntity<>(createRequest, headers);

        ResponseEntity<RideResponse> createResponse = restTemplate.postForEntity(
                getBaseUrl() + BASE_PATH + "/",
                createEntity,
                RideResponse.class
        );

        // Then
        assertThat(createResponse.getStatusCode().value()).isEqualTo(201);
        assertThat(createResponse.getBody()).isNotNull();
        assertThat(createResponse.getBody().latitude()).isEqualTo(40.7128);
        assertThat(createResponse.getBody().longitude()).isEqualTo(-74.0060);
        assertThat(createResponse.getBody().rideStatus()).isEqualTo(RideStatus.LEAD);
    }

    @Test
    public void getCurrentRide_WhenInvalidRideId_ShouldReturnNotFound() {
        // Given - use a random UUID that doesn't exist in the database
        UUID invalidRideId = UUID.randomUUID();

        // When
        ResponseEntity<String> response = restTemplate.getForEntity(
                getBaseUrl() + BASE_PATH + "/" + invalidRideId,
                String.class
        );

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody()).contains("Ride with ID " + invalidRideId + " not found");
    }

    @Test
    public void completeRide_WhenRideNotFound_ShouldReturnNotFound() {
        // Given - use a random UUID that doesn't exist in the database
        UUID nonExistentRideId = UUID.randomUUID();

        // When
        ResponseEntity<String> response = restTemplate.exchange(
                getBaseUrl() + BASE_PATH + "/" + nonExistentRideId,
                HttpMethod.PUT,
                null,
                String.class
        );

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody()).contains("Ride with ID " + nonExistentRideId + " not found");
    }

    @Test
    public void completeRide_ShouldPublishEventAndReturnCurrentRideStatus() {
        // Given - first create a ride to get a valid UUID
        PickupRequest createRequest = new PickupRequest(40.7128, -74.0060);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<PickupRequest> createEntity = new HttpEntity<>(createRequest, headers);

        ResponseEntity<RideResponse> createResponse = restTemplate.postForEntity(
                getBaseUrl() + BASE_PATH + "/",
                createEntity,
                RideResponse.class
        );

        UUID rideId = createResponse.getBody().rideId();
        assertThat(rideId).isNotNull();
        assertThat(createResponse.getBody().rideStatus()).isEqualTo(RideStatus.LEAD);

    }

    @Test
    public void completeRideFlow_ShouldChangeStatusFromLeadToStartedToCompleted() {
        // Given - create a ride
        PickupRequest createRequest = new PickupRequest(40.7128, -74.0060);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<PickupRequest> createEntity = new HttpEntity<>(createRequest, headers);

        ResponseEntity<RideResponse> createResponse = restTemplate.postForEntity(
                getBaseUrl() + BASE_PATH + "/",
                createEntity,
                RideResponse.class
        );

        UUID rideId = createResponse.getBody().rideId();
        assertThat(rideId).isNotNull();
        assertThat(createResponse.getBody().rideStatus()).isEqualTo(RideStatus.LEAD);

        // Wait for ride status to change to STARTED (after domain event processing)
        Ride rideStarted = executeUntil(
                () -> rideService.getRide(rideId),
                ride -> ride.getRideStatus() == RideStatus.STARTED
        );
        assertThat(rideStarted.getRideStatus()).isEqualTo(RideStatus.STARTED);
        assertThat(rideStarted.getDriver()).isNotNull();
        assertThat(rideStarted.getPlate()).isNotNull();

        // When - complete the ride
        ResponseEntity<RideResponse> completeResponse = restTemplate.exchange(
                getBaseUrl() + BASE_PATH + "/" + rideId,
                HttpMethod.PUT,
                null,
                RideResponse.class
        );

//        // Then - wait for ride status to change to COMPLETED (after domain event processing)
        Ride rideCompleted = executeUntil(
                () -> rideService.getRide(rideId),
                ride -> ride.getRideStatus() == RideStatus.COMPLETED
        );
        assertThat(rideCompleted.getRideStatus()).isEqualTo(RideStatus.COMPLETED);

//        // Verify the response
        assertThat(completeResponse.getStatusCode().value()).isEqualTo(200);
        assertThat(completeResponse.getBody()).isNotNull();
        assertThat(completeResponse.getBody().rideId()).isEqualTo(rideId);
    }
}
