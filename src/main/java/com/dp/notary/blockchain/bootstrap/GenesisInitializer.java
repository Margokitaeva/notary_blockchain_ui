package com.dp.notary.blockchain.bootstrap;

import com.dp.notary.blockchain.domain.Block;
import com.dp.notary.blockchain.domain.Transaction;
import com.dp.notary.blockchain.persistence.BlockRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class GenesisInitializer {

    public GenesisInitializer(BlockRepository repo) {
        if (repo.findHead().isEmpty()) {
            Block genesis = new Block(
                    0,
                    "GENESIS",
                    null,
                    Instant.now(),
                    List.of(new Transaction("GENESIS_TX", "GENESIS", "{}"))
            );
            repo.append(genesis);
        }
    }
}

