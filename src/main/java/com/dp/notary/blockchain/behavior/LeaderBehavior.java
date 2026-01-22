package com.dp.notary.blockchain.behavior;
import com.dp.notary.blockchain.api.client.LeaderClient;
import com.dp.notary.blockchain.blockchain.BlockchainService;
import com.dp.notary.blockchain.blockchain.model.BlockEntity;
import com.dp.notary.blockchain.blockchain.model.TransactionEntity;
import com.dp.notary.blockchain.notary.TransactionOrchestrator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@ConditionalOnProperty(name = "notary.role", havingValue = "LEADER")
public class LeaderBehavior implements RoleBehavior {


    private LeaderClient leaderClient;
    private BlockchainService blockchainService;
    private final TransactionOrchestrator orchestrator;

    LeaderBehavior(LeaderClient leaderClient, BlockchainService blockchainService, TransactionOrchestrator orchestrator) {
        this.leaderClient = leaderClient;
        this.blockchainService = blockchainService;
        this.orchestrator = orchestrator;
    }

    @Override
    public void deleteTransaction(String txId) {
        blockchainService.deleteTransaction(txId);
        leaderClient.broadcastDeleteDraft(txId);
    }

    @Override
    public void approveTransaction(String txId) {
        orchestrator.approve(txId);
        leaderClient.broadcastApprove(txId);
        BlockEntity block = blockchainService.createNextBlock();
        if (block != null){
            leaderClient.sendBlock(block);
        }
    }

    @Override
    public void declineTransaction(String txId) {
        orchestrator.decline(txId);
        leaderClient.broadcastDecline(txId);
    }

    @Override
    public void resubmit(String txId) {
        orchestrator.submit(txId);
        leaderClient.broadcastDeleteDraft(txId);
    }

    @Override
    public void addDraft(TransactionEntity tx, String mode) {
        if (Objects.equals(mode, "EDIT")) {
            blockchainService.editDraft(tx);
            leaderClient.broadcastEditDraft(tx);
        }else{
            blockchainService.addTransaction(tx);
            leaderClient.broadcastAddDraft(tx);
        }
    }

    @Override
    public boolean onSubmitDraft(TransactionEntity tx, String mode, boolean isLeader) {
        if (Objects.equals(mode, "EDIT")) {
            blockchainService.editDraft(tx);
            leaderClient.broadcastEditDraft(tx);
        }else{
            blockchainService.addTransaction(tx);
            leaderClient.broadcastAddDraft(tx);
        }
        orchestrator.submit(tx.getTxId());
        leaderClient.broadcastSubmit(tx.getTxId());
        if (isLeader) {
            orchestrator.approve(tx.getTxId());
            leaderClient.broadcastApprove(tx.getTxId());
            BlockEntity block = blockchainService.createNextBlock();
            if (block != null){
                leaderClient.sendBlock(block);
            }
        }
        return true;
    }
}
