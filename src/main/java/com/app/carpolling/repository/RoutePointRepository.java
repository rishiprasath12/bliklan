package com.app.carpolling.repository;

import com.app.carpolling.entity.RoutePoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoutePointRepository extends JpaRepository<RoutePoint, Long> {
    List<RoutePoint> findByRouteIdOrderBySequenceOrderAsc(Long routeId);
    
    // Get all distinct cities
    @Query("SELECT DISTINCT rp.city FROM RoutePoint rp ORDER BY rp.city")
    List<String> findAllDistinctCities();
    
    // Get all distinct boarding cities
    @Query("SELECT DISTINCT rp.city FROM RoutePoint rp WHERE rp.isBoardingPoint = true ORDER BY rp.city")
    List<String> findAllDistinctBoardingCities();
    
    // Get all distinct drop cities
    @Query("SELECT DISTINCT rp.city FROM RoutePoint rp WHERE rp.isDropPoint = true ORDER BY rp.city")
    List<String> findAllDistinctDropCities();
    
    // Get all sub-locations for a specific city (boarding points)
    @Query("SELECT DISTINCT rp.subLocation FROM RoutePoint rp " +
           "WHERE rp.city = :city AND rp.isBoardingPoint = true ORDER BY rp.subLocation")
    List<String> findBoardingSubLocationsByCity(@Param("city") String city);
    
    // Get all sub-locations for a specific city (drop points)
    @Query("SELECT DISTINCT rp.subLocation FROM RoutePoint rp " +
           "WHERE rp.city = :city AND rp.isDropPoint = true ORDER BY rp.subLocation")
    List<String> findDropSubLocationsByCity(@Param("city") String city);
    
    // Get complete route points with city and sub-location (boarding)
    @Query("SELECT rp FROM RoutePoint rp " +
           "WHERE rp.isBoardingPoint = true " +
           "ORDER BY rp.city, rp.subLocation")
    List<RoutePoint> findAllBoardingPoints();
    
    // Get complete route points with city and sub-location (drop)
    @Query("SELECT rp FROM RoutePoint rp " +
           "WHERE rp.isDropPoint = true " +
           "ORDER BY rp.city, rp.subLocation")
    List<RoutePoint> findAllDropPoints();
    
    // Search boarding points by city or sub-location (case-insensitive)
    @Query("SELECT DISTINCT rp FROM RoutePoint rp " +
           "WHERE rp.isBoardingPoint = true " +
           "AND (LOWER(rp.city) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(rp.subLocation) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "ORDER BY rp.city, rp.subLocation")
    List<RoutePoint> searchBoardingPoints(@Param("searchTerm") String searchTerm);
    
    // Search drop points by city or sub-location (case-insensitive)
    @Query("SELECT DISTINCT rp FROM RoutePoint rp " +
           "WHERE rp.isDropPoint = true " +
           "AND (LOWER(rp.city) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(rp.subLocation) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "ORDER BY rp.city, rp.subLocation")
    List<RoutePoint> searchDropPoints(@Param("searchTerm") String searchTerm);
    
    // Search cities by name
    @Query("SELECT DISTINCT rp.city FROM RoutePoint rp " +
           "WHERE LOWER(rp.city) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "ORDER BY rp.city")
    List<String> searchCities(@Param("searchTerm") String searchTerm);

    @Query("SELECT rp FROM RoutePoint rp WHERE rp.city = :city AND rp.isBoardingPoint = true ORDER BY rp.subLocation")
    List<RoutePoint> findBoardingPointsByCity(@Param("city") String city);
    
    // Get all boarding points for a specific route and city
    @Query("SELECT rp FROM RoutePoint rp " +
           "WHERE rp.route.id = :routeId AND rp.city = :city AND rp.isBoardingPoint = true " +
           "ORDER BY rp.sequenceOrder")
    List<RoutePoint> findBoardingPointsByRouteAndCity(@Param("routeId") Long routeId, @Param("city") String city);
    
    // Get all drop points for a specific route and city
    @Query("SELECT rp FROM RoutePoint rp " +
           "WHERE rp.route.id = :routeId AND rp.city = :city AND rp.isDropPoint = true " +
           "ORDER BY rp.sequenceOrder")
    List<RoutePoint> findDropPointsByRouteAndCity(@Param("routeId") Long routeId, @Param("city") String city);
    
    // Get all boarding points for a trip
    @Query("SELECT rp FROM RoutePoint rp " +
           "WHERE rp.route.id = :routeId AND rp.isBoardingPoint = true " +
           "ORDER BY rp.sequenceOrder")
    List<RoutePoint> findBoardingPointsByRoute(@Param("routeId") Long routeId);
    
    // Get all drop points for a trip
    @Query("SELECT rp FROM RoutePoint rp " +
           "WHERE rp.route.id = :routeId AND rp.isDropPoint = true " +
           "ORDER BY rp.sequenceOrder")
    List<RoutePoint> findDropPointsByRoute(@Param("routeId") Long routeId);
}

