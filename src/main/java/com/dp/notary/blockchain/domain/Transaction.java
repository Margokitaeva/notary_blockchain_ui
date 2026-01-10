package com.dp.notary.blockchain.domain;

public record Transaction(
        String txId,
        String type,
        String payload
) {}
