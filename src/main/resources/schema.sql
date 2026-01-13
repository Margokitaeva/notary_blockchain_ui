CREATE TABLE IF NOT EXISTS blocks
(
    height INTEGER PRIMARY KEY AUTOINCREMENT,
    hash TEXT NOT NULL,
    prev_hash TEXT,
    time_stamp TEXT NOT NULL,
    tx_json TEXT NOT NULL
);
CREATE TABLE IF NOT EXISTS users
(
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    name          TEXT NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    role          TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS transactions (
    id       TEXT PRIMARY KEY AUTOINCREMENT,
    type        TEXT,
    payload     TEXT,
    created_by  TEXT,
    status      TEXT
);

