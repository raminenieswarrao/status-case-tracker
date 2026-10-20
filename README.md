# USCIS Case Tracker

Responsive private Spring Boot app for tracking one or many USCIS receipt numbers.

## Included

- Username/email + password registration; no email verification.
- BCrypt password hashes; no forgot-password flow.
- Login/logout and in-app change password.
- Per-case: case name, applicant name, notification email, receipt number.
- Add, edit, delete, pause, resume, and manual refresh.
- Multiple cases per user.
- Strong ownership checks: users can only open their own case IDs.
- Friendly statuses + exact USCIS status/description.
- Invalid receipt -> `INVALID_RECEIPT`, visible on dashboard, hourly checks stop.
- Correcting a receipt revalidates immediately and can reactivate tracking.
- Hourly status scheduler.
- Daily 5 PM America/New_York email summary grouped by recipient email.
- Immediate approval email, deduplicated by `approval_notification_sent`.
- Status history only when status changes.
- PostgreSQL + Flyway migrations.
- Mobile/desktop responsive UI with drawer, animations, status cards.
- `mock` USCIS mode for safe local UI testing without sending a real receipt.
- `api` mode for USCIS OAuth 2.0 Case Status API.

## 1. Create PostgreSQL database

The app cannot inspect your laptop from inside this ZIP, so use the included PowerShell script.

```powershell
Set-ExecutionPolicy -Scope Process Bypass
.\setup-postgres.ps1
```

Default database:
- DB: `uscis_tracker`
- Role: `uscis_app`
- Default setup password: `uscis_app_change_me`

For a stronger password:

```powershell
.\setup-postgres.ps1 -AppPassword "Use-A-Long-Random-Password-Here"
```

Flyway creates the tables automatically on first startup.

## 2. Run locally

The project includes Maven Wrapper. In PowerShell:

```powershell
$env:DB_PASSWORD="the-password-you-used"
mvn spring-boot:run
```

Open:

`http://localhost:8082`

By default `USCIS_MODE=mock` and email sending is disabled.

### Mock receipt behavior

Use any syntactically valid 13-character receipt number.

- ending `0000` -> Invalid Case Number
- ending `0001` -> Approved
- ending `0002` -> Request for Evidence / Action Required
- anything else -> Processing

Examples:
- `IOE1234560000`
- `IOE1234560001`
- `IOE1234560002`
- `IOE1234567890`

This lets you test the whole UI without transmitting a real receipt number.

## 3. Gmail notifications

Use a dedicated Gmail sender account and a Google App Password.

PowerShell example:

```powershell
$env:MAIL_ENABLED="true"
$env:MAIL_USERNAME="sender@gmail.com"
$env:MAIL_APP_PASSWORD="your-app-password"
$env:MAIL_FROM="sender@gmail.com"
```

Never commit the App Password.

## 4. USCIS Sandbox/API

The official USCIS Case Status API uses OAuth 2.0 Client Credentials. Sandbox:
- Case Status base: `https://api-int.uscis.gov/case-status`
- Token: `https://api-int.uscis.gov/oauth/accesstoken`

Set:

```powershell
$env:USCIS_MODE="api"
$env:USCIS_CLIENT_ID="..."
$env:USCIS_CLIENT_SECRET="..."
```

Sandbox accepts USCIS staging receipt numbers only. Live receipt numbers require USCIS production access.

> IMPORTANT: Production API URLs/keys are supplied by USCIS when production access is approved. Do not assume the sandbox URL is the production URL.

## 5. Hourly checks and 5 PM email

Defaults:
- Hourly: `0 0 * * * *`
- Daily summary: `0 0 17 * * *`
- Zone: `America/New_York`

Invalid receipts are excluded because their `tracking_status` becomes `INVALID_RECEIPT`.

## 6. Beelink deployment

Recommended:
1. PostgreSQL DB `uscis_tracker` + role `uscis_app`
2. Build `mvn clean package`
3. Run `target/uscis-case-tracker-1.0.0.jar` as a Scheduled Task/service.
4. Set `COOKIE_SECURE=true` when exposed only through HTTPS.
5. Keep DB password, Gmail App Password, and USCIS API credentials in environment variables.
6. Reverse proxy `/uscis` carefully. The simplest production setup is a dedicated hostname/subdomain; if using a path prefix, configure Caddy and Spring's context path together.

## Security notes

- Passwords are BCrypt hashed and cannot be read back.
- Do not manually write plaintext passwords to `password_hash`.
- If you manually reset a password in PostgreSQL, write a BCrypt hash generated offline.
- Receipt numbers are immigration-related personal data. Keep the database, backups, logs, and email content private.
- Approval/daily emails mask receipt numbers.
- USCIS API credentials never belong in browser JavaScript.

## USCIS production-access note

USCIS currently documents organizational, policy, sandbox traffic, and demo requirements for production access. Check the current USCIS Developer Portal before attempting to use real receipt numbers in production.
