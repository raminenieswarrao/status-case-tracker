$env:DB_URL = "jdbc:postgresql://localhost:5432/uscis_tracker"
$env:DB_USERNAME = "uscis_app"

if (-not $env:DB_PASSWORD) {
  $env:DB_PASSWORD = Read-Host "PostgreSQL password for uscis_app"
}

# Runs safely in demo mode unless you explicitly set USCIS_MODE=api.
if (-not $env:USCIS_MODE) { $env:USCIS_MODE = "mock" }
if (-not $env:MAIL_ENABLED) { $env:MAIL_ENABLED = "false" }

Write-Host "Starting USCIS Case Tracker on http://localhost:8082" -ForegroundColor Cyan
Write-Host "USCIS_MODE=$env:USCIS_MODE  MAIL_ENABLED=$env:MAIL_ENABLED" -ForegroundColor Yellow

mvn spring-boot:run
