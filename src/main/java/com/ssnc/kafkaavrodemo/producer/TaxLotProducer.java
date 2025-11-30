package com.ssnc.kafkaavrodemo.producer;

import com.ssnc.avroModels.TaxLotDetail;
import com.ssnc.avroModels.TaxLotDetailKey;
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
public class TaxLotProducer {

    @Autowired
    @Qualifier("taxLotKafkaTemplate")
    private KafkaTemplate<String, TaxLotDetail> kafkaTemplate;

    @Value("${spring.kafka.topic.taxlots}")
    private String taxLotTopic;

    public void sendTaxLot(TaxLotDetail taxLotDetail, String eventId, String investmentId) {
        log.info("Sending tax lot to Kafka topic: {} with eventId: {}, investmentId: {}",
                taxLotTopic, eventId, investmentId);


        // Create composite key from eventId and investmentId
        String key = eventId + ":" + investmentId;

        CompletableFuture<SendResult<String, TaxLotDetail>> future =
            kafkaTemplate.send(taxLotTopic, key, taxLotDetail);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Successfully sent tax lot [EventId: {}, InvestmentId: {}] with offset [{}]",
                        eventId,
                        investmentId,
                        result.getRecordMetadata().offset());
            } else {
                log.error("Unable to send tax lot [EventId: {}, InvestmentId: {}] due to : {}",
                        eventId,
                        investmentId,
                        ex.getMessage());
            }
        });
    }
}

