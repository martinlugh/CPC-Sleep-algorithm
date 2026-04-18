package com.sleep.platform.domain.response;

import lombok.Data;

@Data
public class SleepConfigResponse {

    private String currentModelVersion;
    private String currentRuleVersion;
    private Integer minValidRriCount;
    private Double maxAllowedMissingRate;
    private Integer wakeStepThreshold;
    private Integer sleepOnsetMaxAlignedStepThreshold;
    private Integer wakeUpAlignedStepThreshold;
    private Double deepHfcLfcRatioThreshold;
    private Double remSd2Sd1RatioThreshold;
    private Double deepSampleEntropyUpperThreshold;
    private Double remSampleEntropyLowerThreshold;
    private Integer hysteresisRequiredSegments;
    private Integer sleepOnsetRequiredSegments;
    private Integer wakeUpRequiredSegments;
    private Double sleepOnsetHeartRateDeltaThreshold;
    private Double wakeUpHeartRateDeltaThreshold;
    private Integer minStableSleepMinutesForOnset;
    private Integer minStableWakeMinutesForWakeUp;
    private Integer daytimeNapMinMinutes;
    private Integer daytimeNapMaxAlignedStepThreshold;
    private Integer daytimeNapRequiredSegments;
    private Integer daytimeNapMergeGapMinutes;
    private Integer motionAlignmentLookbackMinutes;
    private Integer motionAlignmentLookaheadMinutes;
    private Double lowAlignmentConfidenceMotionWeight;
}
