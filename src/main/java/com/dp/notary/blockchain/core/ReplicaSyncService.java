package com.dp.notary.blockchain.core;

import com.dp.notary.blockchain.api.dto.SubmitActResponse;
import com.dp.notary.blockchain.blockchain.BlockchainService;
import com.dp.notary.blockchain.blockchain.model.BlockEntity;
import com.dp.notary.blockchain.blockchain.model.TransactionEntity;
import com.dp.notary.blockchain.blockchain.model.TransactionStatus;
import com.dp.notary.blockchain.blockchain.model.TransactionType;
import com.dp.notary.blockchain.config.NotaryProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ReplicaSyncService {

    private static final Logger log = LoggerFactory.getLogger(ReplicaSyncService.class);

    private final BlockchainService blockchain;
    private final LeaderClient leaderClient;
    private final NotaryProperties props;
    private final ReplicaDraftBuffer draftBuffer;

    public ReplicaSyncService(BlockchainService blockchain, LeaderClient leaderClient, NotaryProperties props, ReplicaDraftBuffer draftBuffer) {
        this.blockchain = blockchain;
        this.leaderClient = leaderClient;
        this.props = props;
        this.draftBuffer = draftBuffer;
    }

    public SyncResult syncFromLeader() {
        if (!isReplica()) {
            return SyncResult.skipped("node is leader");
        }
        try {
            flushBufferedDrafts();
            syncDraftsFromLeader();
            long fromHeight = nextHeight();
            List<BlockEntity> blocks = leaderClient.getBlocks(fromHeight);
            if (blocks.isEmpty()) {
                return SyncResult.success(0);
            }
            blocks.forEach(blockchain::addBlock);
            log.info("Replica appended {} block(s) starting from height {}", blocks.size(), fromHeight);
            return SyncResult.success(blocks.size());
        } catch (Exception e) {
            log.warn("Replica sync failed: {}", e.getMessage());
            return SyncResult.failed(e.getMessage());
        }
    }

    private long nextHeight() {
        return blockchain.getHeight() + 1;
    }

    private boolean isReplica() {
        return "REPLICA".equalsIgnoreCase(props.role());
    }

    private void syncDraftsFromLeader() {
        try {
            List<TransactionEntity> drafts = leaderClient.getLeaderDrafts();
            drafts.forEach(blockchain::addDraft);
        } catch (Exception e) {
            log.warn("Replica failed to sync drafts: {}", e.getMessage());
        }
    }

    private void flushBufferedDrafts() {
        for (ReplicaDraftBuffer.BufferedDraft draft : draftBuffer.all()) {
            try {
                SubmitActResponse resp = leaderClient.forwardAct(draft.request());
                TransactionType type = TransactionType.fromString(draft.request().type());
                //TODO: я хуй пойми какая тут логика


                draftBuffer.remove(draft.clientKey());
            } catch (Exception e) {
                log.warn("Resend of buffered draft {} failed: {}", draft.clientKey(), e.getMessage());
            }
        }
    }

    public record SyncResult(boolean ok, int blocks, String reason) {
        public static SyncResult success(int blocks) { return new SyncResult(true, blocks, null); }
        public static SyncResult failed(String reason) { return new SyncResult(false, 0, reason); }
        public static SyncResult skipped(String reason) { return new SyncResult(true, 0, reason); }
    }
}
