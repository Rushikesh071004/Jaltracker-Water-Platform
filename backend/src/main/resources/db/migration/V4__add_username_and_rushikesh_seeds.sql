-- ====================================================================
-- Flyway Migration V4: Username Column & Rushikesh Waghmare User Seeds
-- ====================================================================

-- 1. Add username column to users table
ALTER TABLE users ADD COLUMN IF NOT EXISTS username VARCHAR(100) UNIQUE;

-- 2. Update existing accounts to use Rushikesh Waghmare and populate username
UPDATE users
SET full_name = 'Rushikesh Waghmare (Superadmin)',
    username = 'superadmin'
WHERE email = 'superadmin@jaltracker.com';

UPDATE users
SET full_name = 'Rushikesh Waghmare (Society Admin)',
    username = 'admin'
WHERE email = 'admin@jaltracker.com';

UPDATE users
SET full_name = 'Rushikesh Waghmare (Society Admin)',
    username = 'admin_legacy'
WHERE email = 'admin@waterbill.com';

UPDATE users
SET full_name = 'Rushikesh Waghmare',
    username = 'rushikesh'
WHERE email = 'resident1@jaltracker.com';

UPDATE users
SET full_name = 'Rushikesh Waghmare',
    username = 'resident1_legacy'
WHERE email = 'resident1@waterbill.com';

UPDATE users
SET full_name = 'Rushikesh Waghmare',
    username = 'resident2'
WHERE email = 'resident2@waterbill.com';

UPDATE users
SET full_name = 'Rushikesh Waghmare',
    username = 'resident3'
WHERE email = 'resident3@waterbill.com';
