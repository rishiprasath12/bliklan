package com.app.carpolling.service;

import com.app.carpolling.dto.*;
import com.app.carpolling.entity.Driver;
import com.app.carpolling.entity.Route;
import com.app.carpolling.entity.RoutePoint;
import com.app.carpolling.entity.RoutePrice;
import com.app.carpolling.exception.BaseException;
import com.app.carpolling.exception.ErrorCode;
import com.app.carpolling.repository.RoutePointRepository;
import com.app.carpolling.repository.RoutePriceRepository;
import com.app.carpolling.repository.RouteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RouteService {
    
    private final RouteRepository routeRepository;
    private final RoutePointRepository routePointRepository;
    private final RoutePriceRepository routePriceRepository;
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
        
        // Create route points from city-based structure
        List<RoutePoint> routePoints = new ArrayList<>();
        int globalSequenceOrder = 1;
        
        // Sort cities by sequence order
        List<CityRouteDto> sortedCities = request.getCities().stream()
            .sorted((c1, c2) -> c1.getSequenceOrder().compareTo(c2.getSequenceOrder()))
            .collect(Collectors.toList());
        
        for (CityRouteDto cityDto : sortedCities) {
            // Sort points by distance from start within each city
            List<StopPointDto> sortedPoints = cityDto.getPoints().stream()
                .sorted((p1, p2) -> p1.getDistanceFromStart().compareTo(p2.getDistanceFromStart()))
                .collect(Collectors.toList());
            
            for (StopPointDto pointDto : sortedPoints) {
                RoutePoint routePoint = new RoutePoint();
                routePoint.setRoute(savedRoute);
                routePoint.setCity(cityDto.getCity());
                routePoint.setSubLocation(pointDto.getSubLocation());
                routePoint.setPointName(cityDto.getCity() + " - " + pointDto.getSubLocation());
                routePoint.setAddress(pointDto.getAddress());
                routePoint.setLatitude(pointDto.getLatitude());
                routePoint.setLongitude(pointDto.getLongitude());
                routePoint.setSequenceOrder(globalSequenceOrder++);
                routePoint.setDistanceFromStart(pointDto.getDistanceFromStart());
                routePoint.setTimeFromStart(pointDto.getTimeFromStart());
                // Apply city-level boarding/drop flags to all points in this city
                routePoint.setIsBoardingPoint(cityDto.getIsBoardingPoint());
                routePoint.setIsDropPoint(cityDto.getIsDropPoint());
                routePoints.add(routePoint);
            }
        }
        
        routePointRepository.saveAll(routePoints);
        savedRoute.setRoutePoints(routePoints);
        
        return savedRoute;
    }
    
    private void createPriceMatrix(Route route, List<RoutePoint> routePoints, List<RoutePriceDto> priceDtos) {
        // Create a map of city to list of route points for easy lookup
        Map<String, List<RoutePoint>> cityPointsMap = routePoints.stream()
            .collect(Collectors.groupingBy(RoutePoint::getCity));
        
        List<RoutePrice> routePrices = new ArrayList<>();
        
        for (RoutePriceDto priceDto : priceDtos) {
            // Get all boarding points from boarding city
            List<RoutePoint> boardingPoints = cityPointsMap.get(priceDto.getBoardingCity());
            if (boardingPoints == null || boardingPoints.isEmpty()) {
                throw new BaseException(ErrorCode.BOARDING_POINT_NOT_FOUND, 
                    "No boarding points found for city: " + priceDto.getBoardingCity());
            }
            
            // Filter only boarding points
            boardingPoints = boardingPoints.stream()
                .filter(RoutePoint::getIsBoardingPoint)
                .collect(Collectors.toList());
            
            // Get all drop points from drop city
            List<RoutePoint> dropPoints = cityPointsMap.get(priceDto.getDropCity());
            if (dropPoints == null || dropPoints.isEmpty()) {
                throw new BaseException(ErrorCode.DROP_POINT_NOT_FOUND, 
                    "No drop points found for city: " + priceDto.getDropCity());
            }
            
            // Filter only drop points
            dropPoints = dropPoints.stream()
                .filter(RoutePoint::getIsDropPoint)
                .collect(Collectors.toList());
            
            // Create price entry for each boarding-drop combination
            for (RoutePoint boardingPoint : boardingPoints) {
                for (RoutePoint dropPoint : dropPoints) {
                    // Validate boarding point comes before drop point
                    if (boardingPoint.getSequenceOrder() >= dropPoint.getSequenceOrder()) {
                        throw new BaseException(ErrorCode.INVALID_ROUTE, 
                            "Boarding point must come before drop point in route sequence");
                    }
                    
                    RoutePrice routePrice = new RoutePrice();
                    routePrice.setRoute(route);
                    routePrice.setBoardingPoint(boardingPoint);
                    routePrice.setDropPoint(dropPoint);
                    routePrice.setPrice(priceDto.getPrice());
                    routePrices.add(routePrice);
                }
            }
        }
        
        routePriceRepository.saveAll(routePrices);
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
    
    @Transactional(readOnly = true)
    public List<Route> getRoutesByDriverId(Long driverId) {
        return routeRepository.findByDriverIdAndIsActiveTrue(driverId);
    }
    
    @Transactional(readOnly = true)
    public List<RoutePriceCombinationDto> getRoutePriceCombinations(Long routeId) {
        // Validate route exists
        getRouteById(routeId);
        
        List<RoutePoint> routePoints = routePointRepository.findByRouteIdOrderBySequenceOrderAsc(routeId);
        
        // Group points by city
        Map<String, List<RoutePoint>> cityPointsMap = routePoints.stream()
            .collect(Collectors.groupingBy(RoutePoint::getCity));
        
        // Get all unique boarding cities
        List<String> boardingCities = routePoints.stream()
            .filter(RoutePoint::getIsBoardingPoint)
            .map(RoutePoint::getCity)
            .distinct()
            .sorted()
            .collect(Collectors.toList());
        
        // Get all unique drop cities
        List<String> dropCities = routePoints.stream()
            .filter(RoutePoint::getIsDropPoint)
            .map(RoutePoint::getCity)
            .distinct()
            .sorted()
            .collect(Collectors.toList());
        
        // Get existing prices
        List<RoutePrice> existingPrices = routePriceRepository.findByRouteId(routeId);
        Map<String, Double> priceMap = new java.util.HashMap<>();
        
        for (RoutePrice rp : existingPrices) {
            String key = rp.getBoardingPoint().getCity() + "-" + rp.getDropPoint().getCity();
            priceMap.put(key, rp.getPrice());
        }
        
        // Generate all combinations
        List<RoutePriceCombinationDto> combinations = new ArrayList<>();
        
        for (String boardingCity : boardingCities) {
            for (String dropCity : dropCities) {
                // Get first boarding point and first drop point for distance/time calculation
                RoutePoint boardingPoint = cityPointsMap.get(boardingCity).stream()
                    .filter(RoutePoint::getIsBoardingPoint)
                    .min((p1, p2) -> p1.getSequenceOrder().compareTo(p2.getSequenceOrder()))
                    .orElse(null);
                
                RoutePoint dropPoint = cityPointsMap.get(dropCity).stream()
                    .filter(RoutePoint::getIsDropPoint)
                    .min((p1, p2) -> p1.getSequenceOrder().compareTo(p2.getSequenceOrder()))
                    .orElse(null);
                
                if (boardingPoint != null && dropPoint != null && 
                    boardingPoint.getSequenceOrder() < dropPoint.getSequenceOrder()) {
                    
                    int distance = dropPoint.getDistanceFromStart() - boardingPoint.getDistanceFromStart();
                    int duration = dropPoint.getTimeFromStart() - boardingPoint.getTimeFromStart();
                    
                    String key = boardingCity + "-" + dropCity;
                    Double price = priceMap.get(key);
                    
                    RoutePriceCombinationDto combo = new RoutePriceCombinationDto(
                        boardingCity,
                        dropCity,
                        price, // null if not set
                        distance,
                        duration
                    );
                    combinations.add(combo);
                }
            }
        }
        
        return combinations;
    }
    
    @Transactional
    public void setRoutePrices(SetRoutePricesRequest request) {
        // Validate route exists
        getRouteById(request.getRouteId());
        
        List<RoutePoint> routePoints = routePointRepository.findByRouteIdOrderBySequenceOrderAsc(request.getRouteId());
        Route route = routePoints.get(0).getRoute();
        
        // Delete existing prices for this route
        routePriceRepository.deleteByRouteId(request.getRouteId());
        
        // Create new price matrix
        createPriceMatrix(route, routePoints, request.getPrices());
    }
}

