package com.cabs.ride.dto;

import com.cabs.ride.model.DriverDetails;
import com.cabs.ride.model.RideStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.UUID;

@Builder
public record RideStarted(DriverDetails driver, RideStatus rideStatus, UUID rideId,  Double latitude, Double longitude) {
}
