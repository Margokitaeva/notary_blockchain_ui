package com.dp.notary.blockchain.persistence;

import com.dp.notary.blockchain.domain.Block;
import java.util.List;
import java.util.Optional;

public interface BlockRepository {
    Optional<Block> findHead();
    List<Block> findFromHeight(long fromHeight, int limit);
    void append(Block block);
    boolean existsHeight(long height);
}
