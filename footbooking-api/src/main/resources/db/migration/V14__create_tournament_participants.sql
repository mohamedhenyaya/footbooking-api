-- =========================
-- CREATE TOURNAMENT PARTICIPANTS TABLE
-- =========================

CREATE TABLE tournament_participants (
    id BIGSERIAL PRIMARY KEY,
    tournament_id BIGINT NOT NULL REFERENCES tournaments(id) ON DELETE CASCADE,
    team_name VARCHAR(255) NOT NULL,
    registered_at TIMESTAMP NOT NULL DEFAULT now()
);

-- Create index for faster lookups
CREATE INDEX idx_tournament_participants_tournament_id ON tournament_participants(tournament_id);
