package com.dp.notary.blockchain.core;

import com.dp.notary.blockchain.api.dto.NodeStatusResponse;
import com.dp.notary.blockchain.api.dto.SubmitActRequest;
import com.dp.notary.blockchain.api.dto.SubmitActResponse;
import com.dp.notary.blockchain.config.NotaryProperties;
import com.dp.notary.blockchain.domain.Block;
import com.dp.notary.blockchain.domain.Transaction;
import com.dp.notary.blockchain.persistence.BlockRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class BlockchainService {

    private final BlockRepository blocks;
    private final PendingPool pool;
    private final NotaryProperties props;
    private final LeaderClient leaderClient;

    public BlockchainService(BlockRepository blocks, PendingPool pool, NotaryProperties props, LeaderClient leaderClient) {
        this.blocks = blocks;
        this.pool = pool;
        this.props = props;
        this.leaderClient = leaderClient;
    }

    public NodeStatusResponse status() {
        var head = blocks.findHead().orElseThrow();
        return new NodeStatusResponse(
                props.role(),
                head.height(),
                head.hash(),
                pool.size()
        );
    }

    public List<Block> getBlocks(long fromHeight) {
        long safeFrom = Math.max(0, fromHeight);
        return blocks.findFromHeight(safeFrom, 500);
    }

    public SubmitActResponse submitAct(SubmitActRequest req) {
        if (isReplica()) {
            return leaderClient.forwardAct(req);
        }

        // LEADER: create tx and keep in pending pool
        String txId = UUID.randomUUID().toString();
        Transaction tx = new Transaction(txId, req.type(), req.payload());
        pool.add(tx);

        return new SubmitActResponse(txId, "ACCEPTED");
    }

    private boolean isReplica() {
        return "REPLICA".equalsIgnoreCase(props.role());
    }
}

