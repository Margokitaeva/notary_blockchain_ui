package com.dp.notary.blockchain.api.dto;

import java.math.BigDecimal;

public record SubmitActRequest(
        String type,
        String payload,
        String createdBy,
        BigDecimal amount,
        String target,
        String initiator,
        String clientRequestId
) {}
