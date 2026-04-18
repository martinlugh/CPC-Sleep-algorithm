package com.sleep.platform.domain.model;

import lombok.Data;

import java.util.Map;

@Data
public class SleepScoreResult {

    private Double mainSleepQualityScore;
    private Double nightlyRecoveryScore;
    private Double nightlyFatigueScore;
    private Double dataQualityScore;
    private Map<String, Object> scoreExplanation;
}
