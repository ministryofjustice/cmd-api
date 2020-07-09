ALTER TABLE "ShiftTaskNotification"
    DROP CONSTRAINT IF EXISTS "ShiftNotification_QuantumId_UserAuthentication_QuantumId";
ALTER TABLE "ShiftTaskNotification"
    DROP CONSTRAINT IF EXISTS "ShiftNotification_QuantumId_LastModifiedDateTime_TaskDate_TaskS";

ALTER TABLE "ShiftTaskNotification"
    ADD COLUMN id SERIAL PRIMARY KEY;

ALTER TABLE "ShiftTaskNotification"
    RENAME COLUMN "QuantumId" to quantum_id;
ALTER TABLE "ShiftTaskNotification"
    RENAME COLUMN "DateTime" to datetime;
ALTER TABLE "ShiftTaskNotification"
    RENAME COLUMN "Description" to description;
ALTER TABLE "ShiftTaskNotification"
    RENAME COLUMN "TaskDate" to task_date;
ALTER TABLE "ShiftTaskNotification"
    RENAME COLUMN "TaskStartTimeInSeconds" to task_start_time_in_seconds;
ALTER TABLE "ShiftTaskNotification"
    RENAME COLUMN "TaskEndTimeInSeconds" to task_end_time_in_seconds;
ALTER TABLE "ShiftTaskNotification"
    RENAME COLUMN "Activity" to activity;
ALTER TABLE "ShiftTaskNotification"
    RENAME COLUMN "LastModifiedDateTime" to last_modified;
ALTER TABLE "ShiftTaskNotification"
    RENAME COLUMN "Read" to read;
ALTER TABLE "ShiftTaskNotification"
    RENAME COLUMN "SentSms" to sent_sms;
ALTER TABLE "ShiftTaskNotification"
    RENAME COLUMN "SentEmail" to sent_email;
ALTER TABLE "ShiftTaskNotification"
    RENAME COLUMN "LastModifiedDateTimeInSeconds" to last_modified_date_time_in_seconds;

ALTER TABLE "ShiftTaskNotification"
    RENAME TO shift_task_notification;

ALTER TABLE shift_task_notification
    ADD COLUMN notification_type smallint;

ALTER TABLE shift_task_notification
    ADD COLUMN processed BOOLEAN DEFAULT FALSE NOT NULL;

UPDATE shift_task_notification
SET processed = TRUE
WHERE read = TRUE
   OR sent_sms = TRUE
   OR sent_email = TRUE;

ALTER TABLE shift_task_notification
    DROP COLUMN read;

ALTER TABLE shift_task_notification
    DROP COLUMN sent_sms;

ALTER TABLE shift_task_notification
    DROP COLUMN sent_email;

ALTER TABLE shift_task_notification
    DROP COLUMN datetime;

ALTER TABLE shift_task_notification
    DROP COLUMN last_modified_date_time_in_seconds;

CREATE INDEX shift_task_notification_processed ON shift_task_notification (processed);

