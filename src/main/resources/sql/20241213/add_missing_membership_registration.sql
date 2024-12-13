DROP FUNCTION IF EXISTS find_membership_registration_id;

DELIMITER
//

CREATE FUNCTION find_membership_registration_id(account_id INT, coupon_id INT)
    RETURNS INT
    READS SQL DATA
BEGIN
    DECLARE
membership_registration_id INT;

SELECT mg.id
INTO membership_registration_id
FROM membership_registration mg
         JOIN membership m ON mg.membership_id = m.id
         JOIN db_heredium.account a ON mg.account_id = a.id
         JOIN coupon c ON m.id = c.membership_id
WHERE mg.account_id = account_id
  AND c.id = coupon_id
  AND c.from_source = 'MEMBERSHIP_PACKAGE'
  AND mg.payment_status = 'COMPLETED';
RETURN membership_registration_id;
END
//

DELIMITER ;

UPDATE coupon_usage
SET membership_registration_id = find_membership_registration_id(account_id, coupon_id)
WHERE membership_registration_id IS NULL;