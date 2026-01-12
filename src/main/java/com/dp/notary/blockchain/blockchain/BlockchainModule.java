package com.dp.notary.blockchain.blockchain;

import com.dp.notary.blockchain.blockchain.logic.BlockProcessor;
import com.dp.notary.blockchain.blockchain.logic.PendingPool;
import com.dp.notary.blockchain.blockchain.model.Block;
import com.dp.notary.blockchain.blockchain.model.BlockchainStatus;
import com.dp.notary.blockchain.blockchain.model.Transaction;
import com.dp.notary.blockchain.blockchain.model.TransactionStatus;
import com.dp.notary.blockchain.blockchain.persistence.BlockRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class BlockchainModule {

    private final BlockRepository blocks;
    private final PendingPool pool;
    private final BlockProcessor processor;

    public BlockchainModule(BlockRepository blocks, PendingPool pool, BlockProcessor processor) {
        this.blocks = blocks;
        this.pool = pool;
        this.processor = processor;
        ensureGenesis();
    }

    public BlockchainStatus status() {
        Block head = blocks.findHead().orElse(null);
        long height = head == null ? -1 : head.height();
        String hash = head == null ? null : head.hash();
        return new BlockchainStatus(height, hash, pool.size());
    }

    public List<Block> getBlocks(long fromHeight) {
        long safeFrom = Math.max(0, fromHeight);
        return blocks.findFromHeight(safeFrom, 500);
    }

    public int pendingSize() {
        return pool.size();
    }

    public String addTransaction(Transaction tx) {
        String txId = tx.txId() == null ? UUID.randomUUID().toString() : tx.txId();
        Transaction normalized = new Transaction(
                txId,
                tx.type(),
                tx.payload(),
                tx.createdBy(),
                tx.status(),
                tx.company()
        );
        pool.add(normalized);
        return txId;
    }

    public Optional<Block> createBlockFromPending() {
        List<Transaction> txs = pool.drainAll();
        if (txs.isEmpty()) {
            return Optional.empty();
        }
        Block head = blocks.findHead().orElse(null);
        List<Transaction> approved = txs.stream()
                .map(this::approve)
                .toList();
        Block newBlock = processor.createNextBlock(approved, head);
        processor.validateNewBlock(newBlock, head);
        blocks.append(newBlock);
        return Optional.of(newBlock);
    }

    public void appendValidated(Block block) {
        if (blocks.existsHeight(block.height())) {
            return;
        }
        Block head = blocks.findHead().orElse(null);
        processor.validateNewBlock(block, head);
        blocks.append(block);
    }

    public void ensureGenesis() {
        if (blocks.findHead().isEmpty()) {
            blocks.append(processor.createGenesisBlock());
        }
    }

    public boolean approvePending(String txId) {
        return pool.updateStatus(txId, TransactionStatus.APPROVED);
    }

    public Optional<Transaction> declinePending(String txId) {
        return pool.removeById(txId)
                .map(tx -> new Transaction(
                        tx.txId(),
                        tx.type(),
                        tx.payload(),
                        tx.createdBy(),
                        TransactionStatus.DECLINED,
                        tx.company()
                ));
    }

    private Transaction approve(Transaction tx) {
        return new Transaction(
                tx.txId(),
                tx.type(),
                tx.payload(),
                tx.createdBy(),
                TransactionStatus.APPROVED,
                tx.company()
        );
    }
    public long getChainSize(){
        return blocks.getHeight();
    }
}
