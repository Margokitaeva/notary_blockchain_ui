CREATE TABLE IF NOT EXISTS blocks
(
    height     INTEGER PRIMARY KEY AUTOINCREMENT,
    prev_hash  TEXT,
    time_stamp TEXT NOT NULL,
    tx_json    TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS users
(
    name          TEXT NOT NULL UNIQUE PRIMARY KEY,
    password_hash TEXT NOT NULL,
    role          TEXT NOT NULL
);
CREATE TABLE IF NOT EXISTS owners
(
    name_surname TEXT NOT NULL UNIQUE PRIMARY KEY
);
INSERT OR IGNORE INTO owners(name_surname)
VALUES ('Chuila Chujevich');
INSERT OR IGNORE INTO owners(name_surname)
VALUES ('Pizdziuk Pizdzievich');

CREATE TABLE IF NOT EXISTS transactions
(
    tx_key     TEXT PRIMARY KEY,
    timestamp  TEXT,
    type       TEXT,
    created_by TEXT REFERENCES users (name),
    status     TEXT,
    amount     TEXT,
    target     TEXT REFERENCES owners (name_surname),
    initiator  TEXT REFERENCES owners (name_surname)
);


CREATE TABLE IF NOT EXISTS replica_draft_buffer
(
    client_key TEXT PRIMARY KEY,
    payload    TEXT NOT NULL,
    created_at TEXT
);
