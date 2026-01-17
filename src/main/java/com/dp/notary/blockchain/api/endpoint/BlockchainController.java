package com.dp.notary.blockchain.api.endpoint;

import com.dp.notary.blockchain.api.client.ReplicaClient;
import com.dp.notary.blockchain.blockchain.model.BlockEntity;
import com.dp.notary.blockchain.blockchain.BlockchainService;
import com.dp.notary.blockchain.blockchain.model.TransactionEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/blocks")
public class BlockchainController {

    private final BlockchainService blockchain;
    private final ReplicaClient replicaClient;
    public BlockchainController(BlockchainService blockchain, ReplicaClient replicaClient) {
        this.blockchain = blockchain;
        this.replicaClient = replicaClient;
    }

    @GetMapping("/leader/all/{fromHeight}")
        public List<BlockEntity> blocks(@PathVariable int fromHeight) {
        return blockchain.getBlocks(fromHeight,500);
    }

    @PostMapping("/replica/one")
    public ResponseEntity<Void> submitBlock(@RequestBody BlockEntity req) {
        try {
            for (String txId : req.getTransactions()){
                TransactionEntity tx = blockchain.getTransactionById(txId);
                if(tx == null) {
                    tx = replicaClient.getTransaction(txId);
                    blockchain.addTransaction(tx);
                }
            }
            blockchain.addBlock(req);
        }catch (IllegalStateException e) {
            System.out.println(e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
        }
        return ResponseEntity.ok().build();
    }
}
