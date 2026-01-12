package com.dp.notary.blockchain.blockchain.model;

public record Transaction(
        String txId,
        TransactionType type,
        String payload,
        String createdBy,
        TransactionStatus status,
        Company company
) {}
