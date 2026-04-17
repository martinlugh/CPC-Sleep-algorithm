package com.sleep.platform.domain.model;

import com.sleep.platform.domain.enums.SleepStage;
import lombok.Data;

@Data
public class SleepCalibrationResult {

    private SleepStage stageAfterCalibration;
    private SleepStage smoothedStage;
    private double confidenceScore;
}
