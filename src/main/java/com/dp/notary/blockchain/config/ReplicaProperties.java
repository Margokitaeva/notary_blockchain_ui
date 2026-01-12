package com.dp.notary.blockchain.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.List;

@ConfigurationProperties(prefix = "notary.replica")
public class ReplicaProperties {
    private List<String> urls = List.of();
    private Duration notifyTimeout = Duration.ofSeconds(2);
    private int notifyRetries = 2;
    private String authToken;

    public List<String> getUrls() {
        return urls;
    }

    public void setUrls(List<String> urls) {
        this.urls = urls;
    }

    public Duration getNotifyTimeout() {
        return notifyTimeout;
    }

    public void setNotifyTimeout(Duration notifyTimeout) {
        this.notifyTimeout = notifyTimeout;
    }

    public int getNotifyRetries() {
        return notifyRetries;
    }

    public void setNotifyRetries(int notifyRetries) {
        this.notifyRetries = notifyRetries;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }
}
