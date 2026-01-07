package com.app.carpolling.repository;

import com.app.carpolling.entity.RoutePrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoutePriceRepository extends JpaRepository<RoutePrice, Long> {
    
    List<RoutePrice> findByRouteId(Long routeId);
    
    @Query("SELECT rp FROM RoutePrice rp WHERE rp.route.id = :routeId " +
           "AND rp.boardingPoint.id = :boardingPointId " +
           "AND rp.dropPoint.id = :dropPointId")
    Optional<RoutePrice> findByRouteAndPoints(
        @Param("routeId") Long routeId,
        @Param("boardingPointId") Long boardingPointId,
        @Param("dropPointId") Long dropPointId
    );
    
    void deleteByRouteId(Long routeId);
}

