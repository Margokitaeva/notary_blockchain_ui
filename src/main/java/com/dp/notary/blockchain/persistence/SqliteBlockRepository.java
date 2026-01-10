package com.dp.notary.blockchain.persistence;

import com.dp.notary.blockchain.domain.Block;
import com.dp.notary.blockchain.domain.Transaction;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public class SqliteBlockRepository implements BlockRepository {

    private final JdbcTemplate jdbc;
    private final ObjectMapper om;

    private static final TypeReference<List<Transaction>> TX_LIST = new TypeReference<>() {};

    public SqliteBlockRepository(JdbcTemplate jdbc, ObjectMapper om) {
        this.jdbc = jdbc;
        this.om = om;
    }

    @Override
    public Optional<Block> findHead() {
        var rows = jdbc.query(
                "SELECT height, hash, prev_hash, ts, tx_json FROM blocks ORDER BY height DESC LIMIT 1",
                (rs, i) -> mapBlock(
                        rs.getLong("height"),
                        rs.getString("hash"),
                        rs.getString("prev_hash"),
                        rs.getString("ts"),
                        rs.getString("tx_json")
                )
        );
        return rows.stream().findFirst();
    }

    @Override
    public List<Block> findFromHeight(long fromHeight, int limit) {
        return jdbc.query(
                "SELECT height, hash, prev_hash, ts, tx_json FROM blocks WHERE height >= ? ORDER BY height ASC LIMIT ?",
                (rs, i) -> mapBlock(
                        rs.getLong("height"),
                        rs.getString("hash"),
                        rs.getString("prev_hash"),
                        rs.getString("ts"),
                        rs.getString("tx_json")
                ),
                fromHeight, limit
        );
    }

    @Override
    public void append(Block b) {
        try {
            String txJson = om.writeValueAsString(b.transactions());
            jdbc.update(
                    "INSERT INTO blocks(height, hash, prev_hash, ts, tx_json) VALUES(?,?,?,?,?)",
                    b.height(),
                    b.hash(),
                    b.prevHash(),
                    b.timestamp().toString(),
                    txJson
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to persist block", e);
        }
    }

    @Override
    public boolean existsHeight(long height) {
        Integer cnt = jdbc.queryForObject("SELECT COUNT(1) FROM blocks WHERE height = ?", Integer.class, height);
        return cnt != null && cnt > 0;
    }

    private Block mapBlock(long height, String hash, String prevHash, String ts, String txJson) {
        try {
            List<Transaction> txs = om.readValue(txJson, TX_LIST);
            return new Block(height, hash, prevHash, Instant.parse(ts), txs);
        } catch (Exception e) {
            throw new RuntimeException("Failed to map block", e);
        }
    }
}

