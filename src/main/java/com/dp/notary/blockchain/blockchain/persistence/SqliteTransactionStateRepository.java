package com.dp.notary.blockchain.blockchain.persistence;

import com.dp.notary.blockchain.blockchain.model.TransactionEntity;
import com.dp.notary.blockchain.blockchain.model.TransactionStatus;
import com.dp.notary.blockchain.blockchain.model.TransactionType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.apache.logging.log4j.util.Strings.trimToNull;

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
                    amount = ?,
                    target = ?
                WHERE tx_key = ?
                """,
                tx.getType().name(),
                tx.getAmount(),
                tx.getTarget(),
                tx.getTxId()
        );
    }

    @Override
    public void delete(String txId) {
        int deleted = jdbc.update(
                "DELETE FROM transactions WHERE tx_key = ?",
                txId
        );
    }
    @Override
    public String insert(TransactionEntity tx) {

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    """
                    INSERT INTO transactions(
                         tx_key,
                         timestamp,
                         type,
                         created_by,
                         status,
                         amount,
                         target,
                        initiator
    )
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?)
    """,
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1,tx.getTxId());
            ps.setString(2, tx.getTimestamp().toString());
            ps.setString(3, tx.getType().name());
            ps.setString(4, tx.getCreatedBy());
            ps.setString(5, tx.getStatus().toString());
            ps.setString(6, tx.getAmount().toString());
            ps.setString(7, tx.getTarget());
            ps.setString(8, tx.getInitiator());
            return ps;
        });

        return tx.getTxId();
    }


    @Override
    public boolean updateStatus(String txId, TransactionStatus status) {
        int updated = jdbc.update(
                "UPDATE transactions SET status = ? WHERE tx_key = ?",
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
                """
                         SELECT tx_key,
                         timestamp,
                         type,
                         created_by,
                         status,
                         amount,
                         target,
                        initiator
                FROM transactions WHERE status = ?
                """,
                this::mapRow,
                status.name()
        );
    }

    @Override
    public List<TransactionEntity> findByStatus(TransactionStatus status, String createdByFilter, String initiatorFilter, String targetFilter, TransactionType typeFilter) {
       if (status == null) {
           return List.of();
       }
        createdByFilter = trimToNull(createdByFilter);
        initiatorFilter = trimToNull(initiatorFilter);
        targetFilter    = trimToNull(targetFilter);
        String typeFilterString = typeFilter != null ? typeFilter.name() : null;
        return jdbc.query(
               """
                           SELECT tx_key,
                           timestamp,
                           type,
                           created_by,
                           status,
                           amount,
                           target,
                           initiator
               FROM transactions WHERE status = ?
                   AND (? IS NULL OR created_by = ?)
                   AND (? IS NULL OR initiator = ?)
                   AND (? IS NULL OR target = ?)
                   AND (? IS NULL OR type = ?)
               """,
                this::mapRow,
                status.name(),
                createdByFilter, createdByFilter,
                initiatorFilter, initiatorFilter,
                targetFilter, targetFilter,
                typeFilterString, typeFilterString
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
    public int countByStatus(TransactionStatus status, String createdByFilter, String initiatorFilter, String targetFilter, TransactionType typeFilter) {
        if (status == null) {
            return 0;
        }

        createdByFilter = trimToNull(createdByFilter);
        initiatorFilter = trimToNull(initiatorFilter);
        targetFilter    = trimToNull(targetFilter);
        String typeFilterString = typeFilter != null ? typeFilter.name() : null;

        Integer cnt = jdbc.queryForObject(
            """
                    SELECT COUNT(1) FROM transactions WHERE status=?
                        AND (? IS NULL OR created_by = ?)
                        AND (? IS NULL OR initiator = ?)
                        AND (? IS NULL OR target = ?)
                        AND (? IS NULL OR type = ?)
                """,
                Integer.class,
                status.name(),
                createdByFilter, createdByFilter,
                initiatorFilter, initiatorFilter,
                targetFilter, targetFilter,
                typeFilterString, typeFilterString
        );
        return cnt == null ? 0 : cnt;
    }

    @Override
    public Optional<TransactionEntity> find(String txId) {
        var rows = jdbc.query(
                """
                         SELECT tx_key,
                         timestamp,
                         type,
                         created_by,
                         status,
                         amount,
                         target,
                        initiator FROM transactions WHERE tx_key = ?
                        """,
                this::mapRow, txId
        );
        return rows.stream().findFirst();
    }

    private TransactionEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new TransactionEntity(
                rs.getString("tx_key"),
                Instant.parse(rs.getString("timestamp")),
                TransactionType.valueOf(rs.getString("type")),
                rs.getString("created_by"),
                TransactionStatus.valueOf(rs.getString("status")),
                new BigDecimal(rs.getString("amount")),
                rs.getString("target"),
                rs.getString("initiator")
        );
    }
}
