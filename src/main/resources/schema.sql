CREATE TABLE IF NOT EXISTS blocks
(
    height INTEGER PRIMARY KEY AUTOINCREMENT,
    prev_hash TEXT,
    time_stamp TEXT NOT NULL,
    tx_json TEXT NOT NULL
)

CREATE TABLE IF NOT EXISTS users
(
    name          TEXT NOT NULL UNIQUE PRIMARY KEY,
    password_hash TEXT NOT NULL,
    role          TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS transactions (
    client_key TEXT PRIMARY KEY,
    timestamp   TEXT,
    type        TEXT,
    created_by  TEXT FOREIGN KEY,
    status      TEXT,
    amount      TEXT,
    target      INTEGER FOREIGN KEY,
    owner_id    INTEGER FOREIGN KEY
);

CREATE TABLE IF NOT EXISTS owners(
    owner_id    INTEGER AUTOINCREMENT,
    name        TEXT,
    surname     TEXT
);
INSERT INTO owners(name,surname) VALUES("Chuila","Chujevich");
INSERT INTO owners(name,surname) VALUES("Pizdziuk","Pizdzievich")

CREATE TABLE IF NOT EXISTS replica_draft_buffer (
    client_key TEXT PRIMARY KEY,
    payload    TEXT NOT NULL,
    created_at TEXT
);
