INSERT INTO users (first_name, last_name, email, role) VALUES
    ('Mate',  'Matić',    'mate@etf.rs',  'STUDENT'),
    ('Ana',   'Anić',     'ana@etf.rs',   'STUDENT'),
    ('Petar', 'Petrović', 'petar@etf.rs', 'PROFESSOR'),
    ('Admin', 'Admin',    'admin@etf.rs', 'ADMIN');

INSERT INTO buildings (name, address) VALUES
    ('Building A', NULL),
    ('Building B', NULL);

INSERT INTO rooms (building_id, name, capacity, type) VALUES
    (1, '101',            60,  'CLASSROOM'),
    (1, 'Amphitheater 1', 200, 'AMPHITHEATER'),
    (2, 'Lab 5',          20,  'LAB'),
    (2, 'Lab 6',          20,  'LAB');

INSERT INTO reservations (user_id, room_id, start_time, end_time, purpose) VALUES
    (1, 3, '2026-10-20 10:00', '2026-10-20 12:00', 'Lab exercise'),
    (2, 1, '2026-10-20 14:00', '2026-10-20 15:30', 'Study group'),
    (3, 2, '2026-10-21 09:00', '2026-10-21 11:00', 'Lecture');