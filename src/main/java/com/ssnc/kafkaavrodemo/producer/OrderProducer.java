package com.ssnc.kafkaavrodemo.producer;

import com.ssnc.avroModels.OrderDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OrderProducer {

    @Autowired
    private StreamBridge streamBridge;

    private static final String BINDING_NAME = "orderProducer-out-0";

    public void sendOrder(OrderDetails orderDetails) {
        log.info("Sending order via Spring Cloud Stream with orderId: {}", orderDetails.getOrderId());

        try {
            // Build message with String key
            Message<OrderDetails> message = MessageBuilder
                    .withPayload(orderDetails)
                    .setHeader(KafkaHeaders.KEY, orderDetails.getOrderId().toString())
                    .build();

            // Send message via StreamBridge
            boolean sent = streamBridge.send(BINDING_NAME, message);

            if (sent) {
                log.info("Successfully sent order [{}] via Spring Cloud Stream", orderDetails.getOrderId());
            } else {
                log.error("Failed to send order [{}]", orderDetails.getOrderId());
            }
        } catch (Exception ex) {
            log.error("Unable to send order [{}] due to : {}", orderDetails.getOrderId(), ex.getMessage(), ex);
        }
    }
}

