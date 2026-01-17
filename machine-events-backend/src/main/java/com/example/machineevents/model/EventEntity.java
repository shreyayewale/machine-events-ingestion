package com.example.machineevents.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

@Entity
@Table(name = "events", uniqueConstraints = {
        @UniqueConstraint(columnNames = "eventId")
})
public class EventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String eventId;
    private Instant eventTime;
    private Instant receivedTime;
    private String machineId;
    private long durationMs;
    private int defectCount;
    private String payloadHash;

    // -------------------
    // Getters
    // -------------------
    public Long getId() {
        return id;
    }

    public String getEventId() {
        return eventId;
    }

    public Instant getEventTime() {
        return eventTime;
    }

    public Instant getReceivedTime() {
        return receivedTime;
    }

    public String getMachineId() {
        return machineId;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public int getDefectCount() {
        return defectCount;
    }

    public String getPayloadHash() {
        return payloadHash;
    }

    // -------------------
    // Setters
    // -------------------
    public void setId(Long id) {
        this.id = id;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public void setEventTime(Instant eventTime) {
        this.eventTime = eventTime;
    }

    public void setReceivedTime(Instant receivedTime) {
        this.receivedTime = receivedTime;
    }

    public void setMachineId(String machineId) {
        this.machineId = machineId;
    }

    public void setDurationMs(long durationMs) {
        this.durationMs = durationMs;
    }

    public void setDefectCount(int defectCount) {
        this.defectCount = defectCount;
    }

    public void setPayloadHash(String payloadHash) {
        this.payloadHash = payloadHash;
    }
}
