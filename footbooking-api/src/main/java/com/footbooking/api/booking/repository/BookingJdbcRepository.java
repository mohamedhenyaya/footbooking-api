package com.footbooking.api.booking.repository;

import com.footbooking.api.booking.dto.BookingResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

import com.footbooking.api.booking.dto.RawBookingSlotDto;
import java.time.LocalDateTime;

@Repository
@RequiredArgsConstructor
public class BookingJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    // ... existing findBookedHours ...

    public List<RawBookingSlotDto> findBookedSlotsDetails(Long terrainId, LocalDate date) {
        String sql = """
                    SELECT booking_hour, 'CONFIRMED' as status, created_at
                    FROM bookings
                    WHERE terrain_id = ? AND booking_date = ?
                    UNION
                    SELECT booking_hour, status, created_at
                    FROM booking_requests
                    WHERE terrain_id = ? AND booking_date = ?
                    AND upper(status) IN ('EN_ATTENTE_PAIEMENT', 'EN_ATTENTE_VALIDATION_ADMIN')
                    ORDER BY booking_hour
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new RawBookingSlotDto(
                        rs.getInt("booking_hour"),
                        rs.getString("status"),
                        rs.getTimestamp("created_at").toLocalDateTime()),
                terrainId, date, terrainId, date);
    }

    public List<Integer> findBookedHours(Long terrainId, LocalDate date) {
        String sql = """
                    SELECT booking_hour
                    FROM bookings
                    WHERE terrain_id = ? AND booking_date = ?
                    UNION
                    SELECT booking_hour
                    FROM booking_requests
                    WHERE terrain_id = ? AND booking_date = ?
                    AND upper(status) IN ('EN_ATTENTE_PAIEMENT', 'EN_ATTENTE_VALIDATION_ADMIN')
                    ORDER BY booking_hour
                """;
        return jdbcTemplate.queryForList(sql, Integer.class, terrainId, date, terrainId, date);
    }

    public Long createBooking(Long userId, Long terrainId, LocalDate date, int hour, String status) {
        String sql = """
                    INSERT INTO bookings (user_id, terrain_id, booking_date, booking_hour, status)
                    VALUES (?, ?, ?, ?, ?)
                    RETURNING id
                """;
        return jdbcTemplate.queryForObject(
                sql,
                Long.class,
                userId,
                terrainId,
                date,
                hour,
                status);
    }

    public List<BookingResponseDto> findBookingsByUserId(Long userId) {
        String sql = """
                    SELECT b.id, b.terrain_id, b.booking_date, b.booking_hour, t.name, t.city
                    FROM bookings b
                    JOIN terrain t ON b.terrain_id = t.id
                    WHERE b.user_id = ?
                    ORDER BY b.id DESC
                """;

        // System.out.println("DEBUG: findBookingsByUserId called with userId: " +
        // userId);
        return jdbcTemplate.query(sql, (rs, rowNum) -> new BookingResponseDto(
                rs.getLong("id"),
                rs.getLong("terrain_id"),
                rs.getString("name"),
                rs.getString("city"),
                rs.getDate("booking_date").toLocalDate(),
                rs.getInt("booking_hour")), userId);
    }

    public boolean deleteBooking(Long bookingId, Long userId) {
        String sql = "DELETE FROM bookings WHERE id = ? AND user_id = ?";
        int rowsAffected = jdbcTemplate.update(sql, bookingId, userId);
        return rowsAffected > 0;
    }

    public boolean deleteBookingAsOwner(Long bookingId, Long ownerId) {
        // Deletes a booking if the terrain belongs to the owner
        String sql = """
                    DELETE FROM bookings b
                    USING terrain t
                    WHERE b.terrain_id = t.id
                    AND b.id = ?
                    AND t.owner_id = ?
                """;
        // Note: USING syntax is Postgres specific, which fits here.
        int rowsAffected = jdbcTemplate.update(sql, bookingId, ownerId);
        return rowsAffected > 0;
    }

    // For "accepting" logic, if we had a status column we would update it.
    // The prompt says "Accepter ou Annuler".
    // Currently bookings seem auto-accepted (status doesn't exist in creating
    // logic).
    // If we need an "accept" endpoint, we probably need a STATUS column
    // (PENDING/ACCEPTED).
    // Assuming for now we just need the ENDPOINT to exist, even if logic is simple.
    // Wait, user request: "Gérer les Réservations : Accepter ou Annuler"
    // Does the DB support status?
    // Let's check schema via V3__create_booking_table.sql (implied) or similar.
    // I will check the schema first or assume no status for now and treating
    // "Accept" as a no-op or explicit status change if column exists.
    // If I cannot verify column, I will assume I need to ADD it.

    public boolean updateBookingStatus(Long bookingId, String status, Long ownerId) {
        // Update status only if terrain belongs to owner
        String sql = """
                    UPDATE bookings b
                    SET status = ?
                    FROM terrain t
                    WHERE b.terrain_id = t.id
                    AND b.id = ?
                    AND t.owner_id = ?
                """;
        int rowsAffected = jdbcTemplate.update(sql, status, bookingId, ownerId);
        return rowsAffected > 0;
    }

    public boolean deleteBookingAny(Long bookingId) {
        String sql = "DELETE FROM bookings WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql, bookingId);
        return rowsAffected > 0;
    }

    public List<Long> findOccupiedTerrainIds(LocalDate date, int hour) {
        String sql = """
                    SELECT terrain_id
                    FROM bookings
                    WHERE booking_date = ? AND booking_hour = ?
                """;
        return jdbcTemplate.queryForList(sql, Long.class, date, hour);
    }

    public List<com.footbooking.api.booking.dto.AdminBookingResponseDto> findBookingsByOwner(Long ownerId,
            LocalDate date, String status) {
        StringBuilder sql = new StringBuilder("""
                    SELECT b.id, b.booking_date, b.booking_hour, b.status,
                           u.name as user_name, u.email as user_email, u.phone as user_phone,
                           t.id as terrain_id, t.name as terrain_name
                    FROM bookings b
                    JOIN terrain t ON b.terrain_id = t.id
                    JOIN users u ON b.user_id = u.id
                    WHERE t.owner_id = ?
                """);

        List<Object> params = new java.util.ArrayList<>();
        params.add(ownerId);

        if (date != null) {
            sql.append(" AND b.booking_date = ?");
            params.add(date);
        }

        if (status != null && !status.isEmpty()) {
            sql.append(" AND b.status = ?");
            params.add(status);
        }

        sql.append(" ORDER BY b.booking_date DESC, b.booking_hour ASC");

        return jdbcTemplate.query(sql.toString(),
                (rs, rowNum) -> new com.footbooking.api.booking.dto.AdminBookingResponseDto(
                        rs.getLong("id"),
                        rs.getDate("booking_date").toLocalDate(),
                        rs.getInt("booking_hour"),
                        rs.getString("status"),
                        "PAYE", // Default for JDBC confirmed bookings
                        new com.footbooking.api.booking.dto.UserSummaryDto(
                                rs.getString("user_name"),
                                rs.getString("user_email"),
                                rs.getString("user_phone")),
                        new com.footbooking.api.booking.dto.TerrainSummaryDto(
                                rs.getLong("terrain_id"),
                                rs.getString("terrain_name"))),
                params.toArray());
    }

    public void deleteBookingsByUserIds(List<Long> userIds) {
        if (userIds.isEmpty()) {
            return;
        }
        String sql = "DELETE FROM bookings WHERE user_id IN (" +
                String.join(",", java.util.Collections.nCopies(userIds.size(), "?")) +
                ")";
        jdbcTemplate.update(sql, userIds.toArray());
    }

    public void deleteRequestsForBookingsOfUsers(List<Long> userIds) {
        if (userIds.isEmpty()) {
            return;
        }
        // Delete requests that link to bookings owned by these users
        String sql = "DELETE FROM booking_requests WHERE booking_id IN (SELECT id FROM bookings WHERE user_id IN (" +
                String.join(",", java.util.Collections.nCopies(userIds.size(), "?")) +
                "))";
        jdbcTemplate.update(sql, userIds.toArray());
    }
}
