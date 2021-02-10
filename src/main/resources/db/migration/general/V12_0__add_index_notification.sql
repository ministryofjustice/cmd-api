CREATE INDEX IF NOT EXISTS notification_business_key
    ON notification (quantum_id, shift_modified);