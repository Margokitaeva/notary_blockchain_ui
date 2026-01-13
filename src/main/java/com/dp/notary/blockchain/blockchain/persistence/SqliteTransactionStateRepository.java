package com.dp.notary.blockchain.blockchain.persistence;

import com.dp.notary.blockchain.blockchain.model.TransactionEntity;
import com.dp.notary.blockchain.blockchain.model.TransactionStatus;
import com.dp.notary.blockchain.blockchain.model.TransactionType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class SqliteTransactionStateRepository implements TransactionStateRepository {

    private final JdbcTemplate jdbc;

    public SqliteTransactionStateRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void update(TransactionEntity tx) {
        int updated = jdbc.update(
                """
                UPDATE transactions
                SET type = ?,
                    payload = ?,
                    created_by = ?,
                    status = ?
                WHERE tx_id = ?
                """,
                tx.getType().name(),
                tx.getPayload(),
                tx.getCreatedBy(),
                tx.getStatus().name(),
                tx.getTxId()
        );
    }

    @Override
    public void delete(int txId) {
        int deleted = jdbc.update(
                "DELETE FROM transactions WHERE tx_id = ?",
                txId
        );
    }
    @Override
    public void insert(TransactionEntity tx) {
        jdbc.update(
                """
                INSERT INTO transactions(tx_id, type, payload, created_by, status)
                VALUES(?,?,?,?,?)
                """,
                tx.getTxId(),
                tx.getType(),
                tx.getPayload(),
                tx.getCreatedBy(),
                tx.getStatus()
        );
    }

    @Override
    public boolean updateStatus(int txId, TransactionStatus status) {
        int updated = jdbc.update(
                "UPDATE transactions SET status = ? WHERE tx_id = ?",
                status.name(), txId
        );
        return updated > 0;
    }

    @Override
    public List<TransactionEntity> findByStatus(TransactionStatus status) {
        if (status == null) {
            return List.of();
        }
        return jdbc.query(
                "SELECT tx_id, type, payload, created_by, status FROM transactions WHERE status = ?",
                this::mapRow,
                status.name()
        );
    }

    @Override
    public int countByStatus(TransactionStatus status) {
        if (status == null) {
            return 0;
        }

        Integer cnt = jdbc.queryForObject(
                "SELECT COUNT(1) FROM transactions WHERE status=?",
                Integer.class,
                status.name()
        );
        return cnt == null ? 0 : cnt;
    }

    @Override
    public Optional<TransactionEntity> find(int txId) {
        var rows = jdbc.query(
                "SELECT tx_id, type, payload, created_by, status FROM transactions WHERE tx_id = ?",
                this::mapRow, txId
        );
        return rows.stream().findFirst();
    }

    private TransactionEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new TransactionEntity(
                rs.getInt("tx_id"),
                TransactionType.valueOf(rs.getString("type")),
                rs.getString("payload"),
                rs.getString("created_by"),
                TransactionStatus.valueOf(rs.getString("status"))
        );
    }
}
