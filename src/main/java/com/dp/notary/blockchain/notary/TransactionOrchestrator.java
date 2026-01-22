package com.dp.notary.blockchain.notary;
import com.dp.notary.blockchain.blockchain.BlockchainService;
import com.dp.notary.blockchain.blockchain.model.TransactionEntity;
import com.dp.notary.blockchain.owner.OwnerService;
import org.springframework.stereotype.Service;

@Service
public class TransactionOrchestrator {
    private final BlockchainService blockchainService;
    private final OwnerService ownerService;

    public TransactionOrchestrator(BlockchainService blockchainService, OwnerService ownerService) {
        this.blockchainService = blockchainService;
        this.ownerService = ownerService;
    }

    public void submit(String txId) {
        TransactionEntity tx = blockchainService.getTransactionById(txId);

        ownerService.submitTransaction(tx);
        blockchainService.submitTransaction(txId);
    }

    public void approve(String txId) {
        TransactionEntity tx = blockchainService.getTransactionById(txId);

        ownerService.approveTransaction(tx);
        blockchainService.approve(txId);
    }

    public void decline(String txId) {
        TransactionEntity tx = blockchainService.getTransactionById(txId);

        ownerService.rejectTransaction(tx);
        blockchainService.decline(txId);
    }
}
