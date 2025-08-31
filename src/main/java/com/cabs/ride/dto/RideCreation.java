package com.cabs.ride.dto;

import com.cabs.ride.model.DriverDetails;
import com.cabs.ride.model.RideStatus;
import lombok.Builder;

@Builder
public record RideCreation(DriverDetails driver, RideStatus rideStatus, Double latitude, Double longitude) {
}
