package com.dp.notary.blockchain.blockchain.persistence;

import com.dp.notary.blockchain.blockchain.entity.BlockEntity;
import com.dp.notary.blockchain.blockchain.entity.CompanyEntity;
import com.dp.notary.blockchain.blockchain.entity.OwnerEntity;
import com.dp.notary.blockchain.blockchain.entity.TransactionEntity;
import com.dp.notary.blockchain.blockchain.model.Block;
import com.dp.notary.blockchain.blockchain.model.Company;
import com.dp.notary.blockchain.blockchain.model.Owner;
import com.dp.notary.blockchain.blockchain.model.Transaction;
import com.dp.notary.blockchain.blockchain.model.TransactionStatus;
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

    private static final TypeReference<List<TransactionEntity>> TX_LIST = new TypeReference<>() {};

    public SqliteBlockRepository(JdbcTemplate jdbc, ObjectMapper om) {
        this.jdbc = jdbc;
        this.om = om;
    }

    @Override
    public Optional<Block> findHead() {
        var rows = jdbc.query(
                "SELECT height, hash, prev_hash, ts, tx_json FROM blocks ORDER BY height DESC LIMIT 1",
                (rs, i) -> mapEntity(
                        rs.getLong("height"),
                        rs.getString("hash"),
                        rs.getString("prev_hash"),
                        rs.getString("ts"),
                        rs.getString("tx_json")
                )
        );
        return rows.stream().findFirst().map(this::toDomain);
    }

    @Override
    public List<Block> findFromHeight(long fromHeight, int limit) {
        return jdbc.query(
                "SELECT height, hash, prev_hash, ts, tx_json FROM blocks WHERE height >= ? ORDER BY height ASC LIMIT ?",
                (rs, i) -> mapEntity(
                        rs.getLong("height"),
                        rs.getString("hash"),
                        rs.getString("prev_hash"),
                        rs.getString("ts"),
                        rs.getString("tx_json")
                ),
                fromHeight, limit
        ).stream().map(this::toDomain).toList();
    }

    @Override
    public void append(Block b) {
        try {
            BlockEntity entity = toEntity(b);
            String txJson = om.writeValueAsString(entity.getTransactions());
            jdbc.update(
                    "INSERT INTO blocks(height, hash, prev_hash, ts, tx_json) VALUES(?,?,?,?,?)",
                    entity.getHeight(),
                    entity.getHash(),
                    entity.getPrevHash(),
                    entity.getTimestamp().toString(),
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

    private BlockEntity mapEntity(long height, String hash, String prevHash, String ts, String txJson) {
        try {
            List<TransactionEntity> txs = om.readValue(txJson, TX_LIST);
            return new BlockEntity(height, hash, prevHash, Instant.parse(ts), txs);
        } catch (Exception e) {
            throw new RuntimeException("Failed to map block", e);
        }
    }

    private Block toDomain(BlockEntity entity) {
        List<Transaction> txs = entity.getTransactions().stream()
                .map(this::toDomain)
                .toList();
        return new Block(entity.getHeight(), entity.getHash(), entity.getPrevHash(), entity.getTimestamp(), txs);
    }

    private BlockEntity toEntity(Block block) {
        List<TransactionEntity> txs = block.transactions().stream()
                .map(this::toEntity)
                .toList();
        return new BlockEntity(block.height(), block.hash(), block.prevHash(), block.timestamp(), txs);
    }

    private Transaction toDomain(TransactionEntity entity) {
        Company company = null;
        if (entity.getCompany() != null) {
            company = new Company(entity.getCompany().getId(), entity.getCompany().getName());
        }
        Owner owner = null;
        if (entity.getOwner() != null) {
            owner = new Owner(entity.getOwner().getId(), entity.getOwner().getName(), entity.getOwner().getSurname());
        }
        TransactionStatus status = entity.getStatus() == null ? TransactionStatus.SUBMITTED : entity.getStatus();
        return new Transaction(
                entity.getTxId(),
                safeType(entity.getType()),
                entity.getPayload(),
                entity.getCreatedBy(),
                status,
                company,
                owner,
                entity.getAmount(),
                entity.getTimestamp(),
                entity.getTarget()
        );
    }

    private TransactionEntity toEntity(Transaction tx) {
        CompanyEntity company = null;
        if (tx.company() != null) {
            company = new CompanyEntity(tx.company().id(), tx.company().name());
        }
        OwnerEntity owner = null;
        if (tx.owner() != null) {
            owner = new OwnerEntity(tx.owner().id(), tx.owner().name(), tx.owner().surname());
        }
        return new TransactionEntity(
                tx.txId(),
                tx.type() == null ? null : tx.type().name(),
                tx.payload(),
                tx.createdBy(),
                tx.status(),
                company,
                owner,
                tx.amount(),
                tx.timestamp(),
                tx.target()
        );
    }

    private TransactionType safeType(String raw) {
        if (raw == null) {
            return TransactionType.GRANT;
        }
        try {
            return TransactionType.fromString(raw);
        } catch (IllegalArgumentException e) {
            return TransactionType.GRANT;
        }
    }
}
