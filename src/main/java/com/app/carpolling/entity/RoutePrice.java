package com.app.carpolling.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "route_prices", 
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"route_id", "boarding_point_id", "drop_point_id"})
    },
    indexes = {
        @Index(name = "idx_route_price_route", columnList = "route_id"),
        @Index(name = "idx_route_price_boarding", columnList = "boarding_point_id"),
        @Index(name = "idx_route_price_drop", columnList = "drop_point_id")
    })
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class RoutePrice {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;
    
    @ManyToOne
    @JoinColumn(name = "boarding_point_id", nullable = false)
    private RoutePoint boardingPoint;
    
    @ManyToOne
    @JoinColumn(name = "drop_point_id", nullable = false)
    private RoutePoint dropPoint;
    
    @Column(nullable = false)
    private Double price; // Fixed price for this boarding-drop combination
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}

