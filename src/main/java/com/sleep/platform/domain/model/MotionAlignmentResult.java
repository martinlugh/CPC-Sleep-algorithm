package com.sleep.platform.domain.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MotionAlignmentResult {

    private Double alignedStepsInFiveMinutes;
    private Double motionAlignmentConfidence;
    private String alignmentReason;
    private LocalDateTime matchedMotionSegmentStartTime;
}
