-- =========================
-- CREATE TERRAIN REVIEWS TABLE
-- =========================

CREATE TABLE terrain_reviews (
    id BIGSERIAL PRIMARY KEY,
    terrain_id BIGINT NOT NULL REFERENCES terrain(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    rating INTEGER NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

-- Create indexes for faster lookups
CREATE INDEX idx_terrain_reviews_terrain_id ON terrain_reviews(terrain_id);
CREATE INDEX idx_terrain_reviews_user_id ON terrain_reviews(user_id);
