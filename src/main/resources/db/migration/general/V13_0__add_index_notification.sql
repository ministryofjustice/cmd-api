CREATE INDEX IF NOT EXISTS notification_business_key
    ON notification (quantum_id, shift_modified);

CREATE INDEX IF NOT EXISTS notification_shift_modified
    ON notification (shift_modified);

CREATE INDEX IF NOT EXISTS notification_count_shift_modified
    ON notification (quantum_id, detail_start, parent_type, shift_modified);

CREATE INDEX IF NOT EXISTS notification_count_action_type
    ON notification (quantum_id, detail_start, parent_type, action_type);
