create table if not exists user_preference
(
    uuid         UUID PRIMARY KEY,
    quantum_id   VARCHAR(128) NOT NULL,
    snooze_until DATE         NOT NULL,
    CONSTRAINT notification_uuid_idempotent UNIQUE (uuid),
    CONSTRAINT notification_user_idempotent UNIQUE (quantum_id)
)