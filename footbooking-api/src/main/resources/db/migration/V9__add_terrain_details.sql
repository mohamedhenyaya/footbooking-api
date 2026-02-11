-- =========================
-- ADD TERRAIN DETAIL FIELDS
-- =========================

-- Add detailed information fields to terrain table
ALTER TABLE terrain ADD COLUMN IF NOT EXISTS description TEXT;
ALTER TABLE terrain ADD COLUMN IF NOT EXISTS surface VARCHAR(50); -- gazon, synthétique, etc.
ALTER TABLE terrain ADD COLUMN IF NOT EXISTS capacity INTEGER; -- max number of players
ALTER TABLE terrain ADD COLUMN IF NOT EXISTS rating NUMERIC(3,2) DEFAULT 0.0; -- average rating
