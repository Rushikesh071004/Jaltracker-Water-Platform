-- ====================================================================
-- Flyway Migration V3: JALTRACKER Upgrade
-- 1. Role hierarchy (SUPERADMIN, ADMIN, RESIDENT) & Account Status (PENDING, VERIFIED, REJECTED)
-- 2. Email OTP Authentication tokens
-- 3. Razorpay payment integration columns on invoices
-- 4. Indian cultural localization for complexes, suppliers, and seed accounts
-- ====================================================================

-- 1. Account status column for user verification workflow
ALTER TABLE users ADD COLUMN IF NOT EXISTS account_status VARCHAR(50) NOT NULL DEFAULT 'VERIFIED';

-- 2. Email OTP tokens table for passwordless login
CREATE TABLE IF NOT EXISTS email_otp_tokens (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(150) NOT NULL,
    otp_code VARCHAR(10) NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_email_otp ON email_otp_tokens(email, otp_code);

-- 3. Razorpay columns on invoices table
ALTER TABLE invoices ADD COLUMN IF NOT EXISTS razorpay_order_id VARCHAR(100);
ALTER TABLE invoices ADD COLUMN IF NOT EXISTS razorpay_payment_id VARCHAR(100);
ALTER TABLE invoices ADD COLUMN IF NOT EXISTS razorpay_signature VARCHAR(255);

-- 4. Culturally Indian localization for existing complex
UPDATE apartments 
SET name = 'Shanti Niketan Heights',
    address = 'Tower B, Link Road, Andheri West, Mumbai, Maharashtra 400053',
    contact_email = 'admin@shantiniketan.in',
    contact_phone = '+91 98201 23456'
WHERE id = 1;

-- 5. Update bulk water purchase suppliers to Indian municipal / tanker names
UPDATE bulk_water_purchases
SET supplier_name = 'Mumbai Jal Sansthan Bulk Supply'
WHERE supplier_name LIKE '%Municipal%';

UPDATE bulk_water_purchases
SET supplier_name = 'Shivaji Nagar Water Tanker Services'
WHERE supplier_name LIKE '%Tanker%';

-- 6. Update existing resident names to Indian names
UPDATE users
SET full_name = 'Rajesh Sharma (Society Admin)',
    phone = '+91 98201 11223'
WHERE email = 'admin@waterbill.com';

UPDATE users
SET full_name = 'Vikram Malhotra',
    phone = '+91 98201 44556'
WHERE email = 'resident1@waterbill.com';

UPDATE users
SET full_name = 'Priya Patel',
    phone = '+91 98201 77889'
WHERE email = 'resident2@waterbill.com';

UPDATE users
SET full_name = 'Amit Verma',
    phone = '+91 98201 99001'
WHERE email = 'resident3@waterbill.com';

-- 7. Seed Superadmin account: superadmin@jaltracker.com (Password: Superadmin@123)
-- BCrypt for 'Superadmin@123' = $2a$10$7Ez4k8sQ3tNf7Ld7fU6DDeR1s4YvK9O5rQ8u0eM5z2bC8L4Y8y7uK
INSERT INTO users (apartment_id, household_id, full_name, email, password_hash, phone, role, is_active, account_status)
VALUES (1, NULL, 'Rajesh Sharma (Master Superadmin)', 'superadmin@jaltracker.com', '$2a$10$7Ez4k8sQ3tNf7Ld7fU6DDeR1s4YvK9O5rQ8u0eM5z2bC8L4Y8y7uK', '+91 98200 00001', 'ROLE_SUPERADMIN', TRUE, 'VERIFIED')
ON CONFLICT (email) DO NOTHING;

-- 8. Seed jaltracker.com aliases for convenience
INSERT INTO users (apartment_id, household_id, full_name, email, password_hash, phone, role, is_active, account_status)
VALUES (1, NULL, 'Rajesh Sharma (Society Admin)', 'admin@jaltracker.com', '$2a$10$7Ez4k8sQ3tNf7Ld7fU6DDeR1s4YvK9O5rQ8u0eM5z2bC8L4Y8y7uK', '+91 98201 11223', 'ROLE_ADMIN', TRUE, 'VERIFIED')
ON CONFLICT (email) DO NOTHING;

INSERT INTO users (apartment_id, household_id, full_name, email, password_hash, phone, role, is_active, account_status)
VALUES (1, 1, 'Vikram Malhotra', 'resident1@jaltracker.com', '$2a$10$7Ez4k8sQ3tNf7Ld7fU6DDeR1s4YvK9O5rQ8u0eM5z2bC8L4Y8y7uK', '+91 98201 44556', 'ROLE_RESIDENT', TRUE, 'VERIFIED')
ON CONFLICT (email) DO NOTHING;

-- 9. Seed PENDING test accounts for the verification approval workflow
-- Pending Resident (Can be approved by Admin or Superadmin)
INSERT INTO users (apartment_id, household_id, full_name, email, password_hash, phone, role, is_active, account_status)
VALUES (1, 3, 'Sunita Rao', 'sunita.pending@jaltracker.com', '$2a$10$7Ez4k8sQ3tNf7Ld7fU6DDeR1s4YvK9O5rQ8u0eM5z2bC8L4Y8y7uK', '+91 98202 33445', 'ROLE_RESIDENT', TRUE, 'PENDING')
ON CONFLICT (email) DO NOTHING;

-- Pending Admin (Requires Superadmin to approve)
INSERT INTO users (apartment_id, household_id, full_name, email, password_hash, phone, role, is_active, account_status)
VALUES (1, NULL, 'Deepak Gupta (Admin Candidate)', 'deepak.admin@jaltracker.com', '$2a$10$7Ez4k8sQ3tNf7Ld7fU6DDeR1s4YvK9O5rQ8u0eM5z2bC8L4Y8y7uK', '+91 98203 55667', 'ROLE_ADMIN', TRUE, 'PENDING')
ON CONFLICT (email) DO NOTHING;
