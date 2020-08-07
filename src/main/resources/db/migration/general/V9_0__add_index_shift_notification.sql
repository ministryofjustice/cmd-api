CREATE INDEX IF NOT EXISTS shift_notification_business_key
    ON shift_notification (quantum_id, shift_date, shift_type, shift_modified);