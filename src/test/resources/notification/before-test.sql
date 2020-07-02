INSERT INTO SHIFT_TASK_NOTIFICATION ("UUID", "DESCRIPTION", "LAST_MODIFIED_DATE_TIME", "QUANTUM_ID", "DATE_TIME",
                                     "TASK_DATE", "TASK_START_TIME_IN_SECONDS", "TASK_END_TIME_IN_SECONDS",
                                     "LAST_MODIFIED_DATE_TIME_IN_SECONDS", "READ")
values ('a3eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'any description', CURRENT_DATE, 'API_TEST_USER', '2020-04-05',
        '2020-04-05', '1234', '1235', '1236', false),
       ('a4eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'any description', CURRENT_DATE + 7, 'API_TEST_USER', '2020-04-05',
        '2020-04-05', '1234', '1235', '1236', true);

INSERT INTO SHIFT_NOTIFICATION ("UUID", "DESCRIPTION", "LAST_MODIFIED_DATE_TIME", "QUANTUM_ID", "DATE_TIME",
                                "SHIFT_DATE", "LAST_MODIFIED_DATE_TIME_IN_SECONDS", "READ")
values ('a5eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'any description', CURRENT_DATE + 7, 'API_TEST_USER', '2020-04-05',
        '2020-04-05', '1236', false),
       ('a6eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'any description', CURRENT_DATE, 'API_TEST_USER', '2020-04-05',
        '2020-04-05', '1236', true)