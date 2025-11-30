package com.ssnc.kafkaavrodemo.consumer;

import com.ssnc.avroModels.OrderDetails;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OrderConsumer {

    @KafkaListener(
            topics = "${spring.kafka.topic.orders}",
            groupId = "${spring.kafka.order.consumer.group-id}",
            containerFactory = "orderKafkaListenerContainerFactory"
    )
    public void consumeOrder(ConsumerRecord<String, OrderDetails> record) {
        try {
            OrderDetails orderDetails = record.value();
            log.info("Received order from Kafka - Topic: {}, Partition: {}, Offset: {}",
                    record.topic(), record.partition(), record.offset());
            log.info("Order Details - ID: {}, Customer: {}, Amount: {}, Status: {}, Created At: {}",
                    orderDetails.getOrderId(),
                    orderDetails.getCustomerId(),
                    orderDetails.getAmount(),
                    orderDetails.getStatus(),
                    orderDetails.getCreatedAt());

            // Process the order here
            processOrder(orderDetails);
        } catch (Exception e) {
            log.error("Error processing order: ", e);
            throw e;
        }
    }

    private void processOrder(OrderDetails orderDetails) {
        // Add your business logic here
        log.info("Processing order with ID: {}", orderDetails.getOrderId());
        // Example: Save to database, trigger workflows, etc.
    }
}

