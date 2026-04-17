package com.sleep.platform.domain.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class MainSleepSummaryResult {

    private LocalDateTime mainSleepStartTime;
    private LocalDateTime mainSleepWakeUpTime;
    private Integer mainSleepLatencyMinutes;
    private Integer mainSleepTotalMinutes;
    private Integer mainSleepDeepMinutes;
    private Integer mainSleepLightMinutes;
    private Integer mainSleepRemMinutes;
    private Integer mainSleepAwakeMinutes;
    private Integer mainSleepAwakenCount;
    private Double mainSleepEfficiency;
    private String summaryExplainText;
    private List<String> summaryExplainTags;
}
