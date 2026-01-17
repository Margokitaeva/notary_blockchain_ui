package com.dp.notary.blockchain.api.client;

import com.dp.notary.blockchain.blockchain.BlockchainService;
import com.dp.notary.blockchain.blockchain.model.BlockEntity;
import com.dp.notary.blockchain.blockchain.model.TransactionEntity;
import com.dp.notary.blockchain.config.NotaryProperties;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class ReplicaClient {

    private final RestClient client;
    private final NotaryProperties props;
    private final BlockchainService blockchain;

    public ReplicaClient(RestClient client, NotaryProperties props, BlockchainService blockchain) {
        this.client = client;
        this.props = props;
        this.blockchain = blockchain;
    }

    public TransactionEntity getTransaction(String txId) {
        return client.get()
                .uri(props.leaderUrl() + "/tx/leader/getTx/"+txId)
                .retrieve()
                .body(TransactionEntity.class);
    }

    public void addDraft(TransactionEntity tx) {
        post("/tx/both/addDraft", tx);
    }

    public void editDraft(TransactionEntity tx) {
        put("/tx/both/editDraft", tx);
    }

    public void deleteDraft(String txId) {
        delete("/tx/both/deleteDraft/" + txId);
    }

    public void submit(String txId) {
        post("/tx/both/submit/" + txId, "");
    }

    public void fetchBlocks(long fromHeight) {
        System.out.println(props.leaderUrl() + "/blocks/leader/all/" + fromHeight);
        List<BlockEntity> blocks = client.get()
                .uri(props.leaderUrl() + "/blocks/leader/all/" + fromHeight)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });

        assert blocks != null;
        for (BlockEntity block : blocks) {
            for (String txId : block.getTransactions()){
                TransactionEntity tx = blockchain.getTransactionById(txId);
                if(tx == null) {
                    tx = getTransaction(txId);
                    blockchain.addTransaction(tx);
                }

            }
            blockchain.addBlock(block);
        }
    }

    private void post(String path, Object body) {
        client.post()
                .uri(props.leaderUrl() + path)
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }

    private void put(String path, Object body) {
        client.put()
                .uri(props.leaderUrl() + path)
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }

    private void delete(String path) {
        client.delete()
                .uri(props.leaderUrl() + path)
                .retrieve()
                .toBodilessEntity();
    }
}
