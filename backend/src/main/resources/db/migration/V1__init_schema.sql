-- ====================================================================
-- Flyway Migration V1: Database Schema for Water Consumption Platform
-- ====================================================================

-- 1. Apartments
CREATE TABLE apartments (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    address TEXT NOT NULL,
    total_flats INT NOT NULL DEFAULT 0,
    contact_email VARCHAR(150),
    contact_phone VARCHAR(50),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 2. Households
CREATE TABLE households (
    id BIGSERIAL PRIMARY KEY,
    apartment_id BIGINT NOT NULL REFERENCES apartments(id) ON DELETE CASCADE,
    flat_number VARCHAR(50) NOT NULL,
    block_or_wing VARCHAR(50),
    floor_number INT,
    area_sqft NUMERIC(10, 2) NOT NULL DEFAULT 1000.00,
    number_of_occupants INT NOT NULL DEFAULT 1,
    is_metered BOOLEAN NOT NULL DEFAULT TRUE,
    daily_usage_threshold_kl NUMERIC(10, 3) DEFAULT 1.500,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_apartment_household UNIQUE (apartment_id, flat_number, block_or_wing)
);

-- 3. Water Meters
CREATE TABLE water_meters (
    id BIGSERIAL PRIMARY KEY,
    meter_serial_number VARCHAR(100) NOT NULL UNIQUE,
    household_id BIGINT UNIQUE REFERENCES households(id) ON DELETE SET NULL,
    installation_date DATE,
    initial_reading NUMERIC(12, 3) NOT NULL DEFAULT 0.000,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, INACTIVE, FAULTY
    meter_type VARCHAR(50) NOT NULL DEFAULT 'DIGITAL', -- DIGITAL, ANALOG, SMART_PULSE
    last_reading_value NUMERIC(12, 3) DEFAULT 0.000,
    last_reading_date DATE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 4. Users
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    apartment_id BIGINT REFERENCES apartments(id) ON DELETE SET NULL,
    household_id BIGINT REFERENCES households(id) ON DELETE SET NULL,
    full_name VARCHAR(150) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    phone VARCHAR(50),
    role VARCHAR(50) NOT NULL, -- ROLE_ADMIN, ROLE_RESIDENT
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 5. Tariff Plans
CREATE TABLE tariff_plans (
    id BIGSERIAL PRIMARY KEY,
    apartment_id BIGINT NOT NULL REFERENCES apartments(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    effective_from DATE NOT NULL,
    effective_to DATE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 6. Tariff Tiers
CREATE TABLE tariff_tiers (
    id BIGSERIAL PRIMARY KEY,
    tariff_plan_id BIGINT NOT NULL REFERENCES tariff_plans(id) ON DELETE CASCADE,
    tier_name VARCHAR(100) NOT NULL,
    min_volume_kl NUMERIC(10, 3) NOT NULL DEFAULT 0.000,
    max_volume_kl NUMERIC(10, 3), -- NULL denotes infinity / unbounded upper limit
    rate_per_kl NUMERIC(10, 2) NOT NULL,
    tier_order INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 7. Billing Cycles
CREATE TABLE billing_cycles (
    id BIGSERIAL PRIMARY KEY,
    apartment_id BIGINT NOT NULL REFERENCES apartments(id) ON DELETE CASCADE,
    cycle_name VARCHAR(100) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    due_date DATE NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'OPEN', -- DRAFT, OPEN, FINALIZED, ARCHIVED
    total_bulk_water_cost NUMERIC(12, 2) DEFAULT 0.00,
    total_common_area_volume_kl NUMERIC(12, 3) DEFAULT 0.000,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    finalized_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT uq_apartment_cycle_name UNIQUE (apartment_id, cycle_name)
);

-- 8. Bulk Water Purchases (Tankers & Municipal bulk deliveries)
CREATE TABLE bulk_water_purchases (
    id BIGSERIAL PRIMARY KEY,
    billing_cycle_id BIGINT NOT NULL REFERENCES billing_cycles(id) ON DELETE CASCADE,
    apartment_id BIGINT NOT NULL REFERENCES apartments(id) ON DELETE CASCADE,
    purchase_date DATE NOT NULL,
    supplier_name VARCHAR(150) NOT NULL,
    purchase_type VARCHAR(50) NOT NULL, -- TANKER, MUNICIPAL, EMERGENCY_SUPPLY
    volume_kl NUMERIC(10, 3) NOT NULL,
    unit_cost_per_kl NUMERIC(10, 2) NOT NULL,
    total_cost NUMERIC(12, 2) NOT NULL,
    invoice_reference VARCHAR(100),
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 9. Water Usage Logs
CREATE TABLE water_usage_logs (
    id BIGSERIAL PRIMARY KEY,
    household_id BIGINT NOT NULL REFERENCES households(id) ON DELETE CASCADE,
    meter_id BIGINT REFERENCES water_meters(id) ON DELETE SET NULL,
    reading_date DATE NOT NULL,
    current_reading_kl NUMERIC(12, 3) NOT NULL,
    daily_consumption_kl NUMERIC(10, 3) NOT NULL,
    log_source VARCHAR(50) NOT NULL DEFAULT 'MANUAL_ENTRY', -- MANUAL_ENTRY, CSV_BULK_UPLOAD, IOT_METER
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_household_reading_date UNIQUE (household_id, reading_date)
);

-- 10. Invoices (Bills per household)
CREATE TABLE invoices (
    id BIGSERIAL PRIMARY KEY,
    billing_cycle_id BIGINT NOT NULL REFERENCES billing_cycles(id) ON DELETE CASCADE,
    household_id BIGINT NOT NULL REFERENCES households(id) ON DELETE CASCADE,
    invoice_number VARCHAR(100) NOT NULL UNIQUE,
    metered_consumption_kl NUMERIC(10, 3) NOT NULL DEFAULT 0.000,
    metered_charge NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    shared_common_charge NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    total_amount NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    payment_status VARCHAR(50) NOT NULL DEFAULT 'PENDING', -- PENDING, PAID, OVERDUE, WAIVED
    payment_date TIMESTAMP WITH TIME ZONE,
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_cycle_household UNIQUE (billing_cycle_id, household_id)
);

-- 11. Alerts (Threshold & 2-sigma Leak notifications)
CREATE TABLE alerts (
    id BIGSERIAL PRIMARY KEY,
    household_id BIGINT NOT NULL REFERENCES households(id) ON DELETE CASCADE,
    alert_type VARCHAR(50) NOT NULL, -- OVERUSE_THRESHOLD, LEAK_DETECTED_2SIGMA, UNMETERED_SPIKE
    severity VARCHAR(50) NOT NULL DEFAULT 'WARNING', -- INFO, WARNING, CRITICAL
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    reading_date DATE NOT NULL,
    consumption_kl NUMERIC(10, 3) NOT NULL,
    threshold_kl NUMERIC(10, 3),
    sigma_value NUMERIC(6, 2),
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, ACKNOWLEDGED, RESOLVED
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance
CREATE INDEX idx_households_apartment ON households(apartment_id);
CREATE INDEX idx_water_meters_household ON water_meters(household_id);
CREATE INDEX idx_usage_logs_household_date ON water_usage_logs(household_id, reading_date);
CREATE INDEX idx_invoices_cycle ON invoices(billing_cycle_id);
CREATE INDEX idx_invoices_household ON invoices(household_id);
CREATE INDEX idx_alerts_household_status ON alerts(household_id, status);
CREATE INDEX idx_billing_cycles_apartment ON billing_cycles(apartment_id, status);
