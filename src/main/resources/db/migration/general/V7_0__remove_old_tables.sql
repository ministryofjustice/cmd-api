DROP TABLE "UserNotificationSetting" CASCADE;

DROP TABLE "ShiftNotification" CASCADE;

DROP TABLE "ShiftTaskNotification" CASCADE;

DROP TABLE "Prison" CASCADE;

DROP TABLE "NotificationConfiguration" CASCADE;

UPDATE shift_notification SET shift_type = 'shift' WHERE shift_type = 'SHIFT';

UPDATE shift_notification SET shift_type = 'task' WHERE shift_type = 'TASK';

UPDATE shift_notification SET action_type = 'edit' WHERE action_type = 'EDIT';