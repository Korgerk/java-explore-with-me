DROP TABLE IF EXISTS compilation_events;

DROP SEQUENCE IF EXISTS comp_seq;
DROP TABLE IF EXISTS compilations;

DROP SEQUENCE IF EXISTS req_seq;
DROP TABLE IF EXISTS requests;

DROP SEQUENCE IF EXISTS event_seq;
DROP TABLE IF EXISTS events;

DROP SEQUENCE IF EXISTS user_seq;
DROP TABLE IF EXISTS users;

DROP SEQUENCE IF EXISTS cat_seq;
DROP TABLE IF EXISTS categories;


CREATE SEQUENCE IF NOT EXISTS user_seq START 1 INCREMENT 1;
CREATE TABLE IF NOT EXISTS users (
    id INT8 PRIMARY KEY,
    name VARCHAR(255),
    email VARCHAR(255) NOT NULL UNIQUE
);

CREATE SEQUENCE IF NOT EXISTS cat_seq START 1 INCREMENT 1;
CREATE TABLE IF NOT EXISTS categories (
    id INTEGER PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

CREATE SEQUENCE IF NOT EXISTS event_seq START 1 INCREMENT 1;
CREATE TABLE IF NOT EXISTS events (
    id INT8 PRIMARY KEY,
    title VARCHAR(120) NOT NULL,
    annotation VARCHAR(2000) NOT NULL,
    description VARCHAR(7000) NOT NULL,
    initiator_id INT8 NOT NULL,
    category_id INTEGER NOT NULL,
    event_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    confirmed_requests INTEGER,
    lat FLOAT,
    lon FLOAT,
    paid BOOLEAN DEFAULT FALSE,
    participant_limit INTEGER DEFAULT 0,
    request_moderation BOOLEAN DEFAULT TRUE,
    state VARCHAR(20) NOT NULL,
    views INT8 DEFAULT 0,
    created_on TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    published_on TIMESTAMP WITHOUT TIME ZONE,

    CONSTRAINT fk_events_categories FOREIGN KEY (category_id) REFERENCES categories (id),
    CONSTRAINT fk_events_users FOREIGN KEY (initiator_id) REFERENCES users (id)
);

CREATE SEQUENCE IF NOT EXISTS req_seq START 1 INCREMENT 1;
CREATE TABLE IF NOT EXISTS requests (
    id INT8 PRIMARY KEY,
    event_id INT8 NOT NULL,
    requester_id INT8 NOT NULL,
    status VARCHAR(20) NOT NULL,
    created TIMESTAMP WITHOUT TIME ZONE NOT NULL,

    CONSTRAINT fk_requests_events FOREIGN KEY (event_id) REFERENCES events (id),
    CONSTRAINT fk_requests_users FOREIGN KEY (requester_id) REFERENCES users (id),
    CONSTRAINT uq_request UNIQUE (event_id, requester_id)
);

CREATE SEQUENCE IF NOT EXISTS comp_seq START 1 INCREMENT 1;
CREATE TABLE IF NOT EXISTS compilations (
    id INT8 PRIMARY KEY,
    title VARCHAR(50) NOT NULL,
    pinned BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS compilation_events (
    compilation_id BIGINT NOT NULL,
    event_id BIGINT NOT NULL,

    CONSTRAINT pk_compilation_events PRIMARY KEY (compilation_id, event_id),
    CONSTRAINT fk_compilation_events_compilations FOREIGN KEY (compilation_id) REFERENCES compilations (id),
    CONSTRAINT fk_compilation_events_events FOREIGN KEY (event_id) REFERENCES events (id)
);