
CREATE INDEX IF NOT EXISTS notification_quantum_id_shift_modified ON dry_run_notification (upper(quantum_id), shift_modified);
CREATE INDEX IF NOT EXISTS notification_quantum_id_detail_start   ON dry_run_notification (upper(quantum_id), detail_start);
