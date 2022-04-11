CREATE TABLE IF NOT EXISTS dry_run_notification
(
    id             SERIAL PRIMARY KEY,
    quantum_id     VARCHAR               NOT NULL,
    shift_modified TIMESTAMP             NOT NULL,
    detail_start   TIMESTAMP             NOT NULL,
    detail_end     TIMESTAMP             NOT NULL,
    activity       VARCHAR,
    parent_type    VARCHAR               NOT NULL,
    action_type    VARCHAR               NOT NULL,
    processed      BOOLEAN DEFAULT FALSE NOT NULL
);

CREATE INDEX IF NOT EXISTS dry_run_notification_processed
    ON dry_run_notification (processed, shift_modified);
