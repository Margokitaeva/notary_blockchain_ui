package com.dp.notary.blockchain.blockchain.persistence;

import com.dp.notary.blockchain.blockchain.model.Transaction;
import com.dp.notary.blockchain.blockchain.model.TransactionScope;
import com.dp.notary.blockchain.blockchain.model.TransactionStatus;

import java.util.List;
import java.util.Optional;

public interface TransactionStateRepository {
    void upsert(TransactionScope scope, Transaction tx);
    boolean updateStatus(TransactionScope scope, String txId, TransactionStatus status);
    boolean delete(TransactionScope scope, String txId);
    List<Transaction> findByScopeAndStatuses(TransactionScope scope, List<TransactionStatus> statuses);
    int countByScopeAndStatuses(TransactionScope scope, List<TransactionStatus> statuses);
    Optional<Transaction> find(TransactionScope scope, String txId);
    void deleteAll(TransactionScope scope, List<String> txIds);
}
