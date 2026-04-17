package com.sleep.platform.domain.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class SleepAnalysisResponse {

    private Long sessionId;
    private String userId;
    private LocalDate analysisDate;
    private LocalDateTime mainSleepStartTime;
    private LocalDateTime mainSleepWakeUpTime;
    private Integer mainSleepLatencyMinutes;
    private Integer mainSleepTotalMinutes;
    private Integer mainSleepDeepMinutes;
    private Integer mainSleepLightMinutes;
    private Integer mainSleepRemMinutes;
    private Integer mainSleepAwakeMinutes;
    private Double mainSleepQualityScore;
    private Double nightlyRecoveryScore;
    private Double nightlyFatigueScore;
    private Integer mainSleepAwakenCount;
    private Double mainSleepEfficiency;
    private Integer dailyTotalSleepMinutes;
    private Integer daytimeNapTotalMinutes;
    private Integer daytimeNapCount;
    private Double dataQualityScore;
    private List<DaytimeNapResultItem> daytimeNapResultList;
    private List<SleepStageTimelineItem> sleepStageTimeline;
    private List<SleepSegmentAnalysisResult> sleepSegmentResultList;
    private Map<String, Object> scoreExplanation;
    private Map<String, Object> sleepOnsetReason;
    private Map<String, Object> wakeUpReason;
    private Map<String, Object> alignmentExplanation;
}
