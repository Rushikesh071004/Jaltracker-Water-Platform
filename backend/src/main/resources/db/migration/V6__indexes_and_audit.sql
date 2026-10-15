-- ====================================================================
-- Flyway Migration V6: Database Indexing & Performance Optimization
-- ====================================================================

-- 1. Invoices / Bills optimization indexes
CREATE INDEX IF NOT EXISTS idx_invoices_household_id ON invoices(household_id);
CREATE INDEX IF NOT EXISTS idx_invoices_billing_cycle_id ON invoices(billing_cycle_id);
CREATE INDEX IF NOT EXISTS idx_invoices_payment_status ON invoices(payment_status);
CREATE INDEX IF NOT EXISTS idx_invoices_created_at ON invoices(created_at);

-- 2. Water Meters optimization indexes (water_meters references households)
CREATE INDEX IF NOT EXISTS idx_water_meters_household_id ON water_meters(household_id);
CREATE INDEX IF NOT EXISTS idx_water_meters_status ON water_meters(status);

-- 3. Water Usage Logs optimization indexes
CREATE INDEX IF NOT EXISTS idx_usage_logs_household_id ON water_usage_logs(household_id);
CREATE INDEX IF NOT EXISTS idx_usage_logs_meter_id ON water_usage_logs(meter_id);
CREATE INDEX IF NOT EXISTS idx_usage_logs_reading_date ON water_usage_logs(reading_date);

-- 4. Users table optimization indexes
CREATE INDEX IF NOT EXISTS idx_users_account_status ON users(account_status);
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);
CREATE INDEX IF NOT EXISTS idx_users_apartment_id ON users(apartment_id);
CREATE INDEX IF NOT EXISTS idx_users_household_id ON users(household_id);
