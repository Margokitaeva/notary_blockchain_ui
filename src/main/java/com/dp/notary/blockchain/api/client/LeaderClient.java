package com.dp.notary.blockchain.api.client;

import com.dp.notary.blockchain.blockchain.model.BlockEntity;
import com.dp.notary.blockchain.blockchain.model.TransactionEntity;
import com.dp.notary.blockchain.config.NotaryProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class LeaderClient {

    private final RestClient client;
    private final NotaryProperties props;

    public LeaderClient(RestClient client, NotaryProperties props) {
        this.client = client;
        this.props = props;
    }

    public void broadcastAddDraft(TransactionEntity tx) {
        broadcast("/tx/both/addDraft", tx);
    }

    public void broadcastEditDraft(TransactionEntity tx) {
        broadcastPut("/tx/both/editDraft", tx);
    }

    public void broadcastDeleteDraft(String txId) {
        broadcastDelete("/tx/both/deleteDraft/" + txId);
    }

    public void broadcastSubmit(String txId) {
        broadcast("/tx/both/submit/" + txId, null);
    }
    public void broadcastApprove(String txId) {
        broadcast("/tx/both/approve/" + txId, null);
    }

    public void sendBlock(BlockEntity block) {
        broadcast("/blocks/replica/one", block);
    }

    private void broadcast(String path, Object body) {
        for (String replica : props.replicas()) {
            client.post()
                    .uri(replica + path)
                    .body(body);
        }
    }

    private void broadcastDelete(String path) {
        for (String replica : props.replicas()) {
            client.delete()
                    .uri(replica + path);
        }
    }

    private void broadcastPut(String path, Object body) {
        for (String replica : props.replicas()) {
            client.put()
                    .uri(replica + path)
                    .body(body);
        }
    }
}
