package com.app.carpolling.repository;

import com.app.carpolling.entity.Trip;
import com.app.carpolling.entity.TripStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {
    List<Trip> findByDriverIdAndStatus(Long driverId, TripStatus status);
    
    List<Trip> findByDriverIdOrderByDepartureTimeDesc(Long driverId);
    
    @Query("SELECT t FROM Trip t WHERE t.route.id IN :routeIds " +
           "AND t.departureTime >= :fromDate " +
           "AND t.departureTime <= :toDate " +
           "AND t.availableSeats > 0 " +
           "AND t.status = 'SCHEDULED' " +
           "ORDER BY t.departureTime ASC")
    List<Trip> findAvailableTrips(
        @Param("routeIds") List<Long> routeIds,
        @Param("fromDate") LocalDateTime fromDate,
        @Param("toDate") LocalDateTime toDate
    );
    
    List<Trip> findByStatusAndDepartureTimeBefore(TripStatus status, LocalDateTime dateTime);

    Optional<Trip> findByIdAndDriver_Id(Long tripId, Long driverId);
}









