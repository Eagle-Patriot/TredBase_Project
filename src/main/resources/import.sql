
INSERT INTO parent (id, name, balance) VALUES (1, 'Parent A', 1000.0);
INSERT INTO parent (id, name, balance) VALUES (2, 'Parent B', 1000.0);

INSERT INTO student (student_id, student_name, balance) VALUES (1, 'Student 1 - Shared', 0.0);
INSERT INTO student (student_id, student_name, balance) VALUES (2, 'Student 2 - Unique to Parent A', 0.0);
INSERT INTO student (student_id, student_name, balance) VALUES (3, 'Student 3 - Unique to Parent B', 0.0);

-- Student 1 is shared by both parents
INSERT INTO parent_student (student_id, parent_id) VALUES (1, 1);
INSERT INTO parent_student (student_id, parent_id) VALUES (1, 2);

-- Student 2 is associated only with Parent A
INSERT INTO parent_student (student_id, parent_id) VALUES (2, 1);

-- Student 3 is associated only with Parent B
INSERT INTO parent_student (student_id, parent_id) VALUES (3, 2);
