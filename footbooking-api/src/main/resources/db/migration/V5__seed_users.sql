-- =========================
-- SEED USERS (DEV)
-- =========================
INSERT INTO users (id, email, phone, password, enabled)
VALUES (1, 'dev@footbooking.com', '0600000000', '{noop}dev', true)
    ON CONFLICT DO NOTHING;
