-- =========================
-- BOOKINGS
-- =========================

CREATE TABLE bookings (
                          id            BIGSERIAL PRIMARY KEY,
                          user_id       BIGINT NOT NULL REFERENCES users(id),
                          terrain_id    BIGINT NOT NULL REFERENCES terrain(id),
                          booking_date  DATE NOT NULL,
                          booking_hour  INTEGER NOT NULL CHECK (booking_hour BETWEEN 0 AND 23),
                          created_at    TIMESTAMP NOT NULL DEFAULT now(),
                          UNIQUE (terrain_id, booking_date, booking_hour)
);
