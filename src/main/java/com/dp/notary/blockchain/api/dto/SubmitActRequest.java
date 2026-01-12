package com.dp.notary.blockchain.api.dto;

public record SubmitActRequest(
        String type,
        String payload,
        String createdBy,
        String companyId,
        String companyName
) {}
