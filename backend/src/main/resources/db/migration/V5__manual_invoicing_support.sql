-- ====================================================================
-- Flyway Migration V5: Support Multiple Invoices and Manual Billing
-- ====================================================================

-- Drop strict single invoice per cycle constraint to support manual bills & adjustments
ALTER TABLE invoices DROP CONSTRAINT IF EXISTS uq_cycle_household;

-- Make billing_cycle_id nullable if needed for standalone manual bills
ALTER TABLE invoices ALTER COLUMN billing_cycle_id DROP NOT NULL;
