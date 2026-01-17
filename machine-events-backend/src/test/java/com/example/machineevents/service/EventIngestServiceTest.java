package com.example.machineevents.service;

import com.example.machineevents.dto.*;
import com.example.machineevents.model.EventEntity;
import com.example.machineevents.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class EventIngestServiceTest {

    @Autowired
    private EventIngestService ingestService;

    @Autowired
    private EventRepository repository;

    @BeforeEach
    void setup() {
        repository.deleteAll();
    }

    @Test
    void testDuplicateEventId() throws Exception {
        EventRequestDTO dto = createEvent("E1", Instant.now(), "M1", 1000, 0);
        ingestService.ingestEvents(Collections.singletonList(dto));
        BatchIngestResponseDTO response = ingestService.ingestEvents(Collections.singletonList(dto));
        assertEquals(0, response.accepted);
        assertEquals(1, response.deduped);
    }

    @Test
    void testUpdateNewerPayload() throws Exception {
        Instant now = Instant.now();
        EventRequestDTO dto1 = createEvent("E2", now, "M1", 1000, 1);
        ingestService.ingestEvents(Collections.singletonList(dto1));
        EventRequestDTO dto2 = createEvent("E2", now.plusSeconds(10), "M1", 2000, 2);
        BatchIngestResponseDTO response = ingestService.ingestEvents(Collections.singletonList(dto2));
        assertEquals(1, response.updated);
    }

    @Test
    void testIgnoreOlderPayload() throws Exception {
        Instant now = Instant.now();
        EventRequestDTO dto1 = createEvent("E3", now.plusSeconds(10), "M1", 1000, 1);
        ingestService.ingestEvents(Collections.singletonList(dto1));
        EventRequestDTO dto2 = createEvent("E3", now, "M1", 2000, 2);
        BatchIngestResponseDTO response = ingestService.ingestEvents(Collections.singletonList(dto2));
        assertEquals(1, response.deduped);
    }

    @Test
    void testInvalidDuration() {
        EventRequestDTO dto = createEvent("E4", Instant.now(), "M1", 10_000_000, 0);
        BatchIngestResponseDTO response = ingestService.ingestEvents(Collections.singletonList(dto));
        assertEquals(1, response.rejected);
        assertEquals("INVALID_DURATION", response.rejections.get(0).reason);
    }

    @Test
    void testFutureEventTime() {
        EventRequestDTO dto = createEvent("E5", Instant.now().plusSeconds(3600), "M1", 1000, 0);
        BatchIngestResponseDTO response = ingestService.ingestEvents(Collections.singletonList(dto));
        assertEquals(1, response.rejected);
        assertEquals("FUTURE_EVENT_TIME", response.rejections.get(0).reason);
    }

    @Test
    void testDefectCountIgnoredInStats() throws Exception {
        EventRequestDTO dto = createEvent("E6", Instant.now(), "M1", 1000, -1);
        ingestService.ingestEvents(Collections.singletonList(dto));
        EventEntity entity = repository.findByEventId("E6").get();
        assertEquals(-1, entity.getDefectCount());
    }

    @Test
    void testStartEndBoundary() throws Exception {
        Instant start = Instant.now();
        Instant end = start.plusSeconds(10);
        EventRequestDTO dto1 = createEvent("E7", start, "M1", 1000, 1);
        EventRequestDTO dto2 = createEvent("E8", end, "M1", 1000, 1);
        ingestService.ingestEvents(Arrays.asList(dto1, dto2));
        List<EventEntity> events = repository.findByMachineIdAndEventTimeBetweenOrderByEventTimeAsc("M1", start, end);
        assertEquals(2, events.size());
    }

    @Test
    void testConcurrentIngestion() throws InterruptedException, ExecutionException {
        int threads = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        List<Callable<BatchIngestResponseDTO>> tasks = Collections.nCopies(threads,
                () -> ingestService
                        .ingestEvents(Collections.singletonList(createEvent("E9", Instant.now(), "M1", 1000, 1))));
        List<Future<BatchIngestResponseDTO>> results = executor.invokeAll(tasks);
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        long acceptedCount = results.stream().mapToLong(f -> {
            try {
                return f.get().accepted;
            } catch (Exception e) {
                return 0;
            }
        }).sum();

        assertEquals(1, acceptedCount);
    }

    private EventRequestDTO createEvent(String eventId, Instant time, String machineId, long durationMs,
            int defectCount) {
        EventRequestDTO dto = new EventRequestDTO();
        dto.eventId = eventId;
        dto.eventTime = time;
        dto.machineId = machineId;
        dto.durationMs = durationMs;
        dto.defectCount = defectCount;
        return dto;
    }
}
