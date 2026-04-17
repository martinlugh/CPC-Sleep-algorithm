package com.sleep.platform.engine;

import com.sleep.platform.domain.enums.SleepStage;
import com.sleep.platform.domain.model.CpcAnalysisResult;
import com.sleep.platform.domain.model.QualityGateResult;
import com.sleep.platform.domain.model.SleepExplainabilityResult;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SleepExplainabilityEngine {

    public SleepExplainabilityResult build(SleepStage finalStage,
                                           double averageHeartRateBpm,
                                           double sampleEntropy,
                                           double sd2Sd1Ratio,
                                           CpcAnalysisResult cpcAnalysisResult,
                                           QualityGateResult qualityGateResult,
                                           double alignedStepsInFiveMinutes) {
        SleepExplainabilityResult result = new SleepExplainabilityResult();
        List<String> tags = new ArrayList<>();
        StringBuilder text = new StringBuilder();

        if (qualityGateResult.getMotionAlignmentConfidence() < 0.4) {
            tags.add("LOW_ALIGNMENT_CONFIDENCE");
            text.append("当前片段运动时间对齐置信度较低，运动证据仅作弱参考；");
        } else {
            tags.add("HIGH_ALIGNMENT_CONFIDENCE");
            text.append("当前片段运动时间对齐置信度较高，可用于边界增强；");
        }

        if (alignedStepsInFiveMinutes <= 6.0) {
            tags.add("ALIGNED_LOW_MOTION");
            text.append("对齐后步数偏低；");
        } else if (alignedStepsInFiveMinutes >= 20.0) {
            tags.add("ALIGNED_HIGH_MOTION");
            text.append("对齐后步数偏高；");
        }

        if (cpcAnalysisResult.getHfcPower() > cpcAnalysisResult.getLfcPower()) {
            tags.add("PARASYMPATHETIC_DOMINANT");
            text.append("HFC功率占优提示副交感主导；");
        } else {
            tags.add("SYMPATHETIC_SHIFT");
            text.append("LFC偏高提示交感活动增强；");
        }

        tags.add("PHYSIOLOGY_PRIMARY_EVIDENCE");
        text.append("分期最终以生理特征为主，运动证据为辅助。")
                .append("当前阶段=").append(finalStage.name())
                .append("，平均心率=").append(String.format("%.1f", averageHeartRateBpm))
                .append("，样本熵=").append(String.format("%.3f", sampleEntropy))
                .append("，SD2/SD1=").append(String.format("%.3f", sd2Sd1Ratio)).append("。");

        result.setExplainTags(tags);
        result.setExplainText(text.toString());
        return result;
    }
}
