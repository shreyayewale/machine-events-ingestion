package com.example.machineevents.service;

import com.example.machineevents.dto.StatsResponseDTO;
import com.example.machineevents.model.EventEntity;
import com.example.machineevents.repository.EventRepository;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public class StatsService {

    private final EventRepository repository;

    public StatsService(EventRepository repository) {
        this.repository = repository;
    }

    public StatsResponseDTO getStats(
            String machineId,
            Instant start,
            Instant end) {

        // ✅ ORDERED FETCH
        List<EventEntity> events = repository.findByMachineIdAndEventTimeBetweenOrderByEventTimeAsc(
                machineId, start, end);

        long eventsCount = events.size();

        long defectsCount = events.stream()
                .filter(e -> e.getDefectCount() != -1)
                .mapToLong(EventEntity::getDefectCount)
                .sum();

        double windowHours = Duration.between(start, end).toSeconds() / 3600.0;

        double avgDefectRate = windowHours == 0 ? 0 : defectsCount / windowHours;

        StatsResponseDTO dto = new StatsResponseDTO();
        dto.machineId = machineId;
        dto.start = start;
        dto.end = end;
        dto.eventsCount = eventsCount;
        dto.defectsCount = defectsCount;
        dto.avgDefectRate = avgDefectRate;
        dto.status = avgDefectRate < 2.0 ? "Healthy" : "Warning";

        return dto;
    }
}
