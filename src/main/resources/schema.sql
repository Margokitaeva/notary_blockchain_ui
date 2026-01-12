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
