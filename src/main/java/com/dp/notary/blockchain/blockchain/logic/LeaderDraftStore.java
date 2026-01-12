package com.dp.notary.blockchain.blockchain.logic;

import com.dp.notary.blockchain.blockchain.model.Transaction;
import com.dp.notary.blockchain.blockchain.model.TransactionScope;
import com.dp.notary.blockchain.blockchain.model.TransactionStatus;
import com.dp.notary.blockchain.blockchain.persistence.TransactionStateRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LeaderDraftStore {

    private final TransactionStateRepository repo;

    public LeaderDraftStore(TransactionStateRepository repo) {
        this.repo = repo;
    }

    public void saveDraft(Transaction tx) {
        repo.upsert(TransactionScope.DRAFT, withStatus(tx, TransactionStatus.DRAFT));
    }

    public List<Transaction> allDrafts() {
        return repo.findByScopeAndStatuses(TransactionScope.DRAFT, List.of(TransactionStatus.DRAFT));
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
