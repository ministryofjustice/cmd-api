create table if not exists user_preference
(
    UUID         uuid primary key,
    QUANTUM_ID   varchar(128) not null,
    SNOOZE_UNTIL date         not null,
    CONSTRAINT notification_uuid_idempotent UNIQUE (UUID),
    CONSTRAINT notification_user_idempotent UNIQUE (QUANTUM_ID)
)