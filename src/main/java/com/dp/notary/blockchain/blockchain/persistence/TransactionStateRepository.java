package com.dp.notary.blockchain.blockchain.persistence;

import com.dp.notary.blockchain.blockchain.model.TransactionEntity;
import com.dp.notary.blockchain.blockchain.model.TransactionStatus;

import java.util.List;
import java.util.Optional;

public interface TransactionStateRepository {
    void update(TransactionEntity tx);
    void delete(int txId);
    void insert(TransactionEntity tx);
    boolean updateStatus(int txId, TransactionStatus status);
    List<TransactionEntity> findByStatus(TransactionStatus status);
    int countByStatus(TransactionStatus status);
    Optional<TransactionEntity> find(int txId);

}
