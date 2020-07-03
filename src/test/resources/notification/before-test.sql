INSERT INTO SHIFT_TASK_NOTIFICATION ("ID", "DESCRIPTION", "LAST_MODIFIED_DATE_TIME", "QUANTUM_ID", "DATE_TIME",
                                     "TASK_DATE", "TASK_START_TIME_IN_SECONDS", "TASK_END_TIME_IN_SECONDS",
                                     "LAST_MODIFIED_DATE_TIME_IN_SECONDS", "READ")
values (1, 'any description aa', CURRENT_DATE, 'API_TEST_USER', '2020-04-05',
        '2020-04-05', '1234', '1235', '1236', false),
       (2, 'any description bb', CURRENT_DATE + 7, 'API_TEST_USER', '2020-04-05',
        '2020-04-05', '1234', '1235', '1236', true);

INSERT INTO SHIFT_NOTIFICATION ("ID", "DESCRIPTION", "LAST_MODIFIED_DATE_TIME", "QUANTUM_ID", "DATE_TIME",
                                "SHIFT_DATE", "LAST_MODIFIED_DATE_TIME_IN_SECONDS", "READ", "NOTIFICATION_TYPE")
values (3, 'any description cc', CURRENT_DATE + 7, 'API_TEST_USER', '2020-04-05',
        '2020-04-05', '1236', false, 0),
       (4, 'any description dd', CURRENT_DATE, 'API_TEST_USER', '2020-04-05',
        '2020-04-05', '1236', true, 1)