# Fix: ClassCastException with Avro Models and Spring Boot DevTools

## **Problem: ClassCastException - Same Class, Different ClassLoaders**

### Error Message
```
java.lang.ClassCastException: class com.ssnc.avroModels.OrderDetails cannot be cast to class com.ssnc.avroModels.OrderDetails 
(com.ssnc.avroModels.OrderDetails is in unnamed module of loader 'app'; 
com.ssnc.avroModels.OrderDetails is in unnamed module of loader org.springframework.boot.devtools.restart.classloader.RestartClassLoader)
```

### Root Cause
This is a **classloader conflict** caused by **Spring Boot DevTools**. The same `OrderDetails` class is being loaded by two different classloaders:

1. **Application ClassLoader** - Loads the initial version
2. **DevTools Restart ClassLoader** - Reloads classes for hot-reloading

When Kafka deserializes your Avro message, it uses the class from one classloader, but when your consumer method tries to use it, Spring expects the class from the other classloader. Even though they're the same class, Java treats them as incompatible types.

---

## **Solution Applied: Exclude Avro Models from DevTools Restart**

I've configured Spring DevTools to **exclude** the Avro-generated classes from being reloaded. This ensures only one classloader is used for these classes.

### Configuration Added to `application.yaml`
```yaml
spring:
  devtools:
    restart:
      additional-exclude: "com/ssnc/avroModels/**"
```

This tells DevTools:
- ✅ Don't reload classes in the `com.ssnc.avroModels` package
- ✅ Keep using the original classloader for Avro models
- ✅ Still hot-reload your business logic classes

---

## **How to Test the Fix**

### 1. Restart Your Application
```powershell
# If running in IDE, just restart
# If running with Maven:
mvn spring-boot:run
```

### 2. Send a Test Order
```powershell
curl -X POST http://localhost:8080/api/orders `
  -H "Content-Type: application/json" `
  -d '{
    "orderId": "ORD-001",
    "customerId": "CUST-123",
    "amount": 99.99,
    "status": "PENDING",
    "createdAt": "2025-11-17T10:00:00Z"
  }'
```

### 3. Verify Success
You should now see in the logs:
```
INFO  Received order from Kafka - Topic: order-details-topic, Partition: 0, Offset: 0
INFO  Order Details - ID: ORD-001, Customer: CUST-123, Amount: 99.99, Status: PENDING
INFO  Processing order with ID: ORD-001
```

**No more ClassCastException!** ✅

---

## **Alternative Solutions** (If the Problem Persists)

### Option 1: Disable DevTools (Simple but Loses Hot-Reload)
In `pom.xml`, comment out the DevTools dependency:
```xml
<!-- Temporarily disabled due to classloader issues
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <scope>runtime</scope>
    <optional>true</optional>
</dependency>
-->
```

**Pros:** Completely eliminates the classloader issue  
**Cons:** Lose hot-reload capability during development

### Option 2: Use `devtools.restart.exclude` in application.properties
Create `src/main/resources/META-INF/spring-devtools.properties`:
```properties
restart.exclude.avro=/com/ssnc/avroModels/**
```

### Option 3: Run Without DevTools in Production
DevTools is automatically disabled when:
- Running the packaged JAR (not from IDE)
- Running in Docker containers
- `SPRING_DEVTOOLS_RESTART_ENABLED=false` environment variable is set

---

## **Why This Happens**

### Understanding the ClassLoader Issue

1. **Initial Load:**
   - App starts → `OrderDetails` loaded by app classloader
   - Kafka deserializer uses this class

2. **DevTools Restart:**
   - You make a code change
   - DevTools creates a new `RestartClassLoader`
   - Reloads your application classes (including Avro models)

3. **The Conflict:**
   - Kafka still uses `OrderDetails` from the original classloader
   - Your consumer method expects `OrderDetails` from the restart classloader
   - Java sees these as **different classes** → ClassCastException

### Why Avro Models Are Problematic
- Avro models are **generated classes** (not source code)
- They're used by Kafka serializers/deserializers (outside Spring's control)
- They shouldn't be reloaded because they represent the message schema

---

## **Best Practices**

### For Development
✅ Keep the DevTools exclusion configuration  
✅ Avro models don't need hot-reload (they're generated from schema)  
✅ Your business logic can still be hot-reloaded

### For Production/Docker
✅ DevTools is automatically disabled  
✅ No classloader conflicts  
✅ The issue only affects local development

### When to Regenerate Avro Models
Only regenerate when you change the `.avsc` schema file:
```powershell
mvn clean compile
```

---

## **Verification Checklist**

After applying the fix:

- [ ] Application starts without errors
- [ ] Can send orders via REST API
- [ ] Consumer receives and processes messages
- [ ] No ClassCastException in logs
- [ ] Hot-reload still works for your business logic
- [ ] DevTools exclusion is in `application.yaml`

---

## **Summary**

✅ **Issue:** ClassCastException due to DevTools creating multiple classloaders for Avro models  
✅ **Fix:** Exclude Avro models from DevTools restart  
✅ **Configuration:** Added `spring.devtools.restart.additional-exclude: "com/ssnc/avroModels/**"`  
✅ **Result:** Single classloader for Avro models, no more cast exceptions  

The fix allows you to keep DevTools for development convenience while avoiding the classloader conflict with Kafka's Avro serialization!

