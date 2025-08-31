package com.cabs.ride.IT;

import com.cabs.ride.RideServiceApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = RideServiceApplication.class)
@ActiveProfiles(value = "test")
@TestConfiguration(proxyBeanMethods = false)
public class BaseServiceIT {

    protected void verifyResponseFail(ResponseEntity<String> req) {
        verifyResponseStatus(req, HttpStatus.UNAUTHORIZED);
    }

    protected void verifyResponseStatus(ResponseEntity<String> req, HttpStatus expectedStatus) {
        assertThat(req.getStatusCode()).isEqualTo(expectedStatus);
    }
}
