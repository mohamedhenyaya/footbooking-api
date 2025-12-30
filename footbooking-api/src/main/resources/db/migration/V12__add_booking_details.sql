-- =========================
-- ADD BOOKING DETAIL FIELDS
-- =========================

-- Add enriched booking information fields
ALTER TABLE bookings ADD COLUMN IF NOT EXISTS status VARCHAR(50) DEFAULT 'confirmée';
ALTER TABLE bookings ADD COLUMN IF NOT EXISTS total_price NUMERIC(8,2);
ALTER TABLE bookings ADD COLUMN IF NOT EXISTS payment_status VARCHAR(50) DEFAULT 'non_payé';
ALTER TABLE bookings ADD COLUMN IF NOT EXISTS cancellation_deadline TIMESTAMP;

-- Add check constraint for status
ALTER TABLE bookings ADD CONSTRAINT check_booking_status 
    CHECK (status IN ('confirmée', 'en_attente', 'annulée'));

-- Add check constraint for payment status
ALTER TABLE bookings ADD CONSTRAINT check_payment_status 
    CHECK (payment_status IN ('payé', 'non_payé'));
