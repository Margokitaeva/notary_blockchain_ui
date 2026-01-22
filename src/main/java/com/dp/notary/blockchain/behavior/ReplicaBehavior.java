package com.dp.notary.blockchain.behavior;


import com.dp.notary.blockchain.api.client.ReplicaClient;
import com.dp.notary.blockchain.blockchain.BlockchainService;
import com.dp.notary.blockchain.blockchain.model.TransactionEntity;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@ConditionalOnProperty(name = "notary.role", havingValue = "REPLICA")
public class ReplicaBehavior implements RoleBehavior {
    private final ReplicaClient replicaClient;
    private final BlockchainService blockchainService;
    ReplicaBehavior(ReplicaClient replicaClient, BlockchainService blockchainService) {
        this.replicaClient = replicaClient;
        this.blockchainService = blockchainService;
    }

    @PostConstruct
    public void init() {
        replicaClient.fetchBlocks(blockchainService.getHeight());
    }
    @Override
    public void deleteTransaction(String txId) {
        replicaClient.deleteDraft(txId);

    }

    @Override
    public void approveTransaction(String txId) {}

    @Override
    public void declineTransaction(String txId) {}

    @Override
    public void resubmit(String txId) {
        replicaClient.submit(txId);
    }

    @Override
    public void addDraft(TransactionEntity tx, String mode) {
        if (Objects.equals(mode, "EDIT")) {
            replicaClient.editDraft(tx);
        }else{
            replicaClient.addDraft(tx);
        }
    }

    @Override
    public boolean onSubmitDraft(TransactionEntity tx, String mode, boolean isLeader) {
        if (Objects.equals(mode, "EDIT")) {
            replicaClient.editDraft(tx);
        }else{
            replicaClient.addDraft(tx);
        }
        replicaClient.submit(tx.getTxId());
        return false;
    }
}
