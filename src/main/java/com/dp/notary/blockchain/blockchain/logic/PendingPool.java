package com.dp.notary.blockchain.blockchain.logic;

import com.dp.notary.blockchain.blockchain.model.Transaction;
import com.dp.notary.blockchain.blockchain.model.TransactionScope;
import com.dp.notary.blockchain.blockchain.model.TransactionStatus;
import com.dp.notary.blockchain.blockchain.persistence.TransactionStateRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class PendingPool {
    private final TransactionStateRepository repo;

    public PendingPool(TransactionStateRepository repo) {
        this.repo = repo;
    }

    public void add(Transaction tx) {
        repo.upsert(TransactionScope.PENDING, tx);
    }

    public int size() {
        return repo.countByScopeAndStatuses(TransactionScope.PENDING, List.of(TransactionStatus.SUBMITTED, TransactionStatus.APPROVED));
    }

    public List<Transaction> snapshot() {
        return repo.findByScopeAndStatuses(TransactionScope.PENDING, List.of(TransactionStatus.SUBMITTED, TransactionStatus.APPROVED));
    }

    public List<Transaction> drainAll() {
        List<Transaction> txs = snapshot();
        repo.deleteAll(TransactionScope.PENDING, txs.stream().map(Transaction::txId).toList());
        return txs;
    }

    public boolean updateStatus(String txId, TransactionStatus status) {
        return repo.updateStatus(TransactionScope.PENDING, txId, status);
    }

    public Optional<Transaction> removeById(String txId) {
        Optional<Transaction> found = repo.find(TransactionScope.PENDING, txId);
        found.ifPresent(tx -> repo.delete(TransactionScope.PENDING, txId));
        return found;
    }
}
