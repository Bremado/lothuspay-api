package com.lothuspay.payments.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic paymentDepositCreated() {
        return TopicBuilder.name("payment.deposit.created")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic paymentDepositCompleted() {
        return TopicBuilder.name("payment.deposit.completed")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic paymentDepositFailed() {
        return TopicBuilder.name("payment.deposit.failed")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic paymentWithdrawalCreated() {
        return TopicBuilder.name("payment.withdrawal.created")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic paymentWithdrawalCompleted() {
        return TopicBuilder.name("payment.withdrawal.completed")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic paymentWithdrawalFailed() {
        return TopicBuilder.name("payment.withdrawal.failed")
                .partitions(3)
                .replicas(1)
                .build();
    }

}
