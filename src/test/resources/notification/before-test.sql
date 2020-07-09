INSERT INTO shift_task_notification (id, description, last_modified, quantum_id,
                                     task_date, task_start_time_in_seconds, task_end_time_in_seconds,
                                     processed)
values (1, 'any description aa', CURRENT_DATE, 'API_TEST_USER', '2020-04-05', '1235', '1236', false),
       (2, 'any description bb', CURRENT_DATE + 7, 'API_TEST_USER', '2020-04-05', '1235', '1236', true);

INSERT INTO shift_notification (id, description, last_modified, quantum_id,
                                shift_date, processed, notification_type)
values (3, 'any description cc', CURRENT_DATE + 7, 'API_TEST_USER', '2020-04-05', false, 0),
       (4, 'any description dd', CURRENT_DATE, 'API_TEST_USER', '2020-04-05', true, 1)