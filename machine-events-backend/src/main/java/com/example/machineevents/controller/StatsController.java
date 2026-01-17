package com.example.machineevents.controller;

import com.example.machineevents.dto.StatsResponseDTO;
import com.example.machineevents.service.StatsService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/stats")
public class StatsController {

    private final StatsService service;

    public StatsController(StatsService service) {
        this.service = service;
    }

    @GetMapping
    public StatsResponseDTO stats(
            @RequestParam String machineId,
            @RequestParam Instant start,
            @RequestParam Instant end) {
        return service.getStats(machineId, start, end);
    }
}
