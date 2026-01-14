package com.dp.notary.blockchain.blockchain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionEntity {
    private String txId;
    private Instant timestamp;
    private TransactionType type;
    private String createdBy;
    private TransactionStatus status;
    private BigDecimal amount;
    private String target;
    private String initiator;
}
