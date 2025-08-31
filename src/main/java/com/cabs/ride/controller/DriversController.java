package com.cabs.ride.controller;

import com.cabs.ride.dto.response.VehicleResponse;
import com.cabs.ride.service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cabs/drivers")
@RequiredArgsConstructor
public class DriversController {

    private final VehicleService vehicleService;

    @GetMapping("/")
    public ResponseEntity<List<VehicleResponse>> getNearestDriversVehicle(@RequestParam Double latitude, @RequestParam Double longitude) {
        List<VehicleResponse> response = vehicleService.getNearestDriversVehicle(latitude, longitude);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
} 