package com.dp.notary.blockchain.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableConfigurationProperties(NotaryProperties.class)
@EnableScheduling
public class AppConfig {}
