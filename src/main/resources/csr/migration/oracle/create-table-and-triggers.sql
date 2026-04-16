set echo on
spool V2_7_CMD_NOTIFICATION_REG1.log 
CREATE TABLE CMD_NOTIFICATION
(
    id                 NUMBER(10)    NOT NULL,
    st_staff_id        NUMBER(10)    NOT NULL,
    level_id           NUMBER(10)    NOT NULL,
    on_date            DATE          NOT NULL,
    LASTMODIFIED       DATE          NOT NULL,
    ACTION_TYPE	       NUMBER(10),
    TASK_START	       NUMBER(10),
    TASK_END	       NUMBER(10),
    REF_ID	           NUMBER(10),
    OPTIONAL_1	       NUMBER(10),
    PROCESSED          NUMBER(5)   DEFAULT 0   NOT NULL,
    CONSTRAINT "CMD_NOTIFICATION_PK" PRIMARY KEY ("ID")
);
CREATE INDEX CMD_NOTIFICATION_PROCESSED_X01 ON CMD_NOTIFICATION (PROCESSED, LASTMODIFIED);
CREATE SEQUENCE CMD_NOTIFICATION_ID START WITH 1;
spool off
spool V2_7_TW_PROTOCOL_TRIGGER_REG1.log
create or replace trigger CMD_NOTIFICATION_TW_PROTOCOL_TRIGGER
after insert on TW_PROTOCOL
referencing new as new old as old
for each row
DISABLE
begin
   if    :new.LEVEL_ID in (1000, 4000)
     and :new.ON_DATE between SYSDATE - 1 and SYSDATE + 14
     and :new.LAYER = -1
   then
       insert into CMD_NOTIFICATION (ID, ST_STAFF_ID, LEVEL_ID, ON_DATE, LASTMODIFIED, ACTION_TYPE)
       values (CMD_NOTIFICATION_ID.nextval, :new.ST_STAFF_ID, :new.LEVEL_ID, :new.ON_DATE, :new.LASTMODIFIED, :new.ACTION_TYPE);
   end if;
end;
/
spool off
spool V2_7_TW_SCHEDULE_TRIGGER_REG1.log
create or replace trigger CMD_NOTIFICATION_TW_SCHEDULE_TRIGGER
after insert on TW_SCHEDULE
referencing new as new old as old
for each row
DISABLE
begin
   if    :new.LEVEL_ID in (1000, 4000)
     and :new.ON_DATE between SYSDATE - 1 and SYSDATE + 2
     and :new.LAYER = -1
   then
       insert into CMD_NOTIFICATION (ID, ST_STAFF_ID, LEVEL_ID, ON_DATE, LASTMODIFIED, TASK_START, TASK_END, REF_ID, OPTIONAL_1)
       values (CMD_NOTIFICATION_ID.nextval, :new.ST_STAFF_ID, :new.LEVEL_ID, :new.ON_DATE, :new.SCHED_LASTMODIFIED, :new.TASK_START, :new.TASK_END, :new.REF_ID, :new.OPTIONAL_1);
   end if;
end;
/
spool off
