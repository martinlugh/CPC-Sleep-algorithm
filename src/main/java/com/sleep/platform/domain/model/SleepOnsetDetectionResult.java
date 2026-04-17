package com.sleep.platform.domain.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class SleepOnsetDetectionResult {

    private LocalDateTime mainSleepStartTime;
    private Integer mainSleepLatencyMinutes;
    private List<String> sleepOnsetReasonTags;
    private String sleepOnsetExplainText;
}
