-- =========================
-- UPDATE PAYMENT STATUS CONSTRAINT
-- =========================

-- Drop the existing constraint
ALTER TABLE bookings DROP CONSTRAINT IF EXISTS check_payment_status;

-- Add the updated constraint with new status
ALTER TABLE bookings ADD CONSTRAINT check_payment_status 
    CHECK (payment_status IN ('payé', 'non_payé', 'en_attente_validation'));
