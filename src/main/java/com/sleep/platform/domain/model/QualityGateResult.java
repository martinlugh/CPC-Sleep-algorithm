package com.sleep.platform.domain.model;

import lombok.Data;

import java.util.List;

@Data
public class QualityGateResult {

    private boolean qualityPassed;
    private String qualityRemark;
    private boolean motionAlignmentAvailable;
    private double motionAlignmentConfidence;
    private List<String> explainTags;
}
