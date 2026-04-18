package com.sleep.platform.engine;

import com.sleep.platform.config.SleepAnalysisProperties;
import com.sleep.platform.domain.model.MotionAlignmentResult;
import com.sleep.platform.domain.model.QualityGateResult;
import com.sleep.platform.domain.request.PhysiologicalSegmentInput;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class QualityGateEngine {

    private final SleepAnalysisProperties sleepAnalysisProperties;

    public QualityGateEngine(SleepAnalysisProperties sleepAnalysisProperties) {
        this.sleepAnalysisProperties = sleepAnalysisProperties;
    }

    public QualityGateResult evaluate(PhysiologicalSegmentInput physiologicalSegmentInput,
                                      MotionAlignmentResult motionAlignmentResult) {
        QualityGateResult result = new QualityGateResult();
        List<String> tags = new ArrayList<>();

        int rawCount = physiologicalSegmentInput.getRriMsList() == null ? 0 : physiologicalSegmentInput.getRriMsList().size();
        int nullCount = 0;
        if (physiologicalSegmentInput.getRriMsList() != null) {
            for (Double value : physiologicalSegmentInput.getRriMsList()) {
                if (value == null) {
                    nullCount++;
                }
            }
        }
        double missingRate = rawCount == 0 ? 1.0 : nullCount * 1.0 / rawCount;
        boolean enoughRri = rawCount >= sleepAnalysisProperties.getMinValidRriCount();
        boolean missingRatePassed = missingRate <= sleepAnalysisProperties.getMaxAllowedMissingRate();
        boolean hrAvailable = physiologicalSegmentInput.getAverageHeartRateBpm() != null;

        result.setMotionAlignmentAvailable(motionAlignmentResult.getMotionAlignmentConfidence() > 0.0);
        result.setMotionAlignmentConfidence(motionAlignmentResult.getMotionAlignmentConfidence());
        if (result.isMotionAlignmentAvailable()) {
            if (motionAlignmentResult.getMotionAlignmentConfidence() >= 0.7) {
                tags.add("MOTION_ALIGNMENT_HIGH_CONFIDENCE");
            } else if (motionAlignmentResult.getMotionAlignmentConfidence() >= 0.4) {
                tags.add("MOTION_ALIGNMENT_MEDIUM_CONFIDENCE");
            } else {
                tags.add("MOTION_ALIGNMENT_LOW_CONFIDENCE");
            }
        } else {
            tags.add("MOTION_ALIGNMENT_UNAVAILABLE");
        }

        boolean passed = enoughRri && missingRatePassed && hrAvailable;
        result.setQualityPassed(passed);
        result.setQualityRemark(buildRemark(enoughRri, missingRatePassed, hrAvailable, missingRate, rawCount,
                result.isMotionAlignmentAvailable(), result.getMotionAlignmentConfidence()));
        result.setExplainTags(tags);
        return result;
    }

    private String buildRemark(boolean enoughRri, boolean missingRatePassed, boolean hrAvailable,
                               double missingRate, int rawCount, boolean motionAvailable, double motionConfidence) {
        return "RRI原始数量=" + rawCount
                + "，缺失率=" + String.format("%.3f", missingRate)
                + "，RRI数量通过=" + enoughRri
                + "，缺失率通过=" + missingRatePassed
                + "，平均心率可用=" + hrAvailable
                + "，运动对齐可用=" + motionAvailable
                + "，运动对齐置信度=" + String.format("%.3f", motionConfidence);
    }
}
