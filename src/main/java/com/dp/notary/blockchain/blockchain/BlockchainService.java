package com.dp.notary.blockchain.blockchain;

import com.dp.notary.blockchain.blockchain.model.*;
import com.dp.notary.blockchain.blockchain.persistence.BlockRepository;
import com.dp.notary.blockchain.blockchain.persistence.TransactionStateRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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
    }

    public TransactionEntity getTransactionById(String id) {
        try {
            return txRepo.find(id).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    public String addTransaction(TransactionEntity tx) {
        return txRepo.insert(tx);
    }

    public void editDraft(TransactionEntity tx) {
        TransactionEntity existing = txRepo.find(tx.getTxId())
                .orElseThrow(() -> new IllegalStateException("Transaction not found"));

        if (existing.getStatus() != TransactionStatus.DRAFT && existing.getStatus() != TransactionStatus.DECLINED) {
            throw new IllegalStateException("Only DRAFT or DECLINED can be edited");
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
        try {
            updateStatusStrict(txId, TransactionStatus.DRAFT, TransactionStatus.SUBMITTED);
        } catch (Exception e) {
            updateStatusStrict(txId, TransactionStatus.DECLINED, TransactionStatus.SUBMITTED);
        }
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

    public int totalTransactions(TransactionStatus status,
                                 String createdByFilter,
                                 String initiatorFilter,
                                 String targetFilter,
                                 TransactionType typeFilter){
        int number = txRepo.countByStatus(status, createdByFilter, initiatorFilter, targetFilter, typeFilter);
        if(status.equals(TransactionStatus.APPROVED)){
            number += txRepo.countByStatus(TransactionStatus.SEALED, createdByFilter, initiatorFilter, targetFilter, typeFilter);
        }
        return number;
    }
    public List<TransactionEntity> getStatusTransactions(
            int from,
            int limit,
            TransactionStatus status,
            String createdByFilter,
            String initiatorFilter,
            String targetFilter,
            TransactionType typeFilter
    ) {
        if (status.equals(TransactionStatus.APPROVED)) {
            return txRepo.findByStatuses(List.of(TransactionStatus.APPROVED, TransactionStatus.SEALED),
                    createdByFilter,
                    initiatorFilter,
                    targetFilter,
                    typeFilter,
                    from*limit, limit);
        }
        else
            return txRepo.findByStatus(status,
                    createdByFilter,
                    initiatorFilter,
                    targetFilter,
                    typeFilter,
                    from * limit,
                    limit);
    }

    public BlockEntity createNextBlock() {
        List<TransactionEntity> approvedTxs = txRepo.findByStatus(TransactionStatus.APPROVED);

        if (approvedTxs.size() < 5) return null;

        List<TransactionEntity> txsForBlock = approvedTxs.stream()
                .limit(5)
                .toList();

        BlockEntity head = blockRepo.findHead()
                .orElseThrow(() -> new IllegalStateException("No genesis block"));
        System.out.println(head.getHeight() + " " + head.getPrevHash());
        List<String> txIds = txsForBlock.stream()
                .map(TransactionEntity::getTxId)
                .toList();
        System.out.println(head.getHeight() + " " + head.getPrevHash());
        BlockEntity next = blockProcessor.createNextBlock(txIds, head);

        blockRepo.append(next);

        txsForBlock.forEach(tx -> seal(tx.getTxId()));
        return next;
    }

    public void addBlock(BlockEntity block) {
        System.out.println(block.getHeight());
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

    public List<BlockEntity> getBlocks(long from, int limit) {
        return blockRepo.findFromHeight(from, limit);
    }

    public long getHeight() {
        return blockRepo.getHeight();
    }



}