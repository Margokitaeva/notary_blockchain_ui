package com.dp.notary.blockchain.blockchain.persistence;

import com.dp.notary.blockchain.blockchain.entity.CompanyEntity;
import com.dp.notary.blockchain.blockchain.entity.TransactionEntity;
import com.dp.notary.blockchain.blockchain.model.Company;
import com.dp.notary.blockchain.blockchain.model.Transaction;
import com.dp.notary.blockchain.blockchain.model.TransactionScope;
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
    public void upsert(TransactionScope scope, Transaction tx) {
        TransactionEntity entity = toEntity(tx);
        jdbc.update(
                """
                INSERT INTO tx_state(tx_id, scope, type, payload, created_by, status, company_id, company_name)
                VALUES(?,?,?,?,?,?,?,?)
                ON CONFLICT(tx_id, scope) DO UPDATE SET
                    type=excluded.type,
                    payload=excluded.payload,
                    created_by=excluded.created_by,
                    status=excluded.status,
                    company_id=excluded.company_id,
                    company_name=excluded.company_name
                """,
                entity.getTxId(),
                scope.name(),
                entity.getType(),
                entity.getPayload(),
                entity.getCreatedBy(),
                entity.getStatus() == null ? null : entity.getStatus().name(),
                entity.getCompany() == null ? null : entity.getCompany().getId(),
                entity.getCompany() == null ? null : entity.getCompany().getName()
        );
    }

    @Override
    public boolean updateStatus(TransactionScope scope, String txId, TransactionStatus status) {
        int updated = jdbc.update(
                "UPDATE tx_state SET status = ? WHERE tx_id = ? AND scope = ?",
                status.name(), txId, scope.name()
        );
        return updated > 0;
    }

    @Override
    public boolean delete(TransactionScope scope, String txId) {
        int updated = jdbc.update("DELETE FROM tx_state WHERE tx_id = ? AND scope = ?", txId, scope.name());
        return updated > 0;
    }

    @Override
    public void deleteAll(TransactionScope scope, List<String> txIds) {
        if (txIds.isEmpty()) {
            return;
        }
        String inClause = txIds.stream().map(id -> "?").collect(Collectors.joining(","));
        jdbc.update("DELETE FROM tx_state WHERE scope = ? AND tx_id IN (" + inClause + ")",
                prepend(scope.name(), txIds).toArray());
    }

    @Override
    public List<Transaction> findByScopeAndStatuses(TransactionScope scope, List<TransactionStatus> statuses) {
        if (statuses.isEmpty()) {
            return List.of();
        }
        String inClause = statuses.stream().map(s -> "?").collect(Collectors.joining(","));
        return jdbc.query(
                "SELECT tx_id, type, payload, created_by, status, company_id, company_name FROM tx_state WHERE scope = ? AND status IN (" + inClause + ")",
                this::mapRow,
                prepend(scope.name(), statuses.stream().map(TransactionStatus::name).toList()).toArray()
        );
    }

    @Override
    public int countByScopeAndStatuses(TransactionScope scope, List<TransactionStatus> statuses) {
        if (statuses.isEmpty()) {
            return 0;
        }
        String inClause = statuses.stream().map(s -> "?").collect(Collectors.joining(","));
        Integer cnt = jdbc.queryForObject(
                "SELECT COUNT(1) FROM tx_state WHERE scope = ? AND status IN (" + inClause + ")",
                Integer.class,
                prepend(scope.name(), statuses.stream().map(TransactionStatus::name).toList()).toArray()
        );
        return cnt == null ? 0 : cnt;
    }

    @Override
    public Optional<Transaction> find(TransactionScope scope, String txId) {
        var rows = jdbc.query(
                "SELECT tx_id, type, payload, created_by, status, company_id, company_name FROM tx_state WHERE scope = ? AND tx_id = ?",
                this::mapRow,
                scope.name(), txId
        );
        return rows.stream().findFirst();
    }

    private Transaction mapRow(ResultSet rs, int rowNum) throws SQLException {
        Company company = null;
        String companyId = rs.getString("company_id");
        String companyName = rs.getString("company_name");
        if (companyId != null || companyName != null) {
            company = new Company(companyId, companyName);
        }
        TransactionStatus status = rs.getString("status") == null ? TransactionStatus.SUBMITTED : TransactionStatus.valueOf(rs.getString("status"));
        return new Transaction(
                rs.getString("tx_id"),
                safeType(rs.getString("type")),
                rs.getString("payload"),
                rs.getString("created_by"),
                status,
                company
        );
    }

    private TransactionEntity toEntity(Transaction tx) {
        CompanyEntity company = tx.company() == null ? null : new CompanyEntity(tx.company().id(), tx.company().name());
        return new TransactionEntity(
                tx.txId(),
                tx.type() == null ? null : tx.type().name(),
                tx.payload(),
                tx.createdBy(),
                tx.status(),
                company
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

    private List<Object> prepend(String first, List<?> rest) {
        Object[] arr = new Object[rest.size() + 1];
        arr[0] = first;
        for (int i = 0; i < rest.size(); i++) {
            arr[i + 1] = rest.get(i);
        }
        return List.of(arr);
    }
}
