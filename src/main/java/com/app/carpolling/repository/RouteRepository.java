package com.app.carpolling.repository;

import com.app.carpolling.entity.Route;
import com.app.carpolling.entity.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {
    List<Route> findByDriverAndIsActiveTrue(Driver driver);
    List<Route> findByDriverIdAndIsActiveTrue(Long driverId);
    
    @Query("SELECT DISTINCT r FROM Route r JOIN r.routePoints rp1 JOIN r.routePoints rp2 " +
           "WHERE r.isActive = true " +
           "AND rp1.city = :boardingPoint " +
           "AND rp2.city = :dropPoint " +
           "AND rp1.sequenceOrder < rp2.sequenceOrder")
    List<Route> findRoutesByBoardingAndDropPoint(
        @Param("boardingPoint") String boardingPoint,
        @Param("dropPoint") String dropPoint
    );
}









