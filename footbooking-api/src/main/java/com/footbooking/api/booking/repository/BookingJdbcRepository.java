package com.footbooking.api.booking.repository;

import com.footbooking.api.booking.dto.BookingResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class BookingJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    public record BookedSlotRecord(int hour, LocalDateTime createdAt) {}

    public List<BookedSlotRecord> findBookedSlotsDetails(Long terrainId, LocalDate date) {
        String sql = """
                SELECT booking_hour, created_at
                FROM bookings
                WHERE terrain_id = ? AND booking_date = ?
                ORDER BY booking_hour ASC
            """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new BookedSlotRecord(
                        rs.getInt("booking_hour"),
                        rs.getTimestamp("created_at").toLocalDateTime()),
                terrainId, date);
    }
    public List<Integer> findBookedHours(Long terrainId, LocalDate date) {
        String sql = """
                    SELECT booking_hour
                    FROM bookings
                    WHERE terrain_id = ? AND booking_date = ?
                    ORDER BY booking_hour
                """;
        return jdbcTemplate.queryForList(sql, Integer.class, terrainId, date);
    }

    public Long createBooking(Long userId, Long terrainId, LocalDate date, int hour, String moovMoneyNumber) {
        String sql = """
                    INSERT INTO bookings (user_id, terrain_id, booking_date, booking_hour, created_at, moov_money_number)
                    VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, ?)
                    RETURNING id
                """;
        return jdbcTemplate.queryForObject(
                sql,
                Long.class,
                userId,
                terrainId,
                date,
                hour,
                moovMoneyNumber
        );
    }

    public List<BookingResponseDto> findBookingsByUserId(Long userId) {
        String sql = """
                    SELECT b.id, b.terrain_id, b.booking_date, b.booking_hour, b.moov_money_number, t.name, t.city
                    FROM bookings b
                    JOIN terrain t ON b.terrain_id = t.id
                    WHERE b.user_id = ?
                    ORDER BY b.id DESC
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> new BookingResponseDto(
                        rs.getLong("id"),
                        rs.getLong("terrain_id"),
                        rs.getString("name"),
                        rs.getString("city"),
                        rs.getDate("booking_date").toLocalDate(),
                        rs.getInt("booking_hour"),
                        rs.getString("moov_money_number"),
                        null),
                userId);
    }

    public boolean deleteBooking(Long bookingId, Long userId) {
        String sql = "DELETE FROM bookings WHERE id = ? AND user_id = ?";
        int rowsAffected = jdbcTemplate.update(sql, bookingId, userId);
        return rowsAffected > 0;
    }

    public boolean deleteBookingAsOwner(Long bookingId, Long ownerId) {
        String sql = """
                    DELETE FROM bookings b
                    USING terrain t
                    WHERE b.terrain_id = t.id
                    AND b.id = ?
                    AND t.owner_id = ?
                """;
        int rowsAffected = jdbcTemplate.update(sql, bookingId, ownerId);
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
                                                                                             LocalDate date) {
        StringBuilder sql = new StringBuilder("""
                    SELECT b.id, b.booking_date, b.booking_hour,
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

        sql.append(" ORDER BY b.booking_date DESC, b.booking_hour ASC");

        return jdbcTemplate.query(sql.toString(),
                (rs, rowNum) -> new com.footbooking.api.booking.dto.AdminBookingResponseDto(
                        rs.getLong("id"),
                        rs.getDate("booking_date").toLocalDate(),
                        rs.getInt("booking_hour"),
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
}