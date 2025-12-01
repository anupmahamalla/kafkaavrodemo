# TaxLot Test Script - Partition Testing Guide

## Overview

The updated `test-taxlots.ps1` script now includes comprehensive partition testing scenarios to demonstrate how Kafka distributes messages across 3 partitions based on message keys.

---

## Test Scenarios

### Test 1: Health Check ✅
**Purpose:** Verify the TaxLot service is running

**Endpoint:** `GET /api/taxlots/test`

**Expected:** "Tax lot service is running!"

---

### Test 2: Single Tax Lot ✅
**Purpose:** Create a single tax lot with standard fields

**Key:** `EVT-001:INV-12345`

**Expected:** Message successfully sent and consumed

---

### Test 3: Partition Distribution Test ✅
**Purpose:** Demonstrate automatic partition distribution

**Endpoint:** `POST /api/taxlots/test-partitions`

**What it does:**
- Sends **5 messages** with different keys
- Shows partition distribution summary
- Lists all sent messages

**Keys used:**
1. `EVT-001:INV-A` → Partition ?
2. `EVT-002:INV-B` → Partition ?
3. `EVT-003:INV-C` → Partition ?
4. `EVT-004:INV-D` → Partition ?
5. `EVT-005:INV-E` → Partition ?

**Expected Response:**
```json
{
  "status": "success",
  "messagesSent": 5,
  "partitions": 3,
  "message": "Check consumer logs to see partition distribution",
  "sentMessages": [...]
}
```

---

### Test 4: Same Key → Same Partition ✅
**Purpose:** Prove messages with identical keys go to the same partition

**Scenario:** Send **3 messages** with the **SAME key**: `EVT-SAME:INV-SAME`

**Messages:**
1. Message 1: `EVT-SAME:INV-SAME`, Client: CLIENT-SAME-1
2. Message 2: `EVT-SAME:INV-SAME`, Client: CLIENT-SAME-2
3. Message 3: `EVT-SAME:INV-SAME`, Client: CLIENT-SAME-3

**Expected in Logs:**
```
📨 PARTITION: 1, Key: [EVT-SAME, INV-SAME] - CLIENT-SAME-1
📨 PARTITION: 1, Key: [EVT-SAME, INV-SAME] - CLIENT-SAME-2
📨 PARTITION: 1, Key: [EVT-SAME, INV-SAME] - CLIENT-SAME-3
```

All 3 messages should show the **same partition number**!

---

### Test 5: Different Keys → Different Partitions ✅
**Purpose:** Demonstrate distribution across partitions with different keys

**Scenario:** Send **6 messages** with **6 different keys**

**Keys:**
1. `EVT-A:INV-001` → Partition ?
2. `EVT-B:INV-002` → Partition ?
3. `EVT-C:INV-003` → Partition ?
4. `EVT-D:INV-004` → Partition ?
5. `EVT-E:INV-005` → Partition ?
6. `EVT-F:INV-006` → Partition ?

**Expected in Logs:**
```
📨 PARTITION: 0, Key: [EVT-A, INV-001]
📨 PARTITION: 1, Key: [EVT-B, INV-002]
📨 PARTITION: 2, Key: [EVT-C, INV-003]
📨 PARTITION: 0, Key: [EVT-D, INV-004]
📨 PARTITION: 1, Key: [EVT-E, INV-005]
📨 PARTITION: 2, Key: [EVT-F, INV-006]
```

Messages should be **distributed across partitions 0, 1, and 2**!

---

## Running the Tests

### Execute the Script

```powershell
.\test-taxlots.ps1
```

### What You'll See

```
Testing TaxLot API Endpoints
==============================

Test 1: Testing health check endpoint...
Response: Tax lot service is running!

Test 2: Creating a sample tax lot...
Request Body: {...}
Response: Tax lot created successfully with EventId: EVT-001

Test 3: Testing partition distribution...
Sending 5 messages with different keys to demonstrate partition distribution
Partition Test Response:
Status: success
Messages Sent: 5
Partitions: 3

Sent Messages:
  - EventId: EVT-001, InvestmentId: INV-A, Client: CLIENT-A
  - EventId: EVT-002, InvestmentId: INV-B, Client: CLIENT-B
  - EventId: EVT-003, InvestmentId: INV-C, Client: CLIENT-C
  - EventId: EVT-004, InvestmentId: INV-D, Client: CLIENT-D
  - EventId: EVT-005, InvestmentId: INV-E, Client: CLIENT-E

Check consumer logs to see partition distribution (PARTITION: 0, 1, or 2)

Test 4: Testing same key -> same partition...
Sending 3 messages with the SAME key (EVT-SAME:INV-SAME)
  Message 1 sent: Tax lot created successfully...
  Message 2 sent: Tax lot created successfully...
  Message 3 sent: Tax lot created successfully...

All 3 messages should appear in the SAME partition in logs!

Test 5: Testing different keys -> different partitions...
Sending 6 messages with DIFFERENT keys
  Message 1 (EVT-A:INV-001) sent: Tax lot created successfully...
  Message 2 (EVT-B:INV-002) sent: Tax lot created successfully...
  Message 3 (EVT-C:INV-003) sent: Tax lot created successfully...
  Message 4 (EVT-D:INV-004) sent: Tax lot created successfully...
  Message 5 (EVT-E:INV-005) sent: Tax lot created successfully...
  Message 6 (EVT-F:INV-006) sent: Tax lot created successfully...

These 6 messages should be distributed across 3 partitions in logs!

==============================
Testing complete!

📊 SUMMARY:
  ✓ Test 1: Health check
  ✓ Test 2: Single tax lot
  ✓ Test 3: Partition distribution test (5 messages)
  ✓ Test 4: Same key -> same partition (3 messages)
  ✓ Test 5: Different keys -> distributed (6 messages)

📋 Total messages sent: 15

🔍 CHECK LOGS TO SEE:
  • Partition numbers (PARTITION: 0, 1, or 2)
  • Messages with same key go to same partition
  • Messages with different keys distributed across partitions
  • 3 consumer threads processing in parallel
```

