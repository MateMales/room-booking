CREATE EXTENSION IF NOT EXISTS btree_gist;

CREATE TABLE users (
    id          SERIAL PRIMARY KEY,
    first_name  VARCHAR(50)  NOT NULL,
    last_name   VARCHAR(50)  NOT NULL,
    email       VARCHAR(100) NOT NULL UNIQUE,
    role        VARCHAR(20)  NOT NULL DEFAULT 'STUDENT'
                CHECK (role IN ('STUDENT', 'PROFESSOR', 'ADMIN'))
);

CREATE TABLE buildings (
    id       SERIAL PRIMARY KEY,
    name     VARCHAR(100) NOT NULL UNIQUE,
    address  VARCHAR(200)
);

CREATE TABLE rooms (
    id           SERIAL PRIMARY KEY,
    building_id  INT NOT NULL REFERENCES buildings(id),
    name         VARCHAR(50) NOT NULL,
    capacity     INT NOT NULL CHECK (capacity > 0),
    type         VARCHAR(20) NOT NULL
                 CHECK (type IN ('CLASSROOM', 'LAB', 'AMPHITHEATER')),
    UNIQUE (building_id, name)
);

CREATE TABLE reservations (
    id          SERIAL PRIMARY KEY,
    user_id     INT NOT NULL REFERENCES users(id),
    room_id     INT NOT NULL REFERENCES rooms(id),
    start_time  TIMESTAMP NOT NULL,
    end_time    TIMESTAMP NOT NULL,
    purpose     VARCHAR(200),
    status      VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
                CHECK (status IN ('ACTIVE', 'CANCELLED')),
    created_at  TIMESTAMP NOT NULL DEFAULT now(),
    CHECK (end_time > start_time),
    CHECK (end_time - start_time <= INTERVAL '4 hours'),
    CONSTRAINT no_overlap EXCLUDE USING gist (
        room_id WITH =,
        tsrange(start_time, end_time) WITH &&
    ) WHERE (status = 'ACTIVE')
);