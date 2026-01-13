package com.dp.notary.blockchain.blockchain.persistence;

import com.dp.notary.blockchain.blockchain.model.BlockEntity;
import com.dp.notary.blockchain.blockchain.model.TransactionEntity;
import com.dp.notary.blockchain.blockchain.model.TransactionStatus;

import java.util.List;
import java.util.Optional;

public interface BlockRepository {
    Optional<BlockEntity> findHead();
    List<BlockEntity> findFromHeight(long fromHeight, int limit);
    void append(BlockEntity block);
    boolean existsHeight(long height);
    long getHeight();
}
