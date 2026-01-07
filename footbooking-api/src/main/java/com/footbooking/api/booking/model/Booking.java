package com.footbooking.api.booking.model;

import com.footbooking.api.auth.model.User;
import com.footbooking.api.terrain.model.Terrain;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "terrain_id", nullable = false)
    private Terrain terrain;

    @Column(name = "booking_date", nullable = false)
    private LocalDate date;

    @Column(name = "booking_hour", nullable = false)
    private Integer hour;

    @Column(nullable = false)
    private String status;

    @Column(name = "payment_status")
    private String paymentStatus;

    @Builder.Default
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
