package com.app.carpolling.scheduler;

import com.app.carpolling.service.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled job to clean up expired tokens from the blacklist.
 * Runs daily at 2 AM to remove tokens that have naturally expired.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TokenCleanupScheduler {
    
    private final TokenBlacklistService tokenBlacklistService;
    
    /**
     * Runs daily at 2:00 AM to clean up expired tokens
     * Cron: "0 0 2 * * *" = At 2:00 AM every day
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void cleanupExpiredTokens() {
        log.info("Starting token blacklist cleanup...");
        tokenBlacklistService.cleanupExpiredTokens();
        log.info("Token blacklist cleanup completed");
    }
}

