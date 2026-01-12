package com.dp.notary.blockchain.api.dto;

public record PendingActionResponse(
        String txId,
        String status
) {}
