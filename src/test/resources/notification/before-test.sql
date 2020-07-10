INSERT INTO shift_notification (quantum_id, shift_date, shift_modified, processed, shift_type, action_type)
VALUES ('API_TEST_USER', '2020-04-05', CURRENT_DATE, false, 'SHIFT', 'ADD'),
       ('API_TEST_USER', '2020-04-05', CURRENT_DATE + 7, true, 'SHIFT', 'EDIT');

INSERT INTO shift_notification (quantum_id, shift_date, shift_modified, task_start, task_end, task, processed,
                                shift_type, action_type)
VALUES ('API_TEST_USER', '2020-04-05', CURRENT_DATE + 7, '1235', '1236', 'watch', false, 'OVERTIME_TASK', 'DELETE'),
       ('API_TEST_USER', '2020-04-05', CURRENT_DATE, '1235', '1236', 'watch', true, 'SHIFT_TASK', 'EDIT');