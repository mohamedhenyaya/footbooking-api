-- =========================
-- CREATE PAYMENT PROOFS TABLE
-- =========================

CREATE TABLE payment_proofs (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL UNIQUE REFERENCES bookings(id) ON DELETE CASCADE,
    screenshot_url TEXT NOT NULL,
    whatsapp_message TEXT,
    submitted_at TIMESTAMP NOT NULL DEFAULT now(),
    validated_at TIMESTAMP,
    validated_by BIGINT REFERENCES users(id),
    validation_status VARCHAR(50) NOT NULL DEFAULT 'pending',
    rejection_reason TEXT
);

-- Add check constraint for validation status
ALTER TABLE payment_proofs ADD CONSTRAINT check_validation_status 
    CHECK (validation_status IN ('pending', 'approved', 'rejected'));

-- Create indexes for faster lookups
CREATE INDEX idx_payment_proofs_booking_id ON payment_proofs(booking_id);
CREATE INDEX idx_payment_proofs_validation_status ON payment_proofs(validation_status);
