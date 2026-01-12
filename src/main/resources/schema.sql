CREATE TABLE IF NOT EXISTS blocks (
                                      height     INTEGER PRIMARY KEY,
                                      hash       TEXT NOT NULL,
                                      prev_hash  TEXT,
                                      ts         TEXT NOT NULL,
                                      tx_json    TEXT NOT NULL
CREATE TABLE IF NOT EXISTS users (
                                     id INTEGER PRIMARY KEY AUTOINCREMENT,
                                     name TEXT NOT NULL UNIQUE,
                                     password_hash TEXT NOT NULL,
                                     role TEXT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_blocks_height ON blocks(height);
