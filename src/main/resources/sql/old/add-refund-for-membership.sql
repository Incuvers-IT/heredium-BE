ALTER TABLE membership_registration
    ADD COLUMN payment_key VARCHAR(255) UNIQUE,
    ADD COLUMN payment_type INT;
ALTER TABLE coupon_usage
    ADD COLUMN membership_registration_id BIGINT,
    ADD FOREIGN KEY (membership_registration_id) REFERENCES membership_registration(id) ON DELETE CASCADE;
