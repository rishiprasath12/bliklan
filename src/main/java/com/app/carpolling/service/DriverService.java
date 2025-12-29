package com.app.carpolling.service;

import com.app.carpolling.dto.DriverRegistrationRequest;
import com.app.carpolling.entity.Driver;
import com.app.carpolling.entity.User;
import com.app.carpolling.exception.BaseException;
import com.app.carpolling.exception.ErrorCode;
import com.app.carpolling.repository.DriverRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DriverService {
    
    private final DriverRepository driverRepository;
    private final UserService userService;
    
    @Transactional
    public Driver registerDriver(DriverRegistrationRequest request) {
        // Check if driver already exists for this user
        if (driverRepository.findByUserId(request.getUserId()).isPresent()) {
            throw new BaseException(ErrorCode.DRIVER_PROFILE_ALREADY_EXISTS);
        }
        
        // Check if license number already exists
        if (driverRepository.existsByLicenseNumber(request.getLicenseNumber())) {
            throw new BaseException(ErrorCode.LICENSE_NUMBER_ALREADY_REGISTERED);
        }
        
        // Get user
        User user = userService.getUserById(request.getUserId());
        
        // Create driver
        Driver driver = new Driver();
        driver.setUser(user);
        driver.setLicenseNumber(request.getLicenseNumber());
        driver.setLicenseExpiryDate(request.getLicenseExpiryDate());
        driver.setExperienceYears(request.getExperienceYears());
        driver.setAdditionalInfo(request.getAdditionalInfo());
        driver.setIsVerified(false);
        driver.setRating(0.0);
        driver.setTotalTrips(0);
        
        return driverRepository.save(driver);
    }
    
    @Transactional(readOnly = true)
    public Driver getDriverById(Long driverId) {
        return driverRepository.findById(driverId)
            .orElseThrow(() -> new BaseException(ErrorCode.DRIVER_NOT_FOUND));
    }
    
    @Transactional(readOnly = true)
    public Driver getDriverByUserId(Long userId) {
        return driverRepository.findByUserId(userId)
            .orElseThrow(() -> new BaseException(ErrorCode.DRIVER_NOT_FOUND, "Driver profile not found for this user"));
    }
}









