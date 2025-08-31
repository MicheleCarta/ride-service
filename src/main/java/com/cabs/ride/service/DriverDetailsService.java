package com.cabs.ride.service;

import com.cabs.ride.exceptions.DriverNotFoundException;
import com.cabs.ride.model.DriverDetails;
import com.cabs.ride.model.Vehicle;
import com.cabs.ride.repository.DriverDetailsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class DriverDetailsService {
    private final DriverDetailsRepository driverDetailsRepository;

    public DriverDetails getDriverDetails(Vehicle vehicle) {
        return driverDetailsRepository.findByVehicle(vehicle)
                .orElseThrow(() -> new DriverNotFoundException("Driver with Vehicle " + vehicle.getPlate() + " not found"));
    }

    public DriverDetails getDriverById(UUID driverId) {
        return driverDetailsRepository.findById(driverId)
                .orElseThrow(() -> new DriverNotFoundException("Driver " + driverId + " not found"));
    }
}
