package com.example.machineevents.repository;

import com.example.machineevents.model.EventEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Sort;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<EventEntity, Long> {

    Optional<EventEntity> findByEventId(String eventId);

    // ORDERED by eventTime
    List<EventEntity> findByMachineIdAndEventTimeBetweenOrderByEventTimeAsc(
            String machineId,
            Instant start,
            Instant end);

    List<EventEntity> findAll(Sort sort);
}
