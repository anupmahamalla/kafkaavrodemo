package com.ssnc.kafkaavrodemo.controller;

import com.ssnc.avroModels.TaxLotDetail;
import com.ssnc.kafkaavrodemo.producer.TaxLotProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/api/taxlots")
@Slf4j
public class TaxLotController {

    @Autowired
    private TaxLotProducer taxLotProducer;

    @PostMapping
    public ResponseEntity<String> createTaxLot(@RequestBody TaxLotRequest request) {
        try {
            String eventId = request.getEventId() != null ? request.getEventId() : UUID.randomUUID().toString();

            // Convert BigDecimal to Avro decimal (ByteBuffer)
            long currentTimestamp = Instant.now().toEpochMilli();

            TaxLotDetail taxLotDetail = TaxLotDetail.newBuilder()
                    .setClientShortName(request.getClientShortName())
                    .setClientId(request.getClientId())
                    .setFundShortName(request.getFundShortName())
                    .setFundId(request.getFundId())
                    .setGenevaServer(request.getGenevaServer())
                    .setTaxLotId(request.getTaxLotId())
                    .setAccrualDate(Instant.ofEpochSecond(request.getAccrualDate() != null ? request.getAccrualDate() : currentTimestamp))
                    .setTradeDate(Instant.ofEpochSecond(request.getTradeDate() != null ? request.getTradeDate() : currentTimestamp))
                    .setEffectiveDate(Instant.ofEpochSecond(request.getEffectiveDate() != null ? request.getEffectiveDate() : currentTimestamp))
                    .setQuantity(convertToAvroDecimal(request.getQuantity()))
                    .setTradeNotional(convertToAvroDecimal(request.getTradeNotional()))
                    .setTradePrice(convertToAvroDecimal(request.getTradePrice()))
                    .setResetPrice(convertToAvroDecimal(request.getResetPrice()))
                    .setSwapCurrency(request.getSwapCurrency())
                    .setSpread(convertToAvroDecimal(request.getSpread()))
                    .setMarketPrice(convertToAvroDecimal(request.getMarketPrice()))
                    .setUnderlyingInvestmentId(request.getUnderlyingInvestmentId())
                    .setUnderlyingCurrency(request.getUnderlyingCurrency())
                    .setUserTranId(request.getUserTranId())
                    .setKnowledgeDate(Instant.ofEpochSecond(request.getKnowledgeDate() != null ? request.getKnowledgeDate() : currentTimestamp))
                    .setCreatedAt(Instant.ofEpochSecond(currentTimestamp))
                    .build();

            taxLotProducer.sendTaxLot(taxLotDetail, eventId, request.getInvestmentId());

            log.info("Tax lot created and sent to Kafka - EventId: {}, InvestmentId: {}",
                    eventId, request.getInvestmentId());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("Tax lot created successfully with EventId: " + eventId);
        } catch (Exception e) {
            log.error("Error creating tax lot: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating tax lot: " + e.getMessage());
        }
    }

    @GetMapping("/test")
    public ResponseEntity<String> testEndpoint() {
        return ResponseEntity.ok("Tax lot service is running!");
    }

