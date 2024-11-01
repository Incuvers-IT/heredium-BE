ALTER TABLE ticket ADD COLUMN coupon_uuid VARCHAR(255);
ALTER TABLE ticket ADD COLUMN is_coupon_already_refund BOOLEAN;