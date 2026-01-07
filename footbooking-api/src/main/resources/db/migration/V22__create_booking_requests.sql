-- Create booking_requests table
CREATE TABLE booking_requests (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    terrain_id BIGINT NOT NULL REFERENCES terrains(id),
    booking_date DATE NOT NULL,
    booking_hour INT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'en_attente_paiement',
    payment_screenshot_url VARCHAR(500),
    whatsapp_message TEXT,
    submitted_at TIMESTAMP,
    deadline TIMESTAMP NOT NULL,
    approved_by BIGINT REFERENCES users(id),
    approved_at TIMESTAMP,
    rejection_reason TEXT,
    booking_id BIGINT REFERENCES bookings(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT check_booking_request_status 
        CHECK (status IN ('en_attente_paiement', 'en_attente_validation_admin', 'approuvée', 'rejetée', 'expirée'))
);

-- Create indexes for performance
CREATE INDEX idx_booking_requests_status ON booking_requests(status);
CREATE INDEX idx_booking_requests_terrain ON booking_requests(terrain_id);
CREATE INDEX idx_booking_requests_user ON booking_requests(user_id);
CREATE INDEX idx_booking_requests_deadline ON booking_requests(deadline);
