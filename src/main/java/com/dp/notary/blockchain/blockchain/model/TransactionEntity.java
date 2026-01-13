package com.dp.notary.blockchain.blockchain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionEntity {
    private int txId;
    private TransactionType type;
    private String payload;
    private String createdBy;
    private TransactionStatus status;
}
