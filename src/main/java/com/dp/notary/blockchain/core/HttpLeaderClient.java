package com.dp.notary.blockchain.core;

import com.dp.notary.blockchain.api.dto.NodeStatusResponse;
import com.dp.notary.blockchain.api.dto.SubmitActRequest;
import com.dp.notary.blockchain.api.dto.SubmitActResponse;
import com.dp.notary.blockchain.blockchain.model.BlockEntity;
import com.dp.notary.blockchain.blockchain.model.TransactionEntity;
import com.dp.notary.blockchain.config.NotaryProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class HttpLeaderClient implements LeaderClient {


    private final RestClient rest;
    private final NotaryProperties props;

    public HttpLeaderClient(RestClient.Builder builder, NotaryProperties props) {
        this.props = props;
        this.rest = builder.baseUrl(this.props.leaderUrl()).build();
    }

    @Override
    public SubmitActResponse forwardAct(SubmitActRequest req) {
        return rest.post()
                .uri("/api/acts")
                .body(req)
                .retrieve()
                .body(SubmitActResponse.class);
    }

    @Override
    public List<BlockEntity> getBlocks(long fromHeight) {
        BlockEntity[] arr = rest.get()
                .uri(uriBuilder -> uriBuilder.path("/api/blocks").queryParam("fromHeight", fromHeight).build())
                .retrieve()
                .body(BlockEntity[].class);
        return arr == null ? List.of() : List.of(arr);
    }

    @Override
    public List<TransactionEntity> getLeaderDrafts() {
        TransactionEntity[] arr = rest.get()
                .uri("/api/queues/drafts/leader")
                .retrieve()
                .body(TransactionEntity[].class);
        return arr == null ? List.of() : List.of(arr);
    }
}
