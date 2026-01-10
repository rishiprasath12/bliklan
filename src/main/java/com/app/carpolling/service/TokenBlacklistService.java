package com.app.carpolling.service;

import com.app.carpolling.entity.InvalidatedToken;
import com.app.carpolling.repository.InvalidatedTokenRepository;
import com.app.carpolling.utils.JWTUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenBlacklistService {
    
    private final InvalidatedTokenRepository invalidatedTokenRepository;
    private final JWTUtils jwtUtils;
    
    /**
     * Add a token to the blacklist
     * @param token JWT token to invalidate
     */
    @Transactional
    public void invalidateToken(String token) {
        try {
            // Check if token is already blacklisted
            if (invalidatedTokenRepository.existsByToken(token)) {
                log.debug("Token already in blacklist");
                return;
            }
            
            // Extract phone number from token
            String phoneNumber = jwtUtils.extractPhoneNumber(token);
            
            // Create invalidated token entry
            InvalidatedToken invalidatedToken = new InvalidatedToken();
            invalidatedToken.setToken(token);
            invalidatedToken.setPhoneNumber(phoneNumber);
            invalidatedToken.setInvalidatedAt(LocalDateTime.now());
            
            // Set expiration time (24 hours from now - matching JWT expiration)
            // Tokens are stored until they would naturally expire
            invalidatedToken.setExpiresAt(LocalDateTime.now().plusHours(24));
            
            invalidatedTokenRepository.save(invalidatedToken);
            
            log.info("Token invalidated for user: {}", phoneNumber);
            
        } catch (Exception e) {
            log.error("Error invalidating token: {}", e.getMessage(), e);
            // Don't throw exception - logout should still work even if blacklist fails
        }
    }
    
    /**
     * Check if a token is blacklisted
     * @param token JWT token to check
     * @return true if token is blacklisted, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean isTokenBlacklisted(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }
        
        boolean isBlacklisted = invalidatedTokenRepository.existsByToken(token);
        
        if (isBlacklisted) {
            log.debug("Token found in blacklist");
        }
        
        return isBlacklisted;
    }
    
    /**
     * Clean up expired tokens from blacklist
     * Called by scheduled job
     */
    @Transactional
    public void cleanupExpiredTokens() {
        try {
            LocalDateTime now = LocalDateTime.now();
            invalidatedTokenRepository.deleteByExpiresAtBefore(now);
            log.info("Cleaned up expired tokens from blacklist");
        } catch (Exception e) {
            log.error("Error cleaning up expired tokens: {}", e.getMessage(), e);
        }
    }
}

