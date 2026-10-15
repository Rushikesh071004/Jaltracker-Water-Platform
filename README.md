# AquaFlow - Web-Based Water Consumption & Billing Management Platform

AquaFlow is an enterprise full-stack platform designed for modern apartment complexes, housing societies, and commercial real estate to measure, monitor, and bill water consumption. It features automated tiered tariff calculations, consumption-based proportional distribution for shared water costs (with flat area fallback for unmetered units), statistical 2-sigma anomaly & leak detection, downloadable PDF invoice generation, and responsive React dashboards for both property administrators and residents.

---

## 🌟 Key Architecture & Engine Features

### 1. Database Schema & Flyway Migrations (Prompt 1)
- **PostgreSQL 16** relational schema managed through **Flyway** migrations (`V1__init_schema.sql` and `V2__seed_demo_data.sql`).
- Relational JPA entities with cascade rules and constraints:
  - `apartments`: Complex metadata, address, contact details.
  - `households`: Units with area (sqft), occupancy, metering flag (`is_metered`), and daily limits.
  - `water_meters`: Meter hardware serial number, digital/analog types, last reading values.
  - `users`: Role-based authentication (`ROLE_ADMIN`, `ROLE_RESIDENT`).
  - `tariff_plans` & `tariff_tiers`: Dynamic rate configuration with unbounded upper tiers.
  - `billing_cycles`: Lifecycle management (`OPEN`, `FINALIZED`, `ARCHIVED`).
  - `bulk_water_purchases`: Tanker deliveries and municipal bulk supplies.
  - `water_usage_logs`: Manual entries and bulk CSV uploads with duplicate prevention.
  - `invoices`: Household statements, metered charges, shared cost allocations, payment status.
  - `alerts`: Threshold exceedance and 2-sigma statistical leak warnings.

### 2. Spring Security 6 & JWT Authentication (Prompt 1)
- Stateless authentication with **Spring Security 6** and **JJWT 0.12.x** (HMAC-SHA256).
- Role-based authorization (`ROLE_ADMIN`, `ROLE_RESIDENT`).
- Password encryption using **BCrypt** (10 salt rounds).
- CORS configuration supporting decoupled React frontend and proxy environments.

### 3. Core Usage & Billing Engines (Prompt 2)
- **Multi-Tiered Tariff Engine (`TariffBillingService`)**: Computes progressive consumption charges step-by-step (e.g. 0-10 kL @ $2.50, 10-20 kL @ $4.00, 20-35 kL @ $6.50, >35 kL @ $10.00) with detailed tier breakdown receipts.
- **Consumption-Based Charge Distribution Algorithm (`ChargeDistributionService`)**:
  - Apportions total shared apartment water costs (tanker deliveries + common area water) proportionally by metered consumption for metered units.
  - Intelligently falls back to flat area (sqft) proportional distribution for unmetered units.
  - Supports hybrid buildings, 100% metered, and 100% unmetered scenarios with penny-accurate rounding.
- **Water Usage Logging Engine (`WaterUsageService`)**:
  - Manual daily entries with auto-consumption deduction from previous readings.
  - Bulk CSV parser using **OpenCSV** with duplicate detection and row-level diagnostic reports.

### 4. Scheduled Alerts & Document Generation (Prompt 3)
- **Statistical Leak Detection Engine (`AlertEngineService`)**:
  - Nightly `@Scheduled` cron job and real-time triggers.
  - Computes baseline 30-day mean ($\mu$) and standard deviation ($\sigma$).
  - Flags statistical anomalies ($> \mu + 2\sigma$) as potential leaks (`LEAK_DETECTED_2SIGMA`).
- **JavaMail & Email Notifications (`EmailNotificationService`)**:
  - Automated monthly bill delivery with payment instructions.
  - Overuse warnings with practical water conservation tips.
  - Admin anomaly reports (with simulation mode console logger fallback).
- **Downloadable PDF Invoice Generator (`PdfInvoiceService`)**:
  - Uses **Apache PDFBox 3.0** to render professional invoices showing consumption breakdowns, shared cost shares, and payment instructions.

### 5. React Frontend (Prompts 4 & 5)
- Scaffolded with **React 18**, **Vite**, **Tailwind CSS**, and **React Router 6**.
- **Admin Console**:
  - Bulk water purchase entry (tanker vs municipal supply).
  - Multi-tier tariff configuration form with live interactive calculator.
  - Meter reading interface (manual log + drag-and-drop CSV upload with sample template).
  - Billing cycle controls (open, preview calculation, finalize, archive).
  - All-household usage comparison bar charts via **Recharts**.
