package com.sleep.platform.domain.model;

import com.sleep.platform.domain.enums.SleepStage;
import lombok.Data;

@Data
public class SleepDecisionResult {

    private SleepStage stageBeforeCalibration;
    private double confidenceScore;
}
