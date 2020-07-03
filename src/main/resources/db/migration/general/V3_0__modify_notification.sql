ALTER TABLE "ShiftNotification"
    DROP CONSTRAINT IF EXISTS "ShiftNotification_QuantumId_UserAuthentication_QuantumId";
ALTER TABLE "ShiftNotification"
    DROP CONSTRAINT IF EXISTS "ShiftNotification_QuantumId_LastModifiedDateTime_ShiftDate";
ALTER TABLE "ShiftTaskNotification"
    DROP CONSTRAINT IF EXISTS "ShiftNotification_QuantumId_UserAuthentication_QuantumId";
ALTER TABLE "ShiftTaskNotification"
    DROP CONSTRAINT IF EXISTS "ShiftNotification_QuantumId_LastModifiedDateTime_TaskDate_TaskS";

ALTER TABLE "ShiftNotification"
    ADD COLUMN ID SERIAL PRIMARY KEY;

ALTER TABLE "ShiftNotification"
    RENAME COLUMN "QuantumId" to "QUANTUM_ID";
ALTER TABLE "ShiftNotification"
    RENAME COLUMN "DateTime" to "DATE_TIME";
ALTER TABLE "ShiftNotification"
    RENAME COLUMN "Description" to "DESCRIPTION";
ALTER TABLE "ShiftNotification"
    RENAME COLUMN "ShiftDate" to "SHIFT_DATE";
ALTER TABLE "ShiftNotification"
    RENAME COLUMN "LastModifiedDateTime" to "LAST_MODIFIED_DATE_TIME";
ALTER TABLE "ShiftNotification"
    RENAME COLUMN "Read" to "READ";
ALTER TABLE "ShiftNotification"
    RENAME COLUMN "SentSms" to "SENT_SMS";
ALTER TABLE "ShiftNotification"
    RENAME COLUMN "SentEmail" to "SENT_EMAIL";
ALTER TABLE "ShiftNotification"
    RENAME COLUMN "LastModifiedDateTimeInSeconds" to "LAST_MODIFIED_DATE_TIME_IN_SECONDS";
ALTER TABLE "ShiftNotification"
    RENAME COLUMN "NotificationType" to "NOTIFICATION_TYPE";

ALTER TABLE "ShiftTaskNotification"
    ADD COLUMN ID SERIAL PRIMARY KEY;

ALTER TABLE "ShiftTaskNotification"
    RENAME COLUMN "QuantumId" to "QUANTUM_ID";
ALTER TABLE "ShiftTaskNotification"
    RENAME COLUMN "DateTime" to "DATE_TIME";
ALTER TABLE "ShiftTaskNotification"
    RENAME COLUMN "Description" to "DESCRIPTION";
ALTER TABLE "ShiftTaskNotification"
    RENAME COLUMN "TaskDate" to "TASK_DATE";
ALTER TABLE "ShiftTaskNotification"
    RENAME COLUMN "TaskStartTimeInSeconds" to "TASK_START_TIME_IN_SECONDS";
ALTER TABLE "ShiftTaskNotification"
    RENAME COLUMN "TaskEndTimeInSeconds" to "TASK_END_TIME_IN_SECONDS";
ALTER TABLE "ShiftTaskNotification"
    RENAME COLUMN "Activity" to "ACTIVITY";
ALTER TABLE "ShiftTaskNotification"
    RENAME COLUMN "LastModifiedDateTime" to "LAST_MODIFIED_DATE_TIME";
ALTER TABLE "ShiftTaskNotification"
    RENAME COLUMN "Read" to "READ";
ALTER TABLE "ShiftTaskNotification"
    RENAME COLUMN "SentSms" to "SENT_SMS";
ALTER TABLE "ShiftTaskNotification"
    RENAME COLUMN "SentEmail" to "SENT_EMAIL";
ALTER TABLE "ShiftTaskNotification"
    RENAME COLUMN "LastModifiedDateTimeInSeconds" to "LAST_MODIFIED_DATE_TIME_IN_SECONDS";

ALTER TABLE "ShiftNotification"
    RENAME TO "SHIFT_NOTIFICATION";

ALTER TABLE "ShiftTaskNotification"
    RENAME TO "SHIFT_TASK_NOTIFICATION";