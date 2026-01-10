package com.dp.notary.blockchain.core;

import com.dp.notary.blockchain.api.dto.SubmitActRequest;
import com.dp.notary.blockchain.api.dto.SubmitActResponse;
import com.dp.notary.blockchain.api.dto.NodeStatusResponse;
import com.dp.notary.blockchain.domain.Block;

import java.util.List;

public interface LeaderClient {
    SubmitActResponse forwardAct(SubmitActRequest req);
    NodeStatusResponse getStatus();
    List<Block> getBlocks(long fromHeight);
}
