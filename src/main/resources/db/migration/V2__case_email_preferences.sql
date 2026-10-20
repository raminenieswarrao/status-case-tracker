ALTER TABLE tracked_cases
    ADD COLUMN daily_summary_enabled BOOLEAN NOT NULL DEFAULT TRUE;

ALTER TABLE tracked_cases
    ADD COLUMN status_change_email_enabled BOOLEAN NOT NULL DEFAULT TRUE;

ALTER TABLE tracked_cases
    ADD COLUMN email_notifications_paused BOOLEAN NOT NULL DEFAULT FALSE;
