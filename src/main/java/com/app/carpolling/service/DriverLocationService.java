package com.app.carpolling.service;

import com.app.carpolling.dto.DriverLocationDto;
import com.app.carpolling.dto.DriverLocationResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class DriverLocationService {

    private static final Logger logger = LoggerFactory.getLogger(DriverLocationService.class);
    private static final String REDIS_KEY_PREFIX = "driver:location:";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final RedisTemplate<String, Object> redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    private final long locationTtlSeconds;

    public DriverLocationService(RedisTemplate<String, Object> redisTemplate,
                                 SimpMessagingTemplate messagingTemplate,
                                 @Value("${driver.location.redis.ttl:300}") long locationTtlSeconds) {
        this.redisTemplate = redisTemplate;
        this.messagingTemplate = messagingTemplate;
        this.locationTtlSeconds = locationTtlSeconds;
    }

    /**
     * Store driver location in Redis and broadcast to subscribed customers.
     */
    public void updateAndBroadcastLocation(DriverLocationDto dto) {
        String key = REDIS_KEY_PREFIX + dto.getTripId();
        long timestamp = dto.getTimestamp() != null ? dto.getTimestamp() : System.currentTimeMillis();

        DriverLocationResponseDto response = new DriverLocationResponseDto(
                dto.getLatitude(),
                dto.getLongitude(),
                timestamp
        );

        try {
            redisTemplate.opsForValue().set(key, response, locationTtlSeconds, TimeUnit.SECONDS);
            logger.debug("Stored driver location for trip {}: lat={}, lng={}", dto.getTripId(), dto.getLatitude(), dto.getLongitude());

            // Broadcast to customers subscribed to this trip
            messagingTemplate.convertAndSend("/topic/trip/" + dto.getTripId(), response);
        } catch (Exception e) {
            logger.error("Failed to store/broadcast driver location for trip {}: {}", dto.getTripId(), e.getMessage());
            throw new RuntimeException("Failed to update driver location", e);
        }
    }

    /**
     * Get latest driver location from Redis (for REST API fallback).
     */
    public DriverLocationResponseDto getLatestLocation(Long tripId) {
        String key = REDIS_KEY_PREFIX + tripId;
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof DriverLocationResponseDto dto) {
            return dto;
        }
        // Handle deserialization from Redis (may come as Map)
        try {
            return objectMapper.convertValue(value, DriverLocationResponseDto.class);
        } catch (Exception e) {
            logger.warn("Could not deserialize driver location for trip {}: {}", tripId, e.getMessage());
            return null;
        }
    }
}
