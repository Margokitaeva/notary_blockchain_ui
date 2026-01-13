package com.dp.notary.blockchain.api;

import com.dp.notary.blockchain.blockchain.BlockchainService;
import com.dp.notary.blockchain.blockchain.model.TransactionEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/queues")
public class QueueController {

    private final BlockchainService blockchain;

    public QueueController(BlockchainService blockchain) {
        this.blockchain = blockchain;
    }


}
