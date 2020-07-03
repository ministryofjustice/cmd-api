drop table if exists "NotificationConfiguration";
drop table if exists "UserNotificationSetting";
drop table if exists "ShiftTaskNotification";
drop table if exists "ShiftNotification";
drop table if exists "UserAuthentication";
drop table if exists "Prison";

create table "Prison"
(
    "Id"          integer      not null
        constraint "Prison_pkey"
            primary key,
    "Name"        varchar(50)  not null,
    "Description" varchar(200),
    "ApiUrl"      varchar(256) not null,
    "RegionId"    smallint
);


create table "UserAuthentication"
(
    "QuantumId"                   varchar(50)  not null primary key,
    "EmailAddress"                varchar(100),
    "Sms"                         varchar(50),
    "UseEmailAddress"             boolean      not null,
    "UseSms"                      boolean      not null,
    "LastLoginDateTime"           timestamp with time zone,
    "ApiUrl"                      varchar(256) not null,
    "PrisonId"                    integer,
    "TwoFactorAuthenticationHash" varchar(100),
    "SessionExpiryDateTime"       timestamp
);


create table "ShiftNotification"
(
    "QuantumId"                     varchar(50)              not null,
    "DateTime"                      timestamp with time zone not null,
    "Description"                   varchar(500),
    "ShiftDate"                     timestamp with time zone not null,
    "LastModifiedDateTime"          timestamp with time zone not null,
    "Read"                          boolean default false    not null,
    "SentSms"                       boolean default false    not null,
    "SentEmail"                     boolean default false    not null,
    "LastModifiedDateTimeInSeconds" bigint not null
);


create table "ShiftTaskNotification"
(
    "QuantumId"                     varchar(50)              not null,
    "DateTime"                      timestamp with time zone not null,
    "Description"                   varchar(500),
    "TaskDate"                      timestamp with time zone not null,
    "TaskStartTimeInSeconds"        integer                  not null,
    "TaskEndTimeInSeconds"          integer                  not null,
    "Activity"                      varchar(500),
    "LastModifiedDateTime"          timestamp with time zone not null,
    "Read"                          boolean default false    not null,
    "SentSms"                       boolean default false    not null,
    "SentEmail"                     boolean default false    not null,
    "LastModifiedDateTimeInSeconds" bigint                   not null
);


create table "UserNotificationSetting"
(
    "QuantumId"       varchar(50) not null primary key,
    "EmailAddress"    varchar(50),
    "Sms"             varchar(50),
    "UseEmailAddress" boolean     not null,
    "UseSms"          boolean     not null
);


create table "NotificationConfiguration"
(
    "NotificationConfigurationId" integer generated always as identity,
    "PrisonId"                    integer      not null,
    "PlanningUnitId"              integer      not null,
    "PlanningUnitName"            varchar(100) not null,
    "ShiftEnabled"                boolean      not null,
    "ShiftTaskEnabled"            boolean      not null,
    "ShiftInterval"               integer      not null,
    "ShiftIntervalType"           smallint     not null,
    "ShiftTaskInterval"           integer      not null,
    "ShiftTaskIntervalType"       smallint     not null,
    "ShiftLastRunDateTime"        timestamp(6),
    "ShiftTaskLastRunDateTime"    timestamp(6)
);