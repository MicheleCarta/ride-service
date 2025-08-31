package com.cabs.ride.service;

import com.cabs.ride.model.DriverDetails;
import com.cabs.ride.model.Vehicle;
import com.cabs.ride.repository.DriverDetailsRepository;
import com.cabs.ride.repository.VehicleRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.UUID;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class VehicleAssignmentService {
    private static final Logger logger = LoggerFactory.getLogger(VehicleAssignmentService.class);

    private final DriverDetailsRepository driverRepository;
    private final VehicleRepository vehicleRepository;

    @Transactional
    public void createEntitiesCabs() {
        Random random = new Random();

        IntStream.range(0, 10).forEach(i -> {
            Vehicle vehicle = new Vehicle();
            vehicle.setLongitude(-180 + 360 * random.nextDouble());
            vehicle.setLatitude(-90 + 180 * random.nextDouble());
            vehicle.setAvailable(true);
            vehicle.setPlate(UUID.randomUUID().toString().substring(0, 5));

            vehicleRepository.save(vehicle);

            DriverDetails driver = new DriverDetails();
            driver.setVehicle(vehicle);

            driverRepository.save(driver);
        });

        logger.info("✅ Initialized 10 random drivers with vehicles");
    }
}