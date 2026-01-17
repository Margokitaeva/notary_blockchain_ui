package com.dp.notary.blockchain.blockchain.persistence;

import com.dp.notary.blockchain.blockchain.model.BlockEntity;
import com.dp.notary.blockchain.blockchain.model.TransactionEntity;
import com.dp.notary.blockchain.blockchain.model.TransactionType;
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

    private static final TypeReference<List<String>> TX_LIST = new TypeReference<>() {};

    public SqliteBlockRepository(JdbcTemplate jdbc, ObjectMapper om) {
        this.jdbc = jdbc;
        this.om = om;
    }

    @Override
    public Optional<BlockEntity> findHead() {
        var rows = jdbc.query(
                "SELECT height, prev_hash, time_stamp, tx_json FROM blocks ORDER BY height DESC LIMIT 1",
                (rs, i) -> mapEntity(
                        rs.getLong("height"),
                        rs.getString("prev_hash"),
                        rs.getString("time_stamp"),
                        rs.getString("tx_json")
                )
        );
        return rows.stream().findFirst();
    }

    @Override
    public long getHeight() {
        Long count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM blocks",
                Long.class
        );
        return count != null ? count : 0;
    }

    @Override
    public List<BlockEntity> findFromHeight(long fromHeight, int limit) {
        return jdbc.query(
                "SELECT height, prev_hash, time_stamp, tx_json FROM blocks WHERE height >= ? ORDER BY height ASC LIMIT ?",
                (rs, i) -> mapEntity(
                        rs.getLong("height"),
                        rs.getString("prev_hash"),
                        rs.getString("time_stamp"),
                        rs.getString("tx_json")
                ),
                fromHeight, limit
        ).stream().toList();
    }

    @Override
    public void append(BlockEntity b) {
        try {
            String txJson = om.writeValueAsString(b.getTransactions());
            jdbc.update(
                    "INSERT INTO blocks(prev_hash, time_stamp, tx_json) VALUES(?,?,?)",
                    b.getPrevHash(),
                    b.getTimestamp().toString(),
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

    private BlockEntity mapEntity(long height, String prevHash, String ts, String txJson) {
        try {
            List<String> txs = om.readValue(txJson, TX_LIST);
            return new BlockEntity(height, prevHash, Instant.parse(ts), txs);
        } catch (Exception e) {
            throw new RuntimeException("Failed to map block", e);
        }
    }
}
