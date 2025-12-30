-- =========================
-- CREATE PUSH TOKENS TABLE
-- =========================

CREATE TABLE push_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token VARCHAR(500) NOT NULL,
    device_type VARCHAR(50), -- 'ios' or 'android'
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (user_id, token)
);

-- Create index for faster lookups
CREATE INDEX idx_push_tokens_user_id ON push_tokens(user_id);
