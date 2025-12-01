package com.ssnc.kafkaavrodemo.producer;

import com.ssnc.avroModels.TaxLotDetail;
import com.ssnc.avroModels.TaxLotDetailKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TaxLotProducer {

    @Autowired
    private StreamBridge streamBridge;

    private static final String BINDING_NAME = "taxLotProducer-out-0";

    public void sendTaxLot(TaxLotDetail taxLotDetail, String eventId, String investmentId) {
        log.info("Sending tax lot via Spring Cloud Stream with eventId: {}, investmentId: {}",
                eventId, investmentId);

        try {
            // Create Avro key from TaxLotDetailKey schema
            TaxLotDetailKey key = TaxLotDetailKey.newBuilder()
                    .setEventId(eventId)
                    .setInvestmentId(investmentId)
                    .build();

            // Build message with Avro key
            Message<TaxLotDetail> message = MessageBuilder
                    .withPayload(taxLotDetail)
                    .setHeader(KafkaHeaders.KEY, key)
                    .build();

            // Send message via StreamBridge
            boolean sent = streamBridge.send(BINDING_NAME, message);

            if (sent) {
                log.info("Successfully sent tax lot [EventId: {}, InvestmentId: {}] via Spring Cloud Stream",
                        eventId, investmentId);
            } else {
                log.error("Failed to send tax lot [EventId: {}, InvestmentId: {}]", eventId, investmentId);
            }
        } catch (Exception ex) {
            log.error("Unable to send tax lot [EventId: {}, InvestmentId: {}] due to : {}",
                    eventId, investmentId, ex.getMessage(), ex);
        }
    }
}

