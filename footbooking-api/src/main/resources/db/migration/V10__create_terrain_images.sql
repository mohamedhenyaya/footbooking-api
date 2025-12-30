-- =========================
-- CREATE TERRAIN IMAGES TABLE
-- =========================

CREATE TABLE terrain_images (
    id BIGSERIAL PRIMARY KEY,
    terrain_id BIGINT NOT NULL REFERENCES terrain(id) ON DELETE CASCADE,
    image_url VARCHAR(500) NOT NULL,
    is_primary BOOLEAN DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

-- Create index for faster lookups
CREATE INDEX idx_terrain_images_terrain_id ON terrain_images(terrain_id);
