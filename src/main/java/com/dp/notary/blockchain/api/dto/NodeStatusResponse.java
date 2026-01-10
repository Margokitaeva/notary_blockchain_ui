package com.dp.notary.blockchain.api.dto;

public record NodeStatusResponse(
        String role,
        long chainHeight,
        String headHash,
        int pendingPoolSize
) {}
