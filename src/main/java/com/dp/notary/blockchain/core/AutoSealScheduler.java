package com.dp.notary.blockchain.core;

import com.dp.notary.blockchain.blockchain.BlockchainModule;
import com.dp.notary.blockchain.config.NotaryProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AutoSealScheduler {

    private static final Logger log = LoggerFactory.getLogger(AutoSealScheduler.class);
    private static final int BATCH_SIZE = 5;

    private final BlockchainModule blockchain;
    private final NotaryProperties props;

    public AutoSealScheduler(BlockchainModule blockchain, NotaryProperties props) {
        this.blockchain = blockchain;
        this.props = props;
    }

    @Scheduled(fixedDelayString = "${notary.auto-seal.delay-ms:1000}")
    public void sealWhenEnoughPending() {
        if (isReplica()) {
            return;
        }
        if (blockchain.pendingSize() >= BATCH_SIZE) {
            blockchain.createBlockFromPending().ifPresent(block ->
                    log.info("Sealed block at height {} with {} txs", block.height(), block.transactions().size())
            );
        }
    }

    private boolean isReplica() {
        return "REPLICA".equalsIgnoreCase(props.role());
    }
}
