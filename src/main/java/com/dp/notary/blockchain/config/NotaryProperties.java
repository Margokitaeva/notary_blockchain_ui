package com.dp.notary.blockchain.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "notary")
public record NotaryProperties(
        String role,
        String leaderUrl,
        List<String> replicas
) {}