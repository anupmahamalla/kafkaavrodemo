package com.ssnc.kafkaavrodemo.consumer;

import com.ssnc.avroModels.TaxLotDetail;
import com.ssnc.avroModels.TaxLotDetailKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.function.Consumer;

@Service
@Slf4j
public class TaxLotConsumer {

    @Bean
    public Consumer<Message<TaxLotDetail>> taxLotConsumer() {
        return message -> {
            try {
                TaxLotDetail taxLotDetail = message.getPayload();

                // Extract metadata from message headers
                Object partition = message.getHeaders().get(KafkaHeaders.RECEIVED_PARTITION);
                Object offset = message.getHeaders().get(KafkaHeaders.OFFSET);
                Object topic = message.getHeaders().get(KafkaHeaders.RECEIVED_TOPIC);
                Object messageKey = message.getHeaders().get(KafkaHeaders.RECEIVED_KEY);

                TaxLotDetailKey key = null;
                if (messageKey instanceof TaxLotDetailKey) {
                    key = (TaxLotDetailKey) messageKey;
                }

                log.info("Received tax lot from Spring Cloud Stream - Topic: {}, PARTITION: {}, Offset: {}, Key: [EventId: {}, InvestmentId: {}]",
                        topic,
                        partition,
                        offset,
                        key != null ? key.getEventId() : "N/A",
                        key != null ? key.getInvestmentId() : "N/A");

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

                // Process the tax lot
                processTaxLot(taxLotDetail);
            } catch (Exception e) {
                log.error("Error processing tax lot: ", e);
                throw new RuntimeException("Failed to process tax lot", e);
            }
        };
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

