-- =========================
-- CREATE PAYMENT METHODS TABLE
-- =========================

CREATE TABLE payment_methods (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    card_type VARCHAR(50) NOT NULL,
    last_four_digits VARCHAR(4) NOT NULL,
    expiry_month INTEGER NOT NULL CHECK (expiry_month BETWEEN 1 AND 12),
    expiry_year INTEGER NOT NULL,
    is_default BOOLEAN DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

-- Create index for faster lookups
CREATE INDEX idx_payment_methods_user_id ON payment_methods(user_id);
