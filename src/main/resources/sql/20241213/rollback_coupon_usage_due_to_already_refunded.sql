DROP FUNCTION IF EXISTS should_remove_coupon_usage_due_to_already_refunded;
DELIMITER
//
CREATE FUNCTION should_remove_coupon_usage_due_to_already_refunded(account_id INT, coupon_id INT)
    RETURNS BOOLEAN
    READS SQL DATA
BEGIN
    DECLARE
count_result INT;

SELECT count(mg.id)
INTO count_result
FROM membership_registration mg
         JOIN membership m ON mg.membership_id = m.id
         JOIN db_heredium.account a ON mg.account_id = a.id
         JOIN coupon c ON m.id = c.membership_id
WHERE mg.account_id = account_id
  AND c.id = coupon_id
  AND c.from_source = 'MEMBERSHIP_PACKAGE'
  AND mg.payment_status = 'REFUND'
  AND mg.id = (SELECT m.id FROM membership_registration m WHERE m.account_id = account_id order by m.id desc limit 1);
RETURN count_result > 0;
END
//
DELIMITER ;

DELETE
FROM coupon_usage
WHERE should_remove_coupon_usage_due_to_already_refunded(account_id, coupon_id);