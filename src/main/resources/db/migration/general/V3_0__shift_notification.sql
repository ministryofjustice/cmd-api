CREATE TABLE IF NOT EXISTS shift_notification
(
    id             SERIAL PRIMARY KEY,
    quantum_id     VARCHAR               NOT NULL,
    shift_date     DATE                  NOT NULL,
    shift_modified TIMESTAMP             NOT NULL,
    task_start     INT,
    task_end       INT,
    task           VARCHAR,
    shift_type     VARCHAR               NOT NULL,
    action_type    VARCHAR               NOT NULL,
    processed      BOOLEAN DEFAULT FALSE NOT NULL
);

CREATE INDEX IF NOT EXISTS shift_notification_processed
    ON shift_notification (processed);

INSERT INTO shift_notification (quantum_id, shift_date, shift_modified, shift_type, action_type, processed)
SELECT "QuantumId", "ShiftDate", "LastModifiedDateTime", 'SHIFT', 'EDIT', TRUE
FROM "ShiftNotification";

INSERT INTO shift_notification (quantum_id, shift_date, shift_modified, task_start, task_end, task, shift_type,
                                action_type, processed)
SELECT "QuantumId",
       "TaskDate",
       "LastModifiedDateTime",
       "TaskStartTimeInSeconds",
       "TaskEndTimeInSeconds",
       "Activity",
       'TASK',
       'EDIT',
       TRUE
FROM "ShiftTaskNotification";