UPDATE user_preference
SET quantum_id = UPPER(quantum_id)
where true;
UPDATE shift_notification
SET quantum_id = UPPER(quantum_id)
where true;