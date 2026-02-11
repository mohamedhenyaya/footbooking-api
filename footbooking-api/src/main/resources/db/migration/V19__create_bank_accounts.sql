-- =========================
-- CREATE BANK ACCOUNTS TABLE
-- =========================

CREATE TABLE bank_accounts (
    id BIGSERIAL PRIMARY KEY,
    terrain_id BIGINT NOT NULL UNIQUE REFERENCES terrain(id) ON DELETE CASCADE,
    bank_name VARCHAR(255) NOT NULL,
    account_number VARCHAR(255) NOT NULL,
    additional_info TEXT,
);

-- Create index for faster lookups
CREATE INDEX idx_bank_accounts_terrain_id ON bank_accounts(terrain_id);
