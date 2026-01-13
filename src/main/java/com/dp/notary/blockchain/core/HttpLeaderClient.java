package com.dp.notary.blockchain.core;

import com.dp.notary.blockchain.api.dto.NodeStatusResponse;
import com.dp.notary.blockchain.api.dto.SubmitActRequest;
import com.dp.notary.blockchain.api.dto.SubmitActResponse;
import com.dp.notary.blockchain.blockchain.model.BlockEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class HttpLeaderClient implements LeaderClient {

    private final RestClient rest;

    public HttpLeaderClient(RestClient.Builder builder) {
        this.rest = builder.baseUrl(props.leaderUrl()).build();
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
    public NodeStatusResponse getStatus() {
        return rest.get()
                .uri("/api/status")
                .retrieve()
                .body(NodeStatusResponse.class);
    }

    @Override
    public List<BlockEntity> getBlocks(long fromHeight) {
        BlockEntity[] arr = rest.get()
                .uri(uriBuilder -> uriBuilder.path("/api/blocks").queryParam("fromHeight", fromHeight).build())
                .retrieve()
                .body(BlockEntity[].class);
        return arr == null ? List.of() : List.of(arr);
    }
}
