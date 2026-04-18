package com.sleep.platform.domain.response;

import com.sleep.platform.domain.enums.SleepStage;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SleepSegmentAnalysisResult {

    private LocalDateTime segmentStartTime;
    private Integer rawRriCount;
    private Integer cleanedRriCount;
    private Double hfcPower;
    private Double lfcPower;
    private Double vlfcPower;
    private Double hfcLfcRatio;
    private Double sd1Ms;
    private Double sd2Ms;
    private Double sd2Sd1Ratio;
    private Double sampleEntropy;
    private SleepStage stageBeforeCalibration;
    private SleepStage stageAfterCalibration;
    private SleepStage smoothedStage;
    private Double confidenceScore;
    private Boolean qualityPassed;
    private String qualityRemark;
}
