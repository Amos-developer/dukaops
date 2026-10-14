# DukaOps

DukaOps is a multi-tenant business management application for managing products, branch inventory, sales, staff, and business contacts from a single workspace. It combines a Spring Boot REST API with a responsive React interface.

The project is under active development. Local development uses PostgreSQL and runs the backend and frontend directly on your computer.

## Technology stack

| Layer | Technologies |
| --- | --- |
| Backend | Java 21, Spring Boot, Spring Data JPA, Spring Security |
| Database | PostgreSQL, Flyway migrations |
| Frontend | React, JavaScript, Vite, React Router, TanStack Query |
| Interface | Bootstrap, Lucide icons, custom page styles |
| Testing | JUnit, Spring Boot Test, Mockito |

## Features

- Business registration and authenticated workspace access.
- Product management with pricing, costs, and active/inactive status.
- Branch inventory, stock receipts, adjustments, and movement history.
- Sales checkout with Cash and Mobile Money payment records.
- Team management with direct account creation, roles, and branch assignments.
- Customer and supplier contact management.
- Reporting endpoints and an analytics interface.
- Business preferences, personal profiles, password changes, and notification preferences.
- A searchable Help Center with practical workspace guides.

Notification settings currently store preferences; automated alert generation and email delivery are not implemented. Subscription and billing information is explicitly unavailable. Customer-linked purchase history, supplier purchases, and supplier-product relationships are not yet implemented.

## Repository structure

```text
dukaops/
├── dukaops-backend/
│   ├── src/main/java/com/dukaops/backend/
│   │   ├── auth/          # Authentication, users, roles, invitations
│   │   ├── branch/        # Branches and inventory persistence
│   │   ├── contact/       # Customers and suppliers
│   │   ├── inventory/     # Stock operations
│   │   ├── product/       # Product management
│   │   ├── reports/       # Reporting queries and endpoints
│   │   ├── sales/         # Orders, checkout, items, payments
│   │   ├── settings/      # Workspace and account settings
│   │   └── tenant/        # Business identity
│   ├── src/main/resources/db/migration/
│   └── src/test/
├── dukaops-frontend/
│   └── src/
│       ├── components/    # Shared UI and application layout
│       ├── hooks/         # Query hooks
│       ├── services/      # API requests and page data
│       ├── styles/        # Shared and page-specific styles
│       └── views/         # Routed pages
└── docker-compose.yml
```

## Local development

### Prerequisites

- JDK 21 with `JAVA_HOME` pointing to the JDK installation directory.
- Node.js compatible with Vite 6 and npm.
- A running PostgreSQL instance, normally on port `5432`.
- Git. Maven is supplied through the project wrapper.

### 1. Clone the repository

```powershell
git clone https://github.com/Amos-developer/dukaops.git
cd dukaops
```

### 2. Create the database

Using pgAdmin's Query Tool connected to an existing database, run:

```sql
CREATE DATABASE dukaops_db;
```

Flyway creates and updates application tables when the backend starts. Hibernate validates the resulting schema.

### 3. Configure and start the backend

In a PowerShell terminal, set your actual database credentials:

```powershell
$env:POSTGRES_DB = 'dukaops_db'
$env:POSTGRES_USER = 'postgres'
$env:POSTGRES_PASSWORD = '<your-postgresql-password>'
cd dukaops-backend
.\mvnw.cmd spring-boot:run
```

The backend listens at `http://localhost:8000`. Its health endpoint is `http://localhost:8000/actuator/health`.

Environment variables above apply to the current terminal session. Override the development password fallback in `application.properties`; do not use it for a deployed environment.

### 4. Configure and start the frontend

In a second terminal:

```powershell
cd dukaops-frontend
npm ci
Copy-Item .env.example .env.local
npm run dev
```

Set the following in `.env.local`:

```dotenv
VITE_API_URL=http://localhost:8000/api
```

Open `http://localhost:5173`. The development server uses a strict port setting, so it fails if `5173` is occupied. Restart Vite after changing environment variables.

Register a business through the public registration page, then sign in. Registration passwords require 12–64 characters. `VITE_API_USERNAME` and `VITE_API_PASSWORD` are optional development fallbacks; normal use relies on sign-in. Never embed production credentials in Vite environment variables because they are exposed in the browser bundle.

