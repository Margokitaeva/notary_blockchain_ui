package com.dp.notary.blockchain.blockchain.entity;

import com.dp.notary.blockchain.blockchain.model.TransactionStatus;

public class TransactionEntity {
    private String txId;
    private String type;
    private String payload;
    private String createdBy;
    private TransactionStatus status;
    private CompanyEntity company;

    public TransactionEntity() {}

    public TransactionEntity(String txId, String type, String payload, String createdBy, TransactionStatus status, CompanyEntity company) {
        this.txId = txId;
        this.type = type;
        this.payload = payload;
        this.createdBy = createdBy;
        this.status = status;
        this.company = company;
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
}
