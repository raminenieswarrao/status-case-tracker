param(
  [string]$PostgresBin = "C:\Program Files\PostgreSQL\17\bin",
  [string]$AdminUser = "postgres",
  [string]$Database = "uscis_tracker",
  [string]$AppUser = "uscis_app",
  [string]$AppPassword = "uscis_app_change_me"
)

$psql = Join-Path $PostgresBin "psql.exe"
if (!(Test-Path $psql)) {
  Write-Host "psql.exe not found at $psql" -ForegroundColor Red
  Write-Host "Pass your PostgreSQL bin folder, for example:" -ForegroundColor Yellow
  Write-Host '.\setup-postgres.ps1 -PostgresBin "C:\Program Files\PostgreSQL\17\bin"'
  exit 1
}

Write-Host "You may be prompted for the PostgreSQL '$AdminUser' password." -ForegroundColor Cyan

$roleSql = @"
DO `$`$
BEGIN
  IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = '$AppUser') THEN
    CREATE ROLE $AppUser LOGIN PASSWORD '$AppPassword';
  ELSE
    ALTER ROLE $AppUser WITH LOGIN PASSWORD '$AppPassword';
  END IF;
END
`$`$;
"@

& $psql -U $AdminUser -d postgres -v ON_ERROR_STOP=1 -c $roleSql
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

$dbExists = & $psql -U $AdminUser -d postgres -tAc "SELECT 1 FROM pg_database WHERE datname='$Database'"
if ($dbExists.Trim() -ne "1") {
  & $psql -U $AdminUser -d postgres -v ON_ERROR_STOP=1 -c "CREATE DATABASE $Database OWNER $AppUser;"
} else {
  & $psql -U $AdminUser -d postgres -v ON_ERROR_STOP=1 -c "ALTER DATABASE $Database OWNER TO $AppUser;"
}
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

Write-Host ""
Write-Host "Database ready." -ForegroundColor Green
Write-Host "DB:   $Database"
Write-Host "User: $AppUser"
Write-Host "Set DB_PASSWORD to: $AppPassword"
Write-Host "Flyway will create the tables automatically when Spring Boot starts."
