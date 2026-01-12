package com.dp.notary.blockchain.blockchain.model;

public record Transaction(
        String txId,
        TransactionType type,
        String payload,
        String createdBy,
        TransactionStatus status,
        Company company,
        Owner owner,
        java.math.BigDecimal amount,
        java.time.Instant timestamp,
        String target
) {}
