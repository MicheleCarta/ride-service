package com.cabs.ride.IT;

import com.cabs.ride.dto.response.VehicleResponse;
import com.cabs.ride.service.VehicleAssignmentService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class DriverControllerIT extends TestContainers {

    private static final String BASE_PATH = "/api/v1/cabs/drivers";

    @Autowired
    private VehicleAssignmentService vehicleAssignmentService;

    @BeforeEach
    public void setUp() {
        super.setUp();
        // Clean up any existing test data first
        cleanupTestData();
        // Set up test data - create drivers and vehicles
        vehicleAssignmentService.createEntitiesCabs();
    }

    @AfterEach
    public void tearDown() {
        // Clean up test data after each test
        cleanupTestData();
    }

    @Test
    public void getNearestDriversVehicle_WhenValidCoordinates_ShouldReturnOrderedByDistance() {
        // Given - coordinates in the middle of the coordinate range
        Double latitude = 0.0;
        Double longitude = 0.0;

        // When
        ResponseEntity<List<VehicleResponse>> response = restTemplate.exchange(
                getBaseUrl() + BASE_PATH + "/?latitude=" + latitude + "&longitude=" + longitude,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<VehicleResponse>>() {
                }
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        List<VehicleResponse> vehicles = response.getBody();
        assertThat(vehicles).hasSize(10);

        // Verify vehicles are ordered by distance (nearest first)
        for (int i = 0; i < vehicles.size() - 1; i++) {
            VehicleResponse current = vehicles.get(i);
            VehicleResponse next = vehicles.get(i + 1);

            double currentDistance = calculateDistanceSquared(0.0, 0.0, current.latitude(), current.longitude());
            double nextDistance = calculateDistanceSquared(0.0, 0.0, next.latitude(), next.longitude());

            assertThat(currentDistance).isLessThanOrEqualTo(nextDistance);
        }
    }

    @Test
    public void getNearestDriversVehicle_WhenMissingLatitude_ShouldReturnBadRequest() {
        // Given - missing latitude parameter
        Double longitude = -74.0060;

        // When
        ResponseEntity<String> response = restTemplate.getForEntity(
                getBaseUrl() + BASE_PATH + "/?longitude=" + longitude,
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void getNearestDriversVehicle_WhenMissingLongitude_ShouldReturnBadRequest() {
        // Given - missing longitude parameter
        Double latitude = 40.7128;

        // When
        ResponseEntity<String> response = restTemplate.getForEntity(
                getBaseUrl() + BASE_PATH + "/?latitude=" + latitude,
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void getNearestDriversVehicle_WhenNullCoordinates_ShouldReturnBadRequest() {
        // Given - null coordinates
        String latitude = null;
        String longitude = null;

        // When
        ResponseEntity<String> response = restTemplate.getForEntity(
                getBaseUrl() + BASE_PATH + "/?latitude=" + latitude + "&longitude=" + longitude,
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void getNearestDriversVehicle_WhenZeroCoordinates_ShouldReturnDrivers() {
        // Given - zero coordinates
        Double latitude = 0.0;
        Double longitude = 0.0;

        // When
        ResponseEntity<List<VehicleResponse>> response = restTemplate.getForEntity(
                getBaseUrl() + BASE_PATH + "/?latitude=" + latitude + "&longitude=" + longitude,
                (Class<List<VehicleResponse>>) (Class<?>) List.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(10);
    }

    @Test
    public void getNearestDriversVehicle_WhenNegativeCoordinates_ShouldReturnDrivers() {
        // Given - negative coordinates
        Double latitude = -40.7128;
        Double longitude = -74.0060;

        // When
        ResponseEntity<List<VehicleResponse>> response = restTemplate.getForEntity(
                getBaseUrl() + BASE_PATH + "/?latitude=" + latitude + "&longitude=" + longitude,
                (Class<List<VehicleResponse>>) (Class<?>) List.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(10);
    }

    /**
     * Helper method to calculate squared distance between two points
     * This matches the calculation used in VehicleService
     */
    private double calculateDistanceSquared(double lat1, double lon1, double lat2, double lon2) {
        double dx = lat2 - lat1;
        double dy = lon2 - lon1;
        return dx * dx + dy * dy;
    }
}
