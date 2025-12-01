package com.ssnc.kafkaavrodemo.consumer;

import com.ssnc.avroModels.TaxLotDetail;
import com.ssnc.avroModels.TaxLotDetailKey;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.time.Instant;

@Service
@Slf4j
public class TaxLotConsumer {

    @KafkaListener(
            topics = "${spring.kafka.topic.taxlots}",
            groupId = "${spring.kafka.taxlot.consumer.group-id}",
            containerFactory = "taxLotKafkaListenerContainerFactory"
    )
    public void consumeTaxLot(ConsumerRecord<TaxLotDetailKey, TaxLotDetail> record) {
        try {
            TaxLotDetailKey key = record.key();
            TaxLotDetail taxLotDetail = record.value();

            log.info(" Received tax lot from Kafka - Topic: {}, PARTITION: {}, Offset: {}, Key: [EventId: {}, InvestmentId: {}]",
                    record.topic(),
                    record.partition(),  // Shows which partition (0, 1, or 2)
                    record.offset(),
                    key.getEventId(),
                    key.getInvestmentId());

            log.info("Tax Lot Details - Client: {} (ID: {}), Fund: {} (ID: {}), TaxLotId: {}, Server: {}",
                    taxLotDetail.getClientShortName(),
                    taxLotDetail.getClientId(),
                    taxLotDetail.getFundShortName(),
                    taxLotDetail.getFundId(),
                    taxLotDetail.getTaxLotId(),
                    taxLotDetail.getGenevaServer());

            log.info("Trade Info - TradeDate: {}, EffectiveDate: {}, Quantity: {}, TradePrice: {}, TradeNotional: {}",
                    taxLotDetail.getTradeDate(), taxLotDetail.getEffectiveDate(),
                    convertFromAvroDecimal(taxLotDetail.getQuantity()),
                    convertFromAvroDecimal(taxLotDetail.getTradePrice()),
                    convertFromAvroDecimal(taxLotDetail.getTradeNotional()));

            log.info("Swap Info - Currency: {}, Spread: {}, MarketPrice: {}, UnderlyingInvestmentId: {}, UnderlyingCurrency: {}",
                    taxLotDetail.getSwapCurrency(),
                    convertFromAvroDecimal(taxLotDetail.getSpread()),
                    convertFromAvroDecimal(taxLotDetail.getMarketPrice()),
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

    private BigDecimal convertFromAvroDecimal(ByteBuffer buffer) {
        if (buffer == null) {
            return BigDecimal.ZERO;
        }
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        buffer.rewind(); // Reset position for potential reuse
        return new BigDecimal(new BigInteger(bytes), 9); // Scale of 9 from schema
    }
}

