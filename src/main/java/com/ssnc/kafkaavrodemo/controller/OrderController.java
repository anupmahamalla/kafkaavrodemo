package com.ssnc.kafkaavrodemo.controller;

import com.ssnc.avroModels.OrderDetails;
import com.ssnc.kafkaavrodemo.producer.OrderProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@Slf4j
public class OrderController {

    @Autowired
    private OrderProducer orderProducer;

    @PostMapping
    public ResponseEntity<String> createOrder(@RequestBody OrderRequest orderRequest) {
        try {
            String orderId = UUID.randomUUID().toString();
            String createdAt = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);

            OrderDetails orderDetails = OrderDetails.newBuilder()
                    .setOrderId(orderId)
                    .setCustomerId(orderRequest.getCustomerId())
                    .setAmount(orderRequest.getAmount())
                    .setStatus(orderRequest.getStatus() != null ? orderRequest.getStatus() : "CREATED")
                    .setCreatedAt(createdAt)
                    .build();

            orderProducer.sendOrder(orderDetails);

            log.info("Order created and sent to Kafka: {}", orderId);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("Order created successfully with ID: " + orderId);
        } catch (Exception e) {
            log.error("Error creating order: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating order: " + e.getMessage());
        }
    }

    @GetMapping("/test")
    public ResponseEntity<String> testEndpoint() {
        return ResponseEntity.ok("Order service is running!");
    }
}

class OrderRequest {
    private String customerId;
    private Double amount;
    private String status;

    public OrderRequest() {}

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

