package com.dp.notary.blockchain.api;

import com.dp.notary.blockchain.api.dto.PendingActionResponse;
import com.dp.notary.blockchain.api.dto.SubmitActRequest;
import com.dp.notary.blockchain.api.dto.SubmitActResponse;
import com.dp.notary.blockchain.blockchain.model.BlockEntity;
import com.dp.notary.blockchain.blockchain.BlockchainService;
import com.dp.notary.blockchain.blockchain.model.TransactionEntity;
import com.dp.notary.blockchain.blockchain.model.TransactionStatus;
import com.dp.notary.blockchain.blockchain.model.TransactionType;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api")
public class BlockchainController {

    private final BlockchainService blockchain;

    public BlockchainController(BlockchainService svc) {
        this.blockchain = svc;
    }

    @GetMapping("/blocks")
        public List<BlockEntity> blocks(@RequestParam(name = "fromHeight", defaultValue = "0") long fromHeight) {
        return blockchain.getBlocks(fromHeight,500);
    }

    @PostMapping("/acts")
    public SubmitActResponse submit(@RequestBody SubmitActRequest req) {
        //TODO: и тут нахуй тоже
        return new SubmitActResponse("хуй","хуй");
    }
}
