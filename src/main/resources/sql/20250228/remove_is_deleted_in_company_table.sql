DELETE FROM company WHERE is_deleted IS TRUE;
ALTER TABLE company DROP COLUMN is_deleted;
