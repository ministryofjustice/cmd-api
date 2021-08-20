
CREATE INDEX IF NOT EXISTS notification_quantum_shift_modified ON notification (upper(quantum_id), shift_modified);
CREATE INDEX IF NOT EXISTS notification_quantum_detail_start   ON notification (upper(quantum_id), detail_start);
