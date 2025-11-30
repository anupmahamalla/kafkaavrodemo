package com.ssnc.kafkaavrodemo.consumer;

import com.ssnc.avroModels.TaxLotDetail;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@Slf4j
public class TaxLotConsumer {

    @KafkaListener(
            topics = "${spring.kafka.topic.taxlots}",
            groupId = "${spring.kafka.consumer.taxlot-group-id}",
            containerFactory = "taxLotKafkaListenerContainerFactory"
    )
    public void consumeTaxLot(ConsumerRecord<String, TaxLotDetail> record) {
        try {
            TaxLotDetail taxLotDetail = record.value();
            String key = record.key();

            log.info("Received tax lot from Kafka - Topic: {}, Partition: {}, Offset: {}, Key: {}",
                    record.topic(), record.partition(), record.offset(), key);

            log.info("Tax Lot Details - Client: {} (ID: {}), Fund: {} (ID: {}), TaxLotId: {}, Server: {}",
                    taxLotDetail.getClientShortName(),
                    taxLotDetail.getClientId(),
                    taxLotDetail.getFundShortName(),
                    taxLotDetail.getFundId(),
                    taxLotDetail.getTaxLotId(),
                    taxLotDetail.getGenevaServer());

            log.info("Trade Info - TradeDate: {}, EffectiveDate: {}, Quantity: {}, TradePrice: {}, TradeNotional: {}",
                    Instant.ofEpochMilli(taxLotDetail.getTradeDate().toEpochMilli()),
                    Instant.ofEpochMilli(taxLotDetail.getEffectiveDate().toEpochMilli()),
                    taxLotDetail.getQuantity(),
                    taxLotDetail.getTradePrice(),
                    taxLotDetail.getTradeNotional());

            log.info("Swap Info - Currency: {}, Spread: {}, MarketPrice: {}, UnderlyingInvestmentId: {}, UnderlyingCurrency: {}",
                    taxLotDetail.getSwapCurrency(),
                    taxLotDetail.getSpread(),
                    taxLotDetail.getMarketPrice(),
                    taxLotDetail.getUnderlyingInvestmentId(),
                    taxLotDetail.getUnderlyingCurrency());

            // Process the tax lot here
            processTaxLot(taxLotDetail);
        } catch (Exception e) {
            log.error("Error processing tax lot: ", e);
            throw e;
        }
    }

    private void processTaxLot(TaxLotDetail taxLotDetail) {
        // Add your business logic here
        log.info("Processing tax lot with ID: {} for client: {}",
                taxLotDetail.getTaxLotId(),
                taxLotDetail.getClientShortName());

        // Example business logic:
        // - Validate tax lot data
        // - Save to database
        // - Calculate accruals
        // - Trigger downstream workflows
        // - Update positions
        // - Send notifications
    }
}

