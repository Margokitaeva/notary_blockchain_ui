package com.dp.notary.blockchain.blockchain.entity;

import java.time.Instant;
import java.util.List;

public class BlockEntity {
    private long height;
    private String hash;
    private String prevHash;
    private Instant timestamp;
    private List<TransactionEntity> transactions;

    public BlockEntity() {}

    public BlockEntity(long height, String hash, String prevHash, Instant timestamp, List<TransactionEntity> transactions) {
        this.height = height;
        this.hash = hash;
        this.prevHash = prevHash;
        this.timestamp = timestamp;
        this.transactions = transactions;
    }

    public long getHeight() {
        return height;
    }

    public void setHeight(long height) {
        this.height = height;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getPrevHash() {
        return prevHash;
    }

    public void setPrevHash(String prevHash) {
        this.prevHash = prevHash;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public List<TransactionEntity> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<TransactionEntity> transactions) {
        this.transactions = transactions;
    }
}
