package com.dp.notary.blockchain.api.endpoint;

import com.dp.notary.blockchain.blockchain.BlockchainService;
import com.dp.notary.blockchain.blockchain.model.TransactionEntity;
import com.dp.notary.blockchain.config.NotaryProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequestMapping("/tx")
public class TransactionController {

    private final BlockchainService blockchainService;
    private final NotaryProperties props;

    public TransactionController(BlockchainService blockchainService, NotaryProperties props) {
        this.blockchainService = blockchainService;
        this.props = props;
    }
    // сервис, который знает: лидер мы или реплика

    /**
     * Добавить черновик
     */
    @PostMapping("/both/addDraft")
    public ResponseEntity<String> addDraft(@RequestBody TransactionEntity tx) {
        String txId = blockchainService.addDraft(tx);

        if (Objects.equals(props.role(), "LEADER")) {
            // TODO:отправка на реплики
        }
        return ResponseEntity.ok(txId);
    }

    /**
     * Обновить черновик
     */
    @PutMapping("/both/editDraft")
    public ResponseEntity<Void> editDraft(@RequestBody TransactionEntity tx) {
        blockchainService.editDraft(tx);

        if (Objects.equals(props.role(), "LEADER")) {
            // TODO:отправка на реплики
        }

        return ResponseEntity.ok().build();
    }

    /**
     * Удалить черновик
     */
    @DeleteMapping("/both/deleteDraft/{txId}")
    public ResponseEntity<Void> deleteDraft(@PathVariable String txId) {
        blockchainService.deleteTransaction(txId);

        if (Objects.equals(props.role(), "LEADER")) {
            // TODO:отправка на реплики
        }

        return ResponseEntity.ok().build();
    }


    @PostMapping("/both/changeStatus/{txId}")
    public ResponseEntity<Void> submit(@PathVariable String txId) {
        blockchainService.submitTransaction(txId);

        if (Objects.equals(props.role(), "LEADER")) {
            // TODO:отправка на реплики
        }

        return ResponseEntity.ok().build();
    }

    @PostMapping("/replica/approve/{txId}")
    public ResponseEntity<Void> approve(@PathVariable String txId) {
        if (Objects.equals(props.role(), "REPLICA")) {
            blockchainService.approve(txId);
            return ResponseEntity.ok().build();
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/replica/decline/{txId}")
    public ResponseEntity<Void> decline(@PathVariable String txId) {
        if (Objects.equals(props.role(), "REPLICA")) {
            blockchainService.decline(txId);
            return ResponseEntity.ok().build();
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

}

