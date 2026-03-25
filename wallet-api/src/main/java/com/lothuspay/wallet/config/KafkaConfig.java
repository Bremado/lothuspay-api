package com.lothuspay.wallet.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic walletBalanceUpdated() {
        return TopicBuilder.name("wallet.balance.updated")
                .partitions(3)
                .replicas(1)
                .build();
    }


}
