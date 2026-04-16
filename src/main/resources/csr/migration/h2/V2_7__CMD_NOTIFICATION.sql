CREATE OR REPLACE TABLE CMD_NOTIFICATION
(
    id                 NUMBER(10)    NOT NULL,

    -- Indexed columns in source tables
    st_staff_id        NUMBER(10)    NOT NULL,
    level_id           NUMBER(10)    NOT NULL,
    on_date            DATE          NOT NULL,

    -- common
    LASTMODIFIED       DATE          NOT NULL,

    --tw_protocol (GET_MODIFIED_SHIFTS):
    ACTION_TYPE	       NUMBER(10),

    --tw_schedule (GET_MODIFIED_DETAILS):
    TASK_START	       NUMBER(10),
    TASK_END	       NUMBER(10),
    REF_ID	           NUMBER(10),
    OPTIONAL_1	       NUMBER(10),

    PROCESSED          NUMBER(5)   DEFAULT 0   NOT NULL,

    CONSTRAINT "CMD_NOTIFICATION_PK" PRIMARY KEY ("ID")
);

CREATE INDEX CMD_NOTIFICATION_PROCESSED_X01 ON CMD_NOTIFICATION (PROCESSED, LASTMODIFIED);

CREATE SEQUENCE CMD_NOTIFICATION_ID START WITH 1;
