package com.dp.notary.blockchain.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "notary")
public record NotaryProperties(
        String role,
        String leaderUrl
) {}