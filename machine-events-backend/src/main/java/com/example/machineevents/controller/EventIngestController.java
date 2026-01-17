package com.example.machineevents.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.example.machineevents.dto.BatchIngestResponseDTO;
import com.example.machineevents.dto.EventRequestDTO;
import com.example.machineevents.model.EventEntity;
import com.example.machineevents.service.EventIngestService;
import com.example.machineevents.service.EventService;

@RestController
@RequestMapping("/events")
public class EventIngestController {

    private final EventService eventService;
    private final EventIngestService ingestService;

    public EventIngestController(EventService eventService, EventIngestService ingestService) {
        this.eventService = eventService;
        this.ingestService = ingestService;
    }

    // ✅ Batch ingestion endpoint
    @PostMapping("/ingest")
    public BatchIngestResponseDTO ingest(@RequestBody List<EventRequestDTO> events) {
        return ingestService.ingestEvents(events);
    }

    // ✅ Get all events (ordered)
    @GetMapping
    public List<EventEntity> getAllEvents() {
        return eventService.getAllEvents();
    }

    // ✅ Get events by machine + time
    @GetMapping("/machine/{machineId}")
    public List<EventEntity> getEventsByMachine(
            @PathVariable String machineId,
            @RequestParam String start,
            @RequestParam String end) {
        return eventService.getEventsByMachineAndTime(machineId,
                java.time.Instant.parse(start),
                java.time.Instant.parse(end));
    }
}
