CREATE TABLE IF NOT EXISTS blocks
(
    height     INTEGER PRIMARY KEY AUTOINCREMENT,
    prev_hash  TEXT,
    time_stamp TEXT NOT NULL,
    tx_json    TEXT NOT NULL
);
INSERT OR IGNORE INTO blocks(height,prev_hash,time_stamp,tx_json)
VALUES (0,'','1970-01-01T00:00:00Z','[]');
CREATE TABLE IF NOT EXISTS users
(
    name          TEXT NOT NULL UNIQUE PRIMARY KEY,
    password_hash TEXT NOT NULL,
    role          TEXT NOT NULL
);
CREATE TABLE IF NOT EXISTS owners
(
    name_surname TEXT NOT NULL UNIQUE PRIMARY KEY,
    shares TEXT NOT NULL,
    locked_shares  TEXT NOT NULL DEFAULT '0'
);
INSERT OR IGNORE INTO owners(name_surname, shares)
VALUES ('Luisa Gemini', 1000);
INSERT OR IGNORE INTO owners(name_surname, shares)
VALUES ('Santiago Pete', 1000);
INSERT OR IGNORE INTO owners(name_surname, shares)
VALUES ('Company', 10000);

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

