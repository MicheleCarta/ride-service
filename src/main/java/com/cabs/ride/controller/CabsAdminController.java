package com.cabs.ride.controller;

import com.cabs.ride.service.VehicleAssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/cabs/admin")
@RequiredArgsConstructor
public class CabsAdminController {

    private final VehicleAssignmentService vehicleAssignmentService;

    @PostMapping("/")
    public ResponseEntity<Void> createEntitiesCabs() {
        vehicleAssignmentService.createEntitiesCabs();
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

} 