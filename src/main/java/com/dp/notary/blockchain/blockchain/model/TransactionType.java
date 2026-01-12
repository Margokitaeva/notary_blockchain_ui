package com.dp.notary.blockchain.blockchain.model;

public enum TransactionType {
    PURCHASE,
    GRANT,
    DIVIDENT,
    SELL;

    public static TransactionType fromString(String value) {
        for (TransactionType type : values()) {
            if (type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unsupported transaction type: " + value);
    }
}
