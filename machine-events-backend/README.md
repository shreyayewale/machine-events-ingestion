# Machine Events Backend Service

## 1. Architecture

This project is implemented as a Spring Boot RESTful backend service following a layered architecture:

- **Controller Layer**  
  Exposes REST APIs for event ingestion and statistics retrieval.

- **Service Layer**  
  Contains core business logic for:
  - Batch ingestion
  - Deduplication and update handling
  - Statistics calculation
  - Validation

- **Repository Layer**  
  Uses Spring Data JPA to interact with the database.

- **Model / Entity Layer**  
  Represents persisted event data using JPA entities.

- **DTO Layer**  
  Separates API request/response models from persistence models.

This separation ensures maintainability, testability, and clarity of responsibilities.

---

## 2. Deduplication & Update Logic

Each event is uniquely identified by `eventId`.

The deduplication and update flow is:

1. When an event arrives, the system checks if an event with the same `eventId` already exists.
2. A **payload hash** (MD5) is generated using:
3. Logic:
- **Same eventId + same payload hash** → event is **deduplicated**
- **Same eventId + different payload hash**
  - If incoming eventTime is newer → existing record is **updated**
  - If incoming eventTime is older → event is **ignored**
4. The “winning” record is always the one with the **newer eventTime**.

This ensures idempotency and correctness even with retries or delayed events.

---

## 3. Thread-Safety

Thread safety is ensured using multiple layers:

- **`@Transactional`**  
Ensures atomicity of batch ingestion.

- **`synchronized` ingestion method**  
Prevents race conditions when multiple threads ingest the same eventId concurrently.

- **Database unique constraint on `eventId`**  
Acts as a final safety net to prevent duplicate records.

Together, these mechanisms guarantee:
- No duplicate rows
- No lost updates
- Correct counts under concurrent ingestion

---

## 4. Data Model

### Events Table Schema

| Column Name    | Type        | Description |
|----------------|------------|-------------|
| id             | Long (PK)  | Auto-generated primary key |
| eventId        | String     | Unique event identifier |
| eventTime      | Instant    | Time when event occurred |
| receivedTime   | Instant    | Time when event was received |
| machineId      | String     | Machine identifier |
| durationMs     | Long       | Duration in milliseconds |
| defectCount    | Integer    | Defect count (-1 means unknown) |
| payloadHash    | String     | Hash used for deduplication |

`eventId` has a **unique constraint** at the database level.

---

## 5. Performance Strategy (1000 events < 1 sec)

To meet performance requirements:

- **Batch ingestion** instead of per-event API calls
- **Single transaction per batch**
- **Indexed unique eventId** for fast lookups
- Minimal object creation and simple hash computation
- No blocking external calls during ingestion

These optimizations allow ingestion of ~1000 events well under 1 second on a local machine.

---

## 6. Edge Cases & Assumptions

Handled edge cases:

- **Duplicate events** → deduplicated using payload hash
- **Out-of-order events** → older events ignored
- **Future eventTime** → rejected
- **Invalid durationMs** (<0 or >6 hours) → rejected
- **defectCount = -1** → excluded from defect totals but event still counted
- **Time window boundaries** → start and end are inclusive

Assumptions:
- `eventId` uniquely identifies a logical event
- Backend sets `receivedTime`
- Accuracy is preferred over accepting potentially corrupt data

---

## 7. Setup & Run Instructions

### Prerequisites
- Java 17+
- Maven
- Any relational DB (H2 / MySQL / PostgreSQL)

### Run Locally

```bash
mvn clean install
mvn spring-boot:run

APIs

POST /events/ingest – Batch event ingestion
GET /stats – Machine statistics for a time window

8. Improvements With More Time

Given more time, the following improvements would be made:

Replace synchronized with optimistic locking or DB-level locking

Asynchronous ingestion using message queues (Kafka/RabbitMQ)

Better hashing strategy for very large payloads

Pagination and caching for stats API

Load testing and production-grade benchmarks

Distributed locking for multi-instance deployments