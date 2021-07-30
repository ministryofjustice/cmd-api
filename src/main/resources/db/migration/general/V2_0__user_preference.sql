CREATE TABLE IF NOT EXISTS user_preference
(
    quantum_id   VARCHAR PRIMARY KEY,
    snooze_until DATE,
    email        VARCHAR,
    sms          VARCHAR,
    comm_pref    VARCHAR NOT NULL DEFAULT 'EMAIL'
);

INSERT INTO user_preference (quantum_id, email, sms)
SELECT "QuantumId", "EmailAddress", "Sms"
FROM "UserNotificationSetting";

UPDATE user_preference
SET comm_pref = 'SMS'
WHERE email IS NULL
  AND quantum_id in
      (SELECT "QuantumId" FROM "UserNotificationSetting" WHERE "UseEmailAddress" = FALSE AND "UseSms" = TRUE)
