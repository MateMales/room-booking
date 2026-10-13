-- Generates a large dataset for performance testing.
-- Run on an empty database after schema.sql and data.sql.
-- Result: 10,000 users, 200 rooms, 2,000,000 non-overlapping reservations.

INSERT INTO users (first_name, last_name, email, role)
SELECT 'User', 'Number' || g, 'user' || g || '@etf.rs', 'STUDENT'
FROM generate_series(1, 10000) AS g;

INSERT INTO rooms (building_id, name, capacity, type)
SELECT 1 + (g % 2), 'Room ' || g, 30, 'CLASSROOM'
FROM generate_series(1, 200) AS g;

INSERT INTO reservations (user_id, room_id, start_time, end_time, purpose)
SELECT
    1 + floor(random() * 10000)::int,
    r.id,
    TIMESTAMP '2027-01-01 08:00' + (n / 6) * INTERVAL '1 day' + (n % 6) * INTERVAL '2 hours',
    TIMESTAMP '2027-01-01 10:00' + (n / 6) * INTERVAL '1 day' + (n % 6) * INTERVAL '2 hours',
    'Generated'
FROM rooms r
    CROSS JOIN generate_series(0, 9999) AS n
WHERE r.name LIKE 'Room %';

ANALYZE;