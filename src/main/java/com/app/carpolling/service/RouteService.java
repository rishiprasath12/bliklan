package com.app.carpolling.service;

import com.app.carpolling.dto.LocationResponseDto;
import com.app.carpolling.dto.RouteCreationRequest;
import com.app.carpolling.dto.RoutePointDto;
import com.app.carpolling.entity.Driver;
import com.app.carpolling.entity.Route;
import com.app.carpolling.entity.RoutePoint;
import com.app.carpolling.exception.BaseException;
import com.app.carpolling.exception.ErrorCode;
import com.app.carpolling.repository.RoutePointRepository;
import com.app.carpolling.repository.RouteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RouteService {
    
    private final RouteRepository routeRepository;
    private final RoutePointRepository routePointRepository;
    private final DriverService driverService;
    
    @Transactional
    public Route createRoute(RouteCreationRequest request) {
        // Get driver
        Driver driver = driverService.getDriverById(request.getDriverId());
        
        // Create route
        Route route = new Route();
        route.setDriver(driver);
        route.setRouteName(request.getRouteName());
        route.setTotalDistance(request.getTotalDistance());
        route.setEstimatedDuration(request.getEstimatedDuration());
        route.setIsActive(true);
        
        // Save route first
        Route savedRoute = routeRepository.save(route);
        
        // Create route points
        List<RoutePoint> routePoints = new ArrayList<>();
        for (RoutePointDto pointDto : request.getRoutePoints()) {
            RoutePoint routePoint = new RoutePoint();
            routePoint.setRoute(savedRoute);
            routePoint.setCity(pointDto.getCity());
            routePoint.setSubLocation(pointDto.getSubLocation());
            routePoint.setPointName(pointDto.getCity() + " - " + pointDto.getSubLocation());
            routePoint.setAddress(pointDto.getAddress());
            routePoint.setLatitude(pointDto.getLatitude());
            routePoint.setLongitude(pointDto.getLongitude());
            routePoint.setSequenceOrder(pointDto.getSequenceOrder());
            routePoint.setDistanceFromStart(pointDto.getDistanceFromStart());
            routePoint.setTimeFromStart(pointDto.getTimeFromStart());
            routePoint.setIsBoardingPoint(pointDto.getIsBoardingPoint());
            routePoint.setIsDropPoint(pointDto.getIsDropPoint());
            routePoints.add(routePoint);
        }
        
        routePointRepository.saveAll(routePoints);
        savedRoute.setRoutePoints(routePoints);
        
        return savedRoute;
    }
    
    @Transactional(readOnly = true)
    public Route getRouteById(Long routeId) {
        return routeRepository.findById(routeId)
            .orElseThrow(() -> new BaseException(ErrorCode.ROUTE_NOT_FOUND));
    }
    
    @Transactional(readOnly = true)
    public List<Route> findRoutesByBoardingAndDropPoint(String boardingPoint, String dropPoint) {
        return routeRepository.findRoutesByBoardingAndDropPoint(boardingPoint, dropPoint);
    }
    
    @Transactional(readOnly = true)
    public List<String> getAllCities() {
        return routePointRepository.findAllDistinctCities();
    }
    
    @Transactional(readOnly = true)
    public List<String> getBoardingCities() {
        return routePointRepository.findAllDistinctBoardingCities();
    }
    
    @Transactional(readOnly = true)
    public List<String> getDropCities() {
        return routePointRepository.findAllDistinctDropCities();
    }
    
    @Transactional(readOnly = true)
    public List<String> getBoardingSubLocationsByCity(String city) {
        return routePointRepository.findBoardingSubLocationsByCity(city);
    }
    
    @Transactional(readOnly = true)
    public List<String> getDropSubLocationsByCity(String city) {
        return routePointRepository.findDropSubLocationsByCity(city);
    }
    
    @Transactional(readOnly = true)
    public List<RoutePoint> getAllBoardingPoints() {
        return routePointRepository.findAllBoardingPoints();
    }
    
    @Transactional(readOnly = true)
    public List<RoutePoint> getAllDropPoints() {
        return routePointRepository.findAllDropPoints();
    }
    
    @Transactional(readOnly = true)
    public List<RoutePoint> searchBoardingPoints(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return routePointRepository.findAllBoardingPoints();
        }
        return routePointRepository.searchBoardingPoints(searchTerm.trim());
    }
    
    @Transactional(readOnly = true)
    public List<RoutePoint> searchDropPoints(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return routePointRepository.findAllDropPoints();
        }
        return routePointRepository.searchDropPoints(searchTerm.trim());
    }
    
    @Transactional(readOnly = true)
    public List<String> searchCities(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return routePointRepository.findAllDistinctCities();
        }
        return routePointRepository.searchCities(searchTerm.trim());
    }
    
    @Transactional(readOnly = true)
    public List<RoutePoint> getRoutePoints(Long routeId) {
        return routePointRepository.findByRouteIdOrderBySequenceOrderAsc(routeId);
    }

    @Transactional(readOnly = true)
    public List<LocationResponseDto> getBoardingLocationsByCity(String city) {
        return routePointRepository.findBoardingPointsByCity(city)
            .stream()
            .map(rp -> new LocationResponseDto(
                rp.getId(),
                rp.getCity(),
                rp.getSubLocation(),
                rp.getPointName(),
                rp.getAddress(),
                rp.getLatitude(),
                rp.getLongitude(),
                rp.getIsBoardingPoint(),
                rp.getIsDropPoint()
            ))
            .collect(Collectors.toList());
    }
}