    @PostMapping("/test-partitions")
    public ResponseEntity<Map<String, Object>> testPartitions() {
        try {
            List<Map<String, String>> results = new ArrayList<>();
            long currentTimestamp = Instant.now().toEpochMilli();

            // Send 5 messages with different keys to demonstrate partition distribution
            String[][] testData = {
                {"EVT-001", "INV-A", "CLIENT-A"},
                {"EVT-002", "INV-B", "CLIENT-B"},
                {"EVT-003", "INV-C", "CLIENT-C"},
                {"EVT-004", "INV-D", "CLIENT-D"},
                {"EVT-005", "INV-E", "CLIENT-E"}
            };

            for (int i = 0; i < testData.length; i++) {
                String eventId = testData[i][0];
                String investmentId = testData[i][1];
                String clientName = testData[i][2];

                TaxLotDetail taxLotDetail = TaxLotDetail.newBuilder()
                        .setClientShortName(clientName)
                        .setClientId((long) (1001 + i))
                        .setFundShortName("FUND-" + (char)('A' + i))
                        .setFundId((long) (5001 + i))
                        .setGenevaServer("GENEVA-PROD-0" + (i + 1))
                        .setTaxLotId((long) (100001 + i))
                        .setAccrualDate(Instant.ofEpochSecond(currentTimestamp))
                        .setTradeDate(Instant.ofEpochSecond(currentTimestamp))
                        .setEffectiveDate(Instant.ofEpochSecond(currentTimestamp))
                        .setQuantity(convertToAvroDecimal(new BigDecimal("1000.500").add(new BigDecimal(i * 100))))
                        .setTradeNotional(convertToAvroDecimal(new BigDecimal("250000.75").add(new BigDecimal(i * 10000))))
                        .setTradePrice(convertToAvroDecimal(new BigDecimal("250.50").add(new BigDecimal(i))))
                        .setResetPrice(convertToAvroDecimal(new BigDecimal("251.00").add(new BigDecimal(i))))
                        .setSwapCurrency("USD")
                        .setSpread(convertToAvroDecimal(new BigDecimal("0.025")))
                        .setMarketPrice(convertToAvroDecimal(new BigDecimal("252.25").add(new BigDecimal(i))))
                        .setUnderlyingInvestmentId("UNDERLYING-INV-00" + (i + 1))
                        .setUnderlyingCurrency("USD")
                        .setUserTranId("TXN-2024-00" + (i + 1))
                        .setKnowledgeDate(Instant.ofEpochSecond(currentTimestamp))
                        .setCreatedAt(Instant.ofEpochSecond(currentTimestamp))
                        .build();

                taxLotProducer.sendTaxLot(taxLotDetail, eventId, investmentId);

                Map<String, String> result = new HashMap<>();
                result.put("eventId", eventId);
                result.put("investmentId", investmentId);
                result.put("client", clientName);
                results.add(result);

                // Small delay to see messages spread across partitions
                Thread.sleep(100);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("messagesSent", testData.length);
            response.put("partitions", 3);
            response.put("message", "Check consumer logs to see partition distribution (PARTITION: 0, 1, or 2)");
            response.put("sentMessages", results);

            log.info("Test partition endpoint: Sent {} messages with different keys", testData.length);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error testing partitions: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    private ByteBuffer convertToAvroDecimal(BigDecimal value) {
        if (value == null) {
            value = BigDecimal.ZERO;
        }
        // Scale to 9 decimal places as per schema
        value = value.setScale(9, RoundingMode.HALF_UP);
        byte[] bytes = value.unscaledValue().toByteArray();
        return ByteBuffer.wrap(bytes);
    }
}

class TaxLotRequest {
    private String eventId;
    private String investmentId;
    private String clientShortName;
    private Long clientId;
    private String fundShortName;
    private Long fundId;
    private String genevaServer;
    private Long taxLotId;
    private Long accrualDate;
    private Long tradeDate;
    private Long effectiveDate;
    private BigDecimal quantity;
    private BigDecimal tradeNotional;
    private BigDecimal tradePrice;
    private BigDecimal resetPrice;
    private String swapCurrency;
    private BigDecimal spread;
    private BigDecimal marketPrice;
    private String underlyingInvestmentId;
    private String underlyingCurrency;
    private String userTranId;
    private Long knowledgeDate;

    // Getters and Setters
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getInvestmentId() { return investmentId; }
    public void setInvestmentId(String investmentId) { this.investmentId = investmentId; }

    public String getClientShortName() { return clientShortName; }
    public void setClientShortName(String clientShortName) { this.clientShortName = clientShortName; }

    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }

    public String getFundShortName() { return fundShortName; }
    public void setFundShortName(String fundShortName) { this.fundShortName = fundShortName; }

    public Long getFundId() { return fundId; }
    public void setFundId(Long fundId) { this.fundId = fundId; }

    public String getGenevaServer() { return genevaServer; }
    public void setGenevaServer(String genevaServer) { this.genevaServer = genevaServer; }

    public Long getTaxLotId() { return taxLotId; }
    public void setTaxLotId(Long taxLotId) { this.taxLotId = taxLotId; }

    public Long getAccrualDate() { return accrualDate; }
    public void setAccrualDate(Long accrualDate) { this.accrualDate = accrualDate; }

    public Long getTradeDate() { return tradeDate; }
    public void setTradeDate(Long tradeDate) { this.tradeDate = tradeDate; }

    public Long getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(Long effectiveDate) { this.effectiveDate = effectiveDate; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public BigDecimal getTradeNotional() { return tradeNotional; }
    public void setTradeNotional(BigDecimal tradeNotional) { this.tradeNotional = tradeNotional; }

    public BigDecimal getTradePrice() { return tradePrice; }
    public void setTradePrice(BigDecimal tradePrice) { this.tradePrice = tradePrice; }

    public BigDecimal getResetPrice() { return resetPrice; }
    public void setResetPrice(BigDecimal resetPrice) { this.resetPrice = resetPrice; }

    public String getSwapCurrency() { return swapCurrency; }
    public void setSwapCurrency(String swapCurrency) { this.swapCurrency = swapCurrency; }

    public BigDecimal getSpread() { return spread; }
    public void setSpread(BigDecimal spread) { this.spread = spread; }

    public BigDecimal getMarketPrice() { return marketPrice; }
    public void setMarketPrice(BigDecimal marketPrice) { this.marketPrice = marketPrice; }

    public String getUnderlyingInvestmentId() { return underlyingInvestmentId; }
    public void setUnderlyingInvestmentId(String underlyingInvestmentId) {
        this.underlyingInvestmentId = underlyingInvestmentId;
    }

    public String getUnderlyingCurrency() { return underlyingCurrency; }
    public void setUnderlyingCurrency(String underlyingCurrency) {
        this.underlyingCurrency = underlyingCurrency;
    }

    public String getUserTranId() { return userTranId; }
    public void setUserTranId(String userTranId) { this.userTranId = userTranId; }

    public Long getKnowledgeDate() { return knowledgeDate; }
    public void setKnowledgeDate(Long knowledgeDate) { this.knowledgeDate = knowledgeDate; }
}

