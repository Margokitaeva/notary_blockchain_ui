package com.dp.notary.blockchain.blockchain.persistence;

import com.dp.notary.blockchain.blockchain.model.TransactionEntity;
import com.dp.notary.blockchain.blockchain.model.TransactionStatus;
import com.dp.notary.blockchain.blockchain.model.TransactionType;

import java.util.List;
import java.util.Optional;

public interface TransactionStateRepository {
    void update(TransactionEntity tx);
    void delete(String txId);
    String insert(TransactionEntity tx);
    boolean updateStatus(String txId, TransactionStatus status);
    List<TransactionEntity> findByStatus(TransactionStatus status);
    List<TransactionEntity> findByStatus(TransactionStatus status, String createdByFilter, String initiatorFilter, String targetFilter, TransactionType typeFilter);
    List<TransactionEntity> findByStatuses(List<TransactionStatus> statuses, String createdByFilter, String initiatorFilter, String targetFilter, TransactionType typeFilter, int offset, int limit);
    int countByStatus(TransactionStatus status);
    int countByStatus(TransactionStatus status, String createdByFilter, String initiatorFilter, String targetFilter, TransactionType typeFilter);
    Optional<TransactionEntity> find(String txId);

}