---

## Verifying Results

### In Application Logs

Look for consumer log messages:

**Test 4 Verification (Same Key):**
```
INFO - 📨 Received tax lot - PARTITION: 1, Offset: 5, Key: [EVT-SAME, INV-SAME] - CLIENT-SAME-1
INFO - 📨 Received tax lot - PARTITION: 1, Offset: 6, Key: [EVT-SAME, INV-SAME] - CLIENT-SAME-2
INFO - 📨 Received tax lot - PARTITION: 1, Offset: 7, Key: [EVT-SAME, INV-SAME] - CLIENT-SAME-3
```
✅ All on **PARTITION: 1** (same partition for same key)

**Test 5 Verification (Different Keys):**
```
INFO - 📨 Received tax lot - PARTITION: 0, Key: [EVT-A, INV-001]
INFO - 📨 Received tax lot - PARTITION: 1, Key: [EVT-B, INV-002]
INFO - 📨 Received tax lot - PARTITION: 2, Key: [EVT-C, INV-003]
INFO - 📨 Received tax lot - PARTITION: 0, Key: [EVT-D, INV-004]
INFO - 📨 Received tax lot - PARTITION: 1, Key: [EVT-E, INV-005]
INFO - 📨 Received tax lot - PARTITION: 2, Key: [EVT-F, INV-006]
```
✅ Distributed across **partitions 0, 1, and 2**

### In Kafka UI (http://localhost:8080)

1. Navigate to **Topics** → **taxlot-details-topic**
2. View **Partitions** tab
3. See message distribution:
   - Partition 0: X messages
   - Partition 1: Y messages
   - Partition 2: Z messages

---

## Message Statistics

| Test | Messages | Keys | Expected Behavior |
|------|----------|------|-------------------|
| Test 1 | 0 | - | Health check only |
| Test 2 | 1 | 1 unique | Single message |
| Test 3 | 5 | 5 unique | Distributed across partitions |
| Test 4 | 3 | 1 same | All to same partition |
| Test 5 | 6 | 6 unique | Distributed across partitions |
| **TOTAL** | **15** | **13 unique** | Mixed distribution |

---

## Key Concepts Demonstrated

### 1. Key-Based Partitioning
- Kafka uses `hash(key) % partitions` to assign partition
- Same key → deterministic partition assignment

### 2. Ordering Guarantees
- Messages with same key maintain order within partition
- Messages with different keys can be processed in parallel

### 3. Parallel Processing
- 3 partitions = 3 concurrent consumers
- Different keys processed simultaneously
- Same keys processed sequentially (in order)

### 4. Load Distribution
- Different keys spread load across partitions
- Prevents hotspots (all messages to one partition)

---

## Troubleshooting

### All Messages Go to Same Partition
**Problem:** Poor key distribution

**Solution:** Check if keys are truly different
```powershell
# Verify keys in logs
Get-Content .\logs\application.log | Select-String "Key:"
```

### No Messages in Logs
**Problem:** Consumer not running or topic doesn't exist

**Solution:** 
1. Check application is running
2. Verify topic created: Check Kafka UI
3. Check consumer group is active

### Partition Numbers Don't Match
**Problem:** Topic recreated or using different topic

**Solution:**
1. Check topic name matches configuration
2. Verify partition count in topic configuration

---

## Advanced Scenarios

### Test Custom Keys

Modify the script to test your own keys:

```powershell
$customData = @{
    eventId = "YOUR-EVENT-ID"
    investmentId = "YOUR-INVESTMENT-ID"
    # ... other fields
} | ConvertTo-Json

Invoke-RestMethod -Uri "$baseUrl/api/taxlots" `
    -Method Post `
    -Body $customData `
    -ContentType "application/json"
```

### Stress Test Partitions

Send many messages quickly:

```powershell
for ($i = 1; $i -le 100; $i++) {
    # Send message with key "EVT-$i:INV-$i"
    # Watch distribution in Kafka UI
}
```

---

## Summary

✅ **5 comprehensive tests** covering all partition scenarios  
✅ **15 total messages** sent with various key combinations  
✅ **Same key → same partition** demonstrated (Test 4)  
✅ **Different keys → distributed** demonstrated (Test 5)  
✅ **Easy verification** with enhanced logging  
✅ **Production-ready** testing approach  

Run `.\test-taxlots.ps1` and watch your messages distribute across 3 partitions! 🚀

