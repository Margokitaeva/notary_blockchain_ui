package com.dp.notary.blockchain.behavior;


import com.dp.notary.blockchain.api.client.ReplicaClient;
import com.dp.notary.blockchain.blockchain.BlockchainService;
import com.dp.notary.blockchain.blockchain.model.TransactionEntity;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@ConditionalOnProperty(name = "notary.role", havingValue = "LEADER")
public class ReplicaBehavior implements RoleBehavior {
    private ReplicaClient replicaClient;
    ReplicaBehavior(ReplicaClient replicaClient) {
        this.replicaClient = replicaClient;
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
