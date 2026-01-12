package com.dp.notary.blockchain.blockchain.logic;

import com.dp.notary.blockchain.blockchain.model.Company;
import com.dp.notary.blockchain.blockchain.model.Transaction;
import com.dp.notary.blockchain.blockchain.model.Owner;
import com.dp.notary.blockchain.blockchain.model.TransactionStatus;
import com.dp.notary.blockchain.blockchain.model.TransactionType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PendingPoolTest {

    @Test
    void addAndDrainAll() {
        PendingPool pool = new PendingPool();
        pool.add(sampleTx("tx1"));
        pool.add(sampleTx("tx2"));

        assertEquals(2, pool.size());
        var drained = pool.drainAll();

        assertEquals(2, drained.size());
        assertTrue(pool.snapshot().isEmpty());
        assertEquals(0, pool.size());
    }

    private Transaction sampleTx(String id) {
        return new Transaction(
                id,
                TransactionType.PURCHASE,
                "{}",
                "alice",
                TransactionStatus.SUBMITTED,
                new Company("c1", "Company One"),
                new Owner("o1", "Alice", "A"),
                BigDecimal.ONE,
                Instant.parse("2024-01-01T00:00:00Z"),
                "target"
        );
    }
}
