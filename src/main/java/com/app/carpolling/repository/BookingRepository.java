package com.app.carpolling.repository;

import com.app.carpolling.entity.Booking;
import com.app.carpolling.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findByBookingReference(String bookingReference);
    List<Booking> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Booking> findByTripIdAndStatus(Long tripId, BookingStatus status);
    List<Booking> findByUserIdAndStatus(Long userId, BookingStatus status);
    long countByStatus(BookingStatus status);
    
    // Find expired pending bookings
    List<Booking> findByStatusAndExpiresAtBefore(BookingStatus status, LocalDateTime expiresAt);
}



