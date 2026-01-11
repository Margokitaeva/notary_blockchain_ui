package com.dp.notary.blockchain.api;

import com.dp.notary.blockchain.api.dto.NodeStatusResponse;
import com.dp.notary.blockchain.api.dto.SubmitActRequest;
import com.dp.notary.blockchain.api.dto.SubmitActResponse;
import com.dp.notary.blockchain.blockchain.model.Block;
import com.dp.notary.blockchain.core.BlockchainService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class BlockchainController {

    private final BlockchainService svc;

    public BlockchainController(BlockchainService svc) {
        this.svc = svc;
    }

    @GetMapping("/status")
    public NodeStatusResponse status() {
        return svc.status();
    }

    @GetMapping("/blocks")
    public List<Block> blocks(@RequestParam(name = "fromHeight", defaultValue = "0") long fromHeight) {
        return svc.getBlocks(fromHeight);
    }

    @PostMapping("/acts")
    public SubmitActResponse submit(@RequestBody SubmitActRequest req) {
        return svc.submitAct(req);
    }
}
