package com.app.carpolling.repository;

import com.app.carpolling.entity.InvalidatedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvalidatedTokenRepository extends JpaRepository<InvalidatedToken, Long> {
    
    // Check if a token is blacklisted
    boolean existsByToken(String token);
    
    // Find token by value
    Optional<InvalidatedToken> findByToken(String token);
    
    // Find all tokens for a specific user
    List<InvalidatedToken> findByPhoneNumber(String phoneNumber);
    
    // Find expired tokens for cleanup
    List<InvalidatedToken> findByExpiresAtBefore(LocalDateTime expiresAt);
    
    // Delete expired tokens
    void deleteByExpiresAtBefore(LocalDateTime expiresAt);
}

