package com.dp.notary.blockchain.blockchain;

import com.dp.notary.blockchain.api.dto.NodeStatusResponse;
import com.dp.notary.blockchain.api.dto.SubmitActRequest;
import com.dp.notary.blockchain.api.dto.SubmitActResponse;
import com.dp.notary.blockchain.blockchain.model.*;
import com.dp.notary.blockchain.blockchain.persistence.BlockRepository;
import com.dp.notary.blockchain.blockchain.persistence.TransactionStateRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class BlockchainService {

    private final TransactionStateRepository txRepo;
    private final BlockRepository blockRepo;
    private final BlockProcessor blockProcessor;

    public BlockchainService(
            TransactionStateRepository txRepo,
            BlockRepository blockRepo,
            BlockProcessor blockProcessor
    ) {
        this.txRepo = txRepo;
        this.blockRepo = blockRepo;
        this.blockProcessor = blockProcessor;
        createGenesisBlock();
    }

    public String addDraft(TransactionEntity tx) {
        tx.setTxId(UUID.randomUUID().toString());
        tx.setStatus(TransactionStatus.DRAFT);
        return txRepo.insert(tx);
    }

    public void editDraft(TransactionEntity tx) {
        TransactionEntity existing = txRepo.find(tx.getTxId())
                .orElseThrow(() -> new IllegalStateException("Transaction not found"));

        if (existing.getStatus() != TransactionStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT can be edited");
        }

        txRepo.update(tx);
    }

    public void deleteTransaction(String txId) {
        TransactionEntity tx = txRepo.find(txId)
                .orElseThrow(() -> new IllegalStateException("Transaction not found"));

        if (tx.getStatus() != TransactionStatus.DRAFT && tx.getStatus() != TransactionStatus.DECLINED) {
            throw new IllegalStateException("Only DRAFT or DECLINED can be deleted");
        }

        txRepo.delete(txId);
    }

    public void submitTransaction(String txId) {
        updateStatusStrict(txId, TransactionStatus.DRAFT, TransactionStatus.SUBMITTED);
    }

    public void approve(String txId) {
        updateStatusStrict(txId, TransactionStatus.SUBMITTED, TransactionStatus.APPROVED);
    }

    public void decline(String txId) {
        updateStatusStrict(txId, TransactionStatus.SUBMITTED, TransactionStatus.DECLINED);
    }

    public void seal(String txId) {
        updateStatusStrict(txId, TransactionStatus.APPROVED, TransactionStatus.SEALED);
    }

    private void updateStatusStrict(
            String txId,
            TransactionStatus from,
            TransactionStatus to
    ) {
        TransactionEntity tx = txRepo.find(txId)
                .orElseThrow(() -> new IllegalStateException("Transaction not found"));

        if (tx.getStatus() != from) {
            throw new IllegalStateException(
                    "Invalid status transition: " + tx.getStatus() + " → " + to
            );
        }

        txRepo.updateStatus(txId, to);
    }

    public int totalApproved(String user) {
        return txRepo.findByStatus(TransactionStatus.APPROVED).stream()
                .filter(tx -> tx.getCreatedBy().equals(user))
                .toList()
                .size()
                + txRepo.findByStatus(TransactionStatus.SEALED).stream()
                .filter(tx -> tx.getCreatedBy().equals(user))
                .toList().size();
    }

    public int totalDraft(String user) {
        return txRepo.findByStatus(TransactionStatus.DRAFT).stream()
                .filter(tx -> tx.getCreatedBy().equals(user))
                .toList()
                .size();
    }

    public int totalDeclined(String user) {
        return txRepo.findByStatus(TransactionStatus.DECLINED).stream()
                .filter(tx -> tx.getCreatedBy().equals(user))
                .toList()
                .size();
    }

    public int totalSubmitted(String user) {
        return txRepo.findByStatus(TransactionStatus.SUBMITTED).stream()
                .filter(tx -> tx.getCreatedBy().equals(user))
                .toList()
                .size();
    }

    public int totalApproved() {
        return txRepo.countByStatus(TransactionStatus.APPROVED)
                + txRepo.countByStatus(TransactionStatus.SEALED);
    }

    public int totalSubmitted() {
        return txRepo.countByStatus(TransactionStatus.SUBMITTED);
    }

    public List<TransactionEntity> getStatusTransactions(
            int from,
            int limit,
            TransactionStatus status,
            String user
    ) {
        return txRepo.findByStatus(status).stream()
                .filter(tx -> user == null || tx.getCreatedBy().equals(user))
                .skip((long) from * limit)
                .limit(limit)
                .toList();
    }

    public void createGenesisBlock() {
        if (blockRepo.getHeight() > 0) {
            return;
        }

        BlockEntity genesis = blockProcessor.createNextBlock(
                List.of(),
                null
        );

        blockRepo.append(genesis);
    }

    public void createNextBlock() {
        List<TransactionEntity> approvedTxs = txRepo.findByStatus(TransactionStatus.APPROVED);

        if (approvedTxs.size() < 5) return;

        List<TransactionEntity> txsForBlock = approvedTxs.stream()
                .limit(5)
                .toList();

        BlockEntity head = blockRepo.findHead()
                .orElseThrow(() -> new IllegalStateException("No genesis block"));

        List<String> txIds = txsForBlock.stream()
                .map(TransactionEntity::getTxId)
                .toList();

        BlockEntity next = blockProcessor.createNextBlock(txIds, head);

        blockRepo.append(next);

        txsForBlock.forEach(tx -> seal(tx.getTxId()));

    }


    public boolean validateBlocks() {
        List<BlockEntity> blocks = blockRepo.findFromHeight(0, Integer.MAX_VALUE);

        for (int i = 1; i < blocks.size(); i++) {
            if (!blockProcessor.validateBlock(blocks.get(i - 1), blocks.get(i))) {
                return false;
            }
        }
        return true;
    }


    public void addBlock(BlockEntity block) {

        BlockEntity head = blockRepo.findHead()
                .orElseThrow(() -> new IllegalStateException("No genesis block"));

        if (!blockProcessor.validateBlock(head, block)) {
            throw new IllegalStateException("Invalid block: failed validation");
        }

        blockRepo.append(block);

        block.getTransactions().forEach(txId ->
                txRepo.updateStatus(txId, TransactionStatus.SEALED)
        );
    }

    public List<BlockEntity> getBlocks(long from,int limit){
        return blockRepo.findFromHeight(from,limit);
    }

    public long getHeight(){
        return blockRepo.getHeight();
    }



}