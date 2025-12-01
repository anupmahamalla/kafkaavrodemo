package com.ssnc.kafkaavrodemo.consumer;

import com.ssnc.avroModels.OrderDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

@Service
@Slf4j
public class OrderConsumer {

    @Bean
    public Consumer<Message<OrderDetails>> orderConsumer() {
        return message -> {
            try {
                OrderDetails orderDetails = message.getPayload();

                // Extract metadata from message headers
                Object partition = message.getHeaders().get(KafkaHeaders.RECEIVED_PARTITION);
                Object offset = message.getHeaders().get(KafkaHeaders.OFFSET);
                Object topic = message.getHeaders().get(KafkaHeaders.RECEIVED_TOPIC);

                log.info("Received order from Spring Cloud Stream - Topic: {}, Partition: {}, Offset: {}",
                        topic, partition, offset);
                log.info("Order Details - ID: {}, Customer: {}, Amount: {}, Status: {}, Created At: {}",
                        orderDetails.getOrderId(),
                        orderDetails.getCustomerId(),
                        orderDetails.getAmount(),
                        orderDetails.getStatus(),
                        orderDetails.getCreatedAt());

                // Process the order
                processOrder(orderDetails);
            } catch (Exception e) {
                log.error("Error processing order: ", e);
                throw new RuntimeException("Failed to process order", e);
            }
        };
    }

    private void processOrder(OrderDetails orderDetails) {
        // Add your business logic here
        log.info("Processing order with ID: {}", orderDetails.getOrderId());
        // Example: Save to database, trigger workflows, etc.
    }
}

