CREATE TABLE IF NOT EXISTS blocks (
                                      height     INTEGER PRIMARY KEY,
                                      hash       TEXT NOT NULL,
                                      prev_hash  TEXT,
                                      ts         TEXT NOT NULL,
                                      tx_json    TEXT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_blocks_height ON blocks(height);

CREATE TABLE IF NOT EXISTS tx_state (
    tx_id       TEXT NOT NULL,
    scope       TEXT NOT NULL,
    type        TEXT,
    payload     TEXT,
    created_by  TEXT,
    status      TEXT,
    company_id  TEXT,
    company_name TEXT,
    amount      TEXT,
    ts          TEXT,
    target      TEXT,
    owner_id    TEXT,
    owner_name  TEXT,
    owner_surname TEXT,
    PRIMARY KEY (tx_id, scope)
);

CREATE INDEX IF NOT EXISTS idx_tx_state_scope ON tx_state(scope);

CREATE TABLE IF NOT EXISTS tx_id_sequence (
    name         TEXT PRIMARY KEY,
    current_value INTEGER NOT NULL
);

INSERT OR IGNORE INTO tx_id_sequence(name, current_value) VALUES ('tx', 0);

CREATE TABLE IF NOT EXISTS tx_id_keys (
    client_key TEXT PRIMARY KEY,
    tx_id      TEXT NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS replica_draft_buffer (
    client_key TEXT PRIMARY KEY,
    payload    TEXT NOT NULL,
    created_at TEXT
);
