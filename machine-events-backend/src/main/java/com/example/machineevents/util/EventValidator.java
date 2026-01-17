package com.example.machineevents.util;

import com.example.machineevents.dto.EventRequestDTO;
import com.example.machineevents.exception.ValidationException;

import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class EventValidator {

    public void validate(EventRequestDTO event) {
        if (event.durationMs < 0 || event.durationMs > 6 * 60 * 60 * 1000) {
            throw new ValidationException("INVALID_DURATION");
        }
        if (event.eventTime.isAfter(Instant.now().plusSeconds(900))) {
            throw new ValidationException("FUTURE_EVENT_TIME");
        }
    }
}
