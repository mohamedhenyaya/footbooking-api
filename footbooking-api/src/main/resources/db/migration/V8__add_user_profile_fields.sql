-- =========================
-- ADD USER PROFILE FIELDS
-- =========================

-- Add avatar field for user profile pictures
ALTER TABLE users ADD COLUMN IF NOT EXISTS avatar VARCHAR(500);
