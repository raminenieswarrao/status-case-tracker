CREATE TABLE IF NOT EXISTS app_users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(60) NOT NULL,
    email VARCHAR(254),
    password_hash VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_app_users_username UNIQUE (username),
    CONSTRAINT uk_app_users_email UNIQUE (email)
);

CREATE TABLE IF NOT EXISTS tracked_cases (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    case_name VARCHAR(120) NOT NULL,
    applicant_name VARCHAR(120) NOT NULL,
    notification_email VARCHAR(254) NOT NULL,
    receipt_number VARCHAR(30) NOT NULL,
    form_type VARCHAR(40),
    friendly_status VARCHAR(40) NOT NULL DEFAULT 'NEW',
    tracking_status VARCHAR(40) NOT NULL DEFAULT 'ACTIVE',
    official_status VARCHAR(500),
    official_description TEXT,
    uscis_updated_at TIMESTAMPTZ,
    last_checked_at TIMESTAMPTZ,
    last_status_changed_at TIMESTAMPTZ,
    approval_notification_sent BOOLEAN NOT NULL DEFAULT FALSE,
    last_error VARCHAR(1000),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_user_receipt UNIQUE (user_id, receipt_number)
);

CREATE INDEX IF NOT EXISTS idx_tracked_cases_user ON tracked_cases(user_id);
CREATE INDEX IF NOT EXISTS idx_tracked_cases_tracking ON tracked_cases(tracking_status);
CREATE INDEX IF NOT EXISTS idx_tracked_cases_receipt ON tracked_cases(receipt_number);

CREATE TABLE IF NOT EXISTS case_status_history (
    id BIGSERIAL PRIMARY KEY,
    tracked_case_id BIGINT NOT NULL REFERENCES tracked_cases(id) ON DELETE CASCADE,
    friendly_status VARCHAR(40) NOT NULL,
    official_status VARCHAR(500),
    official_description TEXT,
    uscis_updated_at TIMESTAMPTZ,
    detected_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_history_case_detected
    ON case_status_history(tracked_case_id, detected_at DESC);
