package com.sleep.platform.domain.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class WakeUpDetectionResult {

    private LocalDateTime mainSleepWakeUpTime;
    private List<String> wakeUpReasonTags;
    private String wakeUpExplainText;
}
