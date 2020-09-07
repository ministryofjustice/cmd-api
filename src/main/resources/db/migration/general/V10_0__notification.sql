CREATE TABLE IF NOT EXISTS notification
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

CREATE INDEX IF NOT EXISTS notification_processed
    ON notification (processed);