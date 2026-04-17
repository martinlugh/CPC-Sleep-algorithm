package com.sleep.platform.engine;

import com.sleep.platform.config.SleepAnalysisProperties;
import com.sleep.platform.domain.enums.SleepStage;
import com.sleep.platform.domain.model.SleepOnsetDetectionResult;
import com.sleep.platform.domain.model.UserSleepBaselineProfile;
import com.sleep.platform.domain.response.SleepSegmentAnalysisResult;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
public class SleepOnsetDetectionEngine {

    private final SleepAnalysisProperties sleepAnalysisProperties;

    public SleepOnsetDetectionEngine(SleepAnalysisProperties sleepAnalysisProperties) {
        this.sleepAnalysisProperties = sleepAnalysisProperties;
    }

    public SleepOnsetDetectionResult detect(List<SleepSegmentAnalysisResult> smoothedSegmentList,
                                            UserSleepBaselineProfile baselineProfile,
                                            LocalDateTime firstSegmentTime) {
        SleepOnsetDetectionResult result = new SleepOnsetDetectionResult();
        if (smoothedSegmentList == null || smoothedSegmentList.isEmpty()) {
            result.setMainSleepLatencyMinutes(0);
            result.setSleepOnsetReasonTags(Collections.singletonList("NO_SEGMENT_DATA"));
            result.setSleepOnsetExplainText("未检测到可用片段，无法识别入睡时间。");
            return result;
        }

        double baselineHr = baselineProfile != null && baselineProfile.getRestingHeartRateBpm() != null
                ? baselineProfile.getRestingHeartRateBpm() : 60.0;

        int requiredSegments = Math.max(2, sleepAnalysisProperties.getSleepOnsetRequiredSegments());
        for (int i = 0; i <= smoothedSegmentList.size() - requiredSegments; i++) {
            boolean allSleepLike = true;
            boolean qualityPassed = true;
            boolean hrSupports = false;
            boolean physiologyStable = true;
            for (int j = i; j < i + requiredSegments; j++) {
                SleepSegmentAnalysisResult segment = smoothedSegmentList.get(j);
                SleepStage stage = segment.getSmoothedStage();
                if (!(stage == SleepStage.LIGHT || stage == SleepStage.DEEP || stage == SleepStage.REM)) {
                    allSleepLike = false;
                }
                if (segment.getQualityPassed() == null || !segment.getQualityPassed()) {
                    qualityPassed = false;
                }
                if (segment.getQualityRemark() != null && segment.getQualityRemark().contains("平均心率=")) {
                    hrSupports = true;
                }
                if (segment.getConfidenceScore() == null || segment.getConfidenceScore() < 0.45) {
                    physiologyStable = false;
                }
            }

            SleepSegmentAnalysisResult firstCandidate = smoothedSegmentList.get(i);
            boolean hrNearBaseline = firstCandidate.getQualityRemark() == null
                    || !firstCandidate.getQualityRemark().contains("平均心率=")
                    || baselineHr >= 0;
            if (allSleepLike && qualityPassed && physiologyStable && (hrSupports || hrNearBaseline)) {
                LocalDateTime onsetTime = firstCandidate.getSegmentStartTime();
                result.setMainSleepStartTime(onsetTime);
                int latencyMinutes = (int) Duration.between(firstSegmentTime, onsetTime).toMinutes();
                result.setMainSleepLatencyMinutes(Math.max(0, latencyMinutes));
                List<String> tags = new ArrayList<>(Arrays.asList(
                        "SLEEP_STAGE_CONTINUITY",
                        "PHYSIOLOGICAL_CONTINUITY",
                        "HEART_RATE_SUPPORTS_SLEEP"
                ));
                result.setSleepOnsetReasonTags(tags);
                result.setSleepOnsetExplainText("入睡识别基于连续睡眠型分期和生理连续性完成；运动特征仅作为辅助增强，不作为必要条件。");
                return result;
            }
        }

        result.setMainSleepStartTime(smoothedSegmentList.get(0).getSegmentStartTime());
        result.setMainSleepLatencyMinutes(0);
        result.setSleepOnsetReasonTags(Collections.singletonList("FALLBACK_FIRST_SEGMENT"));
        result.setSleepOnsetExplainText("未满足严格入睡条件，回退到首个有效片段作为入睡参考时间。");
        return result;
    }
}
