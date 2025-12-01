package com.ssnc.kafkaavrodemo.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Kafka topic configuration.
 * Auto-creates topics with specified partitions and replication factor.
 */
@Configuration
public class KafkaTopicConfig {

    @Value("${spring.kafka.topic.orders}")
    private String orderTopic;

    @Value("${spring.kafka.topic.taxlots}")
    private String taxLotTopic;

    @Value("${spring.kafka.topic.taxlots-partitions}")
    private int taxLotPartitions;

    @Value("${spring.kafka.topic.taxlots-replication-factor}")
    private int taxLotReplicationFactor;

    /**
     * Create Order topic with default settings (1 partition, 1 replica)
     */
    @Bean
    public NewTopic orderTopic() {
        return TopicBuilder.name(orderTopic)
                .partitions(1)
                .replicas(1)
                .build();
    }

    /**
     * Create TaxLot topic with 3 partitions for parallel processing.
     * Messages with the same TaxLotDetailKey will go to the same partition,
     * ensuring ordering while allowing different keys to be processed in parallel.
     */
    @Bean
    public NewTopic taxLotTopic() {
        return TopicBuilder.name(taxLotTopic)
                .partitions(taxLotPartitions)
                .replicas(taxLotReplicationFactor)
                .config("retention.ms", "3600000") // 1 hour retention
                .config("segment.ms", "600000") // 10 minutes per segment
                .build();
    }
}

