package com.dp.notary.blockchain.blockchain.logic;

import com.dp.notary.blockchain.blockchain.model.Transaction;
import com.dp.notary.blockchain.blockchain.model.TransactionScope;
import com.dp.notary.blockchain.blockchain.model.TransactionStatus;
import com.dp.notary.blockchain.blockchain.persistence.TransactionStateRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReplicaTransactionStore {

    private final TransactionStateRepository repo;

    public ReplicaTransactionStore(TransactionStateRepository repo) {
        this.repo = repo;
    }

    public void saveDraft(Transaction tx) {
        repo.upsert(TransactionScope.REPLICA, withStatus(tx, TransactionStatus.DRAFT));
    }

    public void saveSubmitted(Transaction tx) {
        repo.upsert(TransactionScope.REPLICA, withStatus(tx, TransactionStatus.SUBMITTED));
    }

    public void saveDeclined(Transaction tx) {
        repo.upsert(TransactionScope.REPLICA, withStatus(tx, TransactionStatus.DECLINED));
    }

    public List<Transaction> drafts() {
        return repo.findByScopeAndStatuses(TransactionScope.REPLICA, List.of(TransactionStatus.DRAFT));
    }

    public List<Transaction> submitted() {
        return repo.findByScopeAndStatuses(TransactionScope.REPLICA, List.of(TransactionStatus.SUBMITTED));
    }

    public List<Transaction> declined() {
        return repo.findByScopeAndStatuses(TransactionScope.REPLICA, List.of(TransactionStatus.DECLINED));
    }

    private Transaction withStatus(Transaction tx, TransactionStatus status) {
        return new Transaction(
                tx.txId(),
                tx.type(),
                tx.payload(),
                tx.createdBy(),
                status,
                tx.company()
        );
    }
}
