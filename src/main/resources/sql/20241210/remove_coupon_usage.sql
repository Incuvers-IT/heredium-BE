DELETE coupon_usage
FROM coupon_usage
         JOIN coupon ON coupon_usage.coupon_id = coupon.id
WHERE coupon.from_source = 'MEMBERSHIP_PACKAGE'
  AND coupon_usage.membership_registration_id IS NULL;
