package com.app.carpolling.service;

import com.app.carpolling.dto.AdminDashboardStats;
import com.app.carpolling.entity.*;
import com.app.carpolling.exception.BaseException;
import com.app.carpolling.exception.ErrorCode;
import com.app.carpolling.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {
    
    private final UserRepository userRepository;
    private final DriverRepository driverRepository;
    private final VehicleRepository vehicleRepository;
    private final TripRepository tripRepository;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    @Transactional(readOnly = true)
    public List<Driver> getAllDrivers() {
        return driverRepository.findAll();
    }
    
    @Transactional(readOnly = true)
    public List<Driver> getUnverifiedDrivers() {
        return driverRepository.findByIsVerifiedFalse();
    }
    
    @Transactional
    public Driver verifyDriver(Long driverId) {
        Driver driver = driverRepository.findById(driverId)
            .orElseThrow(() -> new BaseException(ErrorCode.DRIVER_NOT_FOUND));
        driver.setIsVerified(true);
        return driverRepository.save(driver);
    }
    
    @Transactional
    public Driver unverifyDriver(Long driverId) {
        Driver driver = driverRepository.findById(driverId)
            .orElseThrow(() -> new BaseException(ErrorCode.DRIVER_NOT_FOUND));
        driver.setIsVerified(false);
        return driverRepository.save(driver);
    }
    
    @Transactional
    public User activateUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));
        user.setIsActive(true);
        return userRepository.save(user);
    }
    
    @Transactional
    public User deactivateUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));
        user.setIsActive(false);
        return userRepository.save(user);
    }
    
    @Transactional(readOnly = true)
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }
    
    @Transactional(readOnly = true)
    public List<Trip> getAllTrips() {
        return tripRepository.findAll();
    }
    
    @Transactional(readOnly = true)
    public List<Vehicle> getAllVehicles() {
        return vehicleRepository.findAll();
    }
    
    @Transactional(readOnly = true)
    public AdminDashboardStats getDashboardStats() {
        long totalUsers = userRepository.count();
        long totalDrivers = driverRepository.count();
        long verifiedDrivers = driverRepository.countByIsVerifiedTrue();
        long totalVehicles = vehicleRepository.count();
        long totalTrips = tripRepository.count();
        long totalBookings = bookingRepository.count();
        long confirmedBookings = bookingRepository.countByStatus(BookingStatus.CONFIRMED);
        long pendingBookings = bookingRepository.countByStatus(BookingStatus.PENDING);
        long cancelledBookings = bookingRepository.countByStatus(BookingStatus.CANCELLED);
        
        return new AdminDashboardStats(
            totalUsers,
            totalDrivers,
            verifiedDrivers,
            totalVehicles,
            totalTrips,
            totalBookings,
            confirmedBookings,
            pendingBookings,
            cancelledBookings
        );
    }
}







