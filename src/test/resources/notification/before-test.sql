INSERT INTO SHIFT_TASK_NOTIFICATION ("ID", "DESCRIPTION", "LAST_MODIFIED_DATE_TIME", "QUANTUM_ID",
                                     "TASK_DATE", "TASK_START_TIME_IN_SECONDS", "TASK_END_TIME_IN_SECONDS",
                                     "ACKNOWLEDGED")
values (1, 'any description aa', CURRENT_DATE, 'API_TEST_USER', '2020-04-05', '1235', '1236', false),
       (2, 'any description bb', CURRENT_DATE + 7, 'API_TEST_USER', '2020-04-05', '1235', '1236', true);

INSERT INTO SHIFT_NOTIFICATION ("ID", "DESCRIPTION", "LAST_MODIFIED_DATE_TIME", "QUANTUM_ID",
                                "SHIFT_DATE", "ACKNOWLEDGED", "NOTIFICATION_TYPE")
values (3, 'any description cc', CURRENT_DATE + 7, 'API_TEST_USER', '2020-04-05', false, 0),
       (4, 'any description dd', CURRENT_DATE, 'API_TEST_USER', '2020-04-05', true, 1)