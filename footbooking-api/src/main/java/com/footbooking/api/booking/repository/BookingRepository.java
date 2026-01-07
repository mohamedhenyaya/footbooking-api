package com.footbooking.api.booking.repository;

import com.footbooking.api.booking.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Find all bookings (Superadmin) with optional status and date filtering
    @org.springframework.data.jpa.repository.Query("SELECT b FROM Booking b WHERE " +
            "(:date IS NULL OR b.date = :date) AND " +
            "(:status IS NULL OR b.status = :status)")
    java.util.List<Booking> findAllbookings(java.time.LocalDate date, String status);

    // Find bookings by terrain owner (Admin) with optional status and date
    // filtering
    @org.springframework.data.jpa.repository.Query("SELECT b FROM Booking b WHERE " +
            "b.terrain.owner.id = :ownerId AND " +
            "(:date IS NULL OR b.date = :date) AND " +
            "(:status IS NULL OR b.status = :status)")
    java.util.List<Booking> findByTerrainOwnerId(Long ownerId, java.time.LocalDate date, String status);
}
