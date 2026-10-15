-- ====================================================================
-- Flyway Migration V2: Comprehensive Seed Data for Water Billing Platform
-- ====================================================================

-- 1. Apartment
INSERT INTO apartments (id, name, address, total_flats, contact_email, contact_phone, created_at, updated_at)
VALUES (1, 'Green Heights Residences', '108 Palm Avenue, Eco District, Metro City', 7, 'admin@greenheights.com', '+1-555-0199', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 2. Households (mix of metered & unmetered units for testing distribution algorithm)
INSERT INTO households (id, apartment_id, flat_number, block_or_wing, floor_number, area_sqft, number_of_occupants, is_metered, daily_usage_threshold_kl)
VALUES
(1, 1, 'A-101', 'Wing A', 1, 1200.00, 3, TRUE, 1.500),
(2, 1, 'A-102', 'Wing A', 1, 1500.00, 4, TRUE, 2.000),
(3, 1, 'A-103', 'Wing A', 1, 950.00, 2, TRUE, 1.200),
(4, 1, 'B-201', 'Wing B', 2, 1250.00, 3, TRUE, 1.600),
(5, 1, 'B-202', 'Wing B', 2, 1800.00, 5, TRUE, 2.400),
(6, 1, 'C-301', 'Wing C', 3, 850.00, 2, FALSE, 1.100), -- Unmetered: flat area share
(7, 1, 'C-302', 'Wing C', 3, 1400.00, 4, FALSE, 1.800); -- Unmetered: flat area share

-- 3. Water Meters
INSERT INTO water_meters (id, meter_serial_number, household_id, installation_date, initial_reading, status, meter_type, last_reading_value, last_reading_date)
VALUES
(1, 'WM-GH-A101', 1, '2026-01-01', 100.000, 'ACTIVE', 'DIGITAL', 145.250, '2026-10-14'),
(2, 'WM-GH-A102', 2, '2026-01-01', 120.000, 'ACTIVE', 'DIGITAL', 182.400, '2026-10-14'),
(3, 'WM-GH-A103', 3, '2026-01-01', 80.000,  'ACTIVE', 'DIGITAL', 112.100, '2026-10-14'),
(4, 'WM-GH-B201', 4, '2026-01-01', 150.000, 'ACTIVE', 'DIGITAL', 211.800, '2026-10-14'),
(5, 'WM-GH-B202', 5, '2026-01-01', 200.000, 'ACTIVE', 'DIGITAL', 298.600, '2026-10-14');

-- 4. Initial Users (Passwords will be verified / initialized with BCrypt encoded 'Admin@123' & 'Resident@123')
INSERT INTO users (id, apartment_id, household_id, full_name, email, password_hash, phone, role, is_active)
VALUES
(1, 1, NULL, 'Sarah Jenkins (Property Admin)', 'admin@waterbill.com', '$2a$10$7Ez4k8sQ3tNf7Ld7fU6DDeR1s4YvK9O5rQ8u0eM5z2bC8L4Y8y7uK', '+1-555-0101', 'ROLE_ADMIN', TRUE),
(2, 1, 1, 'Alex Morgan', 'resident1@waterbill.com', '$2a$10$7Ez4k8sQ3tNf7Ld7fU6DDeR1s4YvK9O5rQ8u0eM5z2bC8L4Y8y7uK', '+1-555-0102', 'ROLE_RESIDENT', TRUE),
(3, 1, 2, 'Priya Sharma', 'resident2@waterbill.com', '$2a$10$7Ez4k8sQ3tNf7Ld7fU6DDeR1s4YvK9O5rQ8u0eM5z2bC8L4Y8y7uK', '+1-555-0103', 'ROLE_RESIDENT', TRUE),
(4, 1, 6, 'David Chen', 'resident3@waterbill.com', '$2a$10$7Ez4k8sQ3tNf7Ld7fU6DDeR1s4YvK9O5rQ8u0eM5z2bC8L4Y8y7uK', '+1-555-0104', 'ROLE_RESIDENT', TRUE);

-- 5. Tariff Plans
INSERT INTO tariff_plans (id, apartment_id, name, description, effective_from, effective_to, is_active)
VALUES
(1, 1, 'Standard Residential Tiered Tariff 2026', 'Progressive tiered tariff scheme to promote water conservation.', '2026-01-01', NULL, TRUE);

-- 6. Tariff Tiers (Base 0-10 kL @ $2.50, Tier 2 10-20 kL @ $4.00, Tier 3 20-35 kL @ $6.50, Tier 4 >35 kL @ $10.00)
INSERT INTO tariff_tiers (id, tariff_plan_id, tier_name, min_volume_kl, max_volume_kl, rate_per_kl, tier_order)
VALUES
(1, 1, 'Tier 1 - Essential Life Line', 0.000, 10.000, 2.50, 1),
(2, 1, 'Tier 2 - Normal Residential', 10.000, 20.000, 4.00, 2),
(3, 1, 'Tier 3 - High Consumption', 20.000, 35.000, 6.50, 3),
(4, 1, 'Tier 4 - Excessive / Surcharge', 35.000, NULL, 10.00, 4);

-- 7. Billing Cycles
INSERT INTO billing_cycles (id, apartment_id, cycle_name, start_date, end_date, due_date, status, total_bulk_water_cost, total_common_area_volume_kl)
VALUES
(1, 1, 'Cycle 2026-09 (September)', '2026-09-01', '2026-09-30', '2026-10-15', 'FINALIZED', 850.00, 45.000),
(2, 1, 'Cycle 2026-10 (October Active)', '2026-10-01', '2026-10-31', '2026-11-15', 'OPEN', 920.00, 52.000);

-- 8. Bulk Water Purchases
INSERT INTO bulk_water_purchases (id, billing_cycle_id, apartment_id, purchase_date, supplier_name, purchase_type, volume_kl, unit_cost_per_kl, total_cost, invoice_reference, notes)
VALUES
(1, 1, 1, '2026-09-08', 'City Water Tankers Ltd', 'TANKER', 30.000, 15.00, 450.00, 'TNK-2026-09-08', 'Scheduled dry spell supplement'),
(2, 1, 1, '2026-09-22', 'Municipal Corporation Bulk', 'MUNICIPAL', 50.000, 8.00, 400.00, 'MCB-2026-09-22', 'Regular monthly municipal bulk quota'),
(3, 2, 1, '2026-10-04', 'City Water Tankers Ltd', 'TANKER', 40.000, 15.50, 620.00, 'TNK-2026-10-04', 'Lobby & Garden reserve refill'),
(4, 2, 1, '2026-10-11', 'Municipal Corporation Bulk', 'MUNICIPAL', 30.000, 10.00, 300.00, 'MCB-2026-10-11', 'Mid-cycle municipal feed');

-- 9. Daily Water Usage Logs for Household 1 (A-101) (covering September and October)
INSERT INTO water_usage_logs (household_id, meter_id, reading_date, current_reading_kl, daily_consumption_kl, log_source, notes)
VALUES
-- September daily entries (stable ~0.45 - 0.65 kL/day)
(1, 1, '2026-09-01', 100.520, 0.520, 'MANUAL_ENTRY', 'Normal usage'),
(1, 1, '2026-09-02', 101.050, 0.530, 'MANUAL_ENTRY', 'Normal usage'),
(1, 1, '2026-09-03', 101.620, 0.570, 'MANUAL_ENTRY', 'Normal usage'),
(1, 1, '2026-09-04', 102.140, 0.520, 'MANUAL_ENTRY', 'Normal usage'),
(1, 1, '2026-09-05', 102.750, 0.610, 'MANUAL_ENTRY', 'Weekend laundry'),
(1, 1, '2026-09-06', 103.390, 0.640, 'MANUAL_ENTRY', 'Weekend'),
(1, 1, '2026-09-07', 103.870, 0.480, 'MANUAL_ENTRY', 'Normal usage'),
(1, 1, '2026-09-08', 104.380, 0.510, 'MANUAL_ENTRY', 'Normal usage'),
(1, 1, '2026-09-09', 104.900, 0.520, 'MANUAL_ENTRY', 'Normal usage'),
(1, 1, '2026-09-10', 105.440, 0.540, 'MANUAL_ENTRY', 'Normal usage'),
(1, 1, '2026-09-11', 105.950, 0.510, 'MANUAL_ENTRY', 'Normal usage'),
(1, 1, '2026-09-12', 106.580, 0.630, 'MANUAL_ENTRY', 'Weekend'),
(1, 1, '2026-09-13', 107.190, 0.610, 'MANUAL_ENTRY', 'Weekend'),
(1, 1, '2026-09-14', 107.680, 0.490, 'MANUAL_ENTRY', 'Normal usage'),
(1, 1, '2026-09-15', 108.200, 0.520, 'MANUAL_ENTRY', 'Normal usage'),
(1, 1, '2026-09-16', 108.710, 0.510, 'MANUAL_ENTRY', 'Normal usage'),
(1, 1, '2026-09-17', 109.250, 0.540, 'MANUAL_ENTRY', 'Normal usage'),
(1, 1, '2026-09-18', 109.780, 0.530, 'MANUAL_ENTRY', 'Normal usage'),
(1, 1, '2026-09-19', 110.420, 0.640, 'MANUAL_ENTRY', 'Weekend'),
(1, 1, '2026-09-20', 111.050, 0.630, 'MANUAL_ENTRY', 'Weekend'),
(1, 1, '2026-09-21', 111.530, 0.480, 'MANUAL_ENTRY', 'Normal usage'),
(1, 1, '2026-09-22', 112.050, 0.520, 'MANUAL_ENTRY', 'Normal usage'),
(1, 1, '2026-09-23', 112.590, 0.540, 'MANUAL_ENTRY', 'Normal usage'),
(1, 1, '2026-09-24', 113.100, 0.510, 'MANUAL_ENTRY', 'Normal usage'),
(1, 1, '2026-09-25', 113.620, 0.520, 'MANUAL_ENTRY', 'Normal usage'),
(1, 1, '2026-09-26', 114.240, 0.620, 'MANUAL_ENTRY', 'Weekend'),
(1, 1, '2026-09-27', 114.860, 0.620, 'MANUAL_ENTRY', 'Weekend'),
(1, 1, '2026-09-28', 115.370, 0.510, 'MANUAL_ENTRY', 'Normal usage'),
(1, 1, '2026-09-29', 115.890, 0.520, 'MANUAL_ENTRY', 'Normal usage'),
(1, 1, '2026-09-30', 116.420, 0.530, 'MANUAL_ENTRY', 'Normal usage'),

-- October entries with a sudden spike (> 2 sigma) on Oct 11 for testing leak detection
(1, 1, '2026-10-01', 116.940, 0.520, 'MANUAL_ENTRY', 'October start'),
(1, 1, '2026-10-02', 117.480, 0.540, 'MANUAL_ENTRY', 'Regular'),
(1, 1, '2026-10-03', 118.110, 0.630, 'MANUAL_ENTRY', 'Weekend'),
(1, 1, '2026-10-04', 118.720, 0.610, 'MANUAL_ENTRY', 'Weekend'),
(1, 1, '2026-10-05', 119.230, 0.510, 'MANUAL_ENTRY', 'Regular'),
(1, 1, '2026-10-06', 119.750, 0.520, 'MANUAL_ENTRY', 'Regular'),
(1, 1, '2026-10-07', 120.280, 0.530, 'MANUAL_ENTRY', 'Regular'),
(1, 1, '2026-10-08', 120.800, 0.520, 'MANUAL_ENTRY', 'Regular'),
(1, 1, '2026-10-09', 121.320, 0.520, 'MANUAL_ENTRY', 'Regular'),
(1, 1, '2026-10-10', 121.950, 0.630, 'MANUAL_ENTRY', 'Weekend'),
(1, 1, '2026-10-11', 124.850, 2.900, 'MANUAL_ENTRY', 'High usage spike - Potential bathroom cistern leak!'),
(1, 1, '2026-10-12', 125.400, 0.550, 'MANUAL_ENTRY', 'Fixed cistern valve'),
(1, 1, '2026-10-13', 125.920, 0.520, 'MANUAL_ENTRY', 'Normal usage'),
(1, 1, '2026-10-14', 126.440, 0.520, 'MANUAL_ENTRY', 'Normal usage');

-- Additional logs for Household 2 (A-102) for comparison chart
INSERT INTO water_usage_logs (household_id, meter_id, reading_date, current_reading_kl, daily_consumption_kl, log_source, notes)
VALUES
(2, 2, '2026-10-10', 178.600, 0.900, 'MANUAL_ENTRY', 'Regular'),
(2, 2, '2026-10-11', 179.550, 0.950, 'MANUAL_ENTRY', 'Regular'),
(2, 2, '2026-10-12', 180.450, 0.900, 'MANUAL_ENTRY', 'Regular'),
(2, 2, '2026-10-13', 181.420, 0.970, 'MANUAL_ENTRY', 'Regular'),
(2, 2, '2026-10-14', 182.400, 0.980, 'MANUAL_ENTRY', 'Regular');

-- 10. September Invoices (Finalized Cycle 1)
INSERT INTO invoices (id, billing_cycle_id, household_id, invoice_number, metered_consumption_kl, metered_charge, shared_common_charge, total_amount, payment_status, payment_date, notes)
VALUES
(1, 1, 1, 'INV-2026-09-001', 16.420, 50.68, 128.40, 179.08, 'PAID', '2026-10-05 14:30:00+00', 'Paid via UPI'),
(2, 1, 2, 'INV-2026-09-002', 22.800, 83.20, 178.33, 261.53, 'PAID', '2026-10-06 10:15:00+00', 'Paid via NetBanking'),
(3, 1, 3, 'INV-2026-09-003', 11.200, 29.80, 87.58, 117.38, 'PENDING', NULL, 'Awaiting payment'),
(4, 1, 4, 'INV-2026-09-004', 18.500, 59.00, 144.68, 203.68, 'PAID', '2026-10-08 09:20:00+00', 'Paid via Card'),
(5, 1, 5, 'INV-2026-09-005', 28.600, 120.90, 223.65, 344.55, 'OVERDUE', NULL, 'Reminder sent'),
(6, 1, 6, 'INV-2026-09-006', 0.000, 0.00, 98.46, 98.46, 'PAID', '2026-10-04 11:00:00+00', 'Unmetered unit flat area share'),
(7, 1, 7, 'INV-2026-09-007', 0.000, 0.00, 162.18, 162.18, 'PENDING', NULL, 'Unmetered unit flat area share');

-- 11. Sample Alerts
INSERT INTO alerts (id, household_id, alert_type, severity, title, message, reading_date, consumption_kl, threshold_kl, sigma_value, status)
VALUES
(1, 1, 'LEAK_DETECTED_2SIGMA', 'CRITICAL', 'Potential Water Leak Detected in A-101', 'Water usage on 2026-10-11 was 2.900 kL, which is 4.31 standard deviations above your 30-day average (0.547 kL). Please check toilet valves, pipes, or running faucets immediately.', '2026-10-11', 2.900, 1.500, 4.31, 'ACKNOWLEDGED'),
(2, 5, 'OVERUSE_THRESHOLD', 'WARNING', 'Daily Usage Limit Exceeded in B-202', 'Water consumption reached 2.850 kL, exceeding your set threshold of 2.400 kL.', '2026-10-12', 2.850, 2.400, NULL, 'ACTIVE');

-- Set sequence values to prevent primary key collision upon future inserts
SELECT setval('apartments_id_seq', (SELECT COALESCE(MAX(id), 1) FROM apartments));
SELECT setval('households_id_seq', (SELECT COALESCE(MAX(id), 1) FROM households));
SELECT setval('water_meters_id_seq', (SELECT COALESCE(MAX(id), 1) FROM water_meters));
SELECT setval('users_id_seq', (SELECT COALESCE(MAX(id), 1) FROM users));
SELECT setval('tariff_plans_id_seq', (SELECT COALESCE(MAX(id), 1) FROM tariff_plans));
SELECT setval('tariff_tiers_id_seq', (SELECT COALESCE(MAX(id), 1) FROM tariff_tiers));
SELECT setval('billing_cycles_id_seq', (SELECT COALESCE(MAX(id), 1) FROM billing_cycles));
SELECT setval('bulk_water_purchases_id_seq', (SELECT COALESCE(MAX(id), 1) FROM bulk_water_purchases));
SELECT setval('water_usage_logs_id_seq', (SELECT COALESCE(MAX(id), 1) FROM water_usage_logs));
SELECT setval('invoices_id_seq', (SELECT COALESCE(MAX(id), 1) FROM invoices));
SELECT setval('alerts_id_seq', (SELECT COALESCE(MAX(id), 1) FROM alerts));
