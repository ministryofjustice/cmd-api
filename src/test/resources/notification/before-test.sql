INSERT INTO shift_notification (quantum_id, shift_date, shift_modified, processed, shift_type, action_type)
VALUES ('API_TEST_USER', '2020-04-05', CURRENT_DATE, false, 'shift', 'add'),
       ('API_TEST_USER', '2020-04-05', CURRENT_DATE + 7, true, 'shift', 'edit');

INSERT INTO shift_notification (quantum_id, shift_date, shift_modified, task_start, task_end, task, processed,
                                shift_type, action_type)
VALUES ('API_TEST_USER', '2020-04-05', CURRENT_DATE + 7, '1235', '1236', 'watch', false, 'overtime', 'delete'),
       ('API_TEST_USER', '2020-04-05', CURRENT_DATE, '1235', '1236', 'watch', true, 'shift', 'edit');