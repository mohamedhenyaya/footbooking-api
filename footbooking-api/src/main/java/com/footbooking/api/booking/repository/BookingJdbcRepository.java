package com.footbooking.api.booking.repository;

import com.footbooking.api.booking.dto.BookingResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class BookingJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    public List<Integer> findBookedHours(Long terrainId, LocalDate date) {
        String sql = """
            SELECT booking_hour
            FROM bookings
            WHERE terrain_id = ? AND booking_date = ?
            ORDER BY booking_hour
        """;
        return jdbcTemplate.queryForList(sql, Integer.class, terrainId, date);
    }
    public Long createBooking(Long userId, Long terrainId, LocalDate date, int hour) {
        String sql = """
        INSERT INTO bookings (user_id, terrain_id, booking_date, booking_hour)
        VALUES (?, ?, ?, ?)
        RETURNING id
    """;
        return jdbcTemplate.queryForObject(
                sql,
                Long.class,
                userId,
                terrainId,
                date,
                hour
        );
    }
    public List<BookingResponseDto> findBookingsByUserId(Long userId) {
        String sql = """
        SELECT b.id, b.terrain_id, b.booking_date, b.booking_hour, t.name, t.city
        FROM bookings b
        JOIN terrain t ON b.terrain_id = t.id
        WHERE b.user_id = ?
        ORDER BY b.id DESC
    """;

        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new BookingResponseDto(
                        rs.getLong("id"),
                        rs.getLong("terrain_id"),
                        rs.getString("name"),
                        rs.getString("city"),
                        rs.getDate("booking_date").toLocalDate(),
                        rs.getInt("booking_hour")
                ), userId);
    }

    public List<Long> findOccupiedTerrainIds(LocalDate date, int hour) {
        String sql = """
        SELECT terrain_id
        FROM bookings
        WHERE booking_date = ? AND booking_hour = ?
    """;
        return jdbcTemplate.queryForList(sql, Long.class, date, hour);
    }
}
