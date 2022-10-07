INSERT INTO notification (quantum_id,  shift_modified, detail_start, detail_end, activity, processed, parent_type, action_type)
VALUES ('API_TEST_USER', CURRENT_DATE, CURRENT_DATE, CURRENT_DATE, null, false, 'SHIFT', 'ADD'),
       ('API_TEST_USER', CURRENT_DATE + 7, CURRENT_DATE, CURRENT_DATE, null ,true, 'SHIFT', 'EDIT');

INSERT INTO notification (quantum_id, shift_modified, detail_start, detail_end, activity, processed, parent_type, action_type)
VALUES ('API_TEST_USER', CURRENT_DATE + 7, CURRENT_DATE, CURRENT_DATE, 'watch', false, 'OVERTIME', 'DELETE'),
       ('API_TEST_USER', CURRENT_DATE, CURRENT_DATE, CURRENT_DATE, 'watch', true, 'SHIFT', 'EDIT');
