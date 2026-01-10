package com.app.carpolling.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "invalidated_tokens", indexes = {
    @Index(name = "idx_invalidated_token", columnList = "token"),
    @Index(name = "idx_invalidated_expires_at", columnList = "expires_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvalidatedToken {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 500)
    private String token;
    
    @Column(nullable = false, length = 15)
    private String phoneNumber;
    
    @Column(nullable = false)
    private LocalDateTime invalidatedAt;
    
    @Column(nullable = false)
    private LocalDateTime expiresAt;
}

