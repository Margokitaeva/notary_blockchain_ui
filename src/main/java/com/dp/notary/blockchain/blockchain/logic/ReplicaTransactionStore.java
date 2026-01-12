package com.dp.notary.blockchain.blockchain.logic;

import com.dp.notary.blockchain.blockchain.model.Company;
import com.dp.notary.blockchain.blockchain.model.Transaction;
import com.dp.notary.blockchain.blockchain.model.TransactionStatus;
import com.dp.notary.blockchain.blockchain.model.TransactionType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ReplicaTransactionStore {

    private final Map<String, Transaction> txs = new ConcurrentHashMap<>();

    public void saveDraft(Transaction tx) {
        txs.put(tx.txId(), copyWithStatus(tx, TransactionStatus.DRAFT));
    }

    public void saveSubmitted(Transaction tx) {
        txs.put(tx.txId(), copyWithStatus(tx, TransactionStatus.SUBMITTED));
    }

    public void saveDeclined(Transaction tx) {
        txs.put(tx.txId(), copyWithStatus(tx, TransactionStatus.DECLINED));
    }

    public List<Transaction> drafts() {
        return byStatus(TransactionStatus.DRAFT);
    }

    public List<Transaction> submitted() {
        return byStatus(TransactionStatus.SUBMITTED);
    }

    public List<Transaction> declined() {
        return byStatus(TransactionStatus.DECLINED);
    }

    private List<Transaction> byStatus(TransactionStatus status) {
        return txs.values().stream()
                .filter(tx -> tx.status() == status)
                .toList();
    }

    private Transaction copyWithStatus(Transaction tx, TransactionStatus status) {
        Company company = tx.company() == null ? null : new Company(tx.company().id(), tx.company().name());
        TransactionType type = tx.type();
        return new Transaction(
                tx.txId(),
                type,
                tx.payload(),
                tx.createdBy(),
                status,
                company
        );
    }
}
