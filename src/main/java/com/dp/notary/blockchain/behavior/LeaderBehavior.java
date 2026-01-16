package com.dp.notary.blockchain.behavior;
import com.dp.notary.blockchain.api.client.LeaderClient;
import com.dp.notary.blockchain.blockchain.BlockchainService;
import com.dp.notary.blockchain.blockchain.model.TransactionEntity;
import com.dp.notary.blockchain.ui.TransactionFormController;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@ConditionalOnProperty(name = "notary.role", havingValue = "REPLICA")
public class LeaderBehavior implements RoleBehavior {


    private LeaderClient leaderClient;
    private BlockchainService blockchainService;

    LeaderBehavior(LeaderClient leaderClient, BlockchainService blockchainService) {
        this.leaderClient = leaderClient;
        this.blockchainService = blockchainService;
    }

    @Override
    public void deleteTransaction(String txId) {
        System.out.println("Leader");
        blockchainService.deleteTransaction(txId);
        leaderClient.broadcastDeleteDraft(txId);
    }

    @Override
    public void approveTransaction(String txId) {
        blockchainService.approve(txId);
        leaderClient.broadcastApprove(txId);
    }

    @Override
    public void declineTransaction(String txId) {
        blockchainService.decline(txId);
        leaderClient.broadcastApprove(txId);
    }

    @Override
    public void resubmit(String txId) {
        blockchainService.submitTransaction(txId);
        leaderClient.broadcastDeleteDraft(txId);
    }

    @Override
    public void addDraft(TransactionEntity tx, String mode) {
        if (Objects.equals(mode, "EDIT")) {
            blockchainService.editDraft(tx);
            leaderClient.broadcastEditDraft(tx);
        }else{
            blockchainService.addDraft(tx);
            leaderClient.broadcastAddDraft(tx);
        }
    }

    @Override
    public boolean onSubmitDraft(TransactionEntity tx, String mode) {
        if (Objects.equals(mode, "EDIT")) {
            blockchainService.editDraft(tx);
            leaderClient.broadcastEditDraft(tx);
        }else{
            blockchainService.addDraft(tx);
            leaderClient.broadcastAddDraft(tx);
        }
        blockchainService.submitTransaction(tx.getTxId());
        blockchainService.approve(tx.getTxId());
        leaderClient.broadcastSubmit(tx.getTxId());
        leaderClient.broadcastApprove(tx.getTxId());
        return true;
    }
}
