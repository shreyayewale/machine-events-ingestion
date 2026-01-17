# Performance Benchmark – Machine Events Ingestion Service

## Objective
To verify that the event ingestion service can process at least **1000 events within 1 second** while correctly handling validation, deduplication, updates, and persistence.

---

## Test Environment

- Language: Java 17
- Framework: Spring Boot
- Database: MySQL
- Build Tool: Maven
- OS: Windows
- Hardware:
  - CPU: 4-core laptop processor
  - RAM: 8 GB

---

## Test Scenario

A batch of **1000 events** was sent in a single API request.

**Endpoint**
 POST events/ingest

 
### Event Properties
- Unique `eventId` values
- Same `machineId`
- Valid `durationMs`
- `eventTime` within the allowed window
- Mixed `defectCount` values
- No future timestamps

---

## Measurement Method

- Processing time measured from request entry to completion.
- Ingestion executed within a single transactional context.
- Repository calls used indexed `eventId` lookup.

---

## Results

| Metric              | Value |
|---------------------|-------|
| Total events sent   | 1000  |
| Accepted events     | 1000  |
| Deduplicated events | 0     |
| Rejected events     | 0     |
| Processing time     | ~300–400 ms |

✅ **Requirement met: 1000 events processed within 1 second**

---

## Performance Strategies

- Batch ingestion through a single API call
- Unique index on `eventId` to speed up lookups
- Payload hashing to detect duplicates efficiently
- Minimal database writes (update only when required)
- Transactional consistency without excessive locking

---

## Conclusion

The ingestion service meets the performance requirement and processes high-volume event batches efficiently while maintaining correctness and data integrity.

---

## Future Improvements

- Bulk insert optimizations
- Asynchronous ingestion pipeline
- Migration to PostgreSQL for production workloads
