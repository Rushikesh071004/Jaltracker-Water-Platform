package com.waterbill.config;

import com.waterbill.entity.*;
import com.waterbill.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final ApartmentRepository apartmentRepository;
    private final HouseholdRepository householdRepository;
    private final WaterMeterRepository waterMeterRepository;
    private final UserRepository userRepository;
    private final TariffPlanRepository tariffPlanRepository;
    private final BillingCycleRepository billingCycleRepository;
    private final BulkWaterPurchaseRepository bulkWaterPurchaseRepository;
    private final WaterUsageLogRepository usageLogRepository;
    private final InvoiceRepository invoiceRepository;
    private final AlertRepository alertRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(
            ApartmentRepository apartmentRepository,
            HouseholdRepository householdRepository,
            WaterMeterRepository waterMeterRepository,
            UserRepository userRepository,
            TariffPlanRepository tariffPlanRepository,
            BillingCycleRepository billingCycleRepository,
            BulkWaterPurchaseRepository bulkWaterPurchaseRepository,
            WaterUsageLogRepository usageLogRepository,
            InvoiceRepository invoiceRepository,
            AlertRepository alertRepository,
            PasswordEncoder passwordEncoder) {
        this.apartmentRepository = apartmentRepository;
        this.householdRepository = householdRepository;
        this.waterMeterRepository = waterMeterRepository;
        this.userRepository = userRepository;
        this.tariffPlanRepository = tariffPlanRepository;
        this.billingCycleRepository = billingCycleRepository;
        this.bulkWaterPurchaseRepository = bulkWaterPurchaseRepository;
        this.usageLogRepository = usageLogRepository;
        this.invoiceRepository = invoiceRepository;
        this.alertRepository = alertRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (apartmentRepository.count() > 0 && userRepository.count() > 0) {
            log.info("Database already seeded. Verifying default user accounts...");
            ensureDefaultUserPasswords();
            return;
        }

        log.info("Seeding initial water billing demonstration data...");

        // 1. Apartment
        Apartment apartment = new Apartment(
                "Green Heights Residences",
                "108 Palm Avenue, Eco District, Metro City",
                7,
                "admin@greenheights.com",
                "+1-555-0199"
        );
        apartment = apartmentRepository.save(apartment);

        // 2. Households (Metered + Unmetered)
        Household h1 = householdRepository.save(new Household(apartment, "A-101", "Wing A", 1, BigDecimal.valueOf(1200.00), 3, true, BigDecimal.valueOf(1.500)));
        Household h2 = householdRepository.save(new Household(apartment, "A-102", "Wing A", 1, BigDecimal.valueOf(1500.00), 4, true, BigDecimal.valueOf(2.000)));
        Household h3 = householdRepository.save(new Household(apartment, "A-103", "Wing A", 1, BigDecimal.valueOf(950.00), 2, true, BigDecimal.valueOf(1.200)));
        Household h4 = householdRepository.save(new Household(apartment, "B-201", "Wing B", 2, BigDecimal.valueOf(1250.00), 3, true, BigDecimal.valueOf(1.600)));
        Household h5 = householdRepository.save(new Household(apartment, "B-202", "Wing B", 2, BigDecimal.valueOf(1800.00), 5, true, BigDecimal.valueOf(2.400)));
        Household h6 = householdRepository.save(new Household(apartment, "C-301", "Wing C", 3, BigDecimal.valueOf(850.00), 2, false, BigDecimal.valueOf(1.100)));
        Household h7 = householdRepository.save(new Household(apartment, "C-302", "Wing C", 3, BigDecimal.valueOf(1400.00), 4, false, BigDecimal.valueOf(1.800)));

        // 3. Water Meters
        WaterMeter m1 = waterMeterRepository.save(new WaterMeter("WM-GH-A101", h1, LocalDate.of(2026, 1, 1), BigDecimal.valueOf(100.000), MeterStatus.ACTIVE, MeterType.DIGITAL));
        WaterMeter m2 = waterMeterRepository.save(new WaterMeter("WM-GH-A102", h2, LocalDate.of(2026, 1, 1), BigDecimal.valueOf(120.000), MeterStatus.ACTIVE, MeterType.DIGITAL));
        WaterMeter m3 = waterMeterRepository.save(new WaterMeter("WM-GH-A103", h3, LocalDate.of(2026, 1, 1), BigDecimal.valueOf(80.000), MeterStatus.ACTIVE, MeterType.DIGITAL));
        WaterMeter m4 = waterMeterRepository.save(new WaterMeter("WM-GH-B201", h4, LocalDate.of(2026, 1, 1), BigDecimal.valueOf(150.000), MeterStatus.ACTIVE, MeterType.DIGITAL));
        WaterMeter m5 = waterMeterRepository.save(new WaterMeter("WM-GH-B202", h5, LocalDate.of(2026, 1, 1), BigDecimal.valueOf(200.000), MeterStatus.ACTIVE, MeterType.DIGITAL));

        h1.setWaterMeter(m1);
        h2.setWaterMeter(m2);
        h3.setWaterMeter(m3);
        h4.setWaterMeter(m4);
        h5.setWaterMeter(m5);

        // 4. Users (Admin + Residents)
        User initAdmin = new User(apartment, null, "Rushikesh Waghmare (Society Admin)", "admin@waterbill.com", passwordEncoder.encode("Admin@123"), "+91 98201 11223", Role.ROLE_ADMIN);
        initAdmin.setUsername("admin_seed");
        userRepository.save(initAdmin);

        User initRes1 = new User(apartment, h1, "Rushikesh Waghmare", "resident1@waterbill.com", passwordEncoder.encode("Resident@123"), "+91 98201 44556", Role.ROLE_RESIDENT);
        initRes1.setUsername("rushikesh_seed");
        userRepository.save(initRes1);

        User initRes2 = new User(apartment, h2, "Rushikesh Waghmare", "resident2@waterbill.com", passwordEncoder.encode("Resident@123"), "+91 98201 77889", Role.ROLE_RESIDENT);
        initRes2.setUsername("resident2_seed");
        userRepository.save(initRes2);

        User initRes3 = new User(apartment, h6, "Rushikesh Waghmare", "resident3@waterbill.com", passwordEncoder.encode("Resident@123"), "+91 98201 99001", Role.ROLE_RESIDENT);
        initRes3.setUsername("resident3_seed");
        userRepository.save(initRes3);

        // 5. Tariff Plan & Tiers
        TariffPlan plan = new TariffPlan(apartment, "Standard Residential Tiered Tariff 2026",
                "Progressive tiered tariff scheme to promote water conservation.", LocalDate.of(2026, 1, 1), true);
        plan.addTier(new TariffTier(plan, "Tier 1 - Essential Life Line", BigDecimal.valueOf(0.000), BigDecimal.valueOf(10.000), BigDecimal.valueOf(2.50), 1));
        plan.addTier(new TariffTier(plan, "Tier 2 - Normal Residential", BigDecimal.valueOf(10.000), BigDecimal.valueOf(20.000), BigDecimal.valueOf(4.00), 2));
        plan.addTier(new TariffTier(plan, "Tier 3 - High Consumption", BigDecimal.valueOf(20.000), BigDecimal.valueOf(35.000), BigDecimal.valueOf(6.50), 3));
        plan.addTier(new TariffTier(plan, "Tier 4 - Excessive Surcharge", BigDecimal.valueOf(35.000), null, BigDecimal.valueOf(10.00), 4));
        tariffPlanRepository.save(plan);

        // 6. Billing Cycles
        BillingCycle cycle1 = new BillingCycle(apartment, "Cycle 2026-09 (September)", LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30), LocalDate.of(2026, 10, 15));
        cycle1.setStatus(CycleStatus.FINALIZED);
        cycle1.setTotalBulkWaterCost(BigDecimal.valueOf(850.00));
        cycle1.setTotalCommonAreaVolumeKl(BigDecimal.valueOf(45.000));
        cycle1 = billingCycleRepository.save(cycle1);

        BillingCycle cycle2 = new BillingCycle(apartment, "Cycle 2026-10 (October Active)", LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31), LocalDate.of(2026, 11, 15));
        cycle2.setStatus(CycleStatus.OPEN);
        cycle2.setTotalBulkWaterCost(BigDecimal.valueOf(920.00));
        cycle2.setTotalCommonAreaVolumeKl(BigDecimal.valueOf(52.000));
        cycle2 = billingCycleRepository.save(cycle2);

        // 7. Bulk Water Purchases
        bulkWaterPurchaseRepository.save(new BulkWaterPurchase(cycle1, apartment, LocalDate.of(2026, 9, 8), "City Water Tankers Ltd", PurchaseType.TANKER, BigDecimal.valueOf(30.000), BigDecimal.valueOf(15.00), "TNK-2026-09-08", "Scheduled dry spell supplement"));
        bulkWaterPurchaseRepository.save(new BulkWaterPurchase(cycle1, apartment, LocalDate.of(2026, 9, 22), "Municipal Corporation Bulk", PurchaseType.MUNICIPAL, BigDecimal.valueOf(50.000), BigDecimal.valueOf(8.00), "MCB-2026-09-22", "Regular monthly municipal bulk quota"));
        bulkWaterPurchaseRepository.save(new BulkWaterPurchase(cycle2, apartment, LocalDate.of(2026, 10, 4), "City Water Tankers Ltd", PurchaseType.TANKER, BigDecimal.valueOf(40.000), BigDecimal.valueOf(15.50), "TNK-2026-10-04", "Lobby & Garden reserve refill"));
        bulkWaterPurchaseRepository.save(new BulkWaterPurchase(cycle2, apartment, LocalDate.of(2026, 10, 11), "Municipal Corporation Bulk", PurchaseType.MUNICIPAL, BigDecimal.valueOf(30.000), BigDecimal.valueOf(10.00), "MCB-2026-10-11", "Mid-cycle municipal feed"));

        // 8. Historical Daily Usage Logs for Household 1 (Alex Morgan, A-101)
        BigDecimal currentReading = BigDecimal.valueOf(100.000);
        LocalDate start = LocalDate.of(2026, 9, 1);
        for (int i = 0; i < 44; i++) {
            LocalDate date = start.plusDays(i);
            BigDecimal dailyUsage;
            if (date.equals(LocalDate.of(2026, 10, 11))) {
                // Outlier Spike > 2σ
                dailyUsage = BigDecimal.valueOf(2.900);
            } else if (date.getDayOfWeek().getValue() >= 6) {
                dailyUsage = BigDecimal.valueOf(0.620);
            } else {
                dailyUsage = BigDecimal.valueOf(0.520);
            }
            currentReading = currentReading.add(dailyUsage);
            usageLogRepository.save(new WaterUsageLog(h1, m1, date, currentReading, dailyUsage, LogSource.MANUAL_ENTRY, "Daily log"));
        }
        m1.setLastReadingValue(currentReading);
        m1.setLastReadingDate(LocalDate.of(2026, 10, 14));
        waterMeterRepository.save(m1);

        // Daily logs for A-102
        BigDecimal m2Reading = BigDecimal.valueOf(120.000);
        for (int i = 0; i < 44; i++) {
            LocalDate date = start.plusDays(i);
            BigDecimal dailyUsage = BigDecimal.valueOf(0.920);
            m2Reading = m2Reading.add(dailyUsage);
            usageLogRepository.save(new WaterUsageLog(h2, m2, date, m2Reading, dailyUsage, LogSource.MANUAL_ENTRY, "Daily log"));
        }
        m2.setLastReadingValue(m2Reading);
        m2.setLastReadingDate(LocalDate.of(2026, 10, 14));
        waterMeterRepository.save(m2);

        // 9. September Finalized Invoices
        invoiceRepository.save(new Invoice(cycle1, h1, "INV-2026-09-001", BigDecimal.valueOf(16.420), BigDecimal.valueOf(50.68), BigDecimal.valueOf(128.40), BigDecimal.valueOf(179.08)));
        invoiceRepository.save(new Invoice(cycle1, h2, "INV-2026-09-002", BigDecimal.valueOf(27.600), BigDecimal.valueOf(114.40), BigDecimal.valueOf(178.33), BigDecimal.valueOf(292.73)));
        invoiceRepository.save(new Invoice(cycle1, h3, "INV-2026-09-003", BigDecimal.valueOf(11.200), BigDecimal.valueOf(29.80), BigDecimal.valueOf(87.58), BigDecimal.valueOf(117.38)));
        invoiceRepository.save(new Invoice(cycle1, h4, "INV-2026-09-004", BigDecimal.valueOf(18.500), BigDecimal.valueOf(59.00), BigDecimal.valueOf(144.68), BigDecimal.valueOf(203.68)));
        invoiceRepository.save(new Invoice(cycle1, h5, "INV-2026-09-005", BigDecimal.valueOf(28.600), BigDecimal.valueOf(120.90), BigDecimal.valueOf(223.65), BigDecimal.valueOf(344.55)));
        invoiceRepository.save(new Invoice(cycle1, h6, "INV-2026-09-006", BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.valueOf(98.46), BigDecimal.valueOf(98.46)));
        invoiceRepository.save(new Invoice(cycle1, h7, "INV-2026-09-007", BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.valueOf(162.18), BigDecimal.valueOf(162.18)));

        // 10. Sample Alerts
        alertRepository.save(new Alert(h1, AlertType.LEAK_DETECTED_2SIGMA, AlertSeverity.CRITICAL,
                "Potential Water Leak Detected in A-101",
                "Water usage on 2026-10-11 was 2.900 kL, which is 4.31 standard deviations above your 30-day average (0.547 kL). Please inspect bathroom fixtures immediately.",
                LocalDate.of(2026, 10, 11), BigDecimal.valueOf(2.900), BigDecimal.valueOf(1.500), BigDecimal.valueOf(4.31)));

        alertRepository.save(new Alert(h5, AlertType.OVERUSE_THRESHOLD, AlertSeverity.WARNING,
                "Daily Usage Limit Exceeded in B-202",
                "Water consumption reached 2.850 kL, exceeding your set threshold of 2.400 kL.",
                LocalDate.of(2026, 10, 12), BigDecimal.valueOf(2.850), BigDecimal.valueOf(2.400), null));

        log.info("Demo data seeding completed successfully! Admin: admin@waterbill.com (Admin@123), Resident: resident1@waterbill.com (Resident@123)");
    }

    private void ensureDefaultUserPasswords() {
        Apartment apt = apartmentRepository.findById(1L).orElse(null);
        if (apt != null) {
            apt.setName("Shanti Niketan Heights");
            apt.setAddress("Tower B, Link Road, Andheri West, Mumbai, Maharashtra 400053");
            apt.setContactEmail("admin@shantiniketan.in");
            apt.setContactPhone("+91 98201 23456");
            apartmentRepository.save(apt);
        }

        Household h1 = householdRepository.findById(1L).orElse(null);
        Household h2 = householdRepository.findById(2L).orElse(null);
        Household h3 = householdRepository.findById(3L).orElse(null);

        // 1. Superadmin
        userRepository.findByEmail("superadmin@jaltracker.com").ifPresentOrElse(
                sa -> {
                    sa.setFullName("Rushikesh Waghmare (Superadmin)");
                    sa.setUsername("superadmin");
                    sa.setPasswordHash(passwordEncoder.encode("Superadmin@123"));
                    sa.setRole(Role.ROLE_SUPERADMIN);
                    sa.setAccountStatus(AccountStatus.VERIFIED);
                    userRepository.save(sa);
                },
                () -> {
                    User sa = new User(apt, null, "Rushikesh Waghmare (Superadmin)", "superadmin@jaltracker.com",
                            passwordEncoder.encode("Superadmin@123"), "+91 98200 00001", Role.ROLE_SUPERADMIN);
                    sa.setUsername("superadmin");
                    sa.setAccountStatus(AccountStatus.VERIFIED);
                    userRepository.save(sa);
                }
        );

        // 2. Society Admin
        userRepository.findByEmail("admin@waterbill.com").ifPresent(admin -> {
            admin.setFullName("Rushikesh Waghmare (Society Admin)");
            admin.setUsername("admin_legacy");
            admin.setPhone("+91 98201 11223");
            admin.setPasswordHash(passwordEncoder.encode("Admin@123"));
            admin.setAccountStatus(AccountStatus.VERIFIED);
            userRepository.save(admin);
        });

        userRepository.findByEmail("admin@jaltracker.com").ifPresentOrElse(
                admin -> {
                    admin.setFullName("Rushikesh Waghmare (Society Admin)");
                    admin.setUsername("admin");
                    admin.setPasswordHash(passwordEncoder.encode("Admin@123"));
                    admin.setRole(Role.ROLE_ADMIN);
                    admin.setAccountStatus(AccountStatus.VERIFIED);
                    userRepository.save(admin);
                },
                () -> {
                    User admin = new User(apt, null, "Rushikesh Waghmare (Society Admin)", "admin@jaltracker.com",
                            passwordEncoder.encode("Admin@123"), "+91 98201 11223", Role.ROLE_ADMIN);
                    admin.setUsername("admin");
                    admin.setAccountStatus(AccountStatus.VERIFIED);
                    userRepository.save(admin);
                }
        );

        // 3. Resident 1 (Unit A-101) - Primary Resident Account
        userRepository.findByEmail("resident1@waterbill.com").ifPresent(resident -> {
            resident.setFullName("Rushikesh Waghmare");
            resident.setUsername("resident1");
            resident.setPhone("+91 98201 44556");
            resident.setPasswordHash(passwordEncoder.encode("Resident@123"));
            resident.setAccountStatus(AccountStatus.VERIFIED);
            userRepository.save(resident);
        });

        userRepository.findByEmail("resident1@jaltracker.com").ifPresentOrElse(
                resident -> {
                    resident.setFullName("Rushikesh Waghmare");
                    resident.setUsername("rushikesh");
                    resident.setPasswordHash(passwordEncoder.encode("Resident@123"));
                    resident.setRole(Role.ROLE_RESIDENT);
                    resident.setAccountStatus(AccountStatus.VERIFIED);
                    userRepository.save(resident);
                },
                () -> {
                    User res1 = new User(apt, h1, "Rushikesh Waghmare", "resident1@jaltracker.com",
                            passwordEncoder.encode("Resident@123"), "+91 98201 44556", Role.ROLE_RESIDENT);
                    res1.setUsername("rushikesh");
                    res1.setAccountStatus(AccountStatus.VERIFIED);
                    userRepository.save(res1);
                }
        );

        // Also ensure rushikesh@jaltracker.com exists with username 'rushikesh'
        if (!userRepository.existsByEmail("rushikesh@jaltracker.com")) {
            User rUser = new User(apt, h1, "Rushikesh Waghmare", "rushikesh@jaltracker.com",
                    passwordEncoder.encode("Resident@123"), "+91 98201 44556", Role.ROLE_RESIDENT);
            rUser.setUsername("rushikesh_primary");
            rUser.setAccountStatus(AccountStatus.VERIFIED);
            userRepository.save(rUser);
        }

        // 4. Resident 2 (Unit A-102)
        userRepository.findByEmail("resident2@waterbill.com").ifPresent(resident -> {
            resident.setFullName("Rushikesh Waghmare");
            resident.setUsername("resident2_legacy");
            resident.setPhone("+91 98201 77889");
            resident.setPasswordHash(passwordEncoder.encode("Resident@123"));
            resident.setAccountStatus(AccountStatus.VERIFIED);
            userRepository.save(resident);
        });

        userRepository.findByEmail("resident2@jaltracker.com").ifPresentOrElse(
                resident -> {
                    resident.setFullName("Rushikesh Waghmare");
                    resident.setUsername("resident2");
                    resident.setPasswordHash(passwordEncoder.encode("Resident@123"));
                    resident.setRole(Role.ROLE_RESIDENT);
                    resident.setAccountStatus(AccountStatus.VERIFIED);
                    userRepository.save(resident);
                },
                () -> {
                    User res2 = new User(apt, h2, "Rushikesh Waghmare", "resident2@jaltracker.com",
                            passwordEncoder.encode("Resident@123"), "+91 98201 77889", Role.ROLE_RESIDENT);
                    res2.setUsername("resident2");
                    res2.setAccountStatus(AccountStatus.VERIFIED);
                    userRepository.save(res2);
                }
        );

        // 5. Resident 3 (Unit A-103)
        userRepository.findByEmail("resident3@waterbill.com").ifPresent(resident -> {
            resident.setFullName("Rushikesh Waghmare");
            resident.setUsername("resident3_legacy");
            resident.setPhone("+91 98201 99001");
            resident.setPasswordHash(passwordEncoder.encode("Resident@123"));
            resident.setAccountStatus(AccountStatus.VERIFIED);
            userRepository.save(resident);
        });

        userRepository.findByEmail("resident3@jaltracker.com").ifPresentOrElse(
                resident -> {
                    resident.setFullName("Rushikesh Waghmare");
                    resident.setUsername("resident3");
                    resident.setPasswordHash(passwordEncoder.encode("Resident@123"));
                    resident.setRole(Role.ROLE_RESIDENT);
                    resident.setAccountStatus(AccountStatus.VERIFIED);
                    userRepository.save(resident);
                },
                () -> {
                    User res3 = new User(apt, h3, "Rushikesh Waghmare", "resident3@jaltracker.com",
                            passwordEncoder.encode("Resident@123"), "+91 98201 99001", Role.ROLE_RESIDENT);
                    res3.setUsername("resident3");
                    res3.setAccountStatus(AccountStatus.VERIFIED);
                    userRepository.save(res3);
                }
        );

        // 6. Pending Resident (for testing admin/superadmin approval workflow)
        if (!userRepository.existsByEmail("sunita.pending@jaltracker.com")) {
            User pRes = new User(apt, h3, "Rushikesh Waghmare (Applicant)", "sunita.pending@jaltracker.com",
                    passwordEncoder.encode("Resident@123"), "+91 98202 33445", Role.ROLE_RESIDENT);
            pRes.setUsername("applicant.resident");
            pRes.setAccountStatus(AccountStatus.PENDING);
            userRepository.save(pRes);
        }

        // 7. Pending Admin (for testing superadmin approval workflow)
        if (!userRepository.existsByEmail("deepak.admin@jaltracker.com")) {
            User pAdm = new User(apt, null, "Rushikesh Waghmare (Admin Applicant)", "deepak.admin@jaltracker.com",
                    passwordEncoder.encode("Admin@123"), "+91 98203 55667", Role.ROLE_ADMIN);
            pAdm.setUsername("applicant.admin");
            pAdm.setAccountStatus(AccountStatus.PENDING);
            userRepository.save(pAdm);
        }

        log.info("Default user accounts verified for Rushikesh Waghmare: superadmin (Superadmin@123), admin (Admin@123), rushikesh (Resident@123), resident2 (Resident@123), resident3 (Resident@123)");
        ensureMeteredUsageDataSeeded();
    }

    private void ensureMeteredUsageDataSeeded() {
        Apartment apt = apartmentRepository.findById(1L).orElse(null);
        if (apt == null) return;

        List<Household> households = householdRepository.findByApartmentId(apt.getId());
        if (households.isEmpty()) return;

        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(29);

        // Check if any usage logs exist in the last 30 days
        List<WaterUsageLog> recentLogs = usageLogRepository.findByApartmentAndDateRange(apt.getId(), startDate, today);
        if (recentLogs != null && recentLogs.size() >= 20) {
            log.info("Volumetric water usage logs already exist in database (count: {}).", recentLogs.size());
            return;
        }

        log.info("Seeding realistic volumetric water usage readings for all households in {}...", apt.getName());

        for (Household h : households) {
            WaterMeter meter = h.getWaterMeter();
            if (meter == null) {
                meter = waterMeterRepository.findByHouseholdId(h.getId()).orElseGet(() -> {
                    String serial = "WM-MH-" + h.getFlatNumber().replaceAll("[^a-zA-Z0-9]", "");
                    WaterMeter newMeter = new WaterMeter(serial, h, LocalDate.of(2026, 1, 1),
                            BigDecimal.valueOf(100.000), MeterStatus.ACTIVE, MeterType.DIGITAL);
                    return waterMeterRepository.save(newMeter);
                });
                h.setWaterMeter(meter);
                h.setIsMetered(true);
                householdRepository.save(h);
            }

            BigDecimal targetVolume = switch (h.getFlatNumber().trim().toUpperCase()) {
                case "A-101" -> BigDecimal.valueOf(16.420);
                case "A-102" -> BigDecimal.valueOf(27.600);
                case "A-103" -> BigDecimal.valueOf(11.200);
                case "B-201" -> BigDecimal.valueOf(18.500);
                case "B-202" -> BigDecimal.valueOf(28.600);
                case "C-301" -> BigDecimal.valueOf(9.800);
                case "C-302" -> BigDecimal.valueOf(14.500);
                default -> BigDecimal.valueOf(15.200);
            };

            BigDecimal dailyAvg = targetVolume.divide(BigDecimal.valueOf(30), 3, java.math.RoundingMode.HALF_UP);
            BigDecimal runningReading = meter.getLastReadingValue() != null ? meter.getLastReadingValue() : BigDecimal.valueOf(100.0);

            for (int i = 0; i < 30; i++) {
                LocalDate date = startDate.plusDays(i);
                if (!usageLogRepository.existsByHouseholdIdAndReadingDate(h.getId(), date)) {
                    BigDecimal dailyUsage = (i % 7 == 5 || i % 7 == 6)
                            ? dailyAvg.multiply(BigDecimal.valueOf(1.18))
                            : dailyAvg.multiply(BigDecimal.valueOf(0.95));
                    runningReading = runningReading.add(dailyUsage);
                    usageLogRepository.save(new WaterUsageLog(
                            h, meter, date, runningReading, dailyUsage,
                            LogSource.MANUAL_ENTRY, "Standard metered consumption"
                    ));
                }
            }

            meter.setLastReadingValue(runningReading);
            meter.setLastReadingDate(today);
            waterMeterRepository.save(meter);
        }
        log.info("Volumetric meter readings seeded successfully for all units (Rushikesh Waghmare residents).");
    }
}
