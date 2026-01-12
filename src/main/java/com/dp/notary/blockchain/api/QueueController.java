package com.dp.notary.blockchain.api;

import com.dp.notary.blockchain.blockchain.logic.LeaderDraftStore;
import com.dp.notary.blockchain.blockchain.logic.PendingPool;
import com.dp.notary.blockchain.blockchain.logic.ReplicaTransactionStore;
import com.dp.notary.blockchain.blockchain.model.Transaction;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/queues")
public class QueueController {

    private final PendingPool pendingPool;
    private final LeaderDraftStore leaderDrafts;
    private final ReplicaTransactionStore replicaStore;

    public QueueController(PendingPool pendingPool, LeaderDraftStore leaderDrafts, ReplicaTransactionStore replicaStore) {
        this.pendingPool = pendingPool;
        this.leaderDrafts = leaderDrafts;
        this.replicaStore = replicaStore;
    }

    @GetMapping("/pending")
    public List<Transaction> pending() {
        return pendingPool.snapshot();
    }

    @GetMapping("/drafts/leader")
    public List<Transaction> leaderDrafts() {
        return leaderDrafts.allDrafts();
    }

    @GetMapping("/replica/drafts")
    public List<Transaction> replicaDrafts() {
        return replicaStore.drafts();
    }

    @GetMapping("/replica/submitted")
    public List<Transaction> replicaSubmitted() {
        return replicaStore.submitted();
    }

    @GetMapping("/replica/declined")
    public List<Transaction> replicaDeclined() {
        return replicaStore.declined();
    }
}
