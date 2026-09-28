package com.transactionprocessor.gateway;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    NewTopic bankTransactionsTopic(@Value("${app.kafka.topics.bank-transactions.name}") String name,
                                   @Value("${app.kafka.topics.bank-transactions.partitions}") int partitions) {
        return TopicBuilder.name(name)
                .partitions(partitions)
                .replicas(1)
                .build();
    }
}
