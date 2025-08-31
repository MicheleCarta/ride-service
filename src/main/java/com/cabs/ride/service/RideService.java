package com.cabs.ride.service;

import com.cabs.ride.exceptions.RideException;
import com.cabs.ride.model.DriverDetails;
import com.cabs.ride.model.Ride;
import com.cabs.ride.repository.RideRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class RideService {
    private final RideRepository rideRepository;

    @Transactional
    public Ride saveOrUpdate(Ride ride) {
        return rideRepository.save(ride);
    }

    public Ride getRideDetails(DriverDetails driver) {
        return rideRepository.findByDriver(driver)
                .orElseThrow(() -> new RideException("Ride with Driver " + driver.getId() + " not found"));
    }

    public Ride getRide(UUID rideId) {
        return rideRepository.findById(rideId)
                .orElseThrow(() -> new RideException("Ride with ID " + rideId + " not found"));
    }

}
