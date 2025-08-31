package com.cabs.ride.controller;

import com.cabs.ride.dto.request.PickupRequest;
import com.cabs.ride.dto.response.RideResponse;
import com.cabs.ride.service.RideBookingSupport;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cabs/ride")
@RequiredArgsConstructor
public class RideController {

    private final RideBookingSupport rideBookingSupport;

    @PostMapping("/")
    public ResponseEntity<RideResponse> createRide(@RequestBody @Valid PickupRequest request) {
        RideResponse response = rideBookingSupport.createRide(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{ride-id}")
    public ResponseEntity<RideResponse> currentRide(@PathVariable("ride-id") UUID rideId) {
        RideResponse response = rideBookingSupport.getCurrentRide(rideId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/{ride-id}")
    public ResponseEntity<RideResponse> completeRide(@PathVariable("ride-id") UUID rideId) {
        rideBookingSupport.completeRide(rideId);
        RideResponse response = rideBookingSupport.getCurrentRide(rideId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
} 