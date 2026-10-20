# PostgreSQL setup

The application uses:

- Database: `uscis_tracker`
- Application role: `uscis_app`
- Tables: `app_users`, `tracked_cases`, `case_status_history`
- Flyway schema history: `flyway_schema_history`

## Windows laptop

Run from the project root:

```powershell
.\setup-postgres.ps1 -AppPassword "choose-a-strong-password"
```

Then set the same password before starting Spring Boot:

```powershell
$env:DB_PASSWORD="choose-a-strong-password"
mvn spring-boot:run
```

## Beelink

Run the same `setup-postgres.ps1` on the Beelink. It creates the database/role if needed.
When the application starts, Flyway automatically applies `src/main/resources/db/migration/V1__initial_schema.sql`.

This gives the laptop and Beelink the same table structure without copying personal case data.
