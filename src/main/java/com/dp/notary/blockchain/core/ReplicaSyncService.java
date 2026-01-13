package com.dp.notary.blockchain.core;

import com.dp.notary.blockchain.api.dto.SubmitActResponse;
import com.dp.notary.blockchain.blockchain.BlockchainModule;
import com.dp.notary.blockchain.blockchain.model.Block;
import com.dp.notary.blockchain.blockchain.model.BlockchainStatus;
import com.dp.notary.blockchain.blockchain.model.Company;
import com.dp.notary.blockchain.blockchain.model.Owner;
import com.dp.notary.blockchain.blockchain.model.Transaction;
import com.dp.notary.blockchain.blockchain.model.TransactionStatus;
import com.dp.notary.blockchain.blockchain.model.TransactionType;
import com.dp.notary.blockchain.blockchain.logic.ReplicaTransactionStore;
import com.dp.notary.blockchain.config.NotaryProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ReplicaSyncService {

    private static final Logger log = LoggerFactory.getLogger(ReplicaSyncService.class);

    private final BlockchainModule blockchain;
    private final LeaderClient leaderClient;
    private final NotaryProperties props;
    private final ReplicaTransactionStore replicaStore;
    private final ReplicaDraftBuffer draftBuffer;

    public ReplicaSyncService(BlockchainModule blockchain, LeaderClient leaderClient, NotaryProperties props, ReplicaTransactionStore replicaStore, ReplicaDraftBuffer draftBuffer) {
        this.blockchain = blockchain;
        this.leaderClient = leaderClient;
        this.props = props;
        this.replicaStore = replicaStore;
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

    private void syncDraftsFromLeader() {
        try {
            List<Transaction> drafts = leaderClient.getLeaderDrafts();
            drafts.forEach(replicaStore::saveDraft);
        } catch (Exception e) {
            log.warn("Replica failed to sync drafts: {}", e.getMessage());
        }
    }

    private void flushBufferedDrafts() {
        for (ReplicaDraftBuffer.BufferedDraft draft : draftBuffer.all()) {
            try {
                SubmitActResponse resp = leaderClient.forwardAct(draft.request());
                TransactionType type = TransactionType.fromString(draft.request().type());
                Transaction tx = new Transaction(
                        resp.txId(),
                        type,
                        draft.request().payload(),
                        draft.request().createdBy(),
                        TransactionStatus.SUBMITTED,
                        new Company(draft.request().companyId(), draft.request().companyName()),
                        new Owner(draft.request().ownerId(), draft.request().ownerName(), draft.request().ownerSurname()),
                        draft.request().amount() == null ? BigDecimal.ZERO : draft.request().amount(),
                        java.time.Instant.now(),
                        draft.request().target()
                );
                replicaStore.saveSubmitted(tx);
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
