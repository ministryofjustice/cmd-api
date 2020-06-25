create table if not exists notification_snooze_preference
(
    ID          serial      primary key,
    UUID        uuid        not null,
    QUANTUM_ID  text        not null,
    SNOOZE      date        not null,
    CONSTRAINT notification_uuid_idempotent UNIQUE (UUID)--,
    -- H2 doesn't allow indexes on BLOB or CLOB columns
    --CONSTRAINT notification_user_idempotent UNIQUE (QUANTUM_ID)
)