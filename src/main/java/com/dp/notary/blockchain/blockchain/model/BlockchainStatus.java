package com.dp.notary.blockchain.blockchain.model;

public record BlockchainStatus(
        long headHeight,
        String headHash,
        int pendingTransactions
) {}
