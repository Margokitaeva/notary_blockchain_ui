package com.dp.notary.blockchain.blockchain;

import com.dp.notary.blockchain.blockchain.logic.BlockProcessor;
import com.dp.notary.blockchain.blockchain.logic.PendingPool;
import com.dp.notary.blockchain.blockchain.model.Block;
import com.dp.notary.blockchain.blockchain.model.Company;
import com.dp.notary.blockchain.blockchain.model.Transaction;
import com.dp.notary.blockchain.blockchain.model.TransactionStatus;
import com.dp.notary.blockchain.blockchain.model.TransactionType;
import com.dp.notary.blockchain.blockchain.persistence.BlockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class BlockchainModuleTest {

    private InMemoryRepo repo;
    private PendingPool pool;
    private BlockProcessor processor;
    private BlockchainModule module;

    @BeforeEach
    void setUp() {
        repo = new InMemoryRepo();
        pool = new PendingPool();
        processor = new BlockProcessor();
        module = new BlockchainModule(repo, pool, processor);
    }

    @Test
    void ensureGenesisCreatesBlock() {
        module.ensureGenesis();
        assertTrue(repo.findHead().isPresent());
        assertEquals(0, repo.findHead().orElseThrow().height());
    }

    @Test
    void createBlockFromPendingApprovesTransactionsAndPersists() {
        Transaction tx = new Transaction(
                "tx-1",
                TransactionType.PURCHASE,
                "{}",
                "alice",
                TransactionStatus.SUBMITTED,
                new Company("c1", "Company One")
        );
        module.addTransaction(tx);

        Optional<Block> maybeBlock = module.createBlockFromPending();

        assertTrue(maybeBlock.isPresent());
        Block block = maybeBlock.get();
        assertEquals(1, block.transactions().size());
        assertEquals(TransactionStatus.APPROVED, block.transactions().get(0).status());
        assertEquals(block, repo.findHead().orElseThrow());
    }

    @Test
    void appendValidatedIgnoresExistingHeight() {
        module.ensureGenesis();
        Block head = repo.findHead().orElseThrow();
        module.appendValidated(head);

        assertEquals(1, repo.blocks.size());
    }

    private static class InMemoryRepo implements BlockRepository {
        private final List<Block> blocks = new ArrayList<>();

        @Override
        public Optional<Block> findHead() {
            if (blocks.isEmpty()) return Optional.empty();
            return Optional.of(blocks.get(blocks.size() - 1));
        }

        @Override
        public List<Block> findFromHeight(long fromHeight, int limit) {
            return blocks.stream().filter(b -> b.height() >= fromHeight).limit(limit).toList();
        }

        @Override
        public void append(Block block) {
            blocks.add(block);
        }

        @Override
        public boolean existsHeight(long height) {
            return blocks.stream().anyMatch(b -> b.height() == height);
        }
    }
}
