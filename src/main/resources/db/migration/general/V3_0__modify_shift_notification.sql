ALTER TABLE "ShiftNotification"
    DROP CONSTRAINT IF EXISTS "ShiftNotification_QuantumId_UserAuthentication_QuantumId";
ALTER TABLE "ShiftNotification"
    DROP CONSTRAINT IF EXISTS "ShiftNotification_QuantumId_LastModifiedDateTime_ShiftDate";

ALTER TABLE "ShiftNotification"
    ADD COLUMN id SERIAL PRIMARY KEY;

ALTER TABLE "ShiftNotification"
    RENAME COLUMN "QuantumId" to quantum_id;
ALTER TABLE "ShiftNotification"
    RENAME COLUMN "DateTime" to datetime;
ALTER TABLE "ShiftNotification"
    RENAME COLUMN "Description" to description;
ALTER TABLE "ShiftNotification"
    RENAME COLUMN "ShiftDate" to shift_date;
ALTER TABLE "ShiftNotification"
    RENAME COLUMN "LastModifiedDateTime" to last_modified;
ALTER TABLE "ShiftNotification"
    RENAME COLUMN "Read" to read;
ALTER TABLE "ShiftNotification"
    RENAME COLUMN "SentSms" to sent_sms;
ALTER TABLE "ShiftNotification"
    RENAME COLUMN "SentEmail" to sent_email;
ALTER TABLE "ShiftNotification"
    RENAME COLUMN "LastModifiedDateTimeInSeconds" to last_modified_date_time_in_seconds;

ALTER TABLE "ShiftNotification"
    RENAME TO shift_notification;

ALTER TABLE shift_notification
    ADD COLUMN notification_type smallint;

ALTER TABLE shift_notification
    ADD COLUMN processed BOOLEAN DEFAULT FALSE NOT NULL;

UPDATE shift_notification
SET processed = TRUE
WHERE read = TRUE
   OR sent_sms = TRUE
   OR sent_email = TRUE;

ALTER TABLE shift_notification
    DROP COLUMN read;

ALTER TABLE shift_notification
    DROP COLUMN sent_sms;

ALTER TABLE shift_notification
    DROP COLUMN sent_email;

ALTER TABLE shift_notification
    DROP COLUMN datetime;

ALTER TABLE shift_notification
    DROP COLUMN last_modified_date_time_in_seconds;

CREATE INDEX shift_notification_processed ON shift_notification (processed);