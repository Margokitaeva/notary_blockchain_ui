package com.dp.notary.blockchain.core;

import com.dp.notary.blockchain.blockchain.BlockchainModule;
import com.dp.notary.blockchain.blockchain.model.Block;
import com.dp.notary.blockchain.blockchain.model.BlockchainStatus;
import com.dp.notary.blockchain.config.NotaryProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReplicaSyncScheduler {

    private static final Logger log = LoggerFactory.getLogger(ReplicaSyncScheduler.class);

    private final BlockchainModule blockchain;
    private final LeaderClient leaderClient;
    private final NotaryProperties props;

    public ReplicaSyncScheduler(BlockchainModule blockchain, LeaderClient leaderClient, NotaryProperties props) {
        this.blockchain = blockchain;
        this.leaderClient = leaderClient;
        this.props = props;
    }

    @Scheduled(fixedDelayString = "${notary.replica-sync.delay-ms:1000}")
    public void syncFromLeader() {
        if (!isReplica()) {
            return;
        }
        try {
            long fromHeight = nextHeight();
            List<Block> blocks = leaderClient.getBlocks(fromHeight);
            if (blocks.isEmpty()) {
                return;
            }
            blocks.forEach(blockchain::appendValidated);
            log.info("Replica appended {} block(s) starting from height {}", blocks.size(), fromHeight);
        } catch (Exception e) {
            log.warn("Replica sync failed: {}", e.getMessage());
        }
    }

    private long nextHeight() {
        BlockchainStatus status = blockchain.status();
        return status.headHeight() + 1;
    }

    private boolean isReplica() {
        return "REPLICA".equalsIgnoreCase(props.role());
    }
}
