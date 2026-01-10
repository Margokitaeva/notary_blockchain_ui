package com.dp.notary.blockchain.domain;

import java.time.Instant;
import java.util.List;

public record Block(
        long height,
        String hash,
        String prevHash,
        Instant timestamp,
        List<Transaction> transactions
) {}
