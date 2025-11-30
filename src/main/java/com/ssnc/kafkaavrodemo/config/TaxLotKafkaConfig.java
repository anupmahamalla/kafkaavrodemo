package com.ssnc.kafkaavrodemo.config;

import com.ssnc.avroModels.TaxLotDetail;
import com.ssnc.avroModels.TaxLotDetailKey;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka configuration for TaxLot producer and consumer.
 * Uses Avro for both keys (TaxLotDetailKey) and values (TaxLotDetail).
 */
@Configuration
public class TaxLotKafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.schema-registry-url}")
    private String schemaRegistryUrl;

    @Value("${spring.kafka.taxlot.producer.key-serializer}")
    private String keySerializer;

    @Value("${spring.kafka.taxlot.producer.value-serializer}")
    private String valueSerializer;

    @Value("${spring.kafka.taxlot.consumer.group-id}")
    private String consumerGroupId;

    @Value("${spring.kafka.taxlot.consumer.key-deserializer}")
    private String keyDeserializer;

    @Value("${spring.kafka.taxlot.consumer.value-deserializer}")
    private String valueDeserializer;

    @Value("${spring.kafka.taxlot.consumer.auto-offset-reset}")
    private String autoOffsetReset;

    @Value("${spring.kafka.taxlot.consumer.specific-avro-reader}")
    private boolean specificAvroReader;

    @Bean
    public ProducerFactory<TaxLotDetailKey, TaxLotDetail> taxLotProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        configProps.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<TaxLotDetailKey, TaxLotDetail> taxLotKafkaTemplate() {
        return new KafkaTemplate<>(taxLotProducerFactory());
    }

    @Bean
    public ConsumerFactory<TaxLotDetailKey, TaxLotDetail> taxLotConsumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
        configProps.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
        configProps.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, specificAvroReader);
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);

        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<TaxLotDetailKey, TaxLotDetail> taxLotKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<TaxLotDetailKey, TaxLotDetail> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(taxLotConsumerFactory());
        return factory;
    }
}

