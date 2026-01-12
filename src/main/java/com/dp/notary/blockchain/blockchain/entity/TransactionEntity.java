package com.dp.notary.blockchain.blockchain.entity;

import com.dp.notary.blockchain.blockchain.model.TransactionStatus;

public class TransactionEntity {
    private String txId;
    private String type;
    private String payload;
    private String createdBy;
    private TransactionStatus status;
    private CompanyEntity company;
    private OwnerEntity owner;
    private java.math.BigDecimal amount;
    private java.time.Instant timestamp;
    private String target;

    public TransactionEntity() {}

    public TransactionEntity(String txId, String type, String payload, String createdBy, TransactionStatus status, CompanyEntity company, OwnerEntity owner, java.math.BigDecimal amount, java.time.Instant timestamp, String target) {
        this.txId = txId;
        this.type = type;
        this.payload = payload;
        this.createdBy = createdBy;
        this.status = status;
        this.company = company;
        this.owner = owner;
        this.amount = amount;
        this.timestamp = timestamp;
        this.target = target;
    }

    public String getTxId() {
        return txId;
    }

    public void setTxId(String txId) {
        this.txId = txId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public CompanyEntity getCompany() {
        return company;
    }

    public void setCompany(CompanyEntity company) {
        this.company = company;
    }

    public OwnerEntity getOwner() {
        return owner;
    }

    public void setOwner(OwnerEntity owner) {
        this.owner = owner;
    }

    public java.math.BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(java.math.BigDecimal amount) {
        this.amount = amount;
    }

    public java.time.Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(java.time.Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }
}
