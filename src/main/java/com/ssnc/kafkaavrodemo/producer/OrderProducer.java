package com.ssnc.kafkaavrodemo.producer;

import com.ssnc.avroModels.OrderDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class OrderProducer {

    @Autowired
    @Qualifier("orderKafkaTemplate")
    private KafkaTemplate<String, OrderDetails> kafkaTemplate;

    @Value("${spring.kafka.topic.orders}")
    private String orderTopic;

    public void sendOrder(OrderDetails orderDetails) {
        log.info("Sending order to Kafka topic: {} with orderId: {}", orderTopic, orderDetails.getOrderId());

        CompletableFuture<SendResult<String, OrderDetails>> future =
            kafkaTemplate.send(orderTopic, orderDetails.getOrderId().toString(), orderDetails);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Successfully sent order [{}] with offset [{}]",
                        orderDetails.getOrderId(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("Unable to send order [{}] due to : {}",
                        orderDetails.getOrderId(),
                        ex.getMessage());
            }
        });
    }
}

