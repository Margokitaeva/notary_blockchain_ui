package com.dp.notary.blockchain.core;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class TransactionIdGenerator {

    private static final String SEQ_NAME = "tx";

    private final JdbcTemplate jdbc;

    public TransactionIdGenerator(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
        ensureSequenceRow();
    }

    public String nextId(String clientKey) {
        String key = normalize(clientKey);
        if (key != null) {
            String existing = findExisting(key);
            if (existing != null) {
                return existing;
            }
        }

        String txId = Long.toString(incrementSequence());

        if (key != null) {
            try {
                jdbc.update("INSERT INTO tx_id_keys(client_key, tx_id) VALUES(?, ?)", key, txId);
            } catch (DuplicateKeyException e) {
                String existing = findExisting(key);
                if (existing != null) {
                    return existing;
                }
                throw e;
            }
        }

        return txId;
    }

    private void ensureSequenceRow() {
        jdbc.update("INSERT OR IGNORE INTO tx_id_sequence(name, current_value) VALUES(?, 0)", SEQ_NAME);
    }

    private long incrementSequence() {
        Long value = jdbc.queryForObject(
                "UPDATE tx_id_sequence SET current_value = current_value + 1 WHERE name = ? RETURNING current_value",
                Long.class,
                SEQ_NAME
        );
        if (value == null) {
            throw new IllegalStateException("Failed to increment transaction sequence");
        }
        return value;
    }

    private String findExisting(String clientKey) {
        return jdbc.query(
                "SELECT tx_id FROM tx_id_keys WHERE client_key = ?",
                rs -> rs.next() ? rs.getString("tx_id") : null,
                clientKey
        );
    }

    private String normalize(String clientKey) {
        return clientKey == null || clientKey.isBlank() ? null : clientKey;
    }
}
