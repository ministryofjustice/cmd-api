create TABLE IF NOT EXISTS SHEDLOCK (
    name       VARCHAR(64) PRIMARY KEY,
    lock_until TIMESTAMP NOT NULL,
    locked_at TIMESTAMP NOT NULL,
    locked_by  VARCHAR(255) NOT NULL
);
