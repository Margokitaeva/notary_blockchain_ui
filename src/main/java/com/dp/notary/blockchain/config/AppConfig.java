package com.dp.notary.blockchain.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties({NotaryProperties.class})
public class AppConfig {
    @Bean
    public RestClient restClient(RestClient.Builder builder) {

        return builder
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
