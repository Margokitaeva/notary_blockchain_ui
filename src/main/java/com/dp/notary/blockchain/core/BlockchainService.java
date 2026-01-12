package com.dp.notary.blockchain.core;

import com.dp.notary.blockchain.api.dto.NodeStatusResponse;
import com.dp.notary.blockchain.api.dto.SubmitActRequest;
import com.dp.notary.blockchain.api.dto.SubmitActResponse;
import com.dp.notary.blockchain.blockchain.BlockchainModule;
import com.dp.notary.blockchain.blockchain.logic.LeaderDraftStore;
import com.dp.notary.blockchain.blockchain.logic.ReplicaTransactionStore;
import com.dp.notary.blockchain.blockchain.model.Block;
import com.dp.notary.blockchain.blockchain.model.BlockchainStatus;
import com.dp.notary.blockchain.blockchain.model.Company;
import com.dp.notary.blockchain.blockchain.model.Owner;
import com.dp.notary.blockchain.blockchain.model.Transaction;
import com.dp.notary.blockchain.blockchain.model.TransactionStatus;
import com.dp.notary.blockchain.blockchain.model.TransactionType;
import com.dp.notary.blockchain.config.NotaryProperties;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.HexFormat;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
public class BlockchainService {

    private final BlockchainModule blockchain;
    private final NotaryProperties props;
    private final LeaderClient leaderClient;
    private final ReplicaTransactionStore replicaStore;
    private final LeaderDraftStore leaderDraftStore;
    private final ReplicaNotifier notifier;
    private final TransactionIdGenerator txIdGenerator;
    private final ReplicaDraftBuffer draftBuffer;

    public BlockchainService(BlockchainModule blockchain, NotaryProperties props, LeaderClient leaderClient, ReplicaTransactionStore replicaStore, LeaderDraftStore leaderDraftStore, ReplicaNotifier notifier, TransactionIdGenerator txIdGenerator, ReplicaDraftBuffer draftBuffer) {
        this.blockchain = blockchain;
        this.props = props;
        this.leaderClient = leaderClient;
        this.replicaStore = replicaStore;
        this.leaderDraftStore = leaderDraftStore;
        this.notifier = notifier;
        this.txIdGenerator = txIdGenerator;
        this.draftBuffer = draftBuffer;
    }

    public NodeStatusResponse status() {
        BlockchainStatus status = blockchain.status();
        return new NodeStatusResponse(
                props.role(),
                status.headHeight(),
                status.headHash(),
                status.pendingTransactions()
        );
    }

    public List<Block> getBlocks(long fromHeight) {
        return blockchain.getBlocks(fromHeight);
    }

    public SubmitActResponse submitAct(SubmitActRequest req) {
        TransactionType type = TransactionType.fromString(req.type());
        Company company = new Company(req.companyId(), req.companyName());
        Owner owner = new Owner(req.ownerId(), req.ownerName(), req.ownerSurname());
        String createdBy = req.createdBy();
        BigDecimal amount = req.amount() == null ? BigDecimal.ZERO : req.amount();
        Instant now = Instant.now();
        String target = req.target();
        String clientKey = clientKey(req);

        if (isReplica()) {
            SubmitActRequest withKey = withClientKey(req, clientKey);
            try {
                SubmitActResponse resp = leaderClient.forwardAct(withKey);
                Transaction submitted = new Transaction(
                        resp.txId(),
                        type,
                        withKey.payload(),
                        createdBy,
                        TransactionStatus.SUBMITTED,
                        company,
                        owner,
                        amount,
                        now,
                        target
                );
                replicaStore.saveSubmitted(submitted);
                return resp;
            } catch (Exception e) {
                draftBuffer.buffer(clientKey, withKey);
                return new SubmitActResponse(null, "QUEUED");
            }
        }

        String txId = txIdGenerator.nextId(clientKey);
        Transaction draft = new Transaction(
                txId,
                type,
                req.payload(),
                createdBy,
                TransactionStatus.DRAFT,
                company,
                owner,
                amount,
                now,
                target
        );
        leaderDraftStore.saveDraft(draft);

        Transaction submitted = new Transaction(
                txId,
                type,
                req.payload(),
                createdBy,
                TransactionStatus.SUBMITTED,
                company,
                owner,
                amount,
                now,
                target
        );
        blockchain.addTransaction(submitted);
        notifier.notifyReplicas();

        return new SubmitActResponse(txId, "ACCEPTED");
    }

    public Optional<Block> createBlockFromPending() {
        return blockchain.createBlockFromPending();
    }

    public void appendBlock(Block block) {
        blockchain.appendValidated(block);
    }

    public boolean approvePending(String txId) {
        if (isReplica()) {
            throw new IllegalStateException("Replica cannot approve transactions");
        }
        boolean ok = blockchain.approvePending(txId);
        if (ok) {
            notifier.notifyReplicas();
        }
        return ok;
    }

    public Optional<Transaction> declinePending(String txId) {
        if (isReplica()) {
            throw new IllegalStateException("Replica cannot decline transactions");
        }
        Optional<Transaction> declined = blockchain.declinePending(txId);
        declined.ifPresent(replicaStore::saveDeclined);
        declined.ifPresent(tx -> notifier.notifyReplicas());
        return declined;
    }

    private boolean isReplica() {
        return "REPLICA".equalsIgnoreCase(props.role());
    }

    private SubmitActRequest withClientKey(SubmitActRequest req, String key) {
        return new SubmitActRequest(
                req.type(),
                req.payload(),
                req.createdBy(),
                req.companyId(),
                req.companyName(),
                req.amount(),
                req.target(),
                req.ownerId(),
                req.ownerName(),
                req.ownerSurname(),
                key
        );
    }

    private String clientKey(SubmitActRequest req) {
        if (req.clientRequestId() != null && !req.clientRequestId().isBlank()) {
            return req.clientRequestId();
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            updateDigest(digest, req.type());
            updateDigest(digest, req.payload());
            updateDigest(digest, req.companyId());
            updateDigest(digest, req.companyName());
            updateDigest(digest, req.target());
            updateDigest(digest, req.ownerId());
            updateDigest(digest, req.ownerName());
            updateDigest(digest, req.ownerSurname());
            updateDigest(digest, req.createdBy());
            updateDigest(digest, req.amount() == null ? null : req.amount().toPlainString());
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private void updateDigest(MessageDigest digest, String value) {
        if (value != null) {
            digest.update(value.getBytes(StandardCharsets.UTF_8));
        }
        digest.update("|".getBytes(StandardCharsets.UTF_8));
    }
}
