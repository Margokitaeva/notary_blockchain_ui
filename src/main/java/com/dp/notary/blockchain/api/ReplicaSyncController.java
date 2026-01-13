package com.dp.notary.blockchain.api;

import com.dp.notary.blockchain.api.dto.PendingActionResponse;
import com.dp.notary.blockchain.core.ReplicaSyncService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/replica")
public class ReplicaSyncController {

    private final ReplicaSyncService sync;

    public ReplicaSyncController(ReplicaSyncService sync) {
        this.sync = sync;
    }

    @PostMapping("/sync")
    public PendingActionResponse triggerSync() {
        var result = sync.syncFromLeader();
        if (!result.ok()) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Sync failed: " + result.reason());
        }
        String status = result.reason() == null ? "SYNCED" : "SKIPPED";
        return new PendingActionResponse("blocks", status + ":" + result.blocks());
    }
}
