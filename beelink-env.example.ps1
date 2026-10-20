# Copy values into your service/scheduled-task environment. Do NOT commit real secrets.

$env:SERVER_PORT="8082"
$env:DB_URL="jdbc:postgresql://localhost:5432/uscis_tracker"
$env:DB_USERNAME="uscis_app"
$env:DB_PASSWORD="CHANGE_ME"

# HTTPS through your reverse proxy/Tailscale:
$env:COOKIE_SECURE="true"
$env:APP_TIME_ZONE="America/New_York"

# Keep mock until USCIS credentials are ready:
$env:USCIS_MODE="mock"
$env:USCIS_API_BASE_URL="https://api-int.uscis.gov/case-status"
$env:USCIS_TOKEN_URL="https://api-int.uscis.gov/oauth/accesstoken"
$env:USCIS_CLIENT_ID=""
$env:USCIS_CLIENT_SECRET=""
$env:USCIS_DEMO_ID=""

# Gmail sender:
$env:MAIL_ENABLED="false"
$env:MAIL_USERNAME="sender@gmail.com"
$env:MAIL_APP_PASSWORD="GOOGLE_APP_PASSWORD"
$env:MAIL_FROM="sender@gmail.com"
