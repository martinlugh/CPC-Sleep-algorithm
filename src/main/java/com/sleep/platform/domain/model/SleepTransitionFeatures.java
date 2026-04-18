package com.sleep.platform.domain.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SleepTransitionFeatures {

    private LocalDateTime segmentStartTime;
    private boolean isLowMotionSegment;
    private boolean isHighMotionSegment;
    private boolean isHeartRateNearNightBaseline;
    private boolean isHeartRateRiseComparedToPrevious;
    private boolean isHeartRateDropComparedToPrevious;
    private boolean isSleepStageCandidate;
    private boolean isWakeStageCandidate;
    private boolean isDaytimeNapCandidate;
    private int consecutiveLowMotionCount;
    private int consecutiveWakeLikeCount;
    private int consecutiveSleepLikeCount;
    private int consecutiveDaytimeNapLikeCount;
    private double motionEvidenceWeight;
    private double physiologicalEvidenceWeight;
    private double motionAlignmentConfidence;
}
