package com.dp.notary.blockchain.core;

import com.dp.notary.blockchain.blockchain.BlockchainModule;
import com.dp.notary.blockchain.blockchain.model.Block;
import com.dp.notary.blockchain.blockchain.model.BlockchainStatus;
import com.dp.notary.blockchain.config.NotaryProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReplicaSyncService {

    private static final Logger log = LoggerFactory.getLogger(ReplicaSyncService.class);

    private final BlockchainModule blockchain;
    private final LeaderClient leaderClient;
    private final NotaryProperties props;

    public ReplicaSyncService(BlockchainModule blockchain, LeaderClient leaderClient, NotaryProperties props) {
        this.blockchain = blockchain;
        this.leaderClient = leaderClient;
        this.props = props;
    }

    public SyncResult syncFromLeader() {
        if (!isReplica()) {
            return SyncResult.skipped("node is leader");
        }
        try {
            long fromHeight = nextHeight();
            List<Block> blocks = leaderClient.getBlocks(fromHeight);
            if (blocks.isEmpty()) {
                return SyncResult.success(0);
            }
            blocks.forEach(blockchain::appendValidated);
            log.info("Replica appended {} block(s) starting from height {}", blocks.size(), fromHeight);
            return SyncResult.success(blocks.size());
        } catch (Exception e) {
            log.warn("Replica sync failed: {}", e.getMessage());
            return SyncResult.failed(e.getMessage());
        }
    }

    private long nextHeight() {
        BlockchainStatus status = blockchain.status();
        return status.headHeight() + 1;
    }

    private boolean isReplica() {
        return "REPLICA".equalsIgnoreCase(props.role());
    }

    public record SyncResult(boolean ok, int blocks, String reason) {
        public static SyncResult success(int blocks) { return new SyncResult(true, blocks, null); }
        public static SyncResult failed(String reason) { return new SyncResult(false, 0, reason); }
        public static SyncResult skipped(String reason) { return new SyncResult(true, 0, reason); }
    }
}