## Configuration reference

| Variable | Purpose |
| --- | --- |
| `POSTGRES_DB` | PostgreSQL database name; defaults to `dukaops_db` |
| `POSTGRES_USER` | PostgreSQL login; defaults to `postgres` |
| `POSTGRES_PASSWORD` | Database password; set explicitly for your environment |
| `SPRING_DATASOURCE_URL` | Optional complete JDBC connection URL override |
| `CORS_ALLOWED_ORIGINS` | Allowed frontend origins; default includes ports `5173` and `4173` |
| `VITE_API_URL` | Browser-facing API base URL |

Business preferences default to currency `TZS`, timezone `Africa/Dar_es_Salaam`, and date format `dd/MM/yyyy`.

## Account and team workflow

Business registration creates an OWNER account. Authorized owners and administrators can create staff accounts from **Team & Users → Add user**, assign a permitted role, and select branch access. The new member can log in immediately using the email and initial password provided by the administrator.

Existing roles are OWNER, ADMIN, MANAGER, and STAFF. Backend services enforce role policies and use authenticated principal identities for tenant-scoped operations. Ordinary profile updates cannot change tenant identity or roles. Passwords are hashed with BCrypt.

The API currently uses stateless HTTP Basic authentication. The frontend stores sign-in credentials in browser local storage. Review this credential-storage approach before production deployment and serve the application over HTTPS.

## API modules

| Base path | Purpose |
| --- | --- |
| `/api/auth` | Registration and authenticated account details |
| `/api/team/users` | Staff creation, roles, status, and branch assignments |
| `/api/team/invitations` | Existing invitation API retained for compatibility |
| `/api/products` | Product management |
| `/api/tenants/{tenantId}/branches` | Branch management |
| `/api/inventory` | Stock receipts, adjustments, and history |
| `/api/sales` | Checkout, order details, lists, and summaries |
| `/api/contacts/customers` | Customer contacts |
| `/api/contacts/suppliers` | Supplier contacts |
| `/api/reports` | Overview, sales, inventory, products, contacts, and CSV exports |
| `/api/settings` | Business, preferences, profile, password, notifications, subscription availability |

Except for registration and invitation acceptance, API requests require authentication. Reporting and Settings are still evolving; not all interface controls or requested analytics are implemented end to end. Confirm authorization and branch filtering for each reporting endpoint before exposing financial data in production.

## Tests and production builds

Run backend tests from `dukaops-backend` with an available PostgreSQL database and the appropriate environment variables:

```powershell
.\mvnw.cmd clean test
```

The integration tests load the Spring application, use PostgreSQL, and run Flyway. Prefer a dedicated test database rather than a production database. No successful backend test run is claimed by this README.

Build and preview the frontend from `dukaops-frontend`:

```powershell
npm run build
npm run preview
```

Build the backend JAR:

```powershell
.\mvnw.cmd clean package
```

## Database changes

Add a new versioned Flyway migration for each schema change. Never edit or delete a migration that has already been applied. Keep entity mappings consistent with migrations because `spring.jpa.hibernate.ddl-auto=validate` is enabled.

## Troubleshooting

**Invalid `JAVA_HOME`:** point it to a JDK directory containing `bin/java.exe`, reopen the terminal, and verify `java -version`.

**Frontend cannot connect:** confirm PostgreSQL and Spring Boot started successfully, verify `VITE_API_URL`, and inspect the failing request in browser developer tools. A successful frontend build does not confirm that the API is running.

**Port `5173` is occupied:** stop the previous development server or configure a different port and update the backend's allowed CORS origins.

**Database authentication fails:** use the password for the configured PostgreSQL role. pgAdmin's own master password is not the database role password.

**A fixed query still fails:** stop the backend and rebuild so the running classes match the source:

```powershell
.\mvnw.cmd clean spring-boot:run
```

**Migration or schema validation fails:** inspect the startup error and applied migration history before changing anything. Preserve existing data and use additive migrations.

## Contributing

Keep changes focused, follow the existing controller/service/repository and DTO conventions, and add meaningful tests for security or persistence changes. Run the relevant checks and describe any unverified behavior in the pull request. Do not commit credentials or database exports.

Suggested commit prefixes include `feat`, `fix`, `test`, `style`, and `docs`.
