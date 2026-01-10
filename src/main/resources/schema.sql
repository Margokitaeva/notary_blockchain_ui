CREATE TABLE IF NOT EXISTS blocks (
                                      height     INTEGER PRIMARY KEY,
                                      hash       TEXT NOT NULL,
                                      prev_hash  TEXT,
                                      ts         TEXT NOT NULL,
                                      tx_json    TEXT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_blocks_height ON blocks(height);
