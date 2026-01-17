package com.example.machineevents.service;

import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.machineevents.model.EventEntity;
import com.example.machineevents.repository.EventRepository;

@Service
public class EventService {

    private final EventRepository repository;

    public EventService(EventRepository repository) {
        this.repository = repository;
    }

    // ✅ GET ALL EVENTS (ORDERED)
    public List<EventEntity> getAllEvents() {
        return repository.findAll(
                Sort.by(Sort.Direction.ASC, "eventTime"));
    }

    // ✅ GET BY MACHINE + TIME (ORDERED)
    public List<EventEntity> getEventsByMachineAndTime(
            String machineId,
            Instant start,
            Instant end) {
        return repository
                .findByMachineIdAndEventTimeBetweenOrderByEventTimeAsc(
                        machineId, start, end);
    }
}
