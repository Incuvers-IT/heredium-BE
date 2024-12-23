-- Delete related coupon_usage records
DELETE cu FROM coupon_usage cu
INNER JOIN membership_registration mr ON cu.account_id = mr.account_id;

-- Delete related ticket records
DELETE t FROM ticket t
INNER JOIN membership_registration mr ON t.account_id = mr.account_id;

-- Delete all membership_registration records
DELETE FROM membership_registration;
