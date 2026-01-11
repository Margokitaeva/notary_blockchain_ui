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
public class LeaderDraftStore {

    private final Map<String, Transaction> drafts = new ConcurrentHashMap<>();

    public void saveDraft(Transaction tx) {
        drafts.put(tx.txId(), copyDraft(tx));
    }

    public List<Transaction> allDrafts() {
        return List.copyOf(drafts.values());
    }

    private Transaction copyDraft(Transaction tx) {
        Company company = tx.company() == null ? null : new Company(tx.company().id(), tx.company().name());
        TransactionType type = tx.type();
        return new Transaction(
                tx.txId(),
                type,
                tx.payload(),
                tx.createdBy(),
                TransactionStatus.DRAFT,
                company
        );
    }
}
