package com.app.carpolling.service;

import com.app.carpolling.dto.VehicleRegistrationRequest;
import com.app.carpolling.entity.Driver;
import com.app.carpolling.entity.Vehicle;
import com.app.carpolling.exception.BaseException;
import com.app.carpolling.exception.ErrorCode;
import com.app.carpolling.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VehicleService {
    
    private final VehicleRepository vehicleRepository;
    private final DriverService driverService;
    
    @Transactional
    public Vehicle registerVehicle(VehicleRegistrationRequest request) {
        // Check if registration number already exists
        if (vehicleRepository.existsByRegistrationNumber(request.getRegistrationNumber())) {
            throw new BaseException(ErrorCode.VEHICLE_REGISTRATION_EXISTS);
        }
        
        // Get driver
        Driver driver = driverService.getDriverById(request.getDriverId());
        
        // Create vehicle
        Vehicle vehicle = new Vehicle();
        vehicle.setDriver(driver);
        vehicle.setRegistrationNumber(request.getRegistrationNumber());
        vehicle.setBrand(request.getBrand());
        vehicle.setModel(request.getModel());
        vehicle.setColor(request.getColor());
        vehicle.setManufacturingYear(request.getManufacturingYear());
        vehicle.setVehicleType(request.getVehicleType());
        vehicle.setTotalSeats(request.getTotalSeats());
        vehicle.setPassengerSeats(request.getTotalSeats() - 1); // Excluding driver seat
        vehicle.setHasAC(request.getHasAC());
        vehicle.setFeatures(request.getFeatures());
        vehicle.setIsActive(true);
        
        return vehicleRepository.save(vehicle);
    }
    
    @Transactional(readOnly = true)
    public Vehicle getVehicleById(Long vehicleId) {
        return vehicleRepository.findById(vehicleId)
            .orElseThrow(() -> new BaseException(ErrorCode.VEHICLE_NOT_FOUND));
    }
    
    @Transactional(readOnly = true)
    public List<Vehicle> getVehiclesByDriverId(Long driverId) {
        return vehicleRepository.findByDriverIdAndIsActiveTrue(driverId);
    }
}









