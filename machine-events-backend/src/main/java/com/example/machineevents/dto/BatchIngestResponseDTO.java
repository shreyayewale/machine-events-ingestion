package com.example.machineevents.dto;

import java.util.ArrayList;
import java.util.List;

public class BatchIngestResponseDTO {
    public int accepted;
    public int deduped;
    public int updated;
    public int rejected;
    public List<RejectionDTO> rejections = new ArrayList<>();
}