- **Resident Portal**:
  - 30-day daily usage line graph with 2-sigma anomaly visualization.
  - Current cycle summary card and water-saving tips feed.
  - Comparative benchmark view (Household vs Apartment Average vs Similar-Sized homes).
  - One-click PDF invoice downloader and online bill payment simulator.

---

## 🚀 Quick Start Guide

### Demo Credentials

| Role | Email | Password | Access / Scope |
|---|---|---|---|
| **Apartment Admin** | `admin@waterbill.com` | `Admin@123` | Full Admin Console, Billing Controls, Tariffs |
| **Resident (Metered + Spike)** | `resident1@waterbill.com` | `Resident@123` | Unit A-101 (Has 2-sigma anomaly spike on Oct 11) |
| **Resident (Metered Regular)** | `resident2@waterbill.com` | `Resident@123` | Unit A-102 (Family of 4, normal usage) |
| **Resident (Unmetered Flat Area)** | `resident3@waterbill.com` | `Resident@123` | Unit C-301 (Unmetered unit, flat area distribution) |

---

### Option A: Local Development Setup

#### Prerequisites
- Java 17+ (JDK 17)
- Maven 3.9+
- Node.js 18+ & npm

#### 1. Start the Spring Boot Backend
```powershell
cd backend
mvn clean spring-boot:run
```
*The backend starts at `http://localhost:8080`. Seed data and demo user accounts are populated automatically upon launch.*

To run the automated JUnit 5 / Mockito / MockMvc test suite:
```powershell
cd backend
mvn test
```

#### 2. Start the React Frontend
```powershell
cd frontend
npm install
npm run dev
```
*The frontend opens at `http://localhost:5173` with reverse proxy configured to forward `/api` requests to port 8080.*

---

### Option B: Docker Compose Deployment (Simultaneous Backend & Frontend)

Ensure Docker Desktop is running, then run:

```bash
docker-compose up --build
```

Services initialized:
- **Frontend**: `http://localhost:3000` (or `http://localhost:80`)
- **Backend API**: `http://localhost:8080`
- **PostgreSQL Database**: `localhost:5432`
- **Mailpit Web Mailbox**: `http://localhost:8025` (view real dispatched email alerts and bill notifications)

---

## 📚 REST API Documentation

### Authentication (`/api/auth`)
- `POST /api/auth/login`: Authenticate with email & password, returns JWT token and profile.
- `POST /api/auth/register`: Register new resident or administrator.
- `GET /api/auth/me`: Fetch authenticated user profile.
- `PUT /api/auth/profile`: Update profile information.
- `POST /api/auth/change-password`: Change user password.

### Admin Operations (`/api/admin`)
- `GET /api/admin/apartments`: List managed apartments.
- `POST /api/admin/households`: Register flat (with area, occupants, metered status).
- `POST /api/admin/meters`: Configure water meter hardware.
- `POST /api/admin/tariffs`: Publish new tiered tariff plan.
- `POST /api/admin/billing/cycles`: Open new billing cycle period.
- `POST /api/admin/billing/bulk-purchases`: Record tanker or municipal bulk delivery.
- `GET /api/admin/billing/cycles/{id}/calculate`: Preview tiered charges & shared distribution.
- `POST /api/admin/billing/cycles/{id}/finalize`: Finalize period, lock rates, and generate invoices.
- `GET /api/admin/analytics/usage-comparison`: All-household comparison bar chart data.

### Water Usage & Alerts (`/api/usage`, `/api/alerts`)
- `POST /api/usage/log`: Manual daily usage reading.
- `POST /api/usage/upload-csv`: Bulk CSV meter readings ingestion.
- `GET /api/usage/history`: Fetch historical reading logs.
- `GET /api/alerts`: List alerts for apartment or household.
- `POST /api/alerts/scan`: Trigger manual 2-sigma outlier and leak detection scan.

### Invoices & Downloads (`/api/invoices`)
- `GET /api/invoices?householdId={id}`: List invoices for resident.
- `GET /api/invoices/{id}/download`: Download official Apache PDFBox generated bill.
- `PATCH /api/invoices/{id}/status`: Settle bill / mark as PAID.

---

## 🧪 Automated Test Verification

The application includes unit and integration tests:
- `TariffBillingServiceTest`: 5 unit tests for multi-tier rates, tier transitions, and unbounded upper brackets.
- `ChargeDistributionServiceTest`: 3 unit tests verifying proportional consumption distribution, flat area fallback, and hybrid complex allocations.
- `AlertEngineServiceTest`: 2 unit tests verifying threshold alerts and 2-sigma outlier leak identification.
- `WaterBillingIntegrationTest`: 4 `@SpringBootTest` integration tests for JWT authentication, protected endpoints, resident benchmarking, and PDF invoice generation.
