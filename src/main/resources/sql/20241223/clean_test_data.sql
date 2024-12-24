-- Delete related coupon_usage records
DELETE FROM coupon_usage;

-- Delete related ticket records (including child tables first)
DELETE tl FROM ticket_log tl
INNER JOIN ticket t ON tl.ticket_id = t.id
INNER JOIN membership_registration mr ON t.account_id = mr.account_id;

DELETE tp FROM ticket_price tp
INNER JOIN ticket t ON tp.ticket_id = t.id
INNER JOIN membership_registration mr ON t.account_id = mr.account_id;

-- Delete related ticket records
DELETE t FROM ticket t
INNER JOIN membership_registration mr ON t.account_id = mr.account_id;

-- Delete all membership_registration records
DELETE FROM membership_registration;

-- Delete company membership registration history records
DELETE FROM company_membership_registration_history;
