package com.sleep.platform.domain.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PhysiologicalMotionMergedSegment {

    private LocalDateTime segmentStartTime;
    private double[] rriMsArray;
    private Double averageHeartRateBpm;
    private Double alignedStepsInFiveMinutes;
    private Double motionAlignmentConfidence;
    private String alignmentReason;
}
