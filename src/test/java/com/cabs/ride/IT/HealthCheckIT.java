package com.cabs.ride.IT;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

public class HealthCheckIT extends TestContainers {

    @Test
    public void readinessActuator() {
        ResponseEntity<AvailabilityResponse> liveness = restTemplate.getForEntity("/actuator/health/liveness", AvailabilityResponse.class);
        assertThat(liveness.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(liveness.getBody()).extracting(AvailabilityResponse::status).isEqualTo("UP");

        ResponseEntity<AvailabilityResponse> readiness = restTemplate.getForEntity("/actuator/health/readiness", AvailabilityResponse.class);
        assertThat(readiness.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(liveness.getBody()).extracting(AvailabilityResponse::status).isEqualTo("UP");
    }

    private record AvailabilityResponse(String status) {
    }
}
