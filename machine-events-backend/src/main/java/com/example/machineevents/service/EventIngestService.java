package com.example.machineevents.service;

import com.example.machineevents.dto.BatchIngestResponseDTO;
import com.example.machineevents.dto.EventRequestDTO;
import com.example.machineevents.dto.RejectionDTO;
import com.example.machineevents.exception.ValidationException;
import com.example.machineevents.model.EventEntity;
import com.example.machineevents.repository.EventRepository;
import com.example.machineevents.util.EventValidator;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.time.Instant;
import java.util.List;

@Service
public class EventIngestService {

    private final EventRepository repository;
    private final EventValidator validator;

    public EventIngestService(EventRepository repository, EventValidator validator) {
        this.repository = repository;
        this.validator = validator;
    }

    @Transactional
    public synchronized BatchIngestResponseDTO ingestEvents(List<EventRequestDTO> events) {
        BatchIngestResponseDTO response = new BatchIngestResponseDTO();

        for (EventRequestDTO dto : events) {
            try {
                validator.validate(dto);
                String payloadHash = generatePayloadHash(dto);

                EventEntity existing = repository.findByEventId(dto.eventId).orElse(null);

                if (existing != null) {
                    if (existing.getPayloadHash().equals(payloadHash)) {
                        response.deduped++;
                        continue;
                    } else if (dto.eventTime.isAfter(existing.getReceivedTime())) {
                        existing.setEventTime(dto.eventTime);
                        existing.setMachineId(dto.machineId);
                        existing.setDurationMs(dto.durationMs);
                        existing.setDefectCount(dto.defectCount);
                        existing.setPayloadHash(payloadHash);
                        existing.setReceivedTime(Instant.now());
                        repository.save(existing);
                        response.updated++;
                        continue;
                    } else {
                        response.deduped++;
                        continue;
                    }
                }

                EventEntity entity = new EventEntity();
                entity.setEventId(dto.eventId);
                entity.setEventTime(dto.eventTime);
                entity.setMachineId(dto.machineId);
                entity.setDurationMs(dto.durationMs);
                entity.setDefectCount(dto.defectCount);
                entity.setReceivedTime(Instant.now());
                entity.setPayloadHash(payloadHash);

                repository.save(entity);
                response.accepted++;

            } catch (ValidationException ex) {
                response.rejected++;
                RejectionDTO rej = new RejectionDTO();
                rej.eventId = dto.eventId;
                rej.reason = ex.getMessage();
                response.rejections.add(rej);
            } catch (Exception ex) {
                response.rejected++;
                RejectionDTO rej = new RejectionDTO();
                rej.eventId = dto.eventId;
                rej.reason = "UNKNOWN_ERROR";
                response.rejections.add(rej);
            }
        }

        return response;
    }

    private String generatePayloadHash(EventRequestDTO dto) throws Exception {
        String data = dto.machineId + "|" + dto.eventTime + "|" + dto.durationMs + "|" + dto.defectCount;
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] digest = md.digest(data.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : digest)
            sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
