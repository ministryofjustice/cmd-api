UPDATE notification
SET quantum_id = UPPER(quantum_id) WHERE TRUE;

UPDATE user_preference
SET quantum_id = UPPER(quantum_id) WHERE TRUE;
