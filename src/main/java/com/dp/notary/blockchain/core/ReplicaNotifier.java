package com.dp.notary.blockchain.core;

import com.dp.notary.blockchain.config.NotaryProperties;
import com.dp.notary.blockchain.config.ReplicaProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.List;

@Component
public class ReplicaNotifier {

    private static final Logger log = LoggerFactory.getLogger(ReplicaNotifier.class);

    private final NotaryProperties notaryProps;
    private final ReplicaProperties replicaProps;
    private final RestClient rest;

    public ReplicaNotifier(NotaryProperties notaryProps, ReplicaProperties replicaProps, RestClient rest) {
        this.notaryProps = notaryProps;
        this.replicaProps = replicaProps;
        this.rest = rest;
    }

    public void notifyReplicas() {
        if (isReplica()) {
            return;
        }
        List<String> urls = replicaProps.getUrls();
        if (urls.isEmpty()) {
            return;
        }
        for (String url : urls) {
            sendWithRetry(url, replicaProps.getNotifyRetries());
        }
    }

    private void sendWithRetry(String baseUrl, int retries) {
        for (int attempt = 0; attempt <= retries; attempt++) {
            try {
                RestClient client = rest.mutate()
                        .baseUrl(baseUrl)
                        .build();
                var request = client.post()
                        .uri("/api/replica/sync")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON);
                if (replicaProps.getAuthToken() != null && !replicaProps.getAuthToken().isBlank()) {
                    request.header("X-Auth-Token", replicaProps.getAuthToken());
                }
                request.retrieve().toBodilessEntity();
                return;
            } catch (Exception e) {
                log.warn("Notify replica {} failed (attempt {}/{}): {}", baseUrl, attempt + 1, retries + 1, e.getMessage());
                if (attempt == retries) {
                    log.error("Replica {} not reachable after retries", baseUrl);
                }
            }
        }
    }

    private boolean isReplica() {
        return "REPLICA".equalsIgnoreCase(notaryProps.role());
    }
}
