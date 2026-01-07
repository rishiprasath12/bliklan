package com.app.carpolling.scheduler;

import com.app.carpolling.entity.Booking;
import com.app.carpolling.entity.BookingStatus;
import com.app.carpolling.repository.BookingRepository;
import com.app.carpolling.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduled job to automatically cancel expired pending bookings
 * and release their seats back to the trip inventory.
 * 
 * Runs every minute to check for expired bookings.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BookingExpirationScheduler {
    
    private final BookingRepository bookingRepository;
    private final BookingService bookingService;
    
    /**
     * Runs every minute to check and cancel expired pending bookings
     * Cron: "0 * * * * *" = At second 0 of every minute
     */
    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void cancelExpiredBookings() {
        log.info("Running booking expiration check...");
        
        LocalDateTime now = LocalDateTime.now();
        
        // Find all pending bookings that have expired
        List<Booking> expiredBookings = bookingRepository
            .findByStatusAndExpiresAtBefore(BookingStatus.PENDING, now);
        
        if (expiredBookings.isEmpty()) {
            log.debug("No expired bookings found");
            return;
        }
        
        log.info("Found {} expired bookings to cancel", expiredBookings.size());
        
        for (Booking booking : expiredBookings) {
            try {
                // Release seats
                bookingService.releaseSeats(booking);
                
                // Update booking status
                booking.setStatus(BookingStatus.CANCELLED);
                bookingRepository.save(booking);
                
                log.info("Cancelled expired booking: {} (Reference: {})", 
                    booking.getId(), booking.getBookingReference());
                    
            } catch (Exception e) {
                log.error("Error cancelling expired booking {}: {}", 
                    booking.getId(), e.getMessage(), e);
            }
        }
        
        log.info("Completed booking expiration check. Cancelled {} bookings", 
            expiredBookings.size());
    }
}

