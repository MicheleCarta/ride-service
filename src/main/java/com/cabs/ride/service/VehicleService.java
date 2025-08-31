package com.cabs.ride.service;

import com.cabs.ride.dto.response.VehicleResponse;
import com.cabs.ride.exceptions.DriverNotFoundException;
import com.cabs.ride.exceptions.VehicleNotFoundException;
import com.cabs.ride.model.Vehicle;
import com.cabs.ride.repository.VehicleRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@RequiredArgsConstructor
@Service
public class VehicleService {

    private static final Logger logger = LoggerFactory.getLogger(VehicleService.class);

    private final VehicleRepository vehicleRepository;

    @Transactional
    public void saveOrUpdate(Vehicle vehicle) {
        vehicleRepository.save(vehicle);
    }

    public Vehicle getVehicleByPlate(String plate) {
        return vehicleRepository.findByPlate(plate)
                .orElseThrow(() -> new VehicleNotFoundException("Vehicle with ID " + plate + " not found"));
    }

    public boolean hasAvailableDrivers() {
        return !vehicleRepository.findByAvailableTrue().isEmpty();
    }

    public Vehicle allocateNearestDriver(double latitude, double longitude) {
        List<Vehicle> nearest = getNearestVehicles(latitude, longitude);

        if (nearest.isEmpty()) {
            throw new DriverNotFoundException("Not Driver Available at the moment");
        }

        Vehicle vehicle = nearest.getFirst();
        vehicle.setAvailable(false);
        Vehicle vehicleReserved = vehicleRepository.save(vehicle);
        logger.info("reserved ride with Vehicle {} ", vehicleReserved.getId());
        return vehicle;
    }

    public List<VehicleResponse> getNearestDriversVehicle(double latitude, double longitude) {
        return getNearestVehicles(latitude, longitude).stream()
                .map(v -> VehicleResponse.builder()
                        .plate(v.getPlate())
                        .longitude(v.getLongitude())
                        .latitude(v.getLatitude())
                        .build())
                .toList();
    }

    private List<Vehicle> getNearestVehicles(double latitude, double longitude) {
        return vehicleRepository.findAll().stream()
                .sorted(Comparator.comparingDouble(v ->
                        euclideanDistanceSquared(latitude, longitude,
                                v.getLatitude(), v.getLongitude()))).toList();
    }

    private double euclideanDistanceSquared(double lat1, double lon1, double lat2, double lon2) {
        double dx = lat2 - lat1;
        double dy = lon2 - lon1;
        return dx * dx + dy * dy;
    }
}
