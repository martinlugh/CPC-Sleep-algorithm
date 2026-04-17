package com.sleep.platform.engine;

import com.sleep.platform.config.SleepAnalysisProperties;
import com.sleep.platform.domain.enums.SleepStage;
import com.sleep.platform.domain.model.WakeUpDetectionResult;
import com.sleep.platform.domain.response.SleepSegmentAnalysisResult;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
public class WakeUpDetectionEngine {

    private final SleepAnalysisProperties sleepAnalysisProperties;

    public WakeUpDetectionEngine(SleepAnalysisProperties sleepAnalysisProperties) {
        this.sleepAnalysisProperties = sleepAnalysisProperties;
    }

    public WakeUpDetectionResult detect(List<SleepSegmentAnalysisResult> smoothedSegmentList, int sleepStartIndex) {
        WakeUpDetectionResult result = new WakeUpDetectionResult();
        if (smoothedSegmentList == null || smoothedSegmentList.isEmpty()) {
            result.setWakeUpReasonTags(Collections.singletonList("NO_SEGMENT_DATA"));
            result.setWakeUpExplainText("无可用片段，无法识别醒来时间。");
            return result;
        }

        int requiredWakeSegments = Math.max(2, sleepAnalysisProperties.getWakeUpRequiredSegments());
        int searchFrom = Math.max(sleepStartIndex + 1, smoothedSegmentList.size() / 2);

        for (int i = smoothedSegmentList.size() - requiredWakeSegments; i >= searchFrom; i--) {
            boolean wakeContinuity = true;
            boolean physiologySupport = true;
            for (int j = i; j < i + requiredWakeSegments; j++) {
                SleepSegmentAnalysisResult segment = smoothedSegmentList.get(j);
                if (segment.getSmoothedStage() != SleepStage.WAKE) {
                    wakeContinuity = false;
                }
                if (segment.getConfidenceScore() == null || segment.getConfidenceScore() < 0.50) {
                    physiologySupport = false;
                }
            }
            if (wakeContinuity && physiologySupport) {
                result.setMainSleepWakeUpTime(smoothedSegmentList.get(i).getSegmentStartTime());
                result.setWakeUpReasonTags(new ArrayList<>(Arrays.asList(
                        "WAKE_STAGE_CONTINUITY",
                        "PHYSIOLOGY_DOMINANT_CONFIRMATION",
                        "SHORT_AWAKEN_EXCLUDED"
                )));
                result.setWakeUpExplainText("醒来识别优先依据尾部连续清醒分期与生理变化确认，已排除夜间短暂觉醒干扰；运动仅为辅助证据。");
                return result;
            }
        }

        result.setMainSleepWakeUpTime(smoothedSegmentList.get(smoothedSegmentList.size() - 1).getSegmentStartTime());
        result.setWakeUpReasonTags(Collections.singletonList("FALLBACK_LAST_SEGMENT"));
        result.setWakeUpExplainText("未找到满足条件的连续清醒区间，回退至最后片段作为醒来参考时间。");
        return result;
    }
}
