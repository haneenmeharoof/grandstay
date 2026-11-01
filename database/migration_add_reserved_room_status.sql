-- Safe schema migration: add RESERVED without removing existing room states.
-- Run once against the intended GrandStay database. Back up the database first.
ALTER TABLE rooms MODIFY COLUMN status ENUM(
  'AVAILABLE','RESERVED','OCCUPIED','DIRTY','CLEANING','INSPECTED','MAINTENANCE','OUT_OF_SERVICE'
) NOT NULL DEFAULT 'AVAILABLE';
