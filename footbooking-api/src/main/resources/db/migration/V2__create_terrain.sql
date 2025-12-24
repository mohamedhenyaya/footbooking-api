-- =========================
-- TERRAINS
-- =========================

CREATE TABLE terrain (
                         id             BIGSERIAL PRIMARY KEY,
                         name           VARCHAR(100) NOT NULL,
                         city           VARCHAR(100) NOT NULL,
                         price_per_hour NUMERIC(6,2) NOT NULL,
                         indoor         BOOLEAN NOT NULL DEFAULT false,
                         created_at     TIMESTAMP NOT NULL DEFAULT now()
);
