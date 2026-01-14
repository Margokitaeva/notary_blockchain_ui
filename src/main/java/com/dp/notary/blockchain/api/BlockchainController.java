package com.dp.notary.blockchain.api;

import com.dp.notary.blockchain.blockchain.model.BlockEntity;
import com.dp.notary.blockchain.blockchain.BlockchainService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/blocks")
public class BlockchainController {

    private final BlockchainService blockchain;

    public BlockchainController(BlockchainService blockchain) {
        this.blockchain = blockchain;
    }

    @GetMapping("leader/all")
        public List<BlockEntity> blocks(@RequestParam(name = "fromHeight", defaultValue = "0") long fromHeight) {
        return blockchain.getBlocks(fromHeight,500);
    }

    @PostMapping("replica/one")
    public ResponseEntity<Void> submitBlock(@RequestBody BlockEntity req) {
        try {
            blockchain.addBlock(req);
        }catch (IllegalStateException e) {
            return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
            //TODO: стоило бы организовать поиск восстановление цепочки
        }
        return ResponseEntity.ok().build();
    }
}
