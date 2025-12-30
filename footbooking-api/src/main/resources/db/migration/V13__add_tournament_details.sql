-- =========================
-- ADD TOURNAMENT DETAIL FIELDS
-- =========================

-- Add enriched tournament information fields
ALTER TABLE tournaments ADD COLUMN IF NOT EXISTS description TEXT;
ALTER TABLE tournaments ADD COLUMN IF NOT EXISTS rules TEXT;
ALTER TABLE tournaments ADD COLUMN IF NOT EXISTS organizer VARCHAR(255);
ALTER TABLE tournaments ADD COLUMN IF NOT EXISTS status VARCHAR(50) DEFAULT 'inscription_ouverte';
ALTER TABLE tournaments ADD COLUMN IF NOT EXISTS image_url VARCHAR(500);

-- Add check constraint for status
ALTER TABLE tournaments ADD CONSTRAINT check_tournament_status 
    CHECK (status IN ('inscription_ouverte', 'en_cours', 'terminé'));
