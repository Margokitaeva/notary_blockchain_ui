package com.dp.notary.blockchain.blockchain.logic;

import com.dp.notary.blockchain.blockchain.model.Block;
import com.dp.notary.blockchain.blockchain.model.Company;
import com.dp.notary.blockchain.blockchain.model.Transaction;
import com.dp.notary.blockchain.blockchain.model.TransactionStatus;
import com.dp.notary.blockchain.blockchain.model.TransactionType;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BlockProcessorTest {

    private final Clock fixedClock = Clock.fixed(Instant.parse("2024-01-01T00:00:00Z"), ZoneOffset.UTC);

    @Test
    void createNextBlockUsesHeadForHeightAndPrevHash() {
        BlockProcessor processor = new BlockProcessor(fixedClock);
        Block head = new Block(1, "hash1", "hash0", Instant.now(), List.of());

        Block block = processor.createNextBlock(List.of(sampleTx()), head);

        assertEquals(2, block.height());
        assertEquals(head.hash(), block.prevHash());
        assertNotNull(block.hash());
    }

    @Test
    void validateNewBlockFailsOnWrongPrevHash() {
        BlockProcessor processor = new BlockProcessor(fixedClock);
        Block head = processor.createNextBlock(List.of(sampleTx()), null);
        Block bad = new Block(1, head.hash(), "BAD_PREV", head.timestamp(), head.transactions());

        assertThrows(IllegalStateException.class, () -> processor.validateNewBlock(bad, head));
    }

    @Test
    void calculateHashChangesWithPayload() {
        BlockProcessor processor = new BlockProcessor(fixedClock);
        String hash1 = processor.calculateHash(0, null, Instant.now(), List.of(sampleTx("p1")));
        String hash2 = processor.calculateHash(0, null, Instant.now(), List.of(sampleTx("p2")));

        assertNotEquals(hash1, hash2);
    }

    private Transaction sampleTx() {
        return sampleTx("{}");
    }

    private Transaction sampleTx(String payload) {
        return new Transaction(
                "tx1",
                TransactionType.PURCHASE,
                payload,
                "alice",
                TransactionStatus.SUBMITTED,
                new Company("c1", "Company One")
        );
    }
}
