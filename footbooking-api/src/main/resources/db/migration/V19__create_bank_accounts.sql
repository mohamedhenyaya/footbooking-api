-- =========================
-- CREATE BANK ACCOUNTS TABLE
-- =========================

CREATE TABLE bank_accounts (
    id BIGSERIAL PRIMARY KEY,
    terrain_id BIGINT NOT NULL UNIQUE REFERENCES terrain(id) ON DELETE CASCADE,
    account_holder_name VARCHAR(255) NOT NULL,
    bank_name VARCHAR(255) NOT NULL,
    account_number VARCHAR(255) NOT NULL,
    rib VARCHAR(255) NOT NULL,
    additional_info TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

-- Create index for faster lookups
CREATE INDEX idx_bank_accounts_terrain_id ON bank_accounts(terrain_id);
