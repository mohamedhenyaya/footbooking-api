-- =========================
-- TERRAINS
-- =========================

CREATE TABLE terrain (
                         id             BIGSERIAL PRIMARY KEY,
                         name           VARCHAR(100) NOT NULL,
                         city           VARCHAR(100) NOT NULL,
                         price_per_hour NUMERIC(6,2) NOT NULL,
                         created_at     TIMESTAMP NOT NULL DEFAULT now()
);
