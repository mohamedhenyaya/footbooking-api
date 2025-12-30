-- =========================
-- CREATE FAVORITES TABLE
-- =========================

CREATE TABLE favorites (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    terrain_id BIGINT NOT NULL REFERENCES terrain(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (user_id, terrain_id)
);

-- Create index for faster lookups
CREATE INDEX idx_favorites_user_id ON favorites(user_id);
CREATE INDEX idx_favorites_terrain_id ON favorites(terrain_id);
